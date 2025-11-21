# Pipeline Framework

A Java-based data processing pipeline framework with context injection and retry strategies.

## Features

- **Context Injection**: Automatic injection of PipelineContext to all operators
- **Retry Strategies**: Multiple built-in retry strategies (No Retry, Fixed Delay, Exponential Backoff, Adaptive)
- **Skip Strategies**: Intelligent failure handling with skip capabilities
- **Flexible Operators**: Support for synchronous, asynchronous, and retryable operators
- **Lifecycle Hooks**: Pre and post-execution hooks for monitoring and logging

## Quick Start

```java
// Create Pipeline Context
PipelineContext context = new PipelineContext();
context.setProperty("api_url", "http://example.com");
context.setProperty("timeout", 5000);

// Create Pipeline
Pipeline<List<Data>, Void> pipeline = new Pipeline<>(new MySourceOperator())
    .withContext(context)
    .addOperator(new ValidateOperator()
        .withRetryStrategy(new FixedDelayRetryStrategy(3, 1000)))
    .addOperator(new EnrichOperator()
        .withRetryStrategy(new ExponentialBackoffRetryStrategy(5, 1000, 30000, 2.0))
        .withSkipStrategy(new SkipFailedRecordsStrategy(2)))
    .addOperator(new MySinkOperator());

// Execute Pipeline
pipeline.run();
```

## Core Components

### Operators

- **AbstractOperator**: Base class with context support and lifecycle hooks
- **SourceOperator**: Data source with context injection
- **SinkOperator**: Data output with context injection
- **AsyncOperator**: Asynchronous processing support
- **RetryableOperator**: Built-in retry and skip capabilities

### Retry Strategies

- **NoRetryStrategy**: No retry on failure
- **FixedDelayRetryStrategy**: Fixed interval retry
- **ExponentialBackoffRetryStrategy**: Exponential backoff retry
- **AdaptiveRetryStrategy**: Configurable retry per exception type

### Skip Strategies

- **NoSkipStrategy**: Never skip on failure
- **SkipFailedRecordsStrategy**: Skip failed records after max attempts

## Context Usage

Operators can access and modify the shared PipelineContext:

```java
public class MyOperator extends AbstractOperator<List<Data>, List<Data>> {
    @Override
    protected List<Data> doProcess(List<Data> input) {
        // Read from context
        String apiUrl = (String) getContextProperty("api_url");
        
        // Write to context
        setContextProperty("processed_count", input.size());
        
        return input;
    }
}
```

## Package Structure

```
com.dus.pipeline/
  core/
    AbstractOperator.java         # Base operator with context support
    SourceOperator.java           # Data source operator
    SinkOperator.java             # Data output operator
    AsyncOperator.java            # Async processing operator
    ContextAware.java             # Context injection interface
    Operator.java                 # Basic operator interface
    Pipeline.java                 # Main pipeline orchestrator
  
  context/
    PipelineContext.java          # Shared context for operators
  
  retry/
    RetryStrategy.java            # Retry strategy interface
    NoRetryStrategy.java          # No retry implementation
    FixedDelayRetryStrategy.java  # Fixed delay retry
    ExponentialBackoffRetryStrategy.java # Exponential backoff retry
    AdaptiveRetryStrategy.java    # Adaptive retry per exception
    SkipStrategy.java             # Skip strategy interface
    NoSkipStrategy.java           # No skip implementation
    SkipFailedRecordsStrategy.java # Skip failed records
    SkipListener.java             # Skip event listener
    RetryableOperator.java        # Operator with retry/skip support
  
  exception/
    PipelineException.java         # Base pipeline exception
    OperatorException.java        # Operator execution exception
  
  example/
    Data.java                     # Example data model
    ExampleSourceOperator.java    # Example source operator
    EnrichOperator.java           # Example enrich operator
    ValidateOperator.java         # Example validation operator
    ExampleSinkOperator.java      # Example sink operator
    PipelineExample.java          # Complete usage examples
  
  test/
    ContextInjectionTest.java     # Context injection tests
    RetryStrategyTest.java        # Retry strategy tests
    SkipStrategyTest.java         # Skip strategy tests
    RetryableOperatorTest.java    # Retryable operator tests
    PipelineRetryIntegrationTest.java # Integration tests
```

## Running Examples

The framework includes comprehensive examples in the `com.dus.pipeline.example` package. Run the `PipelineExample` class to see the framework in action with various retry and skip strategies.

## Testing

The framework includes extensive unit tests and integration tests covering:

- Context injection and sharing
- All retry strategies
- Skip strategies with listeners
- Retryable operator behavior
- End-to-end pipeline integration

Tests are located in the `com.dus.pipeline.test` package.