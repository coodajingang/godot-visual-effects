package com.dus.pipeline.hook.db;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.AfterPipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 临时表清理钩子
 */
public class TemporaryTableCleanupHook implements AfterPipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporaryTableCleanupHook.class);
    
    private DataSource dataSource;
    private String dropTableSql;  // e.g., "DROP TABLE IF EXISTS temp_table"
    
    public TemporaryTableCleanupHook(DataSource dataSource, String dropTableSql) {
        this.dataSource = dataSource;
        this.dropTableSql = dropTableSql;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证数据源和SQL语句
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (dropTableSql == null || dropTableSql.trim().isEmpty()) {
            throw new IllegalArgumentException("Drop table SQL cannot be null or empty");
        }
    }
    
    @Override
    public void afterPipeline(PipelineContext context) throws Exception {
        if (context.getProperty("temp_table_created") == Boolean.TRUE) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(dropTableSql);
                logger.info("Temporary table dropped: {}", dropTableSql);
            }
        } else {
            logger.info("Temporary table was not created, skipping cleanup");
        }
    }
    
    @Override
    public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
        // 失败时也需要清理临时表
        logger.info("Cleaning up temporary table after pipeline failure");
        afterPipeline(context);
    }
    
    @Override
    public void onPipelineInterrupted(PipelineContext context) throws Exception {
        // 中断时也需要清理临时表
        logger.info("Cleaning up temporary table after pipeline interruption");
        afterPipeline(context);
    }
    
    @Override
    public String name() {
        return "TemporaryTableCleanupHook";
    }
}