package com.dus.pipeline.exception;

/**
 * 钩子执行异常
 */
public class HookExecutionException extends PipelineException {
    
    private String hookName;
    
    public HookExecutionException(String hookName, String message, Throwable cause) {
        super("Hook [" + hookName + "] execution failed: " + message, cause);
        this.hookName = hookName;
    }
    
    public HookExecutionException(String hookName, String message) {
        super("Hook [" + hookName + "] execution failed: " + message);
        this.hookName = hookName;
    }
    
    public HookExecutionException(String hookName, Throwable cause) {
        super("Hook [" + hookName + "] execution failed", cause);
        this.hookName = hookName;
    }
    
    public String getHookName() {
        return hookName;
    }
}