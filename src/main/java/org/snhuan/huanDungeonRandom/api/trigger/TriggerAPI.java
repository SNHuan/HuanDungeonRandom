package org.snhuan.huanDungeonRandom.api.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;
import org.snhuan.huanDungeonRandom.trigger.ManualTriggerEvent;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * 触发器系统API - 提供触发器相关的操作接口
 * 
 * 主要功能：
 * - 注册和注销自定义触发器
 * - 手动触发触发器
 * - 触发器信息查询
 * - 触发器状态管理
 * 
 * 使用示例：
 * ```java
 * TriggerAPI api = HuanDungeonAPI.getInstance().getTriggerAPI();
 * api.registerTrigger(new MyCustomTrigger());
 * api.manualTrigger("my_trigger", player, dungeon);
 * ```
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TriggerAPI {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom-TriggerAPI");
    
    private final TriggerManager triggerManager;
    
    /**
     * 构造函数
     * 
     * @param triggerManager 触发器管理器
     */
    public TriggerAPI(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
    }
    
    // ==================== 触发器注册和管理 ====================
    
    /**
     * 注册自定义触发器
     * 
     * @param trigger 触发器实例
     * @return 是否注册成功
     */
    public boolean registerTrigger(Trigger trigger) {
        if (trigger == null) {
            logger.warning("注册触发器失败：触发器实例不能为null");
            return false;
        }
        
        try {
            boolean success = triggerManager.registerTrigger(trigger);
            
            if (success) {
                logger.info("通过API注册触发器: " + trigger.getId());
            } else {
                logger.warning("触发器注册失败，可能已存在: " + trigger.getId());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.severe("注册触发器时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注销触发器
     * 
     * @param triggerId 触发器ID
     * @return 是否注销成功
     */
    public boolean unregisterTrigger(String triggerId) {
        if (triggerId == null) {
            return false;
        }
        
        try {
            boolean success = triggerManager.unregisterTrigger(triggerId);
            
            if (success) {
                logger.info("通过API注销触发器: " + triggerId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.severe("注销触发器时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 触发器执行 ====================
    
    /**
     * 手动触发触发器
     * 
     * @param triggerId 触发器ID
     * @param player 触发玩家
     * @param dungeon 地牢实例
     * @return 触发结果列表
     */
    public List<TriggerResult> manualTrigger(String triggerId, Player player, DungeonInstance dungeon) {
        if (triggerId == null) {
            logger.warning("手动触发失败：触发器ID不能为null");
            return List.of();
        }
        
        try {
            // 创建手动触发事件
            ManualTriggerEvent event = new ManualTriggerEvent(triggerId, player, dungeon);
            
            // 处理事件
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeon);
            
            logger.info("通过API手动触发: " + triggerId + ", 结果数量: " + results.size());
            return results;
            
        } catch (Exception e) {
            logger.severe("手动触发时发生异常: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * 处理事件触发
     * 
     * @param event 事件
     * @param player 相关玩家
     * @param dungeon 地牢实例
     * @return 触发结果列表
     */
    public List<TriggerResult> handleEvent(Event event, Player player, DungeonInstance dungeon) {
        if (event == null) {
            return List.of();
        }
        
        try {
            return triggerManager.handleEvent(event, player, dungeon);
            
        } catch (Exception e) {
            logger.severe("处理事件触发时发生异常: " + event.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    // ==================== 信息查询 ====================
    
    /**
     * 获取触发器实例
     * 
     * @param triggerId 触发器ID
     * @return 触发器实例，不存在返回null
     */
    public Trigger getTrigger(String triggerId) {
        return triggerManager.getTrigger(triggerId);
    }
    
    /**
     * 获取所有已注册的触发器
     * 
     * @return 触发器实例集合
     */
    public Collection<Trigger> getAllTriggers() {
        return triggerManager.getAllTriggers();
    }
    
    /**
     * 获取已注册触发器数量
     * 
     * @return 触发器数量
     */
    public int getRegisteredTriggerCount() {
        return triggerManager.getRegisteredTriggerCount();
    }
    
    /**
     * 获取地牢中的触发器
     * 
     * @param dungeon 地牢实例
     * @return 触发器列表
     */
    public List<Trigger> getDungeonTriggers(DungeonInstance dungeon) {
        return triggerManager.getDungeonTriggers(dungeon);
    }
    
    // ==================== 状态检查 ====================
    
    /**
     * 检查触发器是否存在
     * 
     * @param triggerId 触发器ID
     * @return 是否存在
     */
    public boolean triggerExists(String triggerId) {
        return getTrigger(triggerId) != null;
    }
    
    /**
     * 检查触发器是否启用
     * 
     * @param triggerId 触发器ID
     * @return 是否启用
     */
    public boolean isTriggerEnabled(String triggerId) {
        Trigger trigger = getTrigger(triggerId);
        return trigger != null && trigger.isEnabled();
    }
    
    /**
     * 检查触发器是否在冷却中
     * 
     * @param triggerId 触发器ID
     * @return 是否在冷却中
     */
    public boolean isTriggerOnCooldown(String triggerId) {
        Trigger trigger = getTrigger(triggerId);
        return trigger != null && trigger.isOnCooldown();
    }
    
    // ==================== 触发器控制 ====================
    
    /**
     * 启用触发器
     * 
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean enableTrigger(String triggerId) {
        Trigger trigger = getTrigger(triggerId);
        if (trigger == null) {
            return false;
        }
        
        try {
            trigger.setEnabled(true);
            logger.info("通过API启用触发器: " + triggerId);
            return true;
            
        } catch (Exception e) {
            logger.severe("启用触发器时发生异常: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 禁用触发器
     * 
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean disableTrigger(String triggerId) {
        Trigger trigger = getTrigger(triggerId);
        if (trigger == null) {
            return false;
        }
        
        try {
            trigger.setEnabled(false);
            logger.info("通过API禁用触发器: " + triggerId);
            return true;
            
        } catch (Exception e) {
            logger.severe("禁用触发器时发生异常: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 重置触发器冷却
     * 
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean resetTriggerCooldown(String triggerId) {
        Trigger trigger = getTrigger(triggerId);
        if (trigger == null) {
            return false;
        }
        
        try {
            trigger.resetCooldown();
            logger.info("通过API重置触发器冷却: " + triggerId);
            return true;
            
        } catch (Exception e) {
            logger.severe("重置触发器冷却时发生异常: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取触发器统计信息
     * 
     * @return 统计信息字符串
     */
    public String getTriggerStats() {
        int total = getRegisteredTriggerCount();
        int enabled = 0;
        int onCooldown = 0;
        
        for (Trigger trigger : getAllTriggers()) {
            if (trigger.isEnabled()) {
                enabled++;
            }
            if (trigger.isOnCooldown()) {
                onCooldown++;
            }
        }
        
        return String.format(
            "已注册触发器: %d, 启用: %d, 冷却中: %d",
            total, enabled, onCooldown
        );
    }
}
