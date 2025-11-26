package com.dus.pipeline.retry;

import java.util.HashSet;
import java.util.Set;

/**
 * 固定间隔重试策略
 */
public class FixedDelayRetryStrategy implements RetryStrategy {
    
    private final int maxAttempts;          // 最大重试次数
    private final long delayMs;             // 固定延迟时间
    private final Set<Class<?>> retryableExceptions;  // 可重试的异常类型
    
    public FixedDelayRetryStrategy(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
        this.retryableExceptions = new HashSet<>();
        this.retryableExceptions.add(Exception.class);
    }
    
    /**
     * 添加可重试的异常类型
     */
    public FixedDelayRetryStrategy addRetryableException(Class<?> exceptionClass) {
        retryableExceptions.add(exceptionClass);
        return this;
    }
    
    /**
     * 移除可重试的异常类型
     */
    public FixedDelayRetryStrategy removeRetryableException(Class<?> exceptionClass) {
        retryableExceptions.remove(exceptionClass);
        return this;
    }
    
    @Override
    public boolean shouldRetry(int attemptCount, Exception exception) {
        if (attemptCount >= maxAttempts) {
            return false;
        }
        
        // 检查异常是否可重试
        return retryableExceptions.stream()
            .anyMatch(e -> e.isInstance(exception));
    }
    
    @Override
    public long getWaitTimeMs(int attemptCount) {
        return delayMs;
    }
    
    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    @Override
    public String name() {
        return String.format("FixedDelayRetryStrategy(%d attempts, %d ms delay)", 
            maxAttempts, delayMs);
    }
}