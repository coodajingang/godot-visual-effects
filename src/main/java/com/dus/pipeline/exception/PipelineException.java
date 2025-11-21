package com.dus.pipeline.exception;

/**
 * Pipeline 框架基础异常类
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