package org.snhuan.huanDungeonRandom.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

/**
 * 玩家离开地牢事件 - 在玩家离开地牢前后触发
 * 
 * 包含两个子事件：
 * - Pre: 玩家离开前触发，可以取消
 * - Post: 玩家离开后触发，不可取消
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class PlayerLeaveDungeonEvent extends DungeonEvent {
    
    protected final Player player;
    
    /**
     * 玩家离开地牢前事件 - 可以取消玩家离开
     */
    public static class Pre extends PlayerLeaveDungeonEvent implements Cancellable {
        
        private static final HandlerList handlers = new HandlerList();
        private boolean cancelled = false;
        
        /**
         * 构造函数
         * 
         * @param player 玩家
         * @param dungeon 地牢实例
         */
        public Pre(Player player, DungeonInstance dungeon) {
            super(player, dungeon);
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
     * 玩家离开地牢后事件 - 玩家已成功离开地牢
     */
    public static class Post extends PlayerLeaveDungeonEvent {
        
        private static final HandlerList handlers = new HandlerList();
        
        /**
         * 构造函数
         * 
         * @param player 玩家
         * @param dungeon 地牢实例
         */
        public Post(Player player, DungeonInstance dungeon) {
            super(player, dungeon);
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
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    private PlayerLeaveDungeonEvent(Player player, DungeonInstance dungeon) {
        super(dungeon);
        this.player = player;
    }
    
    /**
     * 获取玩家
     * 
     * @return 玩家
     */
    public Player getPlayer() {
        return player;
    }
}
