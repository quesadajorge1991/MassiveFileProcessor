package com.MassiveFileProcessor.MassiveFileProcessor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BenchmarkResult {

    private int threads;
    private long timeMs;
    private long rowsProcessed;
    private double throughputPerSec;
    private double speedup; // vs baseline (1 thread)

}
