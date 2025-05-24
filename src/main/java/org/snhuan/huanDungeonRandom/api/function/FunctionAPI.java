package org.snhuan.huanDungeonRandom.api.function;

import org.bukkit.entity.Player;
import org.snhuan.huanDungeonRandom.function.Function;
import org.snhuan.huanDungeonRandom.function.FunctionManager;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.function.ExecutionResult;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * 功能系统API - 提供功能相关的操作接口
 * 
 * 主要功能：
 * - 注册和注销自定义功能
 * - 执行功能
 * - 功能信息查询
 * - 功能状态管理
 * 
 * 使用示例：
 * ```java
 * FunctionAPI api = HuanDungeonAPI.getInstance().getFunctionAPI();
 * api.registerFunction(new MyCustomFunction());
 * api.executeFunction("my_function", context);
 * ```
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class FunctionAPI {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom-FunctionAPI");
    
    private final FunctionManager functionManager;
    
    /**
     * 构造函数
     * 
     * @param functionManager 功能管理器
     */
    public FunctionAPI(FunctionManager functionManager) {
        this.functionManager = functionManager;
    }
    
    // ==================== 功能注册和管理 ====================
    
    /**
     * 注册自定义功能
     * 
     * @param function 功能实例
     * @return 是否注册成功
     */
    public boolean registerFunction(Function function) {
        if (function == null) {
            logger.warning("注册功能失败：功能实例不能为null");
            return false;
        }
        
        try {
            boolean success = functionManager.registerFunction(function);
            
            if (success) {
                logger.info("通过API注册功能: " + function.getId());
            } else {
                logger.warning("功能注册失败，可能已存在: " + function.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.severe("注册功能时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注销功能
     * 
     * @param functionId 功能ID
     * @return 是否注销成功
     */
    public boolean unregisterFunction(String functionId) {
        if (functionId == null) {
            return false;
        }
        
        try {
            boolean success = functionManager.unregisterFunction(functionId);
            
            if (success) {
                logger.info("通过API注销功能: " + functionId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.severe("注销功能时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 功能执行 ====================
    
    /**
     * 执行功能
     * 
     * @param functionId 功能ID
     * @param context 执行上下文
     * @return 执行结果
     */
    public ExecutionResult executeFunction(String functionId, ExecutionContext context) {
        if (functionId == null || context == null) {
            return ExecutionResult.failure("参数不能为null");
        }
        
        try {
            return functionManager.executeFunction(functionId, context);
            
        } catch (Exception e) {
            logger.severe("执行功能时发生异常: " + functionId + " - " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.failure("执行异常: " + e.getMessage());
        }
    }
    
    /**
     * 执行功能（简化版本）
     * 
     * @param functionId 功能ID
     * @param player 触发玩家
     * @return 执行结果
     */
    public ExecutionResult executeFunction(String functionId, Player player) {
        ExecutionContext context = ExecutionContext.builder()
            .setTriggerPlayer(player)
            .setTriggerLocation(player != null ? player.getLocation() : null)
            .build();
        
        return executeFunction(functionId, context);
    }
    
    /**
     * 异步执行功能
     * 
     * @param functionId 功能ID
     * @param context 执行上下文
     * @param callback 回调接口
     */
    public void executeAsync(String functionId, ExecutionContext context, FunctionCallback callback) {
        if (functionId == null || context == null) {
            if (callback != null) {
                callback.onComplete(ExecutionResult.failure("参数不能为null"));
            }
            return;
        }
        
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(
            functionManager.getPlugin(),
            () -> {
                ExecutionResult result = executeFunction(functionId, context);
                if (callback != null) {
                    // 回到主线程执行回调
                    org.bukkit.Bukkit.getScheduler().runTask(
                        functionManager.getPlugin(),
                        () -> callback.onComplete(result)
                    );
                }
            }
        );
    }
    
    // ==================== 信息查询 ====================
    
    /**
     * 获取功能实例
     * 
     * @param functionId 功能ID
     * @return 功能实例，不存在返回null
     */
    public Function getFunction(String functionId) {
        return functionManager.getFunction(functionId);
    }
    
    /**
     * 获取所有已注册的功能
     * 
     * @return 功能实例集合
     */
    public Collection<Function> getAllFunctions() {
        return functionManager.getAllFunctions();
    }
    
    /**
     * 获取已注册功能数量
     * 
     * @return 功能数量
     */
    public int getRegisteredFunctionCount() {
        return functionManager.getRegisteredFunctionCount();
    }
    
    // ==================== 状态检查 ====================
    
    /**
     * 检查功能是否存在
     * 
     * @param functionId 功能ID
     * @return 是否存在
     */
    public boolean functionExists(String functionId) {
        return getFunction(functionId) != null;
    }
    
    /**
     * 检查功能是否启用
     * 
     * @param functionId 功能ID
     * @return 是否启用
     */
    public boolean isFunctionEnabled(String functionId) {
        Function function = getFunction(functionId);
        return function != null && function.isEnabled();
    }
    
    // ==================== 功能控制 ====================
    
    /**
     * 启用功能
     * 
     * @param functionId 功能ID
     * @return 是否成功
     */
    public boolean enableFunction(String functionId) {
        Function function = getFunction(functionId);
        if (function == null) {
            return false;
        }
        
        try {
            function.setEnabled(true);
            logger.info("通过API启用功能: " + functionId);
            return true;
            
        } catch (Exception e) {
            logger.severe("启用功能时发生异常: " + functionId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 禁用功能
     * 
     * @param functionId 功能ID
     * @return 是否成功
     */
    public boolean disableFunction(String functionId) {
        Function function = getFunction(functionId);
        if (function == null) {
            return false;
        }
        
        try {
            function.setEnabled(false);
            logger.info("通过API禁用功能: " + functionId);
            return true;
            
        } catch (Exception e) {
            logger.severe("禁用功能时发生异常: " + functionId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取功能统计信息
     * 
     * @return 统计信息字符串
     */
    public String getFunctionStats() {
        int total = getRegisteredFunctionCount();
        int enabled = 0;
        
        for (Function function : getAllFunctions()) {
            if (function.isEnabled()) {
                enabled++;
            }
        }
        
        return String.format(
            "已注册功能: %d, 启用: %d, 禁用: %d",
            total, enabled, total - enabled
        );
    }
    
    /**
     * 功能执行回调接口
     */
    public interface FunctionCallback {
        /**
         * 功能执行完成回调
         * 
         * @param result 执行结果
         */
        void onComplete(ExecutionResult result);
    }
}
