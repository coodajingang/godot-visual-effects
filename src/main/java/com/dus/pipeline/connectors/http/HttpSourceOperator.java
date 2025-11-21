package com.dus.pipeline.connectors.http;

import com.dus.pipeline.core.SourceOperator;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 数据源算子
 * 从 HTTP 接口拉取数据
 * 
 * @author Dus
 * @version 1.0
 */
public class HttpSourceOperator extends SourceOperator<List<Map<String, Object>>> {
    
    private final String baseUrl;
    private final HttpPaginationStrategy paginationStrategy;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private int currentPage = 0;
    
    public HttpSourceOperator(String baseUrl, HttpPaginationStrategy paginationStrategy) {
        this.baseUrl = baseUrl;
        this.paginationStrategy = paginationStrategy;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected List<Map<String, Object>> doNextBatch() throws Exception {
        String url = paginationStrategy.buildUrl(baseUrl, currentPage);
        
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("User-Agent", "Pipeline-Framework/1.0");
        
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            
            if (response.getCode() >= 400) {
                throw new RuntimeException("HTTP error: " + response.getCode());
            }
            
            // 解析JSON响应
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            
            if (data == null || data.isEmpty()) {
                return null;
            }
            
            currentPage++;
            return data;
        }
    }
    
    @Override
    protected void after(List<Map<String, Object>> batch) throws Exception {
        if (batch != null) {
            System.out.println("Retrieved " + batch.size() + " records from HTTP API");
        }
    }
    
    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
    }
}