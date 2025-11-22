package com.dus.pipeline.connectors.http;

import com.dus.pipeline.core.SinkOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 汇算子
 * 将数据写入 HTTP API，支持批量写入
 */
public class HttpSinkOperator extends SinkOperator<List<?>> {
    
    private final HttpSinkConfig config;
    private final ObjectMapper objectMapper;
    private HttpClient httpClient;
    private List<Object> batch;
    
    public HttpSinkOperator(HttpSinkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.objectMapper = new ObjectMapper();
        this.batch = new ArrayList<>();
    }
    
    @Override
    protected void before(List<?> input) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(TimeValue.ofMilliseconds(config.getConnectTimeout()))
            .setResponseTimeout(TimeValue.ofMilliseconds(config.getReadTimeout()))
            .build();
        
        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
        
        this.batch = new ArrayList<>();
    }
    
    @Override
    protected void write(List<?> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        for (Object item : input) {
            batch.add(item);
            if (batch.size() >= config.getBatchSize()) {
                flushBatch();
            }
        }
    }
    
    @Override
    protected void after(List<?> input, Void output) throws Exception {
        if (!batch.isEmpty()) {
            flushBatch();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }
    
    private void flushBatch() throws Exception {
        if (batch.isEmpty()) {
            return;
        }
        
        String payload = objectMapper.writeValueAsString(batch);
        
        int retries = 0;
        Exception lastException = null;
        
        while (retries <= config.getMaxRetries()) {
            try {
                ClassicHttpRequest request = createRequest(config.getUrl(), payload);
                
                int statusCode = httpClient.execute(request, response -> {
                    // Consume response
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        entity.getContent().readAllBytes();
                    }
                    return response.getCode();
                });
                
                if (statusCode >= 200 && statusCode < 300) {
                    batch.clear();
                    return;
                } else {
                    throw new RuntimeException("HTTP " + statusCode + ": " + config.getUrl());
                }
            } catch (Exception e) {
                lastException = e;
                retries++;
                if (retries <= config.getMaxRetries()) {
                    long backoffTime = (long) Math.pow(2, retries - 1) * 1000;
                    Thread.sleep(backoffTime);
                }
            }
        }
        
        if (config.isIgnoreError()) {
            batch.clear();
        } else {
            throw new RuntimeException("Failed to write data after " + config.getMaxRetries() + " retries", lastException);
        }
    }
    
    private ClassicHttpRequest createRequest(String url, String payload) throws Exception {
        ClassicHttpRequest request;
        
        switch (config.getMethod().toUpperCase()) {
            case "PUT":
                request = new HttpPut(url);
                break;
            case "PATCH":
                request = new HttpPatch(url);
                break;
            case "POST":
            default:
                request = new HttpPost(url);
                break;
        }
        
        request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
        addHeaders(request);
        return request;
    }
    
    private void addHeaders(ClassicHttpRequest request) {
        for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        if (!request.containsHeader("Content-Type")) {
            request.setHeader("Content-Type", "application/json");
        }
    }
    
    @Override
    public String name() {
        return "HttpSinkOperator";
    }
}
