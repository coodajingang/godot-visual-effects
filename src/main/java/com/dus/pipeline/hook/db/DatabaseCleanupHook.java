package com.dus.pipeline.hook.db;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.BeforePipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库清理钩子
 */
public class DatabaseCleanupHook implements BeforePipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupHook.class);
    
    private DataSource dataSource;
    private String cleanupSql;  // e.g., "DELETE FROM temp_table"
    
    public DatabaseCleanupHook(DataSource dataSource, String cleanupSql) {
        this.dataSource = dataSource;
        this.cleanupSql = cleanupSql;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证数据源和SQL语句
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (cleanupSql == null || cleanupSql.trim().isEmpty()) {
            throw new IllegalArgumentException("Cleanup SQL cannot be null or empty");
        }
    }
    
    @Override
    public void beforePipeline(PipelineContext context) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            int affectedRows = stmt.executeUpdate(cleanupSql);
            logger.info("Database cleaned: {}, affected rows: {}", cleanupSql, affectedRows);
            context.setProperty("database_cleanup_rows", affectedRows);
        }
    }
    
    @Override
    public String name() {
        return "DatabaseCleanupHook";
    }
}