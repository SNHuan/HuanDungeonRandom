package org.snhuan.huanDungeonRandom.function;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能执行结果类 - 封装功能执行的结果信息
 * 
 * 包含执行状态、消息、数据等信息
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ExecutionResult {
    
    private final boolean success;
    private final String message;
    private final Map<String, Object> data;
    private final long executionTime;
    private final Exception exception;
    
    /**
     * 私有构造函数
     */
    private ExecutionResult(boolean success, String message, Map<String, Object> data, Exception exception) {
        this.success = success;
        this.message = message;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.executionTime = System.currentTimeMillis();
        this.exception = exception;
    }
    
    /**
     * 创建成功结果
     * 
     * @return 成功结果
     */
    public static ExecutionResult success() {
        return new ExecutionResult(true, "执行成功", null, null);
    }
    
    /**
     * 创建成功结果（带消息）
     * 
     * @param message 成功消息
     * @return 成功结果
     */
    public static ExecutionResult success(String message) {
        return new ExecutionResult(true, message, null, null);
    }
    
    /**
     * 创建成功结果（带数据）
     * 
     * @param message 成功消息
     * @param data 结果数据
     * @return 成功结果
     */
    public static ExecutionResult success(String message, Map<String, Object> data) {
        return new ExecutionResult(true, message, data, null);
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败结果
     */
    public static ExecutionResult failure(String message) {
        return new ExecutionResult(false, message, null, null);
    }
    
    /**
     * 创建失败结果（带异常）
     * 
     * @param message 失败消息
     * @param exception 异常信息
     * @return 失败结果
     */
    public static ExecutionResult failure(String message, Exception exception) {
        return new ExecutionResult(false, message, null, exception);
    }
    
    /**
     * 创建失败结果（带数据）
     * 
     * @param message 失败消息
     * @param data 结果数据
     * @return 失败结果
     */
    public static ExecutionResult failure(String message, Map<String, Object> data) {
        return new ExecutionResult(false, message, data, null);
    }
    
    // ==================== Getter 方法 ====================
    
    /**
     * 是否执行成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 是否执行失败
     * 
     * @return 是否失败
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 获取执行消息
     * 
     * @return 执行消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取结果数据
     * 
     * @return 结果数据的副本
     */
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    /**
     * 获取指定键的数据
     * 
     * @param key 数据键
     * @return 数据值
     */
    public Object getData(String key) {
        return data.get(key);
    }
    
    /**
     * 获取指定键的数据（带默认值）
     * 
     * @param key 数据键
     * @param defaultValue 默认值
     * @return 数据值或默认值
     */
    public Object getData(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }
    
    /**
     * 获取执行时间
     * 
     * @return 执行时间戳
     */
    public long getExecutionTime() {
        return executionTime;
    }
    
    /**
     * 获取异常信息
     * 
     * @return 异常对象，没有异常返回null
     */
    public Exception getException() {
        return exception;
    }
    
    /**
     * 是否包含指定数据
     * 
     * @param key 数据键
     * @return 是否包含
     */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
    
    /**
     * 获取数据数量
     * 
     * @return 数据数量
     */
    public int getDataCount() {
        return data.size();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExecutionResult{");
        sb.append("success=").append(success);
        sb.append(", message='").append(message).append('\'');
        
        if (!data.isEmpty()) {
            sb.append(", data=").append(data);
        }
        
        if (exception != null) {
            sb.append(", exception=").append(exception.getClass().getSimpleName());
        }
        
        sb.append(", executionTime=").append(executionTime);
        sb.append('}');
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ExecutionResult that = (ExecutionResult) obj;
        return success == that.success &&
               executionTime == that.executionTime &&
               message.equals(that.message) &&
               data.equals(that.data);
    }
    
    @Override
    public int hashCode() {
        int result = Boolean.hashCode(success);
        result = 31 * result + message.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + Long.hashCode(executionTime);
        return result;
    }
}
