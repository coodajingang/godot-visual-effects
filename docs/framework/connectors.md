# Pipeline Connectors

本文档介绍 Pipeline 框架中的连接器（Connectors）模块，用于实现常见的数据源和数据汇。

## 概述

Connectors 模块提供了一套开箱即用的数据连接器，覆盖多种常见的数据源和目标：

- **HTTP 连接器**：与 HTTP API 交互
- **MySQL 连接器**：与 MySQL 数据库交互
- **Elasticsearch 连接器**：与 Elasticsearch 集群交互
- **文件连接器**：与本地文件系统交互

## 包结构

```
com.dus.pipeline.connectors
├── http/
│   ├── HttpSourceOperator          # HTTP 源算子
│   ├── HttpSinkOperator            # HTTP 汇算子
│   ├── HttpSourceConfig            # HTTP 源配置
│   ├── HttpSinkConfig              # HTTP 汇配置
│   ├── PaginationStrategy          # 分页策略接口
│   ├── OffsetLimitPaginationStrategy    # Offset/Limit 分页
│   └── CursorPaginationStrategy    # 游标分页
├── mysql/
│   ├── MysqlSourceOperator         # MySQL 源算子
│   ├── MysqlSinkOperator           # MySQL 汇算子
│   ├── MysqlSourceConfig           # MySQL 源配置
│   ├── MysqlSinkConfig             # MySQL 汇配置
│   ├── MysqlConnectionPool         # 连接池管理
│   └── ConflictStrategy            # 冲突处理策略
├── elasticsearch/
│   ├── ElasticsearchSourceOperator # Elasticsearch 源算子
│   ├── ElasticsearchSinkOperator   # Elasticsearch 汇算子
│   ├── ElasticsearchSourceConfig   # Elasticsearch 源配置
│   ├── ElasticsearchSinkConfig     # Elasticsearch 汇配置
│   └── WriteMode                   # 写入模式枚举
└── file/
    ├── FileSourceOperator          # 文件源算子
    ├── FileSinkOperator            # 文件汇算子
    ├── FileSourceConfig            # 文件源配置
    ├── FileSinkConfig              # 文件汇配置
    ├── FileFormat                  # 文件格式枚举
    ├── FileWriteMode               # 文件写入模式枚举
    ├── JsonLineParser              # JSON Lines 解析工具
    └── CsvParser                   # CSV 解析工具
```

## HTTP 连接器

### 使用 HTTP 源读取数据

```java
// 创建配置
HttpSourceConfig config = HttpSourceConfig.builder()
    .url("http://api.example.com/users")
    .method("GET")
    .addHeader("Authorization", "Bearer your-token")
    .connectTimeout(30000)
    .readTimeout(30000)
    .maxRetries(3)
    .pagination(new OffsetLimitPaginationStrategy(100, "offset", "limit", "data"))
    .build();

// 创建源算子
HttpSourceOperator source = new HttpSourceOperator(config);

// 在管道中使用
Pipeline<List<?>, ?> pipeline = new Pipeline<>(source)
    .addOperator(new FileSinkOperator(fileSinkConfig));
pipeline.run();
```

### 使用 HTTP 汇写入数据

```java
// 创建配置
HttpSinkConfig config = HttpSinkConfig.builder()
    .url("http://api.example.com/data")
    .method("POST")
    .batchSize(100)
    .maxRetries(3)
    .ignoreError(false)
    .build();

// 创建汇算子
HttpSinkOperator sink = new HttpSinkOperator(config);
```

### 分页策略

支持两种分页策略：

**Offset/Limit 分页**
```java
PaginationStrategy pagination = new OffsetLimitPaginationStrategy(
    100,              // 每页记录数
    "offset",         // offset 参数名
    "limit",          // limit 参数名
    "data"            // 数据在响应中的路径
);
```

**游标分页**
```java
PaginationStrategy pagination = new CursorPaginationStrategy(
    "cursor",         // cursor 参数名
    "next_cursor"     // 下一个游标在响应中的路径
);
```

## MySQL 连接器

### 使用 MySQL 源读取数据

```java
// 创建配置
MysqlSourceConfig config = MysqlSourceConfig.builder()
    .host("localhost")
    .port(3306)
    .database("mydb")
    .username("root")
    .password("password")
    .sql("SELECT * FROM users WHERE status = ?")
    .addParam("active")
    .pageSize(1000)
    .connectionPoolSize(10)
    .connectionTimeout(30000)
    .build();

// 创建源算子
MysqlSourceOperator source = new MysqlSourceOperator(config);
```

### 使用 MySQL 汇写入数据

```java
// 创建配置
MysqlSinkConfig config = MysqlSinkConfig.builder()
    .host("localhost")
    .port(3306)
    .database("mydb")
    .username("root")
    .password("password")
    .sql("INSERT INTO users (id, name, email) VALUES (?, ?, ?)")
    .batchSize(100)
    .autocommit(true)
    .conflictStrategy(ConflictStrategy.ON_DUPLICATE_KEY_UPDATE)
    .build();

// 创建汇算子
MysqlSinkOperator sink = new MysqlSinkOperator(config);
```

### 冲突处理策略

支持三种冲突处理策略：

- `INSERT_IGNORE` - 忽略重复键，不更新
- `ON_DUPLICATE_KEY_UPDATE` - 重复键时更新
- `FAIL_ON_CONFLICT` - 冲突时失败（默认）

## Elasticsearch 连接器

### 使用 Elasticsearch 源读取数据

```java
// 创建配置
ElasticsearchSourceConfig config = ElasticsearchSourceConfig.builder()
    .host("localhost")
    .port(9200)
    .index("logs")
    .query("{\"match_all\": {}}")
    .pageSize(1000)
    .scrollTimeoutMs(60000)
    .username("elastic")
    .password("password")
    .build();

// 创建源算子
ElasticsearchSourceOperator source = new ElasticsearchSourceOperator(config);
```

### 使用 Elasticsearch 汇写入数据

```java
// 创建配置
ElasticsearchSinkConfig config = ElasticsearchSinkConfig.builder()
    .host("localhost")
    .port(9200)
    .index("logs-{yyyy-MM-dd}")  // 支持日期格式化索引名
    .batchSize(100)
    .flushIntervalMs(5000)
    .maxRetries(3)
    .writeMode(WriteMode.INDEX)
    .build();

// 创建汇算子
ElasticsearchSinkOperator sink = new ElasticsearchSinkOperator(config);
```

### 写入模式

支持三种写入模式：

- `INDEX` - 完全覆盖文档
- `UPDATE` - 部分更新文档
- `DELETE` - 删除文档

## 文件连接器

### 使用文件源读取数据

```java
// 创建配置 - JSONL 格式
FileSourceConfig config = FileSourceConfig.builder()
    .filePath("/path/to/data.jsonl")
    .format(FileFormat.JSONL)
    .encoding("UTF-8")
    .gzipCompressed(false)
    .build();

// 创建配置 - CSV 格式
FileSourceConfig csvConfig = FileSourceConfig.builder()
    .filePath("/path/to/data.csv")
    .format(FileFormat.CSV)
    .csvDelimiter(",")
    .csvHeader("id,name,email")
    .build();

// 创建源算子
FileSourceOperator source = new FileSourceOperator(config);
```

### 使用文件汇写入数据

```java
// 创建配置 - JSONL 格式
FileSinkConfig config = FileSinkConfig.builder()
    .filePath("/path/to/output.jsonl")
    .format(FileFormat.JSONL)
    .encoding("UTF-8")
    .writeMode(FileWriteMode.OVERWRITE)
    .createParentDir(true)
    .build();

// 创建汇算子
FileSinkOperator sink = new FileSinkOperator(config);
```

### 支持的文件格式

- `JSONL` - JSON Lines（每行一个 JSON 对象）
- `CSV` - 逗号分隔值
- `TEXT` - 纯文本（每行一条记录）

## 完整示例

### HTTP 源 → 文件汇

```java
HttpSourceConfig httpConfig = HttpSourceConfig.builder()
    .url("http://api.example.com/data")
    .method("GET")
    .addHeader("Authorization", "Bearer token")
    .pagination(new OffsetLimitPaginationStrategy(100, "offset", "limit", "data"))
    .build();

FileSinkConfig fileConfig = FileSinkConfig.builder()
    .filePath("/tmp/output.jsonl")
    .format(FileFormat.JSONL)
    .build();

Pipeline pipeline = new Pipeline(new HttpSourceOperator(httpConfig))
    .addOperator(new FileSinkOperator(fileConfig));

pipeline.run();
```

### MySQL 源 → Elasticsearch 汇

```java
MysqlSourceConfig mysqlConfig = MysqlSourceConfig.builder()
    .host("localhost")
    .port(3306)
    .database("mydb")
    .username("root")
    .password("password")
    .sql("SELECT * FROM products")
    .pageSize(1000)
    .build();

ElasticsearchSinkConfig esConfig = ElasticsearchSinkConfig.builder()
    .host("localhost")
    .port(9200)
    .index("products")
    .batchSize(100)
    .build();

Pipeline pipeline = new Pipeline(new MysqlSourceOperator(mysqlConfig))
    .addOperator(new ElasticsearchSinkOperator(esConfig));

pipeline.run();
```

## 共同特性

所有连接器都支持以下特性：

### 连接管理
- 连接池管理（HTTP Client、Database、Elasticsearch）
- 自动连接生命周期管理
- 可配置的超时时间

### 重试机制
- 指数退避重试策略
- 可配置最大重试次数
- 自动失败恢复

### 批处理
- 支持批量读写
- 可配置批大小
- 内存缓冲管理

### 配置灵活性
- Builder 模式配置
- 支持默认值
- 链式调用

## 性能建议

1. **连接池大小**
   - HTTP：根据并发数选择，通常 10-50
   - MySQL：根据 CPU 核数，推荐 2-4 倍
   - Elasticsearch：根据节点数和查询复杂度

2. **批大小**
   - HTTP：100-1000，根据网络延迟调整
   - MySQL：100-500，避免过大导致内存溢出
   - Elasticsearch：100-1000，Bulk API 优化
   - 文件：1000-10000，根据内存可用量

3. **超时配置**
   - 连接超时：30-60 秒
   - 读取超时：30-300 秒，根据数据量调整

4. **重试策略**
   - 最大重试次数：3-5，避免过多重试
   - 等待时间使用指数退避，避免雪崩

## 依赖项

请在 `pom.xml` 中添加以下依赖：

```xml
<!-- HTTP 客户端 -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.2.x</version>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.x</version>
</dependency>
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.x</version>
</dependency>

<!-- Elasticsearch -->
<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>8.x.x</version>
</dependency>

<!-- JSON 处理 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.x</version>
</dependency>

<!-- CSV 处理 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.x</version>
</dependency>
```

## 扩展和自定义

要创建自定义连接器，继承相应的基类：

```java
// 自定义源算子
public class CustomSourceOperator extends SourceOperator<List<String>> {
    @Override
    protected List<String> doNextBatch() throws Exception {
        // 实现数据获取逻辑
        return null;
    }
}

// 自定义汇算子
public class CustomSinkOperator extends SinkOperator<String> {
    @Override
    protected void write(String input) throws Exception {
        // 实现数据写入逻辑
    }
}
```

## 常见问题

**Q: 如何处理认证？**
A: 对于 HTTP、Elasticsearch 和 MySQL，在配置中设置 `username` 和 `password`。对于 HTTP，可以通过 `addHeader` 添加认证头。

**Q: 如何实现流式处理大文件？**
A: 所有源算子都支持批处理，会自动分页加载数据，不会一次加载整个文件到内存。

**Q: 连接失败时会自动重试吗？**
A: 是的，所有网络相关的操作都支持自动重试，可以通过 `maxRetries` 配置。

**Q: 如何集成监控和日志？**
A: 所有算子都使用 SLF4J 日志，可以在 logback 配置中调整日志级别。
