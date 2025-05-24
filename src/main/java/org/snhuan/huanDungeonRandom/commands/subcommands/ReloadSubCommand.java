package org.snhuan.huanDungeonRandom.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.SubCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * 重载子命令 - 处理配置重载操作
 *
 * 支持的操作：
 * - config - 重载配置文件
 * - blueprints - 重载蓝图文件
 * - all - 重载所有内容
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ReloadSubCommand extends SubCommand {
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     */
    public ReloadSubCommand(JavaPlugin plugin) {
        super(plugin, "reload", "huandungeon.admin.reload", false);
        
        // 添加别名
        addAlias("rl");
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
        String target = args.length > 0 ? args[0].toLowerCase() : "all";
        
        switch (target) {
            case "config":
                return handleReloadConfig(sender);
            case "blueprints":
            case "blueprint":
                return handleReloadBlueprints(sender);
            case "all":
                return handleReloadAll(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendMessage(sender, "§c未知的重载目标: " + target);
                sendMessage(sender, "§e使用 §f/hdr reload help §e查看可用选项");
                return true;
        }
    }
    
    /**
     * 处理重载配置文件
     * 
     * @param sender 命令发送者
     * @return 是否执行成功
     */
    private boolean handleReloadConfig(CommandSender sender) {
        sendMessage(sender, "§a正在重载配置文件...");
        
        try {
            // 重载插件配置
            plugin.reloadConfig();
            sendMessage(sender, "§a配置文件重载完成！");
            return true;
        } catch (Exception e) {
            sendMessage(sender, "§c配置文件重载失败: " + e.getMessage());
            logger.severe("重载配置文件时发生异常: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    /**
     * 处理重载蓝图文件
     * 
     * @param sender 命令发送者
     * @return 是否执行成功
     */
    private boolean handleReloadBlueprints(CommandSender sender) {
        sendMessage(sender, "§a正在重载蓝图文件...");
        sendMessage(sender, "§e蓝图重载功能正在开发中...");
        return true;
    }
    
    /**
     * 处理重载所有内容
     * 
     * @param sender 命令发送者
     * @return 是否执行成功
     */
    private boolean handleReloadAll(CommandSender sender) {
        sendMessage(sender, "§a正在重载所有内容...");
        
        // 重载配置文件
        boolean configSuccess = handleReloadConfig(sender);
        
        // 重载蓝图文件
        sendMessage(sender, "§a正在重载蓝图文件...");
        sendMessage(sender, "§e蓝图重载功能正在开发中...");
        
        if (configSuccess) {
            sendMessage(sender, "§a所有内容重载完成！");
        } else {
            sendMessage(sender, "§c部分内容重载失败，请查看控制台日志！");
        }
        
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
            return filterCompletions(args[0], "config", "blueprints", "all", "help");
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
        lines.add("§e/hdr reload §7- 重载所有内容");
        lines.add("§e/hdr reload config §7- 重载配置文件");
        lines.add("§e/hdr reload blueprints §7- 重载蓝图文件");
        lines.add("§e/hdr reload all §7- 重载所有内容");
        return lines;
    }
}
