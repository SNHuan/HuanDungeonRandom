package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.SubCommand;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 触发器管理子命令 - 处理触发器相关操作
 *
 * 支持的操作：
 * - list - 列出所有触发器
 * - info <id> - 查看触发器信息
 * - manual <id> - 手动触发
 * - enable <id> - 启用触发器
 * - disable <id> - 禁用触发器
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TriggerSubCommand extends SubCommand {
    
    private final TriggerManager triggerManager;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param triggerManager 触发器管理器
     */
    public TriggerSubCommand(JavaPlugin plugin, TriggerManager triggerManager) {
        super(plugin, "trigger", "huandungeon.admin.trigger", false);
        this.triggerManager = triggerManager;
        
        // 添加别名
        addAlias("t");
        addAlias("trig");
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
            case "manual":
                return handleManual(sender, args);
            case "enable":
                return handleEnable(sender, args);
            case "disable":
                return handleDisable(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "§c未知的操作: " + action);
                sendMessage(sender, "§e使用 §f/hdr trigger help §e查看可用操作");
                return true;
        }
    }
    
    /**
     * 处理列出触发器命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleList(CommandSender sender, String[] args) {
        sendMessage(sender, "§6=== 触发器列表 ===");
        sendMessage(sender, "§e触发器管理功能正在开发中...");
        return true;
    }
    
    /**
     * 处理查看触发器信息命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr trigger info <触发器ID>");
            return true;
        }
        
        String triggerId = args[1];
        sendMessage(sender, "§6=== 触发器信息: " + triggerId + " ===");
        sendMessage(sender, "§e触发器信息查看功能正在开发中...");
        return true;
    }
    
    /**
     * 处理手动触发命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleManual(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr trigger manual <触发器ID>");
            return true;
        }
        
        String triggerId = args[1];
        sendMessage(sender, "§a尝试手动触发: " + triggerId);
        sendMessage(sender, "§e手动触发功能正在开发中...");
        return true;
    }
    
    /**
     * 处理启用触发器命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr trigger enable <触发器ID>");
            return true;
        }
        
        String triggerId = args[1];
        sendMessage(sender, "§a已启用触发器: " + triggerId);
        return true;
    }
    
    /**
     * 处理禁用触发器命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleDisable(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr trigger disable <触发器ID>");
            return true;
        }
        
        String triggerId = args[1];
        sendMessage(sender, "§c已禁用触发器: " + triggerId);
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
            return filterCompletions(args[0], "list", "info", "manual", "enable", "disable", "help");
        }
        
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            
            switch (action) {
                case "info":
                case "manual":
                case "enable":
                case "disable":
                    // 这里应该返回实际的触发器ID列表
                    return filterCompletions(args[1], "trigger1", "trigger2", "trigger3");
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
        lines.add("§e/hdr trigger list §7- 列出所有触发器");
        lines.add("§e/hdr trigger info <ID> §7- 查看触发器详细信息");
        lines.add("§e/hdr trigger manual <ID> §7- 手动触发指定触发器");
        lines.add("§e/hdr trigger enable <ID> §7- 启用指定触发器");
        lines.add("§e/hdr trigger disable <ID> §7- 禁用指定触发器");
        return lines;
    }
}
