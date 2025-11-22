package com.dus.pipeline.connectors.file;

/**
 * 文件源配置类
 */
public class FileSourceConfig {
    private String filePath;
    private FileFormat format;
    private String encoding;
    private String csvDelimiter;
    private String csvHeader;
    private int bufferSize;
    private boolean gzipCompressed;
    private Class<?> resultType;
    
    public FileSourceConfig() {
        this.format = FileFormat.JSONL;
        this.encoding = "UTF-8";
        this.csvDelimiter = ",";
        this.bufferSize = 8192;
        this.gzipCompressed = false;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public FileFormat getFormat() {
        return format;
    }
    
    public void setFormat(FileFormat format) {
        this.format = format;
    }
    
    public String getEncoding() {
        return encoding;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public String getCsvDelimiter() {
        return csvDelimiter;
    }
    
    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }
    
    public String getCsvHeader() {
        return csvHeader;
    }
    
    public void setCsvHeader(String csvHeader) {
        this.csvHeader = csvHeader;
    }
    
    public int getBufferSize() {
        return bufferSize;
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public boolean isGzipCompressed() {
        return gzipCompressed;
    }
    
    public void setGzipCompressed(boolean gzipCompressed) {
        this.gzipCompressed = gzipCompressed;
    }
    
    public Class<?> getResultType() {
        return resultType;
    }
    
    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final FileSourceConfig config = new FileSourceConfig();
        
        public Builder filePath(String filePath) {
            config.filePath = filePath;
            return this;
        }
        
        public Builder format(FileFormat format) {
            config.format = format;
            return this;
        }
        
        public Builder encoding(String encoding) {
            config.encoding = encoding;
            return this;
        }
        
        public Builder csvDelimiter(String delimiter) {
            config.csvDelimiter = delimiter;
            return this;
        }
        
        public Builder csvHeader(String header) {
            config.csvHeader = header;
            return this;
        }
        
        public Builder bufferSize(int size) {
            config.bufferSize = size;
            return this;
        }
        
        public Builder gzipCompressed(boolean compressed) {
            config.gzipCompressed = compressed;
            return this;
        }
        
        public Builder resultType(Class<?> type) {
            config.resultType = type;
            return this;
        }
        
        public FileSourceConfig build() {
            if (config.filePath == null || config.filePath.isEmpty()) {
                throw new IllegalArgumentException("FilePath is required");
            }
            return config;
        }
    }
}
