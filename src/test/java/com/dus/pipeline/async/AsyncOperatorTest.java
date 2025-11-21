package com.dus.pipeline.async;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncOperator 单元测试
 * 验证异步算子的功能和异常处理
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("AsyncOperator 测试")
class AsyncOperatorTest {
    
    private TestAsyncOperator asyncOperator;
    
    @BeforeEach
    void setUp() {
        asyncOperator = new TestAsyncOperator();
    }
    
    @AfterEach
    void tearDown() {
        if (asyncOperator != null) {
            asyncOperator.shutdown();
        }
    }
    
    @Test
    @DisplayName("AsyncOperator.processAsync() 返回 CompletableFuture")
    void testProcessAsyncReturnsCompletableFuture() throws Exception {
        // Given
        String input = "test input";
        
        // When
        CompletableFuture<String> future = asyncOperator.processAsync(input);
        
        // Then
        assertThat(future).isNotNull();
        assertThat(future).isInstanceOf(CompletableFuture.class);
        
        // 验证异步结果
        String result = future.get(1, TimeUnit.SECONDS);
        assertThat(result).isEqualTo("async_processed: test input");
    }
    
    @Test
    @DisplayName("AsyncOperator.process() 阻塞等待异步结果")
    void testProcessBlocksForAsyncResult() throws Exception {
        // Given
        String input = "test input";
        TestAsyncOperator slowOperator = new TestAsyncOperator(200); // 200ms延迟
        
        // When
        long startTime = System.currentTimeMillis();
        String result = slowOperator.process(input);
        long endTime = System.currentTimeMillis();
        
        // Then
        assertThat(result).isEqualTo("async_processed: test input");
        assertThat(endTime - startTime).isBetween(180L, 300L); // 考虑调度开销
        
        slowOperator.shutdown();
    }
    
    @Test
    @DisplayName("异步处理中的异常处理")
    void testAsyncExceptionHandling() throws Exception {
        // Given
        FailingAsyncOperator failingOperator = new FailingAsyncOperator();
        String input = "test input";
        
        // When & Then - processAsync应该抛出异常
        CompletableFuture<String> future = failingOperator.processAsync(input);
        assertThatThrownBy(() -> future.get(1, TimeUnit.SECONDS))
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Async processing failed");
        
        // When & Then - process也应该抛出异常
        assertThatThrownBy(() -> failingOperator.process(input))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Async processing failed");
    }
    
    @Test
    @DisplayName("多个异步操作的并发执行")
    void testConcurrentAsyncOperations() throws Exception {
        // Given
        int operationCount = 10;
        TestAsyncOperator operator = new TestAsyncOperator(50); // 50ms延迟
        CompletableFuture<String>[] futures = new CompletableFuture[operationCount];
        
        // When - 并发启动多个异步操作
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < operationCount; i++) {
            final int index = i;
            futures[i] = operator.processAsync("input_" + index);
        }
        
        // 等待所有操作完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
        allFutures.get(2, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        
        // Then - 验证结果
        for (int i = 0; i < operationCount; i++) {
            assertThat(futures[i].isDone()).isTrue();
            String result = futures[i].get();
            assertThat(result).isEqualTo("async_processed: input_" + i);
        }
        
        // 并发执行应该比串行执行快
        assertThat(endTime - startTime).isLessThan(operationCount * 50L);
        
        operator.shutdown();
    }
    
    @Test
    @DisplayName("异步操作的取消")
    void testAsyncOperationCancellation() throws Exception {
        // Given
        TestAsyncOperator slowOperator = new TestAsyncOperator(1000); // 1秒延迟
        CompletableFuture<String> future = slowOperator.processAsync("test input");
        
        // When
        boolean cancelled = future.cancel(true);
        
        // Then
        assertThat(cancelled).isTrue();
        assertThat(future.isCancelled()).isTrue();
        
        // 验证取消后获取结果会抛出异常
        assertThatThrownBy(() -> future.get(100, TimeUnit.MILLISECONDS))
            .isInstanceOf(ExecutionException.class);
        
        slowOperator.shutdown();
    }
    
    @Test
    @DisplayName("异步操作超时")
    void testAsyncOperationTimeout() throws Exception {
        // Given
        TestAsyncOperator slowOperator = new TestAsyncOperator(2000); // 2秒延迟
        
        // When & Then
        CompletableFuture<String> future = slowOperator.processAsync("test input");
        assertThatThrownBy(() -> future.get(500, TimeUnit.MILLISECONDS))
            .isInstanceOf(TimeoutException.class);
        
        slowOperator.shutdown();
    }
    
    @Test
    @DisplayName("异步操作的组合")
    void testAsyncOperationComposition() throws Exception {
        // Given
        TestAsyncOperator operator1 = new TestAsyncOperator(50);
        TestAsyncOperator operator2 = new TestAsyncOperator(30);
        
        // When - 使用thenCompose组合异步操作
        CompletableFuture<String> composedFuture = operator1.processAsync("input")
            .thenCompose(result -> operator2.processAsync(result));
        
        String finalResult = composedFuture.get(1, TimeUnit.SECONDS);
        
        // Then
        assertThat(finalResult).isEqualTo("async_processed: async_processed: input");
        
        operator1.shutdown();
        operator2.shutdown();
    }
    
    @Test
    @DisplayName("异步操作的异常恢复")
    void testAsyncExceptionRecovery() throws Exception {
        // Given
        FailingAsyncOperator failingOperator = new FailingAsyncOperator();
        TestAsyncOperator recoveryOperator = new TestAsyncOperator(10);
        
        // When - 使用exceptionally进行异常恢复
        CompletableFuture<String> recoveredFuture = failingOperator.processAsync("input")
            .exceptionally(ex -> "fallback_input")
            .thenCompose(recoveryInput -> recoveryOperator.processAsync(recoveryInput));
        
        String result = recoveredFuture.get(1, TimeUnit.SECONDS);
        
        // Then
        assertThat(result).isEqualTo("async_processed: fallback_input");
        
        failingOperator.shutdown();
        recoveryOperator.shutdown();
    }
    
    @Test
    @DisplayName("异步操作的线程安全性")
    void testAsyncOperationThreadSafety() throws Exception {
        // Given
        int threadCount = 5;
        int operationsPerThread = 20;
        TestAsyncOperator operator = new TestAsyncOperator(10);
        
        // When - 多线程并发调用异步操作
        CompletableFuture<String>[] allFutures = new CompletableFuture[threadCount * operationsPerThread];
        int index = 0;
        
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            for (int op = 0; op < operationsPerThread; op++) {
                final int operationId = op;
                allFutures[index] = operator.processAsync("thread_" + threadId + "_op_" + operationId);
                index++;
            }
        }
        
        // 等待所有操作完成
        CompletableFuture<Void> allOperations = CompletableFuture.allOf(allFutures);
        allOperations.get(5, TimeUnit.SECONDS);
        
        // Then - 验证所有操作都成功完成
        for (CompletableFuture<String> future : allFutures) {
            assertThat(future.isDone()).isTrue();
            assertThat(future.isCompletedExceptionally()).isFalse();
            String result = future.get();
            assertThat(result).startsWith("async_processed: thread_");
        }
        
        operator.shutdown();
    }
    
    @Test
    @DisplayName("异步操作的资源清理")
    void testAsyncOperationResourceCleanup() throws Exception {
        // Given
        TestAsyncOperator operator = new TestAsyncOperator(50);
        
        // When - 执行一些异步操作
        CompletableFuture<String> future1 = operator.processAsync("input1");
        CompletableFuture<String> future2 = operator.processAsync("input2");
        
        // 等待操作完成
        future1.get(1, TimeUnit.SECONDS);
        future2.get(1, TimeUnit.SECONDS);
        
        // 清理资源
        operator.shutdown();
        
        // Then - 验证资源已清理（通过检查内部状态）
        assertThat(operator.isShutdown()).isTrue();
    }
    
    // 测试辅助类
    
    private static class TestAsyncOperator extends AsyncOperator<String, String> {
        private final long delayMs;
        private volatile boolean isShutdown = false;
        
        public TestAsyncOperator() {
            this(0);
        }
        
        public TestAsyncOperator(long delayMs) {
            this.delayMs = delayMs;
        }
        
        @Override
        protected CompletableFuture<String> processAsync(String input) {
            return CompletableFuture.supplyAsync(() -> {
                if (isShutdown) {
                    throw new RuntimeException("Operator is shutdown");
                }
                
                try {
                    if (delayMs > 0) {
                        Thread.sleep(delayMs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted", e);
                }
                
                return "async_processed: " + input;
            });
        }
        
        public void shutdown() {
            isShutdown = true;
        }
        
        public boolean isShutdown() {
            return isShutdown;
        }
    }
    
    private static class FailingAsyncOperator extends AsyncOperator<String, String> {
        @Override
        protected CompletableFuture<String> processAsync(String input) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(50); // 模拟一些处理时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                throw new RuntimeException("Async processing failed");
            });
        }
    }
}