package org.snhuan.huanDungeonRandom.trigger;

import java.util.HashMap;
import java.util.Map;

/**
 * 触发器执行结果类
 *
 * 封装触发器执行的结果信息，包括：
 * - 执行状态（成功/失败）
 * - 结果消息
 * - 执行数据
 * - 错误信息
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TriggerResult {
    
    private final boolean success;
    private final String message;
    private final Map<String, Object> data;
    private final Throwable error;
    private final long timestamp;
    
    /**
     * 私有构造函数
     * 
     * @param success 是否成功
     * @param message 结果消息
     * @param data 执行数据
     * @param error 错误信息
     */
    private TriggerResult(boolean success, String message, Map<String, Object> data, Throwable error) {
        this.success = success;
        this.message = message;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建成功结果
     * 
     * @return 成功结果
     */
    public static TriggerResult success() {
        return new TriggerResult(true, "触发器执行成功", null, null);
    }
    
    /**
     * 创建成功结果
     * 
     * @param message 成功消息
     * @return 成功结果
     */
    public static TriggerResult success(String message) {
        return new TriggerResult(true, message, null, null);
    }
    
    /**
     * 创建成功结果
     * 
     * @param message 成功消息
     * @param data 执行数据
     * @return 成功结果
     */
    public static TriggerResult success(String message, Map<String, Object> data) {
        return new TriggerResult(true, message, data, null);
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败结果
     */
    public static TriggerResult failure(String message) {
        return new TriggerResult(false, message, null, null);
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @param error 错误信息
     * @return 失败结果
     */
    public static TriggerResult failure(String message, Throwable error) {
        return new TriggerResult(false, message, null, error);
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @param data 执行数据
     * @param error 错误信息
     * @return 失败结果
     */
    public static TriggerResult failure(String message, Map<String, Object> data, Throwable error) {
        return new TriggerResult(false, message, data, error);
    }
    
    /**
     * 检查是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 检查是否失败
     * 
     * @return 是否失败
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * 获取结果消息
     * 
     * @return 结果消息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取执行数据
     * 
     * @return 执行数据的副本
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
     * 获取指定键的数据（带类型转换）
     * 
     * @param key 数据键
     * @param type 目标类型
     * @param <T> 类型参数
     * @return 转换后的数据值
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> type) {
        Object value = data.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * 检查是否包含指定键的数据
     * 
     * @param key 数据键
     * @return 是否包含
     */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
    
    /**
     * 获取错误信息
     * 
     * @return 错误信息
     */
    public Throwable getError() {
        return error;
    }
    
    /**
     * 检查是否有错误信息
     * 
     * @return 是否有错误
     */
    public boolean hasError() {
        return error != null;
    }
    
    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 添加数据
     * 
     * @param key 数据键
     * @param value 数据值
     * @return 当前结果对象（支持链式调用）
     */
    public TriggerResult addData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
    
    /**
     * 添加多个数据
     * 
     * @param additionalData 要添加的数据
     * @return 当前结果对象（支持链式调用）
     */
    public TriggerResult addData(Map<String, Object> additionalData) {
        if (additionalData != null) {
            this.data.putAll(additionalData);
        }
        return this;
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TriggerResult{");
        sb.append("success=").append(success);
        sb.append(", message='").append(message).append('\'');
        if (!data.isEmpty()) {
            sb.append(", data=").append(data);
        }
        if (error != null) {
            sb.append(", error=").append(error.getMessage());
        }
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 检查对象相等性
     * 
     * @param obj 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TriggerResult that = (TriggerResult) obj;
        
        if (success != that.success) return false;
        if (timestamp != that.timestamp) return false;
        if (!message.equals(that.message)) return false;
        if (!data.equals(that.data)) return false;
        return error != null ? error.equals(that.error) : that.error == null;
    }
    
    /**
     * 计算哈希码
     * 
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + message.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + (error != null ? error.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }
}
