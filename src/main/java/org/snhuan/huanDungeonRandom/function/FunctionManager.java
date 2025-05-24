package org.snhuan.huanDungeonRandom.function;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 功能管理器 - 管理地牢中的所有功能
 *
 * 功能包括：
 * - 功能的注册和注销
 * - 功能的执行和调度
 * - 功能配置的加载和保存
 * - 功能状态的监控
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class FunctionManager {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final FunctionFactory functionFactory;

    // 功能存储
    private final Map<String, Function> functions;
    private final Map<String, Set<String>> dungeonFunctions; // 地牢ID -> 功能ID集合
    private final Map<Location, Set<String>> locationFunctions; // 位置 -> 功能ID集合

    // 配置文件
    private File functionsDir;
    private File globalFunctionsFile;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     */
    public FunctionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.functionFactory = new FunctionFactory(logger);

        this.functions = new ConcurrentHashMap<>();
        this.dungeonFunctions = new ConcurrentHashMap<>();
        this.locationFunctions = new ConcurrentHashMap<>();

        initializeDirectories();
    }

    /**
     * 初始化目录结构
     */
    private void initializeDirectories() {
        functionsDir = new File(plugin.getDataFolder(), "functions");
        if (!functionsDir.exists()) {
            functionsDir.mkdirs();
        }

        globalFunctionsFile = new File(functionsDir, "global.yml");
    }

    /**
     * 初始化功能管理器
     *
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            logger.info("正在初始化功能管理器...");

            // 加载全局功能配置
            loadGlobalFunctions();

            logger.info("功能管理器初始化完成，已加载 " + functions.size() + " 个功能");
            return true;

        } catch (Exception e) {
            logger.severe("功能管理器初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭功能管理器
     */
    public void shutdown() {
        logger.info("正在关闭功能管理器...");

        try {
            // 保存所有功能配置
            saveAllFunctions();

            // 清理资源
            functions.clear();
            dungeonFunctions.clear();
            locationFunctions.clear();

            logger.info("功能管理器已关闭");

        } catch (Exception e) {
            logger.severe("关闭功能管理器时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 注册功能
     *
     * @param function 功能实例
     * @return 是否注册成功
     */
    public boolean registerFunction(Function function) {
        if (function == null) {
            logger.warning("尝试注册空的功能实例");
            return false;
        }

        String id = function.getId();
        if (functions.containsKey(id)) {
            logger.warning("功能ID已存在: " + id);
            return false;
        }

        functions.put(id, function);

        // 如果功能有位置信息，添加到位置索引
        if (function.getLocation() != null) {
            addLocationFunction(function.getLocation(), id);
        }

        logger.info("成功注册功能: " + id + " (类型: " + function.getType().getDisplayName() + ")");
        return true;
    }

    /**
     * 注销功能
     *
     * @param functionId 功能ID
     * @return 是否注销成功
     */
    public boolean unregisterFunction(String functionId) {
        Function function = functions.remove(functionId);
        if (function == null) {
            logger.warning("尝试注销不存在的功能: " + functionId);
            return false;
        }

        // 从位置索引中移除
        if (function.getLocation() != null) {
            removeLocationFunction(function.getLocation(), functionId);
        }

        // 从地牢索引中移除
        dungeonFunctions.values().forEach(set -> set.remove(functionId));

        logger.info("成功注销功能: " + functionId);
        return true;
    }

    /**
     * 执行功能
     *
     * @param functionId 功能ID
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 执行结果
     */
    public ExecutionResult executeFunction(String functionId, Player player,
                                         DungeonInstance dungeonInstance, ExecutionContext context) {
        Function function = functions.get(functionId);
        if (function == null) {
            return ExecutionResult.failure("功能不存在: " + functionId);
        }

        try {
            return function.execute(player, dungeonInstance, context);
        } catch (Exception e) {
            logger.severe("执行功能时发生异常: " + functionId + " - " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.failure("功能执行异常: " + e.getMessage());
        }
    }

    /**
     * 执行功能（简化版本）
     *
     * @param functionId 功能ID
     * @param context 执行上下文
     * @return 执行结果
     */
    public ExecutionResult executeFunction(String functionId, ExecutionContext context) {
        return executeFunction(functionId, context.getTriggerPlayer(), context.getDungeonInstance(), context);
    }

    /**
     * 根据位置获取附近的功能
     *
     * @param location 位置
     * @param radius 搜索半径
     * @return 功能列表
     */
    public List<Function> getFunctionsNearLocation(Location location, double radius) {
        List<Function> nearbyFunctions = new ArrayList<>();

        for (Function function : functions.values()) {
            Location funcLocation = function.getLocation();
            if (funcLocation != null &&
                funcLocation.getWorld().equals(location.getWorld()) &&
                funcLocation.distance(location) <= radius) {
                nearbyFunctions.add(function);
            }
        }

        // 按距离排序
        nearbyFunctions.sort((f1, f2) -> {
            double dist1 = f1.getLocation().distance(location);
            double dist2 = f2.getLocation().distance(location);
            return Double.compare(dist1, dist2);
        });

        return nearbyFunctions;
    }

    /**
     * 获取地牢中的所有功能
     *
     * @param dungeonId 地牢ID
     * @return 功能列表
     */
    public List<Function> getDungeonFunctions(String dungeonId) {
        Set<String> functionIds = dungeonFunctions.get(dungeonId);
        if (functionIds == null) {
            return new ArrayList<>();
        }

        List<Function> dungeonFuncs = new ArrayList<>();
        for (String functionId : functionIds) {
            Function function = functions.get(functionId);
            if (function != null) {
                dungeonFuncs.add(function);
            }
        }

        return dungeonFuncs;
    }

    /**
     * 将功能绑定到地牢
     *
     * @param dungeonId 地牢ID
     * @param functionId 功能ID
     */
    public void bindFunctionToDungeon(String dungeonId, String functionId) {
        dungeonFunctions.computeIfAbsent(dungeonId, k -> new HashSet<>()).add(functionId);
    }

    /**
     * 从地牢解绑功能
     *
     * @param dungeonId 地牢ID
     * @param functionId 功能ID
     */
    public void unbindFunctionFromDungeon(String dungeonId, String functionId) {
        Set<String> functionIds = dungeonFunctions.get(dungeonId);
        if (functionIds != null) {
            functionIds.remove(functionId);
            if (functionIds.isEmpty()) {
                dungeonFunctions.remove(dungeonId);
            }
        }
    }

    /**
     * 加载全局功能配置
     */
    private void loadGlobalFunctions() {
        if (!globalFunctionsFile.exists()) {
            createDefaultGlobalFunctions();
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(globalFunctionsFile);
            ConfigurationSection functionsSection = config.getConfigurationSection("functions");

            if (functionsSection != null) {
                for (String functionId : functionsSection.getKeys(false)) {
                    ConfigurationSection functionConfig = functionsSection.getConfigurationSection(functionId);
                    if (functionConfig != null) {
                        Function function = functionFactory.createFunctionFromConfig(functionId, functionConfig);
                        if (function != null) {
                            registerFunction(function);
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.severe("加载全局功能配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建默认的全局功能配置
     */
    private void createDefaultGlobalFunctions() {
        try {
            FileConfiguration config = new YamlConfiguration();

            // 创建示例MythicMobs技能功能
            ConfigurationSection functionsSection = config.createSection("functions");

            ConfigurationSection exampleSkill = functionsSection.createSection("example_heal_skill");
            exampleSkill.set("type", "MYTHIC_SKILL");
            exampleSkill.set("name", "示例治疗技能");
            exampleSkill.set("description", "这是一个示例的MythicMobs治疗技能");
            exampleSkill.set("enabled", true);
            exampleSkill.set("skill-name", "HealPlayer");
            exampleSkill.set("skill-power", 1.0);
            exampleSkill.set("target-type", "TRIGGER_PLAYER");
            exampleSkill.set("cooldown", 5000);

            config.save(globalFunctionsFile);
            logger.info("已创建默认的全局功能配置文件");

        } catch (IOException e) {
            logger.severe("创建默认全局功能配置失败: " + e.getMessage());
        }
    }

    /**
     * 保存所有功能配置
     */
    private void saveAllFunctions() {
        try {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection functionsSection = config.createSection("functions");

            for (Function function : functions.values()) {
                ConfigurationSection functionSection = functionsSection.createSection(function.getId());
                function.saveToConfig(functionSection);
            }

            config.save(globalFunctionsFile);
            logger.info("已保存所有功能配置");

        } catch (IOException e) {
            logger.severe("保存功能配置失败: " + e.getMessage());
        }
    }

    /**
     * 添加位置功能索引
     */
    private void addLocationFunction(Location location, String functionId) {
        // 简化位置键（忽略小数部分）
        Location key = new Location(location.getWorld(),
                                  Math.floor(location.getX()),
                                  Math.floor(location.getY()),
                                  Math.floor(location.getZ()));
        locationFunctions.computeIfAbsent(key, k -> new HashSet<>()).add(functionId);
    }

    /**
     * 移除位置功能索引
     */
    private void removeLocationFunction(Location location, String functionId) {
        Location key = new Location(location.getWorld(),
                                  Math.floor(location.getX()),
                                  Math.floor(location.getY()),
                                  Math.floor(location.getZ()));
        Set<String> functionIds = locationFunctions.get(key);
        if (functionIds != null) {
            functionIds.remove(functionId);
            if (functionIds.isEmpty()) {
                locationFunctions.remove(key);
            }
        }
    }

    // ==================== Getter 方法 ====================

    public Function getFunction(String functionId) {
        return functions.get(functionId);
    }

    public Collection<Function> getAllFunctions() {
        return new ArrayList<>(functions.values());
    }

    public FunctionFactory getFunctionFactory() {
        return functionFactory;
    }

    public int getFunctionCount() {
        return functions.size();
    }

    public int getRegisteredFunctionCount() {
        return functions.size();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public String getStatusInfo() {
        StringBuilder status = new StringBuilder();
        status.append("功能管理器状态:\n");
        status.append("- 已注册功能数: ").append(functions.size()).append("\n");
        status.append("- 地牢绑定数: ").append(dungeonFunctions.size()).append("\n");
        status.append("- 位置索引数: ").append(locationFunctions.size()).append("\n");

        // 按类型统计功能数量
        Map<FunctionType, Integer> typeCount = new HashMap<>();
        for (Function function : functions.values()) {
            typeCount.merge(function.getType(), 1, Integer::sum);
        }

        if (!typeCount.isEmpty()) {
            status.append("- 功能类型分布:\n");
            for (Map.Entry<FunctionType, Integer> entry : typeCount.entrySet()) {
                status.append("  - ").append(entry.getKey().getDisplayName())
                      .append(": ").append(entry.getValue()).append("\n");
            }
        }

        return status.toString();
    }
}
