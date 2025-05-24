package org.snhuan.huanDungeonRandom.trigger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

/**
 * 手动触发事件 - 用于手动触发器的事件包装
 *
 * 当通过API或命令手动触发触发器时使用此事件类型
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ManualTriggerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final Trigger trigger;
    private final String triggerId;
    private final DungeonInstance dungeonInstance;
    private final long timestamp;

    /**
     * 构造函数
     *
     * @param player 触发玩家
     * @param trigger 被触发的触发器
     */
    public ManualTriggerEvent(Player player, Trigger trigger) {
        this.player = player;
        this.trigger = trigger;
        this.triggerId = trigger != null ? trigger.getId() : null;
        this.dungeonInstance = null;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数（API版本）
     *
     * @param triggerId 触发器ID
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     */
    public ManualTriggerEvent(String triggerId, Player player, DungeonInstance dungeonInstance) {
        this.player = player;
        this.trigger = null;
        this.triggerId = triggerId;
        this.dungeonInstance = dungeonInstance;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取触发玩家
     *
     * @return 触发玩家
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 获取被触发的触发器
     *
     * @return 触发器
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * 获取触发器ID
     *
     * @return 触发器ID
     */
    public String getTriggerId() {
        return triggerId;
    }

    /**
     * 获取地牢实例
     *
     * @return 地牢实例
     */
    public DungeonInstance getDungeonInstance() {
        return dungeonInstance;
    }

    /**
     * 获取触发时间戳
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
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

    /**
     * 转换为字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "ManualTriggerEvent{" +
                "player=" + (player != null ? player.getName() : "null") +
                ", trigger=" + (trigger != null ? trigger.getId() : "null") +
                ", timestamp=" + timestamp +
                '}';
    }
}
