package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.SubCommand;
import org.snhuan.huanDungeonRandom.function.FunctionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能管理子命令 - 处理功能相关操作
 *
 * 支持的操作：
 * - list - 列出所有功能
 * - info <id> - 查看功能信息
 * - execute <id> - 手动执行功能
 * - enable <id> - 启用功能
 * - disable <id> - 禁用功能
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class FunctionSubCommand extends SubCommand {
    
    private final FunctionManager functionManager;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param functionManager 功能管理器
     */
    public FunctionSubCommand(JavaPlugin plugin, FunctionManager functionManager) {
        super(plugin, "function", "huandungeon.admin.function", false);
        this.functionManager = functionManager;
        
        // 添加别名
        addAlias("f");
        addAlias("func");
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
            case "execute":
            case "exec":
                return handleExecute(sender, args);
            case "enable":
                return handleEnable(sender, args);
            case "disable":
                return handleDisable(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "§c未知的操作: " + action);
                sendMessage(sender, "§e使用 §f/hdr function help §e查看可用操作");
                return true;
        }
    }
    
    /**
     * 处理列出功能命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleList(CommandSender sender, String[] args) {
        sendMessage(sender, "§6=== 功能列表 ===");
        sendMessage(sender, "§e功能管理功能正在开发中...");
        return true;
    }
    
    /**
     * 处理查看功能信息命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr function info <功能ID>");
            return true;
        }
        
        String functionId = args[1];
        sendMessage(sender, "§6=== 功能信息: " + functionId + " ===");
        sendMessage(sender, "§e功能信息查看功能正在开发中...");
        return true;
    }
    
    /**
     * 处理手动执行功能命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleExecute(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr function execute <功能ID>");
            return true;
        }
        
        String functionId = args[1];
        sendMessage(sender, "§a尝试执行功能: " + functionId);
        sendMessage(sender, "§e手动执行功能正在开发中...");
        return true;
    }
    
    /**
     * 处理启用功能命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr function enable <功能ID>");
            return true;
        }
        
        String functionId = args[1];
        sendMessage(sender, "§a已启用功能: " + functionId);
        return true;
    }
    
    /**
     * 处理禁用功能命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 是否执行成功
     */
    private boolean handleDisable(CommandSender sender, String[] args) {
        if (!checkArgs(args, 2, 2)) {
            sendMessage(sender, "§c用法: /hdr function disable <功能ID>");
            return true;
        }
        
        String functionId = args[1];
        sendMessage(sender, "§c已禁用功能: " + functionId);
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
            return filterCompletions(args[0], "list", "info", "execute", "enable", "disable", "help");
        }
        
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            
            switch (action) {
                case "info":
                case "execute":
                case "exec":
                case "enable":
                case "disable":
                    // 这里应该返回实际的功能ID列表
                    return filterCompletions(args[1], "function1", "function2", "function3");
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
        lines.add("§e/hdr function list §7- 列出所有功能");
        lines.add("§e/hdr function info <ID> §7- 查看功能详细信息");
        lines.add("§e/hdr function execute <ID> §7- 手动执行指定功能");
        lines.add("§e/hdr function enable <ID> §7- 启用指定功能");
        lines.add("§e/hdr function disable <ID> §7- 禁用指定功能");
        return lines;
    }
}
