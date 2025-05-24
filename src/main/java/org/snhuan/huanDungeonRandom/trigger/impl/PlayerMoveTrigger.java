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

public class PlayerMoveTrigger extends Trigger {
    public PlayerMoveTrigger() {
        super();
        this.type = TriggerType.PLAYER_MOVE;
    }
    
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        return event instanceof PlayerMoveEvent;
    }
    
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getName());
        return TriggerResult.success("玩家移动触发", data);
    }
    
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
