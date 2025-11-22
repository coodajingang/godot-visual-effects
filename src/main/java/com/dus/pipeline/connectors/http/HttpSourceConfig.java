package com.dus.pipeline.connectors.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 源配置类
 */
public class HttpSourceConfig {
    private String url;
    private String method;
    private Map<String, String> headers;
    private String body;
    private int connectTimeout;
    private int readTimeout;
    private int maxRetries;
    private PaginationStrategy pagination;
    private Class<?> resultType;
    
    public HttpSourceConfig() {
        this.method = "GET";
        this.headers = new HashMap<>();
        this.connectTimeout = 30000;
        this.readTimeout = 30000;
        this.maxRetries = 3;
        this.pagination = null;
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
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
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
    
    public PaginationStrategy getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationStrategy pagination) {
        this.pagination = pagination;
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
        private final HttpSourceConfig config = new HttpSourceConfig();
        
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
        
        public Builder body(String body) {
            config.body = body;
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
        
        public Builder pagination(PaginationStrategy pagination) {
            config.pagination = pagination;
            return this;
        }
        
        public Builder resultType(Class<?> type) {
            config.resultType = type;
            return this;
        }
        
        public HttpSourceConfig build() {
            if (config.url == null || config.url.isEmpty()) {
                throw new IllegalArgumentException("URL is required");
            }
            return config;
        }
    }
}
