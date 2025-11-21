package com.dus.pipeline.retry;

/**
 * 跳过策略：遇到错误时是否跳过当前数据项继续处理下一个
 */
public interface SkipStrategy {
    
    /**
     * 判断是否应该跳过这条数据
     * @param attemptCount 已尝试次数
     * @param input 输入数据
     * @param exception 抛出的异常
     * @return true 表示跳过，false 表示重新抛出异常
     */
    boolean shouldSkip(int attemptCount, Object input, Exception exception);
    
    /**
     * 获取最大尝试次数（在最终跳过前）
     */
    int getMaxAttempts();
    
    /**
     * 获取跳过策略的名称
     */
    String name();
}