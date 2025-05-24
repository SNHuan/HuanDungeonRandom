package org.snhuan.huanDungeonRandom.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * 配置管理器 - 负责管理插件的所有配置文件
 *
 * 功能包括：
 * - 主配置文件管理
 * - 蓝图配置管理
 * - 地牢主题配置管理
 * - 配置文件的加载、保存和重载
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Logger logger;

    // 配置文件
    private FileConfiguration mainConfig;
    private File mainConfigFile;

    // 配置目录
    private File blueprintsDir;
    private File themesDir;
    private File dataDir;

    // 默认配置值
    private static final int DEFAULT_MAX_INSTANCES = 10;
    private static final int DEFAULT_CLEANUP_INTERVAL = 300;
    private static final int DEFAULT_MAX_TEAM_SIZE = 6;
    private static final int DEFAULT_MIN_TEAM_SIZE = 1;
    private static final int DEFAULT_INVITE_TIMEOUT = 60;
    private static final boolean DEFAULT_DEBUG = false;
    private static final String DEFAULT_LANGUAGE = "zh_CN";

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     */
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * 初始化配置管理器
     */
    public void initialize() {
        initializeDirectories();
        loadMainConfig();
        logger.info("配置管理器初始化完成");
    }

    /**
     * 关闭配置管理器
     */
    public void shutdown() {
        saveMainConfig();
        logger.info("配置管理器已关闭");
    }

    /**
     * 初始化配置目录
     */
    private void initializeDirectories() {
        // 创建主数据目录
        dataDir = plugin.getDataFolder();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // 创建蓝图目录
        blueprintsDir = new File(dataDir, "blueprints");
        if (!blueprintsDir.exists()) {
            blueprintsDir.mkdirs();
        }

        // 创建主题目录
        themesDir = new File(dataDir, "themes");
        if (!themesDir.exists()) {
            themesDir.mkdirs();
        }

        logger.info("配置目录初始化完成");
    }

    /**
     * 加载主配置文件
     */
    private void loadMainConfig() {
        mainConfigFile = new File(dataDir, "config.yml");

        // 如果配置文件不存在，创建默认配置
        if (!mainConfigFile.exists()) {
            createDefaultConfig();
        }

        mainConfig = YamlConfiguration.loadConfiguration(mainConfigFile);

        // 验证配置完整性
        validateConfig();

        logger.info("主配置文件加载完成");
    }

    /**
     * 创建默认配置文件
     */
    private void createDefaultConfig() {
        try {
            mainConfigFile.createNewFile();

            FileConfiguration defaultConfig = new YamlConfiguration();

            // 插件基础配置
            defaultConfig.set("plugin.debug", DEFAULT_DEBUG);
            defaultConfig.set("plugin.language", DEFAULT_LANGUAGE);
            defaultConfig.set("plugin.version", plugin.getDescription().getVersion());

            // 地牢配置
            defaultConfig.set("dungeons.max-instances", DEFAULT_MAX_INSTANCES);
            defaultConfig.set("dungeons.cleanup-interval", DEFAULT_CLEANUP_INTERVAL);
            defaultConfig.set("dungeons.auto-save", true);
            defaultConfig.set("dungeons.generation-timeout", 30);

            // 队伍配置
            defaultConfig.set("teams.max-size", DEFAULT_MAX_TEAM_SIZE);
            defaultConfig.set("teams.min-size", DEFAULT_MIN_TEAM_SIZE);
            defaultConfig.set("teams.invite-timeout", DEFAULT_INVITE_TIMEOUT);
            defaultConfig.set("teams.auto-disband", true);

            // 编辑器配置
            defaultConfig.set("editor.wand-item", "BLAZE_ROD");
            defaultConfig.set("editor.auto-save", true);
            defaultConfig.set("editor.undo-limit", 50);

            // 性能配置
            defaultConfig.set("performance.async-generation", true);
            defaultConfig.set("performance.cache-size", 100);
            defaultConfig.set("performance.max-concurrent-generations", 5);

            defaultConfig.save(mainConfigFile);

            logger.info("默认配置文件创建完成");

        } catch (IOException e) {
            logger.severe("创建默认配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 验证配置文件完整性
     */
    private void validateConfig() {
        boolean needsSave = false;

        // 检查必要的配置项是否存在，不存在则添加默认值
        if (!mainConfig.contains("plugin.debug")) {
            mainConfig.set("plugin.debug", DEFAULT_DEBUG);
            needsSave = true;
        }

        if (!mainConfig.contains("dungeons.max-instances")) {
            mainConfig.set("dungeons.max-instances", DEFAULT_MAX_INSTANCES);
            needsSave = true;
        }

        if (!mainConfig.contains("teams.max-size")) {
            mainConfig.set("teams.max-size", DEFAULT_MAX_TEAM_SIZE);
            needsSave = true;
        }

        // 如果有缺失的配置项，保存文件
        if (needsSave) {
            saveMainConfig();
            logger.info("配置文件已更新缺失的配置项");
        }
    }

    /**
     * 保存主配置文件
     */
    public void saveMainConfig() {
        try {
            mainConfig.save(mainConfigFile);
        } catch (IOException e) {
            logger.severe("保存主配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 重载所有配置文件
     */
    public void reloadConfigs() {
        loadMainConfig();
        logger.info("配置文件重载完成");
    }

    // ==================== 配置获取方法 ====================

    /**
     * 获取调试模式状态
     */
    public boolean isDebugMode() {
        return mainConfig.getBoolean("plugin.debug", DEFAULT_DEBUG);
    }

    /**
     * 获取语言设置
     */
    public String getLanguage() {
        return mainConfig.getString("plugin.language", DEFAULT_LANGUAGE);
    }

    /**
     * 获取最大地牢实例数
     */
    public int getMaxInstances() {
        return mainConfig.getInt("dungeons.max-instances", DEFAULT_MAX_INSTANCES);
    }

    /**
     * 获取清理间隔（秒）
     */
    public int getCleanupInterval() {
        return mainConfig.getInt("dungeons.cleanup-interval", DEFAULT_CLEANUP_INTERVAL);
    }

    /**
     * 获取队伍最大人数
     */
    public int getMaxTeamSize() {
        return mainConfig.getInt("teams.max-size", DEFAULT_MAX_TEAM_SIZE);
    }

    /**
     * 获取队伍最小人数
     */
    public int getMinTeamSize() {
        return mainConfig.getInt("teams.min-size", DEFAULT_MIN_TEAM_SIZE);
    }

    /**
     * 获取邀请超时时间（秒）
     */
    public int getInviteTimeout() {
        return mainConfig.getInt("teams.invite-timeout", DEFAULT_INVITE_TIMEOUT);
    }

    /**
     * 获取编辑魔杖物品类型
     */
    public String getWandItem() {
        return mainConfig.getString("editor.wand-item", "BLAZE_ROD");
    }

    /**
     * 是否启用异步生成
     */
    public boolean isAsyncGeneration() {
        return mainConfig.getBoolean("performance.async-generation", true);
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return mainConfig.getInt("performance.cache-size", 100);
    }

    /**
     * 获取最大并发生成数
     */
    public int getMaxConcurrentGenerations() {
        return mainConfig.getInt("performance.max-concurrent-generations", 5);
    }

    // ==================== 目录获取方法 ====================

    /**
     * 获取蓝图目录
     */
    public File getBlueprintsDirectory() {
        return blueprintsDir;
    }

    /**
     * 获取主题目录
     */
    public File getThemesDirectory() {
        return themesDir;
    }

    /**
     * 获取数据目录
     */
    public File getDataDirectory() {
        return dataDir;
    }

    /**
     * 获取主配置文件
     */
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    /**
     * 获取状态信息
     */
    public String getStatusInfo() {
        StringBuilder status = new StringBuilder();
        status.append("配置管理器状态:\n");
        status.append("- 调试模式: ").append(isDebugMode()).append("\n");
        status.append("- 语言: ").append(getLanguage()).append("\n");
        status.append("- 最大地牢实例: ").append(getMaxInstances()).append("\n");
        status.append("- 最大队伍人数: ").append(getMaxTeamSize()).append("\n");
        status.append("- 异步生成: ").append(isAsyncGeneration()).append("\n");
        status.append("- 缓存大小: ").append(getCacheSize()).append("\n");
        return status.toString();
    }
}
