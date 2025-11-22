package com.dus.pipeline.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pipeline 单元测试
 * 验证管道的核心功能、状态管理和异常处理
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("Pipeline 测试")
class PipelineTest {
    
    @Mock
    private TestSourceOperator mockSource;
    
    @Mock
    private TestOperator mockOperator1;
    
    @Mock
    private TestOperator mockOperator2;
    
    private Pipeline<String, String> pipeline;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockSource.name()).thenReturn("MockSource");
        when(mockOperator1.name()).thenReturn("MockOperator1");
        when(mockOperator2.name()).thenReturn("MockOperator2");
    }
    
    @AfterEach
    void tearDown() {
        if (pipeline != null) {
            pipeline.shutdown();
        }
    }
    
    @Test
    @DisplayName("添加多个算子，验证执行顺序")
    void testAddMultipleOperatorsExecutionOrder() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(3, 2);
        TransformOperator transform1 = new TransformOperator("prefix1_");
        TransformOperator transform2 = new TransformOperator("prefix2_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform1)
                .addOperator(transform2);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(transform1.getProcessedCount()).isEqualTo(3);
        assertThat(transform2.getProcessedCount()).isEqualTo(3);
        
        // 验证执行顺序
        assertThat(source.getCallCount()).isEqualTo(4); // 3次数据 + 1次null
    }
    
    @Test
    @DisplayName("数据通过算子链正确传递")
    void testDataFlowThroughOperatorChain() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 1);
        TransformOperator transform = new TransformOperator("processed_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(transform.getLastProcessedInput()).isNotNull();
        assertThat(transform.getLastProcessedInput()).contains("Data_0_0_");
    }
    
    @Test
    @DisplayName("SourceOperator nextBatch() 返回 null 时停止")
    void testStopWhenSourceReturnsNull() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(0, 1); // 0个批次
        TransformOperator transform = new TransformOperator("test_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(transform.getProcessedCount()).isEqualTo(0);
        assertThat(source.getCallCount()).isEqualTo(1); // 只调用一次返回null
    }
    
    @Test
    @DisplayName("Pipeline 状态管理：INIT → RUNNING → STOPPED")
    void testPipelineStatusManagement() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 1);
        TransformOperator transform = new TransformOperator("test_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // Then - 初始状态
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.INIT);
        
        // When - 运行中
        Thread runThread = new Thread(() -> {
            try {
                pipeline.run();
            } catch (Exception e) {
                // 忽略异常
            }
        });
        runThread.start();
        
        // 等待一下让状态变为RUNNING
        Thread.sleep(50);
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.RUNNING);
        
        // 等待运行完成
        runThread.join(1000);
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
    }
    
    @Test
    @DisplayName("shutdown() 优雅关闭")
    void testGracefulShutdown() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(5, 1);
        SlowOperator slowOperator = new SlowOperator(100); // 每次处理100ms
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(slowOperator);
        
        // When - 在另一个线程中运行管道
        Thread runThread = new Thread(() -> {
            try {
                pipeline.run();
            } catch (Exception e) {
                // 忽略异常
            }
        });
        runThread.start();
        
        // 等待管道开始运行
        Thread.sleep(50);
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.RUNNING);
        
        // 关闭管道
        pipeline.shutdown();
        
        // 等待线程结束
        runThread.join(1000);
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
    }
    
    @Test
    @DisplayName("awaitTermination() 等待完成")
    void testAwaitTermination() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 1);
        TransformOperator transform = new TransformOperator("test_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // When
        Thread runThread = new Thread(() -> {
            try {
                pipeline.run();
            } catch (Exception e) {
                // 忽略异常
            }
        });
        runThread.start();
        
        // Then - 等待完成
        boolean terminated = pipeline.awaitTermination(2000);
        assertThat(terminated).isTrue();
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        
        runThread.join(100);
    }
    
    @Test
    @DisplayName("异常导致 Pipeline 失败")
    void testPipelineFailureOnException() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 1);
        FailingOperator failingOperator = new FailingOperator();
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(failingOperator);
        
        // When & Then
        assertThatThrownBy(() -> pipeline.run())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Simulated operator failure");
        
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.FAILED);
    }
    
    @Test
    @DisplayName("重复运行管道抛出异常")
    void testRunningPipelineTwiceThrowsException() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 1);
        TransformOperator transform = new TransformOperator("test_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // When - 第一次运行
        pipeline.run();
        
        // Then - 第二次运行应该抛出异常
        assertThatThrownBy(() -> pipeline.run())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already running or has been stopped");
    }
    
    @Test
    @DisplayName("获取源算子和算子列表")
    void testGetSourceAndOperators() {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 1);
        TransformOperator transform = new TransformOperator("test_");
        
        Pipeline<String, Void> pipeline = new Pipeline<>(source)
                .addOperator(transform);
        
        // When & Then
        assertThat(pipeline.getSource()).isSameAs(source);
        assertThat(pipeline.getOperators()).hasSize(1);
        assertThat(pipeline.getOperators().get(0)).isSameAs(transform);
        
        // 验证返回的是副本
        pipeline.getOperators().clear();
        assertThat(pipeline.getOperators()).hasSize(1);
    }
    
    @Test
    @DisplayName("空算子链运行")
    void testEmptyOperatorChain() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 1);
        Pipeline<String, String> pipeline = new Pipeline<>(source);
        
        // When
        pipeline.run();
        
        // Then
        assertThat(pipeline.getStatus()).isEqualTo(Pipeline.PipelineStatus.STOPPED);
        assertThat(source.getCallCount()).isEqualTo(3); // 2次数据 + 1次null
    }
    
    // 测试辅助类
    
    private static class TestSourceOperator extends SourceOperator<String> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        private int callCount = 0;
        
        public TestSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            callCount++;
            if (currentBatch >= maxBatches) {
                return null;
            }
            
            StringBuilder batch = new StringBuilder();
            for (int i = 0; i < batchSize; i++) {
                if (i > 0) batch.append(",");
                batch.append("Data_").append(currentBatch).append("_").append(i).append("_").append(System.currentTimeMillis() % 1000);
            }
            
            currentBatch++;
            return batch.toString();
        }
        
        public int getCallCount() { return callCount; }
    }
    
    private static class TransformOperator extends AbstractOperator<String, String> {
        private final String prefix;
        private int processedCount = 0;
        private String lastProcessedInput;
        
        public TransformOperator(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            processedCount++;
            lastProcessedInput = input;
            return prefix + input;
        }
        
        public int getProcessedCount() { return processedCount; }
        public String getLastProcessedInput() { return lastProcessedInput; }
    }
    
    private static class SlowOperator extends AbstractOperator<String, String> {
        private final long delayMs;
        
        public SlowOperator(long delayMs) {
            this.delayMs = delayMs;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            Thread.sleep(delayMs);
            return "processed_" + input;
        }
    }
    
    private static class FailingOperator extends AbstractOperator<String, String> {
        @Override
        protected String doProcess(String input) throws Exception {
            throw new RuntimeException("Simulated operator failure");
        }
    }
    
    private static class TestOperator extends AbstractOperator<String, String> {
        @Override
        protected String doProcess(String input) throws Exception {
            return "processed_" + input;
        }
    }
}