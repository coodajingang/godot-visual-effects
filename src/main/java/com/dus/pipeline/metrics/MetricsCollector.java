package com.dus.pipeline.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标收集器接口
 * 
 * @author Dus
 * @version 1.0
 */
public interface MetricsCollector {
    
    /**
     * 记录算子成功调用
     * 
     * @param operatorName 算子名称
     * @param durationNanos 耗时（纳秒）
     */
    void recordSuccess(String operatorName, long durationNanos);
    
    /**
     * 记录算子失败调用
     * 
     * @param operatorName 算子名称
     * @param durationNanos 耗时（纳秒）
     */
    void recordFailure(String operatorName, long durationNanos);
    
    /**
     * 获取指定算子的指标
     * 
     * @param operatorName 算子名称
     * @return 算子指标
     */
    OperatorMetrics getOperatorMetrics(String operatorName);
    
    /**
     * 获取所有算子的指标
     * 
     * @return 所有算子指标的映射
     */
    Map<String, OperatorMetrics> getAllMetrics();
    
    /**
     * 重置所有统计数据
     */
    void reset();
    
    /**
     * 打印指标报告
     */
    void printMetricsReport();
}