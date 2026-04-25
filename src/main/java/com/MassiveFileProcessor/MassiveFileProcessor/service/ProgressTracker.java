package com.MassiveFileProcessor.MassiveFileProcessor.service;


import org.springframework.stereotype.Service;

import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessResult;
import com.MassiveFileProcessor.MassiveFileProcessor.model.ProcessStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProgressTracker {

     private final Map<String, ProcessStatus> activeProcesses = new ConcurrentHashMap<>();
    private final Map<String, ProcessResult> completedProcesses = new ConcurrentHashMap<>();
    
    public void registerProcess(String processId, String fileName, int totalChunks) {
        ProcessStatus status = new ProcessStatus();
        status.setProcessId(processId);
        status.setFileName(fileName);
        status.setTotalChunks(totalChunks);
        status.setStatus("RUNNING");
        status.setStartTime(LocalDateTime.now());  // ← IMPORTANTE: asignar startTime
        status.setCompletedChunks(new AtomicInteger(0));
        status.setProgress(0);
        activeProcesses.put(processId, status);
    }
    
    public void updateProgress(String processId) {
        ProcessStatus status = activeProcesses.get(processId);
        if (status != null) {
            status.incrementChunks();
        }
    }
    
    public ProcessStatus getStatus(String processId) {
        ProcessStatus status = activeProcesses.get(processId);
        if (status == null) {
            status = new ProcessStatus();
            status.setStatus("NOT_FOUND");
        }
        return status;
    }
    
    public void completeProcess(String processId, ProcessResult result) {
        ProcessStatus status = activeProcesses.remove(processId);
        if (status != null) {
            result.setProcessId(processId);
            result.setFileName(status.getFileName());
            
            // Calcular tiempo total de forma segura
            if (status.getStartTime() != null) {
                long totalTimeMs = Duration.between(status.getStartTime(), LocalDateTime.now()).toMillis();
                result.setTotalTimeMs(totalTimeMs);
            } else {
                result.setTotalTimeMs(0);
            }
        }
        completedProcesses.put(processId, result);
    }
    
    public void failProcess(String processId, String error) {
        ProcessStatus status = activeProcesses.get(processId);
        if (status != null) {
            status.setStatus("FAILED");
            status.setErrorMessage(error);
        }
    }
    
    public ProcessResult getResult(String processId) {
        return completedProcesses.get(processId);
    }

}
