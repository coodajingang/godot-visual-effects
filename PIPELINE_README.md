# Pipeline Framework with Lifecycle Hooks

A comprehensive Java Pipeline framework that supports flexible lifecycle hooks for before/after pipeline execution.

## Features

- ✅ **Flexible Hook Mechanism**: Support for any number of before and after hooks
- ✅ **Complete Lifecycle Control**: beforePipeline / afterPipeline / onFailure / onInterrupted
- ✅ **Context Passing**: Shared state and configuration between hooks
- ✅ **Exception Isolation**: After hook exceptions don't affect completed pipeline
- ✅ **Built-in Common Implementations**: Database, cache, notification hooks
- ✅ **Easy to Extend**: Implement interfaces to create custom hooks
- ✅ **Async Support**: AsyncPipeline with hook integration

## Core Components

### Hook Interfaces

- `PipelineHook`: Base interface for all hooks
- `BeforePipelineHook`: Hooks executed before pipeline starts
- `AfterPipelineHook`: Hooks executed after pipeline completion/failure/interruption

### Core Classes

- `Pipeline<I, O>`: Main synchronous pipeline implementation
- `AsyncPipeline<I, O>`: Asynchronous pipeline implementation
- `PipelineContext`: Runtime context for sharing state and statistics
- `PipelineStatus`: Pipeline status enumeration

### Built-in Hooks

#### Database Hooks
- `DatabaseCleanupHook`: Clean database tables before pipeline
- `TemporaryTableCreationHook`: Create temporary tables
- `TemporaryTableCleanupHook`: Clean up temporary tables after pipeline

#### Cache Hooks
- `CacheInitializationHook`: Initialize/warm up cache
- `CacheCleanupHook`: Clean cache after pipeline

#### Notification Hooks
- `NotificationHook`: Send email notifications on pipeline events
- `MetricsReportingHook`: Report pipeline metrics to monitoring systems

## Quick Start

### Basic Example

```java
// Create a pipeline with temporary table management
Pipeline<List<Data>, Void> pipeline = new Pipeline<>(new MySourceOperator())
    .addBeforeHook(new TemporaryTableCreationHook(
        dataSource,
        "CREATE TEMPORARY TABLE temp_data AS SELECT * FROM source_table LIMIT 0"
    ))
    .addOperator(new TransformOperator())
    .addOperator(new WriteToDbOperator(dataSource, "temp_data"))
    .addAfterHook(new TemporaryTableCleanupHook(
        dataSource,
        "DROP TABLE IF EXISTS temp_data"
    ));

pipeline.run();
```

### Complex Example with Multiple Hooks

```java
PipelineContext context = new PipelineContext();

Pipeline<List<Data>, Void> pipeline = new Pipeline<>(new HttpSourceOperator(httpConfig))
    .withContext(context)
    .addBeforeHook(new CacheInitializationHook(jedisPool, "pipeline:cache:"))
    .addBeforeHook(new DatabaseCleanupHook(dataSource, "DELETE FROM staging_table"))
    .addOperator(new EnrichOperator())
    .addOperator(new MysqlSinkOperator(dataSource, "final_table"))
    .addAfterHook(new CacheCleanupHook(jedisPool, "pipeline:cache:*"))
    .addAfterHook(new MetricsReportingHook(metricsRegistry, pipeline))
    .addAfterHook(new NotificationHook(emailService, "admin@example.com"));

try {
    pipeline.run();
    logger.info("Pipeline completed. Total records: {}", context.getTotalRecordCount());
} catch (Exception e) {
    logger.error("Pipeline failed", e);
}
```

### Async Pipeline Example

```java
AsyncPipeline<List<Data>, Void> asyncPipeline = new AsyncPipeline(new HttpSourceOperator())
    .addBeforeHook(new TemporaryTableCreationHook(dataSource, createTableSql))
    .addOperator(new AsyncTransformOperator())
    .addOperator(new AsyncMysqlSinkOperator(dataSource))
    .addAfterHook(new TemporaryTableCleanupHook(dataSource, dropTableSql))
    .addAfterHook(new NotificationHook(emailService, "team@example.com"));

asyncPipeline.runAsync()
    .thenAccept(v -> logger.info("Async pipeline completed"))
    .exceptionally(e -> {
        logger.error("Async pipeline failed", e);
        return null;
    });
```

## Custom Hook Implementation

```java
public class CustomBeforeHook implements BeforePipelineHook {
    
    @Override
    public void initialize() throws Exception {
        // Initialize resources
    }
    
    @Override
    public void beforePipeline(PipelineContext context) throws Exception {
        // Custom logic before pipeline starts
        context.setProperty("custom_data", "value");
    }
    
    @Override
    public String name() {
        return "CustomBeforeHook";
    }
}

public class CustomAfterHook implements AfterPipelineHook {
    
    @Override
    public void initialize() throws Exception {
        // Initialize resources
    }
    
    @Override
    public void afterPipeline(PipelineContext context) throws Exception {
        // Custom logic after successful completion
    }
    
    @Override
    public void onPipelineFailure(PipelineContext context, Exception exception) throws Exception {
        // Custom logic for failure handling
    }
    
    @Override
    public void onPipelineInterrupted(PipelineContext context) throws Exception {
        // Custom logic for interruption handling
    }
    
    @Override
    public String name() {
        return "CustomAfterHook";
    }
}
```

## Exception Handling

- `PipelineException`: Base exception for pipeline-related errors
- `HookExecutionException`: Thrown when hook execution fails
- Before hook exceptions will abort the pipeline
- After hook exceptions are logged but don't affect completed pipeline

## Common Use Cases

| Scenario | Before Hook | After Hook |
|----------|-------------|------------|
| Data Cleanup | Clean garbage data | - |
| Temporary Table | Create temporary table | Drop temporary table |
| Cache Management | Initialize/warm cache | Clean cache |
| Transaction Management | Begin transaction | Commit/rollback transaction |
| Monitoring | Record start state | Send notifications |
| Resource Management | Pre-allocate resources | Release resources |
| State Tracking | Record start state | Record end state |

## Testing

The framework includes comprehensive tests:

```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests HookExecutionTest
./gradlew test --tests PipelineContextTest
```

## Dependencies

- SLF4J for logging
- JUnit 5 for testing
- Optional: Redis/Jedis for cache hooks
- Optional: JDBC for database hooks
- Optional: Email service for notification hooks

## License

This framework is part of the DUS Pipeline framework package.