package org.snhuan.huanDungeonRandom.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.List;
import java.util.logging.Logger;

/**
 * 玩家事件监听器 - 监听玩家相关事件并触发相应的触发器
 *
 * 监听的事件类型：
 * - 玩家移动事件（PlayerMoveEvent）
 * - 玩家交互事件（PlayerInteractEvent）
 * - 玩家加入/离开事件（PlayerJoinEvent/PlayerQuitEvent）
 * - 玩家传送事件（PlayerTeleportEvent）
 * - 玩家聊天事件（AsyncPlayerChatEvent）
 * - 玩家死亡事件（PlayerDeathEvent）
 * - 玩家重生事件（PlayerRespawnEvent）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class PlayerListener implements Listener {

    private static final Logger logger = Logger.getLogger("HuanDungeonRandom");

    private final DungeonManager dungeonManager;
    private final TriggerManager triggerManager;

    /**
     * 构造函数
     *
     * @param dungeonManager 地牢管理器
     * @param triggerManager 触发器管理器
     */
    public PlayerListener(DungeonManager dungeonManager, TriggerManager triggerManager) {
        this.dungeonManager = dungeonManager;
        this.triggerManager = triggerManager;
    }

    /**
     * 处理玩家移动事件
     *
     * @param event 玩家移动事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);

            // 处理触发结果
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理玩家移动事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家交互事件
     *
     * @param event 玩家交互事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 触发相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);

            // 处理触发结果
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理玩家交互事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家加入事件
     *
     * @param event 玩家加入事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            // 检查玩家是否有未完成的地牢
            DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
            if (dungeonInstance != null) {
                // 重新加入地牢
                dungeonManager.rejoinDungeon(player, dungeonInstance);
                player.sendMessage("§a欢迎回来！您已重新加入地牢: " + dungeonInstance.getId());
            }

        } catch (Exception e) {
            logger.severe("处理玩家加入事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家离开事件
     *
     * @param event 玩家离开事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        try {
            // 检查玩家是否在地牢中
            DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
            if (dungeonInstance != null) {
                // 处理玩家离开地牢
                dungeonManager.handlePlayerLeave(player, dungeonInstance);
            }

        } catch (Exception e) {
            logger.severe("处理玩家离开事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家传送事件
     *
     * @param event 玩家传送事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        try {
            // 检查玩家是否从地牢中传送出去
            DungeonInstance fromDungeon = dungeonManager.getPlayerDungeon(player);
            if (fromDungeon != null) {
                // 检查传送目标是否还在同一个地牢中
                boolean stillInDungeon = fromDungeon.isLocationInDungeon(event.getTo());

                if (!stillInDungeon) {
                    // 玩家传送出地牢
                    dungeonManager.handlePlayerLeave(player, fromDungeon);
                    player.sendMessage("§e您已离开地牢: " + fromDungeon.getId());
                }
            }

            // 检查玩家是否传送到地牢中
            DungeonInstance toDungeon = dungeonManager.getDungeonAtLocation(event.getTo());
            if (toDungeon != null && !toDungeon.equals(fromDungeon)) {
                // 玩家传送进入地牢
                dungeonManager.handlePlayerEnter(player, toDungeon);
                player.sendMessage("§a您已进入地牢: " + toDungeon.getId());
            }

        } catch (Exception e) {
            logger.severe("处理玩家传送事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家聊天事件
     *
     * @param event 玩家聊天事件
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 限制聊天范围到地牢内的玩家
            if (dungeonInstance.getSettings().isIsolateChatInDungeon()) {
                event.getRecipients().clear();
                event.getRecipients().addAll(dungeonInstance.getPlayers());

                // 添加地牢聊天前缀
                String dungeonPrefix = "§7[§6" + dungeonInstance.getId() + "§7] ";
                event.setFormat(dungeonPrefix + event.getFormat());
            }

        } catch (Exception e) {
            logger.severe("处理玩家聊天事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家死亡事件
     *
     * @param event 玩家死亡事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 检查玩家是否在地牢中
        DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
        if (dungeonInstance == null) {
            return;
        }

        try {
            // 处理地牢内死亡
            dungeonManager.handlePlayerDeath(player, dungeonInstance);

            // 触发死亡相关触发器
            List<TriggerResult> results = triggerManager.handleEvent(event, player, dungeonInstance);
            handleTriggerResults(player, results);

        } catch (Exception e) {
            logger.severe("处理玩家死亡事件时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理玩家重生事件
     *
     * @param event 玩家重生事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        try {
            // 检查玩家是否应该在地牢中重生
            DungeonInstance dungeonInstance = dungeonManager.getPlayerDungeon(player);
            if (dungeonInstance != null) {
                // 设置重生位置为地牢内的安全位置
                event.setRespawnLocation(dungeonInstance.getSafeRespawnLocation());

                // 延迟处理重生后的逻辑
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.Bukkit.getPluginManager().getPlugin("HuanDungeonRandom"),
                    () -> {
                        dungeonManager.handlePlayerRespawn(player, dungeonInstance);
                        player.sendMessage("§a您已在地牢中重生: " + dungeonInstance.getId());
                    },
                    1L // 1 tick 延迟
                );
            }

        } catch (Exception e) {
            logger.severe("处理玩家重生事件时发生异常: " + e.getMessage());
            e.printStackTrace();
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
