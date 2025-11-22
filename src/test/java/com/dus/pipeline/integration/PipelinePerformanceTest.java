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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Pipeline 性能测试
 * 验证框架在不同负载下的性能表现
 * 
 * @author Dus
 * @version 1.0
 */
public class PipelinePerformanceTest {
    
    /**
     * 测试同步管道性能
     */
    public static void testSyncPipelinePerformance() throws Exception {
        System.out.println("=== 同步管道性能测试 ===");
        
        int[] dataSizes = {1000, 5000, 10000, 50000};
        
        for (int dataSize : dataSizes) {
            System.out.println("测试数据量: " + dataSize);
            
            // Given
            int batchSize = 100;
            int batchCount = dataSize / batchSize;
            
            SourceOperator<List<Map<String, Object>>> source = MockDataSource.createMapSource(batchCount, batchSize);
            PerformanceProcessor processor = new PerformanceProcessor();
            MockSink sink = new MockSink();
            
            DefaultMetricsCollector metrics = new DefaultMetricsCollector();
            Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(source, metrics)
                    .addOperator(processor)
                    .addOperator(sink);
            
            // When
            long startTime = System.nanoTime();
            pipeline.run();
            long endTime = System.nanoTime();
            
            // Then
            long durationMs = (endTime - startTime) / 1_000_000;
            double throughput = (double) dataSize / durationMs * 1000; // records per second
            
            System.out.println("  处理时间: " + durationMs + "ms");
            System.out.println("  吞吐量: " + String.format("%.2f", throughput) + " records/sec");
            System.out.println("  内存使用: " + getMemoryUsage() + "MB");
            System.out.println();
            
            assertThat(processor.getProcessedCount()).isEqualTo(batchCount);
            assertThat(sink.getTotalWritten()).isEqualTo(dataSize);
        }
    }
    
    /**
     * 测试异步管道性能
     */
    public static void testAsyncPipelinePerformance() throws Exception {
        System.out.println("=== 异步管道性能测试 ===");
        
        int[] dataSizes = {1000, 5000, 10000, 50000};
        
        for (int dataSize : dataSizes) {
            System.out.println("测试数据量: " + dataSize);
            
            // Given
            int batchSize = 100;
            int batchCount = dataSize / batchSize;
            
            AsyncPerformanceSource source = new AsyncPerformanceSource(batchCount, batchSize);
            AsyncPerformanceProcessor processor = new AsyncPerformanceProcessor();
            AsyncPerformanceSink sink = new AsyncPerformanceSink();
            
            DefaultMetricsCollector metrics = new DefaultMetricsCollector();
            com.dus.pipeline.async.AsyncPipeline<List<Map<String, Object>>, Void> pipeline = 
                new com.dus.pipeline.async.AsyncPipeline<>(source, metrics)
                    .addOperator(processor)
                    .addOperator(sink);
            
            // When
            long startTime = System.nanoTime();
            CompletableFuture<Void> future = pipeline.runAsync();
            future.get(60, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            
            // Then
            long durationMs = (endTime - startTime) / 1_000_000;
            double throughput = (double) dataSize / durationMs * 1000; // records per second
            
            System.out.println("  处理时间: " + durationMs + "ms");
            System.out.println("  吞吐量: " + String.format("%.2f", throughput) + " records/sec");
            System.out.println("  内存使用: " + getMemoryUsage() + "MB");
            System.out.println();
            
            assertThat(processor.getProcessedCount()).isEqualTo(batchCount);
            assertThat(sink.getTotalWritten()).isEqualTo(dataSize);
        }
    }
    
    /**
     * 测试并发管道性能
     */
    public static void testConcurrentPipelinePerformance() throws Exception {
        System.out.println("=== 并发管道性能测试 ===");
        
        int threadCount = 4;
        int dataSizePerThread = 10000;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
        
        long startTime = System.nanoTime();
        
        // 启动多个并发管道
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    runSinglePipeline(threadId, dataSizePerThread);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
        }
        
        // 等待所有管道完成
        CompletableFuture.allOf(futures).get(120, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        
        long durationMs = (endTime - startTime) / 1_000_000;
        int totalDataSize = dataSizePerThread * threadCount;
        double throughput = (double) totalDataSize / durationMs * 1000; // records per second
        
        System.out.println("线程数: " + threadCount);
        System.out.println("每线程数据量: " + dataSizePerThread);
        System.out.println("总数据量: " + totalDataSize);
        System.out.println("总处理时间: " + durationMs + "ms");
        System.out.println("总吞吐量: " + String.format("%.2f", throughput) + " records/sec");
        System.out.println("平均吞吐量/线程: " + String.format("%.2f", throughput / threadCount) + " records/sec");
        System.out.println("内存使用: " + getMemoryUsage() + "MB");
        System.out.println();
        
        executor.shutdown();
    }
    
    /**
     * 测试内存使用情况
     */
    public static void testMemoryUsage() throws Exception {
        System.out.println("=== 内存使用测试 ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 强制垃圾回收
        System.gc();
        Thread.sleep(1000);
        
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 处理大量数据
        int dataSize = 100000;
        SourceOperator<List<Map<String, Object>>> source = MockDataSource.createMapSource(1000, 100);
        MemoryTestProcessor processor = new MemoryTestProcessor();
        MockSink sink = new MockSink();
        
        Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(source)
                .addOperator(processor)
                .addOperator(sink);
        
        pipeline.run();
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = (afterMemory - beforeMemory) / 1024 / 1024; // MB
        
        System.out.println("处理数据量: " + dataSize);
        System.out.println("内存增长: " + memoryUsed + "MB");
        System.out.println("每条记录内存: " + String.format("%.2f", (double) memoryUsed * 1024 / dataSize) + "KB");
        System.out.println();
    }
    
    /**
     * 运行所有性能测试
     */
    public static void runAllPerformanceTests() {
        try {
            testSyncPipelinePerformance();
            testAsyncPipelinePerformance();
            testConcurrentPipelinePerformance();
            testMemoryUsage();
            
            System.out.println("🚀 所有性能测试完成！");
            
        } catch (Exception e) {
            System.err.println("❌ 性能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runSinglePipeline(int threadId, int dataSize) throws Exception {
        int batchSize = 100;
        int batchCount = dataSize / batchSize;
        
        SourceOperator<List<Map<String, Object>>> source = MockDataSource.createMapSource(batchCount, batchSize);
        PerformanceProcessor processor = new PerformanceProcessor();
        MockSink sink = new MockSink();
        
        Pipeline<List<Map<String, Object>>, Void> pipeline = new Pipeline<>(source)
                .addOperator(processor)
                .addOperator(sink);
        
        pipeline.run();
        
        System.out.println("Thread " + threadId + " completed: " + dataSize + " records");
    }
    
    private static long getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
    }
    
    // 测试辅助类
    
    private static class PerformanceProcessor extends AbstractOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
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
                for (int i = 0; i < 50; i++) {
                    sum += Math.sin(i) * Math.cos(i) + Math.sqrt(i + 1);
                }
                newRecord.put("computedValue", sum);
                newRecord.put("processedAt", System.currentTimeMillis());
                
                processed.add(newRecord);
            }
            
            return processed;
        }
        
        public int getProcessedCount() { return processedCount; }
    }
    
    private static class MockSink extends SinkOperator<List<Map<String, Object>>> {
        private int totalWritten = 0;
        
        @Override
        protected void write(List<Map<String, Object>> input) throws Exception {
            if (input != null) {
                totalWritten += input.size();
            }
        }
        
        public int getTotalWritten() { return totalWritten; }
    }
    
    private static class AsyncPerformanceSource extends com.dus.pipeline.async.AsyncSourceOperator<List<Map<String, Object>>> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        
        public AsyncPerformanceSource(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected CompletableFuture<List<Map<String, Object>>> doNextBatchAsync() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(5); // 模拟异步I/O
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                List<Map<String, Object>> batch = TestDataFactory.createPerformanceTestData(batchSize, 3);
                for (Map<String, Object> item : batch) {
                    item.put("asyncBatchId", currentBatch);
                }
                
                currentBatch++;
                return batch;
            });
        }
    }
    
    private static class AsyncPerformanceProcessor extends com.dus.pipeline.async.AsyncOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private int processedCount = 0;
        
        @Override
        protected CompletableFuture<List<Map<String, Object>>> processAsync(List<Map<String, Object>> input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10); // 模拟异步处理
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                processedCount++;
                
                List<Map<String, Object>> processed = new ArrayList<>();
                for (Map<String, Object> record : input) {
                    Map<String, Object> newRecord = new java.util.HashMap<>(record);
                    newRecord.put("asyncProcessed", true);
                    newRecord.put("asyncProcessedAt", System.currentTimeMillis());
                    processed.add(newRecord);
                }
                
                return processed;
            });
        }
        
        public int getProcessedCount() { return processedCount; }
    }
    
    private static class AsyncPerformanceSink extends com.dus.pipeline.async.AsyncOperator<List<Map<String, Object>>, Void> {
        private int totalWritten = 0;
        
        @Override
        protected CompletableFuture<Void> processAsync(List<Map<String, Object>> input) {
            return CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(8); // 模拟异步写入
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                if (input != null) {
                    totalWritten += input.size();
                }
            });
        }
        
        public int getTotalWritten() { return totalWritten; }
    }
    
    private static class MemoryTestProcessor extends AbstractOperator<List<Map<String, Object>>, List<Map<String, Object>>> {
        private final List<List<Map<String, Object>>> processedData = new ArrayList<>();
        
        @Override
        protected List<Map<String, Object>> doProcess(List<Map<String, Object>> input) throws Exception {
            // 故意保留数据引用以测试内存使用
            processedData.add(new ArrayList<>(input));
            
            List<Map<String, Object>> processed = new ArrayList<>();
            for (Map<String, Object> record : input) {
                Map<String, Object> newRecord = new java.util.HashMap<>(record);
                newRecord.put("memoryTest", true);
                processed.add(newRecord);
            }
            
            return processed;
        }
    }
}