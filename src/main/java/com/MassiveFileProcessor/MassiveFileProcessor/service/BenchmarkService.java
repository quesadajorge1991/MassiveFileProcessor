package com.MassiveFileProcessor.MassiveFileProcessor.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.MassiveFileProcessor.MassiveFileProcessor.model.BenchmarkResult;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenchmarkService {

   private final FileProcessorService fileProcessorService;
    private final ProgressTracker progressTracker;  
    
    public List<BenchmarkResult> runBenchmark() throws Exception {
        log.info("Iniciando benchmark de rendimiento...");
        List<BenchmarkResult> results = new ArrayList<>();
        
        // Crear archivo de prueba
        File testFile = createTestFile(100000); // 100k registros
        
        // Probar con diferentes números de chunks (simulan diferentes niveles de paralelismo)
        int[] chunkConfigs = {1, 2, 4, 8, 16};
        long baselineTime = 0;
        
        for (int chunks : chunkConfigs) {
            log.info("Probando con {} chunks...", chunks);
            
            long startTime = System.nanoTime();
            String processId = fileProcessorService.processFileAsync(testFile, chunks);
            
            // Esperar resultado
            long timeout = 120;
            long startWait = System.currentTimeMillis();
            com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessResult result = null;
            
            while (System.currentTimeMillis() - startWait < timeout * 1000) {
                result = progressTracker.getResult(processId);
                if (result != null) break;
                Thread.sleep(1000);
            }
            
            long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            
            if (chunks == 1) {
                baselineTime = timeMs;
            }
            
            double speedup = baselineTime > 0 ? (double) baselineTime / timeMs : 1.0;
            
            BenchmarkResult benchmarkResult = BenchmarkResult.builder()
                .threads(chunks)
                .timeMs(timeMs)
                .rowsProcessed(result != null ? result.getTotalRows() : 0)
                .throughputPerSec(result != null ? result.getThroughputPerSec() : 0)
                .speedup(speedup)
                .build();
            
            results.add(benchmarkResult);
            log.info("Chunks {}: {} ms, speedup: {}x", chunks, timeMs, speedup);
        }
        
        testFile.delete();
        return results;
    }
    
    private File createTestFile(int rows) throws IOException {
        File file = File.createTempFile("benchmark_", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,nombre,email,edad\n");
            for (int i = 1; i <= rows; i++) {
                writer.write(i + ",Usuario" + i + ",user" + i + "@test.com," + (20 + (i % 50)) + "\n");
                if (i % 10000 == 0) {
                    writer.flush();
                }
            }
        }
        log.info("Archivo de prueba creado: {} filas, {} bytes", rows, file.length());
        return file;
    }
}
