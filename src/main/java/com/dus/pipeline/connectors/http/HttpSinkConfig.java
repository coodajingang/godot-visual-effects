package com.dus.pipeline.connectors.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 汇配置类
 */
public class HttpSinkConfig {
    private String url;
    private String method;
    private Map<String, String> headers;
    private int batchSize;
    private int connectTimeout;
    private int readTimeout;
    private int maxRetries;
    private boolean ignoreError;
    
    public HttpSinkConfig() {
        this.method = "POST";
        this.headers = new HashMap<>();
        this.batchSize = 100;
        this.connectTimeout = 30000;
        this.readTimeout = 30000;
        this.maxRetries = 3;
        this.ignoreError = false;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public boolean isIgnoreError() {
        return ignoreError;
    }
    
    public void setIgnoreError(boolean ignoreError) {
        this.ignoreError = ignoreError;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final HttpSinkConfig config = new HttpSinkConfig();
        
        public Builder url(String url) {
            config.url = url;
            return this;
        }
        
        public Builder method(String method) {
            config.method = method;
            return this;
        }
        
        public Builder headers(Map<String, String> headers) {
            config.headers = headers;
            return this;
        }
        
        public Builder addHeader(String key, String value) {
            config.headers.put(key, value);
            return this;
        }
        
        public Builder batchSize(int size) {
            config.batchSize = size;
            return this;
        }
        
        public Builder connectTimeout(int timeout) {
            config.connectTimeout = timeout;
            return this;
        }
        
        public Builder readTimeout(int timeout) {
            config.readTimeout = timeout;
            return this;
        }
        
        public Builder maxRetries(int retries) {
            config.maxRetries = retries;
            return this;
        }
        
        public Builder ignoreError(boolean ignore) {
            config.ignoreError = ignore;
            return this;
        }
        
        public HttpSinkConfig build() {
            if (config.url == null || config.url.isEmpty()) {
                throw new IllegalArgumentException("URL is required");
            }
            return config;
        }
    }
}
