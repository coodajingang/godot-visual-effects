package com.dus.pipeline.exception;

/**
 * Pipeline 自定义异常
 */
public class PipelineException extends RuntimeException {
    
    public PipelineException(String message) {
        super(message);
    }
    
    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PipelineException(Throwable cause) {
        super(cause);
    }
}