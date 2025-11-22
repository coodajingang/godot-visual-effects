package com.dus.pipeline.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * SourceOperator 单元测试
 * 验证数据源算子的模板方法模式和数据处理流程
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("SourceOperator 测试")
class SourceOperatorTest {
    
    private TestSourceOperator sourceOperator;
    
    @BeforeEach
    void setUp() {
        sourceOperator = new TestSourceOperator(3, 2);
    }
    
    @Test
    @DisplayName("SourceOperator 模板方法流程：before -> doNextBatch -> after")
    void testTemplateMethodExecutionOrder() throws Exception {
        // When
        String result1 = sourceOperator.nextBatch();
        String result2 = sourceOperator.nextBatch();
        String result3 = sourceOperator.nextBatch();
        String result4 = sourceOperator.nextBatch(); // 应该返回null
        
        // Then
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();
        assertThat(result4).isNull();
        
        // 验证执行顺序
        assertThat(sourceOperator.getExecutionOrder()).isEqualTo("before-doNextBatch-after-".repeat(3) + "before-doNextBatch-after");
        assertThat(sourceOperator.isBeforeCalled()).isTrue();
        assertThat(sourceOperator.isDoNextBatchCalled()).isTrue();
        assertThat(sourceOperator.isAfterCalled()).isTrue();
    }
    
    @Test
    @DisplayName("nextBatch() 调用链")
    void testNextBatchCallChain() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(2, 3);
        
        // When
        String batch1 = source.nextBatch();
        String batch2 = source.nextBatch();
        String batch3 = source.nextBatch();
        
        // Then
        assertThat(batch1).contains("Data_0_0");
        assertThat(batch1).contains("Data_0_1");
        assertThat(batch1).contains("Data_0_2");
        
        assertThat(batch2).contains("Data_1_0");
        assertThat(batch2).contains("Data_1_1");
        assertThat(batch2).contains("Data_1_2");
        
        assertThat(batch3).isNull();
        
        assertThat(source.getCurrentBatch()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("数据分批读取")
    void testBatchDataReading() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 5);
        
        // When
        String batch = source.nextBatch();
        
        // Then
        assertThat(batch).isNotNull();
        assertThat(batch.split(",")).hasSize(5);
        
        for (int i = 0; i < 5; i++) {
            assertThat(batch).contains("Data_0_" + i + "_");
        }
    }
    
    @Test
    @DisplayName("before() 抛异常时处理")
    void testBeforeException() throws Exception {
        // Given
        BeforeFailingSourceOperator source = new BeforeFailingSourceOperator();
        
        // When & Then
        assertThatThrownBy(() -> source.nextBatch())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Before method failed");
        
        assertThat(source.isBeforeCalled()).isTrue();
        assertThat(source.isDoNextBatchCalled()).isFalse();
        assertThat(source.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("doNextBatch() 抛异常时处理")
    void testDoNextBatchException() throws Exception {
        // Given
        DoNextBatchFailingSourceOperator source = new DoNextBatchFailingSourceOperator();
        
        // When & Then
        assertThatThrownBy(() -> source.nextBatch())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("doNextBatch failed");
        
        assertThat(source.isBeforeCalled()).isTrue();
        assertThat(source.isDoNextBatchCalled()).isTrue();
        assertThat(source.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("after() 抛异常时处理")
    void testAfterException() throws Exception {
        // Given
        AfterFailingSourceOperator source = new AfterFailingSourceOperator();
        
        // When & Then
        assertThatThrownBy(() -> source.nextBatch())
            .isInstanceOf(RuntimeException.class)
            .hasMessage("After method failed");
        
        assertThat(source.isBeforeCalled()).isTrue();
        assertThat(source.isDoNextBatchCalled()).isTrue();
        assertThat(source.isAfterCalled()).isTrue();
    }
    
    @Test
    @DisplayName("process() 方法调用")
    void testProcessMethod() throws Exception {
        // Given
        TestSourceOperator source = new TestSourceOperator(1, 2);
        
        // When
        String result = source.process(null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("Data_0_0");
        assertThat(result).contains("Data_0_1");
    }
    
    @Test
    @DisplayName("name() 默认返回类名")
    void testDefaultName() {
        // When
        String name = sourceOperator.name();
        
        // Then
        assertThat(name).isEqualTo("TestSourceOperator");
    }
    
    @Test
    @DisplayName("空批次处理")
    void testEmptyBatch() throws Exception {
        // Given
        EmptyBatchSourceOperator source = new EmptyBatchSourceOperator();
        
        // When
        String batch = source.nextBatch();
        
        // Then
        assertThat(batch).isEqualTo("");
        assertThat(source.isDoNextBatchCalled()).isTrue();
    }
    
    // 测试辅助类
    
    private static class TestSourceOperator extends SourceOperator<String> {
        private final int maxBatches;
        private final int batchSize;
        private int currentBatch = 0;
        private boolean beforeCalled = false;
        private boolean doNextBatchCalled = false;
        private boolean afterCalled = false;
        private String executionOrder = "";
        
        public TestSourceOperator(int maxBatches, int batchSize) {
            this.maxBatches = maxBatches;
            this.batchSize = batchSize;
        }
        
        @Override
        protected void before() throws Exception {
            beforeCalled = true;
            executionOrder += "before-";
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            doNextBatchCalled = true;
            executionOrder += "doNextBatch-";
            
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
        
        @Override
        protected void after(String batch) throws Exception {
            afterCalled = true;
            executionOrder += "after-";
        }
        
        // Getter methods for testing
        public int getCurrentBatch() { return currentBatch; }
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoNextBatchCalled() { return doNextBatchCalled; }
        public boolean isAfterCalled() { return afterCalled; }
        public String getExecutionOrder() { return executionOrder; }
    }
    
    private static class BeforeFailingSourceOperator extends SourceOperator<String> {
        private boolean beforeCalled = false;
        private boolean doNextBatchCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before() throws Exception {
            beforeCalled = true;
            throw new RuntimeException("Before method failed");
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            doNextBatchCalled = true;
            return "test data";
        }
        
        @Override
        protected void after(String batch) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoNextBatchCalled() { return doNextBatchCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class DoNextBatchFailingSourceOperator extends SourceOperator<String> {
        private boolean beforeCalled = false;
        private boolean doNextBatchCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before() throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            doNextBatchCalled = true;
            throw new RuntimeException("doNextBatch failed");
        }
        
        @Override
        protected void after(String batch) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoNextBatchCalled() { return doNextBatchCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class AfterFailingSourceOperator extends SourceOperator<String> {
        private boolean beforeCalled = false;
        private boolean doNextBatchCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before() throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected String doNextBatch() throws Exception {
            doNextBatchCalled = true;
            return "test data";
        }
        
        @Override
        protected void after(String batch) throws Exception {
            afterCalled = true;
            throw new RuntimeException("After method failed");
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoNextBatchCalled() { return doNextBatchCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class EmptyBatchSourceOperator extends SourceOperator<String> {
        private boolean doNextBatchCalled = false;
        
        @Override
        protected String doNextBatch() throws Exception {
            doNextBatchCalled = true;
            return ""; // 空批次
        }
        
        public boolean isDoNextBatchCalled() { return doNextBatchCalled; }
    }
}