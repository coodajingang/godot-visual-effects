package com.dus.pipeline.connectors.mysql;

/**
 * MySQL 冲突处理策略枚举
 */
public enum ConflictStrategy {
    INSERT_IGNORE,              // INSERT IGNORE
    ON_DUPLICATE_KEY_UPDATE,    // ON DUPLICATE KEY UPDATE
    FAIL_ON_CONFLICT            // 失败时抛异常
}
