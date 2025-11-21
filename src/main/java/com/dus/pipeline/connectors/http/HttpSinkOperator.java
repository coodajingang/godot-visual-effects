package com.dus.pipeline.connectors.http;

import com.dus.pipeline.core.SinkOperator;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * HTTP 数据写入算子
 * 将数据批量写入 HTTP 接口
 * 
 * @author Dus
 * @version 1.0
 */
public class HttpSinkOperator extends SinkOperator<List<Map<String, Object>>> {
    
    private final String targetUrl;
    private final boolean ignoreError;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int successCount = 0;
    private int failureCount = 0;
    
    public HttpSinkOperator(String targetUrl) {
        this(targetUrl, false);
    }
    
    public HttpSinkOperator(String targetUrl, boolean ignoreError) {
        this.targetUrl = targetUrl;
        this.ignoreError = ignoreError;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void write(List<Map<String, Object>> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        String jsonPayload = objectMapper.writeValueAsString(input);
        
        HttpPost httpPost = new HttpPost(targetUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("User-Agent", "Pipeline-Framework/1.0");
        httpPost.setEntity(new StringEntity(jsonPayload));
        
        try (var response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            
            if (statusCode >= 200 && statusCode < 300) {
                successCount++;
                System.out.println("Successfully wrote " + input.size() + " records to " + targetUrl);
            } else {
                failureCount++;
                String errorMsg = "HTTP error " + statusCode + " when writing to " + targetUrl;
                System.err.println(errorMsg);
                
                if (!ignoreError) {
                    throw new RuntimeException(errorMsg);
                }
            }
        }
    }
    
    @Override
    protected void after(List<Map<String, Object>> input, Void output) throws Exception {
        System.out.println("HTTP Sink Statistics - Success: " + successCount + ", Failure: " + failureCount);
    }
    
    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }
    
    /**
     * 获取成功写入次数
     */
    public int getSuccessCount() {
        return successCount;
    }
    
    /**
     * 获取失败写入次数
     */
    public int getFailureCount() {
        return failureCount;
    }
}