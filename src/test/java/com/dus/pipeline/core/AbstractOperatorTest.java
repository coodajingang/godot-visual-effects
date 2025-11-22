package com.dus.pipeline.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AbstractOperator 单元测试
 * 验证模板方法模式的正确执行流程和异常处理
 * 
 * @author Dus
 * @version 1.0
 */
@DisplayName("AbstractOperator 测试")
class AbstractOperatorTest {
    
    private TestOperator testOperator;
    
    @Mock
    private String mockInput;
    
    @Mock
    private String mockOutput;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testOperator = new TestOperator();
    }
    
    @Test
    @DisplayName("模板方法流程验证：before -> doProcess -> after 执行顺序")
    void testTemplateMethodExecutionOrder() throws Exception {
        // Given
        String input = "test input";
        String expectedOutput = "processed: test input";
        
        // When
        String result = testOperator.process(input);
        
        // Then
        assertThat(result).isEqualTo(expectedOutput);
        assertThat(testOperator.isBeforeCalled()).isTrue();
        assertThat(testOperator.isDoProcessCalled()).isTrue();
        assertThat(testOperator.isAfterCalled()).isTrue();
        
        // 验证执行顺序
        assertThat(testOperator.getExecutionOrder()).isEqualTo("before-doProcess-after");
    }
    
    @Test
    @DisplayName("before() 和 after() 可选覆盖")
    void testOptionalBeforeAfterOverride() throws Exception {
        // Given
        MinimalOperator minimalOperator = new MinimalOperator();
        String input = "test input";
        
        // When
        String result = minimalOperator.process(input);
        
        // Then
        assertThat(result).isEqualTo("processed: test input");
        // 默认的before()和after()应该不会抛出异常
    }
    
    @Test
    @DisplayName("异常处理：doProcess 抛异常时流程")
    void testExceptionHandling() throws Exception {
        // Given
        FailingOperator failingOperator = new FailingOperator();
        String input = "test input";
        
        // When & Then
        assertThatThrownBy(() -> failingOperator.process(input))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Simulated processing failure");
        
        // before应该被调用，但after不应该被调用
        assertThat(failingOperator.isBeforeCalled()).isTrue();
        assertThat(failingOperator.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("name() 默认返回类名")
    void testDefaultName() {
        // When
        String name = testOperator.name();
        
        // Then
        assertThat(name).isEqualTo("TestOperator");
    }
    
    @Test
    @DisplayName("自定义 name() 实现")
    void testCustomName() {
        // Given
        CustomNameOperator customOperator = new CustomNameOperator();
        
        // When
        String name = customOperator.name();
        
        // Then
        assertThat(name).isEqualTo("CustomProcessor");
    }
    
    @Test
    @DisplayName("before() 抛异常时处理")
    void testBeforeException() throws Exception {
        // Given
        BeforeFailingOperator operator = new BeforeFailingOperator();
        String input = "test input";
        
        // When & Then
        assertThatThrownBy(() -> operator.process(input))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Before method failed");
        
        assertThat(operator.isBeforeCalled()).isTrue();
        assertThat(operator.isDoProcessCalled()).isFalse();
        assertThat(operator.isAfterCalled()).isFalse();
    }
    
    @Test
    @DisplayName("after() 抛异常时处理")
    void testAfterException() throws Exception {
        // Given
        AfterFailingOperator operator = new AfterFailingOperator();
        String input = "test input";
        
        // When & Then
        assertThatThrownBy(() -> operator.process(input))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("After method failed");
        
        assertThat(operator.isBeforeCalled()).isTrue();
        assertThat(operator.isDoProcessCalled()).isTrue();
        assertThat(operator.isAfterCalled()).isTrue();
    }
    
    // 测试用的算子实现类
    
    private static class TestOperator extends AbstractOperator<String, String> {
        private boolean beforeCalled = false;
        private boolean doProcessCalled = false;
        private boolean afterCalled = false;
        private String executionOrder = "";
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
            executionOrder += "before-";
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            doProcessCalled = true;
            executionOrder += "doProcess-";
            return "processed: " + input;
        }
        
        @Override
        protected void after(String input, String output) throws Exception {
            afterCalled = true;
            executionOrder += "after";
        }
        
        // Getter methods for testing
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoProcessCalled() { return doProcessCalled; }
        public boolean isAfterCalled() { return afterCalled; }
        public String getExecutionOrder() { return executionOrder; }
    }
    
    private static class MinimalOperator extends AbstractOperator<String, String> {
        @Override
        protected String doProcess(String input) throws Exception {
            return "processed: " + input;
        }
    }
    
    private static class FailingOperator extends AbstractOperator<String, String> {
        private boolean beforeCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            throw new RuntimeException("Simulated processing failure");
        }
        
        @Override
        protected void after(String input, String output) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class CustomNameOperator extends AbstractOperator<String, String> {
        @Override
        protected String doProcess(String input) throws Exception {
            return "processed: " + input;
        }
        
        @Override
        public String name() {
            return "CustomProcessor";
        }
    }
    
    private static class BeforeFailingOperator extends AbstractOperator<String, String> {
        private boolean beforeCalled = false;
        private boolean doProcessCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
            throw new RuntimeException("Before method failed");
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            doProcessCalled = true;
            return "processed: " + input;
        }
        
        @Override
        protected void after(String input, String output) throws Exception {
            afterCalled = true;
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoProcessCalled() { return doProcessCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
    
    private static class AfterFailingOperator extends AbstractOperator<String, String> {
        private boolean beforeCalled = false;
        private boolean doProcessCalled = false;
        private boolean afterCalled = false;
        
        @Override
        protected void before(String input) throws Exception {
            beforeCalled = true;
        }
        
        @Override
        protected String doProcess(String input) throws Exception {
            doProcessCalled = true;
            return "processed: " + input;
        }
        
        @Override
        protected void after(String input, String output) throws Exception {
            afterCalled = true;
            throw new RuntimeException("After method failed");
        }
        
        public boolean isBeforeCalled() { return beforeCalled; }
        public boolean isDoProcessCalled() { return doProcessCalled; }
        public boolean isAfterCalled() { return afterCalled; }
    }
}