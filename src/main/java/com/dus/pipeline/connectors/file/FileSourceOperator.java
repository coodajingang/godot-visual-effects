package com.dus.pipeline.connectors.file;

import com.dus.pipeline.core.SourceOperator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 文件源算子
 * 从文件读取数据，支持多种格式
 */
public class FileSourceOperator extends SourceOperator<List<Map<String, Object>>> {
    
    private final FileSourceConfig config;
    private BufferedReader reader;
    private boolean finished;
    
    public FileSourceOperator(FileSourceConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        this.config = config;
        this.finished = false;
    }
    
    @Override
    protected void before() throws Exception {
        File file = new File(config.getFilePath());
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + config.getFilePath());
        }
        
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr;
        
        if (config.isGzipCompressed()) {
            GZIPInputStream gis = new GZIPInputStream(fis);
            isr = new InputStreamReader(gis, Charset.forName(config.getEncoding()));
        } else {
            isr = new InputStreamReader(fis, Charset.forName(config.getEncoding()));
        }
        
        this.reader = new BufferedReader(isr, config.getBufferSize());
    }
    
    @Override
    protected List<Map<String, Object>> doNextBatch() throws Exception {
        if (finished || reader == null) {
            return null;
        }
        
        List<Map<String, Object>> batch = new ArrayList<>();
        String line;
        int count = 0;
        
        while ((line = reader.readLine()) != null && count < 1000) {
            Map<String, Object> item = parseLine(line);
            if (item != null) {
                batch.add(item);
                count++;
            }
        }
        
        if (line == null) {
            finished = true;
        }
        
        return batch.isEmpty() ? null : batch;
    }
    
    @Override
    protected void after(List<Map<String, Object>> batch) throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
    
    private Map<String, Object> parseLine(String line) throws Exception {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        switch (config.getFormat()) {
            case JSONL:
                return JsonLineParser.parseLine(line);
            case CSV:
                return CsvParser.parseLine(line, config.getCsvDelimiter(), config.getCsvHeader());
            case TEXT:
                Map<String, Object> map = new HashMap<>();
                map.put("content", line);
                return map;
            default:
                return null;
        }
    }
    
    @Override
    public String name() {
        return "FileSourceOperator";
    }
}
