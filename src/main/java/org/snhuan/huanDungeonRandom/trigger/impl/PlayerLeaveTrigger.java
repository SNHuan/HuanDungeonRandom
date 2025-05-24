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

public class PlayerLeaveTrigger extends Trigger {
    private double radius = 3.0;
    
    public PlayerLeaveTrigger() {
        super();
        this.type = TriggerType.PLAYER_LEAVE;
    }
    
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        if (!(event instanceof PlayerMoveEvent) || location == null) return false;
        PlayerMoveEvent moveEvent = (PlayerMoveEvent) event;
        return moveEvent.getTo() != null && 
               moveEvent.getFrom().distance(location) <= radius &&
               moveEvent.getTo().distance(location) > radius;
    }
    
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getName());
        return TriggerResult.success("玩家离开区域", data);
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
