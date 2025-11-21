package com.dus.pipeline.example;

import java.util.Objects;

/**
 * 示例数据类
 */
public class Data {
    
    private String id;
    private String content;
    private String type;
    private long timestamp;
    
    public Data() {}
    
    public Data(String id, String content, String type) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(id, data.id) && 
               Objects.equals(content, data.content) && 
               Objects.equals(type, data.type);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, content, type);
    }
    
    @Override
    public String toString() {
        return "Data{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}