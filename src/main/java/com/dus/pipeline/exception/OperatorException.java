package com.dus.pipeline.exception;

/**
 * 算子执行异常
 */
public class OperatorException extends PipelineException {
    
    private int attemptCount;
    
    public OperatorException(String message, Exception cause) {
        super(message, cause);
    }
    
    public OperatorException(String message, Exception cause, int attemptCount) {
        super(message, cause);
        this.attemptCount = attemptCount;
    }
    
    public OperatorException(String message) {
        super(message);
    }
    
    public OperatorException(Exception cause) {
        super(cause);
    }
    
    /**
     * 获取尝试次数
     */
    public int getAttemptCount() {
        return attemptCount;
    }
    
    /**
     * 设置尝试次数
     */
    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (attemptCount > 0) {
            sb.append(" (attempt ").append(attemptCount).append(")");
        }
        return sb.toString();
    }
}