package org.snhuan.huanDungeonRandom.blueprint;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证结果类 - 存储验证操作的结果信息
 * 
 * 用于各种验证操作的结果返回，包含：
 * - 验证是否成功
 * - 错误或警告消息
 * - 详细的验证信息
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ValidationResult {
    
    private final boolean valid;
    private final String mainMessage;
    private final List<String> errors;
    private final List<String> warnings;
    private final List<String> infos;
    
    /**
     * 构造函数 - 创建简单的验证结果
     * 
     * @param valid 是否验证成功
     * @param message 主要消息
     */
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.mainMessage = message != null ? message : "";
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.infos = new ArrayList<>();
        
        // 如果验证失败且有消息，将消息添加到错误列表
        if (!valid && message != null && !message.trim().isEmpty()) {
            this.errors.add(message);
        }
    }
    
    /**
     * 构造函数 - 创建详细的验证结果
     * 
     * @param valid 是否验证成功
     * @param mainMessage 主要消息
     * @param errors 错误列表
     * @param warnings 警告列表
     * @param infos 信息列表
     */
    public ValidationResult(boolean valid, String mainMessage, 
                          List<String> errors, List<String> warnings, List<String> infos) {
        this.valid = valid;
        this.mainMessage = mainMessage != null ? mainMessage : "";
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
        this.infos = infos != null ? new ArrayList<>(infos) : new ArrayList<>();
    }
    
    /**
     * 创建成功的验证结果
     * 
     * @param message 成功消息
     * @return 验证结果
     */
    public static ValidationResult success(String message) {
        return new ValidationResult(true, message);
    }
    
    /**
     * 创建失败的验证结果
     * 
     * @param message 失败消息
     * @return 验证结果
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message);
    }
    
    /**
     * 创建带警告的成功结果
     * 
     * @param message 主要消息
     * @param warnings 警告列表
     * @return 验证结果
     */
    public static ValidationResult successWithWarnings(String message, List<String> warnings) {
        return new ValidationResult(true, message, new ArrayList<>(), warnings, new ArrayList<>());
    }
    
    /**
     * 创建验证结果构建器
     * 
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 是否验证成功
     * 
     * @return 验证是否成功
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 获取主要消息
     * 
     * @return 主要消息
     */
    public String getMessage() {
        return mainMessage;
    }
    
    /**
     * 获取错误列表
     * 
     * @return 错误列表的副本
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 获取警告列表
     * 
     * @return 警告列表的副本
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    /**
     * 获取信息列表
     * 
     * @return 信息列表的副本
     */
    public List<String> getInfos() {
        return new ArrayList<>(infos);
    }
    
    /**
     * 是否有错误
     * 
     * @return 是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * 是否有警告
     * 
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    /**
     * 是否有信息
     * 
     * @return 是否有信息
     */
    public boolean hasInfos() {
        return !infos.isEmpty();
    }
    
    /**
     * 获取错误数量
     * 
     * @return 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * 获取警告数量
     * 
     * @return 警告数量
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * 获取格式化的完整消息
     * 
     * @return 格式化消息
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (!mainMessage.isEmpty()) {
            sb.append(mainMessage);
        }
        
        if (hasErrors()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("错误:");
            for (String error : errors) {
                sb.append("\n  - ").append(error);
            }
        }
        
        if (hasWarnings()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("警告:");
            for (String warning : warnings) {
                sb.append("\n  - ").append(warning);
            }
        }
        
        if (hasInfos()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append("信息:");
            for (String info : infos) {
                sb.append("\n  - ").append(info);
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 合并另一个验证结果
     * 
     * @param other 另一个验证结果
     * @return 合并后的新验证结果
     */
    public ValidationResult merge(ValidationResult other) {
        if (other == null) {
            return this;
        }
        
        boolean mergedValid = this.valid && other.valid;
        String mergedMessage = this.mainMessage;
        if (!other.mainMessage.isEmpty()) {
            if (!mergedMessage.isEmpty()) {
                mergedMessage += "; " + other.mainMessage;
            } else {
                mergedMessage = other.mainMessage;
            }
        }
        
        List<String> mergedErrors = new ArrayList<>(this.errors);
        mergedErrors.addAll(other.errors);
        
        List<String> mergedWarnings = new ArrayList<>(this.warnings);
        mergedWarnings.addAll(other.warnings);
        
        List<String> mergedInfos = new ArrayList<>(this.infos);
        mergedInfos.addAll(other.infos);
        
        return new ValidationResult(mergedValid, mergedMessage, mergedErrors, mergedWarnings, mergedInfos);
    }
    
    @Override
    public String toString() {
        return getFormattedMessage();
    }
    
    /**
     * 验证结果构建器
     */
    public static class Builder {
        private boolean valid = true;
        private String mainMessage = "";
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> infos = new ArrayList<>();
        
        /**
         * 设置验证状态
         * 
         * @param valid 是否有效
         * @return 构建器
         */
        public Builder setValid(boolean valid) {
            this.valid = valid;
            return this;
        }
        
        /**
         * 设置主要消息
         * 
         * @param message 主要消息
         * @return 构建器
         */
        public Builder setMessage(String message) {
            this.mainMessage = message != null ? message : "";
            return this;
        }
        
        /**
         * 添加错误
         * 
         * @param error 错误消息
         * @return 构建器
         */
        public Builder addError(String error) {
            if (error != null && !error.trim().isEmpty()) {
                this.errors.add(error);
                this.valid = false; // 有错误时自动设置为无效
            }
            return this;
        }
        
        /**
         * 添加警告
         * 
         * @param warning 警告消息
         * @return 构建器
         */
        public Builder addWarning(String warning) {
            if (warning != null && !warning.trim().isEmpty()) {
                this.warnings.add(warning);
            }
            return this;
        }
        
        /**
         * 添加信息
         * 
         * @param info 信息消息
         * @return 构建器
         */
        public Builder addInfo(String info) {
            if (info != null && !info.trim().isEmpty()) {
                this.infos.add(info);
            }
            return this;
        }
        
        /**
         * 添加多个错误
         * 
         * @param errors 错误列表
         * @return 构建器
         */
        public Builder addErrors(List<String> errors) {
            if (errors != null) {
                for (String error : errors) {
                    addError(error);
                }
            }
            return this;
        }
        
        /**
         * 添加多个警告
         * 
         * @param warnings 警告列表
         * @return 构建器
         */
        public Builder addWarnings(List<String> warnings) {
            if (warnings != null) {
                for (String warning : warnings) {
                    addWarning(warning);
                }
            }
            return this;
        }
        
        /**
         * 构建验证结果
         * 
         * @return 验证结果
         */
        public ValidationResult build() {
            return new ValidationResult(valid, mainMessage, errors, warnings, infos);
        }
    }
}
