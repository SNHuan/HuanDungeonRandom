package org.snhuan.huanDungeonRandom.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.commands.subcommands.*;
import org.snhuan.huanDungeonRandom.core.DungeonManager;
import org.snhuan.huanDungeonRandom.function.FunctionManager;
import org.snhuan.huanDungeonRandom.trigger.TriggerManager;

import java.util.*;

/**
 * 主命令处理器 - 处理 /hdr 命令
 *
 * 子命令结构：
 * - /hdr dungeon <子命令> - 地牢管理
 * - /hdr trigger <子命令> - 触发器管理
 * - /hdr function <子命令> - 功能管理
 * - /hdr blueprint <子命令> - 蓝图管理
 * - /hdr reload - 重载配置
 * - /hdr help - 显示帮助
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class HuanDungeonCommand extends BaseCommand {
    
    private final Map<String, SubCommand> subCommands;
    private final DungeonManager dungeonManager;
    private final TriggerManager triggerManager;
    private final FunctionManager functionManager;
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     * @param dungeonManager 地牢管理器
     * @param triggerManager 触发器管理器
     * @param functionManager 功能管理器
     */
    public HuanDungeonCommand(JavaPlugin plugin, DungeonManager dungeonManager, 
                             TriggerManager triggerManager, FunctionManager functionManager) {
        super(plugin, "hdr", "huandungeon.admin", false);
        
        this.dungeonManager = dungeonManager;
        this.triggerManager = triggerManager;
        this.functionManager = functionManager;
        this.subCommands = new HashMap<>();
        
        registerSubCommands();
    }
    
    /**
     * 注册子命令
     */
    private void registerSubCommands() {
        // 地牢管理子命令
        registerSubCommand(new DungeonSubCommand(plugin, dungeonManager));
        
        // 触发器管理子命令
        registerSubCommand(new TriggerSubCommand(plugin, triggerManager));
        
        // 功能管理子命令
        registerSubCommand(new FunctionSubCommand(plugin, functionManager));
        
        // 蓝图管理子命令
        registerSubCommand(new BlueprintSubCommand(plugin));
        
        // 重载子命令
        registerSubCommand(new ReloadSubCommand(plugin));
        
        // 帮助子命令
        registerSubCommand(new HelpSubCommand(plugin, this));
    }
    
    /**
     * 注册子命令
     * 
     * @param subCommand 子命令
     */
    private void registerSubCommand(SubCommand subCommand) {
        subCommands.put(subCommand.getName().toLowerCase(), subCommand);
        
        // 注册别名
        for (String alias : subCommand.getAliases()) {
            subCommands.put(alias.toLowerCase(), subCommand);
        }
    }
    
    /**
     * 执行命令
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param label 命令标签
     * @param args 命令参数
     * @return 是否处理成功
     */
    @Override
    protected boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        // 如果没有参数，显示帮助
        if (args.length == 0) {
            sendMainHelp(sender);
            return true;
        }
        
        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        
        if (subCommand == null) {
            sendMessage(sender, "§c未知的子命令: " + args[0]);
            sendMessage(sender, "§e使用 §f/hdr help §e查看可用命令");
            return true;
        }
        
        // 检查子命令权限
        if (!subCommand.hasPermission(sender)) {
            sendMessage(sender, "§c您没有权限执行此命令！");
            return true;
        }
        
        // 检查是否需要玩家执行
        if (subCommand.requiresPlayer() && !(sender instanceof Player)) {
            sendMessage(sender, "§c此命令只能由玩家执行！");
            return true;
        }
        
        // 执行子命令
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCommand.execute(sender, subArgs);
    }
    
    /**
     * 获取Tab补全
     * 
     * @param sender 命令发送者
     * @param command 命令对象
     * @param alias 命令别名
     * @param args 命令参数
     * @return 补全列表
     */
    @Override
    protected List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // 第一个参数：子命令名称
            List<String> availableCommands = new ArrayList<>();
            
            for (SubCommand subCommand : subCommands.values()) {
                if (subCommand.hasPermission(sender)) {
                    availableCommands.add(subCommand.getName());
                }
            }
            
            return filterCompletions(args[0], availableCommands);
        }
        
        if (args.length > 1) {
            // 后续参数：委托给子命令
            String subCommandName = args[0].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandName);
            
            if (subCommand != null && subCommand.hasPermission(sender)) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subCommand.getTabCompletions(sender, subArgs);
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
        lines.add("§e/hdr help §7- 显示此帮助信息");
        lines.add("§e/hdr dungeon §7- 地牢管理命令");
        lines.add("§e/hdr trigger §7- 触发器管理命令");
        lines.add("§e/hdr function §7- 功能管理命令");
        lines.add("§e/hdr blueprint §7- 蓝图管理命令");
        lines.add("§e/hdr reload §7- 重载插件配置");
        return lines;
    }
    
    /**
     * 发送主帮助信息
     * 
     * @param sender 命令发送者
     */
    private void sendMainHelp(CommandSender sender) {
        sendMessages(sender,
            "§6=== HuanDungeonRandom 命令帮助 ===",
            "§e/hdr help §7- 显示此帮助信息",
            "§e/hdr dungeon §7- 地牢管理命令",
            "§e/hdr trigger §7- 触发器管理命令", 
            "§e/hdr function §7- 功能管理命令",
            "§e/hdr blueprint §7- 蓝图管理命令",
            "§e/hdr reload §7- 重载插件配置",
            "§7使用 §f/hdr <子命令> help §7查看具体命令帮助"
        );
    }
    
    /**
     * 获取所有子命令
     * 
     * @return 子命令映射
     */
    public Map<String, SubCommand> getSubCommands() {
        return new HashMap<>(subCommands);
    }
    
    /**
     * 获取子命令
     * 
     * @param name 子命令名称
     * @return 子命令实例
     */
    public SubCommand getSubCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
    
    /**
     * 获取可用的子命令名称列表
     * 
     * @param sender 命令发送者
     * @return 可用命令列表
     */
    public List<String> getAvailableSubCommands(CommandSender sender) {
        List<String> available = new ArrayList<>();
        Set<String> added = new HashSet<>();
        
        for (SubCommand subCommand : subCommands.values()) {
            if (subCommand.hasPermission(sender) && !added.contains(subCommand.getName())) {
                available.add(subCommand.getName());
                added.add(subCommand.getName());
            }
        }
        
        return available;
    }
}
