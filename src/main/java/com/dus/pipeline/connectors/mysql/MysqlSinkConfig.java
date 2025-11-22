package com.dus.pipeline.connectors.mysql;

/**
 * MySQL 汇配置类
 */
public class MysqlSinkConfig {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String sql;
    private int batchSize;
    private boolean autocommit;
    private ConflictStrategy conflictStrategy;
    private int connectionPoolSize;
    private long connectionTimeout;
    
    public MysqlSinkConfig() {
        this.port = 3306;
        this.batchSize = 100;
        this.autocommit = true;
        this.conflictStrategy = ConflictStrategy.FAIL_ON_CONFLICT;
        this.connectionPoolSize = 10;
        this.connectionTimeout = 30000;
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
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public boolean isAutocommit() {
        return autocommit;
    }
    
    public void setAutocommit(boolean autocommit) {
        this.autocommit = autocommit;
    }
    
    public ConflictStrategy getConflictStrategy() {
        return conflictStrategy;
    }
    
    public void setConflictStrategy(ConflictStrategy conflictStrategy) {
        this.conflictStrategy = conflictStrategy;
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
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MysqlSinkConfig config = new MysqlSinkConfig();
        
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
        
        public Builder batchSize(int size) {
            config.batchSize = size;
            return this;
        }
        
        public Builder autocommit(boolean autocommit) {
            config.autocommit = autocommit;
            return this;
        }
        
        public Builder conflictStrategy(ConflictStrategy strategy) {
            config.conflictStrategy = strategy;
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
        
        public MysqlSinkConfig build() {
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
