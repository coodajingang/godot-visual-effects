package com.dus.pipeline.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * 默认算子指标实现
 * 使用线程安全的原子类和LongAdder确保并发安全
 * 
 * @author Dus
 * @version 1.0
 */
public class DefaultOperatorMetrics implements OperatorMetrics {
    
    private final String operatorName;
    private final LongAdder successCount;
    private final LongAdder failureCount;
    private final LongAdder totalDurationNanos;
    private volatile long minDurationNanos;
    private volatile long maxDurationNanos;
    
    /**
     * 构造函数
     * 
     * @param operatorName 算子名称
     */
    public DefaultOperatorMetrics(String operatorName) {
        this.operatorName = operatorName;
        this.successCount = new LongAdder();
        this.failureCount = new LongAdder();
        this.totalDurationNanos = new LongAdder();
        this.minDurationNanos = Long.MAX_VALUE;
        this.maxDurationNanos = Long.MIN_VALUE;
    }
    
    /**
     * 记录成功调用
     * 
     * @param durationNanos 耗时（纳秒）
     */
    public void recordSuccess(long durationNanos) {
        successCount.increment();
        totalDurationNanos.add(durationNanos);
        updateMinMax(durationNanos);
    }
    
    /**
     * 记录失败调用
     * 
     * @param durationNanos 耗时（纳秒）
     */
    public void recordFailure(long durationNanos) {
        failureCount.increment();
        totalDurationNanos.add(durationNanos);
        updateMinMax(durationNanos);
    }
    
    /**
     * 更新最小和最大耗时
     * 
     * @param durationNanos 当前耗时
     */
    private void updateMinMax(long durationNanos) {
        // 使用CAS操作更新最小值
        long currentMin = minDurationNanos;
        while (durationNanos < currentMin && !compareAndSetMin(currentMin, durationNanos)) {
            currentMin = minDurationNanos;
        }
        
        // 使用CAS操作更新最大值
        long currentMax = maxDurationNanos;
        while (durationNanos > currentMax && !compareAndSetMax(currentMax, durationNanos)) {
            currentMax = maxDurationNanos;
        }
    }
    
    /**
     * 原子性更新最小值
     */
    private boolean compareAndSetMin(long expect, long update) {
        synchronized (this) {
            if (minDurationNanos == expect) {
                minDurationNanos = update;
                return true;
            }
            return false;
        }
    }
    
    /**
     * 原子性更新最大值
     */
    private boolean compareAndSetMax(long expect, long update) {
        synchronized (this) {
            if (maxDurationNanos == expect) {
                maxDurationNanos = update;
                return true;
            }
            return false;
        }
    }
    
    @Override
    public String getOperatorName() {
        return operatorName;
    }
    
    @Override
    public long getSuccessCount() {
        return successCount.sum();
    }
    
    @Override
    public long getFailureCount() {
        return failureCount.sum();
    }
    
    @Override
    public double getAverageDurationNanos() {
        long totalCount = getTotalCount();
        return totalCount == 0 ? 0.0 : (double) totalDurationNanos.sum() / totalCount;
    }
    
    @Override
    public long getMinDurationNanos() {
        return minDurationNanos == Long.MAX_VALUE ? 0 : minDurationNanos;
    }
    
    @Override
    public long getMaxDurationNanos() {
        return maxDurationNanos == Long.MIN_VALUE ? 0 : maxDurationNanos;
    }
    
    @Override
    public long getTotalDurationNanos() {
        return totalDurationNanos.sum();
    }
    
    @Override
    public void reset() {
        successCount.reset();
        failureCount.reset();
        totalDurationNanos.reset();
        minDurationNanos = Long.MAX_VALUE;
        maxDurationNanos = Long.MIN_VALUE;
    }
}