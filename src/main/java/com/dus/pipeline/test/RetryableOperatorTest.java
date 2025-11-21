package com.dus.pipeline.test;

import com.dus.pipeline.retry.*;
import com.dus.pipeline.exception.OperatorException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * RetryableOperator 测试
 */
public class RetryableOperatorTest {
    
    @Test
    public void testRetryableOperatorSuccessOnFirstAttempt() {
        TestRetryableOperator operator = new TestRetryableOperator(false); // 不失败
        
        String result = operator.process("test_input");
        
        assertEquals("processed: test_input", result);
        assertEquals(1, operator.getAttemptCount()); // 只尝试一次
    }
    
    @Test
    public void testRetryableOperatorRetrySuccess() {
        TestRetryableOperator operator = new TestRetryableOperator(true, 2); // 失败2次后成功
        
        String result = operator.process("test_input");
        
        assertEquals("processed: test_input", result);
        assertEquals(3, operator.getAttemptCount()); // 失败2次 + 成功1次
    }
    
    @Test
    public void testRetryableOperatorRetryFailure() {
        TestRetryableOperator operator = new TestRetryableOperator(true, -1); // 总是失败
        
        assertThrows(OperatorException.class, () -> {
            operator.process("test_input");
        });
        
        assertEquals(3, operator.getAttemptCount()); // 默认最大重试3次
    }
    
    @Test
    public void testRetryableOperatorWithCustomRetryStrategy() {
        TestRetryableOperator operator = new TestRetryableOperator(true, 1); // 失败1次后成功
        operator.withRetryStrategy(new FixedDelayRetryStrategy(5, 100)); // 最多重试5次
        
        String result = operator.process("test_input");
        
        assertEquals("processed: test_input", result);
        assertEquals(2, operator.getAttemptCount()); // 失败1次 + 成功1次
    }
    
    @Test
    public void testRetryableOperatorWithSkipStrategy() {
        TestRetryableOperator operator = new TestRetryableOperator(true, -1); // 总是失败
        operator.withSkipStrategy(new SkipFailedRecordsStrategy(2)); // 2次尝试后跳过
        
        String result = operator.process("test_input");
        
        assertEquals("default_value", result); // 返回默认值
        assertEquals(2, operator.getAttemptCount()); // 尝试2次后跳过
    }
    
    @Test
    public void testRetryableOperatorRetryAndSkipCombination() {
        TestRetryableOperator operator = new TestRetryableOperator(true, -1); // 总是失败
        operator.withRetryStrategy(new FixedDelayRetryStrategy(2, 50)); // 重试2次
        operator.withSkipStrategy(new SkipFailedRecordsStrategy(1)); // 1次尝试后跳过
        
        String result = operator.process("test_input");
        
        assertEquals("default_value", result); // 跳过策略优先级更高
        assertEquals(1, operator.getAttemptCount()); // 只尝试1次就跳过
    }
    
    @Test
    public void testRetryableOperatorWithExponentialBackoff() {
        TestRetryableOperator operator = new TestRetryableOperator(true, 2); // 失败2次后成功
        operator.withRetryStrategy(new ExponentialBackoffRetryStrategy(5, 100, 1000, 2.0));
        
        long startTime = System.currentTimeMillis();
        String result = operator.process("test_input");
        long endTime = System.currentTimeMillis();
        
        assertEquals("processed: test_input", result);
        assertEquals(3, operator.getAttemptCount());
        
        // 验证等待时间（第1次重试等待100ms，第2次重试等待200ms）
        assertTrue(endTime - startTime >= 250); // 至少等待250ms（加上执行时间）
    }
    
    @Test
    public void testRetryableOperatorExceptionFiltering() {
        TestRetryableOperator operator = new TestRetryableOperator(true, -1, IllegalStateException.class);
        operator.withRetryStrategy(new FixedDelayRetryStrategy(3, 100)
            .removeRetryableException(Exception.class)
            .addRetryableException(IllegalStateException.class));
        
        assertThrows(OperatorException.class, () -> {
            operator.process("test_input");
        });
        
        assertEquals(3, operator.getAttemptCount()); // IllegalStateException 会重试
    }
    
    @Test
    public void testRetryableOperatorGetStrategies() {
        TestRetryableOperator operator = new TestRetryableOperator(false);
        FixedDelayRetryStrategy retryStrategy = new FixedDelayRetryStrategy(2, 500);
        SkipFailedRecordsStrategy skipStrategy = new SkipFailedRecordsStrategy(1);
        
        operator.withRetryStrategy(retryStrategy)
               .withSkipStrategy(skipStrategy);
        
        assertEquals(retryStrategy, operator.getRetryStrategy());
        assertEquals(skipStrategy, operator.getSkipStrategy());
    }
    
    // 测试用的 RetryableOperator 实现
    private static class TestRetryableOperator extends RetryableOperator<String, String> {
        
        private final boolean shouldFail;
        private final int failBeforeSuccess; // 失败多少次后成功（-1表示总是失败）
        private final Class<? extends Exception> exceptionType;
        private final AtomicInteger attemptCount = new AtomicInteger(0);
        
        public TestRetryableOperator(boolean shouldFail) {
            this(shouldFail, -1, RuntimeException.class);
        }
        
        public TestRetryableOperator(boolean shouldFail, int failBeforeSuccess) {
            this(shouldFail, failBeforeSuccess, RuntimeException.class);
        }
        
        public TestRetryableOperator(boolean shouldFail, int failBeforeSuccess, 
                                    Class<? extends Exception> exceptionType) {
            this.shouldFail = shouldFail;
            this.failBeforeSuccess = failBeforeSuccess;
            this.exceptionType = exceptionType;
        }
        
        @Override
        protected String doProcess(String input) {
            int currentAttempt = attemptCount.incrementAndGet();
            
            if (shouldFail && (failBeforeSuccess == -1 || currentAttempt <= failBeforeSuccess)) {
                try {
                    throw exceptionType.getDeclaredConstructor(String.class)
                        .newInstance("Simulated failure at attempt " + currentAttempt);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            return "processed: " + input;
        }
        
        @Override
        protected String getDefaultValue() {
            return "default_value";
        }
        
        public int getAttemptCount() {
            return attemptCount.get();
        }
    }
}