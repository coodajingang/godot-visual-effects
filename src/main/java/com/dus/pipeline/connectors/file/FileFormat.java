package com.dus.pipeline.connectors.file;

/**
 * 文件格式枚举
 */
public enum FileFormat {
    JSONL,   // JSON Lines - 每行一个 JSON 对象
    CSV,     // 逗号分隔值
    TEXT     // 纯文本 - 每行一条数据
}
