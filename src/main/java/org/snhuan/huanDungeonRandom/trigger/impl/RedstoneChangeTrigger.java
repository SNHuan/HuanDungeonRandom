package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;

public class RedstoneChangeTrigger extends Trigger {
    public RedstoneChangeTrigger() { super(); this.type = TriggerType.REDSTONE_CHANGE; }
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) {
        return event instanceof BlockRedstoneEvent;
    }
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        return TriggerResult.success("红石变化触发", new HashMap<>());
    }
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
