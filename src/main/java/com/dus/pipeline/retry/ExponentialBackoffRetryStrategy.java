package com.dus.pipeline.retry;

import java.util.HashSet;
import java.util.Set;

/**
 * 指数退避重试策略
 */
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    
    private final int maxAttempts;
    private final long initialDelayMs;      // 初始延迟
    private final long maxDelayMs;          // 最大延迟
    private final double multiplier;        // 延迟倍数
    private final Set<Class<?>> retryableExceptions;
    
    public ExponentialBackoffRetryStrategy(
        int maxAttempts, 
        long initialDelayMs, 
        long maxDelayMs,
        double multiplier) {
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.multiplier = multiplier;
        this.retryableExceptions = new HashSet<>();
        this.retryableExceptions.add(Exception.class);
    }
    
    /**
     * 添加可重试的异常类型
     */
    public ExponentialBackoffRetryStrategy addRetryableException(Class<?> exceptionClass) {
        retryableExceptions.add(exceptionClass);
        return this;
    }
    
    /**
     * 移除可重试的异常类型
     */
    public ExponentialBackoffRetryStrategy removeRetryableException(Class<?> exceptionClass) {
        retryableExceptions.remove(exceptionClass);
        return this;
    }
    
    @Override
    public boolean shouldRetry(int attemptCount, Exception exception) {
        if (attemptCount >= maxAttempts) {
            return false;
        }
        
        return retryableExceptions.stream()
            .anyMatch(e -> e.isInstance(exception));
    }
    
    @Override
    public long getWaitTimeMs(int attemptCount) {
        // 延迟 = initialDelay * (multiplier ^ (attemptCount - 1))
        long delay = (long) (initialDelayMs * Math.pow(multiplier, attemptCount - 1));
        return Math.min(delay, maxDelayMs);
    }
    
    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    @Override
    public String name() {
        return String.format("ExponentialBackoffRetryStrategy(%d attempts, multiplier=%.2f)", 
            maxAttempts, multiplier);
    }
}