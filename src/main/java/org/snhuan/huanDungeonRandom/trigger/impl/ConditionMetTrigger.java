package org.snhuan.huanDungeonRandom.trigger.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.function.ExecutionContext;
import org.snhuan.huanDungeonRandom.trigger.Trigger;
import org.snhuan.huanDungeonRandom.trigger.TriggerResult;

import java.util.HashMap;

public class ConditionMetTrigger extends Trigger {
    public ConditionMetTrigger() { super(); this.type = TriggerType.CONDITION_MET; }
    @Override
    protected boolean checkTriggerConditions(Player player, Event event, DungeonInstance dungeonInstance) { return true; }
    @Override
    protected TriggerResult doTrigger(Player player, Event event, DungeonInstance dungeonInstance, ExecutionContext context) {
        return TriggerResult.success("条件满足触发", new HashMap<>());
    }
    @Override
    protected void loadTriggerSpecificData(ConfigurationSection config) {}
    @Override
    protected void saveTriggerSpecificData(ConfigurationSection config) {}
}
