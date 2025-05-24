package org.snhuan.huanDungeonRandom;

import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintManager;
import org.snhuan.huanDungeonRandom.config.ConfigManager;
import org.snhuan.huanDungeonRandom.function.FunctionManager;
import org.snhuan.huanDungeonRandom.generation.AsyncGenerator;
import org.snhuan.huanDungeonRandom.generation.DungeonGenerator;
import org.snhuan.huanDungeonRandom.generation.RandomGenerator;
import org.snhuan.huanDungeonRandom.utils.MessageUtils;

/**
 * HuanDungeonRandom 主插件类
 *
 * 负责插件的生命周期管理和核心组件的初始化
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public final class HuanDungeonRandom extends JavaPlugin {

    // 核心管理器
    private ConfigManager configManager;
    private BlueprintManager blueprintManager;
    private FunctionManager functionManager;
    private RandomGenerator randomGenerator;
    private DungeonGenerator dungeonGenerator;
    private AsyncGenerator asyncGenerator;

    // 插件实例
    private static HuanDungeonRandom instance;

    @Override
    public void onEnable() {
        // 设置插件实例
        instance = this;

        // 显示启动信息
        getLogger().info("=================================");
        getLogger().info("  HuanDungeonRandom 正在启动...");
        getLogger().info("  版本: " + getDescription().getVersion());
        getLogger().info("  作者: " + String.join(", ", getDescription().getAuthors()));
        getLogger().info("=================================");

        // 初始化核心组件
        if (!initializeComponents()) {
            getLogger().severe("核心组件初始化失败，插件将被禁用");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 注册命令和事件监听器
        registerCommandsAndListeners();

        // 显示启动完成信息
        getLogger().info("=================================");
        getLogger().info("  HuanDungeonRandom 启动完成!");
        getLogger().info("  蓝图数量: " + blueprintManager.getTotalBlueprintCount());
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("=================================");
        getLogger().info("  HuanDungeonRandom 正在关闭...");
        getLogger().info("=================================");

        // 关闭核心组件
        shutdownComponents();

        // 清理实例引用
        instance = null;

        getLogger().info("HuanDungeonRandom 已安全关闭");
    }

    /**
     * 初始化核心组件
     *
     * @return 是否初始化成功
     */
    private boolean initializeComponents() {
        try {
            // 初始化配置管理器
            getLogger().info("正在初始化配置管理器...");
            configManager = new ConfigManager(this);
            configManager.initialize();
            getLogger().info("配置管理器初始化完成");

            // 初始化蓝图管理器
            getLogger().info("正在初始化蓝图管理器...");
            blueprintManager = new BlueprintManager(this);
            if (!blueprintManager.initialize()) {
                getLogger().severe("蓝图管理器初始化失败");
                return false;
            }
            getLogger().info("蓝图管理器初始化完成");

            // 初始化功能管理器
            getLogger().info("正在初始化功能管理器...");
            functionManager = new FunctionManager(this);
            if (!functionManager.initialize()) {
                getLogger().severe("功能管理器初始化失败");
                return false;
            }
            getLogger().info("功能管理器初始化完成");

            // 初始化随机生成器
            getLogger().info("正在初始化随机生成器...");
            randomGenerator = new RandomGenerator();
            getLogger().info("随机生成器初始化完成");

            // 初始化地牢生成器
            getLogger().info("正在初始化地牢生成器...");
            dungeonGenerator = new DungeonGenerator(blueprintManager, randomGenerator, getLogger());
            getLogger().info("地牢生成器初始化完成");

            // 初始化异步生成器
            getLogger().info("正在初始化异步生成器...");
            asyncGenerator = new AsyncGenerator(this, dungeonGenerator, blueprintManager);
            getLogger().info("异步生成器初始化完成");

            return true;

        } catch (Exception e) {
            getLogger().severe("初始化组件时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册命令和事件监听器
     */
    private void registerCommandsAndListeners() {
        getLogger().info("正在注册命令和事件监听器...");

        // TODO: 注册命令
        // getCommand("dungeon").setExecutor(new DungeonCommand(this));

        // TODO: 注册事件监听器
        // getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("命令和事件监听器注册完成");
    }

    /**
     * 关闭核心组件
     */
    private void shutdownComponents() {
        try {
            // 关闭异步生成器
            if (asyncGenerator != null) {
                getLogger().info("正在关闭异步生成器...");
                asyncGenerator.shutdown();
                asyncGenerator = null;
                getLogger().info("异步生成器已关闭");
            }

            // 关闭功能管理器
            if (functionManager != null) {
                getLogger().info("正在关闭功能管理器...");
                functionManager.shutdown();
                functionManager = null;
                getLogger().info("功能管理器已关闭");
            }

            // 关闭蓝图管理器
            if (blueprintManager != null) {
                getLogger().info("正在关闭蓝图管理器...");
                blueprintManager.shutdown();
                blueprintManager = null;
                getLogger().info("蓝图管理器已关闭");
            }

            // 关闭配置管理器
            if (configManager != null) {
                getLogger().info("正在关闭配置管理器...");
                configManager.shutdown();
                configManager = null;
                getLogger().info("配置管理器已关闭");
            }

        } catch (Exception e) {
            getLogger().severe("关闭组件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 公共访问方法 ====================

    /**
     * 获取插件实例
     *
     * @return 插件实例
     */
    public static HuanDungeonRandom getInstance() {
        return instance;
    }

    /**
     * 获取配置管理器
     *
     * @return 配置管理器实例
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取蓝图管理器
     *
     * @return 蓝图管理器实例
     */
    public BlueprintManager getBlueprintManager() {
        return blueprintManager;
    }

    /**
     * 获取功能管理器
     *
     * @return 功能管理器实例
     */
    public FunctionManager getFunctionManager() {
        return functionManager;
    }

    /**
     * 获取随机生成器
     *
     * @return 随机生成器实例
     */
    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    /**
     * 获取地牢生成器
     *
     * @return 地牢生成器实例
     */
    public DungeonGenerator getDungeonGenerator() {
        return dungeonGenerator;
    }

    /**
     * 获取异步生成器
     *
     * @return 异步生成器实例
     */
    public AsyncGenerator getAsyncGenerator() {
        return asyncGenerator;
    }

    /**
     * 重新加载插件配置
     *
     * @return 是否重新加载成功
     */
    public boolean reloadPluginConfig() {
        try {
            getLogger().info("正在重新加载插件配置...");

            // 重新加载配置
            if (configManager != null) {
                configManager.reloadConfigs();
            }

            // 重新加载蓝图
            if (blueprintManager != null) {
                int reloadedCount = blueprintManager.reloadAllBlueprints();
                getLogger().info("重新加载了 " + reloadedCount + " 个蓝图");
            }

            getLogger().info("插件配置重新加载完成");
            return true;

        } catch (Exception e) {
            getLogger().severe("重新加载配置时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取插件状态信息
     *
     * @return 格式化的状态信息
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HuanDungeonRandom 状态信息 ===\n");
        sb.append("插件版本: ").append(getDescription().getVersion()).append("\n");
        sb.append("服务器版本: ").append(getServer().getVersion()).append("\n");
        sb.append("Java版本: ").append(System.getProperty("java.version")).append("\n");

        if (configManager != null) {
            sb.append("\n").append(configManager.getStatusInfo());
        }

        if (blueprintManager != null) {
            sb.append("\n").append(blueprintManager.getStatusInfo());
        }

        if (functionManager != null) {
            sb.append("\n").append(functionManager.getStatusInfo());
        }

        if (asyncGenerator != null) {
            sb.append("\n").append(asyncGenerator.getStatusInfo());
        }

        return sb.toString();
    }
}
