package com.dus.pipeline.async;

import com.dus.pipeline.core.Pipeline;
import com.dus.pipeline.core.SourceOperator;
import com.dus.pipeline.core.AbstractOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncPipeline 单元测试
 * 验证异步管道的功能和性能
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("AsyncPipeline 测试")
class AsyncPipelineTest {
    
    private AsyncPipeline<String, Void> asyncPipeline;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的异步算子
        TestAsyncSourceOperator source = new TestAsyncSourceOperator(3, 2);
        asyncPipeline = new AsyncPipeline<>(source);
    }
    
    @AfterEach
    void tearDown() {
        if (asyncPipeline != null) {
            asyncPipeline.shutdown();
        }
    }
    
    @Test
    @DisplayName("runAsync() 返回 CompletableFuture<Void>")
    void testRunAsyncReturnsCompletableFuture() throws Exception {
        // Given
        TestAsyncOperator asyncOperator = new TestAsyncOperator("AsyncProcessor", 10);
        asyncPipeline.addOperator(asyncOperator);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        
        // Then
        assertThat(future).isNotNull();
        assertThat(future).isInstanceOf(CompletableFuture.class);
        
        // 等待异步执行完成
        future.get(5, TimeUnit.SECONDS);
        
        // 验证管道状态
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(asyncOperator.getProcessedCount()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("异步管道的完整链路")
    void testCompleteAsyncPipelineChain() throws Exception {
        // Given
        TestAsyncOperator asyncOperator1 = new TestAsyncOperator("AsyncProcessor1", 20);
        TestAsyncOperator asyncOperator2 = new TestAsyncOperator("AsyncProcessor2", 15);
        
        asyncPipeline.addOperator(asyncOperator1)
                     .addOperator(asyncOperator2);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        future.get(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(asyncOperator1.getProcessedCount()).isEqualTo(3);
        assertThat(asyncOperator2.getProcessedCount()).isEqualTo(3);
    }
    
    @Test
    @DisplayName("异常情况下的处理")
    void testExceptionHandling() throws Exception {
        // Given
        TestAsyncOperator normalOperator = new TestAsyncOperator("NormalProcessor", 10);
        FailingAsyncOperator failingOperator = new FailingAsyncOperator("FailingProcessor", 5);
        
        asyncPipeline.addOperator(normalOperator)
                     .addOperator(failingOperator);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        
        // Then
        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Async processing failed");
        
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.FAILED);
        assertThat(normalOperator.getProcessedCount()).isGreaterThan(0); // 正常算子应该处理了一些数据
    }
    
    @Test
    @DisplayName("性能：异步 vs 同步")
    void testPerformanceAsyncVsSync() throws Exception {
        // Given - 同步管道
        TestSyncSourceOperator syncSource = new TestSyncSourceOperator(5, 2);
        TestSyncOperator syncOperator = new TestSyncOperator("SyncProcessor", 50);
        Pipeline<String, Void> syncPipeline = new Pipeline<>(syncSource)
                .addOperator(syncOperator);
        
        // Given - 异步管道
        TestAsyncSourceOperator asyncSource = new TestAsyncSourceOperator(5, 2);
        TestAsyncOperator asyncOperator = new TestAsyncOperator("AsyncProcessor", 50);
        AsyncPipeline<String, Void> asyncPipe = new AsyncPipeline<>(asyncSource)
                .addOperator(asyncOperator);
        
        // When - 测试同步性能
        long syncStartTime = System.currentTimeMillis();
        syncPipeline.run();
        long syncEndTime = System.currentTimeMillis();
        long syncDuration = syncEndTime - syncStartTime;
        
        // When - 测试异步性能
        long asyncStartTime = System.currentTimeMillis();
        CompletableFuture<Void> asyncFuture = asyncPipe.runAsync();
        asyncFuture.get(10, TimeUnit.SECONDS);
        long asyncEndTime = System.currentTimeMillis();
        long asyncDuration = asyncEndTime - asyncStartTime;
        
        // Then - 验证处理结果相同
        assertThat(syncOperator.getProcessedCount()).isEqualTo(asyncOperator.getProcessedCount());
        
        // 注意：异步性能优势在更复杂的场景下更明显
        // 在这个简单测试中，异步可能不会明显更快，甚至可能稍慢（由于线程调度开销）
        System.out.println("Sync duration: " + syncDuration + "ms");
        System.out.println("Async duration: " + asyncDuration + "ms");
        
        // 清理
        asyncPipe.shutdown();
    }
    
    @Test
    @DisplayName("shutdown() 和 awaitTermination() 在异步场景")
    void testShutdownAndAwaitTerminationInAsyncScenario() throws Exception {
        // Given
        TestAsyncOperator slowOperator = new TestAsyncOperator("SlowProcessor", 1000); // 1秒延迟
        asyncPipeline.addOperator(slowOperator);
        
        // When - 启动异步管道
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        
        // 等待管道开始运行
        Thread.sleep(100);
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.RUNNING);
        
        // 关闭管道
        asyncPipeline.shutdown();
        
        // Then
        boolean terminated = asyncPipeline.awaitTermination(2000);
        assertThat(terminated).isTrue();
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        
        // 验证future被取消或完成
        assertThat(future.isDone()).isTrue();
    }
    
    @Test
    @DisplayName("awaitTermination 超时处理")
    void testAwaitTerminationTimeout() throws Exception {
        // Given
        TestAsyncOperator slowOperator = new TestAsyncOperator("VerySlowProcessor", 3000); // 3秒延迟
        asyncPipeline.addOperator(slowOperator);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        Thread.sleep(100); // 让管道开始运行
        
        // 短时间等待，应该超时
        boolean terminated = asyncPipeline.awaitTermination(500); // 500ms
        
        // Then
        assertThat(terminated).isFalse();
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.RUNNING);
        
        // 清理
        asyncPipeline.shutdown();
        asyncPipeline.awaitTermination(5000);
        future.cancel(true);
    }
    
    @Test
    @DisplayName("多次异步运行")
    void testMultipleAsyncRuns() throws Exception {
        // Given
        TestAsyncOperator asyncOperator = new TestAsyncOperator("AsyncProcessor", 10);
        asyncPipeline.addOperator(asyncOperator);
        
        // When - 第一次运行
        CompletableFuture<Void> future1 = asyncPipeline.runAsync();
        future1.get(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        int firstRunCount = asyncOperator.getProcessedCount();
        
        // When - 第二次运行需要创建新的管道实例
        TestAsyncSourceOperator newSource = new TestAsyncSourceOperator(2, 2);
        AsyncPipeline<String, Void> newPipeline = new AsyncPipeline<>(newSource)
                .addOperator(asyncOperator);
        
        CompletableFuture<Void> future2 = newPipeline.runAsync();
        future2.get(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(newPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(asyncOperator.getProcessedCount()).isEqualTo(firstRunCount + 2);
        
        // 清理
        newPipeline.shutdown();
    }
    
    @Test
    @DisplayName("异步管道的指标收集")
    void testAsyncPipelineMetrics() throws Exception {
        // Given
        TestAsyncOperator asyncOperator = new TestAsyncOperator("AsyncProcessor", 20);
        asyncPipeline.addOperator(asyncOperator);
        
        // When
        CompletableFuture<Void> future = asyncPipeline.runAsync();
        future.get(5, TimeUnit.SECONDS);
        
        // Then
        var metrics = asyncPipeline.getMetricsCollector();
        var allMetrics = metrics.getAllMetrics();
        
        assertThat(allMetrics).hasSize(2); // Source + AsyncOperator
        
        var operatorMetrics = metrics.getOperatorMetrics("AsyncProcessor");
        assertThat(operatorMetrics).isNotNull();
        assertThat(operatorMetrics.getSuccessCount()).isEqualTo(3);
        assertThat(operatorMetrics.getFailureCount()).isEqualTo(0);
        assertThat(operatorMetrics.getTotalDurationNanos()).isGreaterThan(20_000_000L * 3); // 至少60ms
    }
    
    @Test
    @DisplayName("异步管道的组合操作")
    void testAsyncPipelineComposition() throws Exception {
        // Given
        TestAsyncOperator operator1 = new TestAsyncOperator("Processor1", 10);
        TestAsyncOperator operator2 = new TestAsyncOperator("Processor2", 15);
        
        asyncPipeline.addOperator(operator1).addOperator(operator2);
        
        // When - 使用thenCompose组合异步操作
        CompletableFuture<Void> pipelineFuture = asyncPipeline.runAsync();
        CompletableFuture<String> composedFuture = pipelineFuture.thenCompose(v -> {
            return CompletableFuture.supplyAsync(() -> "Pipeline completed successfully");
        });
        
        String result = composedFuture.get(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(result).isEqualTo("Pipeline completed successfully");
        assertThat(asyncPipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
    }
    
    // 测试辅助类
    
    private static class TestAsyncSourceOperator extends AsyncSourceOperator<String> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        
        public TestAsyncSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected CompletableFuture<String> doNextBatchAsync() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(10); // 模拟异步数据获取延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                
                if (currentBatch >= maxBatches) {
                    return null;
                }
                
                StringBuilder batch = new StringBuilder();
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) batch.append(",");
                    batch.append("AsyncData_").append(currentBatch).append("_").append(i);
                }
                
                currentBatch++;
                return batch.toString();
            });
        }
    }
    
    private static class TestAsyncOperator extends AsyncOperator<String, String> {
        private final String name;
        private final long delayMs;
        private int processedCount = 0;
        
        public TestAsyncOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected CompletableFuture<String> processAsync(String input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", e);
                }
                
                processedCount++;
                return "async_processed: " + input;
            });
        }
        
        @Override
        public String name() {
            return name;
        }
        
        public int getProcessedCount() {
            return processedCount;
        }
    }
    
    private static class FailingAsyncOperator extends AsyncOperator<String, String> {
        private final String name;
        private final long delayMs;
        private int callCount = 0;
        
        public FailingAsyncOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected CompletableFuture<String> processAsync(String input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                callCount++;
                throw new RuntimeException("Async processing failed");
            });
        }
        
        @Override
        public String name() {
            return name;
        }
        
        public int getCallCount() {
            return callCount;
        }
    }
    
    private static class TestSyncSourceOperator extends SourceOperator<String> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        
        public TestSyncSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            if (currentBatch >= maxBatches) {
                return null;
            }
            
            StringBuilder batch = new StringBuilder();
            for (int i = 0; i < batchSize; i++) {
                if (i > 0) batch.append(",");
                batch.append("SyncData_").append(currentBatch).append("_").append(i);
            }
            
            currentBatch++;
            return batch.toString();
        }
    }
    
    private static class TestSyncOperator extends AbstractOperator<String, String> {
        private final String name;
        private final long delayMs;
        private int processedCount = 0;
        
        public TestSyncOperator(String name, long delayMs) {
            this.name = name;
            this.delayMs = delayMs;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            processedCount++;
            return "sync_processed: " + input;
        }
        
        @Override
        public String name() {
            return name;
        }
        
        public int getProcessedCount() {
            return processedCount;
        }
    }
}