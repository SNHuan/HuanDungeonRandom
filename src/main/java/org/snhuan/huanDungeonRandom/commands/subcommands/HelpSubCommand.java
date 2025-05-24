package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.HuanDungeonCommand;
import org.snhuan.huanDungeonRandom.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * 帮助子命令 - 显示命令帮助信息
 *
 * 支持的操作：
 * - 无参数 - 显示主帮助
 * - <子命令> - 显示指定子命令的帮助
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class HelpSubCommand extends SubCommand {

    private final HuanDungeonCommand mainCommand;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     * @param mainCommand 主命令实例
     */
    public HelpSubCommand(JavaPlugin plugin, HuanDungeonCommand mainCommand) {
        super(plugin, "help", null, false); // 帮助命令不需要特殊权限
        this.mainCommand = mainCommand;

        // 添加别名
        addAlias("h");
        addAlias("?");
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
            // 显示主帮助
            showMainHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = mainCommand.getSubCommand(subCommandName);

        if (subCommand == null) {
            sendMessage(sender, "§c未找到子命令: " + subCommandName);
            sendMessage(sender, "§e使用 §f/hdr help §e查看所有可用命令");
            return true;
        }

        // 检查权限
        if (!subCommand.hasPermission(sender)) {
            sendMessage(sender, "§c您没有权限查看此命令的帮助！");
            return true;
        }

        // 显示子命令帮助
        showSubCommandHelp(sender, subCommand);
        return true;
    }

    /**
     * 显示子命令帮助信息
     *
     * @param sender 命令发送者
     * @param subCommand 子命令
     */
    private void showSubCommandHelp(CommandSender sender, SubCommand subCommand) {
        sendMessage(sender, "§6=== " + subCommand.getName() + " 命令帮助 ===");
        List<String> helpLines = subCommand.getPublicHelpLines();
        for (String line : helpLines) {
            sendMessage(sender, line);
        }
    }

    /**
     * 显示主帮助信息
     *
     * @param sender 命令发送者
     */
    private void showMainHelp(CommandSender sender) {
        sendMessages(sender,
            "§6=== HuanDungeonRandom 命令帮助 ===",
            "§7插件版本: §f" + plugin.getDescription().getVersion(),
            "§7作者: §f" + String.join(", ", plugin.getDescription().getAuthors()),
            "",
            "§e主要命令:",
            "§f/hdr dungeon §7- 地牢管理命令",
            "§f/hdr trigger §7- 触发器管理命令",
            "§f/hdr function §7- 功能管理命令",
            "§f/hdr blueprint §7- 蓝图管理命令",
            "§f/hdr reload §7- 重载插件配置",
            "",
            "§e获取详细帮助:",
            "§f/hdr help <子命令> §7- 查看具体命令的详细帮助",
            "§7例如: §f/hdr help dungeon",
            "",
            "§e权限说明:",
            "§7- §fhuandungeon.admin §7- 管理员权限（包含所有功能）",
            "§7- §fhuandungeon.user §7- 普通用户权限（基础功能）",
            "",
            "§e支持与反馈:",
            "§7如有问题请联系服务器管理员"
        );
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
            // 返回用户有权限查看的子命令列表
            return filterCompletions(args[0], mainCommand.getAvailableSubCommands(sender));
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
        lines.add("§e/hdr help §7- 显示主帮助信息");
        lines.add("§e/hdr help <子命令> §7- 显示指定子命令的帮助");
        lines.add("§7可用的子命令: dungeon, trigger, function, blueprint, reload");
        return lines;
    }
}
