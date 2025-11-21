package com.dus.pipeline.connectors.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * CSV 解析工具类
 * 支持自定义分隔符和标题行
 */
public class CsvParser {
    
    /**
     * 解析 CSV 行为 Map
     */
    public static Map<String, Object> parseLine(String line, String delimiter, String header) throws IOException {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        
        Map<String, Object> row = new HashMap<>();
        
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0));
        
        String[] headerArray = null;
        if (header != null && !header.isEmpty()) {
            headerArray = header.split(java.util.regex.Pattern.quote(delimiter), -1);
        }
        
        StringReader reader = new StringReader(line);
        Iterable<CSVRecord> records = format.parse(reader);
        Iterator<CSVRecord> iterator = records.iterator();
        
        if (iterator.hasNext()) {
            CSVRecord record = iterator.next();
            
            if (headerArray != null) {
                for (int i = 0; i < headerArray.length && i < record.size(); i++) {
                    row.put(headerArray[i], record.get(i));
                }
            } else {
                for (int i = 0; i < record.size(); i++) {
                    row.put("col_" + i, record.get(i));
                }
            }
        }
        
        return row;
    }
    
    /**
     * 序列化对象为 CSV 行
     */
    public static String serialize(Map<String, Object> obj, String delimiter) throws IOException {
        StringWriter sw = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0));
        
        try (CSVPrinter printer = new CSVPrinter(sw, format)) {
            for (Object value : obj.values()) {
                printer.print(value);
            }
            printer.println();
        }
        
        return sw.toString();
    }
}
