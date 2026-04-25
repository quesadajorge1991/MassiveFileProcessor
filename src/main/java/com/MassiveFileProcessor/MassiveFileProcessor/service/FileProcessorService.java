package com.MassiveFileProcessor.MassiveFileProcessor.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.MassiveFileProcessor.MassiveFileProcessor.model.ChunkResult;
import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessResult;
import com.MassiveFileProcessor.MassiveFileProcessor.util.FileSplitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessorService {

    private final FileSplitter fileSplitter;
    private final ProgressTracker progressTracker;
    
    @Qualifier("fileProcessorExecutor")
    private final Executor executor;
    
    public String processFileAsync(File file, int chunks) {
        String processId = UUID.randomUUID().toString();
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return processFile(processId, file, chunks);
            } catch (Exception e) {
                log.error("Error procesando archivo", e);
                progressTracker.failProcess(processId, e.getMessage());
                return null;
            }
        }, executor);
        
        return processId;
    }
    
    private ProcessResult processFile(String processId, File file, int chunks) throws Exception {
        long startTime = System.nanoTime();
        log.info("Iniciando procesamiento de {} con {} chunks", file.getName(), chunks);
        
        // Dividir archivo
        List<long[]> chunkPositions = fileSplitter.splitFile(file, chunks);
        progressTracker.registerProcess(processId, file.getName(), chunkPositions.size());
        
        // Procesar chunks en paralelo
        List<CompletableFuture<ChunkResult>> futures = new ArrayList<>();
        
        for (int i = 0; i < chunkPositions.size(); i++) {
            final int chunkIndex = i;
            final long[] positions = chunkPositions.get(i);
            
            CompletableFuture<ChunkResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processChunk(file, positions, chunkIndex);
                } catch (Exception e) {
                    log.error("Error en chunk {}", chunkIndex, e);
                    return ChunkResult.builder()
                        .chunkIndex(chunkIndex)
                        .rowsProcessed(0)
                        .validRows(0)
                        .invalidRows(0)
                        .build();
                }
            }, executor);
            
            future.thenRun(() -> progressTracker.updateProgress(processId));
            futures.add(future);
        }
        
        // Esperar todos los chunks
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .get(30, TimeUnit.MINUTES);
        
        // Consolidar resultados
        List<ChunkResult> results = new ArrayList<>();
        for (CompletableFuture<ChunkResult> future : futures) {
            results.add(future.get());
        }
        
        ProcessResult finalResult = mergeResults(results);
        
        long totalTimeNs = System.nanoTime() - startTime;
        long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(totalTimeNs);
        finalResult.setTotalTimeMs(totalTimeMs);
        finalResult.setThroughputPerSec((double) finalResult.getTotalRows() / (totalTimeMs / 1000.0));
        finalResult.setThreadsUsed(Runtime.getRuntime().availableProcessors());
        
        progressTracker.completeProcess(processId, finalResult);
        log.info("Procesamiento completado en {} ms. Throughput: {} rows/seg", 
            totalTimeMs, finalResult.getThroughputPerSec());
        
        // Limpiar archivo temporal
        file.delete();
        
        return finalResult;
    }
    
    private ChunkResult processChunk(File file, long[] positions, int chunkIndex) throws IOException {
        long chunkStartTime = System.nanoTime();
        long validRows = 0;
        long invalidRows = 0;
        List<String> errors = new ArrayList<>();
        
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(positions[0]);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(raf.getFD()), StandardCharsets.UTF_8))) {
                
                long bytesRead = 0;
                String line;
                boolean firstLine = true;
                
                while ((line = reader.readLine()) != null && raf.getFilePointer() <= positions[1]) {
                    if (firstLine && chunkIndex > 0) {
                        firstLine = false;
                        continue; // Saltar línea de encabezado en chunks no iniciales
                    }
                    
                    // Procesar línea (simulación de validación)
                    if (isValidLine(line)) {
                        validRows++;
                    } else {
                        invalidRows++;
                        if (errors.size() < 100) {
                            errors.add("Error en línea: " + line.substring(0, Math.min(50, line.length())));
                        }
                    }
                    
                    bytesRead = raf.getFilePointer() - positions[0];
                }
            }
        }
        
        long processingTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - chunkStartTime);
        
        return ChunkResult.builder()
            .chunkIndex(chunkIndex)
            .rowsProcessed(validRows + invalidRows)
            .validRows(validRows)
            .invalidRows(invalidRows)
            .processingTimeMs(processingTimeMs)
            .errors(errors)
            .build();
    }
    
    private boolean isValidLine(String line) {
        // Validación simple: línea no vacía y con al menos 3 campos
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        String[] fields = line.split(",");
        return fields.length >= 3;
    }
    
    private ProcessResult mergeResults(List<ChunkResult> results) {
        long totalRows = 0;
        long totalValid = 0;
        long totalInvalid = 0;
        long totalProcessingTime = 0;
        long maxChunkTime = 0;
        long minChunkTime = Long.MAX_VALUE;
        List<String> allErrors = new ArrayList<>();
        
        for (ChunkResult r : results) {
            totalRows += r.getRowsProcessed();
            totalValid += r.getValidRows();
            totalInvalid += r.getInvalidRows();
            totalProcessingTime += r.getProcessingTimeMs();
            maxChunkTime = Math.max(maxChunkTime, r.getProcessingTimeMs());
            minChunkTime = Math.min(minChunkTime, r.getProcessingTimeMs());
            
            for (String error : r.getErrors()) {
                if (allErrors.size() < 20) {
                    allErrors.add(error);
                }
            }
        }
        
        return ProcessResult.builder()
            .totalRows(totalRows)
            .validRows(totalValid)
            .invalidRows(totalInvalid)
            .sampleErrors(allErrors)
            .avgProcessingTimePerChunk((double) totalProcessingTime / results.size())
            .maxChunkTimeMs(maxChunkTime)
            .minChunkTimeMs(minChunkTime == Long.MAX_VALUE ? 0 : minChunkTime)
            .build();
    }

}
