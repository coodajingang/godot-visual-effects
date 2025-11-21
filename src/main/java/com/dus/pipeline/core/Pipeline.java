package com.dus.pipeline.core;

import com.dus.pipeline.context.PipelineContext;
import com.dus.pipeline.retry.RetryStrategy;
import com.dus.pipeline.retry.SkipStrategy;
import com.dus.pipeline.retry.RetryableOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline 主类，负责编排算子的执行流程
 */
public class Pipeline<I, O> {
    
    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);
    
    private final List<Operator<I, ?>> operators = new ArrayList<>();
    private SourceOperator<I> source;
    private PipelineContext context;
    private List<Runnable> preHooks = new ArrayList<>();
    private List<Runnable> postHooks = new ArrayList<>();
    
    /**
     * 构造函数
     */
    public Pipeline(SourceOperator<I> source) {
        this.source = source;
    }
    
    /**
     * 添加算子
     */
    @SuppressWarnings("unchecked")
    public Pipeline<I, O> addOperator(Operator<?, ?> operator) {
        operators.add((Operator<I, ?>) operator);
        return (Pipeline<I, O>) this;
    }
    
    /**
     * 设置 Pipeline 上下文
     */
    public Pipeline<I, O> withContext(PipelineContext context) {
        this.context = context;
        return this;
    }
    
    /**
     * 添加前置钩子
     */
    public Pipeline<I, O> addPreHook(Runnable hook) {
        preHooks.add(hook);
        return this;
    }
    
    /**
     * 添加后置钩子
     */
    public Pipeline<I, O> addPostHook(Runnable hook) {
        postHooks.add(hook);
        return this;
    }
    
    /**
     * 为某个算子设置重试策略
     */
    public Pipeline<I, O> setOperatorRetryStrategy(int operatorIndex, RetryStrategy strategy) {
        if (operatorIndex < 0 || operatorIndex >= operators.size()) {
            throw new IllegalArgumentException("Operator index out of bounds: " + operatorIndex);
        }
        
        Operator<?, ?> op = operators.get(operatorIndex);
        if (op instanceof RetryableOperator) {
            ((RetryableOperator<?, ?>) op).withRetryStrategy(strategy);
        } else {
            logger.warn("Operator {} does not support retry", operatorIndex);
        }
        return this;
    }
    
    /**
     * 为某个算子设置跳过策略
     */
    public Pipeline<I, O> setOperatorSkipStrategy(int operatorIndex, SkipStrategy strategy) {
        if (operatorIndex < 0 || operatorIndex >= operators.size()) {
            throw new IllegalArgumentException("Operator index out of bounds: " + operatorIndex);
        }
        
        Operator<?, ?> op = operators.get(operatorIndex);
        if (op instanceof RetryableOperator) {
            ((RetryableOperator<?, ?>) op).withSkipStrategy(strategy);
        } else {
            logger.warn("Operator {} does not support skip", operatorIndex);
        }
        return this;
    }
    
    /**
     * 内部方法：注入 Context 到所有算子
     */
    private void injectContextToOperators() {
        if (context == null) {
            return;
        }
        
        // 注入到 source
        if (source instanceof ContextAware) {
            ((ContextAware) source).setContext(context);
        }
        
        // 注入到所有 operators
        for (Operator<?, ?> op : operators) {
            if (op instanceof ContextAware) {
                ((ContextAware) op).setContext(context);
            }
        }
    }
    
    /**
     * 执行 Pipeline
     */
    public void run() {
        logger.info("Starting pipeline execution");
        
        // 初始化 Context
        if (context == null) {
            context = new PipelineContext();
        }
        
        // 注入 Context 到所有算子
        injectContextToOperators();
        
        try {
            // 执行前置钩子
            executePreHooks();
            
            // 执行 Pipeline 主逻辑
            executePipeline();
            
            // 执行后置钩子
            executePostHooks();
            
            logger.info("Pipeline completed successfully");
            
        } catch (Exception e) {
            logger.error("Pipeline execution failed", e);
            throw new RuntimeException("Pipeline execution failed", e);
        }
    }
    
    /**
     * 执行前置钩子
     */
    private void executePreHooks() {
        for (Runnable hook : preHooks) {
            try {
                hook.run();
            } catch (Exception e) {
                logger.warn("Pre-hook execution failed", e);
            }
        }
    }
    
    /**
     * 执行后置钩子
     */
    private void executePostHooks() {
        for (Runnable hook : postHooks) {
            try {
                hook.run();
            } catch (Exception e) {
                logger.warn("Post-hook execution failed", e);
            }
        }
    }
    
    /**
     * 执行 Pipeline 主逻辑
     */
    @SuppressWarnings("unchecked")
    private void executePipeline() {
        while (source.hasNext()) {
            // 获取数据
            I batch = source.nextBatch();
            if (batch == null) {
                continue;
            }
            
            // 依次通过所有算子
            Object currentData = batch;
            for (int i = 0; i < operators.size(); i++) {
                Operator<Object, Object> operator = (Operator<Object, Object>) operators.get(i);
                
                try {
                    currentData = operator.process(currentData);
                    
                    // 如果返回 null，跳过后续算子
                    if (currentData == null) {
                        logger.debug("Operator {} returned null, skipping remaining operators", operator.name());
                        break;
                    }
                    
                } catch (Exception e) {
                    logger.error("Operator {} failed", operator.name(), e);
                    throw new RuntimeException("Operator " + operator.name() + " failed", e);
                }
            }
        }
    }
    
    /**
     * 获取 Pipeline 上下文
     */
    public PipelineContext getContext() {
        return context;
    }
    
    /**
     * 获取算子列表
     */
    public List<Operator<I, ?>> getOperators() {
        return new ArrayList<>(operators);
    }
    
    /**
     * 获取源算子
     */
    public SourceOperator<I> getSource() {
        return source;
    }
}