package org.snhuan.huanDungeonRandom.generation;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.Blueprint;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintManager;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintType;
import org.snhuan.huanDungeonRandom.blueprint.templates.RoomBlueprint;
import org.snhuan.huanDungeonRandom.blueprint.templates.TileBlueprint;
import org.snhuan.huanDungeonRandom.blueprint.templates.CorridorBlueprint;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.*;
import java.util.logging.Logger;

/**
 * 地牢生成器 - 负责生成完整的地牢实例
 *
 * 核心功能：
 * - 地牢布局规划
 * - 蓝图选择和放置
 * - 连接路径生成
 * - 冲突检测和解决
 * - 生成结果验证
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonGenerator {

    private final BlueprintManager blueprintManager;
    private final RandomGenerator randomGenerator;
    private final Logger logger;

    // 生成配置
    private final GenerationConfig config;

    /**
     * 生成配置类
     */
    public static class GenerationConfig {
        private final int maxAttempts;
        private final int gridSize;
        private final int minSpacing;
        private final boolean allowOverlap;
        private final boolean validateConnections;
        private final double placementTolerance;

        public GenerationConfig(int maxAttempts, int gridSize, int minSpacing,
                              boolean allowOverlap, boolean validateConnections, double placementTolerance) {
            this.maxAttempts = maxAttempts;
            this.gridSize = gridSize;
            this.minSpacing = minSpacing;
            this.allowOverlap = allowOverlap;
            this.validateConnections = validateConnections;
            this.placementTolerance = placementTolerance;
        }

        public static GenerationConfig defaultConfig() {
            return new GenerationConfig(100, 16, 5, false, true, 0.8);
        }

        // Getters
        public int getMaxAttempts() { return maxAttempts; }
        public int getGridSize() { return gridSize; }
        public int getMinSpacing() { return minSpacing; }
        public boolean isAllowOverlap() { return allowOverlap; }
        public boolean isValidateConnections() { return validateConnections; }
        public double getPlacementTolerance() { return placementTolerance; }
    }

    /**
     * 放置结果类
     */
    public static class PlacementResult {
        private final boolean success;
        private final String message;
        private final DungeonInstance instance;
        private final GenerationStatistics statistics;

        public PlacementResult(boolean success, String message, DungeonInstance instance, GenerationStatistics statistics) {
            this.success = success;
            this.message = message;
            this.instance = instance;
            this.statistics = statistics;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public DungeonInstance getInstance() { return instance; }
        public GenerationStatistics getStatistics() { return statistics; }
    }

    /**
     * 生成统计信息类
     */
    public static class GenerationStatistics {
        private final long startTime;
        private final long endTime;
        private final int totalAttempts;
        private final int successfulPlacements;
        private final int failedPlacements;
        private final Map<BlueprintType, Integer> placementCounts;

        public GenerationStatistics(long startTime, long endTime, int totalAttempts,
                                   int successfulPlacements, int failedPlacements,
                                   Map<BlueprintType, Integer> placementCounts) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.totalAttempts = totalAttempts;
            this.successfulPlacements = successfulPlacements;
            this.failedPlacements = failedPlacements;
            this.placementCounts = new HashMap<>(placementCounts);
        }

        public long getDuration() { return endTime - startTime; }
        public int getTotalAttempts() { return totalAttempts; }
        public int getSuccessfulPlacements() { return successfulPlacements; }
        public int getFailedPlacements() { return failedPlacements; }
        public Map<BlueprintType, Integer> getPlacementCounts() { return new HashMap<>(placementCounts); }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (double) successfulPlacements / totalAttempts : 0.0;
        }

        public static class Builder {
            private final long startTime;
            private int totalAttempts = 0;
            private int successfulPlacements = 0;
            private int failedPlacements = 0;
            private final Map<BlueprintType, Integer> placementCounts = new HashMap<>();

            public Builder(long startTime) {
                this.startTime = startTime;

                // 初始化计数器
                for (BlueprintType type : BlueprintType.values()) {
                    placementCounts.put(type, 0);
                }
            }

            public void incrementPlacement(BlueprintType type, boolean success) {
                totalAttempts++;
                if (success) {
                    successfulPlacements++;
                    placementCounts.merge(type, 1, Integer::sum);
                } else {
                    failedPlacements++;
                }
            }

            public GenerationStatistics build(long endTime) {
                return new GenerationStatistics(
                    startTime, endTime, totalAttempts,
                    successfulPlacements, failedPlacements, placementCounts
                );
            }
        }
    }

    /**
     * 构造函数
     *
     * @param blueprintManager 蓝图管理器
     * @param randomGenerator 随机生成器
     * @param logger 日志记录器
     */
    public DungeonGenerator(BlueprintManager blueprintManager, RandomGenerator randomGenerator, Logger logger) {
        this.blueprintManager = blueprintManager;
        this.randomGenerator = randomGenerator;
        this.logger = logger;
        this.config = GenerationConfig.defaultConfig();
    }

    /**
     * 构造函数（自定义配置）
     *
     * @param blueprintManager 蓝图管理器
     * @param randomGenerator 随机生成器
     * @param logger 日志记录器
     * @param config 生成配置
     */
    public DungeonGenerator(BlueprintManager blueprintManager, RandomGenerator randomGenerator,
                          Logger logger, GenerationConfig config) {
        this.blueprintManager = blueprintManager;
        this.randomGenerator = randomGenerator;
        this.logger = logger;
        this.config = config != null ? config : GenerationConfig.defaultConfig();
    }

    /**
     * 生成地牢实例
     *
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param world 世界
     * @param origin 原点位置
     * @param createdBy 创建者
     * @return 生成的地牢实例，失败返回null
     */
    public DungeonInstance generateDungeon(String dungeonId, DungeonTheme theme,
                                         World world, Location origin, UUID createdBy) {
        String instanceId = "dungeon_" + System.currentTimeMillis();
        long startTime = System.currentTimeMillis();

        logger.info("开始生成地牢: " + instanceId);

        try {
            // 生成地牢参数
            RandomGenerator.GenerationParameters params = randomGenerator.generateDungeonParameters(theme);

            // 创建地牢实例构建器
            DungeonInstance.Builder instanceBuilder = DungeonInstance.builder(instanceId, dungeonId, theme, world, origin)
                .setCreatedBy(createdBy)
                .setCreationReason("Random Generation");

            // 生成统计信息
            GenerationStatistics.Builder statsBuilder = new GenerationStatistics.Builder(startTime);

            // 第一阶段：放置主要房间
            if (!placeMainRooms(instanceBuilder, theme, params, statsBuilder)) {
                logger.severe("主要房间放置失败");
                return null;
            }

            // 第二阶段：放置走廊连接
            if (!placeCorridors(instanceBuilder, theme, params, statsBuilder)) {
                logger.severe("走廊连接失败");
                return null;
            }

            // 第三阶段：填充瓦片
            if (!placeTiles(instanceBuilder, theme, params, statsBuilder)) {
                logger.severe("瓦片填充失败");
                return null;
            }

            // 第四阶段：验证和优化
            DungeonInstance instance = instanceBuilder.build();
            if (config.isValidateConnections() && !validateConnections(instance)) {
                logger.severe("连接验证失败");
                return null;
            }

            long endTime = System.currentTimeMillis();
            GenerationStatistics statistics = statsBuilder.build(endTime);

            logger.info("地牢生成完成: " + instanceId + " (耗时: " + statistics.getDuration() + "ms)");

            return instance;

        } catch (Exception e) {
            logger.severe("地牢生成异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 放置主要房间
     *
     * @param instanceBuilder 实例构建器
     * @param theme 主题
     * @param params 生成参数
     * @param statsBuilder 统计构建器
     * @return 是否成功
     */
    private boolean placeMainRooms(DungeonInstance.Builder instanceBuilder, DungeonTheme theme,
                                 RandomGenerator.GenerationParameters params, GenerationStatistics.Builder statsBuilder) {

        List<RoomBlueprint> roomBlueprints = blueprintManager.getBlueprintsByType(BlueprintType.ROOM)
            .stream()
            .map(blueprint -> (RoomBlueprint) blueprint)
            .filter(room -> isThemeCompatible(room, theme))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (roomBlueprints.isEmpty()) {
            logger.warning("没有找到兼容的房间蓝图");
            return false;
        }

        int roomsToPlace = params.getRoomCount();
        int placedRooms = 0;

        // 首先放置出生房间
        RoomBlueprint spawnRoom = findRoomByType(roomBlueprints, RoomBlueprint.RoomType.SPAWN);
        if (spawnRoom != null) {
            Location spawnLocation = instanceBuilder.getOrigin().clone();
            instanceBuilder.addPlacedBlueprint(spawnRoom, spawnLocation, 0);
            instanceBuilder.addNamedLocation("spawn", spawnLocation.clone().add(0, 1, 0));
            placedRooms++;
            statsBuilder.incrementPlacement(BlueprintType.ROOM, true);
        }

        // 放置其他房间
        for (int i = placedRooms; i < roomsToPlace; i++) {
            RoomBlueprint room = randomGenerator.randomRoomBlueprint(roomBlueprints);
            if (room != null) {
                Location roomLocation = findSuitableLocation(instanceBuilder, room);
                if (roomLocation != null) {
                    int rotation = randomGenerator.randomChoice(0, 90, 180, 270);
                    instanceBuilder.addPlacedBlueprint(room, roomLocation, rotation);
                    placedRooms++;
                    statsBuilder.incrementPlacement(BlueprintType.ROOM, true);
                } else {
                    statsBuilder.incrementPlacement(BlueprintType.ROOM, false);
                }
            }
        }

        logger.info("放置了 " + placedRooms + "/" + roomsToPlace + " 个房间");
        return placedRooms >= Math.max(1, roomsToPlace * config.getPlacementTolerance());
    }

    /**
     * 放置走廊
     *
     * @param instanceBuilder 实例构建器
     * @param theme 主题
     * @param params 生成参数
     * @param statsBuilder 统计构建器
     * @return 是否成功
     */
    private boolean placeCorridors(DungeonInstance.Builder instanceBuilder, DungeonTheme theme,
                                 RandomGenerator.GenerationParameters params, GenerationStatistics.Builder statsBuilder) {

        List<CorridorBlueprint> corridorBlueprints = blueprintManager.getBlueprintsByType(BlueprintType.CORRIDOR)
            .stream()
            .map(blueprint -> (CorridorBlueprint) blueprint)
            .filter(corridor -> isThemeCompatible(corridor, theme))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (corridorBlueprints.isEmpty()) {
            logger.warning("没有找到兼容的走廊蓝图");
            return true; // 走廊不是必需的
        }

        int corridorsToPlace = params.getCorridorCount();
        int placedCorridors = 0;

        for (int i = 0; i < corridorsToPlace; i++) {
            CorridorBlueprint corridor = randomGenerator.randomCorridorBlueprint(corridorBlueprints);
            if (corridor != null) {
                Location corridorLocation = findSuitableLocation(instanceBuilder, corridor);
                if (corridorLocation != null) {
                    int rotation = randomGenerator.randomChoice(0, 90, 180, 270);
                    instanceBuilder.addPlacedBlueprint(corridor, corridorLocation, rotation);
                    placedCorridors++;
                    statsBuilder.incrementPlacement(BlueprintType.CORRIDOR, true);
                } else {
                    statsBuilder.incrementPlacement(BlueprintType.CORRIDOR, false);
                }
            }
        }

        logger.info("放置了 " + placedCorridors + "/" + corridorsToPlace + " 个走廊");
        return true; // 走廊放置失败不影响整体生成
    }

    /**
     * 放置瓦片
     *
     * @param instanceBuilder 实例构建器
     * @param theme 主题
     * @param params 生成参数
     * @param statsBuilder 统计构建器
     * @return 是否成功
     */
    private boolean placeTiles(DungeonInstance.Builder instanceBuilder, DungeonTheme theme,
                             RandomGenerator.GenerationParameters params, GenerationStatistics.Builder statsBuilder) {

        List<TileBlueprint> tileBlueprints = blueprintManager.getBlueprintsByType(BlueprintType.TILE)
            .stream()
            .map(blueprint -> (TileBlueprint) blueprint)
            .filter(tile -> isThemeCompatible(tile, theme))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (tileBlueprints.isEmpty()) {
            logger.warning("没有找到兼容的瓦片蓝图");
            return true; // 瓦片不是必需的
        }

        // 根据参数决定瓦片数量
        int tilesToPlace = randomGenerator.randomInt(5, 15);
        int placedTiles = 0;

        for (int i = 0; i < tilesToPlace; i++) {
            TileBlueprint tile = randomGenerator.randomTileBlueprint(tileBlueprints);
            if (tile != null) {
                Location tileLocation = findSuitableLocation(instanceBuilder, tile);
                if (tileLocation != null) {
                    int rotation = randomGenerator.randomChoice(0, 90, 180, 270);
                    instanceBuilder.addPlacedBlueprint(tile, tileLocation, rotation);
                    placedTiles++;
                    statsBuilder.incrementPlacement(BlueprintType.TILE, true);
                } else {
                    statsBuilder.incrementPlacement(BlueprintType.TILE, false);
                }
            }
        }

        logger.info("放置了 " + placedTiles + "/" + tilesToPlace + " 个瓦片");
        return true; // 瓦片放置失败不影响整体生成
    }

    /**
     * 查找合适的放置位置
     *
     * @param instanceBuilder 实例构建器
     * @param blueprint 蓝图
     * @return 合适的位置，找不到返回null
     */
    private Location findSuitableLocation(DungeonInstance.Builder instanceBuilder, Blueprint blueprint) {
        Location origin = instanceBuilder.getOrigin();
        int attempts = 0;

        while (attempts < config.getMaxAttempts()) {
            // 在原点周围随机选择位置
            int x = randomGenerator.randomInt(-50, 50) * config.getGridSize();
            int z = randomGenerator.randomInt(-50, 50) * config.getGridSize();
            Location candidate = origin.clone().add(x, 0, z);

            // 检查位置是否合适
            if (isLocationSuitable(instanceBuilder, blueprint, candidate)) {
                return candidate;
            }

            attempts++;
        }

        return null;
    }

    /**
     * 检查位置是否合适放置蓝图
     *
     * @param instanceBuilder 实例构建器
     * @param blueprint 蓝图
     * @param location 位置
     * @return 是否合适
     */
    private boolean isLocationSuitable(DungeonInstance.Builder instanceBuilder, Blueprint blueprint, Location location) {
        // 检查是否与已放置的蓝图冲突
        if (!config.isAllowOverlap()) {
            BoundingBox blueprintBounds = createBoundingBox(blueprint, location);

            for (DungeonInstance.PlacedBlueprint placed : instanceBuilder.getPlacedBlueprints()) {
                BoundingBox placedBounds = createBoundingBox(placed.getBlueprint(), placed.getLocation());

                if (blueprintBounds.overlaps(placedBounds)) {
                    return false;
                }
            }
        }

        // 检查最小间距
        for (DungeonInstance.PlacedBlueprint placed : instanceBuilder.getPlacedBlueprints()) {
            double distance = location.distance(placed.getLocation());
            if (distance < config.getMinSpacing()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 创建蓝图的边界框
     *
     * @param blueprint 蓝图
     * @param location 位置
     * @return 边界框
     */
    private BoundingBox createBoundingBox(Blueprint blueprint, Location location) {
        Vector min = location.toVector();
        Vector max = min.clone().add(new Vector(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ()));
        return new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    /**
     * 验证连接
     *
     * @param instance 地牢实例
     * @return 是否验证通过
     */
    private boolean validateConnections(DungeonInstance instance) {
        // 检查是否有出生点
        if (instance.getSpawnLocation() == null) {
            logger.warning("地牢缺少出生点");
            return false;
        }

        // 检查是否有足够的门
        if (instance.getDoorLocations().size() < 2) {
            logger.warning("地牢门数量不足");
            return false;
        }

        // 这里可以添加更复杂的连通性检查
        return true;
    }

    /**
     * 检查蓝图是否与主题兼容
     *
     * @param blueprint 蓝图
     * @param theme 主题
     * @return 是否兼容
     */
    private boolean isThemeCompatible(Blueprint blueprint, DungeonTheme theme) {
        String category = blueprint.getCategory().toLowerCase();
        String themeId = theme.getId().toLowerCase();

        return category.contains(themeId) || category.equals("default") || category.equals("universal");
    }

    /**
     * 根据类型查找房间
     *
     * @param roomBlueprints 房间蓝图列表
     * @param roomType 房间类型
     * @return 找到的房间蓝图，没找到返回null
     */
    private RoomBlueprint findRoomByType(List<RoomBlueprint> roomBlueprints, RoomBlueprint.RoomType roomType) {
        return roomBlueprints.stream()
            .filter(room -> room.getRoomType() == roomType)
            .findFirst()
            .orElse(null);
    }

    /**
     * 创建失败结果
     *
     * @param message 失败消息
     * @param startTime 开始时间
     * @param statsBuilder 统计构建器
     * @return 失败结果
     */
    private PlacementResult createFailureResult(String message, long startTime, GenerationStatistics.Builder statsBuilder) {
        long endTime = System.currentTimeMillis();
        GenerationStatistics statistics = statsBuilder.build(endTime);
        return new PlacementResult(false, message, null, statistics);
    }



    // ==================== Getter 方法 ====================

    public BlueprintManager getBlueprintManager() {
        return blueprintManager;
    }

    public RandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public GenerationConfig getConfig() {
        return config;
    }
}
