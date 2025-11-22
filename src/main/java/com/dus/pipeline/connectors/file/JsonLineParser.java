package com.dus.pipeline.connectors.file;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * JSON Lines 解析工具类
 * 每一行都是一个有效的 JSON 对象
 */
public class JsonLineParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 解析单行 JSON
     */
    public static Map<String, Object> parseLine(String line) throws Exception {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> obj = objectMapper.readValue(line, Map.class);
        return obj;
    }
    
    /**
     * 序列化对象为 JSON 字符串
     */
    public static String serialize(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
