package com.dus.pipeline.example.sync;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.example.EnrichOperator;
import com.dus.pipeline.example.MySourceOperator;
import com.dus.pipeline.example.TransformOperator;
import com.dus.pipeline.example.WriteToDbOperator;
import com.dus.pipeline.metrics.DefaultMetricsCollector;
import com.dus.pipeline.metrics.OperatorMetrics;

import java.util.List;
import java.util.Map;

/**
 * Metrics 报告示例
 * 展示如何收集和查看性能指标
 *
 * @author Dus
 * @version 1.0
 */
public class MetricsReportExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Metrics Report Example ==========\n");

        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new MySourceOperator());

        pipeline.addOperator(new TransformOperator())
                .addOperator(new EnrichOperator())
                .addOperator(new WriteToDbOperator());

        DefaultMetricsCollector metricsCollector = new DefaultMetricsCollector();
        pipeline.setMetricsCollector(metricsCollector);

        System.out.println("Pipeline Status before execution: " + pipeline.getStatus() + "\n");

        pipeline.run();

        System.out.println("\nPipeline Status after execution: " + pipeline.getStatus());

        printCustomMetricsReport(pipeline);

        pipeline.shutdown();
    }

    private static void printCustomMetricsReport(Pipeline<List<String>, Void> pipeline) {
        System.out.println("\n========== Custom Metrics Report ==========");
        System.out.println("Pipeline Executor: " + pipeline.getExecutor().getClass().getSimpleName());
        System.out.println("Batch Splitter: " + pipeline.getBatchSplitter().name());

        Map<String, OperatorMetrics> metrics = pipeline.getMetrics();

        System.out.println("\nDetailed Operator Metrics:");
        System.out.println("-------------------------");

        for (Map.Entry<String, OperatorMetrics> entry : metrics.entrySet()) {
            OperatorMetrics m = entry.getValue();
            System.out.println("\nOperator: " + m.getOperatorName());
            System.out.println("  Total Invocations: " + m.getInvokeCount());
            System.out.println("  Successful: " + m.getSuccessCount());
            System.out.println("  Failed: " + m.getFailureCount());
            System.out.println("  Total Duration: " + (m.getTotalDurationNanos() / 1_000_000.0) + " ms");
            System.out.println("  Average Duration: " + (m.getAvgDurationNanos() / 1_000_000.0) + " ms");
            System.out.println("  Min Duration: " + (m.getMinDurationNanos() / 1_000_000.0) + " ms");
            System.out.println("  Max Duration: " + (m.getMaxDurationNanos() / 1_000_000.0) + " ms");
        }

        System.out.println("\n========================================\n");
    }
}
