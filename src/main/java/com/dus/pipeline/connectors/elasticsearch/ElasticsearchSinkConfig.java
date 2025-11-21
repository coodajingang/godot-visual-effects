package com.dus.pipeline.connectors.elasticsearch;

/**
 * Elasticsearch 汇配置类
 */
public class ElasticsearchSinkConfig {
    private String host;
    private int port;
    private String index;
    private String type;
    private int batchSize;
    private long flushIntervalMs;
    private int maxRetries;
    private String username;
    private String password;
    private WriteMode writeMode;
    private boolean ssl;
    
    public ElasticsearchSinkConfig() {
        this.port = 9200;
        this.batchSize = 100;
        this.flushIntervalMs = 5000;
        this.maxRetries = 3;
        this.writeMode = WriteMode.INDEX;
        this.ssl = false;
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
    
    public String getIndex() {
        return index;
    }
    
    public void setIndex(String index) {
        this.index = index;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public long getFlushIntervalMs() {
        return flushIntervalMs;
    }
    
    public void setFlushIntervalMs(long flushIntervalMs) {
        this.flushIntervalMs = flushIntervalMs;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
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
    
    public WriteMode getWriteMode() {
        return writeMode;
    }
    
    public void setWriteMode(WriteMode writeMode) {
        this.writeMode = writeMode;
    }
    
    public boolean isSsl() {
        return ssl;
    }
    
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ElasticsearchSinkConfig config = new ElasticsearchSinkConfig();
        
        public Builder host(String host) {
            config.host = host;
            return this;
        }
        
        public Builder port(int port) {
            config.port = port;
            return this;
        }
        
        public Builder index(String index) {
            config.index = index;
            return this;
        }
        
        public Builder type(String type) {
            config.type = type;
            return this;
        }
        
        public Builder batchSize(int size) {
            config.batchSize = size;
            return this;
        }
        
        public Builder flushIntervalMs(long interval) {
            config.flushIntervalMs = interval;
            return this;
        }
        
        public Builder maxRetries(int retries) {
            config.maxRetries = retries;
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
        
        public Builder writeMode(WriteMode mode) {
            config.writeMode = mode;
            return this;
        }
        
        public Builder ssl(boolean ssl) {
            config.ssl = ssl;
            return this;
        }
        
        public ElasticsearchSinkConfig build() {
            if (config.host == null || config.host.isEmpty()) {
                throw new IllegalArgumentException("Host is required");
            }
            if (config.index == null || config.index.isEmpty()) {
                throw new IllegalArgumentException("Index is required");
            }
            return config;
        }
    }
}
