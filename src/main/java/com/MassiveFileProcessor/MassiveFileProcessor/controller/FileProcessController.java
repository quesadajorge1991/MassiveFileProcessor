package com.MassiveFileProcessor.MassiveFileProcessor.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.MassiveFileProcessor.MassiveFileProcessor.model.BenchmarkResult;
import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessResult;
import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessStatus;
import com.MassiveFileProcessor.MassiveFileProcessor.service.BenchmarkService;
import com.MassiveFileProcessor.MassiveFileProcessor.service.FileProcessorService;
import com.MassiveFileProcessor.MassiveFileProcessor.service.ProgressTracker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileProcessController {


    
    private final FileProcessorService fileProcessorService;
    private final ProgressTracker progressTracker;
    private final BenchmarkService benchmarkService;
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "chunks", defaultValue = "8") int chunks) throws IOException {
        
        // Guardar archivo temporal
        File tempFile = File.createTempFile("upload_", ".csv");
        file.transferTo(tempFile);
        
        String processId = fileProcessorService.processFileAsync(tempFile, chunks);
        
        Map<String, String> response = new HashMap<>();
        response.put("processId", processId);
        response.put("message", "Archivo recibido. Procesamiento iniciado.");
        response.put("chunks", String.valueOf(chunks));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessStatus> getStatus(@PathVariable String processId) {
        ProcessStatus status = progressTracker.getStatus(processId);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/result/{processId}")
    public ResponseEntity<ProcessResult> getResult(@PathVariable String processId) {
        ProcessResult result = progressTracker.getResult(processId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/benchmark")
    public ResponseEntity<List<BenchmarkResult>> runBenchmark() throws Exception {
        List<BenchmarkResult> results = benchmarkService.runBenchmark();
        return ResponseEntity.ok(results);
    }

}
