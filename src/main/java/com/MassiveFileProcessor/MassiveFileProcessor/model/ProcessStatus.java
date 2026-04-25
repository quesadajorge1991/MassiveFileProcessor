package com.MassiveFileProcessor.MassiveFileProcessor.model;


import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

@Data
public class ProcessStatus {

     private String processId;
    private String fileName;
    private String status;
    private int totalChunks;
    private AtomicInteger completedChunks = new AtomicInteger(0);
    private int progress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String errorMessage;
    
    public void incrementChunks() {
        int completed = completedChunks.incrementAndGet();
        if (totalChunks > 0) {
            this.progress = (completed * 100) / totalChunks;
        }
        if (this.progress >= 100) {
            this.status = "COMPLETED";
            this.endTime = LocalDateTime.now();
        }
    }

}
