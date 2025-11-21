# Java8 Pipeline/Operator Framework

基于模板方法模式的数据处理管道框架，使用Java8特性实现。

## 框架特性

- **模板方法模式**：确保算子逻辑结构一致性
- **链式调用**：支持流式API构建管道
- **类型安全**：泛型支持确保类型安全
- **扩展性强**：before/after钩子支持日志、监控、限流等
- **Java8优化**：使用Stream API和Lambda表达式
- **异常处理**：完整的异常传播机制

## 核心组件

### 1. Operator<I,O> 接口
定义算子的基础契约：
```java
public interface Operator<I, O> {
    O process(I input) throws Exception;
    String name();
}
```

### 2. AbstractOperator<I,O> 抽象类
模板方法模式的核心实现：
```java
public abstract class AbstractOperator<I, O> implements Operator<I, O> {
    public final O process(I input) throws Exception {
        before(input);           // 前置处理
        O output = doProcess(input);  // 核心业务逻辑
        after(input, output);    // 后置处理
        return output;
    }
    
    protected abstract O doProcess(I input) throws Exception;
    protected void before(I input) throws Exception { /* 可覆盖 */ }
    protected void after(I input, O output) throws Exception { /* 可覆盖 */ }
}
```

### 3. SourceOperator<O> 抽象类
数据源算子模板：
```java
public abstract class SourceOperator<O> implements Operator<Void, O> {
    public final O nextBatch() throws Exception {
        before();
        O batch = doNextBatch();
        after(batch);
        return batch;
    }
    
    protected abstract O doNextBatch() throws Exception;
    protected void before() throws Exception { /* 可覆盖 */ }
    protected void after(O batch) throws Exception { /* 可覆盖 */ }
}
```

### 4. SinkOperator<I> 抽象类
数据写入算子模板：
```java
public abstract class SinkOperator<I> extends AbstractOperator<I, Void> {
    protected final Void doProcess(I input) throws Exception {
        write(input);
        return null;
    }
    
    protected abstract void write(I input) throws Exception;
}
```

### 5. Pipeline<I,O> 类
流程调度器：
```java
Pipeline<List<String>, Void> pipeline = new Pipeline<>(source)
    .addOperator(transform)
    .addOperator(enrich)
    .addOperator(sink);

pipeline.run();
```

## 使用示例

### 基础用法

```java
// 1. 创建数据源
MySourceOperator source = new MySourceOperator(10, 100);

// 2. 创建处理算子
TransformOperator transform = new TransformOperator("PROCESSED_", true);
WriteToDbOperator sink = new WriteToDbOperator("output_table", false);

// 3. 构建管道
Pipeline<List<String>, Void> pipeline = new Pipeline<>(source)
    .addOperator(transform)
    .addOperator(sink);

// 4. 执行管道
pipeline.run();
```

### 自定义算子

```java
public class MyCustomOperator extends AbstractOperator<String, Integer> {
    
    @Override
    protected Integer doProcess(String input) throws Exception {
        return input.length();
    }
    
    @Override
    protected void before(String input) throws Exception {
        System.out.println("Processing: " + input);
    }
    
    @Override
    protected void after(String input, Integer output) throws Exception {
        System.out.println("Length: " + output);
    }
}
```

## 设计模式优势

1. **模板方法模式**：
   - 保证算子逻辑结构一致
   - before/after钩子支持扩展
   - 核心逻辑与辅助逻辑分离

2. **责任链模式**：
   - 算子易复用、易组合
   - Pipeline负责流程调度
   - 算子专注于业务逻辑

3. **策略模式**：
   - 不同算子实现不同处理策略
   - 支持运行时动态组合

## 扩展点

### 1. 日志和监控
```java
@Override
protected void before(I input) throws Exception {
    logger.info("Starting {} operator", name());
    metrics.startTimer(name());
}

@Override
protected void after(I input, O output) throws Exception {
    metrics.stopTimer(name());
    logger.info("Completed {} operator", name());
}
```

### 2. 限流和熔断
```java
@Override
protected void before(I input) throws Exception {
    rateLimiter.acquire();
    circuitBreaker.checkState();
}
```

### 3. 异步处理
```java
@Override
protected O doProcess(I input) throws Exception {
    return CompletableFuture
        .supplyAsync(() -> processAsync(input))
        .get(5, TimeUnit.SECONDS);
}
```

## 运行示例

```bash
# 编译
javac -cp . src/main/java/com/dus/pipeline/example/PipelineExample.java

# 运行
java -cp . com.dus.pipeline.example.PipelineExample
```

## 目录结构

```
src/
  main/
    java/
      com/dus/pipeline/
        core/                    # 核心框架
          Operator.java
          AbstractOperator.java
          Pipeline.java
          SourceOperator.java
          SinkOperator.java
        example/                 # 示例实现
          MySourceOperator.java
          TransformOperator.java
          EnrichOperator.java
          WriteToDbOperator.java
          PipelineExample.java
```

## 技术栈

- **Java 8+**：Lambda表达式、Stream API
- **设计模式**：模板方法、责任链、策略模式
- **并发支持**：线程安全的算子实现
- **异常处理**：完整的异常传播机制

## 性能特点

- **零拷贝**：数据在算子间直接传递
- **内存友好**：批处理模式控制内存使用
- **并发安全**：支持多线程环境
- **可扩展**：支持水平扩展和垂直扩展