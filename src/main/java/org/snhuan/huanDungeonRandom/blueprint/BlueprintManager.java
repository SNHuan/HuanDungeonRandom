package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.plugin.Plugin;
import org.snhuan.huanDungeonRandom.utils.FileUtils;
import org.snhuan.huanDungeonRandom.utils.MessageUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 蓝图管理器 - 负责蓝图的加载、保存、缓存和管理
 *
 * 核心功能：
 * - 蓝图的注册和注销
 * - 蓝图文件的加载和保存
 * - 蓝图缓存管理
 * - 蓝图验证和预览
 * - 按类型和分类查询蓝图
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class BlueprintManager {

    private final Plugin plugin;
    private final Logger logger;
    private final File blueprintsFolder;

    // 蓝图存储 - 按ID索引
    private final Map<String, Blueprint> blueprints;

    // 按类型分类的蓝图索引
    private final Map<BlueprintType, Set<String>> blueprintsByType;

    // 按分类分组的蓝图索引
    private final Map<String, Set<String>> blueprintsByCategory;

    // 蓝图缓存系统
    private final BlueprintCache cache;

    // 管理器状态
    private boolean initialized;
    private long lastLoadTime;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     */
    public BlueprintManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.blueprintsFolder = new File(plugin.getDataFolder(), "blueprints");

        this.blueprints = new ConcurrentHashMap<>();
        this.blueprintsByType = new ConcurrentHashMap<>();
        this.blueprintsByCategory = new ConcurrentHashMap<>();
        this.cache = new BlueprintCache(plugin);

        this.initialized = false;
        this.lastLoadTime = 0;

        // 初始化类型索引
        for (BlueprintType type : BlueprintType.values()) {
            blueprintsByType.put(type, ConcurrentHashMap.newKeySet());
        }
    }

    /**
     * 初始化管理器
     *
     * @return 是否初始化成功
     */
    public boolean initialize() {
        if (initialized) {
            logger.warning("蓝图管理器已经初始化");
            return true;
        }

        try {
            // 创建蓝图文件夹
            if (!createBlueprintsFolder()) {
                return false;
            }

            // 初始化缓存系统
            if (!cache.initialize()) {
                logger.severe("蓝图缓存系统初始化失败");
                return false;
            }

            // 加载所有蓝图
            int loadedCount = loadAllBlueprints();
            logger.info("蓝图管理器初始化完成，加载了 " + loadedCount + " 个蓝图");

            this.initialized = true;
            this.lastLoadTime = System.currentTimeMillis();

            return true;

        } catch (Exception e) {
            logger.severe("蓝图管理器初始化失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }

        try {
            // 保存所有修改的蓝图
            saveAllModifiedBlueprints();

            // 关闭缓存系统
            cache.shutdown();

            // 清理数据
            blueprints.clear();
            blueprintsByType.clear();
            blueprintsByCategory.clear();

            this.initialized = false;
            logger.info("蓝图管理器已关闭");

        } catch (Exception e) {
            logger.severe("蓝图管理器关闭时发生错误: " + e.getMessage());
        }
    }

    /**
     * 创建蓝图文件夹
     *
     * @return 是否创建成功
     */
    private boolean createBlueprintsFolder() {
        if (!blueprintsFolder.exists()) {
            if (!blueprintsFolder.mkdirs()) {
                logger.severe("无法创建蓝图文件夹: " + blueprintsFolder.getPath());
                return false;
            }
        }

        // 创建子文件夹
        for (BlueprintType type : BlueprintType.values()) {
            File typeFolder = new File(blueprintsFolder, type.name().toLowerCase());
            if (!typeFolder.exists() && !typeFolder.mkdirs()) {
                logger.warning("无法创建蓝图类型文件夹: " + typeFolder.getPath());
            }
        }

        return true;
    }

    /**
     * 加载所有蓝图
     *
     * @return 加载的蓝图数量
     */
    private int loadAllBlueprints() {
        int loadedCount = 0;

        for (BlueprintType type : BlueprintType.values()) {
            File typeFolder = new File(blueprintsFolder, type.name().toLowerCase());
            if (!typeFolder.exists()) {
                continue;
            }

            File[] blueprintFiles = typeFolder.listFiles((dir, name) ->
                name.endsWith(".yml") || name.endsWith(".yaml"));

            if (blueprintFiles == null) {
                continue;
            }

            for (File file : blueprintFiles) {
                try {
                    Blueprint blueprint = loadBlueprintFromFile(file, type);
                    if (blueprint != null) {
                        registerBlueprint(blueprint);
                        loadedCount++;
                    }
                } catch (Exception e) {
                    logger.warning("加载蓝图文件失败: " + file.getName() + " - " + e.getMessage());
                }
            }
        }

        return loadedCount;
    }

    /**
     * 从文件加载蓝图
     *
     * @param file 蓝图文件
     * @param expectedType 期望的蓝图类型
     * @return 加载的蓝图，失败返回null
     */
    private Blueprint loadBlueprintFromFile(File file, BlueprintType expectedType) {
        // 这里需要根据具体的蓝图类型创建对应的实例
        // 暂时返回null，等具体蓝图类实现后再完善
        logger.info("正在加载蓝图文件: " + file.getName());
        return null;
    }

    /**
     * 注册蓝图
     *
     * @param blueprint 要注册的蓝图
     * @return 是否注册成功
     */
    public boolean registerBlueprint(Blueprint blueprint) {
        if (blueprint == null) {
            logger.warning("尝试注册空蓝图");
            return false;
        }

        String id = blueprint.getId();
        if (id == null || id.trim().isEmpty()) {
            logger.warning("蓝图ID不能为空");
            return false;
        }

        // 检查ID是否已存在
        if (blueprints.containsKey(id)) {
            logger.warning("蓝图ID已存在: " + id);
            return false;
        }

        // 验证蓝图
        ValidationResult validation = blueprint.validate();
        if (!validation.isValid()) {
            logger.warning("蓝图验证失败: " + id + " - " + validation.getMessage());
            return false;
        }

        // 注册蓝图
        blueprints.put(id, blueprint);

        // 更新类型索引
        blueprintsByType.get(blueprint.getType()).add(id);

        // 更新分类索引
        String category = blueprint.getCategory();
        if (category != null && !category.trim().isEmpty()) {
            blueprintsByCategory.computeIfAbsent(category, k -> ConcurrentHashMap.newKeySet()).add(id);
        }

        // 添加到缓存
        cache.put(blueprint);

        logger.info("成功注册蓝图: " + id + " (类型: " + blueprint.getType().getDisplayName() + ")");
        return true;
    }

    /**
     * 注销蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 是否注销成功
     */
    public boolean unregisterBlueprint(String blueprintId) {
        if (blueprintId == null || blueprintId.trim().isEmpty()) {
            return false;
        }

        Blueprint blueprint = blueprints.remove(blueprintId);
        if (blueprint == null) {
            return false;
        }

        // 从类型索引中移除
        blueprintsByType.get(blueprint.getType()).remove(blueprintId);

        // 从分类索引中移除
        String category = blueprint.getCategory();
        if (category != null) {
            Set<String> categorySet = blueprintsByCategory.get(category);
            if (categorySet != null) {
                categorySet.remove(blueprintId);
                if (categorySet.isEmpty()) {
                    blueprintsByCategory.remove(category);
                }
            }
        }

        // 从缓存中移除
        cache.remove(blueprintId);

        logger.info("成功注销蓝图: " + blueprintId);
        return true;
    }

    /**
     * 保存所有修改的蓝图
     */
    private void saveAllModifiedBlueprints() {
        int savedCount = 0;
        for (Blueprint blueprint : blueprints.values()) {
            try {
                File file = getBlueprintFile(blueprint);
                if (blueprint.save(file)) {
                    savedCount++;
                }
            } catch (Exception e) {
                logger.warning("保存蓝图失败: " + blueprint.getId() + " - " + e.getMessage());
            }
        }

        if (savedCount > 0) {
            logger.info("保存了 " + savedCount + " 个蓝图");
        }
    }

    /**
     * 获取蓝图文件路径
     *
     * @param blueprint 蓝图
     * @return 文件对象
     */
    private File getBlueprintFile(Blueprint blueprint) {
        File typeFolder = new File(blueprintsFolder, blueprint.getType().name().toLowerCase());
        return new File(typeFolder, blueprint.getId() + ".yml");
    }

    // ==================== 查询方法 ====================

    /**
     * 根据ID获取蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 蓝图实例，不存在返回null
     */
    public Blueprint getBlueprint(String blueprintId) {
        if (blueprintId == null || blueprintId.trim().isEmpty()) {
            return null;
        }

        // 先从缓存查找
        Blueprint cached = cache.get(blueprintId);
        if (cached != null) {
            return cached;
        }

        // 从内存查找
        return blueprints.get(blueprintId);
    }

    /**
     * 根据类型获取所有蓝图
     *
     * @param type 蓝图类型
     * @return 蓝图列表
     */
    public List<Blueprint> getBlueprintsByType(BlueprintType type) {
        if (type == null) {
            return new ArrayList<>();
        }

        Set<String> blueprintIds = blueprintsByType.get(type);
        List<Blueprint> result = new ArrayList<>();

        for (String id : blueprintIds) {
            Blueprint blueprint = getBlueprint(id);
            if (blueprint != null) {
                result.add(blueprint);
            }
        }

        return result;
    }

    /**
     * 根据分类获取所有蓝图
     *
     * @param category 分类名称
     * @return 蓝图列表
     */
    public List<Blueprint> getBlueprintsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> blueprintIds = blueprintsByCategory.get(category);
        if (blueprintIds == null) {
            return new ArrayList<>();
        }

        List<Blueprint> result = new ArrayList<>();
        for (String id : blueprintIds) {
            Blueprint blueprint = getBlueprint(id);
            if (blueprint != null) {
                result.add(blueprint);
            }
        }

        return result;
    }

    /**
     * 获取所有蓝图
     *
     * @return 所有蓝图的列表
     */
    public List<Blueprint> getAllBlueprints() {
        return new ArrayList<>(blueprints.values());
    }

    /**
     * 获取所有蓝图ID
     *
     * @return 所有蓝图ID的集合
     */
    public Set<String> getAllBlueprintIds() {
        return new HashSet<>(blueprints.keySet());
    }

    /**
     * 检查蓝图是否存在
     *
     * @param blueprintId 蓝图ID
     * @return 是否存在
     */
    public boolean hasBlueprint(String blueprintId) {
        return blueprintId != null && blueprints.containsKey(blueprintId);
    }

    /**
     * 获取蓝图数量统计
     *
     * @return 按类型统计的蓝图数量
     */
    public Map<BlueprintType, Integer> getBlueprintCounts() {
        Map<BlueprintType, Integer> counts = new HashMap<>();
        for (BlueprintType type : BlueprintType.values()) {
            counts.put(type, blueprintsByType.get(type).size());
        }
        return counts;
    }

    /**
     * 获取所有分类
     *
     * @return 所有分类的集合
     */
    public Set<String> getAllCategories() {
        return new HashSet<>(blueprintsByCategory.keySet());
    }

    // ==================== 操作方法 ====================

    /**
     * 保存蓝图到文件
     *
     * @param blueprint 要保存的蓝图
     * @return 是否保存成功
     */
    public boolean saveBlueprint(Blueprint blueprint) {
        if (blueprint == null) {
            return false;
        }

        try {
            File file = getBlueprintFile(blueprint);
            boolean success = blueprint.save(file);

            if (success) {
                // 更新缓存
                cache.put(blueprint);
                logger.info("成功保存蓝图: " + blueprint.getId());
            }

            return success;

        } catch (Exception e) {
            logger.severe("保存蓝图失败: " + blueprint.getId() + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 删除蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 是否删除成功
     */
    public boolean deleteBlueprint(String blueprintId) {
        if (!hasBlueprint(blueprintId)) {
            return false;
        }

        Blueprint blueprint = getBlueprint(blueprintId);
        if (blueprint == null) {
            return false;
        }

        try {
            // 删除文件
            File file = getBlueprintFile(blueprint);
            if (file.exists() && !file.delete()) {
                logger.warning("无法删除蓝图文件: " + file.getPath());
            }

            // 注销蓝图
            return unregisterBlueprint(blueprintId);

        } catch (Exception e) {
            logger.severe("删除蓝图失败: " + blueprintId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 重新加载蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 是否重新加载成功
     */
    public boolean reloadBlueprint(String blueprintId) {
        if (!hasBlueprint(blueprintId)) {
            return false;
        }

        Blueprint oldBlueprint = getBlueprint(blueprintId);
        if (oldBlueprint == null) {
            return false;
        }

        try {
            // 注销旧蓝图
            unregisterBlueprint(blueprintId);

            // 重新加载
            File file = getBlueprintFile(oldBlueprint);
            Blueprint newBlueprint = loadBlueprintFromFile(file, oldBlueprint.getType());

            if (newBlueprint != null) {
                return registerBlueprint(newBlueprint);
            }

            return false;

        } catch (Exception e) {
            logger.severe("重新加载蓝图失败: " + blueprintId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * 重新加载所有蓝图
     *
     * @return 重新加载的蓝图数量
     */
    public int reloadAllBlueprints() {
        logger.info("开始重新加载所有蓝图...");

        // 清理现有数据
        blueprints.clear();
        blueprintsByType.clear();
        blueprintsByCategory.clear();
        cache.clear();

        // 重新初始化类型索引
        for (BlueprintType type : BlueprintType.values()) {
            blueprintsByType.put(type, ConcurrentHashMap.newKeySet());
        }

        // 重新加载
        int loadedCount = loadAllBlueprints();
        this.lastLoadTime = System.currentTimeMillis();

        logger.info("重新加载完成，加载了 " + loadedCount + " 个蓝图");
        return loadedCount;
    }

    // ==================== 状态和信息方法 ====================

    /**
     * 检查管理器是否已初始化
     *
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取上次加载时间
     *
     * @return 上次加载时间戳
     */
    public long getLastLoadTime() {
        return lastLoadTime;
    }

    /**
     * 获取蓝图总数
     *
     * @return 蓝图总数
     */
    public int getTotalBlueprintCount() {
        return blueprints.size();
    }

    /**
     * 获取缓存系统
     *
     * @return 缓存系统实例
     */
    public BlueprintCache getCache() {
        return cache;
    }

    /**
     * 获取蓝图文件夹
     *
     * @return 蓝图文件夹
     */
    public File getBlueprintsFolder() {
        return blueprintsFolder;
    }

    /**
     * 获取管理器状态信息
     *
     * @return 格式化的状态信息
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 蓝图管理器状态 ===\n");
        sb.append("初始化状态: ").append(initialized ? "已初始化" : "未初始化").append("\n");
        sb.append("蓝图总数: ").append(getTotalBlueprintCount()).append("\n");
        sb.append("上次加载时间: ").append(new Date(lastLoadTime)).append("\n");

        sb.append("\n按类型统计:\n");
        Map<BlueprintType, Integer> counts = getBlueprintCounts();
        for (Map.Entry<BlueprintType, Integer> entry : counts.entrySet()) {
            sb.append("  ").append(entry.getKey().getDisplayName())
              .append(": ").append(entry.getValue()).append("\n");
        }

        sb.append("\n分类数量: ").append(getAllCategories().size()).append("\n");
        sb.append("缓存状态: ").append(cache.getStatusInfo());

        return sb.toString();
    }
}
