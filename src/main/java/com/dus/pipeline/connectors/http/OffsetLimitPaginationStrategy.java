package com.dus.pipeline.connectors.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Offset/Limit 分页策略
 * 支持 ?offset=X&limit=Y 的分页方式
 */
public class OffsetLimitPaginationStrategy implements PaginationStrategy {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final int pageSize;
    private final String offsetParamName;
    private final String limitParamName;
    private final String dataPath;
    
    public OffsetLimitPaginationStrategy(int pageSize, String offsetParamName, 
                                        String limitParamName, String dataPath) {
        this.pageSize = pageSize;
        this.offsetParamName = offsetParamName;
        this.limitParamName = limitParamName;
        this.dataPath = dataPath;
    }
    
    @Override
    public String buildUrl(String baseUrl, int pageNum) {
        int offset = pageNum * pageSize;
        char separator = baseUrl.contains("?") ? '&' : '?';
        return baseUrl + separator + offsetParamName + "=" + offset + "&" + limitParamName + "=" + pageSize;
    }
    
    @Override
    public boolean hasNext(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataNode = root.at("/" + dataPath.replace(".", "/"));
            if (dataNode.isArray()) {
                return dataNode.size() >= pageSize;
            }
        } catch (Exception e) {
            // JSON 解析失败，返回 false
        }
        return false;
    }
    
    @Override
    public int nextPageNum(int currentPage) {
        return currentPage + 1;
    }
}
