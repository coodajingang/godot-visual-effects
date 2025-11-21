package com.dus.pipeline.example.sync;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.example.EnrichOperator;
import com.dus.pipeline.example.MySourceOperator;
import com.dus.pipeline.example.TransformOperator;
import com.dus.pipeline.example.WriteToDbOperator;
import com.dus.pipeline.metrics.DefaultMetricsCollector;
import com.dus.pipeline.splitter.FixedSizeBatchSplitter;

import java.util.Arrays;
import java.util.List;

/**
 * 同步 Pipeline 示例
 * 展示使用 metrics、batch splitter、生命周期管理等功能
 *
 * @author Dus
 * @version 1.0
 */
public class SyncPipelineExample {

    public static void main(String[] args) throws Exception {
        System.out.println("========== Sync Pipeline Example ==========\n");

        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new MySourceOperator());
        
        pipeline.addOperator(new TransformOperator())
                .addOperator(new EnrichOperator())
                .addOperator(new WriteToDbOperator());

        pipeline.setMetricsCollector(new DefaultMetricsCollector());
        pipeline.setBatchSplitter(new FixedSizeBatchSplitter<>(2));

        System.out.println("Pipeline Status: " + pipeline.getStatus());
        
        pipeline.run();

        pipeline.printMetricsReport();

        pipeline.shutdown();
    }
}
