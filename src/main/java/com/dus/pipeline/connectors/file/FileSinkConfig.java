package com.dus.pipeline.connectors.file;

/**
 * 文件汇配置类
 */
public class FileSinkConfig {
    private String filePath;
    private FileFormat format;
    private String encoding;
    private String csvDelimiter;
    private String csvHeader;
    private int bufferSize;
    private boolean gzipCompressed;
    private FileWriteMode writeMode;
    private boolean createParentDir;
    
    public FileSinkConfig() {
        this.format = FileFormat.JSONL;
        this.encoding = "UTF-8";
        this.csvDelimiter = ",";
        this.bufferSize = 8192;
        this.gzipCompressed = false;
        this.writeMode = FileWriteMode.OVERWRITE;
        this.createParentDir = true;
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
    
    public FileWriteMode getWriteMode() {
        return writeMode;
    }
    
    public void setWriteMode(FileWriteMode writeMode) {
        this.writeMode = writeMode;
    }
    
    public boolean isCreateParentDir() {
        return createParentDir;
    }
    
    public void setCreateParentDir(boolean createParentDir) {
        this.createParentDir = createParentDir;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final FileSinkConfig config = new FileSinkConfig();
        
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
        
        public Builder writeMode(FileWriteMode mode) {
            config.writeMode = mode;
            return this;
        }
        
        public Builder createParentDir(boolean create) {
            config.createParentDir = create;
            return this;
        }
        
        public FileSinkConfig build() {
            if (config.filePath == null || config.filePath.isEmpty()) {
                throw new IllegalArgumentException("FilePath is required");
            }
            return config;
        }
    }
}
