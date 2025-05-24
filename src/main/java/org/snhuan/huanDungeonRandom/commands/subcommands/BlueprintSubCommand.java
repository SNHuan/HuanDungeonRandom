package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * 蓝图管理子命令 - 处理蓝图相关操作
 *
 * 支持的操作：
 * - list - 列出所有蓝图
 * - info <id> - 查看蓝图信息
 * - load <file> - 加载蓝图文件
 * - reload - 重载所有蓝图
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class BlueprintSubCommand extends SubCommand {
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     */
    public BlueprintSubCommand(JavaPlugin plugin) {
        super(plugin, "blueprint", "huandungeon.admin.blueprint", false);
        
        // 添加别名
        addAlias("bp");
        addAlias("blue");
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
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "load":
                return handleLoad(sender, args);
            case "reload":
                return handleReload(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "§c未知的操作: " + action);
                sendMessage(sender, "§e使用 §f/hdr blueprint help §e查看可用操作");
                return true;
        }
    }
    
    /**
     * 处理列出蓝图命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleList(CommandSender sender, String[] args) {
        sendMessage(sender, "§6=== 蓝图列表 ===");
        sendMessage(sender, "§e蓝图管理功能正在开发中...");
        return true;
    }
    
    /**
     * 处理查看蓝图信息命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr blueprint info <蓝图ID>");
            return true;
        }
        
        String blueprintId = args[1];
        sendMessage(sender, "§6=== 蓝图信息: " + blueprintId + " ===");
        sendMessage(sender, "§e蓝图信息查看功能正在开发中...");
        return true;
    }
    
    /**
     * 处理加载蓝图命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleLoad(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr blueprint load <文件名>");
            return true;
        }
        
        String fileName = args[1];
        sendMessage(sender, "§a尝试加载蓝图文件: " + fileName);
        sendMessage(sender, "§e蓝图加载功能正在开发中...");
        return true;
    }
    
    /**
     * 处理重载蓝图命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleReload(CommandSender sender, String[] args) {
        sendMessage(sender, "§a正在重载所有蓝图...");
        sendMessage(sender, "§e蓝图重载功能正在开发中...");
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
            return filterCompletions(args[0], "list", "info", "load", "reload", "help");
        }
        
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            
            switch (action) {
                case "info":
                    // 这里应该返回实际的蓝图ID列表
                    return filterCompletions(args[1], "room1", "corridor1", "entrance1");
                case "load":
                    // 这里应该返回蓝图文件列表
                    return filterCompletions(args[1], "room.yml", "corridor.yml", "entrance.yml");
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
        lines.add("§e/hdr blueprint list §7- 列出所有已加载的蓝图");
        lines.add("§e/hdr blueprint info <ID> §7- 查看蓝图详细信息");
        lines.add("§e/hdr blueprint load <文件> §7- 加载指定蓝图文件");
        lines.add("§e/hdr blueprint reload §7- 重载所有蓝图文件");
        return lines;
    }
}
