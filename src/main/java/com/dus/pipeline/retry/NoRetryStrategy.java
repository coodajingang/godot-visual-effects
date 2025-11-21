package com.dus.pipeline.retry;

/**
 * 不重试策略
 */
public class NoRetryStrategy implements RetryStrategy {
    
    @Override
    public boolean shouldRetry(int attemptCount, Exception exception) {
        return false;
    }
    
    @Override
    public long getWaitTimeMs(int attemptCount) {
        return 0;
    }
    
    @Override
    public int getMaxAttempts() {
        return 1;
    }
    
    @Override
    public String name() {
        return "NoRetryStrategy";
    }
}