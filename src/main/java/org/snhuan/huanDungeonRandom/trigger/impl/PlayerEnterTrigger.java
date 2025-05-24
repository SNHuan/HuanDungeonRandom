package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家进入触发器实现 - 当玩家进入指定区域时触发
 */
public class PlayerEnterTrigger extends Trigger {
    
    private double radius = 3.0;
    
    public PlayerEnterTrigger() {
        super();
        this.type = TriggerType.PLAYER_ENTER;
    }
    
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        if (!(event instanceof PlayerMoveEvent) || location == null) {
            return false;
        }
        
        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
        return moveEvent.getTo() != null && 
               moveEvent.getTo().distance(location) <= radius &&
               (moveEvent.getFrom().distance(location) > radius);
    }
    
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getName());
        data.put("location", location);
        return TriggerResult.success("玩家进入区域", data);
    }
    
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {
        this.radius = config.getDouble("radius", 3.0);
    }
    
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {
        config.set("radius", radius);
    }
}
