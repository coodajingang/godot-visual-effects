package com.dus.pipeline.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的指标收集器实现
 * 使用 ConcurrentHashMap 存储每个算子的统计数据，保证线程安全
 *
 * @author Dus
 * @version 1.0
 */
public class DefaultMetricsCollector implements MetricsCollector {

    private final Map<String, OperatorMetrics> metrics;

    /**
     * 构造函数
     */
    public DefaultMetricsCollector() {
        this.metrics = new ConcurrentHashMap<>();
    }

    @Override
    public void recordStart(String operatorName) {
        // 不需要特别处理，getOrCreateMetrics 会在需要时创建
    }

    @Override
    public void recordSuccess(String operatorName, long durationNanos) {
        OperatorMetrics m = getOrCreateMetrics(operatorName);
        m.recordSuccess(durationNanos);
    }

    @Override
    public void recordFailure(String operatorName, long durationNanos, Throwable ex) {
        OperatorMetrics m = getOrCreateMetrics(operatorName);
        m.recordFailure(durationNanos);
    }

    @Override
    public OperatorMetrics getMetrics(String operatorName) {
        return metrics.get(operatorName);
    }

    @Override
    public Map<String, OperatorMetrics> getAllMetrics() {
        return Collections.unmodifiableMap(new ConcurrentHashMap<>(metrics));
    }

    @Override
    public void reset() {
        for (OperatorMetrics m : metrics.values()) {
            m.reset();
        }
    }

    /**
     * 获取或创建指定算子的指标对象
     *
     * @param operatorName 算子名称
     * @return 算子指标对象
     */
    private OperatorMetrics getOrCreateMetrics(String operatorName) {
        return metrics.computeIfAbsent(operatorName, OperatorMetrics::new);
    }
}
