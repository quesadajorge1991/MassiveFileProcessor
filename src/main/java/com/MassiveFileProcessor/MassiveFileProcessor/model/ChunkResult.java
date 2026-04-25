package com.MassiveFileProcessor.MassiveFileProcessor.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkResult {

    private int chunkIndex;
    private long rowsProcessed;
    private long validRows;
    private long invalidRows;
    private long processingTimeMs;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    // Para métricas acumulables
    private double sumValue; // Para promedios

}
