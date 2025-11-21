package com.dus.pipeline.metrics;

import java.util.Map;

/**
 * 指标收集器接口
 * 定义了对算子性能指标的收集、查询、重置等操作
 *
 * @author Dus
 * @version 1.0
 */
public interface MetricsCollector {

    /**
     * 记录算子执行开始
     *
     * @param operatorName 算子名称
     */
    void recordStart(String operatorName);

    /**
     * 记录算子执行成功
     *
     * @param operatorName 算子名称
     * @param durationNanos 执行耗时（纳秒）
     */
    void recordSuccess(String operatorName, long durationNanos);

    /**
     * 记录算子执行失败
     *
     * @param operatorName 算子名称
     * @param durationNanos 执行耗时（纳秒）
     * @param ex 异常对象
     */
    void recordFailure(String operatorName, long durationNanos, Throwable ex);

    /**
     * 获取特定算子的指标
     *
     * @param operatorName 算子名称
     * @return 算子指标对象，如果不存在返回null
     */
    OperatorMetrics getMetrics(String operatorName);

    /**
     * 获取所有算子的指标
     *
     * @return 包含所有算子指标的Map，key为算子名称，value为指标对象
     */
    Map<String, OperatorMetrics> getAllMetrics();

    /**
     * 重置所有指标数据
     */
    void reset();
}
