package org.snhuan.huanDungeonRandom.trigger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.function.ExecutionResult;
import org.snhuan.huanDungeonRandom.function.FunctionManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * 触发器管理器 - 负责触发器的注册、管理和执行
 *
 * 主要功能：
 * - 触发器的注册和注销
 * - 触发器配置的加载和保存
 * - 事件监听和触发器激活
 * - 触发器状态管理
 * - 位置索引和快速查找
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TriggerManager {

    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");

    private final JavaPlugin plugin;
    private final FunctionManager functionManager;
    private final TriggerFactory triggerFactory;

    // 触发器存储
    private final Map<String, Trigger> triggers;
    private final Map<String, Set<String>> dungeonTriggers;
    private final Map<Location, Set<String>> locationTriggers;
    private final Map<Trigger.TriggerType, Set<String>> typeTriggers;

    // 配置文件
    private File triggersDir;
    private File globalTriggersFile;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     * @param functionManager 功能管理器
     */
    public TriggerManager(JavaPlugin plugin, FunctionManager functionManager) {
        this.plugin = plugin;
        this.functionManager = functionManager;
        this.triggerFactory = new TriggerFactory();

        this.triggers = new ConcurrentHashMap<>();
        this.dungeonTriggers = new ConcurrentHashMap<>();
        this.locationTriggers = new ConcurrentHashMap<>();
        this.typeTriggers = new ConcurrentHashMap<>();

        initializeDirectories();
    }

    /**
     * 初始化目录结构
     */
    private void initializeDirectories() {
        triggersDir = new File(plugin.getDataFolder(), "triggers");
        if (!triggersDir.exists()) {
            triggersDir.mkdirs();
        }

        globalTriggersFile = new File(triggersDir, "global.yml");
    }

    /**
     * 初始化触发器管理器
     *
     * @return 是否初始化成功
     */
    public boolean initialize() {
        try {
            logger.info("正在初始化触发器管理器...");

            // 加载全局触发器配置
            loadGlobalTriggers();

            logger.info("触发器管理器初始化完成，已加载 " + triggers.size() + " 个触发器");
            return true;

        } catch (Exception e) {
            logger.severe("触发器管理器初始化失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭触发器管理器
     */
    public void shutdown() {
        try {
            logger.info("正在关闭触发器管理器...");

            // 保存所有触发器配置
            saveGlobalTriggers();

            // 停用所有触发器
            triggers.values().forEach(Trigger::deactivate);

            // 清理数据
            triggers.clear();
            dungeonTriggers.clear();
            locationTriggers.clear();
            typeTriggers.clear();

            logger.info("触发器管理器已关闭");

        } catch (Exception e) {
            logger.severe("关闭触发器管理器时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载全局触发器配置
     */
    private void loadGlobalTriggers() {
        if (!globalTriggersFile.exists()) {
            createDefaultGlobalTriggers();
            return;
        }

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(globalTriggersFile);
            ConfigurationSection triggersSection = config.getConfigurationSection("triggers");

            if (triggersSection != null) {
                for (String triggerId : triggersSection.getKeys(false)) {
                    ConfigurationSection triggerSection = triggersSection.getConfigurationSection(triggerId);
                    if (triggerSection != null) {
                        loadTriggerFromConfig(triggerId, triggerSection);
                    }
                }
            }

            logger.info("已从全局配置加载 " + triggers.size() + " 个触发器");

        } catch (Exception e) {
            logger.severe("加载全局触发器配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从配置加载触发器
     *
     * @param triggerId 触发器ID
     * @param config 配置节点
     */
    private void loadTriggerFromConfig(String triggerId, ConfigurationSection config) {
        try {
            String typeName = config.getString("type");
            if (typeName == null) {
                logger.warning("触发器 " + triggerId + " 缺少类型配置");
                return;
            }

            Trigger.TriggerType type;
            try {
                type = Trigger.TriggerType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                logger.warning("触发器 " + triggerId + " 的类型无效: " + typeName);
                return;
            }

            Trigger trigger = triggerFactory.createTrigger(type);
            if (trigger == null) {
                logger.warning("无法创建触发器: " + triggerId + " (类型: " + typeName + ")");
                return;
            }

            trigger.setId(triggerId);
            trigger.loadFromConfig(config);

            registerTrigger(trigger);

        } catch (Exception e) {
            logger.severe("加载触发器配置失败: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存全局触发器配置
     */
    private void saveGlobalTriggers() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            ConfigurationSection triggersSection = config.createSection("triggers");

            for (Trigger trigger : triggers.values()) {
                ConfigurationSection triggerSection = triggersSection.createSection(trigger.getId());
                trigger.saveToConfig(triggerSection);
            }

            config.save(globalTriggersFile);
            logger.info("已保存 " + triggers.size() + " 个触发器到全局配置");

        } catch (Exception e) {
            logger.severe("保存全局触发器配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建默认全局触发器配置
     */
    private void createDefaultGlobalTriggers() {
        try {
            YamlConfiguration config = new YamlConfiguration();

            // 添加示例触发器配置
            ConfigurationSection triggersSection = config.createSection("triggers");

            // 示例：玩家进入触发器
            ConfigurationSection enterTrigger = triggersSection.createSection("example_enter");
            enterTrigger.set("type", "PLAYER_ENTER");
            enterTrigger.set("name", "示例进入触发器");
            enterTrigger.set("description", "当玩家进入指定区域时触发");
            enterTrigger.set("enabled", false);
            enterTrigger.set("repeatable", true);
            enterTrigger.set("functions", Arrays.asList("example_function"));

            config.save(globalTriggersFile);
            logger.info("已创建默认全局触发器配置");

        } catch (Exception e) {
            logger.severe("创建默认全局触发器配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 注册触发器
     *
     * @param trigger 触发器实例
     * @return 是否注册成功
     */
    public boolean registerTrigger(Trigger trigger) {
        if (trigger == null) {
            logger.warning("尝试注册空的触发器实例");
            return false;
        }

        String id = trigger.getId();
        if (triggers.containsKey(id)) {
            logger.warning("触发器ID已存在: " + id);
            return false;
        }

        triggers.put(id, trigger);

        // 添加到类型索引
        addTypeTrigger(trigger.getType(), id);

        // 如果触发器有位置信息，添加到位置索引
        if (trigger.getLocation() != null) {
            addLocationTrigger(trigger.getLocation(), id);
        }

        // 激活触发器
        trigger.activate();

        logger.info("成功注册触发器: " + id + " (类型: " + trigger.getType().getDisplayName() + ")");
        return true;
    }

    /**
     * 注销触发器
     *
     * @param triggerId 触发器ID
     * @return 是否注销成功
     */
    public boolean unregisterTrigger(String triggerId) {
        Trigger trigger = triggers.remove(triggerId);
        if (trigger == null) {
            logger.warning("尝试注销不存在的触发器: " + triggerId);
            return false;
        }

        // 停用触发器
        trigger.deactivate();

        // 从类型索引中移除
        removeTypeTrigger(trigger.getType(), triggerId);

        // 从位置索引中移除
        if (trigger.getLocation() != null) {
            removeLocationTrigger(trigger.getLocation(), triggerId);
        }

        // 从地牢索引中移除
        dungeonTriggers.values().forEach(set -> set.remove(triggerId));

        logger.info("成功注销触发器: " + triggerId);
        return true;
    }

    /**
     * 处理事件触发
     *
     * @param event 事件
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @return 触发结果列表
     */
    public List<TriggerResult> handleEvent(Event event, Player player, DungeonInstance dungeonInstance) {
        List<TriggerResult> results = new ArrayList<>();

        try {
            // 获取相关触发器
            Set<Trigger> relevantTriggers = getRelevantTriggers(event, player, dungeonInstance);

            for (Trigger trigger : relevantTriggers) {
                try {
                    if (trigger.canTrigger(player, event, dungeonInstance)) {
                        TriggerResult result = trigger.trigger(player, event, dungeonInstance);
                        results.add(result);

                        // 如果触发成功，执行关联的功能
                        if (result.isSuccess()) {
                            executeTriggerFunctions(trigger, player, dungeonInstance, result);
                        }
                    }
                } catch (Exception e) {
                    logger.severe("处理触发器时发生异常: " + trigger.getId() + " - " + e.getMessage());
                    results.add(TriggerResult.failure("触发器执行异常: " + e.getMessage(), e));
                }
            }

        } catch (Exception e) {
            logger.severe("处理事件触发时发生异常: " + e.getMessage());
            e.printStackTrace();
            results.add(TriggerResult.failure("事件处理异常: " + e.getMessage(), e));
        }

        return results;
    }

    /**
     * 获取相关触发器
     *
     * @param event 事件
     * @param player 玩家
     * @param dungeonInstance 地牢实例
     * @return 相关触发器集合
     */
    private Set<Trigger> getRelevantTriggers(Event event, Player player, DungeonInstance dungeonInstance) {
        Set<Trigger> relevantTriggers = new HashSet<>();

        // 根据事件类型获取触发器
        Trigger.TriggerType eventType = mapEventToTriggerType(event);
        if (eventType != null) {
            Set<String> typeTriggersIds = typeTriggers.get(eventType);
            if (typeTriggersIds != null) {
                typeTriggersIds.stream()
                    .map(triggers::get)
                    .filter(Objects::nonNull)
                    .forEach(relevantTriggers::add);
            }
        }

        // 根据位置获取触发器
        Location playerLocation = player.getLocation();
        for (Map.Entry<Location, Set<String>> entry : locationTriggers.entrySet()) {
            Location triggerLocation = entry.getKey();
            if (isLocationNear(playerLocation, triggerLocation, 5.0)) { // 5格范围内
                entry.getValue().stream()
                    .map(triggers::get)
                    .filter(Objects::nonNull)
                    .forEach(relevantTriggers::add);
            }
        }

        // 根据地牢获取触发器
        if (dungeonInstance != null) {
            Set<String> dungeonTriggersIds = dungeonTriggers.get(dungeonInstance.getId());
            if (dungeonTriggersIds != null) {
                dungeonTriggersIds.stream()
                    .map(triggers::get)
                    .filter(Objects::nonNull)
                    .forEach(relevantTriggers::add);
            }
        }

        return relevantTriggers;
    }

    /**
     * 执行触发器关联的功能
     *
     * @param trigger 触发器
     * @param player 玩家
     * @param dungeonInstance 地牢实例
     * @param triggerResult 触发结果
     */
    private void executeTriggerFunctions(Trigger trigger, Player player, DungeonInstance dungeonInstance, TriggerResult triggerResult) {
        List<String> functionIds = trigger.getFunctionIds();
        if (functionIds.isEmpty()) {
            return;
        }

        // 创建执行上下文
        ExecutionContext context = ExecutionContext.builder(trigger.getId(), ExecutionContext.TriggerType.SYSTEM_TRIGGER)
            .setTriggerPlayer(player)
            .setTriggerLocation(trigger.getLocation() != null ? trigger.getLocation() : player.getLocation())
            .addData("trigger_result", triggerResult)
            .addData("trigger_type", trigger.getType().name())
            .addAllData(trigger.getFunctionParameters())
            .build();

        for (String functionId : functionIds) {
            try {
                ExecutionResult result = functionManager.executeFunction(functionId, player, dungeonInstance, context);
                if (result.isFailure()) {
                    logger.warning("触发器关联功能执行失败: " + functionId + " - " + result.getMessage());
                }
            } catch (Exception e) {
                logger.severe("执行触发器关联功能时发生异常: " + functionId + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 映射事件到触发器类型
     *
     * @param event 事件
     * @return 触发器类型
     */
    private Trigger.TriggerType mapEventToTriggerType(Event event) {
        String eventName = event.getClass().getSimpleName();

        switch (eventName) {
            case "PlayerMoveEvent":
                return Trigger.TriggerType.PLAYER_MOVE;
            case "PlayerInteractEvent":
                return Trigger.TriggerType.PLAYER_INTERACT;
            case "BlockBreakEvent":
                return Trigger.TriggerType.BLOCK_BREAK;
            case "BlockPlaceEvent":
                return Trigger.TriggerType.BLOCK_PLACE;
            case "BlockRedstoneEvent":
                return Trigger.TriggerType.REDSTONE_CHANGE;
            default:
                return null;
        }
    }

    /**
     * 检查位置是否接近
     *
     * @param loc1 位置1
     * @param loc2 位置2
     * @param distance 距离阈值
     * @return 是否接近
     */
    private boolean isLocationNear(Location loc1, Location loc2, double distance) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distance(loc2) <= distance;
    }

    /**
     * 手动触发指定触发器
     *
     * @param triggerId 触发器ID
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @return 触发结果
     */
    public TriggerResult manualTrigger(String triggerId, Player player, DungeonInstance dungeonInstance) {
        Trigger trigger = triggers.get(triggerId);
        if (trigger == null) {
            return TriggerResult.failure("触发器不存在: " + triggerId);
        }

        try {
            // 创建手动触发事件
            Event manualEvent = new ManualTriggerEvent(player, trigger);

            if (trigger.canTrigger(player, manualEvent, dungeonInstance)) {
                TriggerResult result = trigger.trigger(player, manualEvent, dungeonInstance);

                // 如果触发成功，执行关联的功能
                if (result.isSuccess()) {
                    executeTriggerFunctions(trigger, player, dungeonInstance, result);
                }

                return result;
            } else {
                return TriggerResult.failure("触发条件不满足");
            }

        } catch (Exception e) {
            logger.severe("手动触发器执行异常: " + triggerId + " - " + e.getMessage());
            e.printStackTrace();
            return TriggerResult.failure("触发器执行异常: " + e.getMessage(), e);
        }
    }

    /**
     * 为地牢绑定触发器
     *
     * @param dungeonId 地牢ID
     * @param triggerId 触发器ID
     */
    public void bindTriggerToDungeon(String dungeonId, String triggerId) {
        dungeonTriggers.computeIfAbsent(dungeonId, k -> new HashSet<>()).add(triggerId);
        logger.info("已将触发器 " + triggerId + " 绑定到地牢 " + dungeonId);
    }

    /**
     * 解除地牢触发器绑定
     *
     * @param dungeonId 地牢ID
     * @param triggerId 触发器ID
     */
    public void unbindTriggerFromDungeon(String dungeonId, String triggerId) {
        Set<String> triggerIds = dungeonTriggers.get(dungeonId);
        if (triggerIds != null) {
            triggerIds.remove(triggerId);
            if (triggerIds.isEmpty()) {
                dungeonTriggers.remove(dungeonId);
            }
        }
        logger.info("已解除触发器 " + triggerId + " 与地牢 " + dungeonId + " 的绑定");
    }

    // ==================== 索引管理方法 ====================

    /**
     * 添加类型触发器索引
     *
     * @param type 触发器类型
     * @param triggerId 触发器ID
     */
    private void addTypeTrigger(Trigger.TriggerType type, String triggerId) {
        typeTriggers.computeIfAbsent(type, k -> new HashSet<>()).add(triggerId);
    }

    /**
     * 移除类型触发器索引
     *
     * @param type 触发器类型
     * @param triggerId 触发器ID
     */
    private void removeTypeTrigger(Trigger.TriggerType type, String triggerId) {
        Set<String> triggerIds = typeTriggers.get(type);
        if (triggerIds != null) {
            triggerIds.remove(triggerId);
            if (triggerIds.isEmpty()) {
                typeTriggers.remove(type);
            }
        }
    }

    /**
     * 添加位置触发器索引
     *
     * @param location 位置
     * @param triggerId 触发器ID
     */
    private void addLocationTrigger(Location location, String triggerId) {
        locationTriggers.computeIfAbsent(location, k -> new HashSet<>()).add(triggerId);
    }

    /**
     * 移除位置触发器索引
     *
     * @param location 位置
     * @param triggerId 触发器ID
     */
    private void removeLocationTrigger(Location location, String triggerId) {
        Set<String> triggerIds = locationTriggers.get(location);
        if (triggerIds != null) {
            triggerIds.remove(triggerId);
            if (triggerIds.isEmpty()) {
                locationTriggers.remove(location);
            }
        }
    }

    // ==================== 查询方法 ====================

    /**
     * 获取触发器
     *
     * @param triggerId 触发器ID
     * @return 触发器实例
     */
    public Trigger getTrigger(String triggerId) {
        return triggers.get(triggerId);
    }

    /**
     * 获取所有触发器
     *
     * @return 触发器集合
     */
    public Collection<Trigger> getAllTriggers() {
        return new ArrayList<>(triggers.values());
    }

    /**
     * 根据类型获取触发器
     *
     * @param type 触发器类型
     * @return 触发器列表
     */
    public List<Trigger> getTriggersByType(Trigger.TriggerType type) {
        Set<String> triggerIds = typeTriggers.get(type);
        if (triggerIds == null) {
            return new ArrayList<>();
        }

        return triggerIds.stream()
            .map(triggers::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 根据地牢获取触发器
     *
     * @param dungeonId 地牢ID
     * @return 触发器列表
     */
    public List<Trigger> getTriggersByDungeon(String dungeonId) {
        Set<String> triggerIds = dungeonTriggers.get(dungeonId);
        if (triggerIds == null) {
            return new ArrayList<>();
        }

        return triggerIds.stream()
            .map(triggers::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 根据位置获取触发器
     *
     * @param location 位置
     * @param radius 搜索半径
     * @return 触发器列表
     */
    public List<Trigger> getTriggersByLocation(Location location, double radius) {
        List<Trigger> nearbyTriggers = new ArrayList<>();

        for (Map.Entry<Location, Set<String>> entry : locationTriggers.entrySet()) {
            Location triggerLocation = entry.getKey();
            if (isLocationNear(location, triggerLocation, radius)) {
                entry.getValue().stream()
                    .map(triggers::get)
                    .filter(Objects::nonNull)
                    .forEach(nearbyTriggers::add);
            }
        }

        return nearbyTriggers;
    }

    /**
     * 检查触发器是否存在
     *
     * @param triggerId 触发器ID
     * @return 是否存在
     */
    public boolean hasTrigger(String triggerId) {
        return triggers.containsKey(triggerId);
    }

    /**
     * 获取触发器数量
     *
     * @return 触发器数量
     */
    public int getTriggerCount() {
        return triggers.size();
    }

    public int getRegisteredTriggerCount() {
        return triggers.size();
    }

    public List<Trigger> getDungeonTriggers(DungeonInstance dungeonInstance) {
        return getTriggersByDungeon(dungeonInstance.getInstanceId());
    }

    /**
     * 获取活跃触发器数量
     *
     * @return 活跃触发器数量
     */
    public int getActiveTriggerCount() {
        return (int) triggers.values().stream()
            .filter(Trigger::isActive)
            .count();
    }

    /**
     * 启用触发器
     *
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean enableTrigger(String triggerId) {
        Trigger trigger = triggers.get(triggerId);
        if (trigger != null) {
            trigger.setEnabled(true);
            trigger.activate();
            logger.info("已启用触发器: " + triggerId);
            return true;
        }
        return false;
    }

    /**
     * 禁用触发器
     *
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean disableTrigger(String triggerId) {
        Trigger trigger = triggers.get(triggerId);
        if (trigger != null) {
            trigger.setEnabled(false);
            trigger.deactivate();
            logger.info("已禁用触发器: " + triggerId);
            return true;
        }
        return false;
    }

    /**
     * 重置触发器状态
     *
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    public boolean resetTrigger(String triggerId) {
        Trigger trigger = triggers.get(triggerId);
        if (trigger != null) {
            trigger.reset();
            logger.info("已重置触发器状态: " + triggerId);
            return true;
        }
        return false;
    }

    /**
     * 重置所有触发器状态
     */
    public void resetAllTriggers() {
        triggers.values().forEach(Trigger::reset);
        logger.info("已重置所有触发器状态");
    }

    /**
     * 获取触发器工厂
     *
     * @return 触发器工厂
     */
    public TriggerFactory getTriggerFactory() {
        return triggerFactory;
    }
}
