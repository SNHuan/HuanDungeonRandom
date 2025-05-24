package org.snhuan.huanDungeonRandom.dungeon;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地牢状态管理类 - 跟踪地牢实例的运行状态
 *
 * 管理内容：
 * - 地牢生命周期状态
 * - 玩家进入/离开记录
 * - 完成进度跟踪
 * - 统计信息收集
 * - 状态变更事件
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonState {

    private final String dungeonId;
    private volatile State currentState;
    private final long createdTime;
    private volatile long startTime;
    private volatile long endTime;

    // 玩家管理
    private final Set<UUID> playersInside;
    private final Map<UUID, Long> playerEnterTimes;
    private final Map<UUID, Long> playerExitTimes;
    private final Set<UUID> completedPlayers;

    // 进度跟踪
    private final Map<String, Object> progressData;
    private final Set<String> completedObjectives;
    private final Map<String, Integer> statistics;
    private final Map<String, Object> metadata;

    // 状态监听器
    private final List<StateChangeListener> listeners;

    /**
     * 地牢状态枚举
     */
    public enum State {
        CREATING("创建中", "地牢正在生成"),
        WAITING("等待中", "等待玩家进入"),
        ACTIVE("活跃中", "玩家正在游玩"),
        PAUSED("暂停中", "地牢已暂停"),
        COMPLETED("已完成", "地牢已完成"),
        FAILED("已失败", "地牢挑战失败"),
        EXPIRED("已过期", "地牢已过期"),
        DESTROYING("销毁中", "地牢正在销毁"),
        DESTROYED("已销毁", "地牢已被销毁");

        private final String displayName;
        private final String description;

        State(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 检查是否可以转换到目标状态
         *
         * @param target 目标状态
         * @return 是否可以转换
         */
        public boolean canTransitionTo(State target) {
            switch (this) {
                case CREATING:
                    return target == WAITING || target == FAILED || target == DESTROYING;
                case WAITING:
                    return target == ACTIVE || target == EXPIRED || target == DESTROYING;
                case ACTIVE:
                    return target == PAUSED || target == COMPLETED || target == FAILED || target == DESTROYING;
                case PAUSED:
                    return target == ACTIVE || target == FAILED || target == DESTROYING;
                case COMPLETED:
                case FAILED:
                case EXPIRED:
                    return target == DESTROYING;
                case DESTROYING:
                    return target == DESTROYED;
                case DESTROYED:
                    return false;
                default:
                    return false;
            }
        }

        /**
         * 检查状态是否为终止状态
         *
         * @return 是否为终止状态
         */
        public boolean isTerminal() {
            return this == COMPLETED || this == FAILED || this == EXPIRED || this == DESTROYED;
        }

        /**
         * 检查状态是否为活跃状态
         *
         * @return 是否为活跃状态
         */
        public boolean isActive() {
            return this == ACTIVE || this == PAUSED;
        }
    }

    /**
     * 状态变更监听器接口
     */
    public interface StateChangeListener {
        /**
         * 状态变更时调用
         *
         * @param dungeonId 地牢ID
         * @param oldState 旧状态
         * @param newState 新状态
         */
        void onStateChanged(String dungeonId, State oldState, State newState);
    }

    /**
     * 构造函数
     *
     * @param dungeonId 地牢ID
     */
    public DungeonState(String dungeonId) {
        this.dungeonId = dungeonId;
        this.currentState = State.CREATING;
        this.createdTime = System.currentTimeMillis();
        this.startTime = 0;
        this.endTime = 0;

        this.playersInside = ConcurrentHashMap.newKeySet();
        this.playerEnterTimes = new ConcurrentHashMap<>();
        this.playerExitTimes = new ConcurrentHashMap<>();
        this.completedPlayers = ConcurrentHashMap.newKeySet();

        this.progressData = new ConcurrentHashMap<>();
        this.completedObjectives = ConcurrentHashMap.newKeySet();
        this.statistics = new ConcurrentHashMap<>();
        this.metadata = new ConcurrentHashMap<>();

        this.listeners = new ArrayList<>();
    }

    /**
     * 转换状态
     *
     * @param newState 新状态
     * @return 是否转换成功
     */
    public synchronized boolean transitionTo(State newState) {
        if (newState == null || !currentState.canTransitionTo(newState)) {
            return false;
        }

        State oldState = this.currentState;
        this.currentState = newState;

        // 更新时间戳
        updateTimestamps(newState);

        // 通知监听器
        notifyStateChange(oldState, newState);

        return true;
    }

    /**
     * 更新时间戳
     *
     * @param newState 新状态
     */
    private void updateTimestamps(State newState) {
        long currentTime = System.currentTimeMillis();

        switch (newState) {
            case ACTIVE:
                if (startTime == 0) {
                    startTime = currentTime;
                }
                break;
            case COMPLETED:
            case FAILED:
            case EXPIRED:
                if (endTime == 0) {
                    endTime = currentTime;
                }
                break;
        }
    }

    /**
     * 通知状态变更
     *
     * @param oldState 旧状态
     * @param newState 新状态
     */
    private void notifyStateChange(State oldState, State newState) {
        for (StateChangeListener listener : listeners) {
            try {
                listener.onStateChanged(dungeonId, oldState, newState);
            } catch (Exception e) {
                // 忽略监听器异常，避免影响状态转换
            }
        }
    }

    /**
     * 玩家进入地牢
     *
     * @param player 玩家
     */
    public void playerEnter(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        playersInside.add(playerId);
        playerEnterTimes.put(playerId, System.currentTimeMillis());

        // 如果是第一个玩家进入且状态为等待中，转换为活跃状态
        if (playersInside.size() == 1 && currentState == State.WAITING) {
            transitionTo(State.ACTIVE);
        }

        incrementStatistic("player_enters");
    }

    /**
     * 玩家离开地牢
     *
     * @param player 玩家
     */
    public void playerExit(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        playersInside.remove(playerId);
        playerExitTimes.put(playerId, System.currentTimeMillis());

        incrementStatistic("player_exits");
    }

    /**
     * 玩家完成地牢
     *
     * @param player 玩家
     */
    public void playerComplete(Player player) {
        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();
        completedPlayers.add(playerId);

        incrementStatistic("player_completions");
    }

    /**
     * 设置进度数据
     *
     * @param key 键
     * @param value 值
     */
    public void setProgressData(String key, Object value) {
        if (key != null) {
            progressData.put(key, value);
        }
    }

    /**
     * 获取进度数据
     *
     * @param key 键
     * @return 值
     */
    public Object getProgressData(String key) {
        return progressData.get(key);
    }

    /**
     * 完成目标
     *
     * @param objective 目标名称
     */
    public void completeObjective(String objective) {
        if (objective != null && !objective.trim().isEmpty()) {
            completedObjectives.add(objective);
            incrementStatistic("objectives_completed");
        }
    }

    /**
     * 检查目标是否已完成
     *
     * @param objective 目标名称
     * @return 是否已完成
     */
    public boolean isObjectiveCompleted(String objective) {
        return completedObjectives.contains(objective);
    }

    /**
     * 增加统计数据
     *
     * @param key 统计键
     */
    public void incrementStatistic(String key) {
        incrementStatistic(key, 1);
    }

    /**
     * 增加统计数据
     *
     * @param key 统计键
     * @param amount 增加数量
     */
    public void incrementStatistic(String key, int amount) {
        if (key != null) {
            statistics.merge(key, amount, Integer::sum);
        }
    }

    /**
     * 获取统计数据
     *
     * @param key 统计键
     * @return 统计值
     */
    public int getStatistic(String key) {
        return statistics.getOrDefault(key, 0);
    }

    /**
     * 添加元数据
     *
     * @param key 键
     * @param value 值
     */
    public void addMetadata(String key, Object value) {
        if (key != null) {
            metadata.put(key, value);
        }
    }

    /**
     * 获取元数据
     *
     * @param key 键
     * @return 值
     */
    public Object getMetadata(String key) {
        return metadata.get(key);
    }

    /**
     * 获取元数据（带类型转换和默认值）
     *
     * @param key 键
     * @param type 目标类型
     * @param defaultValue 默认值
     * @param <T> 类型参数
     * @return 转换后的值
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type, T defaultValue) {
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    /**
     * 添加状态变更监听器
     *
     * @param listener 监听器
     */
    public void addStateChangeListener(StateChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * 移除状态变更监听器
     *
     * @param listener 监听器
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    // ==================== Getter 方法 ====================

    public String getDungeonId() { return dungeonId; }
    public State getCurrentState() { return currentState; }
    public long getCreatedTime() { return createdTime; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }

    public Set<UUID> getPlayersInside() { return new HashSet<>(playersInside); }
    public int getPlayerCount() { return playersInside.size(); }

    public Set<UUID> getCompletedPlayers() { return new HashSet<>(completedPlayers); }
    public Set<String> getCompletedObjectives() { return new HashSet<>(completedObjectives); }

    public Map<String, Integer> getStatistics() { return new HashMap<>(statistics); }

    /**
     * 获取地牢运行时长（毫秒）
     *
     * @return 运行时长
     */
    public long getDuration() {
        if (startTime == 0) {
            return 0;
        }

        long endTimeToUse = endTime > 0 ? endTime : System.currentTimeMillis();
        return endTimeToUse - startTime;
    }

    /**
     * 获取玩家在地牢中的时长
     *
     * @param playerId 玩家ID
     * @return 时长（毫秒）
     */
    public long getPlayerDuration(UUID playerId) {
        Long enterTime = playerEnterTimes.get(playerId);
        if (enterTime == null) {
            return 0;
        }

        Long exitTime = playerExitTimes.get(playerId);
        long endTimeToUse = exitTime != null ? exitTime : System.currentTimeMillis();

        return endTimeToUse - enterTime;
    }

    /**
     * 检查玩家是否在地牢中
     *
     * @param playerId 玩家ID
     * @return 是否在地牢中
     */
    public boolean isPlayerInside(UUID playerId) {
        return playersInside.contains(playerId);
    }

    /**
     * 检查玩家是否已完成地牢
     *
     * @param playerId 玩家ID
     * @return 是否已完成
     */
    public boolean hasPlayerCompleted(UUID playerId) {
        return completedPlayers.contains(playerId);
    }
}
