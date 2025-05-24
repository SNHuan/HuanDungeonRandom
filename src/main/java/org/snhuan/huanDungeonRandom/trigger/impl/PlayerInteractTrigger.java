package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.*;

/**
 * 玩家交互触发器实现 - 当玩家与指定对象交互时触发
 *
 * 支持的交互类型：
 * - 右键点击方块
 * - 左键点击方块
 * - 右键点击空气
 * - 左键点击空气
 * - 与特定材质的方块交互
 * - 手持特定物品交互
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class PlayerInteractTrigger extends Trigger {
    
    // 交互触发器特有配置
    private Set<Material> targetMaterials;
    private Set<Material> requiredItems;
    private Set<String> allowedActions;
    private boolean requireSneaking;
    private double interactionRange;
    
    /**
     * 构造函数
     */
    public PlayerInteractTrigger() {
        super();
        this.type = TriggerType.PLAYER_INTERACT;
        this.targetMaterials = new HashSet<>();
        this.requiredItems = new HashSet<>();
        this.allowedActions = new HashSet<>();
        this.requireSneaking = false;
        this.interactionRange = 5.0;
        
        // 默认允许所有交互动作
        this.allowedActions.add("RIGHT_CLICK_BLOCK");
        this.allowedActions.add("LEFT_CLICK_BLOCK");
    }
    
    /**
     * 检查具体触发条件
     * 
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @return 是否满足触发条件
     */
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        // 只处理玩家交互事件
        if (!(event instanceof PlayerInteractEvent)) {
            return false;
        }
        
        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
        
        // 检查交互动作
        if (!allowedActions.isEmpty() && !allowedActions.contains(interactEvent.getAction().name())) {
            return false;
        }
        
        // 检查是否需要潜行
        if (requireSneaking && !player.isSneaking()) {
            return false;
        }
        
        // 检查交互位置
        if (location != null) {
            Block clickedBlock = interactEvent.getClickedBlock();
            if (clickedBlock == null) {
                return false;
            }
            
            if (!isLocationNear(clickedBlock.getLocation(), location, interactionRange)) {
                return false;
            }
        }
        
        // 检查目标材质
        if (!targetMaterials.isEmpty()) {
            Block clickedBlock = interactEvent.getClickedBlock();
            if (clickedBlock == null || !targetMaterials.contains(clickedBlock.getType())) {
                return false;
            }
        }
        
        // 检查手持物品
        if (!requiredItems.isEmpty()) {
            Material itemInHand = player.getInventory().getItemInMainHand().getType();
            if (!requiredItems.contains(itemInHand)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 执行具体触发逻辑
     * 
     * @param player 触发玩家
     * @param event 触发事件
     * @param dungeonInstance 地牢实例
     * @param context 执行上下文
     * @return 触发结果
     */
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        try {
            PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
            
            // 创建结果数据
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("trigger_id", this.id);
            resultData.put("trigger_type", "PLAYER_INTERACT");
            resultData.put("player", player.getName());
            resultData.put("action", interactEvent.getAction().name());
            resultData.put("timestamp", System.currentTimeMillis());
            
            Block clickedBlock = interactEvent.getClickedBlock();
            if (clickedBlock != null) {
                resultData.put("block_type", clickedBlock.getType().name());
                resultData.put("block_location", clickedBlock.getLocation());
            }
            
            Material itemInHand = player.getInventory().getItemInMainHand().getType();
            resultData.put("item_in_hand", itemInHand.name());
            
            if (dungeonInstance != null) {
                resultData.put("dungeon_id", dungeonInstance.getId());
            }
            
            // 记录触发日志
            logger.info("玩家交互触发器被激活: " + this.id + " by " + player.getName() + 
                       " (动作: " + interactEvent.getAction().name() + ")");
            
            return TriggerResult.success("玩家交互触发器执行成功", resultData);
            
        } catch (Exception e) {
            logger.severe("玩家交互触发器执行异常: " + this.id + " - " + e.getMessage());
            e.printStackTrace();
            return TriggerResult.failure("触发器执行异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载触发器特定数据
     * 
     * @param config 配置节点
     */
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {
        // 加载目标材质
        this.targetMaterials.clear();
        if (config.contains("target-materials")) {
            List<String> materialNames = config.getStringList("target-materials");
            for (String materialName : materialNames) {
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    this.targetMaterials.add(material);
                } catch (IllegalArgumentException e) {
                    logger.warning("无效的材质名称: " + materialName);
                }
            }
        }
        
        // 加载所需物品
        this.requiredItems.clear();
        if (config.contains("required-items")) {
            List<String> itemNames = config.getStringList("required-items");
            for (String itemName : itemNames) {
                try {
                    Material material = Material.valueOf(itemName.toUpperCase());
                    this.requiredItems.add(material);
                } catch (IllegalArgumentException e) {
                    logger.warning("无效的物品名称: " + itemName);
                }
            }
        }
        
        // 加载允许的动作
        this.allowedActions.clear();
        if (config.contains("allowed-actions")) {
            this.allowedActions.addAll(config.getStringList("allowed-actions"));
        }
        
        this.requireSneaking = config.getBoolean("require-sneaking", false);
        this.interactionRange = config.getDouble("interaction-range", 5.0);
    }
    
    /**
     * 保存触发器特定数据
     * 
     * @param config 配置节点
     */
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {
        // 保存目标材质
        if (!targetMaterials.isEmpty()) {
            List<String> materialNames = new ArrayList<>();
            for (Material material : targetMaterials) {
                materialNames.add(material.name());
            }
            config.set("target-materials", materialNames);
        }
        
        // 保存所需物品
        if (!requiredItems.isEmpty()) {
            List<String> itemNames = new ArrayList<>();
            for (Material material : requiredItems) {
                itemNames.add(material.name());
            }
            config.set("required-items", itemNames);
        }
        
        // 保存允许的动作
        if (!allowedActions.isEmpty()) {
            config.set("allowed-actions", new ArrayList<>(allowedActions));
        }
        
        config.set("require-sneaking", requireSneaking);
        config.set("interaction-range", interactionRange);
    }
    
    /**
     * 检查位置是否接近
     * 
     * @param loc1 位置1
     * @param loc2 位置2
     * @param distance 距离阈值
     * @return 是否接近
     */
    private boolean isLocationNear(org.bukkit.Location loc1, org.bukkit.Location loc2, double distance) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distance(loc2) <= distance;
    }
    
    // ==================== Getter 和 Setter 方法 ====================
    
    public Set<Material> getTargetMaterials() {
        return new HashSet<>(targetMaterials);
    }
    
    public void setTargetMaterials(Set<Material> targetMaterials) {
        this.targetMaterials = new HashSet<>(targetMaterials);
    }
    
    public void addTargetMaterial(Material material) {
        this.targetMaterials.add(material);
    }
    
    public Set<Material> getRequiredItems() {
        return new HashSet<>(requiredItems);
    }
    
    public void setRequiredItems(Set<Material> requiredItems) {
        this.requiredItems = new HashSet<>(requiredItems);
    }
    
    public void addRequiredItem(Material material) {
        this.requiredItems.add(material);
    }
    
    public Set<String> getAllowedActions() {
        return new HashSet<>(allowedActions);
    }
    
    public void setAllowedActions(Set<String> allowedActions) {
        this.allowedActions = new HashSet<>(allowedActions);
    }
    
    public void addAllowedAction(String action) {
        this.allowedActions.add(action);
    }
    
    public boolean isRequireSneaking() {
        return requireSneaking;
    }
    
    public void setRequireSneaking(boolean requireSneaking) {
        this.requireSneaking = requireSneaking;
    }
    
    public double getInteractionRange() {
        return interactionRange;
    }
    
    public void setInteractionRange(double interactionRange) {
        this.interactionRange = interactionRange;
    }
}
