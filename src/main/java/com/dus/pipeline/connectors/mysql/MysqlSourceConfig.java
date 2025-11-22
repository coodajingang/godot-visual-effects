package com.dus.pipeline.connectors.mysql;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 源配置类
 */
public class MysqlSourceConfig {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String sql;
    private List<Object> sqlParams;
    private int pageSize;
    private int connectionPoolSize;
    private long connectionTimeout;
    private Class<?> resultType;
    
    public MysqlSourceConfig() {
        this.port = 3306;
        this.pageSize = 1000;
        this.connectionPoolSize = 10;
        this.connectionTimeout = 30000;
        this.sqlParams = new ArrayList<>();
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getDatabase() {
        return database;
    }
    
    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public List<Object> getSqlParams() {
        return sqlParams;
    }
    
    public void setSqlParams(List<Object> sqlParams) {
        this.sqlParams = sqlParams;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }
    
    public void setConnectionPoolSize(int connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }
    
    public long getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public Class<?> getResultType() {
        return resultType;
    }
    
    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MysqlSourceConfig config = new MysqlSourceConfig();
        
        public Builder host(String host) {
            config.host = host;
            return this;
        }
        
        public Builder port(int port) {
            config.port = port;
            return this;
        }
        
        public Builder database(String database) {
            config.database = database;
            return this;
        }
        
        public Builder username(String username) {
            config.username = username;
            return this;
        }
        
        public Builder password(String password) {
            config.password = password;
            return this;
        }
        
        public Builder sql(String sql) {
            config.sql = sql;
            return this;
        }
        
        public Builder sqlParams(List<Object> params) {
            config.sqlParams = params;
            return this;
        }
        
        public Builder addParam(Object param) {
            config.sqlParams.add(param);
            return this;
        }
        
        public Builder pageSize(int size) {
            config.pageSize = size;
            return this;
        }
        
        public Builder connectionPoolSize(int size) {
            config.connectionPoolSize = size;
            return this;
        }
        
        public Builder connectionTimeout(long timeout) {
            config.connectionTimeout = timeout;
            return this;
        }
        
        public Builder resultType(Class<?> type) {
            config.resultType = type;
            return this;
        }
        
        public MysqlSourceConfig build() {
            if (config.host == null || config.host.isEmpty()) {
                throw new IllegalArgumentException("Host is required");
            }
            if (config.database == null || config.database.isEmpty()) {
                throw new IllegalArgumentException("Database is required");
            }
            if (config.sql == null || config.sql.isEmpty()) {
                throw new IllegalArgumentException("SQL is required");
            }
            return config;
        }
    }
}
