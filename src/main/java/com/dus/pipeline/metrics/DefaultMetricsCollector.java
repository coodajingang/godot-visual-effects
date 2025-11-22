package com.dus.pipeline.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认指标收集器实现
 * 线程安全的指标收集器，使用ConcurrentHashMap存储算子指标
 *
 * @author Dus
 * @version 1.0
 */
public class DefaultMetricsCollector implements MetricsCollector {
    
    private final ConcurrentHashMap<String, OperatorMetrics> metricsMap;

    /**
     * 构造函数
     */
    public DefaultMetricsCollector() {
        this.metricsMap = new ConcurrentHashMap<>();
    }
    
    @Override
    public void recordSuccess(String operatorName, long durationNanos) {
        OperatorMetrics metrics = metricsMap.computeIfAbsent(operatorName, DefaultOperatorMetrics::new);
        ((DefaultOperatorMetrics) metrics).recordSuccess(durationNanos);
    }
    
    @Override
    public void recordFailure(String operatorName, long durationNanos) {
        OperatorMetrics metrics = metricsMap.computeIfAbsent(operatorName, DefaultOperatorMetrics::new);
        ((DefaultOperatorMetrics) metrics).recordFailure(durationNanos);
    }
    
    @Override
    public OperatorMetrics getOperatorMetrics(String operatorName) {
        return metricsMap.get(operatorName);
    }
    
    @Override
    public Map<String, OperatorMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(metricsMap);
    }
    
    @Override
    public void reset() {
        metricsMap.values().forEach(OperatorMetrics::reset);
        metricsMap.clear();
    }

    @Override
    public void printMetricsReport() {
        System.out.println("\n=== Pipeline Metrics Report ===");
        System.out.println("Total Operators: " + metricsMap.size());
        System.out.println();

        for (Map.Entry<String, OperatorMetrics> entry : metricsMap.entrySet()) {
            OperatorMetrics metrics = entry.getValue();
            System.out.println("Operator: " + metrics.getOperatorName());
            System.out.println("  Total Calls: " + metrics.getTotalCount());
            System.out.println("  Success: " + metrics.getSuccessCount());
            System.out.println("  Failure: " + metrics.getFailureCount());
            System.out.println("  Success Rate: " + String.format("%.2f%%",
                metrics.getTotalCount() == 0 ? 0.0 : (double) metrics.getSuccessCount() / metrics.getTotalCount() * 100));
            System.out.println("  Avg Duration: " + String.format("%.2f ms", metrics.getAverageDurationNanos() / 1_000_000.0));
            System.out.println("  Min Duration: " + String.format("%.2f ms", metrics.getMinDurationNanos() / 1_000_000.0));
            System.out.println("  Max Duration: " + String.format("%.2f ms", metrics.getMaxDurationNanos() / 1_000_000.0));
            System.out.println("  Total Duration: " + String.format("%.2f s", metrics.getTotalDurationNanos() / 1_000_000_000.0));
            System.out.println();
        }

        System.out.println("=== End of Report ===\n");
    }
}
