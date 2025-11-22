package com.dus.pipeline.connectors.file;

import com.dus.pipeline.core.SinkOperator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * 文件汇算子
 * 将数据写入文件，支持多种格式
 */
public class FileSinkOperator extends SinkOperator<List<Map<String, Object>>> {
    
    private final FileSinkConfig config;
    private BufferedWriter writer;
    private boolean headerWritten;
    
    public FileSinkOperator(FileSinkConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.headerWritten = false;
    }
    
    @Override
    protected void before(List<Map<String, Object>> input) throws Exception {
        File file = new File(config.getFilePath());
        
        if (config.isCreateParentDir()) {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }
        
        boolean fileExists = file.exists();
        boolean append = config.getWriteMode() == FileWriteMode.APPEND && fileExists;
        
        FileOutputStream fos = new FileOutputStream(file, append);
        OutputStreamWriter osw;
        
        if (config.isGzipCompressed()) {
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            osw = new OutputStreamWriter(gos, Charset.forName(config.getEncoding()));
        } else {
            osw = new OutputStreamWriter(fos, Charset.forName(config.getEncoding()));
        }
        
        this.writer = new BufferedWriter(osw, config.getBufferSize());
        
        // 如果是 CSV 格式且没有追加，需要写入标题行
        if (config.getFormat() == FileFormat.CSV && !append && config.getCsvHeader() != null) {
            writer.write(config.getCsvHeader());
            writer.newLine();
            headerWritten = true;
        }
    }
    
    @Override
    protected void write(List<Map<String, Object>> input) throws Exception {
        if (input == null || input.isEmpty()) {
            return;
        }
        
        for (Map<String, Object> item : input) {
            writeLine(item);
        }
        
        // 定期刷新
        writer.flush();
    }
    
    @Override
    protected void after(List<Map<String, Object>> input, Void output) throws Exception {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
    
    private void writeLine(Map<String, Object> item) throws Exception {
        if (item == null) {
            return;
        }
        
        String line;
        switch (config.getFormat()) {
            case JSONL:
                line = JsonLineParser.serialize(item);
                writer.write(line);
                break;
            case CSV:
                line = CsvParser.serialize(item, config.getCsvDelimiter());
                writer.write(line);
                break;
            case TEXT:
                Object content = item.get("content");
                line = content != null ? content.toString() : "";
                writer.write(line);
                writer.newLine();
                break;
            default:
                writer.write(item.toString());
                writer.newLine();
        }
        
        if (config.getFormat() != FileFormat.TEXT) {
            writer.newLine();
        }
    }
    
    @Override
    public String name() {
        return "FileSinkOperator";
    }
}
