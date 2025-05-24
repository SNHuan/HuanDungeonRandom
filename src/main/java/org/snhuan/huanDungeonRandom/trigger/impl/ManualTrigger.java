package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.ManualTriggerEvent;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 手动触发器实现 - 通过命令或API手动触发
 *
 * 特点：
 * - 只能通过手动方式触发
 * - 支持权限检查
 * - 支持冷却时间
 * - 支持触发次数限制
 * - 可配置触发消息
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ManualTrigger extends Trigger {
    
    // 手动触发器特有配置
    private String triggerMessage;
    private String successMessage;
    private String failureMessage;
    private boolean requireConfirmation;
    
    /**
     * 构造函数
     */
    public ManualTrigger() {
        super();
        this.type = TriggerType.MANUAL;
        this.triggerMessage = "手动触发器已激活";
        this.successMessage = "触发器执行成功";
        this.failureMessage = "触发器执行失败";
        this.requireConfirmation = false;
    }
    
    /**
     * 检查具体触发条件
     * 
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否满足触发条件
     */
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        // 手动触发器只接受ManualTriggerEvent
        if (!(event instanceof ManualTriggerEvent)) {
            return false;
        }
        
        ManualTriggerEvent manualEvent = (ManualTriggerEvent) event;
        
        // 检查事件中的触发器是否是当前触发器
        return this.equals(manualEvent.getTrigger());
    }
    
    /**
     * 执行具体触发逻辑
     * 
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 触发结果
     */
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        try {
            // 发送触发消息
            if (triggerMessage != null && !triggerMessage.isEmpty()) {
                player.sendMessage(triggerMessage);
            }
            
            // 创建结果数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("trigger_id", this.id);
            resultData.put("trigger_type", "MANUAL");
            resultData.put("player", player.getName());
            resultData.put("timestamp", System.currentTimeMillis());
            
            if (dungeonInstance != null) {
                resultData.put("dungeon_id", dungeonInstance.getId());
            }
            
            // 记录触发日志
            logger.info("手动触发器被激活: " + this.id + " by " + player.getName());
            
            return TriggerResult.success(successMessage, resultData);
            
        } catch (Exception e) {
            logger.severe("手动触发器执行异常: " + this.id + " - " + e.getMessage());
            e.printStackTrace();
            return TriggerResult.failure(failureMessage + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载触发器特定数据
     * 
     * @param config 配置节点
     */
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {
        this.triggerMessage = config.getString("trigger-message", "手动触发器已激活");
        this.successMessage = config.getString("success-message", "触发器执行成功");
        this.failureMessage = config.getString("failure-message", "触发器执行失败");
        this.requireConfirmation = config.getBoolean("require-confirmation", false);
    }
    
    /**
     * 保存触发器特定数据
     * 
     * @param config 配置节点
     */
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {
        config.set("trigger-message", triggerMessage);
        config.set("success-message", successMessage);
        config.set("failure-message", failureMessage);
        config.set("require-confirmation", requireConfirmation);
    }
    
    /**
     * 检查自定义条件
     * 
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否满足条件
     */
    @Override
    protected boolean checkConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        // 检查基础条件
        if (!super.checkConditions(player, event, dungeonInstance)) {
            return false;
        }
        
        // 如果需要确认，可以在这里添加确认逻辑
        if (requireConfirmation) {
            // 这里可以实现确认机制，比如检查玩家是否在指定时间内发送了确认命令
            // 暂时简化处理
            return true;
        }
        
        return true;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public String getTriggerMessage() {
        return triggerMessage;
    }
    
    public void setTriggerMessage(String triggerMessage) {
        this.triggerMessage = triggerMessage;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
    
    public String getFailureMessage() {
        return failureMessage;
    }
    
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
    
    public boolean isRequireConfirmation() {
        return requireConfirmation;
    }
    
    public void setRequireConfirmation(boolean requireConfirmation) {
        this.requireConfirmation = requireConfirmation;
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ManualTrigger{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", active=" + active +
                ", triggerMessage='" + triggerMessage + '\'' +
                ", requireConfirmation=" + requireConfirmation +
                '}';
    }
}
