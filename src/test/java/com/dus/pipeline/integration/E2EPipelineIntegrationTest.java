package com.dus.pipeline.integration;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.core.SourceOperator;
import com.dus.pipeline.core.AbstractOperator;
import com.dus.pipeline.core.SinkOperator;
import com.dus.pipeline.metrics.DefaultMetricsCollector;
import com.dus.pipeline.util.TestDataFactory;
import com.dus.pipeline.util.MockDataSource;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

/**
 * E2E Pipeline 集成测试
 * 完整端到端流程测试
 * 
 * @author Dus
 * @version 1.0
 */
public class E2EPipelineIntegrationTest {
    
    /**
     * 场景1：HTTP → 转换 → MySQL
     * 从模拟 HTTP 接口拉数据，转换处理，批量写入 MySQL
     */
    public static void testHttpToMySQLPipeline() throws Exception {
        System.out.println("=== E2E Test 1: HTTP → 转换 → MySQL ===");
        
        // Given - 模拟HTTP数据源
        SourceOperator<List<Map<String, Object>>> httpSource = MockDataSource.createMapSource(3, 5);
        
        // 转换算子
        TransformOperator transform = new TransformOperator();
        
        // 模拟MySQL写入算子
        MockMySQLSinkOperator mysqlSink = new MockMySQLSinkOperator();
        
        // 创建管道
        DefaultMetricsCollector metrics = new DefaultMetricsCollector();
        Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(httpSource, metrics)
                .addOperator(transform)
                .addOperator(mysqlSink);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(transform.getProcessedCount()).isEqualTo(3);
        assertThat(mysqlSink.getTotalWritten()).isEqualTo(15); // 3批次 × 5条数据
        assertThat(mysqlSink.getBatchCount()).isEqualTo(3);
        
        // 验证数据转换正确性
        List<Map<String, Object>> writtenData = mysqlSink.getWrittenData();
        for (Map<String, Object> record : writtenData) {
            assertThat(record.containsKey("id")).isTrue();
            assertThat(record.containsKey("name")).isTrue();
            assertThat(record.containsKey("transformed")).isTrue();
            assertThat(record.get("transformed")).isEqualTo(true);
        }
        
        // 打印指标报告
        pipeline.printMetricsReport();
        
        System.out.println("✓ HTTP → 转换 → MySQL 测试通过\n");
    }
    
    /**
     * 场景2：MySQL → 富化 → 文件
     * 从 MySQL 读数据，外部 API 调用富化，写入文件
     */
    public static void testMySQLToEnrichToFilePipeline() throws Exception {
        System.out.println("=== E2E Test 2: MySQL → 富化 → 文件 ===");
        
        // Given - 模拟MySQL数据源
        SourceOperator<List<Map<String, Object>>> mysqlSource = MockDataSource.createUserSource(2, 4);
        
        // 富化算子（模拟外部API调用）
        EnrichOperator enrich = new EnrichOperator();
        
        // 文件写入算子
        MockFileSinkOperator fileSink = new MockFileSinkOperator("enriched_users.json");
        
        // 创建管道
        DefaultMetricsCollector metrics = new DefaultMetricsCollector();
        Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(mysqlSource, metrics)
                .addOperator(enrich)
                .addOperator(fileSink);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(enrich.getProcessedCount()).isEqualTo(2);
        assertThat(fileSink.getTotalWritten()).isEqualTo(8); // 2批次 × 4条数据
        
        // 验证富化数据
        List<Map<String, Object>> fileContent = fileSink.getFileContent();
        for (Map<String, Object> record : fileContent) {
            assertThat(record.containsKey("enrichmentData")).isTrue();
            assertThat(record.containsKey("enrichmentTimestamp")).isTrue();
            assertThat(record.get("enriched")).isEqualTo(true);
        }
        
        // 打印指标报告
        pipeline.printMetricsReport();
        
        System.out.println("✓ MySQL → 富化 → 文件 测试通过\n");
    }
    
    /**
     * 场景3：异步管道 + Metrics
     * AsyncPipeline 完整流程，性能指标收集，报告生成
     */
    public static void testAsyncPipelineWithMetrics() throws Exception {
        System.out.println("=== E2E Test 3: 异步管道 + Metrics ===");
        
        // Given - 创建异步管道
        AsyncSourceOperator asyncSource = new AsyncSourceOperator(4, 3);
        AsyncTransformOperator asyncTransform = new AsyncTransformOperator();
        AsyncSinkOperator asyncSink = new AsyncSinkOperator();
        
        DefaultMetricsCollector metrics = new DefaultMetricsCollector();
        AsyncPipeline<List<Map<String, Object>>, Void> asyncPipeline = new AsyncPipeline<>(asyncSource, metrics)
                .addOperator(asyncTransform)
                .addOperator(asyncSink);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        future.get(); // 等待完成
        
        // Then
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(asyncTransform.getProcessedCount()).isEqualTo(4);
        assertThat(asyncSink.getTotalWritten()).isEqualTo(12); // 4批次 × 3条数据
        
        // 验证异步处理
        assertThat(asyncTransform.isAsyncProcessed()).isTrue();
        assertThat(asyncSink.isAsyncProcessed()).isTrue();
        
        // 打印详细的性能指标
        System.out.println("异步管道性能指标:");
        metrics.printMetricsReport();
        
        // 验证指标数据
        var allMetrics = metrics.getAllMetrics();
        assertThat(allMetrics).hasSize(3); // Source + 2个异步算子
        
        for (var operatorMetrics : allMetrics.values()) {
            assertThat(operatorMetrics.getSuccessCount()).isGreaterThan(0);
            assertThat(operatorMetrics.getFailureCount()).isEqualTo(0);
            assertThat(operatorMetrics.getTotalDurationNanos()).isGreaterThan(0);
        }
        
        System.out.println("✓ 异步管道 + Metrics 测试通过\n");
    }
    
    /**
     * 场景4：错误处理和恢复
     * 测试管道在遇到错误时的处理机制
     */
    public static void testErrorHandlingAndRecovery() throws Exception {
        System.out.println("=== E2E Test 4: 错误处理和恢复 ===");
        
        // Given - 创建会失败的数据源
        SourceOperator<String> failingSource = MockDataSource.createFailingSource(2, 3);
        
        TransformOperator transform = new TransformOperator();
        FailingSinkOperator failingSink = new FailingSinkOperator(2); // 第2次写入失败
        
        DefaultMetricsCollector metrics = new DefaultMetricsCollector();
        Pipeline<String, Void> pipeline = new Pipeline<>(failingSource, metrics)
                .addOperator(transform)
                .addOperator(failingSink);
        
        // When & Then
        try {
            pipeline.run();
            fail("Expected pipeline to fail");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Simulated sink failure");
        }
        
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.FAILED);
        assertThat(transform.getProcessedCount()).isEqualTo(2); // 在失败前处理了2批次
        assertThat(failingSink.getCallCount()).isEqualTo(2); // 被调用2次
        
        // 验证错误指标
        var sinkMetrics = metrics.getOperatorMetrics(failingSink.name());
        assertThat(sinkMetrics).isNotNull();
        assertThat(sinkMetrics.getFailureCount()).isEqualTo(1);
        
        System.out.println("✓ 错误处理和恢复 测试通过\n");
    }
    
    /**
     * 场景5：大数据量性能测试
     * 测试管道处理大数据量时的性能表现
     */
    public static void testLargeDataVolumePerformance() throws Exception {
        System.out.println("=== E2E Test 5: 大数据量性能测试 ===");
        
        // Given - 大数据量测试
        int totalRecords = 10000;
        int batchSize = 100;
        int batchCount = totalRecords / batchSize;
        
        SourceOperator<List<Map<String, Object>>> largeDataSource = MockDataSource.createMapSource(batchCount, batchSize);
        
        PerformanceTestOperator perfOperator = new PerformanceTestOperator();
        MockFileSinkOperator fileSink = new MockFileSinkOperator("large_data_output.json");
        
        DefaultMetricsCollector metrics = new DefaultMetricsCollector();
        Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(largeDataSource, metrics)
                .addOperator(perfOperator)
                .addOperator(fileSink);
        
        // When
        long startTime = System.currentTimeMillis();
        pipeline.run();
        long endTime = System.currentTimeMillis();
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(perfOperator.getProcessedCount()).isEqualTo(batchCount);
        assertThat(fileSink.getTotalWritten()).isEqualTo(totalRecords);
        
        long duration = endTime - startTime;
        double throughput = (double) totalRecords / duration * 1000; // records per second
        
        System.out.println("性能测试结果:");
        System.out.println("- 总记录数: " + totalRecords);
        System.out.println("- 处理时间: " + duration + "ms");
        System.out.println("- 吞吐量: " + String.format("%.2f", throughput) + " records/sec");
        
        // 打印性能指标
        pipeline.printMetricsReport();
        
        System.out.println("✓ 大数据量性能测试通过\n");
    }
    
    /**
     * 运行所有E2E测试
     */
    public static void runAllTests() {
        try {
            testHttpToMySQLPipeline();
            testMySQLToEnrichToFilePipeline();
            testAsyncPipelineWithMetrics();
            testErrorHandlingAndRecovery();
            testLargeDataVolumePerformance();
            
            System.out.println("🎉 所有E2E集成测试通过！");
            
        } catch (Exception e) {
            System.err.println("❌ E2E测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 测试辅助类
    
    private static class TransformOperator extends AbstractOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private int processedCount = 0;
        
        @Override
        protected List<Map<String, Object>> doProcess(List<Map<String, Object>> input) throws Exception {
            processedCount++;
            
            List<Map<String, Object>> transformed = new ArrayList<>();
            for (Map<String, Object> record : input) {
                Map<String, Object> newRecord = new java.util.HashMap<>(record);
                newRecord.put("transformed", true);
                newRecord.put("transformTimestamp", System.currentTimeMillis());
                transformed.add(newRecord);
            }
            
            return transformed;
        }
        
        public int getProcessedCount() { return processedCount; }
    }
    
    private static class MockMySQLSinkOperator extends SinkOperator<List<Map<String, Object>>> {
        private int totalWritten = 0;
        private int batchCount = 0;
        private List<Map<String, Object>> writtenData = new ArrayList<>();
        
        @Override
        protected void write(List<Map<String, Object>> input) throws Exception {
            if (input != null) {
                totalWritten += input.size();
                batchCount++;
                writtenData.addAll(input);
            }
        }
        
        public int getTotalWritten() { return totalWritten; }
        public int getBatchCount() { return batchCount; }
        public List<Map<String, Object>> getWrittenData() { return new ArrayList<>(writtenData); }
    }
    
    private static class EnrichOperator extends AbstractOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private int processedCount = 0;
        
        @Override
        protected List<Map<String, Object>> doProcess(List<Map<String, Object>> input) throws Exception {
            processedCount++;
            
            List<Map<String, Object>> enriched = new ArrayList<>();
            for (Map<String, Object> record : input) {
                Map<String, Object> enrichedRecord = new java.util.HashMap<>(record);
                
                // 模拟外部API调用富化
                Map<String, Object> enrichmentData = new java.util.HashMap<>();
                enrichmentData.put("source", "external_api");
                enrichmentData.put("version", "1.0");
                enrichmentData.put("additionalInfo", "Enriched at " + System.currentTimeMillis());
                
                enrichedRecord.put("enrichmentData", enrichmentData);
                enrichedRecord.put("enrichmentTimestamp", System.currentTimeMillis());
                enrichedRecord.put("enriched", true);
                
                enriched.add(enrichedRecord);
            }
            
            return enriched;
        }
        
        public int getProcessedCount() { return processedCount; }
    }
    
    private static class MockFileSinkOperator extends SinkOperator<List<Map<String, Object>>> {
        private final String filename;
        private int totalWritten = 0;
        private List<Map<String, Object>> fileContent = new ArrayList<>();
        
        public MockFileSinkOperator(String filename) {
            this.filename = filename;
        }
        
        @Override
        protected void write(List<Map<String, Object>> input) throws Exception {
            if (input != null) {
                totalWritten += input.size();
                fileContent.addAll(input);
                System.out.println("写入文件 " + filename + ": " + input.size() + " 条记录");
            }
        }
        
        public int getTotalWritten() { return totalWritten; }
        public List<Map<String, Object>> getFileContent() { return new ArrayList<>(fileContent); }
    }
    
    private static class AsyncSourceOperator extends com.dus.pipeline.async.AsyncSourceOperator<List<Map<String, Object>>> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        
        public AsyncSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected java.util.concurrent.CompletableFuture<List<Map<String, Object>>> doNextBatchAsync() {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10); // 模拟异步延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = TestDataFactory.createMapList(batchSize);
                for (Map<String, Object> item : batch) {
                    item.put("asyncBatchId", currentBatch);
                }
                
                currentBatch++;
                return batch;
            });
        }
    }
    
    private static class AsyncTransformOperator extends com.dus.pipeline.async.AsyncOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private int processedCount = 0;
        private boolean asyncProcessed = false;
        
        @Override
        protected java.util.concurrent.CompletableFuture<List<Map<String, Object>>> processAsync(List<Map<String, Object>> input) {
            return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(20); // 模拟异步处理
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                processedCount++;
                asyncProcessed = true;
                
                List<Map<String, Object>> transformed = new ArrayList<>();
                for (Map<String, Object> record : input) {
                    Map<String, Object> newRecord = new java.util.HashMap<>(record);
                    newRecord.put("asyncTransformed", true);
                    transformed.add(newRecord);
                }
                
                return transformed;
            });
        }
        
        public int getProcessedCount() { return processedCount; }
        public boolean isAsyncProcessed() { return asyncProcessed; }
    }
    
    private static class AsyncSinkOperator extends com.dus.pipeline.async.AsyncOperator<List<Map<String, Object>>, Void> {
        private int totalWritten = 0;
        private boolean asyncProcessed = false;
        
        @Override
        protected java.util.concurrent.CompletableFuture<Void> processAsync(List<Map<String, Object>> input) {
            return java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(15); // 模拟异步写入
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (input != null) {
                    totalWritten += input.size();
                }
                asyncProcessed = true;
            });
        }
        
        public int getTotalWritten() { return totalWritten; }
        public boolean isAsyncProcessed() { return asyncProcessed; }
    }
    
    private static class FailingSinkOperator extends SinkOperator<String> {
        private final int failAfterCalls;
        private int callCount = 0;
        
        public FailingSinkOperator(int failAfterCalls) {
            this.failAfterCalls = failAfterCalls;
        }
        
        @Override
        protected void write(String input) throws Exception {
            callCount++;
            if (callCount >= failAfterCalls) {
                throw new RuntimeException("Simulated sink failure");
            }
        }
        
        public int getCallCount() { return callCount; }
    }
    
    private static class PerformanceTestOperator extends AbstractOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private int processedCount = 0;
        
        @Override
        protected List<Map<String, Object>> doProcess(List<Map<String, Object>> input) throws Exception {
            processedCount++;
            
            // 模拟一些CPU密集型处理
            List<Map<String, Object>> processed = new ArrayList<>();
            for (Map<String, Object> record : input) {
                Map<String, Object> newRecord = new java.util.HashMap<>(record);
                
                // 模拟复杂计算
                double sum = 0;
                for (int i = 0; i < 100; i++) {
                    sum += Math.sin(i) * Math.cos(i);
                }
                newRecord.put("computedValue", sum);
                
                processed.add(newRecord);
            }
            
            return processed;
        }
        
        public int getProcessedCount() { return processedCount; }
    }
}