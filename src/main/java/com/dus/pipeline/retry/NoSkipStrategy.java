package com.dus.pipeline.retry;

/**
 * 不跳过策略
 */
public class NoSkipStrategy implements SkipStrategy {
    
    @Override
    public boolean shouldSkip(int attemptCount, Object input, Exception exception) {
        return false;
    }
    
    @Override
    public int getMaxAttempts() {
        return 1;
    }
    
    @Override
    public String name() {
        return "NoSkipStrategy";
    }
}