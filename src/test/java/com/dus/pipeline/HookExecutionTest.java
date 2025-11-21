package com.dus.pipeline;

import com.dus.pipeline.core.*;
import com.dus.pipeline.hook.db.DatabaseCleanupHook;
import com.dus.pipeline.hook.db.TemporaryTableCreationHook;
import com.dus.pipeline.hook.db.TemporaryTableCleanupHook;
import com.dus.pipeline.hook.notification.NotificationHook;
import com.dus.pipeline.exception.HookExecutionException;
import com.dus.pipeline.exception.PipelineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pipeline 生命周期钩子测试
 */
public class HookExecutionTest {
    
    @Mock
    private DataSource mockDataSource;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private Statement mockStatement;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
    }
    
    @Test
    @DisplayName("单个 BeforePipelineHook 执行")
    void testSingleBeforeHookExecution() throws Exception {
        // Given
        TestBeforeHook hook = new TestBeforeHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(hook)
            .addOperator(new NoOpOperator());
        
        // When
        pipeline.run();
        
        // Then
        assertEquals(1, hook.getBeforeCallCount());
        assertTrue(hook.isInitialized());
    }
    
    @Test
    @DisplayName("多个 BeforePipelineHook 顺序执行")
    void testMultipleBeforeHooksExecution() throws Exception {
        // Given
        TestBeforeHook hook1 = new TestBeforeHook();
        TestBeforeHook hook2 = new TestBeforeHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(hook1)
            .addBeforeHook(hook2)
            .addOperator(new NoOpOperator());
        
        // When
        pipeline.run();
        
        // Then
        assertEquals(1, hook1.getBeforeCallCount());
        assertEquals(1, hook2.getBeforeCallCount());
        assertTrue(hook1.isInitialized());
        assertTrue(hook2.isInitialized());
    }
    
    @Test
    @DisplayName("单个 AfterPipelineHook 执行")
    void testSingleAfterHookExecution() throws Exception {
        // Given
        TestAfterHook hook = new TestAfterHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addOperator(new NoOpOperator())
            .addAfterHook(hook);
        
        // When
        pipeline.run();
        
        // Then
        assertEquals(1, hook.getAfterCallCount());
        assertEquals(0, hook.getFailureCallCount());
        assertTrue(hook.isInitialized());
    }
    
    @Test
    @DisplayName("多个 AfterPipelineHook 顺序执行")
    void testMultipleAfterHooksExecution() throws Exception {
        // Given
        TestAfterHook hook1 = new TestAfterHook();
        TestAfterHook hook2 = new TestAfterHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addOperator(new NoOpOperator())
            .addAfterHook(hook1)
            .addAfterHook(hook2);
        
        // When
        pipeline.run();
        
        // Then
        assertEquals(1, hook1.getAfterCallCount());
        assertEquals(1, hook2.getAfterCallCount());
        assertEquals(0, hook1.getFailureCallCount());
        assertEquals(0, hook2.getFailureCallCount());
    }
    
    @Test
    @DisplayName("BeforePipelineHook 异常导致 Pipeline 中止")
    void testBeforeHookExceptionAbortsPipeline() {
        // Given
        TestBeforeHook failingHook = new TestBeforeHook(true); // 会抛出异常
        TestAfterHook afterHook = new TestAfterHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(failingHook)
            .addOperator(new NoOpOperator())
            .addAfterHook(afterHook);
        
        // When & Then
        HookExecutionException exception = assertThrows(HookExecutionException.class, pipeline::run);
        assertTrue(exception.getMessage().contains("TestBeforeHook"));
        assertEquals(0, afterHook.getAfterCallCount()); // 后置钩子不应该执行
    }
    
    @Test
    @DisplayName("Pipeline 成功后触发 afterPipeline()")
    void testAfterPipelineCalledOnSuccess() throws Exception {
        // Given
        TestAfterHook hook = new TestAfterHook();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addOperator(new NoOpOperator())
            .addAfterHook(hook);
        
        // When
        pipeline.run();
        
        // Then
        assertEquals(1, hook.getAfterCallCount());
        assertEquals(0, hook.getFailureCallCount());
        assertEquals(PipelineStatus.STOPPED, pipeline.getPipelineStatus());
    }
    
    @Test
    @DisplayName("Pipeline 失败后触发 onPipelineFailure()")
    void testOnPipelineFailureCalledOnFailure() {
        // Given
        TestAfterHook hook = new TestAfterHook();
        FailingOperator failingOperator = new FailingOperator();
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addOperator(failingOperator)
            .addAfterHook(hook);
        
        // When
        assertThrows(PipelineException.class, pipeline::run);
        
        // Then
        assertEquals(0, hook.getAfterCallCount());
        assertEquals(1, hook.getFailureCallCount());
        assertEquals(PipelineStatus.FAILED, pipeline.getPipelineStatus());
    }
    
    @Test
    @DisplayName("临时表钩子集成测试")
    void testTemporaryTableHooksIntegration() throws Exception {
        // Given
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(new TemporaryTableCreationHook(mockDataSource, "CREATE TEMP TABLE test (id INT)"))
            .addOperator(new NoOpOperator())
            .addAfterHook(new TemporaryTableCleanupHook(mockDataSource, "DROP TABLE test"));
        
        // When
        pipeline.run();
        
        // Then
        verify(mockStatement).execute("CREATE TEMP TABLE test (id INT)");
        verify(mockStatement).execute("DROP TABLE test");
    }
    
    // 测试辅助类
    static class TestSource implements Source<List<String>> {
        private int count = 0;
        
        @Override
        public List<String> nextBatch() {
            return count++ < 2 ? Arrays.asList("test" + count) : null;
        }
        
        @Override
        public String name() {
            return "TestSource";
        }
    }
    
    static class NoOpOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            return input;
        }
        
        @Override
        public String name() {
            return "NoOpOperator";
        }
    }
    
    static class FailingOperator implements Operator<List<String>, List<String>> {
        @Override
        public List<String> process(List<String> input) {
            throw new RuntimeException("Operator failed intentionally");
        }
        
        @Override
        public String name() {
            return "FailingOperator";
        }
    }
    
    static class TestBeforeHook implements BeforePipelineHook {
        private final boolean shouldFail;
        private final AtomicInteger beforeCallCount = new AtomicInteger(0);
        private boolean initialized = false;
        
        public TestBeforeHook() {
            this(false);
        }
        
        public TestBeforeHook(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }
        
        @Override
        public void initialize() throws Exception {
            initialized = true;
            if (shouldFail) {
                throw new RuntimeException("TestBeforeHook initialization failed");
            }
        }
        
        @Override
        public void beforePipeline(PipelineContext context) throws Exception {
            beforeCallCount.incrementAndGet();
            if (shouldFail) {
                throw new RuntimeException("TestBeforeHook beforePipeline failed");
            }
        }
        
        @Override
        public String name() {
            return "TestBeforeHook";
        }
        
        public int getBeforeCallCount() {
            return beforeCallCount.get();
        }
        
        public boolean isInitialized() {
            return initialized;
        }
    }
    
    static class TestAfterHook implements AfterPipelineHook {
        private final AtomicInteger afterCallCount = new AtomicInteger(0);
        private final AtomicInteger failureCallCount = new AtomicInteger(0);
        private final AtomicInteger interruptedCallCount = new AtomicInteger(0);
        private boolean initialized = false;
        
        @Override
        public void initialize() throws Exception {
            initialized = true;
        }
        
        @Override
        public void afterPipeline(PipelineContext context) throws Exception {
            afterCallCount.incrementAndGet();
        }
        
        @Override
        public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
            failureCallCount.incrementAndGet();
        }
        
        @Override
        public void onPipelineInterrupted(PipelineContext context) throws Exception {
            interruptedCallCount.incrementAndGet();
        }
        
        @Override
        public String name() {
            return "TestAfterHook";
        }
        
        public int getAfterCallCount() {
            return afterCallCount.get();
        }
        
        public int getFailureCallCount() {
            return failureCallCount.get();
        }
        
        public int getInterruptedCallCount() {
            return interruptedCallCount.get();
        }
        
        public boolean isInitialized() {
            return initialized;
        }
    }
}