package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.SubCommand;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地牢管理子命令 - 处理地牢相关操作
 *
 * 支持的操作：
 * - create <theme> - 创建地牢
 * - list - 列出所有地牢
 * - info <id> - 查看地牢信息
 * - tp <id> - 传送到地牢
 * - destroy <id> - 销毁地牢
 * - join <id> - 加入地牢
 * - leave - 离开当前地牢
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonSubCommand extends SubCommand {

    private final DungeonManager dungeonManager;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     * @param dungeonManager 地牢管理器
     */
    public DungeonSubCommand(JavaPlugin plugin, DungeonManager dungeonManager) {
        super(plugin, "dungeon", "huandungeon.admin.dungeon", false);
        this.dungeonManager = dungeonManager;

        // 添加别名
        addAlias("d");
        addAlias("dg");
    }

    /**
     * 执行子命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    @Override
    protected boolean executeSubCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "create":
                return handleCreate(sender, args);
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "destroy":
            case "delete":
                return handleDestroy(sender, args);
            case "join":
                return handleJoin(sender, args);
            case "leave":
            case "exit":
                return handleLeave(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "§c未知的操作: " + action);
                sendMessage(sender, "§e使用 §f/hdr dungeon help §e查看可用操作");
                return true;
        }
    }

    /**
     * 处理创建地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家执行！");
            return true;
        }

        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr dungeon create <主题>");
            return true;
        }

        Player player = (Player) sender;
        String themeName = args[1];

        // 这里应该从配置或注册表中获取主题
        // 暂时创建一个简单的主题
        DungeonTheme theme = createSimpleTheme(themeName);
        if (theme == null) {
            sendMessage(sender, "§c未找到主题: " + themeName);
            return true;
        }

        Location location = player.getLocation();
        DungeonInstance dungeon = dungeonManager.createDungeon(
            "dungeon_" + themeName, theme, location, player.getUniqueId());

        if (dungeon != null) {
            sendMessage(sender, "§a成功创建地牢: " + dungeon.getInstanceId());
            sendMessage(sender, "§e位置: " + formatLocation(location));
        } else {
            sendMessage(sender, "§c创建地牢失败！");
        }

        return true;
    }

    /**
     * 处理列出地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleList(CommandSender sender, String[] args) {
        List<DungeonInstance> dungeons = new ArrayList<>(dungeonManager.getAllDungeons());

        if (dungeons.isEmpty()) {
            sendMessage(sender, "§e当前没有活跃的地牢");
            return true;
        }

        sendMessage(sender, "§6=== 活跃地牢列表 ===");
        for (DungeonInstance dungeon : dungeons) {
            String status = dungeon.getState().getCurrentState().getDisplayName();
            int playerCount = dungeon.getState().getPlayerCount();

            sendMessage(sender, String.format("§e%s §7- §f%s §7(§a%d玩家§7)",
                dungeon.getInstanceId(), status, playerCount));
        }

        sendMessage(sender, "§7总计: §f" + dungeons.size() + " §7个地牢");
        return true;
    }

    /**
     * 处理查看地牢信息命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr dungeon info <地牢ID>");
            return true;
        }

        String dungeonId = args[1];
        DungeonInstance dungeon = dungeonManager.getDungeon(dungeonId);

        if (dungeon == null) {
            sendMessage(sender, "§c未找到地牢: " + dungeonId);
            return true;
        }

        sendMessage(sender, "§6=== 地牢信息: " + dungeonId + " ===");
        sendMessage(sender, "§e状态: §f" + dungeon.getState().getCurrentState().getDisplayName());
        sendMessage(sender, "§e主题: §f" + dungeon.getTheme().getName());
        sendMessage(sender, "§e玩家数量: §f" + dungeon.getState().getPlayerCount());
        sendMessage(sender, "§e创建时间: §f" + formatTime(dungeon.getCreatedTime()));
        sendMessage(sender, "§e位置: §f" + formatLocation(dungeon.getOrigin()));

        return true;
    }

    /**
     * 处理传送到地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家执行！");
            return true;
        }

        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr dungeon tp <地牢ID>");
            return true;
        }

        Player player = (Player) sender;
        String dungeonId = args[1];
        DungeonInstance dungeon = dungeonManager.getDungeon(dungeonId);

        if (dungeon == null) {
            sendMessage(sender, "§c未找到地牢: " + dungeonId);
            return true;
        }

        Location spawnLocation = dungeon.getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            sendMessage(sender, "§a已传送到地牢: " + dungeonId);
        } else {
            sendMessage(sender, "§c无法获取地牢出生点！");
        }

        return true;
    }

    /**
     * 处理销毁地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleDestroy(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr dungeon destroy <地牢ID>");
            return true;
        }

        String dungeonId = args[1];

        if (dungeonManager.destroyDungeon(dungeonId)) {
            sendMessage(sender, "§a成功销毁地牢: " + dungeonId);
        } else {
            sendMessage(sender, "§c销毁地牢失败或地牢不存在: " + dungeonId);
        }

        return true;
    }

    /**
     * 处理加入地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家执行！");
            return true;
        }

        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr dungeon join <地牢ID>");
            return true;
        }

        Player player = (Player) sender;
        String dungeonId = args[1];
        DungeonInstance dungeon = dungeonManager.getDungeon(dungeonId);

        if (dungeon == null) {
            sendMessage(sender, "§c未找到地牢: " + dungeonId);
            return true;
        }

        dungeonManager.handlePlayerEnter(player, dungeon);
        sendMessage(sender, "§a已加入地牢: " + dungeonId);

        return true;
    }

    /**
     * 处理离开地牢命令
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleLeave(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家执行！");
            return true;
        }

        Player player = (Player) sender;
        DungeonInstance dungeon = dungeonManager.getPlayerDungeon(player);

        if (dungeon == null) {
            sendMessage(sender, "§c您当前不在任何地牢中！");
            return true;
        }

        dungeonManager.handlePlayerLeave(player, dungeon);
        sendMessage(sender, "§a已离开地牢: " + dungeon.getInstanceId());

        return true;
    }

    /**
     * 获取Tab补全列表
     *
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 补全列表
     */
    @Override
    protected List<String> getSubCommandTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletions(args[0], "create", "list", "info", "tp", "destroy", "join", "leave", "help");
        }

        if (args.length == 2) {
            String action = args[0].toLowerCase();

            switch (action) {
                case "create":
                    return filterCompletions(args[1], "forest", "cave", "castle", "desert", "ocean");
                case "info":
                case "tp":
                case "teleport":
                case "destroy":
                case "delete":
                case "join":
                    return filterCompletions(args[1], getDungeonIds());
            }
        }

        return new ArrayList<>();
    }

    /**
     * 获取帮助信息
     *
     * @return 帮助信息列表
     */
    @Override
    protected List<String> getHelpLines() {
        List<String> lines = new ArrayList<>();
        lines.add("§e/hdr dungeon create <主题> §7- 在当前位置创建地牢");
        lines.add("§e/hdr dungeon list §7- 列出所有活跃地牢");
        lines.add("§e/hdr dungeon info <ID> §7- 查看地牢详细信息");
        lines.add("§e/hdr dungeon tp <ID> §7- 传送到指定地牢");
        lines.add("§e/hdr dungeon join <ID> §7- 加入指定地牢");
        lines.add("§e/hdr dungeon leave §7- 离开当前地牢");
        lines.add("§e/hdr dungeon destroy <ID> §7- 销毁指定地牢");
        return lines;
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建简单主题（临时方法）
     *
     * @param themeName 主题名称
     * @return 主题对象
     */
    private DungeonTheme createSimpleTheme(String themeName) {
        // 这里应该从配置文件或主题注册表中获取
        // 暂时创建一个基础主题用于测试
        try {
            return DungeonTheme.builder(themeName, "测试主题: " + themeName)
                .setDescription("临时创建的测试主题")
                .setStoneTheme() // 设置默认的石质主题
                .build();
        } catch (Exception e) {
            logger.warning("创建测试主题失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取所有地牢ID
     *
     * @return 地牢ID列表
     */
    private List<String> getDungeonIds() {
        return dungeonManager.getAllDungeons().stream()
            .map(DungeonInstance::getInstanceId)
            .collect(Collectors.toList());
    }

    /**
     * 格式化位置
     *
     * @param location 位置
     * @return 格式化字符串
     */
    private String formatLocation(Location location) {
        return String.format("%s: %.1f, %.1f, %.1f",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ());
    }

    /**
     * 格式化时间
     *
     * @param timestamp 时间戳
     * @return 格式化字符串
     */
    private String formatTime(long timestamp) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
}
