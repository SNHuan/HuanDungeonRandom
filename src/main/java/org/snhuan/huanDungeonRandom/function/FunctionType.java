package org.snhuan.huanDungeonRandom.function;

/**
 * 功能类型枚举 - 定义地牢中各种功能点的类型
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public enum FunctionType {

    /**
     * 传送点 - 用于玩家传送
     */
    TELEPORT("传送点", "teleport", true),

    /**
     * 宝箱 - 包含奖励物品
     */
    CHEST("宝箱", "chest", true),

    /**
     * 机关 - 需要玩家触发的机制
     */
    MECHANISM("机关", "mechanism", true),

    /**
     * NPC - 非玩家角色
     */
    NPC("NPC", "npc", true),

    /**
     * 触发器 - 自动触发的功能
     */
    TRIGGER("触发器", "trigger", false),

    /**
     * 生成点 - 怪物或物品生成位置
     */
    SPAWNER("生成点", "spawner", false),

    /**
     * 检查点 - 进度保存点
     */
    CHECKPOINT("检查点", "checkpoint", true),

    /**
     * 商店 - 交易功能
     */
    SHOP("商店", "shop", true),

    /**
     * 治疗点 - 恢复玩家状态
     */
    HEALING("治疗点", "healing", true),

    /**
     * 陷阱 - 对玩家造成负面效果
     */
    TRAP("陷阱", "trap", false),

    /**
     * 开关 - 控制其他功能的开关
     */
    SWITCH("开关", "switch", true),

    /**
     * MythicMobs技能 - 执行MythicMobs技能
     */
    MYTHIC_SKILL("MythicMobs技能", "mythic_skill", true),

    /**
     * 自定义 - 用户自定义功能
     */
    CUSTOM("自定义", "custom", true);

    private final String displayName;
    private final String configKey;
    private final boolean requiresPlayerInteraction;

    /**
     * 构造函数
     *
     * @param displayName 显示名称
     * @param configKey 配置键
     * @param requiresPlayerInteraction 是否需要玩家交互
     */
    FunctionType(String displayName, String configKey, boolean requiresPlayerInteraction) {
        this.displayName = displayName;
        this.configKey = configKey;
        this.requiresPlayerInteraction = requiresPlayerInteraction;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取配置键
     *
     * @return 配置键
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * 是否需要玩家交互
     *
     * @return 是否需要玩家交互
     */
    public boolean requiresPlayerInteraction() {
        return requiresPlayerInteraction;
    }

    /**
     * 根据配置键获取功能类型
     *
     * @param configKey 配置键
     * @return 功能类型，如果未找到则返回CUSTOM
     */
    public static FunctionType fromConfigKey(String configKey) {
        if (configKey == null) {
            return CUSTOM;
        }

        for (FunctionType type : values()) {
            if (type.configKey.equalsIgnoreCase(configKey)) {
                return type;
            }
        }

        return CUSTOM;
    }

    /**
     * 获取所有需要玩家交互的功能类型
     *
     * @return 功能类型数组
     */
    public static FunctionType[] getInteractiveTypes() {
        return java.util.Arrays.stream(values())
            .filter(FunctionType::requiresPlayerInteraction)
            .toArray(FunctionType[]::new);
    }

    /**
     * 获取所有自动触发的功能类型
     *
     * @return 功能类型数组
     */
    public static FunctionType[] getAutomaticTypes() {
        return java.util.Arrays.stream(values())
            .filter(type -> !type.requiresPlayerInteraction)
            .toArray(FunctionType[]::new);
    }
}
