package org.snhuan.huanDungeonRandom.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * 命令基类 - 提供命令处理的通用功能
 *
 * 功能特性：
 * - 权限检查
 * - 参数验证
 * - 错误处理
 * - 消息发送
 * - Tab补全支持
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    
    protected static final Logger logger = Logger.getLogger("HuanDungeonRandom");
    
    protected final JavaPlugin plugin;
    protected final String commandName;
    protected final String permission;
    protected final boolean requirePlayer;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param commandName 命令名称
     * @param permission 所需权限
     * @param requirePlayer 是否需要玩家执行
     */
    public BaseCommand(JavaPlugin plugin, String commandName, String permission, boolean requirePlayer) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.permission = permission;
        this.requirePlayer = requirePlayer;
    }
    
    /**
     * 命令执行入口
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 是否处理成功
     */
    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            // 检查是否需要玩家执行
            if (requirePlayer && !(sender instanceof Player)) {
                sendMessage(sender, "§c此命令只能由玩家执行！");
                return true;
            }
            
            // 检查权限
            if (permission != null && !sender.hasPermission(permission)) {
                sendMessage(sender, "§c您没有权限执行此命令！");
                return true;
            }
            
            // 执行具体命令逻辑
            return executeCommand(sender, command, label, args);
            
        } catch (Exception e) {
            logger.severe("执行命令时发生异常: " + commandName + " - " + e.getMessage());
            e.printStackTrace();
            sendMessage(sender, "§c命令执行时发生错误，请查看控制台日志！");
            return true;
        }
    }
    
    /**
     * Tab补全入口
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param alias 命令别名
     * @param args 命令参数
     * @return 补全列表
     */
    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            // 检查权限
            if (permission != null && !sender.hasPermission(permission)) {
                return new ArrayList<>();
            }
            
            return getTabCompletions(sender, command, alias, args);
            
        } catch (Exception e) {
            logger.warning("Tab补全时发生异常: " + commandName + " - " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 执行具体命令逻辑（由子类实现）
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 是否处理成功
     */
    protected abstract boolean executeCommand(CommandSender sender, Command command, String label, String[] args);
    
    /**
     * 获取Tab补全列表（由子类实现）
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param alias 命令别名
     * @param args 命令参数
     * @return 补全列表
     */
    protected abstract List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args);
    
    /**
     * 发送消息给命令发送者
     * 
     * @param sender 命令发送者
     * @param message 消息内容
     */
    protected void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(message);
        }
    }
    
    /**
     * 发送多行消息
     * 
     * @param sender 命令发送者
     * @param messages 消息列表
     */
    protected void sendMessages(CommandSender sender, String... messages) {
        if (sender != null && messages != null) {
            for (String message : messages) {
                if (message != null) {
                    sender.sendMessage(message);
                }
            }
        }
    }
    
    /**
     * 发送帮助信息
     * 
     * @param sender 命令发送者
     */
    protected void sendHelp(CommandSender sender) {
        sendMessage(sender, "§6=== " + commandName + " 命令帮助 ===");
        List<String> helpLines = getHelpLines();
        for (String line : helpLines) {
            sendMessage(sender, line);
        }
    }
    
    /**
     * 获取帮助信息行（由子类实现）
     * 
     * @return 帮助信息列表
     */
    protected abstract List<String> getHelpLines();
    
    /**
     * 检查参数数量
     * 
     * @param args 参数数组
     * @param minArgs 最小参数数量
     * @param maxArgs 最大参数数量（-1表示无限制）
     * @return 是否符合要求
     */
    protected boolean checkArgs(String[] args, int minArgs, int maxArgs) {
        if (args.length < minArgs) {
            return false;
        }
        if (maxArgs >= 0 && args.length > maxArgs) {
            return false;
        }
        return true;
    }
    
    /**
     * 获取玩家对象
     * 
     * @param sender 命令发送者
     * @return 玩家对象，如果不是玩家返回null
     */
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }
    
    /**
     * 过滤Tab补全列表
     * 
     * @param input 输入内容
     * @param options 选项列表
     * @return 过滤后的列表
     */
    protected List<String> filterCompletions(String input, List<String> options) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>(options);
        }
        
        List<String> filtered = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        
        for (String option : options) {
            if (option != null && option.toLowerCase().startsWith(lowerInput)) {
                filtered.add(option);
            }
        }
        
        return filtered;
    }
    
    /**
     * 过滤Tab补全列表（数组版本）
     * 
     * @param input 输入内容
     * @param options 选项数组
     * @return 过滤后的列表
     */
    protected List<String> filterCompletions(String input, String... options) {
        return filterCompletions(input, Arrays.asList(options));
    }
    
    /**
     * 检查字符串是否为数字
     * 
     * @param str 字符串
     * @return 是否为数字
     */
    protected boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 安全解析整数
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 解析结果
     */
    protected int parseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 安全解析双精度浮点数
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 解析结果
     */
    protected double parseDouble(String str, double defaultValue) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    // ==================== Getter 方法 ====================
    
    public String getCommandName() {
        return commandName;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public boolean isRequirePlayer() {
        return requirePlayer;
    }
}
