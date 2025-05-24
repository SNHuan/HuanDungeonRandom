package org.snhuan.huanDungeonRandom.trigger;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;

import java.util.*;
import java.util.logging.Logger;

/**
 * 触发器抽象基类 - 定义地牢内事件触发的通用接口
 *
 * 触发器系统特点：
 * - 监听特定事件并在满足条件时激活
 * - 支持复杂的条件判断
 * - 可配置的触发参数
 * - 支持触发器的启用/禁用
 * - 提供触发结果反馈
 * - 支持触发器链式调用
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class Trigger {

    protected static final Logger logger = Logger.getLogger("HuanDungeonRandom");

    // 基础属性
    protected String id;
    protected String name;
    protected String description;
    protected TriggerType type;
    protected boolean enabled;
    protected Location location;

    // 触发条件
    protected Map<String, Object> conditions;
    protected Set<String> requiredPermissions;
    protected long cooldownTime;
    protected int maxTriggers;
    protected boolean repeatable;

    // 关联的功能
    protected List<String> functionIds;
    protected Map<String, Object> functionParameters;

    // 运行时状态
    protected Map<UUID, Long> lastTriggerTime;
    protected Map<UUID, Integer> triggerCount;
    protected boolean active;

    /**
     * 触发器类型枚举
     */
    public enum TriggerType {
        PLAYER_ENTER("玩家进入", "当玩家进入指定区域时触发"),
        PLAYER_LEAVE("玩家离开", "当玩家离开指定区域时触发"),
        PLAYER_INTERACT("玩家交互", "当玩家与指定对象交互时触发"),
        PLAYER_MOVE("玩家移动", "当玩家移动到指定位置时触发"),
        BLOCK_BREAK("方块破坏", "当指定方块被破坏时触发"),
        BLOCK_PLACE("方块放置", "当指定方块被放置时触发"),
        REDSTONE_CHANGE("红石变化", "当红石信号发生变化时触发"),
        TIME_INTERVAL("时间间隔", "按指定时间间隔触发"),
        TIME_DELAY("时间延迟", "延迟指定时间后触发"),
        CONDITION_MET("条件满足", "当指定条件满足时触发"),
        SIGNAL_RECEIVED("信号接收", "当接收到指定信号时触发"),
        DUNGEON_START("地牢开始", "当地牢开始时触发"),
        DUNGEON_END("地牢结束", "当地牢结束时触发"),
        MANUAL("手动触发", "通过命令或API手动触发");

        private final String displayName;
        private final String description;

        TriggerType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    /**
     * 构造函数
     */
    public Trigger() {
        this.conditions = new HashMap<>();
        this.requiredPermissions = new HashSet<>();
        this.functionIds = new ArrayList<>();
        this.functionParameters = new HashMap<>();
        this.lastTriggerTime = new HashMap<>();
        this.triggerCount = new HashMap<>();
        this.enabled = true;
        this.active = false;
        this.repeatable = true;
        this.maxTriggers = -1; // -1表示无限制
    }

    /**
     * 检查是否可以触发
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否可以触发
     */
    public final boolean canTrigger(Player player, Event event, DungeonInstance dungeonInstance) {
        // 检查触发器是否启用
        if (!enabled || !active) {
            return false;
        }

        // 检查冷却时间
        if (!checkCooldown(player)) {
            return false;
        }

        // 检查触发次数限制
        if (!checkTriggerLimit(player)) {
            return false;
        }

        // 检查权限
        if (!checkPermissions(player)) {
            return false;
        }

        // 检查自定义条件
        if (!checkConditions(player, event, dungeonInstance)) {
            return false;
        }

        // 检查具体触发条件
        return checkTriggerConditions(player, event, dungeonInstance);
    }

    /**
     * 执行触发
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 触发结果
     */
    public final TriggerResult trigger(Player player, Event event, DungeonInstance dungeonInstance) {
        if (!canTrigger(player, event, dungeonInstance)) {
            return TriggerResult.failure("触发条件不满足");
        }

        try {
            // 更新触发状态
            updateTriggerState(player);

            // 创建执行上下文
            ExecutionContext context = createExecutionContext(player, event, dungeonInstance);

            // 执行具体触发逻辑
            TriggerResult result = doTrigger(player, event, dungeonInstance, context);

            // 如果不可重复触发，禁用触发器
            if (!repeatable && result.isSuccess()) {
                this.active = false;
            }

            return result;

        } catch (Exception e) {
            logger.severe("触发器执行异常: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return TriggerResult.failure("触发器执行异常: " + e.getMessage());
        }
    }

    /**
     * 检查具体触发条件（由子类实现）
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否满足触发条件
     */
    protected abstract boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance);

    /**
     * 执行具体触发逻辑（由子类实现）
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 触发结果
     */
    protected abstract TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context);

    /**
     * 加载触发器特定数据（由子类实现）
     *
     * @param config 配置节点
     */
    protected abstract void loadTriggerSpecificData(ConfigurationSection config);

    /**
     * 保存触发器特定数据（由子类实现）
     *
     * @param config 配置节点
     */
    protected abstract void saveTriggerSpecificData(ConfigurationSection config);

    /**
     * 检查冷却时间
     *
     * @param player 玩家
     * @return 是否通过冷却检查
     */
    protected boolean checkCooldown(Player player) {
        if (cooldownTime <= 0) {
            return true;
        }

        Long lastTrigger = lastTriggerTime.get(player.getUniqueId());
        if (lastTrigger == null) {
            return true;
        }

        return System.currentTimeMillis() - lastTrigger >= cooldownTime;
    }

    /**
     * 检查触发次数限制
     *
     * @param player 玩家
     * @return 是否通过次数检查
     */
    protected boolean checkTriggerLimit(Player player) {
        if (maxTriggers <= 0) {
            return true;
        }

        Integer count = triggerCount.get(player.getUniqueId());
        return count == null || count < maxTriggers;
    }

    /**
     * 检查权限
     *
     * @param player 玩家
     * @return 是否有权限
     */
    protected boolean checkPermissions(Player player) {
        if (requiredPermissions.isEmpty()) {
            return true;
        }

        for (String permission : requiredPermissions) {
            if (!player.hasPermission(permission)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查自定义条件
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否满足条件
     */
    protected boolean checkConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        // 基础实现，子类可以重写
        return true;
    }

    /**
     * 更新触发状态
     *
     * @param player 触发玩家
     */
    protected void updateTriggerState(Player player) {
        UUID playerId = player.getUniqueId();

        // 更新最后触发时间
        lastTriggerTime.put(playerId, System.currentTimeMillis());

        // 更新触发次数
        triggerCount.put(playerId, triggerCount.getOrDefault(playerId, 0) + 1);
    }

    /**
     * 创建执行上下文
     *
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 执行上下文
     */
    protected ExecutionContext createExecutionContext(Player player, Event event, DungeonInstance dungeonInstance) {
        ExecutionContext.TriggerType contextType = mapToContextTriggerType(this.type);

        return ExecutionContext.builder(this.id, contextType)
            .setTriggerPlayer(player)
            .setTriggerLocation(this.location != null ? this.location : player.getLocation())
            .setTriggerEvent(event)
            .addData("trigger_type", this.type.name())
            .addData("trigger_name", this.name)
            .addData("dungeon_id", dungeonInstance.getId())
            .build();
    }

    /**
     * 映射触发器类型到上下文触发器类型
     *
     * @param triggerType 触发器类型
     * @return 上下文触发器类型
     */
    private ExecutionContext.TriggerType mapToContextTriggerType(TriggerType triggerType) {
        switch (triggerType) {
            case PLAYER_INTERACT:
                return ExecutionContext.TriggerType.PLAYER_INTERACT;
            case PLAYER_MOVE:
            case PLAYER_ENTER:
            case PLAYER_LEAVE:
                return ExecutionContext.TriggerType.PLAYER_MOVE;
            case BLOCK_BREAK:
                return ExecutionContext.TriggerType.BLOCK_BREAK;
            case BLOCK_PLACE:
                return ExecutionContext.TriggerType.BLOCK_PLACE;
            case TIME_INTERVAL:
            case TIME_DELAY:
                return ExecutionContext.TriggerType.TIME_TRIGGER;
            case CONDITION_MET:
                return ExecutionContext.TriggerType.CONDITION_TRIGGER;
            case MANUAL:
                return ExecutionContext.TriggerType.MANUAL_TRIGGER;
            default:
                return ExecutionContext.TriggerType.SYSTEM_TRIGGER;
        }
    }

    /**
     * 从配置加载触发器数据
     *
     * @param config 配置节点
     */
    public void loadFromConfig(ConfigurationSection config) {
        this.id = config.getString("id", "");
        this.name = config.getString("name", "");
        this.description = config.getString("description", "");
        this.enabled = config.getBoolean("enabled", true);
        this.repeatable = config.getBoolean("repeatable", true);
        this.cooldownTime = config.getLong("cooldown", 0);
        this.maxTriggers = config.getInt("max-triggers", -1);

        // 加载位置信息
        if (config.contains("location")) {
            ConfigurationSection locSection = config.getConfigurationSection("location");
            if (locSection != null) {
                double x = locSection.getDouble("x");
                double y = locSection.getDouble("y");
                double z = locSection.getDouble("z");
                String worldName = locSection.getString("world");
                if (worldName != null) {
                    this.location = new Location(
                        org.bukkit.Bukkit.getWorld(worldName), x, y, z
                    );
                }
            }
        }

        // 加载权限要求
        this.requiredPermissions.clear();
        if (config.contains("permissions")) {
            this.requiredPermissions.addAll(config.getStringList("permissions"));
        }

        // 加载条件
        this.conditions.clear();
        if (config.contains("conditions")) {
            ConfigurationSection condSection = config.getConfigurationSection("conditions");
            if (condSection != null) {
                for (String key : condSection.getKeys(false)) {
                    this.conditions.put(key, condSection.get(key));
                }
            }
        }

        // 加载关联功能
        this.functionIds.clear();
        if (config.contains("functions")) {
            this.functionIds.addAll(config.getStringList("functions"));
        }

        // 加载功能参数
        this.functionParameters.clear();
        if (config.contains("function-parameters")) {
            ConfigurationSection paramSection = config.getConfigurationSection("function-parameters");
            if (paramSection != null) {
                for (String key : paramSection.getKeys(false)) {
                    this.functionParameters.put(key, paramSection.get(key));
                }
            }
        }

        // 加载触发器特定数据
        loadTriggerSpecificData(config);
    }

    /**
     * 保存触发器数据到配置
     *
     * @param config 配置节点
     */
    public void saveToConfig(ConfigurationSection config) {
        config.set("id", id);
        config.set("name", name);
        config.set("description", description);
        config.set("type", type.name());
        config.set("enabled", enabled);
        config.set("repeatable", repeatable);
        config.set("cooldown", cooldownTime);
        config.set("max-triggers", maxTriggers);

        // 保存位置信息
        if (location != null) {
            ConfigurationSection locSection = config.createSection("location");
            locSection.set("x", location.getX());
            locSection.set("y", location.getY());
            locSection.set("z", location.getZ());
            locSection.set("world", location.getWorld().getName());
        }

        // 保存权限要求
        if (!requiredPermissions.isEmpty()) {
            config.set("permissions", new ArrayList<>(requiredPermissions));
        }

        // 保存条件
        if (!conditions.isEmpty()) {
            ConfigurationSection condSection = config.createSection("conditions");
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                condSection.set(entry.getKey(), entry.getValue());
            }
        }

        // 保存关联功能
        if (!functionIds.isEmpty()) {
            config.set("functions", functionIds);
        }

        // 保存功能参数
        if (!functionParameters.isEmpty()) {
            ConfigurationSection paramSection = config.createSection("function-parameters");
            for (Map.Entry<String, Object> entry : functionParameters.entrySet()) {
                paramSection.set(entry.getKey(), entry.getValue());
            }
        }

        // 保存触发器特定数据
        saveTriggerSpecificData(config);
    }

    /**
     * 激活触发器
     */
    public void activate() {
        this.active = true;
        logger.info("触发器已激活: " + id);
    }

    /**
     * 停用触发器
     */
    public void deactivate() {
        this.active = false;
        logger.info("触发器已停用: " + id);
    }

    /**
     * 重置触发器状态
     */
    public void reset() {
        this.lastTriggerTime.clear();
        this.triggerCount.clear();
        this.active = true;
        logger.info("触发器状态已重置: " + id);
    }

    /**
     * 获取玩家剩余冷却时间
     *
     * @param player 玩家
     * @return 剩余冷却时间（毫秒）
     */
    public long getRemainingCooldown(Player player) {
        if (cooldownTime <= 0) {
            return 0;
        }

        Long lastTrigger = lastTriggerTime.get(player.getUniqueId());
        if (lastTrigger == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastTrigger;
        return Math.max(0, cooldownTime - elapsed);
    }

    /**
     * 获取玩家触发次数
     *
     * @param player 玩家
     * @return 触发次数
     */
    public int getTriggerCount(Player player) {
        return triggerCount.getOrDefault(player.getUniqueId(), 0);
    }

    // ==================== Getter 和 Setter 方法 ====================

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TriggerType getType() { return type; }
    public void setType(TriggerType type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isActive() { return active; }

    public Location getLocation() { return location != null ? location.clone() : null; }
    public void setLocation(Location location) { this.location = location != null ? location.clone() : null; }

    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }

    public long getCooldownTime() { return cooldownTime; }
    public void setCooldownTime(long cooldownTime) { this.cooldownTime = cooldownTime; }

    public int getMaxTriggers() { return maxTriggers; }
    public void setMaxTriggers(int maxTriggers) { this.maxTriggers = maxTriggers; }

    public List<String> getFunctionIds() { return new ArrayList<>(functionIds); }
    public void setFunctionIds(List<String> functionIds) {
        this.functionIds = new ArrayList<>(functionIds);
    }
    public void addFunctionId(String functionId) { this.functionIds.add(functionId); }
    public void removeFunctionId(String functionId) { this.functionIds.remove(functionId); }

    public Map<String, Object> getFunctionParameters() { return new HashMap<>(functionParameters); }
    public void setFunctionParameters(Map<String, Object> functionParameters) {
        this.functionParameters = new HashMap<>(functionParameters);
    }
    public void setFunctionParameter(String key, Object value) { this.functionParameters.put(key, value); }

    public Set<String> getRequiredPermissions() { return new HashSet<>(requiredPermissions); }
    public void setRequiredPermissions(Set<String> requiredPermissions) {
        this.requiredPermissions = new HashSet<>(requiredPermissions);
    }
    public void addRequiredPermission(String permission) { this.requiredPermissions.add(permission); }

    public Map<String, Object> getConditions() { return new HashMap<>(conditions); }
    public void setConditions(Map<String, Object> conditions) {
        this.conditions = new HashMap<>(conditions);
    }
    public void setCondition(String key, Object value) { this.conditions.put(key, value); }

    /**
     * 检查触发器是否在冷却中
     *
     * @return 是否在冷却中
     */
    public boolean isOnCooldown() {
        // 检查是否有任何玩家在冷却中
        long currentTime = System.currentTimeMillis();
        return lastTriggerTime.values().stream()
            .anyMatch(lastTime -> currentTime - lastTime < cooldownTime);
    }

    /**
     * 重置冷却时间
     */
    public void resetCooldown() {
        lastTriggerTime.clear();
        logger.info("触发器冷却已重置: " + id);
    }
}
