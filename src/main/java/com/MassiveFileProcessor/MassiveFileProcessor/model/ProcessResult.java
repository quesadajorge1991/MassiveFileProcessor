package com.MassiveFileProcessor.MassiveFileProcessor.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessResult {

    private String processId;
    private String fileName;
    private long totalRows;
    private long validRows;
    private long invalidRows;
    private long totalTimeMs;  // ← Asegurar que existe
    private double throughputPerSec;
    private int threadsUsed;
    private double speedup;
    
    @Builder.Default
    private List<String> sampleErrors = new ArrayList<>();
    
    private double avgProcessingTimePerChunk;
    private long maxChunkTimeMs;
    private long minChunkTimeMs;

}
