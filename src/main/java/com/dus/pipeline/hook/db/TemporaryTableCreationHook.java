package com.dus.pipeline.hook.db;

import com.dus.pipeline.core.PipelineContext;
import com.dus.pipeline.hook.BeforePipelineHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 临时表创建钩子
 */
public class TemporaryTableCreationHook implements BeforePipelineHook {
    
    private static final Logger logger = LoggerFactory.getLogger(TemporaryTableCreationHook.class);
    
    private DataSource dataSource;
    private String createTableSql;
    
    public TemporaryTableCreationHook(DataSource dataSource, String createTableSql) {
        this.dataSource = dataSource;
        this.createTableSql = createTableSql;
    }
    
    @Override
    public void initialize() throws Exception {
        // 验证数据源和SQL语句
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (createTableSql == null || createTableSql.trim().isEmpty()) {
            throw new IllegalArgumentException("Create table SQL cannot be null or empty");
        }
    }
    
    @Override
    public void beforePipeline(PipelineContext context) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
            context.setProperty("temp_table_created", true);
            logger.info("Temporary table created: {}", createTableSql);
        }
    }
    
    @Override
    public String name() {
        return "TemporaryTableCreationHook";
    }
}