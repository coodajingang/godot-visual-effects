package com.dus.pipeline.connectors.http;

import com.dus.pipeline.core.SourceOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * HTTP 源算子
 * 从 HTTP API 拉取数据，支持分页
 */
public class HttpSourceOperator extends SourceOperator<List<?>> {
    
    private final HttpSourceConfig config;
    private final ObjectMapper objectMapper;
    private HttpClient httpClient;
    private int currentPage;
    private boolean hasMore;
    
    public HttpSourceOperator(HttpSourceConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.currentPage = 0;
        this.hasMore = true;
    }
    
    @Override
    protected void before() throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(TimeValue.ofMilliseconds(config.getConnectTimeout()))
            .setResponseTimeout(TimeValue.ofMilliseconds(config.getReadTimeout()))
            .build();
        
        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
    }
    
    @Override
    protected List<?> doNextBatch() throws Exception {
        if (!hasMore) {
            return null;
        }
        
        String url = buildUrl();
        if (url == null) {
            hasMore = false;
            return null;
        }
        
        String response = executeRequest(url);
        List<?> data = parseResponse(response);
        
        if (config.getPagination() != null) {
            if (config.getPagination().hasNext(response)) {
                currentPage = config.getPagination().nextPageNum(currentPage);
            } else {
                hasMore = false;
            }
        } else {
            hasMore = false;
        }
        
        return data;
    }
    
    @Override
    protected void after(List<?> batch) throws Exception {
        // 默认实现，不需要特殊处理
    }
    
    private String buildUrl() {
        if (config.getPagination() != null) {
            return config.getPagination().buildUrl(config.getUrl(), currentPage);
        }
        return config.getUrl();
    }
    
    private String executeRequest(String url) throws Exception {
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= config.getMaxRetries()) {
            try {
                ClassicHttpRequest request = createRequest(url);
                
                return httpClient.execute(request, response -> {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        byte[] bytes = entity.getContent().readAllBytes();
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                    return "";
                });
            } catch (Exception e) {
                lastException = e;
                retries++;
                if (retries <= config.getMaxRetries()) {
                    long backoffTime = (long) Math.pow(2, retries - 1) * 1000;
                    Thread.sleep(backoffTime);
                }
            }
        }
        
        throw new RuntimeException("Failed to fetch data after " + config.getMaxRetries() + " retries", lastException);
    }
    
    private ClassicHttpRequest createRequest(String url) throws Exception {
        if ("POST".equalsIgnoreCase(config.getMethod())) {
            HttpPost request = new HttpPost(url);
            if (config.getBody() != null) {
                request.setEntity(new StringEntity(config.getBody(), StandardCharsets.UTF_8));
            }
            addHeaders(request);
            return request;
        } else {
            HttpGet request = new HttpGet(url);
            addHeaders(request);
            return request;
        }
    }
    
    private void addHeaders(ClassicHttpRequest request) {
        for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        if (!request.containsHeader("Content-Type")) {
            request.setHeader("Content-Type", "application/json");
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<?> parseResponse(String response) throws Exception {
        if (config.getResultType() != null) {
            if (config.getResultType() == String.class) {
                List<String> list = new ArrayList<>();
                list.add(response);
                return list;
            }
        }
        
        try {
            Object result = objectMapper.readValue(response, Object.class);
            if (result instanceof List) {
                return (List<?>) result;
            } else if (result instanceof Map) {
                List<Object> list = new ArrayList<>();
                list.add(result);
                return list;
            }
        } catch (Exception e) {
            // Fallback
            List<String> list = new ArrayList<>();
            list.add(response);
            return list;
        }
        
        return new ArrayList<>();
    }
    
    @Override
    public String name() {
        return "HttpSourceOperator";
    }
}
