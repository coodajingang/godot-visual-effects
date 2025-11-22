package com.dus.pipeline.connectors.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 游标分页策略
 * 支持 ?cursor=X 的分页方式
 */
public class CursorPaginationStrategy implements PaginationStrategy {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String cursorParamName;
    private final String nextCursorPath;
    private String nextCursor;
    
    public CursorPaginationStrategy(String cursorParamName, String nextCursorPath) {
        this.cursorParamName = cursorParamName;
        this.nextCursorPath = nextCursorPath;
        this.nextCursor = null;
    }
    
    @Override
    public String buildUrl(String baseUrl, int pageNum) {
        if (pageNum == 0) {
            // 第一页不需要游标
            return baseUrl;
        }
        if (nextCursor == null) {
            return null;
        }
        char separator = baseUrl.contains("?") ? '&' : '?';
        return baseUrl + separator + cursorParamName + "=" + nextCursor;
    }
    
    @Override
    public boolean hasNext(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode cursorNode = root.at("/" + nextCursorPath.replace(".", "/"));
            if (!cursorNode.isMissingNode() && !cursorNode.isNull()) {
                nextCursor = cursorNode.asText();
                return true;
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
