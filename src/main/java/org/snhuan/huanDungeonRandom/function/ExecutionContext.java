package org.snhuan.huanDungeonRandom.function;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能执行上下文类 - 提供功能执行时的环境信息
 *
 * 包含触发事件、位置信息、自定义数据等
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ExecutionContext {

    private final String triggerId;
    private final TriggerType triggerType;
    private final long timestamp;

    // 触发信息
    private final Player triggerPlayer;
    private final Location triggerLocation;
    private final Event triggerEvent;
    private final DungeonInstance dungeonInstance;

    // 上下文数据
    private final Map<String, Object> contextData;

    /**
     * 触发类型枚举
     */
    public enum TriggerType {
        PLAYER_INTERACT("玩家交互"),
        PLAYER_MOVE("玩家移动"),
        BLOCK_BREAK("方块破坏"),
        BLOCK_PLACE("方块放置"),
        TIME_TRIGGER("时间触发"),
        CONDITION_TRIGGER("条件触发"),
        MANUAL_TRIGGER("手动触发"),
        SYSTEM_TRIGGER("系统触发");

        private final String displayName;

        TriggerType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 私有构造函数
     */
    private ExecutionContext(Builder builder) {
        this.triggerId = builder.triggerId;
        this.triggerType = builder.triggerType;
        this.timestamp = System.currentTimeMillis();

        this.triggerPlayer = builder.triggerPlayer;
        this.triggerLocation = builder.triggerLocation;
        this.triggerEvent = builder.triggerEvent;
        this.dungeonInstance = builder.dungeonInstance;

        this.contextData = new HashMap<>(builder.contextData);
    }

    /**
     * 创建构建器
     *
     * @param triggerId 触发器ID
     * @param triggerType 触发类型
     * @return 构建器实例
     */
    public static Builder builder(String triggerId, TriggerType triggerType) {
        return new Builder(triggerId, triggerType);
    }

    /**
     * 创建构建器（简化版本）
     *
     * @return 构建器实例
     */
    public static Builder builder() {
        return new Builder("unknown", TriggerType.SYSTEM_TRIGGER);
    }

    /**
     * 创建玩家交互上下文
     *
     * @param player 触发玩家
     * @param location 触发位置
     * @return 执行上下文
     */
    public static ExecutionContext playerInteract(Player player, Location location) {
        return builder("player_interact", TriggerType.PLAYER_INTERACT)
            .setTriggerPlayer(player)
            .setTriggerLocation(location)
            .build();
    }

    /**
     * 创建玩家移动上下文
     *
     * @param player 触发玩家
     * @param from 起始位置
     * @param to 目标位置
     * @return 执行上下文
     */
    public static ExecutionContext playerMove(Player player, Location from, Location to) {
        return builder("player_move", TriggerType.PLAYER_MOVE)
            .setTriggerPlayer(player)
            .setTriggerLocation(to)
            .addData("from_location", from)
            .addData("to_location", to)
            .build();
    }

    /**
     * 创建时间触发上下文
     *
     * @param triggerId 触发器ID
     * @return 执行上下文
     */
    public static ExecutionContext timeTrigger(String triggerId) {
        return builder(triggerId, TriggerType.TIME_TRIGGER)
            .build();
    }

    /**
     * 创建手动触发上下文
     *
     * @param triggerId 触发器ID
     * @param player 操作玩家
     * @return 执行上下文
     */
    public static ExecutionContext manualTrigger(String triggerId, Player player) {
        return builder(triggerId, TriggerType.MANUAL_TRIGGER)
            .setTriggerPlayer(player)
            .build();
    }

    // ==================== Getter 方法 ====================

    public String getTriggerId() { return triggerId; }
    public TriggerType getTriggerType() { return triggerType; }
    public long getTimestamp() { return timestamp; }
    public Player getTriggerPlayer() { return triggerPlayer; }
    public Location getTriggerLocation() { return triggerLocation != null ? triggerLocation.clone() : null; }
    public Event getTriggerEvent() { return triggerEvent; }
    public DungeonInstance getDungeonInstance() { return dungeonInstance; }

    /**
     * 获取上下文数据
     *
     * @return 上下文数据的副本
     */
    public Map<String, Object> getContextData() {
        return new HashMap<>(contextData);
    }

    /**
     * 获取指定键的数据
     *
     * @param key 数据键
     * @return 数据值
     */
    public Object getData(String key) {
        return contextData.get(key);
    }

    /**
     * 获取指定键的数据（带默认值）
     *
     * @param key 数据键
     * @param defaultValue 默认值
     * @return 数据值或默认值
     */
    public Object getData(String key, Object defaultValue) {
        return contextData.getOrDefault(key, defaultValue);
    }

    /**
     * 是否包含指定数据
     *
     * @param key 数据键
     * @return 是否包含
     */
    public boolean hasData(String key) {
        return contextData.containsKey(key);
    }

    @Override
    public String toString() {
        return String.format("ExecutionContext{triggerId='%s', triggerType=%s, timestamp=%d, player=%s}",
                           triggerId, triggerType, timestamp,
                           triggerPlayer != null ? triggerPlayer.getName() : "null");
    }

    /**
     * 执行上下文构建器
     */
    public static class Builder {
        private final String triggerId;
        private final TriggerType triggerType;

        private Player triggerPlayer;
        private Location triggerLocation;
        private Event triggerEvent;
        private DungeonInstance dungeonInstance;
        private final Map<String, Object> contextData = new HashMap<>();

        private Builder(String triggerId, TriggerType triggerType) {
            this.triggerId = triggerId;
            this.triggerType = triggerType;
        }

        public Builder setTriggerPlayer(Player player) {
            this.triggerPlayer = player;
            return this;
        }

        public Builder setTriggerLocation(Location location) {
            this.triggerLocation = location != null ? location.clone() : null;
            return this;
        }

        public Builder setTriggerEvent(Event event) {
            this.triggerEvent = event;
            return this;
        }

        public Builder setDungeonInstance(DungeonInstance dungeonInstance) {
            this.dungeonInstance = dungeonInstance;
            return this;
        }

        public Builder addData(String key, Object value) {
            this.contextData.put(key, value);
            return this;
        }

        public Builder addAllData(Map<String, Object> data) {
            if (data != null) {
                this.contextData.putAll(data);
            }
            return this;
        }

        public ExecutionContext build() {
            return new ExecutionContext(this);
        }
    }
}
