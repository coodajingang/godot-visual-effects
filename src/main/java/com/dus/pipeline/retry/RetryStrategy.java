package com.dus.pipeline.retry;

/**
 * 重试策略接口
 */
public interface RetryStrategy {
    
    /**
     * 判断是否应该重试
     * @param attemptCount 已尝试次数（从 1 开始）
     * @param exception 抛出的异常
     * @return true 表示应该重试，false 表示不重试
     */
    boolean shouldRetry(int attemptCount, Exception exception);
    
    /**
     * 获取下次重试前的等待时间（毫秒）
     * @param attemptCount 已尝试次数
     * @return 等待时间，单位毫秒
     */
    long getWaitTimeMs(int attemptCount);
    
    /**
     * 获取最大重试次数
     */
    int getMaxAttempts();
    
    /**
     * 获取重试策略的名称
     */
    String name();
}