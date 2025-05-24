package org.snhuan.huanDungeonRandom.dungeon;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.Blueprint;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 地牢实例类 - 表示一个具体的地牢实例
 *
 * 包含内容：
 * - 地牢的物理位置和边界
 * - 使用的蓝图和主题
 * - 地牢状态管理
 * - 玩家管理
 * - 生成的结构信息
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonInstance {

    private final String instanceId;
    private final String dungeonId;
    private final DungeonTheme theme;
    private final World world;
    private final Location origin;
    private final BoundingBox bounds;

    // 状态管理
    private final DungeonState state;

    // 蓝图和结构信息
    private final List<PlacedBlueprint> placedBlueprints;
    private final Map<String, Location> namedLocations;
    private final Set<Location> doorLocations;
    private final Set<Location> functionPoints;

    // 配置信息
    private final DungeonConfig config;

    // 创建信息
    private final long createdTime;
    private final UUID createdBy;
    private final String creationReason;

    /**
     * 已放置的蓝图信息
     */
    public static class PlacedBlueprint {
        private final Blueprint blueprint;
        private final Location location;
        private final int rotation;
        private final Map<String, Object> metadata;

        public PlacedBlueprint(Blueprint blueprint, Location location, int rotation) {
            this.blueprint = blueprint;
            this.location = location.clone();
            this.rotation = rotation;
            this.metadata = new HashMap<>();
        }

        public Blueprint getBlueprint() { return blueprint; }
        public Location getLocation() { return location.clone(); }
        public int getRotation() { return rotation; }
        public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }

        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }

    /**
     * 地牢配置类
     */
    public static class DungeonConfig {
        private final int maxPlayers;
        private final long timeLimit;
        private final boolean allowPvP;
        private final boolean allowBreaking;
        private final boolean allowPlacing;
        private final boolean keepInventory;
        private final String difficulty;
        private final Map<String, Object> customSettings;

        public DungeonConfig(int maxPlayers, long timeLimit, boolean allowPvP,
                           boolean allowBreaking, boolean allowPlacing, boolean keepInventory,
                           String difficulty, Map<String, Object> customSettings) {
            this.maxPlayers = maxPlayers;
            this.timeLimit = timeLimit;
            this.allowPvP = allowPvP;
            this.allowBreaking = allowBreaking;
            this.allowPlacing = allowPlacing;
            this.keepInventory = keepInventory;
            this.difficulty = difficulty;
            this.customSettings = new HashMap<>(customSettings != null ? customSettings : new HashMap<>());
        }

        // Getters
        public int getMaxPlayers() { return maxPlayers; }
        public long getTimeLimit() { return timeLimit; }
        public boolean isAllowPvP() { return allowPvP; }
        public boolean isAllowBreaking() { return allowBreaking; }
        public boolean isAllowPlacing() { return allowPlacing; }
        public boolean isKeepInventory() { return keepInventory; }
        public String getDifficulty() { return difficulty; }
        public Map<String, Object> getCustomSettings() { return new HashMap<>(customSettings); }

        public Object getCustomSetting(String key) {
            return customSettings.get(key);
        }
    }

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private DungeonInstance(Builder builder) {
        this.instanceId = builder.instanceId;
        this.dungeonId = builder.dungeonId;
        this.theme = builder.theme;
        this.world = builder.world;
        this.origin = builder.origin.clone();
        this.bounds = builder.bounds.clone();

        this.state = new DungeonState(instanceId);

        this.placedBlueprints = new ArrayList<>(builder.placedBlueprints);
        this.namedLocations = new ConcurrentHashMap<>(builder.namedLocations);
        this.doorLocations = ConcurrentHashMap.newKeySet();
        this.functionPoints = ConcurrentHashMap.newKeySet();

        this.config = builder.config;
        this.createdTime = System.currentTimeMillis();
        this.createdBy = builder.createdBy;
        this.creationReason = builder.creationReason;

        // 初始化门和功能点位置
        initializeLocations();
    }

    /**
     * 创建构建器
     *
     * @param instanceId 实例ID
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param world 世界
     * @param origin 原点位置
     * @return 构建器实例
     */
    public static Builder builder(String instanceId, String dungeonId, DungeonTheme theme,
                                World world, Location origin) {
        return new Builder(instanceId, dungeonId, theme, world, origin);
    }

    /**
     * 初始化位置信息
     */
    private void initializeLocations() {
        doorLocations.clear();
        functionPoints.clear();

        for (PlacedBlueprint placed : placedBlueprints) {
            Blueprint blueprint = placed.getBlueprint();
            Location baseLocation = placed.getLocation();
            int rotation = placed.getRotation();

            // 添加门位置
            blueprint.getDoors().forEach(door -> {
                Vector doorVector = door.getLocation().toVector();
                Location doorLoc = calculateRotatedLocation(baseLocation, doorVector, rotation);
                doorLocations.add(doorLoc);
            });

            // 添加功能点位置（如果是房间蓝图）
            if (blueprint instanceof org.snhuan.huanDungeonRandom.blueprint.templates.RoomBlueprint) {
                var roomBlueprint = (org.snhuan.huanDungeonRandom.blueprint.templates.RoomBlueprint) blueprint;
                roomBlueprint.getFunctionPoints().forEach(point -> {
                    Location funcLoc = calculateRotatedLocation(baseLocation, point, rotation);
                    functionPoints.add(funcLoc);
                });
            }
        }
    }

    /**
     * 计算旋转后的位置
     *
     * @param base 基础位置
     * @param relative 相对位置
     * @param rotation 旋转角度（0, 90, 180, 270）
     * @return 旋转后的位置
     */
    private Location calculateRotatedLocation(Location base, Vector relative, int rotation) {
        double x = relative.getX();
        double z = relative.getZ();

        // 根据旋转角度计算新坐标
        switch (rotation) {
            case 90:
                return base.clone().add(-z, relative.getY(), x);
            case 180:
                return base.clone().add(-x, relative.getY(), -z);
            case 270:
                return base.clone().add(z, relative.getY(), -x);
            default: // 0度
                return base.clone().add(x, relative.getY(), z);
        }
    }

    /**
     * 玩家进入地牢
     *
     * @param player 玩家
     * @return 是否成功进入
     */
    public boolean playerEnter(Player player) {
        if (player == null) {
            return false;
        }

        // 检查地牢状态
        if (!state.getCurrentState().isActive() && state.getCurrentState() != DungeonState.State.WAITING) {
            return false;
        }

        // 检查玩家数量限制
        if (config.getMaxPlayers() > 0 && state.getPlayerCount() >= config.getMaxPlayers()) {
            return false;
        }

        // 传送玩家到入口
        Location spawnLocation = getSpawnLocation();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            state.playerEnter(player);
            return true;
        }

        return false;
    }

    /**
     * 玩家离开地牢
     *
     * @param player 玩家
     */
    public void playerExit(Player player) {
        if (player != null) {
            state.playerExit(player);

            // 如果没有玩家了，可能需要暂停或销毁地牢
            if (state.getPlayerCount() == 0 && state.getCurrentState() == DungeonState.State.ACTIVE) {
                // 这里可以添加逻辑决定是暂停还是继续
            }
        }
    }

    /**
     * 获取出生位置
     *
     * @return 出生位置
     */
    public Location getSpawnLocation() {
        // 优先使用命名位置中的spawn点
        Location spawn = namedLocations.get("spawn");
        if (spawn != null) {
            return spawn.clone();
        }

        // 使用player_spawn位置
        spawn = namedLocations.get("player_spawn");
        if (spawn != null) {
            return spawn.clone();
        }

        // 使用第一个门的位置
        if (!doorLocations.isEmpty()) {
            return doorLocations.iterator().next().clone();
        }

        // 使用原点位置
        return origin.clone().add(0, 1, 0);
    }

    /**
     * 获取出口位置
     *
     * @return 出口位置
     */
    public Location getExitLocation() {
        Location exit = namedLocations.get("exit");
        if (exit != null) {
            return exit.clone();
        }

        exit = namedLocations.get("exit_portal");
        if (exit != null) {
            return exit.clone();
        }

        return null;
    }

    /**
     * 检查位置是否在地牢范围内
     *
     * @param location 位置
     * @return 是否在范围内
     */
    public boolean isLocationInside(Location location) {
        if (location == null || !location.getWorld().equals(world)) {
            return false;
        }

        return bounds.contains(location.toVector());
    }

    /**
     * 添加命名位置
     *
     * @param name 位置名称
     * @param location 位置
     */
    public void addNamedLocation(String name, Location location) {
        if (name != null && !name.trim().isEmpty() && location != null) {
            namedLocations.put(name, location.clone());
        }
    }

    /**
     * 获取命名位置
     *
     * @param name 位置名称
     * @return 位置，不存在返回null
     */
    public Location getNamedLocation(String name) {
        Location location = namedLocations.get(name);
        return location != null ? location.clone() : null;
    }

    /**
     * 添加已放置的蓝图
     *
     * @param blueprint 蓝图
     * @param location 位置
     * @param rotation 旋转角度
     */
    public void addPlacedBlueprint(Blueprint blueprint, Location location, int rotation) {
        if (blueprint != null && location != null) {
            PlacedBlueprint placed = new PlacedBlueprint(blueprint, location, rotation);
            placedBlueprints.add(placed);

            // 重新初始化位置信息
            initializeLocations();
        }
    }

    /**
     * 完成地牢
     *
     * @param player 完成的玩家
     */
    public void complete(Player player) {
        if (player != null) {
            state.playerComplete(player);
        }

        // 检查是否所有玩家都完成了
        if (state.getCompletedPlayers().containsAll(state.getPlayersInside())) {
            state.transitionTo(DungeonState.State.COMPLETED);
        }
    }

    /**
     * 地牢失败
     */
    public void fail() {
        state.transitionTo(DungeonState.State.FAILED);
    }

    /**
     * 销毁地牢
     */
    public void destroy() {
        state.transitionTo(DungeonState.State.DESTROYING);

        // 清理所有玩家
        for (UUID playerId : state.getPlayersInside()) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) {
                playerExit(player);
                // 这里可以传送玩家到安全位置
            }
        }

        state.transitionTo(DungeonState.State.DESTROYED);
    }

    // ==================== Getter 方法 ====================

    public String getInstanceId() { return instanceId; }
    public String getDungeonId() { return dungeonId; }
    public DungeonTheme getTheme() { return theme; }
    public World getWorld() { return world; }
    public Location getOrigin() { return origin.clone(); }
    public BoundingBox getBounds() { return bounds.clone(); }
    public DungeonState getState() { return state; }
    public DungeonConfig getConfig() { return config; }
    public long getCreatedTime() { return createdTime; }
    public UUID getCreatedBy() { return createdBy; }
    public String getCreationReason() { return creationReason; }

    public List<PlacedBlueprint> getPlacedBlueprints() { return new ArrayList<>(placedBlueprints); }
    public Map<String, Location> getNamedLocations() { return new HashMap<>(namedLocations); }
    public Set<Location> getDoorLocations() { return new HashSet<>(doorLocations); }
    public Set<Location> getFunctionPoints() { return new HashSet<>(functionPoints); }

    @Override
    public String toString() {
        return String.format("DungeonInstance{id='%s', dungeonId='%s', state=%s, players=%d}",
                           instanceId, dungeonId, state.getCurrentState(), state.getPlayerCount());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DungeonInstance that = (DungeonInstance) obj;
        return Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId);
    }

    // ==================== 监听器支持方法 ====================

    /**
     * 检查位置是否在地牢中
     *
     * @param location 位置
     * @return 是否在地牢中
     */
    public boolean isLocationInDungeon(Location location) {
        return isLocationInside(location);
    }

    /**
     * 获取地牢设置
     *
     * @return 地牢设置
     */
    public DungeonSettings getSettings() {
        return new DungeonSettings(config);
    }

    /**
     * 检查方块是否受保护
     *
     * @param location 方块位置
     * @return 是否受保护
     */
    public boolean isProtectedBlock(Location location) {
        // 检查是否是重要的结构方块（如门、功能点等）
        return doorLocations.contains(location) || functionPoints.contains(location);
    }

    /**
     * 检查区域是否受保护
     *
     * @param location 位置
     * @return 是否受保护
     */
    public boolean isProtectedArea(Location location) {
        // 检查是否在重要区域附近（如出生点、出口等）
        Location spawn = getSpawnLocation();
        if (spawn != null && spawn.distance(location) < 3.0) {
            return true;
        }

        Location exit = getExitLocation();
        if (exit != null && exit.distance(location) < 3.0) {
            return true;
        }

        return false;
    }

    /**
     * 记录方块变化
     *
     * @param location 位置
     * @param oldType 原方块类型
     * @param newType 新方块类型
     */
    public void recordBlockChange(Location location, org.bukkit.Material oldType, org.bukkit.Material newType) {
        // 这里可以记录方块变化用于恢复地牢
        // 暂时只记录日志
        if (oldType != null && newType == null) {
            // 方块被破坏
            state.addMetadata("blocks_broken", state.getMetadata("blocks_broken", Integer.class, 0) + 1);
        } else if (oldType == null && newType != null) {
            // 方块被放置
            state.addMetadata("blocks_placed", state.getMetadata("blocks_placed", Integer.class, 0) + 1);
        }
    }

    /**
     * 获取安全重生位置
     *
     * @return 安全重生位置
     */
    public Location getSafeRespawnLocation() {
        // 优先使用命名的重生点
        Location respawn = namedLocations.get("respawn");
        if (respawn != null) {
            return respawn.clone();
        }

        // 使用出生点
        return getSpawnLocation();
    }

    /**
     * 获取地牢中的玩家列表
     *
     * @return 玩家列表
     */
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (UUID playerId : state.getPlayersInside()) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    /**
     * 检查是否有活跃玩家
     *
     * @return 是否有活跃玩家
     */
    public boolean hasActivePlayers() {
        return !state.getPlayersInside().isEmpty();
    }

    /**
     * 处理实体死亡
     *
     * @param entity 死亡的实体
     */
    public void handleEntityDeath(org.bukkit.entity.Entity entity) {
        // 记录实体死亡
        state.addMetadata("entities_killed", state.getMetadata("entities_killed", Integer.class, 0) + 1);
    }

    /**
     * 处理实体生成
     *
     * @param entity 生成的实体
     */
    public void handleEntitySpawn(org.bukkit.entity.Entity entity) {
        // 记录实体生成
        state.addMetadata("entities_spawned", state.getMetadata("entities_spawned", Integer.class, 0) + 1);
    }

    /**
     * 处理区块加载
     *
     * @param chunk 加载的区块
     */
    public void handleChunkLoad(org.bukkit.Chunk chunk) {
        // 地牢区块加载处理
        state.addMetadata("chunks_loaded", state.getMetadata("chunks_loaded", Integer.class, 0) + 1);
    }

    /**
     * 处理区块卸载
     *
     * @param chunk 卸载的区块
     */
    public void handleChunkUnload(org.bukkit.Chunk chunk) {
        // 地牢区块卸载处理
        state.addMetadata("chunks_unloaded", state.getMetadata("chunks_unloaded", Integer.class, 0) + 1);
    }

    /**
     * 获取地牢ID（别名方法）
     *
     * @return 地牢ID
     */
    public String getId() {
        return instanceId;
    }

    // ==================== 构建器类 ====================

    /**
     * 地牢实例构建器
     */
    public static class Builder {
        private final String instanceId;
        private final String dungeonId;
        private final DungeonTheme theme;
        private final World world;
        private final Location origin;

        private BoundingBox bounds;
        private final List<PlacedBlueprint> placedBlueprints = new ArrayList<>();
        private final Map<String, Location> namedLocations = new HashMap<>();
        private DungeonConfig config;
        private UUID createdBy;
        private String creationReason = "Unknown";

        public Builder(String instanceId, String dungeonId, DungeonTheme theme, World world, Location origin) {
            this.instanceId = instanceId;
            this.dungeonId = dungeonId;
            this.theme = theme;
            this.world = world;
            this.origin = origin;

            // 设置默认边界（100x100x100）
            Vector min = origin.toVector().subtract(new Vector(50, 10, 50));
            Vector max = origin.toVector().add(new Vector(50, 40, 50));
            this.bounds = new BoundingBox(min.getX(), min.getY(), min.getZ(),
                                        max.getX(), max.getY(), max.getZ());

            // 设置默认配置
            this.config = new DungeonConfig(4, 30 * 60 * 1000, false, false, false, true, "normal", new HashMap<>());
        }

        public Builder setBounds(BoundingBox bounds) {
            if (bounds != null) {
                this.bounds = bounds.clone();
            }
            return this;
        }

        public Builder setBounds(Vector min, Vector max) {
            if (min != null && max != null) {
                this.bounds = new BoundingBox(min.getX(), min.getY(), min.getZ(),
                                            max.getX(), max.getY(), max.getZ());
            }
            return this;
        }

        public Builder addPlacedBlueprint(Blueprint blueprint, Location location, int rotation) {
            if (blueprint != null && location != null) {
                this.placedBlueprints.add(new PlacedBlueprint(blueprint, location, rotation));
            }
            return this;
        }

        public Builder addNamedLocation(String name, Location location) {
            if (name != null && !name.trim().isEmpty() && location != null) {
                this.namedLocations.put(name, location.clone());
            }
            return this;
        }

        public Builder setConfig(DungeonConfig config) {
            if (config != null) {
                this.config = config;
            }
            return this;
        }

        public Builder setCreatedBy(UUID createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setCreationReason(String reason) {
            this.creationReason = reason != null ? reason : "Unknown";
            return this;
        }

        // Getter方法供生成器使用
        public Location getOrigin() {
            return origin.clone();
        }

        public List<PlacedBlueprint> getPlacedBlueprints() {
            return new ArrayList<>(placedBlueprints);
        }

        public DungeonInstance build() {
            return new DungeonInstance(this);
        }
    }
}
