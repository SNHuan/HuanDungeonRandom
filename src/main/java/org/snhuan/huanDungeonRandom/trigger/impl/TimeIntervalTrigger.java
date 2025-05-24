package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;

public class TimeIntervalTrigger extends Trigger {
    public TimeIntervalTrigger() { super(); this.type = TriggerType.TIME_INTERVAL; }
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) { return true; }
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        return TriggerResult.success("时间间隔触发", new HashMap<>());
    }
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
