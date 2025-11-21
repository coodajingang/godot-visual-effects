package com.dus.pipeline.test;

import com.dus.pipeline.core.*;
import com.dus.pipeline.context.PipelineContext;
import com.dus.pipeline.example.Data;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Context 注入功能测试
 */
public class ContextInjectionTest {
    
    @Test
    public void testContextAutoInjectionToSource() {
        // 创建测试源算子
        TestSourceOperator source = new TestSourceOperator();
        
        // 创建 Pipeline 和 Context
        PipelineContext context = new PipelineContext();
        context.setProperty("test_key", "test_value");
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context);
        
        // 执行 Pipeline
        pipeline.run();
        
        // 验证 Context 被注入到 source
        assertNotNull(source.getContext());
        assertEquals("test_value", source.getContextProperty("test_key"));
    }
    
    @Test
    public void testContextAutoInjectionToOperators() {
        TestSourceOperator source = new TestSourceOperator();
        TestOperator operator = new TestOperator();
        
        PipelineContext context = new PipelineContext();
        context.setProperty("operator_key", "operator_value");
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator);
        
        pipeline.run();
        
        // 验证 Context 被注入到算子
        assertNotNull(operator.getContext());
        assertEquals("operator_value", operator.getContextProperty("operator_key"));
    }
    
    @Test
    public void testOperatorCanReadContextProperties() {
        TestSourceOperator source = new TestSourceOperator();
        TestOperator operator = new TestOperator();
        
        PipelineContext context = new PipelineContext();
        context.setProperty("read_test", "success");
        
        new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator)
            .run();
        
        assertEquals("success", operator.getReadValue());
    }
    
    @Test
    public void testOperatorCanModifyContextProperties() {
        TestSourceOperator source = new TestSourceOperator();
        TestOperator operator = new TestOperator();
        
        PipelineContext context = new PipelineContext();
        
        new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator)
            .run();
        
        // 验证算子修改的 Context 属性
        assertEquals("modified_by_operator", context.getProperty("modified_key"));
    }
    
    @Test
    public void testContextSharingBetweenOperators() {
        TestSourceOperator source = new TestSourceOperator();
        TestOperator operator1 = new TestOperator();
        TestOperator operator2 = new TestOperator();
        
        PipelineContext context = new PipelineContext();
        
        new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator1)
            .addOperator(operator2)
            .run();
        
        // 验证第一个算子设置的值能被第二个算子读取
        assertEquals("modified_by_operator", operator2.getReadValue());
    }
    
    // 测试用的源算子
    private static class TestSourceOperator extends SourceOperator<List<Data>> {
        private boolean hasNext = true;
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        
        @Override
        protected List<Data> doNextBatch() {
            hasNext = false;
            return Arrays.asList(new Data("1", "test", "type"));
        }
    }
    
    // 测试用的算子
    private static class TestOperator extends AbstractOperator<List<Data>, List<Data>> {
        private String readValue;
        
        @Override
        protected List<Data> doProcess(List<Data> input) {
            // 读取 Context 属性
            readValue = (String) getContextProperty("read_test");
            
            // 修改 Context 属性
            setContextProperty("modified_key", "modified_by_operator");
            
            return input;
        }
        
        public String getReadValue() {
            return readValue;
        }
    }
}