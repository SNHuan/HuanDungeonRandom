package org.snhuan.huanDungeonRandom.function;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 功能抽象基类 - 定义地牢内可执行功能的通用接口
 *
 * 功能系统特点：
 * - 支持多种触发方式（玩家交互、时间触发、条件触发等）
 * - 可配置的参数系统
 * - 支持功能的启用/禁用
 * - 提供执行结果反馈
 * - 支持功能链式调用
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class Function {

    protected final String id;
    protected final FunctionType type;
    protected final Logger logger;

    // 功能基本属性
    protected String name;
    protected String description;
    protected boolean enabled;
    protected int priority;
    protected long cooldown;
    protected long createdTime;

    // 位置信息
    protected Location location;
    protected double radius;

    // 配置参数
    protected final Map<String, Object> parameters;

    // 执行状态
    protected long lastExecutionTime;
    protected int executionCount;
    protected final Map<UUID, Long> playerCooldowns;

    /**
     * 构造函数
     *
     * @param id 功能ID
     * @param type 功能类型
     * @param logger 日志记录器
     */
    protected Function(String id, FunctionType type, Logger logger) {
        this.id = id;
        this.type = type;
        this.logger = logger;

        this.name = id;
        this.description = "";
        this.enabled = true;
        this.priority = 0;
        this.cooldown = 0;
        this.createdTime = System.currentTimeMillis();

        this.radius = 1.0;

        this.parameters = new HashMap<>();
        this.lastExecutionTime = 0;
        this.executionCount = 0;
        this.playerCooldowns = new HashMap<>();
    }

    /**
     * 执行功能
     *
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 执行结果
     */
    public final ExecutionResult execute(Player player, DungeonInstance dungeonInstance, ExecutionContext context) {
        // 检查功能是否启用
        if (!enabled) {
            return ExecutionResult.failure("功能已禁用: " + name);
        }

        // 检查冷却时间
        if (!checkCooldown(player)) {
            long remainingCooldown = getRemainingCooldown(player);
            return ExecutionResult.failure("功能冷却中，剩余时间: " + remainingCooldown + "ms");
        }

        // 检查执行条件
        if (!canExecute(player, dungeonInstance, context)) {
            return ExecutionResult.failure("不满足执行条件");
        }

        try {
            // 执行具体功能
            ExecutionResult result = doExecute(player, dungeonInstance, context);

            // 更新执行状态
            if (result.isSuccess()) {
                updateExecutionState(player);
            }

            return result;

        } catch (Exception e) {
            logger.severe("执行功能时发生异常: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.failure("执行异常: " + e.getMessage());
        }
    }

    /**
     * 具体的功能执行逻辑（由子类实现）
     *
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 执行结果
     */
    protected abstract ExecutionResult doExecute(Player player, DungeonInstance dungeonInstance, ExecutionContext context);

    /**
     * 检查是否可以执行功能
     *
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 是否可以执行
     */
    protected boolean canExecute(Player player, DungeonInstance dungeonInstance, ExecutionContext context) {
        // 检查玩家是否在地牢中
        if (!dungeonInstance.getState().getPlayersInside().contains(player.getUniqueId())) {
            return false;
        }

        // 检查位置范围（如果设置了位置）
        if (location != null && player.getLocation().distance(location) > radius) {
            return false;
        }

        return true;
    }

    /**
     * 检查冷却时间
     *
     * @param player 玩家
     * @return 是否可以执行
     */
    protected boolean checkCooldown(Player player) {
        if (cooldown <= 0) {
            return true;
        }

        long currentTime = System.currentTimeMillis();

        // 检查全局冷却
        if (currentTime - lastExecutionTime < cooldown) {
            return false;
        }

        // 检查玩家个人冷却
        Long playerLastExecution = playerCooldowns.get(player.getUniqueId());
        if (playerLastExecution != null && currentTime - playerLastExecution < cooldown) {
            return false;
        }

        return true;
    }

    /**
     * 获取剩余冷却时间
     *
     * @param player 玩家
     * @return 剩余冷却时间（毫秒）
     */
    protected long getRemainingCooldown(Player player) {
        if (cooldown <= 0) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();

        // 检查全局冷却
        long globalRemaining = cooldown - (currentTime - lastExecutionTime);
        if (globalRemaining > 0) {
            return globalRemaining;
        }

        // 检查玩家个人冷却
        Long playerLastExecution = playerCooldowns.get(player.getUniqueId());
        if (playerLastExecution != null) {
            long playerRemaining = cooldown - (currentTime - playerLastExecution);
            if (playerRemaining > 0) {
                return playerRemaining;
            }
        }

        return 0;
    }

    /**
     * 更新执行状态
     *
     * @param player 触发玩家
     */
    protected void updateExecutionState(Player player) {
        long currentTime = System.currentTimeMillis();
        lastExecutionTime = currentTime;
        executionCount++;

        if (cooldown > 0) {
            playerCooldowns.put(player.getUniqueId(), currentTime);
        }
    }

    // ==================== Getter 和 Setter 方法 ====================

    /**
     * 从配置加载功能数据
     *
     * @param config 配置节
     */
    public void loadFromConfig(ConfigurationSection config) {
        this.name = config.getString("name", id);
        this.description = config.getString("description", "");
        this.enabled = config.getBoolean("enabled", true);
        this.priority = config.getInt("priority", 0);
        this.cooldown = config.getLong("cooldown", 0);
        this.radius = config.getDouble("radius", 1.0);

        // 加载参数
        ConfigurationSection paramsSection = config.getConfigurationSection("parameters");
        if (paramsSection != null) {
            for (String key : paramsSection.getKeys(false)) {
                parameters.put(key, paramsSection.get(key));
            }
        }

        // 加载类型特定数据
        loadTypeSpecificData(config);
    }

    /**
     * 保存功能数据到配置
     *
     * @param config 配置节
     */
    public void saveToConfig(ConfigurationSection config) {
        config.set("type", type.name());
        config.set("name", name);
        config.set("description", description);
        config.set("enabled", enabled);
        config.set("priority", priority);
        config.set("cooldown", cooldown);
        config.set("radius", radius);

        // 保存参数
        if (!parameters.isEmpty()) {
            ConfigurationSection paramsSection = config.createSection("parameters");
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                paramsSection.set(entry.getKey(), entry.getValue());
            }
        }

        // 保存类型特定数据
        saveTypeSpecificData(config);
    }

    /**
     * 加载类型特定数据（由子类实现）
     *
     * @param config 配置节
     */
    protected abstract void loadTypeSpecificData(ConfigurationSection config);

    /**
     * 保存类型特定数据（由子类实现）
     *
     * @param config 配置节
     */
    protected abstract void saveTypeSpecificData(ConfigurationSection config);

    /**
     * 获取功能信息
     *
     * @return 功能信息字符串
     */
    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append("功能ID: ").append(id).append("\n");
        info.append("名称: ").append(name).append("\n");
        info.append("类型: ").append(type.getDisplayName()).append("\n");
        info.append("状态: ").append(enabled ? "启用" : "禁用").append("\n");
        info.append("优先级: ").append(priority).append("\n");
        info.append("冷却时间: ").append(cooldown).append("ms\n");
        info.append("执行次数: ").append(executionCount).append("\n");

        if (!description.isEmpty()) {
            info.append("描述: ").append(description).append("\n");
        }

        return info.toString();
    }

    // ==================== Getter 和 Setter 方法 ====================

    public String getId() { return id; }
    public FunctionType getType() { return type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public long getCooldown() { return cooldown; }
    public void setCooldown(long cooldown) { this.cooldown = cooldown; }
    public Location getLocation() { return location != null ? location.clone() : null; }
    public void setLocation(Location location) { this.location = location != null ? location.clone() : null; }
    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
    public Map<String, Object> getParameters() { return new HashMap<>(parameters); }
    public void setParameter(String key, Object value) { parameters.put(key, value); }
    public Object getParameter(String key) { return parameters.get(key); }
    public Object getParameter(String key, Object defaultValue) { return parameters.getOrDefault(key, defaultValue); }
    public long getLastExecutionTime() { return lastExecutionTime; }
    public int getExecutionCount() { return executionCount; }
    public long getCreatedTime() { return createdTime; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Function function = (Function) obj;
        return id.equals(function.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Function{id='%s', name='%s', type=%s}", id, name, type);
    }
}
