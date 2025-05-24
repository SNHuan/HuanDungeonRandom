package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;
import java.util.Map;

public class BlockPlaceTrigger extends Trigger {
    public BlockPlaceTrigger() { super(); this.type = TriggerType.BLOCK_PLACE; }
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        return event instanceof BlockPlaceEvent;
    }
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        return TriggerResult.success("方块放置触发", new HashMap<>());
    }
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
