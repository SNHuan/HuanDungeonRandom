package org.snhuan.huanDungeonRandom.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.List;
import java.util.logging.Logger;

/**
 * 地牢事件监听器 - 监听地牢相关的特殊事件
 *
 * 监听的事件类型：
 * - 实体伤害事件（EntityDamageEvent）
 * - 实体死亡事件（EntityDeathEvent）
 * - 实体生成事件（EntitySpawnEvent）
 * - 物品栏事件（InventoryClickEvent/OpenEvent/CloseEvent）
 * - 区块加载/卸载事件（ChunkLoadEvent/ChunkUnloadEvent）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonListener implements Listener {

    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");

    private final DungeonManager dungeonManager;
    private final TriggerManager triggerManager;

    /**
     * 构造函数
     *
     * @param dungeonManager 地牢管理器
     * @param triggerManager 触发器管理器
     */
    public DungeonListener(DungeonManager dungeonManager, TriggerManager triggerManager) {
        this.dungeonManager = dungeonManager;
        this.triggerManager = triggerManager;
    }

    /**
     * 处理实体伤害事件
     *
     * @param event 实体伤害事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        // 检查实体是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(event.getEntity().getLocation());
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 如果是玩家受伤
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();

                // 检查是否启用PvP
                if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                    !dungeonInstance.getSettings().isPvpEnabled()) {
                    event.setCancelled(true);
                    return;
                }

                // 检查是否启用环境伤害
                if (!dungeonInstance.getSettings().isEnvironmentalDamageEnabled() &&
                    isEnvironmentalDamage(event.getCause())) {
                    event.setCancelled(true);
                    return;
                }

                // 触发相关触发器
                List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
                handleTriggerResults(player, results);
            }

        } catch (Exception e) {
            logger.severe("处理实体伤害事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理实体死亡事件
     *
     * @param event 实体死亡事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // 检查实体是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(event.getEntity().getLocation());
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 如果是玩家死亡，在PlayerListener中已经处理
            if (event.getEntity() instanceof Player) {
                return;
            }

            // 处理怪物死亡
            dungeonInstance.handleEntityDeath(event.getEntity());

            // 触发相关触发器
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                List<TriggerResult> results = triggerManager.handleEvent(event, killer, dungeonInstance);
                handleTriggerResults(killer, results);
            }

        } catch (Exception e) {
            logger.severe("处理实体死亡事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理实体生成事件
     *
     * @param event 实体生成事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        // 检查实体是否在地牢中生成
        DungeonInstance dungeonInstance = dungeonManager.getDungeonAtLocation(event.getLocation());
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 检查是否允许自然生成
            if (!dungeonInstance.getSettings().isAllowNaturalSpawning()) {
                event.setCancelled(true);
                return;
            }

            // 记录实体生成
            dungeonInstance.handleEntitySpawn(event.getEntity());

        } catch (Exception e) {
            logger.severe("处理实体生成事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理物品栏点击事件
     *
     * @param event 物品栏点击事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 检查是否限制物品栏操作
            if (dungeonInstance.getSettings().isRestrictInventoryAccess()) {
                // 只允许访问玩家自己的物品栏
                if (!event.getInventory().equals(player.getInventory())) {
                    event.setCancelled(true);
                    player.sendMessage("§c在此地牢中不允许访问其他容器！");
                    return;
                }
            }

            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理物品栏点击事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理物品栏打开事件
     *
     * @param event 物品栏打开事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理物品栏打开事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理物品栏关闭事件
     *
     * @param event 物品栏关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理物品栏关闭事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理区块加载事件
     *
     * @param event 区块加载事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        try {
            // 检查区块是否包含地牢
            List<DungeonInstance> dungeons = dungeonManager.getDungeonsInChunk(event.getChunk());

            for (DungeonInstance dungeon : dungeons) {
                dungeon.handleChunkLoad(event.getChunk());
            }

        } catch (Exception e) {
            logger.severe("处理区块加载事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理区块卸载事件
     *
     * @param event 区块卸载事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        try {
            // 检查区块是否包含活跃的地牢
            List<DungeonInstance> dungeons = dungeonManager.getDungeonsInChunk(event.getChunk());

            for (DungeonInstance dungeon : dungeons) {
                if (dungeon.hasActivePlayers()) {
                    // 记录有活跃玩家的地牢区块卸载（新版本无法阻止）
                    logger.warning("包含活跃玩家的地牢区块被卸载: " + dungeon.getId());
                    // 可以考虑强制加载区块或其他处理方式
                } else {
                    dungeon.handleChunkUnload(event.getChunk());
                }
            }

        } catch (Exception e) {
            logger.severe("处理区块卸载事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查是否是环境伤害
     *
     * @param cause 伤害原因
     * @return 是否是环境伤害
     */
    private boolean isEnvironmentalDamage(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case FALL:
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case DROWNING:
            case SUFFOCATION:
            case STARVATION:
            case POISON:
            case WITHER:
            case VOID:
            case LIGHTNING:
            case FREEZE:
            case HOT_FLOOR:
                return true;
            default:
                return false;
        }
    }

    /**
     * 处理触发器结果
     *
     * @param player 玩家
     * @param results 触发器结果列表
     */
    private void handleTriggerResults(Player player, List<TriggerResult> results) {
        for (TriggerResult result : results) {
            if (result.isFailure()) {
                logger.warning("触发器执行失败: " + result.getMessage());

                // 如果有错误消息需要发送给玩家
                if (result.hasData("player_message")) {
                    String message = result.getData("player_message", String.class);
                    if (message != null) {
                        player.sendMessage("§c" + message);
                    }
                }
            } else {
                // 成功的触发器可能有成功消息
                if (result.hasData("player_message")) {
                    String message = result.getData("player_message", String.class);
                    if (message != null) {
                        player.sendMessage("§a" + message);
                    }
                }
            }
        }
    }
}
