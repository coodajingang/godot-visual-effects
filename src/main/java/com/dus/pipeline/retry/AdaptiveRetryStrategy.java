package com.dus.pipeline.retry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 自适应重试策略（根据异常类型调整策略）
 */
public class AdaptiveRetryStrategy implements RetryStrategy {
    
    private final Map<Class<?>, Integer> exceptionRetryCount;  // 异常类型 → 重试次数
    private final Map<Class<?>, Long> exceptionDelayMs;        // 异常类型 → 延迟时间
    private int defaultMaxAttempts = 1;
    private long defaultDelayMs = 0;
    
    public AdaptiveRetryStrategy() {
        this.exceptionRetryCount = new HashMap<>();
        this.exceptionDelayMs = new HashMap<>();
    }
    
    /**
     * 设置默认重试参数
     */
    public AdaptiveRetryStrategy withDefaults(int maxAttempts, long delayMs) {
        this.defaultMaxAttempts = maxAttempts;
        this.defaultDelayMs = delayMs;
        return this;
    }
    
    /**
     * 配置某个异常类型的重试策略
     */
    public AdaptiveRetryStrategy configureException(Class<?> exceptionClass, int retryCount, long delayMs) {
        exceptionRetryCount.put(exceptionClass, retryCount);
        exceptionDelayMs.put(exceptionClass, delayMs);
        return this;
    }
    
    /**
     * 移除某个异常类型的配置
     */
    public AdaptiveRetryStrategy removeException(Class<?> exceptionClass) {
        exceptionRetryCount.remove(exceptionClass);
        exceptionDelayMs.remove(exceptionClass);
        return this;
    }
    
    @Override
    public boolean shouldRetry(int attemptCount, Exception exception) {
        Class<?> exceptionClass = exception.getClass();
        int maxRetries = exceptionRetryCount.getOrDefault(exceptionClass, defaultMaxAttempts);
        return attemptCount < maxRetries;
    }
    
    @Override
    public long getWaitTimeMs(int attemptCount) {
        // 注意：这个方法设计上有缺陷，因为无法获取异常信息
        // 在实际使用中，建议结合具体的异常类型来获取延迟时间
        return defaultDelayMs;
    }
    
    /**
     * 根据异常类型获取等待时间
     */
    public long getWaitTimeMs(Exception exception) {
        Class<?> exceptionClass = exception.getClass();
        return exceptionDelayMs.getOrDefault(exceptionClass, defaultDelayMs);
    }
    
    @Override
    public int getMaxAttempts() {
        return exceptionRetryCount.values().stream()
            .max(Integer::compare)
            .orElse(defaultMaxAttempts);
    }
    
    @Override
    public String name() {
        return "AdaptiveRetryStrategy";
    }
    
    /**
     * 获取所有配置的异常类型
     */
    public Map<Class<?>, Integer> getConfiguredExceptions() {
        return new HashMap<>(exceptionRetryCount);
    }
    
    /**
     * 获取异常类型的重试次数
     */
    public Optional<Integer> getRetryCountForException(Class<?> exceptionClass) {
        return Optional.ofNullable(exceptionRetryCount.get(exceptionClass));
    }
    
    /**
     * 获取异常类型的延迟时间
     */
    public Optional<Long> getDelayForException(Class<?> exceptionClass) {
        return Optional.ofNullable(exceptionDelayMs.get(exceptionClass));
    }
}