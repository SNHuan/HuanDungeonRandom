package org.snhuan.huanDungeonRandom.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

/**
 * 地牢销毁事件 - 在地牢销毁前后触发
 * 
 * 包含两个子事件：
 * - Pre: 地牢销毁前触发，可以取消
 * - Post: 地牢销毁后触发，不可取消
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class DungeonDestroyEvent extends DungeonEvent {
    
    /**
     * 地牢销毁前事件 - 可以取消地牢销毁
     */
    public static class Pre extends DungeonDestroyEvent implements Cancellable {
        
        private static final HandlerList handlers = new HandlerList();
        private boolean cancelled = false;
        
        /**
         * 构造函数
         * 
         * @param dungeon 要销毁的地牢实例
         */
        public Pre(DungeonInstance dungeon) {
            super(dungeon);
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
     * 地牢销毁后事件 - 地牢已被销毁
     */
    public static class Post extends DungeonDestroyEvent {
        
        private static final HandlerList handlers = new HandlerList();
        private final String dungeonId;
        
        /**
         * 构造函数
         * 
         * @param dungeonId 已销毁的地牢ID
         */
        public Post(String dungeonId) {
            super(null); // 地牢已被销毁
            this.dungeonId = dungeonId;
        }
        
        /**
         * 获取已销毁的地牢ID
         * 
         * @return 地牢ID
         */
        @Override
        public String getDungeonId() {
            return dungeonId;
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
    private DungeonDestroyEvent(DungeonInstance dungeon) {
        super(dungeon);
    }
}
