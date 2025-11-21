package com.dus.pipeline.test;

import com.dus.pipeline.core.*;
import com.dus.pipeline.context.PipelineContext;
import com.dus.pipeline.retry.*;
import com.dus.pipeline.example.Data;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pipeline 重试集成测试
 */
public class PipelineRetryIntegrationTest {
    
    @Test
    public void testMultipleOperatorsWithIndependentRetryStrategies() {
        // 创建会失败的算子
        FailingOperator operator1 = new FailingOperator(2, RuntimeException.class); // 失败2次
        FailingOperator operator2 = new FailingOperator(1, IllegalStateException.class); // 失败1次
        
        PipelineContext context = new PipelineContext();
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(new TestSourceOperator())
            .withContext(context)
            .addOperator(operator1.withRetryStrategy(new FixedDelayRetryStrategy(3, 50)))
            .addOperator(operator2.withRetryStrategy(new ExponentialBackoffRetryStrategy(2, 100, 500, 2.0)));
        
        assertDoesNotThrow(() -> pipeline.run());
        
        // 验证每个算子的重试次数
        assertEquals(3, operator1.getAttemptCount()); // 失败2次 + 成功1次
        assertEquals(2, operator2.getAttemptCount()); // 失败1次 + 成功1次
    }
    
    @Test
    public void testContextConsistencyDuringRetry() {
        TestSourceOperator source = new TestSourceOperator();
        ContextTrackingOperator operator = new ContextTrackingOperator(1); // 失败1次
        
        PipelineContext context = new PipelineContext();
        context.setProperty("initial_value", "unchanged");
        context.setProperty("counter", 0);
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator.withRetryStrategy(new FixedDelayRetryStrategy(3, 50)));
        
        pipeline.run();
        
        // 验证 Context 在重试过程中保持一致
        assertEquals("unchanged", context.getProperty("initial_value"));
        assertEquals(2, context.getProperty("attempt_counter")); // 2次尝试
        assertEquals("modified_by_operator", context.getProperty("modified_value"));
        
        // 验证 Context 对象本身是同一个
        assertSame(context, operator.getContext());
    }
    
    @Test
    public void testPerformanceMonitoringWithRetry() {
        TestSourceOperator source = new TestSourceOperator();
        FailingOperator operator = new FailingOperator(1, RuntimeException.class);
        
        PipelineContext context = new PipelineContext();
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator.withRetryStrategy(new FixedDelayRetryStrategy(3, 100)));
        
        long startTime = System.currentTimeMillis();
        pipeline.run();
        long endTime = System.currentTimeMillis();
        
        // 验证执行时间包含重试等待时间
        assertTrue(endTime - startTime >= 100); // 至少等待100ms重试延迟
        
        // 验证重试统计信息
        assertEquals(2, operator.getAttemptCount());
    }
    
    @Test
    public void testSkipStrategyIntegration() {
        TestSourceOperator source = new TestSourceOperator();
        FailingOperator operator = new FailingOperator(-1, RuntimeException.class); // 总是失败
        
        PipelineContext context = new PipelineContext();
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator
                .withRetryStrategy(new FixedDelayRetryStrategy(2, 50))
                .withSkipStrategy(new SkipFailedRecordsStrategy(2)));
        
        pipeline.run();
        
        // 验证算子在重试失败后被跳过
        assertEquals(2, operator.getAttemptCount());
    }
    
    @Test
    public void testMixedRetryAndSkipStrategies() {
        TestSourceOperator source = new TestSourceOperator();
        
        // 第一个算子：重试成功
        FailingOperator operator1 = new FailingOperator(1, RuntimeException.class);
        
        // 第二个算子：跳过失败
        FailingOperator operator2 = new FailingOperator(-1, IllegalStateException.class);
        
        PipelineContext context = new PipelineContext();
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(operator1.withRetryStrategy(new FixedDelayRetryStrategy(3, 50)))
            .addOperator(operator2
                .withRetryStrategy(new FixedDelayRetryStrategy(2, 50))
                .withSkipStrategy(new SkipFailedRecordsStrategy(2)));
        
        pipeline.run();
        
        // 验证第一个算子重试成功
        assertEquals(2, operator1.getAttemptCount());
        
        // 验证第二个算子被跳过
        assertEquals(2, operator2.getAttemptCount());
    }
    
    @Test
    public void testAsyncOperatorWithRetry() {
        TestSourceOperator source = new TestSourceOperator();
        AsyncFailingOperator asyncOperator = new AsyncFailingOperator(1);
        
        PipelineContext context = new PipelineContext();
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .withContext(context)
            .addOperator(asyncOperator.withRetryStrategy(new FixedDelayRetryStrategy(3, 50)));
        
        assertDoesNotThrow(() -> pipeline.run());
        
        // 验证异步算子的重试
        assertEquals(2, asyncOperator.getAttemptCount());
    }
    
    @Test
    public void testPipelineHooksWithRetry() {
        AtomicInteger preHookCount = new AtomicInteger(0);
        AtomicInteger postHookCount = new AtomicInteger(0);
        
        TestSourceOperator source = new TestSourceOperator();
        FailingOperator operator = new FailingOperator(1, RuntimeException.class);
        
        Pipeline<List<Data>, Void> pipeline = new Pipeline<>(source)
            .addPreHook(preHookCount::incrementAndGet)
            .addPostHook(postHookCount::incrementAndGet)
            .addOperator(operator.withRetryStrategy(new FixedDelayRetryStrategy(3, 50)));
        
        pipeline.run();
        
        // 验证钩子只执行一次，不受重试影响
        assertEquals(1, preHookCount.get());
        assertEquals(1, postHookCount.get());
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
    
    // 测试用的失败算子
    private static class FailingOperator extends RetryableOperator<List<Data>, List<Data>> {
        private final int failCount;
        private final Class<? extends Exception> exceptionType;
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        
        public FailingOperator(int failCount, Class<? extends Exception> exceptionType) {
            this.failCount = failCount;
            this.exceptionType = exceptionType;
        }
        
        @Override
        protected List<Data> doProcess(List<Data> input) {
            int currentAttempt = attemptCount.incrementAndGet();
            
            if (failCount == -1 || currentAttempt <= failCount) {
                try {
                    throw exceptionType.getDeclaredConstructor(String.class)
                        .newInstance("Simulated failure at attempt " + currentAttempt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            return input;
        }
        
        @Override
        protected List<Data> getDefaultValue() {
            return new ArrayList<>();
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
    }
    
    // 测试用的上下文跟踪算子
    private static class ContextTrackingOperator extends RetryableOperator<List<Data>, List<Data>> {
        private final int failCount;
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        
        public ContextTrackingOperator(int failCount) {
            this.failCount = failCount;
        }
        
        @Override
        protected List<Data> doProcess(List<Data> input) {
            int currentAttempt = attemptCount.incrementAndGet();
            
            // 更新 Context 中的尝试计数
            int counter = getContextProperty("attempt_counter", 0);
            setContextProperty("attempt_counter", counter + 1);
            
            // 修改 Context 属性
            setContextProperty("modified_value", "modified_by_operator");
            
            if (currentAttempt <= failCount) {
                throw new RuntimeException("Simulated failure at attempt " + currentAttempt);
            }
            
            return input;
        }
        
        @Override
        protected List<Data> getDefaultValue() {
            return new ArrayList<>();
        }
    }
    
    // 测试用的异步失败算子
    private static class AsyncFailingOperator extends RetryableOperator<List<Data>, List<Data>> {
        private final int failCount;
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        
        public AsyncFailingOperator(int failCount) {
            this.failCount = failCount;
        }
        
        @Override
        protected List<Data> doProcess(List<Data> input) {
            int currentAttempt = attemptCount.incrementAndGet();
            
            if (currentAttempt <= failCount) {
                throw new RuntimeException("Async simulated failure at attempt " + currentAttempt);
            }
            
            return input;
        }
        
        @Override
        protected List<Data> getDefaultValue() {
            return new ArrayList<>();
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
    }
}