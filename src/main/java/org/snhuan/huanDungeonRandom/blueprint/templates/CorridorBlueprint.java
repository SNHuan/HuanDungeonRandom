package org.snhuan.huanDungeonRandom.blueprint.templates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.*;

import java.util.*;

/**
 * 走廊蓝图 - 用于连接房间和瓦片的通道结构
 *
 * 走廊蓝图特点：
 * - 用于连接两个房间或瓦片
 * - 通常是线性结构
 * - 两端必须有门
 * - 支持不同的走廊样式（直线、弯曲、带装饰等）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class CorridorBlueprint extends Blueprint {

    // 走廊特有属性
    private final CorridorType corridorType;
    private final int length;
    private final boolean allowBranching;
    private final Set<String> compatibleRooms;
    private final Set<String> compatibleTiles;
    private final int priority;

    // 路径信息
    private final List<Vector> pathPoints;
    private final Map<Vector, PathType> pathTypes;

    // 方块数据存储
    private final Map<Vector, Material> blockData;
    private final Set<Vector> airBlocks;

    /**
     * 走廊类型枚举
     */
    public enum CorridorType {
        STRAIGHT("直线走廊", 1),
        L_SHAPED("L型走廊", 2),
        T_JUNCTION("T型路口", 3),
        CROSS("十字路口", 4),
        CURVED("弯曲走廊", 2),
        DECORATED("装饰走廊", 2);

        private final String displayName;
        private final int complexity;

        CorridorType(String displayName, int complexity) {
            this.displayName = displayName;
            this.complexity = complexity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getComplexity() {
            return complexity;
        }
    }

    /**
     * 路径类型枚举
     */
    public enum PathType {
        FLOOR("地板"),
        WALL("墙壁"),
        CEILING("天花板"),
        DECORATION("装饰");

        private final String displayName;

        PathType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private CorridorBlueprint(Builder builder) {
        super(builder.name, BlueprintType.CORRIDOR);

        // 设置基本属性
        this.setDescription(builder.description);
        this.setAuthor(builder.author);
        this.setCategory(builder.category);

        this.sizeX = builder.sizeX;
        this.sizeY = builder.sizeY;
        this.sizeZ = builder.sizeZ;

        // 添加门信息
        for (DoorInfo door : builder.doors) {
            this.addDoor(door);
        }

        this.corridorType = builder.corridorType;
        this.length = builder.length;
        this.allowBranching = builder.allowBranching;
        this.compatibleRooms = new HashSet<>(builder.compatibleRooms);
        this.compatibleTiles = new HashSet<>(builder.compatibleTiles);
        this.priority = builder.priority;

        this.pathPoints = new ArrayList<>(builder.pathPoints);
        this.pathTypes = new HashMap<>(builder.pathTypes);

        this.blockData = new HashMap<>(builder.blockData);
        this.airBlocks = new HashSet<>(builder.airBlocks);
    }

    /**
     * 创建构建器
     *
     * @param name 走廊名称
     * @param corridorType 走廊类型
     * @param length 走廊长度
     * @param sizeX X轴尺寸
     * @param sizeY Y轴尺寸
     * @param sizeZ Z轴尺寸
     * @return 构建器实例
     */
    public static Builder builder(String name, CorridorType corridorType, int length, int sizeX, int sizeY, int sizeZ) {
        return new Builder(name, corridorType, length, sizeX, sizeY, sizeZ);
    }

    @Override
    public ValidationResult validate() {
        ValidationResult.Builder resultBuilder = ValidationResult.builder()
            .setMessage("走廊蓝图验证");

        // 基础验证
        ValidationResult baseValidation = super.validate();
        if (!baseValidation.isValid()) {
            return baseValidation;
        }

        // 走廊特有验证
        validateCorridorSpecific(resultBuilder);

        return resultBuilder.build();
    }

    /**
     * 走廊特有验证
     *
     * @param builder 验证结果构建器
     */
    private void validateCorridorSpecific(ValidationResult.Builder builder) {
        // 检查门的数量
        if (doors.size() < 2) {
            builder.addError("走廊必须至少有两个门");
        }

        // 检查走廊类型特定要求
        validateCorridorTypeRequirements(builder);

        // 检查长度
        if (length <= 0) {
            builder.addError("走廊长度必须大于0");
        }

        if (length > Math.max(sizeX, sizeZ)) {
            builder.addWarning("走廊长度超过了蓝图尺寸");
        }

        // 检查路径点
        if (pathPoints.isEmpty()) {
            builder.addWarning("走廊没有定义路径点");
        }

        // 检查优先级
        if (priority <= 0) {
            builder.addWarning("走廊优先级应该大于0");
        }

        // 检查尺寸合理性
        if (sizeY < 3) {
            builder.addWarning("走廊高度过低，可能影响通行");
        }

        if (Math.min(sizeX, sizeZ) < 3) {
            builder.addWarning("走廊宽度过窄，可能影响通行");
        }
    }

    /**
     * 验证走廊类型特定要求
     *
     * @param builder 验证结果构建器
     */
    private void validateCorridorTypeRequirements(ValidationResult.Builder builder) {
        switch (corridorType) {
            case STRAIGHT:
                if (doors.size() != 2) {
                    builder.addError("直线走廊必须有且仅有两个门");
                }
                break;

            case L_SHAPED:
                if (doors.size() != 2) {
                    builder.addError("L型走廊必须有且仅有两个门");
                }
                // 检查门是否在相邻的边
                if (doors.size() == 2) {
                    DoorDirection dir1 = doors.get(0).getDirection();
                    DoorDirection dir2 = doors.get(1).getDirection();
                    if (dir1.isOpposite(dir2)) {
                        builder.addError("L型走廊的门不能在相对的方向");
                    }
                }
                break;

            case T_JUNCTION:
                if (doors.size() != 3) {
                    builder.addError("T型路口必须有且仅有三个门");
                }
                break;

            case CROSS:
                if (doors.size() != 4) {
                    builder.addError("十字路口必须有且仅有四个门");
                }
                break;

            case CURVED:
                if (doors.size() != 2) {
                    builder.addError("弯曲走廊必须有且仅有两个门");
                }
                if (pathPoints.size() < 3) {
                    builder.addWarning("弯曲走廊建议定义更多路径点以体现弯曲效果");
                }
                break;

            case DECORATED:
                if (blockData.isEmpty()) {
                    builder.addWarning("装饰走廊建议添加装饰方块");
                }
                break;
        }
    }

    @Override
    public PreviewInfo getPreviewInfo() {
        PreviewInfo.Builder builder = PreviewInfo.builder(
            getName(),
            getType(),
            new Vector(sizeX, sizeY, sizeZ)
        );

        // 统计方块信息
        int totalBlocks = sizeX * sizeY * sizeZ;
        int solidBlocks = blockData.size();
        int airBlocks = this.airBlocks.size();

        builder.setBlockCounts(totalBlocks, airBlocks, solidBlocks)
               .setDoorCount(doors.size())
               .setFunctionCount(0); // 走廊通常不包含功能点

        // 统计材料
        Map<Material, Integer> materialCounts = new HashMap<>();
        for (Material material : blockData.values()) {
            materialCounts.merge(material, 1, Integer::sum);
        }

        for (Map.Entry<Material, Integer> entry : materialCounts.entrySet()) {
            builder.addMaterial(entry.getKey(), entry.getValue());
        }

        // 计算复杂度
        double complexity = calculateComplexity();
        builder.setComplexity(complexity);

        // 添加建议和信息
        addSuggestionsAndInfo(builder);

        return builder.build();
    }

    /**
     * 计算走廊复杂度
     *
     * @return 复杂度分数
     */
    private double calculateComplexity() {
        double complexity = 0.0;

        // 基于走廊类型
        complexity += corridorType.getComplexity() * 0.5;

        // 基于长度
        complexity += length / 10.0;

        // 基于尺寸
        complexity += (sizeX + sizeY + sizeZ) / 30.0;

        // 基于门的数量
        complexity += doors.size() * 0.3;

        // 基于路径点数量
        complexity += pathPoints.size() * 0.1;

        // 基于材料种类
        Set<Material> uniqueMaterials = new HashSet<>(blockData.values());
        complexity += uniqueMaterials.size() * 0.1;

        // 基于分支支持
        if (allowBranching) {
            complexity += 0.5;
        }

        return complexity;
    }

    /**
     * 添加建议和信息
     *
     * @param builder 预览信息构建器
     */
    private void addSuggestionsAndInfo(PreviewInfo.Builder builder) {
        // 走廊类型信息
        builder.addInfo("走廊类型: " + corridorType.getDisplayName());
        builder.addInfo("走廊长度: " + length);

        if (allowBranching) {
            builder.addInfo("支持分支连接");
        }

        // 连接建议
        if (compatibleRooms.isEmpty() && compatibleTiles.isEmpty()) {
            builder.addSuggestion("设置兼容的房间和瓦片可以优化连接效果");
        }

        // 尺寸建议
        if (length > 20) {
            builder.addSuggestion("考虑在长走廊中添加装饰以避免单调");
        }

        // 优先级建议
        if (priority > 5) {
            builder.addInfo("高优先级走廊会优先被选择");
        }

        // 路径建议
        if (pathPoints.size() < 2) {
            builder.addSuggestion("定义路径点可以帮助生成算法更好地连接");
        }
    }

    @Override
    protected void saveTypeSpecificData(YamlConfiguration config) {
        // 保存走廊特有数据
        config.set("corridor.type", corridorType.name());
        config.set("corridor.length", length);
        config.set("corridor.allow-branching", allowBranching);
        config.set("corridor.priority", priority);

        // 保存兼容性信息
        if (!compatibleRooms.isEmpty()) {
            config.set("corridor.compatible-rooms", new ArrayList<>(compatibleRooms));
        }
        if (!compatibleTiles.isEmpty()) {
            config.set("corridor.compatible-tiles", new ArrayList<>(compatibleTiles));
        }

        // 保存路径信息
        if (!pathPoints.isEmpty()) {
            List<String> pathPointStrings = new ArrayList<>();
            for (Vector point : pathPoints) {
                pathPointStrings.add(point.getBlockX() + "," + point.getBlockY() + "," + point.getBlockZ());
            }
            config.set("corridor.path-points", pathPointStrings);
        }

        // 保存路径类型
        if (!pathTypes.isEmpty()) {
            ConfigurationSection pathTypesSection = config.createSection("corridor.path-types");
            for (Map.Entry<Vector, PathType> entry : pathTypes.entrySet()) {
                Vector pos = entry.getKey();
                String key = pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ();
                pathTypesSection.set(key, entry.getValue().name());
            }
        }

        // 保存方块数据
        saveBlockData(config);
    }

    /**
     * 保存方块数据
     *
     * @param config 配置对象
     */
    private void saveBlockData(YamlConfiguration config) {
        ConfigurationSection blocksSection = config.createSection("blocks");

        for (Map.Entry<Vector, Material> entry : blockData.entrySet()) {
            Vector pos = entry.getKey();
            String key = pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ();
            blocksSection.set(key, entry.getValue().name());
        }

        // 保存空气方块位置
        List<String> airPositions = new ArrayList<>();
        for (Vector pos : airBlocks) {
            airPositions.add(pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ());
        }
        config.set("air-blocks", airPositions);
    }

    @Override
    protected void loadTypeSpecificData(YamlConfiguration config) {
        try {
            // 加载兼容性信息
            compatibleRooms.clear();
            compatibleRooms.addAll(config.getStringList("corridor.compatible-rooms"));

            compatibleTiles.clear();
            compatibleTiles.addAll(config.getStringList("corridor.compatible-tiles"));

            // 加载路径点
            pathPoints.clear();
            List<String> pathPointStrings = config.getStringList("corridor.path-points");
            for (String pointStr : pathPointStrings) {
                String[] coords = pointStr.split(",");
                if (coords.length == 3) {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    pathPoints.add(new Vector(x, y, z));
                }
            }

            // 加载路径类型
            pathTypes.clear();
            ConfigurationSection pathTypesSection = config.getConfigurationSection("corridor.path-types");
            if (pathTypesSection != null) {
                for (String key : pathTypesSection.getKeys(false)) {
                    String[] coords = key.split(",");
                    if (coords.length == 3) {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);

                        String pathTypeName = pathTypesSection.getString(key);
                        PathType pathType = PathType.valueOf(pathTypeName);

                        pathTypes.put(new Vector(x, y, z), pathType);
                    }
                }
            }

            // 加载方块数据
            loadBlockData(config);

        } catch (Exception e) {
            logger.severe("加载走廊蓝图自定义数据失败: " + e.getMessage());
        }
    }

    /**
     * 加载方块数据
     *
     * @param config 配置对象
     */
    private void loadBlockData(YamlConfiguration config) {
        try {
            blockData.clear();
            airBlocks.clear();

            // 加载方块数据
            ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
            if (blocksSection != null) {
                for (String key : blocksSection.getKeys(false)) {
                    String[] coords = key.split(",");
                    if (coords.length == 3) {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);

                        String materialName = blocksSection.getString(key);
                        Material material = Material.valueOf(materialName);

                        blockData.put(new Vector(x, y, z), material);
                    }
                }
            }

            // 加载空气方块
            List<String> airPositions = config.getStringList("air-blocks");
            for (String pos : airPositions) {
                String[] coords = pos.split(",");
                if (coords.length == 3) {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    airBlocks.add(new Vector(x, y, z));
                }
            }

        } catch (Exception e) {
            logger.severe("加载方块数据失败: " + e.getMessage());
        }
    }

    @Override
    public boolean place(World world, Location location, int rotation) {
        if (world == null || location == null) {
            return false;
        }

        try {
            // 放置方块数据
            for (Map.Entry<Vector, Material> entry : blockData.entrySet()) {
                Vector pos = entry.getKey();
                Vector rotatedPos = rotateVector(pos, rotation);
                Location blockLocation = location.clone().add(rotatedPos);

                world.getBlockAt(blockLocation).setType(entry.getValue());
            }

            // 设置空气方块
            for (Vector pos : airBlocks) {
                Vector rotatedPos = rotateVector(pos, rotation);
                Location blockLocation = location.clone().add(rotatedPos);

                world.getBlockAt(blockLocation).setType(Material.AIR);
            }

            return true;
        } catch (Exception e) {
            logger.severe("放置走廊蓝图失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 旋转向量
     */
    private Vector rotateVector(Vector vector, int rotation) {
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double newX = vector.getX() * cos - vector.getZ() * sin;
        double newZ = vector.getX() * sin + vector.getZ() * cos;

        return new Vector(Math.round(newX), vector.getY(), Math.round(newZ));
    }

    @Override
    public Blueprint clone() {
        return builder(this.getName(), this.corridorType, this.length, this.sizeX, this.sizeY, this.sizeZ)
            .setDescription(this.getDescription())
            .setAuthor(this.getAuthor())
            .setCategory(this.getCategory())
            .setAllowBranching(this.allowBranching)
            .setPriority(this.priority)
            .setBlockData(this.blockData)
            .build();
    }

    // ==================== Getter 方法 ====================

    /**
     * 获取走廊类型
     *
     * @return 走廊类型
     */
    public CorridorType getCorridorType() {
        return corridorType;
    }

    /**
     * 获取走廊长度
     *
     * @return 走廊长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 是否允许分支
     *
     * @return 是否允许分支
     */
    public boolean isAllowBranching() {
        return allowBranching;
    }

    /**
     * 获取兼容的房间ID集合
     *
     * @return 兼容房间ID集合的副本
     */
    public Set<String> getCompatibleRooms() {
        return new HashSet<>(compatibleRooms);
    }

    /**
     * 获取兼容的瓦片ID集合
     *
     * @return 兼容瓦片ID集合的副本
     */
    public Set<String> getCompatibleTiles() {
        return new HashSet<>(compatibleTiles);
    }

    /**
     * 获取优先级
     *
     * @return 优先级值
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 获取路径点列表
     *
     * @return 路径点列表的副本
     */
    public List<Vector> getPathPoints() {
        return new ArrayList<>(pathPoints);
    }

    /**
     * 获取路径类型映射
     *
     * @return 路径类型映射的副本
     */
    public Map<Vector, PathType> getPathTypes() {
        return new HashMap<>(pathTypes);
    }

    /**
     * 获取方块数据
     *
     * @return 方块数据的副本
     */
    public Map<Vector, Material> getBlockData() {
        return new HashMap<>(blockData);
    }

    /**
     * 获取空气方块位置
     *
     * @return 空气方块位置的副本
     */
    public Set<Vector> getAirBlocks() {
        return new HashSet<>(airBlocks);
    }

    /**
     * 检查是否与指定房间兼容
     *
     * @param roomId 房间ID
     * @return 是否兼容
     */
    public boolean isCompatibleWithRoom(String roomId) {
        return compatibleRooms.isEmpty() || compatibleRooms.contains(roomId);
    }

    /**
     * 检查是否与指定瓦片兼容
     *
     * @param tileId 瓦片ID
     * @return 是否兼容
     */
    public boolean isCompatibleWithTile(String tileId) {
        return compatibleTiles.isEmpty() || compatibleTiles.contains(tileId);
    }

    /**
     * 获取指定位置的方块材料
     *
     * @param position 位置
     * @return 方块材料，空气或不存在返回null
     */
    public Material getBlockAt(Vector position) {
        return blockData.get(position);
    }

    /**
     * 检查指定位置是否为空气
     *
     * @param position 位置
     * @return 是否为空气
     */
    public boolean isAirAt(Vector position) {
        return airBlocks.contains(position);
    }

    /**
     * 获取指定位置的路径类型
     *
     * @param position 位置
     * @return 路径类型，不存在返回null
     */
    public PathType getPathTypeAt(Vector position) {
        return pathTypes.get(position);
    }

    // ==================== 构建器类 ====================

    /**
     * 走廊蓝图构建器
     */
    public static class Builder {
        private final String name;
        private final CorridorType corridorType;
        private final int length;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;

        private String description = "";
        private String author = "";
        private String category = "default";

        private final List<DoorInfo> doors = new ArrayList<>();
        private boolean allowBranching = false;
        private final Set<String> compatibleRooms = new HashSet<>();
        private final Set<String> compatibleTiles = new HashSet<>();
        private int priority = 1;

        private final List<Vector> pathPoints = new ArrayList<>();
        private final Map<Vector, PathType> pathTypes = new HashMap<>();

        private final Map<Vector, Material> blockData = new HashMap<>();
        private final Set<Vector> airBlocks = new HashSet<>();

        public Builder(String name, CorridorType corridorType, int length, int sizeX, int sizeY, int sizeZ) {
            this.name = name;
            this.corridorType = corridorType;
            this.length = length;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }

        public Builder setDescription(String description) {
            this.description = description != null ? description : "";
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author != null ? author : "";
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category != null ? category : "default";
            return this;
        }

        public Builder addDoor(DoorInfo door) {
            if (door != null) {
                this.doors.add(door);
            }
            return this;
        }

        public Builder addDoor(int x, int y, int z, DoorDirection direction) {
            return addDoor(new DoorInfo(x, y, z, direction));
        }

        public Builder setAllowBranching(boolean allowBranching) {
            this.allowBranching = allowBranching;
            return this;
        }

        public Builder addCompatibleRoom(String roomId) {
            if (roomId != null && !roomId.trim().isEmpty()) {
                this.compatibleRooms.add(roomId);
            }
            return this;
        }

        public Builder addCompatibleTile(String tileId) {
            if (tileId != null && !tileId.trim().isEmpty()) {
                this.compatibleTiles.add(tileId);
            }
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = Math.max(1, priority);
            return this;
        }

        public Builder addPathPoint(int x, int y, int z) {
            this.pathPoints.add(new Vector(x, y, z));
            return this;
        }

        public Builder addPathPoint(Vector point) {
            if (point != null) {
                this.pathPoints.add(point.clone());
            }
            return this;
        }

        public Builder setPathType(int x, int y, int z, PathType pathType) {
            if (pathType != null) {
                this.pathTypes.put(new Vector(x, y, z), pathType);
            }
            return this;
        }

        public Builder setPathType(Vector position, PathType pathType) {
            if (position != null && pathType != null) {
                this.pathTypes.put(position.clone(), pathType);
            }
            return this;
        }

        public Builder setBlock(int x, int y, int z, Material material) {
            if (material != null && material != Material.AIR) {
                this.blockData.put(new Vector(x, y, z), material);
                this.airBlocks.remove(new Vector(x, y, z));
            }
            return this;
        }

        public Builder setAir(int x, int y, int z) {
            Vector pos = new Vector(x, y, z);
            this.airBlocks.add(pos);
            this.blockData.remove(pos);
            return this;
        }

        public Builder setBlockData(Map<Vector, Material> blockData) {
            if (blockData != null) {
                this.blockData.clear();
                this.blockData.putAll(blockData);
            }
            return this;
        }

        public CorridorBlueprint build() {
            return new CorridorBlueprint(this);
        }
    }

    /**
     * 实现具体的构建逻辑
     *
     * @param location 构建位置
     * @return 是否构建成功
     */
    @Override
    protected boolean buildSpecific(org.bukkit.Location location) {
        try {
            // 这里应该实现实际的走廊构建逻辑
            // 暂时返回true表示构建成功
            logger.info("构建走廊蓝图: " + getName() + " 在位置: " + formatLocation(location));

            // TODO: 实现实际的方块放置逻辑
            // 1. 根据蓝图数据构建走廊结构
            // 2. 放置路径点
            // 3. 处理走廊连接

            return true;

        } catch (Exception e) {
            logger.severe("构建走廊蓝图时发生异常: " + getName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 格式化位置信息
     *
     * @param location 位置
     * @return 格式化字符串
     */
    private String formatLocation(org.bukkit.Location location) {
        return String.format("%s: %.1f, %.1f, %.1f",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ());
    }
}
