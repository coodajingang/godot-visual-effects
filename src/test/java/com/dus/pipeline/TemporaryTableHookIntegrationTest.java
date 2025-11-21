package com.dus.pipeline;

import com.dus.pipeline.core.*;
import com.dus.pipeline.hook.db.*;
import com.dus.pipeline.hook.cache.*;
import com.dus.pipeline.hook.notification.*;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 临时表钩子集成测试
 */
public class TemporaryTableHookIntegrationTest {
    
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
    @DisplayName("创建临时表")
    void testCreateTemporaryTable() throws Exception {
        // Given
        String createTableSql = "CREATE TEMPORARY TABLE temp_test (id INT, name VARCHAR(100))";
        TemporaryTableCreationHook hook = new TemporaryTableCreationHook(mockDataSource, createTableSql);
        PipelineContext context = new PipelineContext();
        
        // When
        hook.initialize();
        hook.beforePipeline(context);
        
        // Then
        verify(mockStatement).execute(createTableSql);
        assertEquals(Boolean.TRUE, context.getProperty("temp_table_created"));
    }
    
    @Test
    @DisplayName("写入数据到临时表")
    void testWriteDataToTemporaryTable() throws Exception {
        // Given
        String createTableSql = "CREATE TEMPORARY TABLE temp_data (id INT, name VARCHAR(100))";
        String insertSql = "INSERT INTO temp_data VALUES (1, 'test')";
        
        TemporaryTableCreationHook createHook = new TemporaryTableCreationHook(mockDataSource, createTableSql);
        TestWriteOperator writeOperator = new TestWriteOperator(mockDataSource, insertSql);
        
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(createHook)
            .addOperator(writeOperator);
        
        // When
        pipeline.run();
        
        // Then
        verify(mockStatement).execute(createTableSql);
        verify(mockStatement, times(2)).executeUpdate(insertSql); // 2 batches
    }
    
    @Test
    @DisplayName("完成后删除临时表")
    void testDropTemporaryTableAfterCompletion() throws Exception {
        // Given
        String createTableSql = "CREATE TEMPORARY TABLE temp_test (id INT)";
        String dropTableSql = "DROP TABLE IF EXISTS temp_test";
        
        TemporaryTableCreationHook createHook = new TemporaryTableCreationHook(mockDataSource, createTableSql);
        TemporaryTableCleanupHook cleanupHook = new TemporaryTableCleanupHook(mockDataSource, dropTableSql);
        
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(createHook)
            .addOperator(new NoOpOperator())
            .addAfterHook(cleanupHook);
        
        // When
        pipeline.run();
        
        // Then
        verify(mockStatement).execute(createTableSql);
        verify(mockStatement).execute(dropTableSql);
    }
    
    @Test
    @DisplayName("失败时也删除临时表")
    void testDropTemporaryTableOnFailure() {
        // Given
        String createTableSql = "CREATE TEMPORARY TABLE temp_test (id INT)";
        String dropTableSql = "DROP TABLE IF EXISTS temp_test";
        
        TemporaryTableCreationHook createHook = new TemporaryTableCreationHook(mockDataSource, createTableSql);
        TemporaryTableCleanupHook cleanupHook = new TemporaryTableCleanupHook(mockDataSource, dropTableSql);
        FailingOperator failingOperator = new FailingOperator();
        
        Pipeline<List<String>, Void> pipeline = new Pipeline<>(new TestSource())
            .addBeforeHook(createHook)
            .addOperator(failingOperator)
            .addAfterHook(cleanupHook);
        
        // When
        assertThrows(PipelineException.class, pipeline::run);
        
        // Then
        verify(mockStatement).execute(createTableSql);
        verify(mockStatement).execute(dropTableSql);
    }
    
    // 测试辅助类
    static class TestSource implements Source<List<String>> {
        private int count = 0;
        
        @Override
        public List<String> nextBatch() {
            return count++ < 1 ? Arrays.asList("test" + count) : null;
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
    
    static class TestWriteOperator implements Operator<List<String>, Void> {
        private final DataSource dataSource;
        private final String insertSql;
        
        public TestWriteOperator(DataSource dataSource, String insertSql) {
            this.dataSource = dataSource;
            this.insertSql = insertSql;
        }
        
        @Override
        public Void process(List<String> input) throws Exception {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(insertSql);
            }
            return null;
        }
        
        @Override
        public String name() {
            return "TestWriteOperator";
        }
    }
}