package org.snhuan.huanDungeonRandom.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

/**
 * 地牢事件基类 - 所有地牢相关事件的基类
 * 
 * 提供地牢事件的通用功能：
 * - 地牢实例引用
 * - 事件处理器列表
 * - 基础事件信息
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class DungeonEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    protected final DungeonInstance dungeon;
    
    /**
     * 构造函数
     * 
     * @param dungeon 地牢实例
     */
    public DungeonEvent(DungeonInstance dungeon) {
        this.dungeon = dungeon;
    }
    
    /**
     * 构造函数（异步版本）
     * 
     * @param dungeon 地牢实例
     * @param isAsync 是否异步
     */
    public DungeonEvent(DungeonInstance dungeon, boolean isAsync) {
        super(isAsync);
        this.dungeon = dungeon;
    }
    
    /**
     * 获取地牢实例
     * 
     * @return 地牢实例
     */
    public DungeonInstance getDungeon() {
        return dungeon;
    }
    
    /**
     * 获取地牢ID
     * 
     * @return 地牢ID
     */
    public String getDungeonId() {
        return dungeon != null ? dungeon.getInstanceId() : null;
    }
    
    /**
     * 获取事件处理器列表
     * 
     * @return 处理器列表
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * 获取静态处理器列表
     * 
     * @return 静态处理器列表
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
