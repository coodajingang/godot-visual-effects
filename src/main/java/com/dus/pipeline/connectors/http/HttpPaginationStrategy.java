package com.dus.pipeline.connectors.http;

/**
 * HTTP 分页策略接口
 * 
 * @author Dus
 * @version 1.0
 */
public interface HttpPaginationStrategy {
    
    /**
     * 构建请求URL
     * 
     * @param baseUrl 基础URL
     * @param page 当前页码
     * @return 完整的请求URL
     */
    String buildUrl(String baseUrl, int page);
}