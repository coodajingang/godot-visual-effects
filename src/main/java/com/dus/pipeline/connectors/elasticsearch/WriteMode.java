package com.dus.pipeline.connectors.elasticsearch;

/**
 * Elasticsearch 写入模式枚举
 */
public enum WriteMode {
    INDEX,      // 覆盖
    UPDATE,     // 部分更新
    DELETE      // 删除
}
