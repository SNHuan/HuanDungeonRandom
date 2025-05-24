package org.snhuan.huanDungeonRandom.trigger;

import org.snhuan.huanDungeonRandom.trigger.impl.*;

import java.util.logging.Logger;

/**
 * 触发器工厂类 - 负责创建各种类型的触发器
 *
 * 支持的触发器类型：
 * - 玩家进入/离开触发器
 * - 玩家交互触发器
 * - 玩家移动触发器
 * - 方块破坏/放置触发器
 * - 红石变化触发器
 * - 时间间隔/延迟触发器
 * - 条件满足触发器
 * - 信号接收触发器
 * - 地牢开始/结束触发器
 * - 手动触发器
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TriggerFactory {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");
    
    /**
     * 创建触发器
     * 
     * @param type 触发器类型
     * @return 触发器实例，如果类型不支持则返回null
     */
    public Trigger createTrigger(Trigger.TriggerType type) {
        if (type == null) {
            logger.warning("尝试创建空类型的触发器");
            return null;
        }
        
        try {
            switch (type) {
                case PLAYER_ENTER:
                    return new PlayerEnterTrigger();
                case PLAYER_LEAVE:
                    return new PlayerLeaveTrigger();
                case PLAYER_INTERACT:
                    return new PlayerInteractTrigger();
                case PLAYER_MOVE:
                    return new PlayerMoveTrigger();
                case BLOCK_BREAK:
                    return new BlockBreakTrigger();
                case BLOCK_PLACE:
                    return new BlockPlaceTrigger();
                case REDSTONE_CHANGE:
                    return new RedstoneChangeTrigger();
                case TIME_INTERVAL:
                    return new TimeIntervalTrigger();
                case TIME_DELAY:
                    return new TimeDelayTrigger();
                case CONDITION_MET:
                    return new ConditionMetTrigger();
                case SIGNAL_RECEIVED:
                    return new SignalReceivedTrigger();
                case DUNGEON_START:
                    return new DungeonStartTrigger();
                case DUNGEON_END:
                    return new DungeonEndTrigger();
                case MANUAL:
                    return new ManualTrigger();
                default:
                    logger.warning("不支持的触发器类型: " + type);
                    return null;
            }
        } catch (Exception e) {
            logger.severe("创建触发器时发生异常: " + type + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建触发器（带ID）
     * 
     * @param id 触发器ID
     * @param type 触发器类型
     * @return 触发器实例
     */
    public Trigger createTrigger(String id, Trigger.TriggerType type) {
        Trigger trigger = createTrigger(type);
        if (trigger != null) {
            trigger.setId(id);
        }
        return trigger;
    }
    
    /**
     * 创建触发器（带完整信息）
     * 
     * @param id 触发器ID
     * @param type 触发器类型
     * @param name 触发器名称
     * @param description 触发器描述
     * @return 触发器实例
     */
    public Trigger createTrigger(String id, Trigger.TriggerType type, String name, String description) {
        Trigger trigger = createTrigger(id, type);
        if (trigger != null) {
            trigger.setName(name);
            trigger.setDescription(description);
        }
        return trigger;
    }
    
    /**
     * 检查触发器类型是否支持
     * 
     * @param type 触发器类型
     * @return 是否支持
     */
    public boolean isTypeSupported(Trigger.TriggerType type) {
        return createTrigger(type) != null;
    }
    
    /**
     * 获取所有支持的触发器类型
     * 
     * @return 支持的触发器类型数组
     */
    public Trigger.TriggerType[] getSupportedTypes() {
        return Trigger.TriggerType.values();
    }
    
    /**
     * 获取触发器类型的显示名称
     * 
     * @param type 触发器类型
     * @return 显示名称
     */
    public String getTypeDisplayName(Trigger.TriggerType type) {
        return type != null ? type.getDisplayName() : "未知类型";
    }
    
    /**
     * 获取触发器类型的描述
     * 
     * @param type 触发器类型
     * @return 描述
     */
    public String getTypeDescription(Trigger.TriggerType type) {
        return type != null ? type.getDescription() : "未知类型";
    }
}
