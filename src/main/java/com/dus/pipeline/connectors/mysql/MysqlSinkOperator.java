package com.dus.pipeline.connectors.mysql;

import com.dus.pipeline.core.SinkOperator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MySQL 汇算子
 * 将数据写入 MySQL 数据库
 */
public class MysqlSinkOperator extends SinkOperator<List<Map<String, Object>>> {
    
    private final MysqlSinkConfig config;
    private final MysqlConnectionPool connectionPool;
    private final ObjectMapper objectMapper;
    private List<Map<String, Object>> batch;
    private Connection connection;
    
    public MysqlSinkOperator(MysqlSinkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.connectionPool = new MysqlConnectionPool();
        this.objectMapper = new ObjectMapper();
        this.batch = new ArrayList<>();
    }
    
    @Override
    protected void before(List<Map<String, Object>> input) throws Exception {
        connectionPool.init(
            config.getHost(),
            config.getPort(),
            config.getDatabase(),
            config.getUsername(),
            config.getPassword(),
            config.getConnectionPoolSize(),
            config.getConnectionTimeout()
        );
        
        this.connection = connectionPool.getConnection();
        this.connection.setAutoCommit(config.isAutocommit());
        this.batch = new ArrayList<>();
    }
    
    @Override
    protected void write(List<Map<String, Object>> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        for (Map<String, Object> item : input) {
            batch.add(item);
            if (batch.size() >= config.getBatchSize()) {
                flushBatch();
            }
        }
    }
    
    @Override
    protected void after(List<Map<String, Object>> input, Void output) throws Exception {
        if (!batch.isEmpty()) {
            flushBatch();
        }
        
        if (connection != null && !config.isAutocommit()) {
            try {
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
        
        if (connection != null) {
            connection.close();
        }
        connectionPool.close();
    }
    
    private void flushBatch() throws Exception {
        if (batch.isEmpty()) {
            return;
        }
        
        String sql = buildInsertSql();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Map<String, Object> row : batch) {
                setStatementParameters(stmt, row);
                stmt.addBatch();
            }
            stmt.executeBatch();
            
            if (!config.isAutocommit()) {
                connection.commit();
            }
        } catch (Exception e) {
            if (!config.isAutocommit()) {
                connection.rollback();
            }
            throw e;
        }
        
        batch.clear();
    }
    
    private String buildInsertSql() {
        String baseSql = config.getSql();
        
        switch (config.getConflictStrategy()) {
            case INSERT_IGNORE:
                return baseSql.replaceFirst("(?i)INSERT", "INSERT IGNORE");
            case ON_DUPLICATE_KEY_UPDATE:
                return baseSql + " ON DUPLICATE KEY UPDATE id=id";
            case FAIL_ON_CONFLICT:
            default:
                return baseSql;
        }
    }
    
    private void setStatementParameters(PreparedStatement stmt, Map<String, Object> row) throws Exception {
        int index = 1;
        for (Object value : row.values()) {
            stmt.setObject(index++, value);
        }
    }
    
    @Override
    public String name() {
        return "MysqlSinkOperator";
    }
}
