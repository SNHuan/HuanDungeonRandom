package org.snhuan.huanDungeonRandom.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.api.dungeon.DungeonAPI;
import org.snhuan.huanDungeonRandom.api.function.FunctionAPI;
import org.snhuan.huanDungeonRandom.api.trigger.TriggerAPI;
import org.snhuan.huanDungeonRandom.api.blueprint.BlueprintAPI;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.FunctionManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * HuanDungeonRandom 主API接口
 * 
 * 提供给其他插件使用的核心API，包括：
 * - 地牢管理API
 * - 功能系统API
 * - 触发器系统API
 * - 蓝图系统API
 * - 事件监听API
 * 
 * 使用示例：
 * ```java
 * HuanDungeonAPI api = HuanDungeonAPI.getInstance();
 * DungeonInstance dungeon = api.getDungeonAPI().createDungeon("test", theme, location, player.getUniqueId());
 * ```
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class HuanDungeonAPI {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom-API");
    private static HuanDungeonAPI instance;
    
    private final JavaPlugin plugin;
    private final String version;
    
    // 子API实例
    private final DungeonAPI dungeonAPI;
    private final FunctionAPI functionAPI;
    private final TriggerAPI triggerAPI;
    private final BlueprintAPI blueprintAPI;
    
    // 管理器实例
    private final DungeonManager dungeonManager;
    private final FunctionManager functionManager;
    private final TriggerManager triggerManager;
    
    /**
     * 私有构造函数
     * 
     * @param plugin 插件实例
     * @param dungeonManager 地牢管理器
     * @param functionManager 功能管理器
     * @param triggerManager 触发器管理器
     */
    private HuanDungeonAPI(JavaPlugin plugin, DungeonManager dungeonManager, 
                          FunctionManager functionManager, TriggerManager triggerManager) {
        this.plugin = plugin;
        this.version = plugin.getDescription().getVersion();
        this.dungeonManager = dungeonManager;
        this.functionManager = functionManager;
        this.triggerManager = triggerManager;
        
        // 初始化子API
        this.dungeonAPI = new DungeonAPI(dungeonManager);
        this.functionAPI = new FunctionAPI(functionManager);
        this.triggerAPI = new TriggerAPI(triggerManager);
        this.blueprintAPI = new BlueprintAPI();
        
        logger.info("HuanDungeonRandom API v" + version + " 已初始化");
    }
    
    /**
     * 初始化API实例
     * 
     * @param plugin 插件实例
     * @param dungeonManager 地牢管理器
     * @param functionManager 功能管理器
     * @param triggerManager 触发器管理器
     */
    public static void initialize(JavaPlugin plugin, DungeonManager dungeonManager,
                                 FunctionManager functionManager, TriggerManager triggerManager) {
        if (instance != null) {
            throw new IllegalStateException("API已经初始化！");
        }
        
        instance = new HuanDungeonAPI(plugin, dungeonManager, functionManager, triggerManager);
        logger.info("HuanDungeonRandom API 初始化完成");
    }
    
    /**
     * 获取API实例
     * 
     * @return API实例
     * @throws IllegalStateException 如果API未初始化
     */
    public static HuanDungeonAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("API未初始化！请确保HuanDungeonRandom插件已正确加载。");
        }
        return instance;
    }
    
    /**
     * 检查API是否可用
     * 
     * @return 是否可用
     */
    public static boolean isAvailable() {
        return instance != null;
    }
    
    /**
     * 关闭API
     */
    public static void shutdown() {
        if (instance != null) {
            logger.info("HuanDungeonRandom API 正在关闭...");
            instance = null;
            logger.info("HuanDungeonRandom API 已关闭");
        }
    }
    
    // ==================== 子API获取方法 ====================
    
    /**
     * 获取地牢管理API
     * 
     * @return 地牢API实例
     */
    public DungeonAPI getDungeonAPI() {
        return dungeonAPI;
    }
    
    /**
     * 获取功能系统API
     * 
     * @return 功能API实例
     */
    public FunctionAPI getFunctionAPI() {
        return functionAPI;
    }
    
    /**
     * 获取触发器系统API
     * 
     * @return 触发器API实例
     */
    public TriggerAPI getTriggerAPI() {
        return triggerAPI;
    }
    
    /**
     * 获取蓝图系统API
     * 
     * @return 蓝图API实例
     */
    public BlueprintAPI getBlueprintAPI() {
        return blueprintAPI;
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取玩家当前所在的地牢
     * 
     * @param player 玩家
     * @return 地牢实例，如果不在地牢中返回null
     */
    public DungeonInstance getPlayerDungeon(Player player) {
        return dungeonManager.getPlayerDungeon(player);
    }
    
    /**
     * 获取指定位置的地牢
     * 
     * @param location 位置
     * @return 地牢实例，如果位置不在地牢中返回null
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
    
    // ==================== 信息获取方法 ====================
    
    /**
     * 获取插件实例
     * 
     * @return 插件实例
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }
    
    /**
     * 获取API版本
     * 
     * @return API版本
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 获取插件名称
     * 
     * @return 插件名称
     */
    public String getPluginName() {
        return plugin.getName();
    }
    
    /**
     * 获取活跃地牢数量
     * 
     * @return 活跃地牢数量
     */
    public int getActiveDungeonCount() {
        return dungeonManager.getActiveDungeonCount();
    }
    
    /**
     * 获取API统计信息
     * 
     * @return 统计信息字符串
     */
    public String getAPIStats() {
        return String.format(
            "HuanDungeonRandom API v%s - 活跃地牢: %d, 注册功能: %d, 注册触发器: %d",
            version,
            getActiveDungeonCount(),
            functionManager.getRegisteredFunctionCount(),
            triggerManager.getRegisteredTriggerCount()
        );
    }
    
    // ==================== 内部方法 ====================
    
    /**
     * 获取地牢管理器（内部使用）
     * 
     * @return 地牢管理器
     */
    protected DungeonManager getDungeonManager() {
        return dungeonManager;
    }
    
    /**
     * 获取功能管理器（内部使用）
     * 
     * @return 功能管理器
     */
    protected FunctionManager getFunctionManager() {
        return functionManager;
    }
    
    /**
     * 获取触发器管理器（内部使用）
     * 
     * @return 触发器管理器
     */
    protected TriggerManager getTriggerManager() {
        return triggerManager;
    }
}
