package dev.logMonitor.dto;

import java.time.Instant;

public record LogEvent (
        String serviceName,
        String logLevel,
        String message,
        String stackTrace,
        String traceId,
        Instant timestamp
){
    public LogEvent{
        if(timestamp == null){
            timestamp = Instant.now();
        }
    }


    public Object getServiceName() {
        return serviceName;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Object getTraceId() {
        return traceId;
    }

    public Object getMessage() {
        return message;
    }

    public Object getStackTrace() {
        return stackTrace;
    }
}
