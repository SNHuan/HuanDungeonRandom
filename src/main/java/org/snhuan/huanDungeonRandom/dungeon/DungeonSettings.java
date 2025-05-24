package org.snhuan.huanDungeonRandom.dungeon;

/**
 * 地牢设置类 - 封装地牢的各种设置选项
 *
 * 提供地牢运行时的各种设置，包括：
 * - 玩家行为限制
 * - 环境设置
 * - 游戏规则
 * - 保护设置
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonSettings {
    
    private final DungeonInstance.DungeonConfig config;
    
    /**
     * 构造函数
     * 
     * @param config 地牢配置
     */
    public DungeonSettings(DungeonInstance.DungeonConfig config) {
        this.config = config;
    }
    
    /**
     * 是否允许方块破坏
     * 
     * @return 是否允许
     */
    public boolean isAllowBlockBreaking() {
        return config.isAllowBreaking();
    }
    
    /**
     * 是否允许方块放置
     * 
     * @return 是否允许
     */
    public boolean isAllowBlockPlacing() {
        return config.isAllowPlacing();
    }
    
    /**
     * 是否启用PvP
     * 
     * @return 是否启用
     */
    public boolean isPvpEnabled() {
        return config.isAllowPvP();
    }
    
    /**
     * 是否启用环境伤害
     * 
     * @return 是否启用
     */
    public boolean isEnvironmentalDamageEnabled() {
        return getBooleanSetting("environmental_damage", true);
    }
    
    /**
     * 是否允许方块物理效果
     * 
     * @return 是否允许
     */
    public boolean isAllowBlockPhysics() {
        return getBooleanSetting("block_physics", true);
    }
    
    /**
     * 是否允许爆炸
     * 
     * @return 是否允许
     */
    public boolean isAllowExplosions() {
        return getBooleanSetting("explosions", false);
    }
    
    /**
     * 是否允许方块燃烧
     * 
     * @return 是否允许
     */
    public boolean isAllowBlockBurning() {
        return getBooleanSetting("block_burning", false);
    }
    
    /**
     * 是否允许方块生长
     * 
     * @return 是否允许
     */
    public boolean isAllowBlockGrowth() {
        return getBooleanSetting("block_growth", true);
    }
    
    /**
     * 是否允许自然生成
     * 
     * @return 是否允许
     */
    public boolean isAllowNaturalSpawning() {
        return getBooleanSetting("natural_spawning", false);
    }
    
    /**
     * 是否限制物品栏访问
     * 
     * @return 是否限制
     */
    public boolean isRestrictInventoryAccess() {
        return getBooleanSetting("restrict_inventory", false);
    }
    
    /**
     * 是否隔离地牢内聊天
     * 
     * @return 是否隔离
     */
    public boolean isIsolateChatInDungeon() {
        return getBooleanSetting("isolate_chat", true);
    }
    
    /**
     * 是否保持物品栏
     * 
     * @return 是否保持
     */
    public boolean isKeepInventoryOnDeath() {
        return config.isKeepInventory();
    }
    
    /**
     * 获取最大玩家数
     * 
     * @return 最大玩家数
     */
    public int getMaxPlayers() {
        return config.getMaxPlayers();
    }
    
    /**
     * 获取时间限制
     * 
     * @return 时间限制（毫秒）
     */
    public long getTimeLimit() {
        return config.getTimeLimit();
    }
    
    /**
     * 获取难度
     * 
     * @return 难度
     */
    public String getDifficulty() {
        return config.getDifficulty();
    }
    
    /**
     * 获取布尔设置
     * 
     * @param key 设置键
     * @param defaultValue 默认值
     * @return 设置值
     */
    private boolean getBooleanSetting(String key, boolean defaultValue) {
        Object value = config.getCustomSetting(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    /**
     * 获取整数设置
     * 
     * @param key 设置键
     * @param defaultValue 默认值
     * @return 设置值
     */
    private int getIntSetting(String key, int defaultValue) {
        Object value = config.getCustomSetting(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取字符串设置
     * 
     * @param key 设置键
     * @param defaultValue 默认值
     * @return 设置值
     */
    private String getStringSetting(String key, String defaultValue) {
        Object value = config.getCustomSetting(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
    
    /**
     * 获取自定义设置
     * 
     * @param key 设置键
     * @return 设置值
     */
    public Object getCustomSetting(String key) {
        return config.getCustomSetting(key);
    }
    
    /**
     * 获取自定义设置（带类型转换）
     * 
     * @param key 设置键
     * @param type 目标类型
     * @param defaultValue 默认值
     * @param <T> 类型参数
     * @return 设置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomSetting(String key, Class<T> type, T defaultValue) {
        Object value = config.getCustomSetting(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * 转换为字符串表示
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "DungeonSettings{" +
                "maxPlayers=" + getMaxPlayers() +
                ", timeLimit=" + getTimeLimit() +
                ", pvpEnabled=" + isPvpEnabled() +
                ", allowBreaking=" + isAllowBlockBreaking() +
                ", allowPlacing=" + isAllowBlockPlacing() +
                ", keepInventory=" + isKeepInventoryOnDeath() +
                ", difficulty='" + getDifficulty() + '\'' +
                '}';
    }
}
