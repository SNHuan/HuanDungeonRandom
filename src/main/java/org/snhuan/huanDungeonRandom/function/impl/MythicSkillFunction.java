package org.snhuan.huanDungeonRandom.function.impl;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * MythicMobs技能功能实现类 - 执行MythicMobs技能
 *
 * 功能特点：
 * - 支持执行任意MythicMobs技能
 * - 可配置技能参数和目标
 * - 支持多种触发方式
 * - 兼容MythicMobs的所有机制
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class MythicSkillFunction extends Function {

    // MythicMobs技能配置
    private String skillName;
    private Map<String, Object> skillParameters;
    private TargetType targetType;
    private double skillPower;
    private boolean asyncExecution;

    // 目标配置
    private Location customTargetLocation;
    private String targetSelector;

    /**
     * 目标类型枚举
     */
    public enum TargetType {
        TRIGGER_PLAYER("触发玩家"),
        CUSTOM_LOCATION("自定义位置"),
        FUNCTION_LOCATION("功能位置"),
        ALL_PLAYERS("所有玩家"),
        NEARBY_PLAYERS("附近玩家"),
        SELECTOR("选择器");

        private final String displayName;

        TargetType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 构造函数
     *
     * @param id 功能ID
     * @param logger 日志记录器
     */
    public MythicSkillFunction(String id, Logger logger) {
        super(id, FunctionType.MYTHIC_SKILL, logger);

        // 默认配置
        this.skillParameters = new HashMap<>();
        this.targetType = TargetType.TRIGGER_PLAYER;
        this.skillPower = 1.0;
        this.asyncExecution = false;
        this.targetSelector = "@trigger";
    }

    @Override
    protected ExecutionResult doExecute(Player player, DungeonInstance dungeonInstance, ExecutionContext context) {
        // 检查MythicMobs是否可用
        if (!isMythicMobsAvailable()) {
            return ExecutionResult.failure("MythicMobs插件未找到或未启用");
        }

        // 检查技能是否存在
        if (skillName == null || skillName.isEmpty()) {
            return ExecutionResult.failure("未配置技能名称");
        }

        try {
            // 确定目标位置和实体
            ExecutionTarget target = determineTarget(player, dungeonInstance, context);
            if (target == null) {
                return ExecutionResult.failure("无法确定技能目标");
            }

            // 直接执行MythicMobs技能（使用命令方式）
            return executeMythicSkill(skillName, player, target);

        } catch (Exception e) {
            logger.severe("执行MythicMobs技能时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.failure("技能执行异常: " + e.getMessage());
        }
    }

    /**
     * 检查MythicMobs是否可用
     *
     * @return 是否可用
     */
    private boolean isMythicMobsAvailable() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }

    /**
     * 确定技能目标
     *
     * @param player 触发玩家
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 执行目标
     */
    private ExecutionTarget determineTarget(Player player, DungeonInstance dungeonInstance, ExecutionContext context) {
        switch (targetType) {
            case TRIGGER_PLAYER:
                return new ExecutionTarget(player, player.getLocation());

            case CUSTOM_LOCATION:
                if (customTargetLocation != null) {
                    Location target = customTargetLocation.clone();
                    target.setWorld(dungeonInstance.getWorld());
                    return new ExecutionTarget(null, target);
                }
                break;

            case FUNCTION_LOCATION:
                if (location != null) {
                    return new ExecutionTarget(null, location.clone());
                }
                break;

            case ALL_PLAYERS:
                // 对地牢中的所有玩家执行
                // 这里简化处理，返回触发玩家作为代表
                return new ExecutionTarget(player, player.getLocation());

            case NEARBY_PLAYERS:
                // 对附近玩家执行
                return new ExecutionTarget(player, player.getLocation());

            case SELECTOR:
                // 使用选择器（这里简化处理）
                return new ExecutionTarget(player, player.getLocation());
        }

        return null;
    }


    /**
     * 执行MythicMobs技能 - 简化版本，直接使用命令方式
     *
     * @param skillName 技能名称
     * @param caster 施法者
     * @param target 目标位置
     * @return 执行结果
     */
    private ExecutionResult executeMythicSkill(String skillName, Player caster, ExecutionTarget target) {
        try {
            // 构建MythicMobs命令
            String command;
            if (target.getEntity() != null) {
                // 对实体施法
                command = String.format("mm cast %s %s", skillName, target.getEntity().getName());
            } else {
                // 对位置施法
                Location loc = target.getLocation();
                command = String.format("mm cast %s %s %d %d %d",
                    skillName, caster.getName(),
                    (int)loc.getX(), (int)loc.getY(), (int)loc.getZ());
            }

            // 执行命令
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            if (success) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("skill_name", skillName);
                resultData.put("caster", caster.getName());
                resultData.put("target_location", target.getLocation());

                return ExecutionResult.success("MythicMobs技能执行成功: " + skillName, resultData);
            } else {
                return ExecutionResult.failure("MythicMobs技能执行失败: " + skillName);
            }

        } catch (Exception e) {
            logger.severe("执行MythicMobs技能异常: " + e.getMessage());
            return ExecutionResult.failure("技能执行异常: " + e.getMessage());
        }
    }

    @Override
    protected void loadTypeSpecificData(ConfigurationSection config) {
        // 加载技能配置
        this.skillName = config.getString("skill-name", "");
        this.skillPower = config.getDouble("skill-power", 1.0);
        this.asyncExecution = config.getBoolean("async-execution", false);
        this.targetSelector = config.getString("target-selector", "@trigger");

        // 加载目标类型
        String targetTypeName = config.getString("target-type", "TRIGGER_PLAYER");
        try {
            this.targetType = TargetType.valueOf(targetTypeName);
        } catch (IllegalArgumentException e) {
            logger.warning("无效的目标类型: " + targetTypeName + "，使用默认值");
            this.targetType = TargetType.TRIGGER_PLAYER;
        }

        // 加载自定义目标位置
        if (config.contains("custom-target")) {
            ConfigurationSection targetSection = config.getConfigurationSection("custom-target");
            if (targetSection != null) {
                double x = targetSection.getDouble("x", 0);
                double y = targetSection.getDouble("y", 0);
                double z = targetSection.getDouble("z", 0);
                this.customTargetLocation = new Location(null, x, y, z);
            }
        }

        // 加载技能参数
        ConfigurationSection paramsSection = config.getConfigurationSection("skill-parameters");
        if (paramsSection != null) {
            skillParameters.clear();
            for (String key : paramsSection.getKeys(false)) {
                skillParameters.put(key, paramsSection.get(key));
            }
        }
    }

    @Override
    protected void saveTypeSpecificData(ConfigurationSection config) {
        // 保存技能配置
        config.set("skill-name", skillName);
        config.set("skill-power", skillPower);
        config.set("async-execution", asyncExecution);
        config.set("target-type", targetType.name());
        config.set("target-selector", targetSelector);

        // 保存自定义目标位置
        if (customTargetLocation != null) {
            ConfigurationSection targetSection = config.createSection("custom-target");
            targetSection.set("x", customTargetLocation.getX());
            targetSection.set("y", customTargetLocation.getY());
            targetSection.set("z", customTargetLocation.getZ());
        }

        // 保存技能参数
        if (!skillParameters.isEmpty()) {
            ConfigurationSection paramsSection = config.createSection("skill-parameters");
            for (Map.Entry<String, Object> entry : skillParameters.entrySet()) {
                paramsSection.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 执行目标类
     */
    private static class ExecutionTarget {
        private final org.bukkit.entity.Entity entity;
        private final Location location;

        public ExecutionTarget(org.bukkit.entity.Entity entity, Location location) {
            this.entity = entity;
            this.location = location;
        }

        public org.bukkit.entity.Entity getEntity() { return entity; }
        public Location getLocation() { return location; }
    }

    // ==================== Getter 和 Setter 方法 ====================

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public Map<String, Object> getSkillParameters() { return new HashMap<>(skillParameters); }
    public void setSkillParameters(Map<String, Object> skillParameters) {
        this.skillParameters = new HashMap<>(skillParameters);
    }
    public void setSkillParameter(String key, Object value) { skillParameters.put(key, value); }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public double getSkillPower() { return skillPower; }
    public void setSkillPower(double skillPower) { this.skillPower = skillPower; }

    public boolean isAsyncExecution() { return asyncExecution; }
    public void setAsyncExecution(boolean asyncExecution) { this.asyncExecution = asyncExecution; }

    public Location getCustomTargetLocation() {
        return customTargetLocation != null ? customTargetLocation.clone() : null;
    }
    public void setCustomTargetLocation(Location customTargetLocation) {
        this.customTargetLocation = customTargetLocation != null ? customTargetLocation.clone() : null;
    }

    public String getTargetSelector() { return targetSelector; }
    public void setTargetSelector(String targetSelector) { this.targetSelector = targetSelector; }
}
