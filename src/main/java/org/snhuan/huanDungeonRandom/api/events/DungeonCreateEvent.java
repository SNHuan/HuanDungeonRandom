package org.snhuan.huanDungeonRandom.api.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.UUID;

/**
 * 地牢创建事件 - 在地牢创建前后触发
 * 
 * 包含两个子事件：
 * - Pre: 地牢创建前触发，可以取消
 * - Post: 地牢创建后触发，不可取消
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class DungeonCreateEvent extends DungeonEvent {
    
    /**
     * 地牢创建前事件 - 可以取消地牢创建
     */
    public static class Pre extends DungeonCreateEvent implements Cancellable {
        
        private static final HandlerList handlers = new HandlerList();
        private boolean cancelled = false;
        
        private final String dungeonId;
        private final DungeonTheme theme;
        private final Location location;
        private final UUID createdBy;
        
        /**
         * 构造函数
         * 
         * @param dungeonId 地牢ID
         * @param theme 地牢主题
         * @param location 创建位置
         * @param createdBy 创建者UUID
         */
        public Pre(String dungeonId, DungeonTheme theme, Location location, UUID createdBy) {
            super(null); // 地牢还未创建
            this.dungeonId = dungeonId;
            this.theme = theme;
            this.location = location;
            this.createdBy = createdBy;
        }
        
        /**
         * 获取地牢ID
         * 
         * @return 地牢ID
         */
        public String getDungeonId() {
            return dungeonId;
        }
        
        /**
         * 获取地牢主题
         * 
         * @return 地牢主题
         */
        public DungeonTheme getTheme() {
            return theme;
        }
        
        /**
         * 获取创建位置
         * 
         * @return 创建位置
         */
        public Location getLocation() {
            return location;
        }
        
        /**
         * 获取创建者UUID
         * 
         * @return 创建者UUID
         */
        public UUID getCreatedBy() {
            return createdBy;
        }
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }
        
        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
        
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }
        
        public static HandlerList getHandlerList() {
            return handlers;
        }
    }
    
    /**
     * 地牢创建后事件 - 地牢已成功创建
     */
    public static class Post extends DungeonCreateEvent {
        
        private static final HandlerList handlers = new HandlerList();
        
        /**
         * 构造函数
         * 
         * @param dungeon 创建的地牢实例
         */
        public Post(DungeonInstance dungeon) {
            super(dungeon);
        }
        
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }
        
        public static HandlerList getHandlerList() {
            return handlers;
        }
    }
    
    /**
     * 私有构造函数
     * 
     * @param dungeon 地牢实例
     */
    private DungeonCreateEvent(DungeonInstance dungeon) {
        super(dungeon);
    }
}
