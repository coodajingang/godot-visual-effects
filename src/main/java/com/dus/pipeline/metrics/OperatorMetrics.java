package com.dus.pipeline.metrics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;

/**
 * 算子指标统计接口
 * 
 * @author Dus
 * @version 1.0
 */
public interface OperatorMetrics {
    
    /**
     * 获取算子名称
     * 
     * @return 算子名称
     */
    String getOperatorName();
    
    /**
     * 获取成功调用次数
     * 
     * @return 成功调用次数
     */
    long getSuccessCount();
    
    /**
     * 获取失败调用次数
     * 
     * @return 失败调用次数
     */
    long getFailureCount();
    
    /**
     * 获取总调用次数
     * 
     * @return 总调用次数
     */
    default long getTotalCount() {
        return getSuccessCount() + getFailureCount();
    }
    
    /**
     * 获取平均耗时（纳秒）
     * 
     * @return 平均耗时
     */
    double getAverageDurationNanos();
    
    /**
     * 获取最小耗时（纳秒）
     * 
     * @return 最小耗时
     */
    long getMinDurationNanos();
    
    /**
     * 获取最大耗时（纳秒）
     * 
     * @return 最大耗时
     */
    long getMaxDurationNanos();
    
    /**
     * 获取总耗时（纳秒）
     * 
     * @return 总耗时
     */
    long getTotalDurationNanos();
    
    /**
     * 重置统计数据
     */
    void reset();
}