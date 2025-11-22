package com.dus.pipeline.connectors.http;

/**
 * HTTP 分页策略接口
 * 定义了不同的分页方式
 */
public interface PaginationStrategy {
    
    /**
     * 根据页码构建完整的 URL
     * 
     * @param baseUrl 基础 URL
     * @param pageNum 页码（从 0 或 1 开始，取决于实现）
     * @return 完整的带分页参数的 URL
     */
    String buildUrl(String baseUrl, int pageNum);
    
    /**
     * 判断是否有下一页
     * 
     * @param response HTTP 响应内容
     * @return 是否有下一页
     */
    boolean hasNext(String response);
    
    /**
     * 计算下一页页码
     * 
     * @param currentPage 当前页码
     * @return 下一页页码
     */
    int nextPageNum(int currentPage);
}
