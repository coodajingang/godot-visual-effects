package com.dus.pipeline.connectors.http;

/**
 * Offset-Limit 分页策略
 * 
 * @author Dus
 * @version 1.0
 */
public class OffsetLimitPaginationStrategy implements HttpPaginationStrategy {
    
    private final int pageSize;
    private final String offsetParam;
    private final String limitParam;
    
    public OffsetLimitPaginationStrategy(int pageSize) {
        this(pageSize, "offset", "limit");
    }
    
    public OffsetLimitPaginationStrategy(int pageSize, String offsetParam, String limitParam) {
        this.pageSize = pageSize;
        this.offsetParam = offsetParam;
        this.limitParam = limitParam;
    }
    
    @Override
    public String buildUrl(String baseUrl, int page) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        int offset = page * pageSize;
        return baseUrl + separator + offsetParam + "=" + offset + "&" + limitParam + "=" + pageSize;
    }
}