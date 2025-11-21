package com.dus.pipeline.hook.notification;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.AfterPipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 操作器指标接口
 */
public interface OperatorMetrics {
    String getOperatorName();
    long getProcessedCount();
    long getErrorCount();
    long getTotalProcessingTimeMs();
    double getAverageProcessingTimeMs();
}

/**
 * 指标注册表接口
 */
public interface MetricsRegistry {
    
    /**
     * 记录 Pipeline 指标
     */
    void recordPipelineMetrics(String runId, long durationMs, long batchCount, long totalRecordCount, Map<String, OperatorMetrics> operatorMetrics) throws Exception;
    
    /**
     * 记录 Pipeline 失败
     */
    void recordPipelineFailure(String runId, Exception exception) throws Exception;
    
    /**
     * 记录 Pipeline 中断
     */
    void recordPipelineInterruption(String runId) throws Exception;
}

/**
 * 指标报告钩子
 */
public class MetricsReportingHook implements AfterPipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsReportingHook.class);
    
    private MetricsRegistry metricsRegistry;
    private Object pipeline;  // Pipeline 或 AsyncPipeline 实例
    
    public MetricsReportingHook(MetricsRegistry metricsRegistry, Object pipeline) {
        this.metricsRegistry = metricsRegistry;
        this.pipeline = pipeline;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证参数
        if (metricsRegistry == null) {
            throw new IllegalArgumentException("MetricsRegistry cannot be null");
        }
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline cannot be null");
        }
    }
    
    @Override
    public void afterPipeline(PipelineContext context) throws Exception {
        Map<String, OperatorMetrics> metrics = getOperatorMetrics();
        metricsRegistry.recordPipelineMetrics(
            context.getRunId(),
            context.getElapsedTimeMs(),
            context.getBatchCount(),
            context.getTotalRecordCount(),
            metrics
        );
        logger.info("Metrics reported for run: {}", context.getRunId());
    }
    
    @Override
    public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
        metricsRegistry.recordPipelineFailure(context.getRunId(), exception);
        logger.info("Failure metrics reported for run: {}", context.getRunId());
    }
    
    @Override
    public void onPipelineInterrupted(PipelineContext context) throws Exception {
        metricsRegistry.recordPipelineInterruption(context.getRunId());
        logger.info("Interruption metrics reported for run: {}", context.getRunId());
    }
    
    @Override
    public String name() {
        return "MetricsReportingHook";
    }
    
    /**
     * 获取操作器指标（通过反射获取）
     */
    @SuppressWarnings("unchecked")
    private Map<String, OperatorMetrics> getOperatorMetrics() {
        try {
            // 尝试调用 pipeline.getMetrics() 方法
            java.lang.reflect.Method method = pipeline.getClass().getMethod("getMetrics");
            return (Map<String, OperatorMetrics>) method.invoke(pipeline);
        } catch (Exception e) {
            logger.warn("Failed to get operator metrics from pipeline", e);
            return java.util.Collections.emptyMap();
        }
    }
}