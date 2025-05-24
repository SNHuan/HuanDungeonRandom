package org.snhuan.huanDungeonRandom.api.dungeon;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.snhuan.huanDungeonRandom.api.events.DungeonCreateEvent;
import org.snhuan.huanDungeonRandom.api.events.DungeonDestroyEvent;
import org.snhuan.huanDungeonRandom.api.events.PlayerEnterDungeonEvent;
import org.snhuan.huanDungeonRandom.api.events.PlayerLeaveDungeonEvent;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 地牢管理API - 提供地牢相关的操作接口
 * 
 * 主要功能：
 * - 创建和销毁地牢
 * - 玩家进入和离开地牢
 * - 地牢信息查询
 * - 地牢状态管理
 * 
 * 使用示例：
 * ```java
 * DungeonAPI api = HuanDungeonAPI.getInstance().getDungeonAPI();
 * DungeonInstance dungeon = api.createDungeon("test", theme, location, player.getUniqueId());
 * api.addPlayerToDungeon(player, dungeon);
 * ```
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonAPI {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom-DungeonAPI");
    
    private final DungeonManager dungeonManager;
    
    /**
     * 构造函数
     * 
     * @param dungeonManager 地牢管理器
     */
    public DungeonAPI(DungeonManager dungeonManager) {
        this.dungeonManager = dungeonManager;
    }
    
    // ==================== 地牢创建和销毁 ====================
    
    /**
     * 创建地牢实例
     * 
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param location 创建位置
     * @param createdBy 创建者UUID
     * @return 创建的地牢实例，失败返回null
     */
    public DungeonInstance createDungeon(String dungeonId, DungeonTheme theme, Location location, UUID createdBy) {
        if (dungeonId == null || theme == null || location == null) {
            logger.warning("创建地牢失败：参数不能为null");
            return null;
        }
        
        try {
            // 触发创建前事件
            DungeonCreateEvent.Pre preEvent = new DungeonCreateEvent.Pre(dungeonId, theme, location, createdBy);
            org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
            
            if (preEvent.isCancelled()) {
                logger.info("地牢创建被事件取消: " + dungeonId);
                return null;
            }
            
            // 创建地牢
            DungeonInstance dungeon = dungeonManager.createDungeon(dungeonId, theme, location, createdBy);
            
            if (dungeon != null) {
                // 触发创建后事件
                DungeonCreateEvent.Post postEvent = new DungeonCreateEvent.Post(dungeon);
                org.bukkit.Bukkit.getPluginManager().callEvent(postEvent);
                
                logger.info("通过API创建地牢: " + dungeon.getInstanceId());
            }
            
            return dungeon;
            
        } catch (Exception e) {
            logger.severe("创建地牢时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 销毁地牢实例
     * 
     * @param instanceId 地牢实例ID
     * @return 是否销毁成功
     */
    public boolean destroyDungeon(String instanceId) {
        if (instanceId == null) {
            return false;
        }
        
        try {
            DungeonInstance dungeon = dungeonManager.getDungeon(instanceId);
            if (dungeon == null) {
                return false;
            }
            
            // 触发销毁前事件
            DungeonDestroyEvent.Pre preEvent = new DungeonDestroyEvent.Pre(dungeon);
            org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
            
            if (preEvent.isCancelled()) {
                logger.info("地牢销毁被事件取消: " + instanceId);
                return false;
            }
            
            // 销毁地牢
            boolean success = dungeonManager.destroyDungeon(instanceId);
            
            if (success) {
                // 触发销毁后事件
                DungeonDestroyEvent.Post postEvent = new DungeonDestroyEvent.Post(instanceId);
                org.bukkit.Bukkit.getPluginManager().callEvent(postEvent);
                
                logger.info("通过API销毁地牢: " + instanceId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.severe("销毁地牢时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 玩家管理 ====================
    
    /**
     * 玩家进入地牢
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     * @return 是否成功进入
     */
    public boolean addPlayerToDungeon(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return false;
        }
        
        try {
            // 触发进入前事件
            PlayerEnterDungeonEvent.Pre preEvent = new PlayerEnterDungeonEvent.Pre(player, dungeon);
            org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
            
            if (preEvent.isCancelled()) {
                return false;
            }
            
            // 玩家进入地牢
            dungeonManager.handlePlayerEnter(player, dungeon);
            
            // 触发进入后事件
            PlayerEnterDungeonEvent.Post postEvent = new PlayerEnterDungeonEvent.Post(player, dungeon);
            org.bukkit.Bukkit.getPluginManager().callEvent(postEvent);
            
            return true;
            
        } catch (Exception e) {
            logger.severe("玩家进入地牢时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 玩家离开地牢
     * 
     * @param player 玩家
     * @return 是否成功离开
     */
    public boolean removePlayerFromDungeon(Player player) {
        if (player == null) {
            return false;
        }
        
        try {
            DungeonInstance dungeon = dungeonManager.getPlayerDungeon(player);
            if (dungeon == null) {
                return false;
            }
            
            // 触发离开前事件
            PlayerLeaveDungeonEvent.Pre preEvent = new PlayerLeaveDungeonEvent.Pre(player, dungeon);
            org.bukkit.Bukkit.getPluginManager().callEvent(preEvent);
            
            if (preEvent.isCancelled()) {
                return false;
            }
            
            // 玩家离开地牢
            dungeonManager.handlePlayerLeave(player, dungeon);
            
            // 触发离开后事件
            PlayerLeaveDungeonEvent.Post postEvent = new PlayerLeaveDungeonEvent.Post(player, dungeon);
            org.bukkit.Bukkit.getPluginManager().callEvent(postEvent);
            
            return true;
            
        } catch (Exception e) {
            logger.severe("玩家离开地牢时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ==================== 信息查询 ====================
    
    /**
     * 获取地牢实例
     * 
     * @param instanceId 实例ID
     * @return 地牢实例，不存在返回null
     */
    public DungeonInstance getDungeon(String instanceId) {
        return dungeonManager.getDungeon(instanceId);
    }
    
    /**
     * 获取玩家当前所在的地牢
     * 
     * @param player 玩家
     * @return 地牢实例，不在地牢中返回null
     */
    public DungeonInstance getPlayerDungeon(Player player) {
        return dungeonManager.getPlayerDungeon(player);
    }
    
    /**
     * 获取指定位置的地牢
     * 
     * @param location 位置
     * @return 地牢实例，位置不在地牢中返回null
     */
    public DungeonInstance getDungeonAtLocation(Location location) {
        return dungeonManager.getDungeonAtLocation(location);
    }
    
    /**
     * 获取所有活跃的地牢
     * 
     * @return 地牢实例集合
     */
    public Collection<DungeonInstance> getAllDungeons() {
        return dungeonManager.getAllDungeons();
    }
    
    /**
     * 获取活跃地牢数量
     * 
     * @return 活跃地牢数量
     */
    public int getActiveDungeonCount() {
        return dungeonManager.getActiveDungeonCount();
    }
    
    // ==================== 状态检查 ====================
    
    /**
     * 检查玩家是否在地牢中
     * 
     * @param player 玩家
     * @return 是否在地牢中
     */
    public boolean isPlayerInDungeon(Player player) {
        return getPlayerDungeon(player) != null;
    }
    
    /**
     * 检查位置是否在地牢中
     * 
     * @param location 位置
     * @return 是否在地牢中
     */
    public boolean isLocationInDungeon(Location location) {
        return getDungeonAtLocation(location) != null;
    }
    
    /**
     * 检查地牢是否存在
     * 
     * @param instanceId 实例ID
     * @return 是否存在
     */
    public boolean dungeonExists(String instanceId) {
        return getDungeon(instanceId) != null;
    }
}
