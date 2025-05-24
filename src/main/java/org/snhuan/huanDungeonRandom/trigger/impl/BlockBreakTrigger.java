package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakTrigger extends Trigger {
    public BlockBreakTrigger() {
        super();
        this.type = TriggerType.BLOCK_BREAK;
    }
    
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        return event instanceof BlockBreakEvent;
    }
    
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        Map<String, Object> data = new HashMap<>();
        data.put("player", player.getName());
        return TriggerResult.success("方块破坏触发", data);
    }
    
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
