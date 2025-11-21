package com.dus.pipeline.retry;

import com.dus.pipeline.core.AbstractOperator;
import com.dus.pipeline.exception.OperatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带重试功能的算子
 */
public abstract class RetryableOperator<I, O> extends AbstractOperator<I, O> {
    
    protected RetryStrategy retryStrategy = new NoRetryStrategy();
    protected SkipStrategy skipStrategy = new NoSkipStrategy();
    
    /**
     * 设置重试策略
     */
    @SuppressWarnings("unchecked")
    public RetryableOperator<I, O> withRetryStrategy(RetryStrategy strategy) {
        this.retryStrategy = strategy;
        return (RetryableOperator<I, O>) this;
    }
    
    /**
     * 设置跳过策略
     */
    @SuppressWarnings("unchecked")
    public RetryableOperator<I, O> withSkipStrategy(SkipStrategy strategy) {
        this.skipStrategy = strategy;
        return (RetryableOperator<I, O>) this;
    }
    
    @Override
    public final O process(I input) {
        before(input);
        
        int attemptCount = 0;
        Exception lastException = null;
        
        while (true) {
            attemptCount++;
            try {
                O output = doProcess(input);
                after(input, output);
                return output;
            } catch (Exception e) {
                lastException = e;
                logger.warn("Attempt {} failed for operator {}: {}", 
                    attemptCount, name(), e.getMessage());
                
                // 检查是否应该重试
                if (retryStrategy.shouldRetry(attemptCount, e)) {
                    long waitTimeMs = retryStrategy.getWaitTimeMs(attemptCount);
                    logger.info("Retrying after {} ms...", waitTimeMs);
                    try {
                        Thread.sleep(waitTimeMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new OperatorException("Retry interrupted", ie);
                    }
                    continue;
                }
                
                // 检查是否应该跳过
                if (skipStrategy.shouldSkip(attemptCount, input, e)) {
                    logger.warn("Skipping record due to: {}", e.getMessage());
                    // 返回 null 或默认值表示跳过
                    return getDefaultValue();
                }
                
                // 都不重试也不跳过，抛出异常
                throw new OperatorException("Operator failed after " + attemptCount + " attempts", e);
            }
        }
    }
    
    /**
     * 返回跳过时的默认值
     */
    protected abstract O getDefaultValue();
    
    /**
     * 获取当前重试策略
     */
    public RetryStrategy getRetryStrategy() {
        return retryStrategy;
    }
    
    /**
     * 获取当前跳过策略
     */
    public SkipStrategy getSkipStrategy() {
        return skipStrategy;
    }
}