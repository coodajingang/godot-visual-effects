package com.dus.pipeline.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * DefaultOperatorMetrics 单元测试
 * 验证算子指标的统计功能和线程安全性
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("DefaultOperatorMetrics 测试")
class DefaultOperatorMetricsTest {
    
    private DefaultOperatorMetrics metrics;
    
    @BeforeEach
    void setUp() {
        metrics = new DefaultOperatorMetrics("TestOperator");
    }
    
    @Test
    @DisplayName("记录单个算子的成功调用次数")
    void testRecordSuccess() {
        // Given
        long duration1 = 1_000_000; // 1ms
        long duration2 = 2_000_000; // 2ms
        long duration3 = 1_500_000; // 1.5ms
        
        // When
        metrics.recordSuccess(duration1);
        metrics.recordSuccess(duration2);
        metrics.recordSuccess(duration3);
        
        // Then
        assertThat(metrics.getSuccessCount()).isEqualTo(3);
        assertThat(metrics.getFailureCount()).isEqualTo(0);
        assertThat(metrics.getTotalCount()).isEqualTo(3);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(duration1 + duration2 + duration3);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo((double)(duration1 + duration2 + duration3) / 3);
    }
    
    @Test
    @DisplayName("记录单个算子的失败次数")
    void testRecordFailure() {
        // Given
        long duration1 = 500_000; // 0.5ms
        long duration2 = 1_200_000; // 1.2ms
        
        // When
        metrics.recordFailure(duration1);
        metrics.recordFailure(duration2);
        
        // Then
        assertThat(metrics.getSuccessCount()).isEqualTo(0);
        assertThat(metrics.getFailureCount()).isEqualTo(2);
        assertThat(metrics.getTotalCount()).isEqualTo(2);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(duration1 + duration2);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo((double)(duration1 + duration2) / 2);
    }
    
    @Test
    @DisplayName("计算平均耗时、最小/最大耗时")
    void testDurationCalculations() {
        // Given
        long[] durations = {100_000, 500_000, 1_000_000, 2_000_000, 5_000_000}; // 0.1ms to 5ms
        
        // When
        for (long duration : durations) {
            metrics.recordSuccess(duration);
        }
        
        // Then
        assertThat(metrics.getMinDurationNanos()).isEqualTo(100_000);
        assertThat(metrics.getMaxDurationNanos()).isEqualTo(5_000_000);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo((100_000 + 500_000 + 1_000_000 + 2_000_000 + 5_000_000) / 5.0);
    }
    
    @Test
    @DisplayName("多线程并发记录（线程安全性）")
    void testConcurrentRecording() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // When - 并发记录指标
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        long duration = (threadId * 1000 + j) % 10_000; // 0-10ms
                        if (j % 3 == 0) {
                            metrics.recordSuccess(duration);
                            successCount.incrementAndGet();
                        } else {
                            metrics.recordFailure(duration);
                            failureCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        // Then
        assertThat(metrics.getSuccessCount()).isEqualTo(successCount.get());
        assertThat(metrics.getFailureCount()).isEqualTo(failureCount.get());
        assertThat(metrics.getTotalCount()).isEqualTo(successCount.get() + failureCount.get());
        assertThat(metrics.getTotalDurationNanos()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("reset() 清空统计数据")
    void testReset() {
        // Given - 先添加一些数据
        metrics.recordSuccess(1_000_000);
        metrics.recordFailure(500_000);
        metrics.recordSuccess(2_000_000);
        
        // When
        metrics.reset();
        
        // Then
        assertThat(metrics.getSuccessCount()).isEqualTo(0);
        assertThat(metrics.getFailureCount()).isEqualTo(0);
        assertThat(metrics.getTotalCount()).isEqualTo(0);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(0);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo(0.0);
        assertThat(metrics.getMinDurationNanos()).isEqualTo(0);
        assertThat(metrics.getMaxDurationNanos()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("异常情况下记录失败")
    void testRecordFailureWithException() {
        // Given
        RuntimeException exception = new RuntimeException("Test exception");
        long duration = 750_000;
        
        // When
        metrics.recordFailure(duration);
        
        // Then
        assertThat(metrics.getFailureCount()).isEqualTo(1);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(duration);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo(duration);
    }
    
    @Test
    @DisplayName("空指标的平均耗时计算")
    void testAverageDurationWithEmptyMetrics() {
        // When
        double avgDuration = metrics.getAverageDurationNanos();
        
        // Then
        assertThat(avgDuration).isEqualTo(0.0);
    }
    
    @Test
    @DisplayName("空指标的最小/最大耗时")
    void testMinMaxDurationWithEmptyMetrics() {
        // When
        long minDuration = metrics.getMinDurationNanos();
        long maxDuration = metrics.getMaxDurationNanos();
        
        // Then
        assertThat(minDuration).isEqualTo(0);
        assertThat(maxDuration).isEqualTo(0);
    }
    
    @Test
    @DisplayName("获取算子名称")
    void testGetOperatorName() {
        // When
        String name = metrics.getOperatorName();
        
        // Then
        assertThat(name).isEqualTo("TestOperator");
    }
    
    @Test
    @DisplayName("混合成功和失败记录")
    void testMixedSuccessAndFailureRecords() {
        // Given
        metrics.recordSuccess(1_000_000);
        metrics.recordFailure(500_000);
        metrics.recordSuccess(2_000_000);
        metrics.recordFailure(750_000);
        metrics.recordSuccess(1_500_000);
        
        // When & Then
        assertThat(metrics.getSuccessCount()).isEqualTo(3);
        assertThat(metrics.getFailureCount()).isEqualTo(2);
        assertThat(metrics.getTotalCount()).isEqualTo(5);
        assertThat(metrics.getTotalDurationNanos()).isEqualTo(1_000_000 + 500_000 + 2_000_000 + 750_000 + 1_500_000);
        assertThat(metrics.getAverageDurationNanos()).isEqualTo((1_000_000 + 500_000 + 2_000_000 + 750_000 + 1_500_000) / 5.0);
        assertThat(metrics.getMinDurationNanos()).isEqualTo(500_000);
        assertThat(metrics.getMaxDurationNanos()).isEqualTo(2_000_000);
    }
    
    @Test
    @DisplayName("高并发下的最小/最大值更新")
    @RepeatedTest(5)
    void testConcurrentMinMaxUpdate() throws InterruptedException {
        // Given
        int threadCount = 20;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // When - 并发记录不同时长的操作
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        long duration = (threadId * operationsPerThread + j) * 1000; // 确保有不同的值
                        metrics.recordSuccess(duration);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        
        // Then
        assertThat(metrics.getMinDurationNanos()).isEqualTo(0); // 第一个线程的第一个操作
        assertThat(metrics.getMaxDurationNanos()).isEqualTo((threadCount * operationsPerThread - 1) * 1000); // 最后一个操作
        assertThat(metrics.getTotalCount()).isEqualTo(threadCount * operationsPerThread);
    }
    
    @Test
    @DisplayName("长时间运行的压力测试")
    void testLongRunningStressTest() throws InterruptedException {
        // Given
        int durationSeconds = 2;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        long startTime = System.currentTimeMillis();
        AtomicInteger operationCount = new AtomicInteger(0);
        
        // When - 持续运行指定时间
        while (System.currentTimeMillis() - startTime < durationSeconds * 1000) {
            executor.submit(() -> {
                long duration = (long)(Math.random() * 10_000); // 0-10ms
                if (Math.random() > 0.3) {
                    metrics.recordSuccess(duration);
                } else {
                    metrics.recordFailure(duration);
                }
                operationCount.incrementAndGet();
            });
            
            Thread.sleep(1); // 避免CPU过载
        }
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        // Then
        assertThat(metrics.getTotalCount()).isEqualTo(operationCount.get());
        assertThat(metrics.getTotalDurationNanos()).isGreaterThan(0);
    }
}