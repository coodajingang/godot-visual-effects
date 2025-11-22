package com.dus.pipeline.connectors.elasticsearch;

/**
 * Elasticsearch 源配置类
 */
public class ElasticsearchSourceConfig {
    private String host;
    private int port;
    private String index;
    private String type;
    private String query;
    private int pageSize;
    private long scrollTimeoutMs;
    private String username;
    private String password;
    private Class<?> resultType;
    private boolean ssl;
    
    public ElasticsearchSourceConfig() {
        this.port = 9200;
        this.pageSize = 1000;
        this.scrollTimeoutMs = 60000;
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
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public long getScrollTimeoutMs() {
        return scrollTimeoutMs;
    }
    
    public void setScrollTimeoutMs(long scrollTimeoutMs) {
        this.scrollTimeoutMs = scrollTimeoutMs;
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
    
    public Class<?> getResultType() {
        return resultType;
    }
    
    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
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
        private final ElasticsearchSourceConfig config = new ElasticsearchSourceConfig();
        
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
        
        public Builder query(String query) {
            config.query = query;
            return this;
        }
        
        public Builder pageSize(int size) {
            config.pageSize = size;
            return this;
        }
        
        public Builder scrollTimeoutMs(long timeout) {
            config.scrollTimeoutMs = timeout;
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
        
        public Builder resultType(Class<?> type) {
            config.resultType = type;
            return this;
        }
        
        public Builder ssl(boolean ssl) {
            config.ssl = ssl;
            return this;
        }
        
        public ElasticsearchSourceConfig build() {
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
