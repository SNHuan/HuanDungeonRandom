package org.snhuan.huanDungeonRandom.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 消息工具类 - 提供统一的消息发送和格式化功能
 * 
 * 功能包括：
 * - 格式化消息发送
 * - 标题和副标题显示
 * - 动作栏消息
 * - 声音播放
 * - 多语言支持
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class MessageUtils {
    
    private static final Logger logger = Logger.getLogger(MessageUtils.class.getName());
    private static JavaPlugin plugin;
    private static final Map<String, String> messages = new HashMap<>();
    
    // 消息前缀
    private static final String PREFIX = "§8[§6HuanDungeon§8] §r";
    private static final String ERROR_PREFIX = "§8[§cHuanDungeon§8] §c";
    private static final String SUCCESS_PREFIX = "§8[§aHuanDungeon§8] §a";
    private static final String WARNING_PREFIX = "§8[§eHuanDungeon§8] §e";
    
    /**
     * 私有构造函数，防止实例化工具类
     */
    private MessageUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    
    /**
     * 初始化消息系统
     * 
     * @param pluginInstance 插件实例
     */
    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        loadDefaultMessages();
    }
    
    /**
     * 加载默认消息
     */
    private static void loadDefaultMessages() {
        // 通用消息
        messages.put("command.no-permission", "§c您没有权限执行此命令！");
        messages.put("command.player-only", "§c此命令只能由玩家执行！");
        messages.put("command.invalid-args", "§c命令参数无效！");
        messages.put("command.unknown", "§c未知命令！使用 /dungeon help 查看帮助。");
        
        // 地牢相关消息
        messages.put("dungeon.creating", "§a正在创建地牢，请稍候...");
        messages.put("dungeon.created", "§a地牢创建成功！");
        messages.put("dungeon.create-failed", "§c地牢创建失败！");
        messages.put("dungeon.not-found", "§c找不到指定的地牢！");
        messages.put("dungeon.already-in", "§c您已经在地牢中了！");
        messages.put("dungeon.max-instances", "§c地牢实例数量已达上限！");
        
        // 队伍相关消息
        messages.put("team.created", "§a队伍创建成功！");
        messages.put("team.joined", "§a您已加入队伍！");
        messages.put("team.left", "§e您已离开队伍。");
        messages.put("team.disbanded", "§c队伍已解散。");
        messages.put("team.invite-sent", "§a邀请已发送给 §f{player}§a！");
        messages.put("team.invite-received", "§a您收到了来自 §f{player} §a的队伍邀请！");
        messages.put("team.invite-expired", "§c队伍邀请已过期。");
        messages.put("team.full", "§c队伍已满员！");
        messages.put("team.not-leader", "§c您不是队长！");
        
        // 蓝图相关消息
        messages.put("blueprint.saved", "§a蓝图 §f{name} §a保存成功！");
        messages.put("blueprint.loaded", "§a蓝图 §f{name} §a加载成功！");
        messages.put("blueprint.not-found", "§c找不到蓝图 §f{name}§c！");
        messages.put("blueprint.invalid", "§c蓝图文件格式无效！");
        
        // 编辑器相关消息
        messages.put("editor.enabled", "§a编辑模式已启用！");
        messages.put("editor.disabled", "§e编辑模式已禁用。");
        messages.put("editor.function-placed", "§a功能已放置！");
        messages.put("editor.function-removed", "§e功能已移除。");
        
        // 系统消息
        messages.put("system.reloaded", "§a配置文件重载完成！");
        messages.put("system.saving", "§e正在保存数据...");
        messages.put("system.saved", "§a数据保存完成！");
        
        logger.info("默认消息加载完成，共 " + messages.size() + " 条消息");
    }
    
    /**
     * 发送普通消息给玩家
     * 
     * @param player 玩家
     * @param message 消息内容
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        player.sendMessage(PREFIX + message);
    }
    
    /**
     * 发送错误消息给玩家
     * 
     * @param player 玩家
     * @param message 错误消息
     */
    public static void sendError(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        player.sendMessage(ERROR_PREFIX + message);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }
    
    /**
     * 发送成功消息给玩家
     * 
     * @param player 玩家
     * @param message 成功消息
     */
    public static void sendSuccess(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        player.sendMessage(SUCCESS_PREFIX + message);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }
    
    /**
     * 发送警告消息给玩家
     * 
     * @param player 玩家
     * @param message 警告消息
     */
    public static void sendWarning(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        player.sendMessage(WARNING_PREFIX + message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
    }
    
    /**
     * 发送预定义消息给玩家
     * 
     * @param player 玩家
     * @param messageKey 消息键
     * @param placeholders 占位符替换
     */
    public static void sendMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey, placeholders);
        if (message != null) {
            sendMessage(player, message);
        }
    }
    
    /**
     * 发送预定义错误消息给玩家
     * 
     * @param player 玩家
     * @param messageKey 消息键
     */
    public static void sendErrorMessage(Player player, String messageKey) {
        String message = getMessage(messageKey);
        if (message != null) {
            sendError(player, message);
        }
    }
    
    /**
     * 发送预定义成功消息给玩家
     * 
     * @param player 玩家
     * @param messageKey 消息键
     */
    public static void sendSuccessMessage(Player player, String messageKey) {
        String message = getMessage(messageKey);
        if (message != null) {
            sendSuccess(player, message);
        }
    }
    
    /**
     * 显示标题给玩家
     * 
     * @param player 玩家
     * @param title 主标题
     * @param subtitle 副标题
     * @param fadeIn 淡入时间（tick）
     * @param stay 停留时间（tick）
     * @param fadeOut 淡出时间（tick）
     */
    public static void showTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }
        
        Component titleComponent = title != null ? 
            Component.text(title).color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true) : 
            Component.empty();
            
        Component subtitleComponent = subtitle != null ? 
            Component.text(subtitle).color(NamedTextColor.YELLOW) : 
            Component.empty();
        
        Title titleObj = Title.title(
            titleComponent,
            subtitleComponent,
            Title.Times.times(
                Duration.ofMillis(fadeIn * 50),
                Duration.ofMillis(stay * 50),
                Duration.ofMillis(fadeOut * 50)
            )
        );
        
        player.showTitle(titleObj);
    }
    
    /**
     * 发送动作栏消息给玩家
     * 
     * @param player 玩家
     * @param message 消息内容
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        
        Component actionBarComponent = Component.text(message).color(NamedTextColor.AQUA);
        player.sendActionBar(actionBarComponent);
    }
    
    /**
     * 播放声音给玩家
     * 
     * @param player 玩家
     * @param sound 声音类型
     * @param volume 音量
     * @param pitch 音调
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player == null || sound == null) {
            return;
        }
        
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * 获取消息内容
     * 
     * @param messageKey 消息键
     * @return 消息内容
     */
    public static String getMessage(String messageKey) {
        return messages.getOrDefault(messageKey, "§c消息未找到: " + messageKey);
    }
    
    /**
     * 获取消息内容并替换占位符
     * 
     * @param messageKey 消息键
     * @param placeholders 占位符替换
     * @return 处理后的消息内容
     */
    public static String getMessage(String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey);
        
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    /**
     * 添加或更新消息
     * 
     * @param messageKey 消息键
     * @param message 消息内容
     */
    public static void setMessage(String messageKey, String message) {
        if (messageKey != null && message != null) {
            messages.put(messageKey, message);
        }
    }
    
    /**
     * 格式化时间显示
     * 
     * @param seconds 秒数
     * @return 格式化的时间字符串
     */
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分" + (remainingSeconds > 0 ? remainingSeconds + "秒" : "");
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + "小时" + (remainingMinutes > 0 ? remainingMinutes + "分" : "");
        }
    }
    
    /**
     * 格式化玩家列表显示
     * 
     * @param players 玩家名称数组
     * @return 格式化的玩家列表
     */
    public static String formatPlayerList(String[] players) {
        if (players == null || players.length == 0) {
            return "§7无";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.length; i++) {
            if (i > 0) {
                sb.append("§7, ");
            }
            sb.append("§f").append(players[i]);
        }
        
        return sb.toString();
    }
}
