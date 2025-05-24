package org.snhuan.huanDungeonRandom.api.examples;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.api.HuanDungeonAPI;
import org.snhuan.huanDungeonRandom.api.events.DungeonCreateEvent;
import org.snhuan.huanDungeonRandom.api.events.PlayerEnterDungeonEvent;
import org.snhuan.huanDungeonRandom.api.function.FunctionAPI;
import org.snhuan.huanDungeonRandom.api.trigger.TriggerAPI;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.function.ExecutionResult;

/**
 * HuanDungeonRandom API 使用示例
 * 
 * 展示如何在其他插件中使用HuanDungeonRandom的API：
 * - 基础API使用
 * - 地牢管理
 * - 功能执行
 * - 触发器操作
 * - 事件监听
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class APIExamples extends JavaPlugin implements Listener {
    
    private HuanDungeonAPI api;
    
    @Override
    public void onEnable() {
        // 等待HuanDungeonRandom插件加载
        if (!HuanDungeonAPI.isAvailable()) {
            getLogger().warning("HuanDungeonRandom API 不可用，请确保插件已正确加载");
            return;
        }
        
        // 获取API实例
        api = HuanDungeonAPI.getInstance();
        getLogger().info("成功连接到 HuanDungeonRandom API v" + api.getVersion());
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        
        // 运行示例
        runExamples();
    }
    
    /**
     * 运行API使用示例
     */
    private void runExamples() {
        // 基础API使用示例
        basicAPIUsage();
        
        // 地牢管理示例
        dungeonManagementExample();
        
        // 功能系统示例
        functionSystemExample();
        
        // 触发器系统示例
        triggerSystemExample();
    }
    
    /**
     * 基础API使用示例
     */
    private void basicAPIUsage() {
        getLogger().info("=== 基础API使用示例 ===");
        
        // 获取API信息
        getLogger().info("插件名称: " + api.getPluginName());
        getLogger().info("API版本: " + api.getVersion());
        getLogger().info("活跃地牢数量: " + api.getActiveDungeonCount());
        getLogger().info("API统计: " + api.getAPIStats());
        
        // 检查玩家状态（需要有在线玩家）
        for (Player player : getServer().getOnlinePlayers()) {
            boolean inDungeon = api.isPlayerInDungeon(player);
            getLogger().info("玩家 " + player.getName() + " 是否在地牢中: " + inDungeon);
            
            if (inDungeon) {
                DungeonInstance dungeon = api.getPlayerDungeon(player);
                getLogger().info("玩家所在地牢: " + dungeon.getInstanceId());
            }
            break; // 只检查第一个玩家
        }
    }
    
    /**
     * 地牢管理示例
     */
    private void dungeonManagementExample() {
        getLogger().info("=== 地牢管理示例 ===");
        
        // 获取地牢API
        var dungeonAPI = api.getDungeonAPI();
        
        // 列出所有地牢
        var allDungeons = dungeonAPI.getAllDungeons();
        getLogger().info("当前活跃地牢数量: " + allDungeons.size());
        
        for (DungeonInstance dungeon : allDungeons) {
            getLogger().info("地牢: " + dungeon.getInstanceId() + 
                           ", 状态: " + dungeon.getState().getCurrentState().getDisplayName() +
                           ", 玩家数量: " + dungeon.getState().getPlayerCount());
        }
        
        // 创建测试地牢（需要有在线玩家）
        Player firstPlayer = getServer().getOnlinePlayers().stream().findFirst().orElse(null);
        if (firstPlayer != null) {
            try {
                DungeonTheme theme = DungeonTheme.builder("test_theme", "测试主题")
                    .setDescription("API示例创建的测试主题")
                    .setStoneTheme()
                    .build();
                
                Location location = firstPlayer.getLocation();
                DungeonInstance newDungeon = dungeonAPI.createDungeon(
                    "api_test", theme, location, firstPlayer.getUniqueId());
                
                if (newDungeon != null) {
                    getLogger().info("成功创建测试地牢: " + newDungeon.getInstanceId());
                    
                    // 5秒后销毁测试地牢
                    getServer().getScheduler().runTaskLater(this, () -> {
                        if (dungeonAPI.destroyDungeon(newDungeon.getInstanceId())) {
                            getLogger().info("成功销毁测试地牢: " + newDungeon.getInstanceId());
                        }
                    }, 100L); // 5秒 = 100 ticks
                }
            } catch (Exception e) {
                getLogger().warning("创建测试地牢失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 功能系统示例
     */
    private void functionSystemExample() {
        getLogger().info("=== 功能系统示例 ===");
        
        // 获取功能API
        FunctionAPI functionAPI = api.getFunctionAPI();
        
        // 列出所有功能
        var allFunctions = functionAPI.getAllFunctions();
        getLogger().info("已注册功能数量: " + allFunctions.size());
        getLogger().info("功能统计: " + functionAPI.getFunctionStats());
        
        // 执行功能示例（如果有可用的功能）
        if (!allFunctions.isEmpty()) {
            var firstFunction = allFunctions.iterator().next();
            getLogger().info("测试执行功能: " + firstFunction.getId());
            
            Player firstPlayer = getServer().getOnlinePlayers().stream().findFirst().orElse(null);
            if (firstPlayer != null) {
                ExecutionContext context = ExecutionContext.builder()
                    .setTriggerPlayer(firstPlayer)
                    .setTriggerLocation(firstPlayer.getLocation())
                    .build();
                
                // 异步执行功能
                functionAPI.executeAsync(firstFunction.getId(), context, result -> {
                    if (result.isSuccess()) {
                        getLogger().info("功能执行成功: " + result.getMessage());
                    } else {
                        getLogger().warning("功能执行失败: " + result.getMessage());
                    }
                });
            }
        }
    }
    
    /**
     * 触发器系统示例
     */
    private void triggerSystemExample() {
        getLogger().info("=== 触发器系统示例 ===");
        
        // 获取触发器API
        TriggerAPI triggerAPI = api.getTriggerAPI();
        
        // 列出所有触发器
        var allTriggers = triggerAPI.getAllTriggers();
        getLogger().info("已注册触发器数量: " + allTriggers.size());
        getLogger().info("触发器统计: " + triggerAPI.getTriggerStats());
        
        // 手动触发示例（如果有可用的触发器和地牢）
        if (!allTriggers.isEmpty()) {
            var firstTrigger = allTriggers.iterator().next();
            var allDungeons = api.getDungeonAPI().getAllDungeons();
            
            if (!allDungeons.isEmpty()) {
                var firstDungeon = allDungeons.iterator().next();
                Player firstPlayer = getServer().getOnlinePlayers().stream().findFirst().orElse(null);
                
                if (firstPlayer != null) {
                    getLogger().info("测试手动触发: " + firstTrigger.getId());
                    
                    var results = triggerAPI.manualTrigger(
                        firstTrigger.getId(), firstPlayer, firstDungeon);
                    
                    getLogger().info("触发结果数量: " + results.size());
                    for (var result : results) {
                        getLogger().info("触发结果: " + result.getMessage());
                    }
                }
            }
        }
    }
    
    // ==================== 事件监听示例 ====================
    
    /**
     * 监听地牢创建事件
     */
    @EventHandler
    public void onDungeonCreate(DungeonCreateEvent.Post event) {
        DungeonInstance dungeon = event.getDungeon();
        getLogger().info("检测到地牢创建: " + dungeon.getInstanceId());
        
        // 可以在这里添加自定义逻辑
        // 例如：给地牢添加特殊效果、记录统计信息等
    }
    
    /**
     * 监听玩家进入地牢事件
     */
    @EventHandler
    public void onPlayerEnterDungeon(PlayerEnterDungeonEvent.Post event) {
        Player player = event.getPlayer();
        DungeonInstance dungeon = event.getDungeon();
        
        getLogger().info("玩家 " + player.getName() + " 进入地牢: " + dungeon.getInstanceId());
        
        // 可以在这里添加自定义逻辑
        // 例如：发送欢迎消息、给予特殊物品等
        player.sendMessage("§a欢迎进入地牢: " + dungeon.getInstanceId());
    }
    
    /**
     * 监听地牢创建前事件（可以取消）
     */
    @EventHandler
    public void onDungeonCreatePre(DungeonCreateEvent.Pre event) {
        // 示例：阻止在某些世界创建地牢
        if (event.getLocation().getWorld().getName().equals("spawn")) {
            event.setCancelled(true);
            getLogger().info("阻止在spawn世界创建地牢");
        }
    }
}
