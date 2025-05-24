package org.snhuan.huanDungeonRandom.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.List;
import java.util.logging.Logger;

/**
 * 方块事件监听器 - 监听方块相关事件并触发相应的触发器
 *
 * 监听的事件类型：
 * - 方块破坏事件（BlockBreakEvent）
 * - 方块放置事件（BlockPlaceEvent）
 * - 红石变化事件（BlockRedstoneEvent）
 * - 方块物理事件（BlockPhysicsEvent）
 * - 方块爆炸事件（BlockExplodeEvent）
 * - 方块燃烧事件（BlockBurnEvent）
 * - 方块生长事件（BlockGrowEvent）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class BlockListener implements Listener {
    
    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");
    
    private final DungeonManager dungeonManager;
    private final TriggerManager triggerManager;
    
    /**
     * 构造函数
     * 
     * @param dungeonManager 地牢管理器
     * @param triggerManager 触发器管理器
     */
    public BlockListener(DungeonManager dungeonManager, TriggerManager triggerManager) {
        this.dungeonManager = dungeonManager;
        this.triggerManager = triggerManager;
    }
    
    /**
     * 处理方块破坏事件
     * 
     * @param event 方块破坏事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否允许在地牢中破坏方块
            if (!dungeonInstance.getSettings().isAllowBlockBreaking()) {
                event.setCancelled(true);
                player.sendMessage("§c在此地牢中不允许破坏方块！");
                return;
            }
            
            // 检查是否是受保护的方块
            if (dungeonInstance.isProtectedBlock(block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§c这个方块受到保护，无法破坏！");
                return;
            }
            
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);
            
            // 记录方块破坏
            dungeonInstance.recordBlockChange(block.getLocation(), block.getType(), null);
            
        } catch (Exception e) {
            logger.severe("处理方块破坏事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理方块放置事件
     * 
     * @param event 方块放置事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否允许在地牢中放置方块
            if (!dungeonInstance.getSettings().isAllowBlockPlacing()) {
                event.setCancelled(true);
                player.sendMessage("§c在此地牢中不允许放置方块！");
                return;
            }
            
            // 检查是否在受保护的区域
            if (dungeonInstance.isProtectedArea(block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage("§c这个区域受到保护，无法放置方块！");
                return;
            }
            
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);
            
            // 记录方块放置
            dungeonInstance.recordBlockChange(block.getLocation(), null, block.getType());
            
        } catch (Exception e) {
            logger.severe("处理方块放置事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理红石变化事件
     * 
     * @param event 红石变化事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 触发相关触发器（红石触发器不需要玩家参与）
            List<TriggerResult> results = triggerManager.handleEvent(event, null, dungeonInstance);
            
            // 记录红石变化日志
            if (!results.isEmpty()) {
                logger.info("地牢 " + dungeonInstance.getId() + " 中的红石信号变化触发了 " + results.size() + " 个触发器");
            }
            
        } catch (Exception e) {
            logger.severe("处理红石变化事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理方块物理事件
     * 
     * @param event 方块物理事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否禁用物理效果
            if (!dungeonInstance.getSettings().isAllowBlockPhysics()) {
                event.setCancelled(true);
                return;
            }
            
            // 检查是否是受保护的方块
            if (dungeonInstance.isProtectedBlock(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
            
        } catch (Exception e) {
            logger.severe("处理方块物理事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理方块爆炸事件
     * 
     * @param event 方块爆炸事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        
        // 检查爆炸是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否允许爆炸
            if (!dungeonInstance.getSettings().isAllowExplosions()) {
                event.setCancelled(true);
                return;
            }
            
            // 移除受保护的方块
            event.blockList().removeIf(explodedBlock -> 
                dungeonInstance.isProtectedBlock(explodedBlock.getLocation()));
            
            // 记录爆炸影响的方块
            for (Block explodedBlock : event.blockList()) {
                dungeonInstance.recordBlockChange(explodedBlock.getLocation(), explodedBlock.getType(), null);
            }
            
        } catch (Exception e) {
            logger.severe("处理方块爆炸事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理方块燃烧事件
     * 
     * @param event 方块燃烧事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否允许方块燃烧
            if (!dungeonInstance.getSettings().isAllowBlockBurning()) {
                event.setCancelled(true);
                return;
            }
            
            // 检查是否是受保护的方块
            if (dungeonInstance.isProtectedBlock(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
            
            // 记录方块燃烧
            dungeonInstance.recordBlockChange(block.getLocation(), block.getType(), null);
            
        } catch (Exception e) {
            logger.severe("处理方块燃烧事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理方块生长事件
     * 
     * @param event 方块生长事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        
        // 检查方块是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(block.getLocation());
        if (dungeonInstance == null) {
            return;
        }
        
        try {
            // 检查是否允许方块生长
            if (!dungeonInstance.getSettings().isAllowBlockGrowth()) {
                event.setCancelled(true);
                return;
            }
            
            // 记录方块生长
            dungeonInstance.recordBlockChange(block.getLocation(), null, event.getNewState().getType());
            
        } catch (Exception e) {
            logger.severe("处理方块生长事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 处理触发器结果
     * 
     * @param player 玩家（可能为null）
     * @param results 触发器结果列表
     */
    private void handleTriggerResults(Player player, List<TriggerResult> results) {
        for (TriggerResult result : results) {
            if (result.isFailure()) {
                logger.warning("触发器执行失败: " + result.getMessage());
                
                // 如果有错误消息需要发送给玩家
                if (player != null && result.hasData("player_message")) {
                    String message = result.getData("player_message", String.class);
                    if (message != null) {
                        player.sendMessage("§c" + message);
                    }
                }
            } else {
                // 成功的触发器可能有成功消息
                if (player != null && result.hasData("player_message")) {
                    String message = result.getData("player_message", String.class);
                    if (message != null) {
                        player.sendMessage("§a" + message);
                    }
                }
            }
        }
    }
}
