package com.dus.pipeline.connectors.mysql;

import com.dus.pipeline.core.SourceOperator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL 源算子
 * 从 MySQL 数据库读取数据
 */
public class MysqlSourceOperator extends SourceOperator<List<Map<String, Object>>> {
    
    private final MysqlSourceConfig config;
    private final MysqlConnectionPool connectionPool;
    private final ObjectMapper objectMapper;
    private int offset;
    private boolean hasMore;
    
    public MysqlSourceOperator(MysqlSourceConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.connectionPool = new MysqlConnectionPool();
        this.objectMapper = new ObjectMapper();
        this.offset = 0;
        this.hasMore = true;
    }
    
    @Override
    protected void before() throws Exception {
        connectionPool.init(
            config.getHost(),
            config.getPort(),
            config.getDatabase(),
            config.getUsername(),
            config.getPassword(),
            config.getConnectionPoolSize(),
            config.getConnectionTimeout()
        );
    }
    
    @Override
    protected List<Map<String, Object>> doNextBatch() throws Exception {
        if (!hasMore) {
            return null;
        }
        
        String sql = config.getSql();
        String pagedSql = sql + " LIMIT " + config.getPageSize() + " OFFSET " + offset;
        
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(pagedSql)) {
            
            // Set parameters
            List<Object> params = config.getSqlParams();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = resultSetToMap(rs);
                    data.add(row);
                }
            }
        }
        
        if (data.size() < config.getPageSize()) {
            hasMore = false;
        } else {
            offset += config.getPageSize();
        }
        
        return data;
    }
    
    @Override
    protected void after(List<Map<String, Object>> batch) throws Exception {
        // 默认实现，不需要特殊处理
    }
    
    private Map<String, Object> resultSetToMap(ResultSet rs) throws Exception {
        Map<String, Object> map = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            Object value = rs.getObject(i);
            map.put(columnName, value);
        }
        
        return map;
    }
    
    @Override
    public String name() {
        return "MysqlSourceOperator";
    }
}
