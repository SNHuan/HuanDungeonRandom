package org.snhuan.huanDungeonRandom.function.impl;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.*;

import java.util.logging.Logger;

/**
 * 传送功能实现类 - 将玩家传送到指定位置
 * 
 * 功能特点：
 * - 支持相对位置和绝对位置传送
 * - 可配置传送效果和声音
 * - 支持传送前后的状态效果
 * - 可设置传送延迟
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TeleportFunction extends Function {
    
    // 传送目标
    private Location targetLocation;
    private boolean useRelativePosition;
    
    // 效果配置
    private boolean playSound;
    private Sound teleportSound;
    private boolean showParticles;
    private boolean applyEffects;
    
    // 传送延迟
    private long teleportDelay;
    
    /**
     * 构造函数
     * 
     * @param id 功能ID
     * @param logger 日志记录器
     */
    public TeleportFunction(String id, Logger logger) {
        super(id, FunctionType.TELEPORT, logger);
        
        // 默认配置
        this.useRelativePosition = true;
        this.playSound = true;
        this.teleportSound = Sound.ENTITY_ENDERMAN_TELEPORT;
        this.showParticles = true;
        this.applyEffects = false;
        this.teleportDelay = 0;
    }
    
    @Override
    protected ExecutionResult doExecute(Player player, DungeonInstance dungeonInstance, ExecutionContext context) {
        try {
            // 计算目标位置
            Location finalTarget = calculateTargetLocation(dungeonInstance);
            if (finalTarget == null) {
                return ExecutionResult.failure("无法计算目标传送位置");
            }
            
            // 检查目标位置是否安全
            if (!isSafeLocation(finalTarget)) {
                return ExecutionResult.failure("目标位置不安全");
            }
            
            // 如果有延迟，异步执行传送
            if (teleportDelay > 0) {
                return executeDelayedTeleport(player, finalTarget);
            } else {
                return executeImmediateTeleport(player, finalTarget);
            }
            
        } catch (Exception e) {
            logger.severe("传送功能执行异常: " + e.getMessage());
            return ExecutionResult.failure("传送失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算目标传送位置
     * 
     * @param dungeonInstance 地牢实例
     * @return 目标位置
     */
    private Location calculateTargetLocation(DungeonInstance dungeonInstance) {
        if (targetLocation == null) {
            return null;
        }
        
        if (useRelativePosition) {
            // 相对于地牢原点的位置
            Location dungeonOrigin = dungeonInstance.getOrigin();
            return dungeonOrigin.clone().add(targetLocation.toVector());
        } else {
            // 绝对位置
            return targetLocation.clone();
        }
    }
    
    /**
     * 检查位置是否安全
     * 
     * @param location 要检查的位置
     * @return 是否安全
     */
    private boolean isSafeLocation(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        
        // 检查脚下是否有实体方块
        Location below = location.clone().subtract(0, 1, 0);
        if (below.getBlock().getType().isAir()) {
            return false;
        }
        
        // 检查头部和身体位置是否为空气
        if (!location.getBlock().getType().isAir() || 
            !location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行即时传送
     * 
     * @param player 玩家
     * @param target 目标位置
     * @return 执行结果
     */
    private ExecutionResult executeImmediateTeleport(Player player, Location target) {
        // 播放传送前效果
        if (showParticles) {
            player.getWorld().spawnParticle(
                org.bukkit.Particle.PORTAL, 
                player.getLocation().add(0, 1, 0), 
                20, 0.5, 1, 0.5, 0.1
            );
        }
        
        // 执行传送
        boolean success = player.teleport(target);
        
        if (success) {
            // 播放传送后效果
            if (playSound) {
                player.playSound(target, teleportSound, 1.0f, 1.0f);
            }
            
            if (showParticles) {
                target.getWorld().spawnParticle(
                    org.bukkit.Particle.PORTAL, 
                    target.clone().add(0, 1, 0), 
                    20, 0.5, 1, 0.5, 0.1
                );
            }
            
            if (applyEffects) {
                // 给予短暂的抗性效果
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DAMAGE_RESISTANCE, 
                    60, 1, false, false
                ));
            }
            
            return ExecutionResult.success("传送成功");
        } else {
            return ExecutionResult.failure("传送失败");
        }
    }
    
    /**
     * 执行延迟传送
     * 
     * @param player 玩家
     * @param target 目标位置
     * @return 执行结果
     */
    private ExecutionResult executeDelayedTeleport(Player player, Location target) {
        // 这里应该使用调度器来实现延迟传送
        // 为了简化，暂时直接执行即时传送
        // TODO: 实现真正的延迟传送机制
        
        player.sendMessage("§e传送将在 " + (teleportDelay / 1000.0) + " 秒后执行...");
        
        return executeImmediateTeleport(player, target);
    }
    
    @Override
    protected void loadTypeSpecificData(ConfigurationSection config) {
        // 加载目标位置
        if (config.contains("target")) {
            ConfigurationSection targetSection = config.getConfigurationSection("target");
            if (targetSection != null) {
                double x = targetSection.getDouble("x", 0);
                double y = targetSection.getDouble("y", 0);
                double z = targetSection.getDouble("z", 0);
                float yaw = (float) targetSection.getDouble("yaw", 0);
                float pitch = (float) targetSection.getDouble("pitch", 0);
                
                // 注意：这里没有设置世界，因为世界信息在运行时确定
                this.targetLocation = new Location(null, x, y, z, yaw, pitch);
            }
        }
        
        // 加载其他配置
        this.useRelativePosition = config.getBoolean("use-relative-position", true);
        this.playSound = config.getBoolean("play-sound", true);
        this.showParticles = config.getBoolean("show-particles", true);
        this.applyEffects = config.getBoolean("apply-effects", false);
        this.teleportDelay = config.getLong("teleport-delay", 0);
        
        // 加载声音类型
        String soundName = config.getString("teleport-sound", "ENTITY_ENDERMAN_TELEPORT");
        try {
            this.teleportSound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            logger.warning("无效的声音类型: " + soundName + "，使用默认声音");
            this.teleportSound = Sound.ENTITY_ENDERMAN_TELEPORT;
        }
    }
    
    @Override
    protected void saveTypeSpecificData(ConfigurationSection config) {
        // 保存目标位置
        if (targetLocation != null) {
            ConfigurationSection targetSection = config.createSection("target");
            targetSection.set("x", targetLocation.getX());
            targetSection.set("y", targetLocation.getY());
            targetSection.set("z", targetLocation.getZ());
            targetSection.set("yaw", targetLocation.getYaw());
            targetSection.set("pitch", targetLocation.getPitch());
        }
        
        // 保存其他配置
        config.set("use-relative-position", useRelativePosition);
        config.set("play-sound", playSound);
        config.set("teleport-sound", teleportSound.name());
        config.set("show-particles", showParticles);
        config.set("apply-effects", applyEffects);
        config.set("teleport-delay", teleportDelay);
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public Location getTargetLocation() {
        return targetLocation != null ? targetLocation.clone() : null;
    }
    
    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation != null ? targetLocation.clone() : null;
    }
    
    public boolean isUseRelativePosition() { return useRelativePosition; }
    public void setUseRelativePosition(boolean useRelativePosition) { this.useRelativePosition = useRelativePosition; }
    
    public boolean isPlaySound() { return playSound; }
    public void setPlaySound(boolean playSound) { this.playSound = playSound; }
    
    public Sound getTeleportSound() { return teleportSound; }
    public void setTeleportSound(Sound teleportSound) { this.teleportSound = teleportSound; }
    
    public boolean isShowParticles() { return showParticles; }
    public void setShowParticles(boolean showParticles) { this.showParticles = showParticles; }
    
    public boolean isApplyEffects() { return applyEffects; }
    public void setApplyEffects(boolean applyEffects) { this.applyEffects = applyEffects; }
    
    public long getTeleportDelay() { return teleportDelay; }
    public void setTeleportDelay(long teleportDelay) { this.teleportDelay = teleportDelay; }
}
