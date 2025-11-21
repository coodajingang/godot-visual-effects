package com.dus.pipeline.retry;

import java.util.HashSet;
import java.util.Set;

/**
 * 跳过失败记录策略
 */
public class SkipFailedRecordsStrategy implements SkipStrategy {
    
    private final int maxAttempts;
    private final Set<Class<?>> skippableExceptions;
    private SkipListener skipListener;  // 监听被跳过的数据
    
    public SkipFailedRecordsStrategy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.skippableExceptions = new HashSet<>();
        this.skippableExceptions.add(Exception.class);
    }
    
    /**
     * 添加可跳过的异常类型
     */
    public SkipFailedRecordsStrategy addSkippableException(Class<?> exceptionClass) {
        skippableExceptions.add(exceptionClass);
        return this;
    }
    
    /**
     * 移除可跳过的异常类型
     */
    public SkipFailedRecordsStrategy removeSkippableException(Class<?> exceptionClass) {
        skippableExceptions.remove(exceptionClass);
        return this;
    }
    
    /**
     * 设置跳过监听器
     */
    public SkipFailedRecordsStrategy setSkipListener(SkipListener listener) {
        this.skipListener = listener;
        return this;
    }
    
    @Override
    public boolean shouldSkip(int attemptCount, Object input, Exception exception) {
        if (attemptCount >= maxAttempts) {
            // 达到最大尝试次数，跳过该数据
            if (skipListener != null) {
                skipListener.onSkipped(input, exception);
            }
            return true;
        }
        
        // 检查是否为可跳过的异常
        boolean isSkippable = skippableExceptions.stream()
            .anyMatch(e -> e.isInstance(exception));
        
        return isSkippable && attemptCount >= maxAttempts;
    }
    
    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    @Override
    public String name() {
        return "SkipFailedRecordsStrategy";
    }
    
    /**
     * 获取所有可跳过的异常类型
     */
    public Set<Class<?>> getSkippableExceptions() {
        return new HashSet<>(skippableExceptions);
    }
    
    /**
     * 获取跳过监听器
     */
    public SkipListener getSkipListener() {
        return skipListener;
    }
}