package com.dus.pipeline.example;

import com.dus.pipeline.connectors.http.HttpSourceConfig;
import com.dus.pipeline.connectors.http.HttpSourceOperator;
import com.dus.pipeline.connectors.http.OffsetLimitPaginationStrategy;
import com.dus.pipeline.connectors.file.FileFormat;
import com.dus.pipeline.connectors.file.FileSinkConfig;
import com.dus.pipeline.connectors.file.FileSinkOperator;

import java.util.List;
import java.util.Map;

/**
 * 连接器使用示例
 * 演示如何使用 HTTP 源和文件汇
 */
public class ConnectorExample {
    
    public static void main(String[] args) {
        System.out.println("=== Pipeline Connectors Example ===");
        System.out.println();
        System.out.println("This example demonstrates the usage of:");
        System.out.println("- HttpSourceOperator: Fetch data from HTTP API");
        System.out.println("- MysqlSourceOperator: Read data from MySQL");
        System.out.println("- ElasticsearchSourceOperator: Query Elasticsearch");
        System.out.println("- FileSourceOperator: Read from files (JSONL, CSV, TEXT)");
        System.out.println("- HttpSinkOperator: Write data to HTTP API");
        System.out.println("- MysqlSinkOperator: Write data to MySQL");
        System.out.println("- ElasticsearchSinkOperator: Index data to Elasticsearch");
        System.out.println("- FileSinkOperator: Write to files");
        System.out.println();
        System.out.println("Configuration examples:");
        System.out.println();
        
        // HTTP Source Config Example
        System.out.println("1. HTTP Source Configuration:");
        System.out.println("   HttpSourceConfig httpConfig = HttpSourceConfig.builder()");
        System.out.println("       .url(\"http://api.example.com/data\")");
        System.out.println("       .method(\"GET\")");
        System.out.println("       .addHeader(\"Authorization\", \"Bearer token\")");
        System.out.println("       .connectTimeout(30000)");
        System.out.println("       .readTimeout(30000)");
        System.out.println("       .maxRetries(3)");
        System.out.println("       .pagination(new OffsetLimitPaginationStrategy(100, \"offset\", \"limit\", \"data\"))");
        System.out.println("       .build();");
        System.out.println();
        
        // File Sink Config Example
        System.out.println("2. File Sink Configuration:");
        System.out.println("   FileSinkConfig fileConfig = FileSinkConfig.builder()");
        System.out.println("       .filePath(\"/tmp/output.jsonl\")");
        System.out.println("       .format(FileFormat.JSONL)");
        System.out.println("       .encoding(\"UTF-8\")");
        System.out.println("       .createParentDir(true)");
        System.out.println("       .build();");
        System.out.println();
        
        // Usage Example
        System.out.println("3. Pipeline Usage Example:");
        System.out.println("   HttpSourceOperator source = new HttpSourceOperator(httpConfig);");
        System.out.println("   FileSinkOperator sink = new FileSinkOperator(fileConfig);");
        System.out.println("   Pipeline pipeline = new Pipeline(source)");
        System.out.println("       .addOperator(sink);");
        System.out.println("   pipeline.run();");
        System.out.println();
        
        // MySQL Source Example
        System.out.println("4. MySQL Source Configuration:");
        System.out.println("   MysqlSourceConfig mysqlConfig = MysqlSourceConfig.builder()");
        System.out.println("       .host(\"localhost\")");
        System.out.println("       .port(3306)");
        System.out.println("       .database(\"mydb\")");
        System.out.println("       .username(\"root\")");
        System.out.println("       .password(\"password\")");
        System.out.println("       .sql(\"SELECT * FROM users WHERE status = ?\")");
        System.out.println("       .addParam(\"active\")");
        System.out.println("       .pageSize(1000)");
        System.out.println("       .connectionPoolSize(10)");
        System.out.println("       .build();");
        System.out.println();
        
        // Elasticsearch Example
        System.out.println("5. Elasticsearch Sink Configuration:");
        System.out.println("   ElasticsearchSinkConfig esConfig = ElasticsearchSinkConfig.builder()");
        System.out.println("       .host(\"localhost\")");
        System.out.println("       .port(9200)");
        System.out.println("       .index(\"logs-{yyyy-MM-dd}\")");
        System.out.println("       .batchSize(100)");
        System.out.println("       .writeMode(WriteMode.INDEX)");
        System.out.println("       .maxRetries(3)");
        System.out.println("       .build();");
        System.out.println();
        
        System.out.println("All connectors support:");
        System.out.println("- Automatic retry with exponential backoff");
        System.out.println("- Configurable timeouts and connection pooling");
        System.out.println("- Batch processing for efficient data transfer");
        System.out.println("- Flexible configuration via Builder pattern");
        System.out.println("- JSON serialization/deserialization");
        System.out.println();
        System.out.println("For more information, see the documentation in docs/framework/");
    }
}
