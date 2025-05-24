package org.snhuan.huanDungeonRandom.core;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 地牢管理器 - 负责管理所有地牢实例的生命周期
 *
 * 主要功能：
 * - 地牢实例的创建和销毁
 * - 玩家与地牢的关联管理
 * - 地牢位置索引和查找
 * - 地牢状态监控
 * - 区块管理
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonManager {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");
    
    private final JavaPlugin plugin;
    
    // 地牢实例存储
    private final Map<String, DungeonInstance> dungeonInstances;
    private final Map<UUID, String> playerDungeonMap;
    private final Map<String, Set<String>> chunkDungeonMap;
    
    // 统计信息
    private long totalDungeonsCreated;
    private long totalDungeonsDestroyed;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     */
    public DungeonManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dungeonInstances = new ConcurrentHashMap<>();
        this.playerDungeonMap = new ConcurrentHashMap<>();
        this.chunkDungeonMap = new ConcurrentHashMap<>();
        this.totalDungeonsCreated = 0;
        this.totalDungeonsDestroyed = 0;
    }
    
    /**
     * 初始化地牢管理器
     * 
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            logger.info("正在初始化地牢管理器...");
            
            // 这里可以加载持久化的地牢数据
            
            logger.info("地牢管理器初始化完成");
            return true;
            
        } catch (Exception e) {
            logger.severe("地牢管理器初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 关闭地牢管理器
     */
    public void shutdown() {
        try {
            logger.info("正在关闭地牢管理器...");
            
            // 销毁所有活跃的地牢
            for (DungeonInstance dungeon : new ArrayList<>(dungeonInstances.values())) {
                destroyDungeon(dungeon.getInstanceId());
            }
            
            // 清理数据
            dungeonInstances.clear();
            playerDungeonMap.clear();
            chunkDungeonMap.clear();
            
            logger.info("地牢管理器已关闭");
            
        } catch (Exception e) {
            logger.severe("关闭地牢管理器时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建地牢实例
     * 
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param location 创建位置
     * @param createdBy 创建者
     * @return 创建的地牢实例
     */
    public DungeonInstance createDungeon(String dungeonId, DungeonTheme theme, Location location, UUID createdBy) {
        if (dungeonId == null || theme == null || location == null) {
            return null;
        }
        
        try {
            // 生成唯一的实例ID
            String instanceId = generateInstanceId(dungeonId);
            
            // 创建地牢实例
            DungeonInstance dungeon = DungeonInstance.builder(instanceId, dungeonId, theme, location.getWorld(), location)
                .setCreatedBy(createdBy)
                .setCreationReason("Manual Creation")
                .build();
            
            // 注册地牢实例
            registerDungeon(dungeon);
            
            totalDungeonsCreated++;
            logger.info("成功创建地牢实例: " + instanceId);
            
            return dungeon;
            
        } catch (Exception e) {
            logger.severe("创建地牢实例失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 注册地牢实例
     * 
     * @param dungeon 地牢实例
     */
    private void registerDungeon(DungeonInstance dungeon) {
        dungeonInstances.put(dungeon.getInstanceId(), dungeon);
        
        // 建立区块索引
        indexDungeonChunks(dungeon);
    }
    
    /**
     * 销毁地牢实例
     * 
     * @param instanceId 实例ID
     * @return 是否销毁成功
     */
    public boolean destroyDungeon(String instanceId) {
        DungeonInstance dungeon = dungeonInstances.get(instanceId);
        if (dungeon == null) {
            return false;
        }
        
        try {
            // 移除所有玩家
            for (Player player : dungeon.getPlayers()) {
                handlePlayerLeave(player, dungeon);
            }
            
            // 销毁地牢
            dungeon.destroy();
            
            // 注销地牢实例
            unregisterDungeon(dungeon);
            
            totalDungeonsDestroyed++;
            logger.info("成功销毁地牢实例: " + instanceId);
            
            return true;
            
        } catch (Exception e) {
            logger.severe("销毁地牢实例失败: " + instanceId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 注销地牢实例
     * 
     * @param dungeon 地牢实例
     */
    private void unregisterDungeon(DungeonInstance dungeon) {
        dungeonInstances.remove(dungeon.getInstanceId());
        
        // 移除区块索引
        removeChunkIndex(dungeon);
        
        // 移除玩家关联
        playerDungeonMap.entrySet().removeIf(entry -> 
            entry.getValue().equals(dungeon.getInstanceId()));
    }
    
    /**
     * 获取玩家所在的地牢
     * 
     * @param player 玩家
     * @return 地牢实例，如果不在地牢中返回null
     */
    public DungeonInstance getPlayerDungeon(Player player) {
        if (player == null) {
            return null;
        }
        
        String instanceId = playerDungeonMap.get(player.getUniqueId());
        if (instanceId != null) {
            return dungeonInstances.get(instanceId);
        }
        
        return null;
    }
    
    /**
     * 获取指定位置的地牢
     * 
     * @param location 位置
     * @return 地牢实例，如果位置不在地牢中返回null
     */
    public DungeonInstance getDungeonAtLocation(Location location) {
        if (location == null) {
            return null;
        }
        
        // 遍历所有地牢实例检查位置
        for (DungeonInstance dungeon : dungeonInstances.values()) {
            if (dungeon.isLocationInDungeon(location)) {
                return dungeon;
            }
        }
        
        return null;
    }
    
    /**
     * 玩家重新加入地牢
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    public void rejoinDungeon(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return;
        }
        
        // 传送玩家到地牢
        Location spawnLocation = dungeon.getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        }
        
        // 更新玩家地牢关联
        playerDungeonMap.put(player.getUniqueId(), dungeon.getInstanceId());
    }
    
    /**
     * 处理玩家离开地牢
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    public void handlePlayerLeave(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return;
        }
        
        // 从地牢中移除玩家
        dungeon.playerExit(player);
        
        // 移除玩家地牢关联
        playerDungeonMap.remove(player.getUniqueId());
        
        // 传送玩家到安全位置（这里可以配置默认的离开位置）
        // player.teleport(getDefaultExitLocation());
    }
    
    /**
     * 处理玩家进入地牢
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    public void handlePlayerEnter(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return;
        }
        
        // 玩家进入地牢
        if (dungeon.playerEnter(player)) {
            // 更新玩家地牢关联
            playerDungeonMap.put(player.getUniqueId(), dungeon.getInstanceId());
        }
    }
    
    /**
     * 处理玩家死亡
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    public void handlePlayerDeath(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return;
        }
        
        // 这里可以添加死亡处理逻辑
        // 比如记录死亡次数、检查是否需要复活等
    }
    
    /**
     * 处理玩家重生
     * 
     * @param player 玩家
     * @param dungeon 地牢实例
     */
    public void handlePlayerRespawn(Player player, DungeonInstance dungeon) {
        if (player == null || dungeon == null) {
            return;
        }
        
        // 这里可以添加重生处理逻辑
        // 比如恢复状态、给予物品等
    }
    
    /**
     * 获取区块中的地牢列表
     * 
     * @param chunk 区块
     * @return 地牢列表
     */
    public List<DungeonInstance> getDungeonsInChunk(Chunk chunk) {
        if (chunk == null) {
            return new ArrayList<>();
        }
        
        String chunkKey = getChunkKey(chunk);
        Set<String> dungeonIds = chunkDungeonMap.get(chunkKey);
        
        if (dungeonIds == null) {
            return new ArrayList<>();
        }
        
        return dungeonIds.stream()
            .map(dungeonInstances::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 建立地牢区块索引
     * 
     * @param dungeon 地牢实例
     */
    private void indexDungeonChunks(DungeonInstance dungeon) {
        // 这里应该根据地牢的边界计算涉及的区块
        // 暂时简化处理
        Location origin = dungeon.getOrigin();
        Chunk chunk = origin.getChunk();
        String chunkKey = getChunkKey(chunk);
        
        chunkDungeonMap.computeIfAbsent(chunkKey, k -> new HashSet<>())
            .add(dungeon.getInstanceId());
    }
    
    /**
     * 移除区块索引
     * 
     * @param dungeon 地牢实例
     */
    private void removeChunkIndex(DungeonInstance dungeon) {
        chunkDungeonMap.values().forEach(set -> set.remove(dungeon.getInstanceId()));
        chunkDungeonMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * 获取区块键
     * 
     * @param chunk 区块
     * @return 区块键
     */
    private String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    
    /**
     * 生成实例ID
     * 
     * @param dungeonId 地牢ID
     * @return 实例ID
     */
    private String generateInstanceId(String dungeonId) {
        return dungeonId + "_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString(new Random().nextInt());
    }
    
    // ==================== Getter 方法 ====================
    
    /**
     * 获取所有地牢实例
     * 
     * @return 地牢实例集合
     */
    public Collection<DungeonInstance> getAllDungeons() {
        return new ArrayList<>(dungeonInstances.values());
    }
    
    /**
     * 获取地牢实例
     * 
     * @param instanceId 实例ID
     * @return 地牢实例
     */
    public DungeonInstance getDungeon(String instanceId) {
        return dungeonInstances.get(instanceId);
    }
    
    /**
     * 获取活跃地牢数量
     * 
     * @return 活跃地牢数量
     */
    public int getActiveDungeonCount() {
        return dungeonInstances.size();
    }
    
    /**
     * 获取总创建数量
     * 
     * @return 总创建数量
     */
    public long getTotalDungeonsCreated() {
        return totalDungeonsCreated;
    }
    
    /**
     * 获取总销毁数量
     * 
     * @return 总销毁数量
     */
    public long getTotalDungeonsDestroyed() {
        return totalDungeonsDestroyed;
    }
}
