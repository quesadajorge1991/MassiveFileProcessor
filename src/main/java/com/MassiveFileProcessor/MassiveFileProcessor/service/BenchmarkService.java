package com.MassiveFileProcessor.MassiveFileProcessor.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.MassiveFileProcessor.MassiveFileProcessor.model.BenchmarkResult;
import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        
        // Crear archivo de prueba - 10,000 registros para benchmark rápido
        File testFile = createTestFile(10000);
        
        // Verificar que el archivo se creó correctamente
        if (testFile == null || !testFile.exists() || testFile.length() == 0) {
            log.error("No se pudo crear el archivo de prueba");
            return results;
        }
        
        log.info("Archivo de prueba creado: {} bytes, {} registros", testFile.length(), 10000);
        
        // Probar con diferentes números de chunks
        int[] chunkConfigs = {1, 2, 4, 8};
        long baselineTime = 0;
        
        for (int chunks : chunkConfigs) {
            log.info("Probando con {} chunks...", chunks);
            
            // Crear una copia del archivo para cada prueba
            File testFileCopy = new File(System.getProperty("java.io.tmpdir"), "benchmark_" + chunks + ".csv");
            copyFile(testFile, testFileCopy);
            
            long startTime = System.nanoTime();
            String processId = fileProcessorService.processFileAsync(testFileCopy, chunks);
            
            // Esperar resultado
            long timeout = 120;
            long startWait = System.currentTimeMillis();
            ProcessResult result = null;
            
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
            log.info("Chunks {}: {} ms, speedup: {}x, rows: {}", chunks, timeMs, speedup, 
                result != null ? result.getTotalRows() : 0);
        }
        
        // Limpiar archivo original
        testFile.delete();
        
        return results;
    }
    
    private File createTestFile(int rows) throws IOException {
        File file = File.createTempFile("benchmark_", ".csv");
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            // Escribir encabezado
            writer.write("id,nombre,email,edad");
            writer.newLine();
            
            // Escribir datos
            for (int i = 1; i <= rows; i++) {
                String line = String.format("%d,Usuario%d,user%d@test.com,%d",
                    i, i, i, 20 + (i % 50));
                writer.write(line);
                writer.newLine();
                
                // Flush cada 10,000 registros
                if (i % 10000 == 0) {
                    writer.flush();
                    log.info("Generados {} registros...", i);
                }
            }
            
            writer.flush();
        }
        
        // Verificar que el archivo no esté vacío
        if (file.length() == 0) {
            throw new IOException("El archivo de prueba se creó vacío");
        }
        
        log.info("Archivo de prueba creado: {} registros, {} bytes", rows, file.length());
        return file;
    }
    
    private void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
