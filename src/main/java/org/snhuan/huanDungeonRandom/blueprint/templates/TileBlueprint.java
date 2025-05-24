package org.snhuan.huanDungeonRandom.blueprint.templates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.*;

import java.io.File;
import java.util.*;

/**
 * 瓦片蓝图 - 用于地牢随机生成的基本建筑单元
 *
 * 瓦片蓝图特点：
 * - 必须定义门的位置用于连接
 * - 支持四个方向的旋转
 * - 可以与其他瓦片或房间连接
 * - 通常尺寸较小（推荐16x16以内）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class TileBlueprint extends Blueprint {

    // 瓦片特有属性
    private final Set<String> compatibleTiles;
    private final Map<DoorDirection, Integer> doorLimits;
    private final boolean isCornerTile;
    private final boolean isDeadEndTile;
    private final int weight;

    // 方块数据存储
    private final Map<Vector, Material> blockData;
    private final Set<Vector> airBlocks;

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private TileBlueprint(Builder builder) {
        super(builder.name, BlueprintType.TILE);

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

        this.compatibleTiles = new HashSet<>(builder.compatibleTiles);
        this.doorLimits = new HashMap<>(builder.doorLimits);
        this.isCornerTile = builder.isCornerTile;
        this.isDeadEndTile = builder.isDeadEndTile;
        this.weight = builder.weight;

        this.blockData = new HashMap<>(builder.blockData);
        this.airBlocks = new HashSet<>(builder.airBlocks);
    }

    /**
     * 创建构建器
     *
     * @param name 蓝图名称
     * @param sizeX X轴尺寸
     * @param sizeY Y轴尺寸
     * @param sizeZ Z轴尺寸
     * @return 构建器实例
     */
    public static Builder builder(String name, int sizeX, int sizeY, int sizeZ) {
        return new Builder(name, sizeX, sizeY, sizeZ);
    }

    @Override
    public ValidationResult validate() {
        ValidationResult.Builder resultBuilder = ValidationResult.builder()
            .setMessage("瓦片蓝图验证");

        // 基础验证
        ValidationResult baseValidation = super.validate();
        if (!baseValidation.isValid()) {
            return baseValidation;
        }

        // 瓦片特有验证
        validateTileSpecific(resultBuilder);

        return resultBuilder.build();
    }

    /**
     * 瓦片特有验证
     *
     * @param builder 验证结果构建器
     */
    private void validateTileSpecific(ValidationResult.Builder builder) {
        // 检查尺寸限制
        if (sizeX > 32 || sizeY > 32 || sizeZ > 32) {
            builder.addWarning("瓦片尺寸较大，可能影响性能");
        }

        // 检查门的数量
        if (doors.isEmpty()) {
            builder.addError("瓦片蓝图必须至少有一个门");
        }

        if (doors.size() > 4) {
            builder.addWarning("门的数量过多，可能影响连接效果");
        }

        // 检查死胡同瓦片
        if (isDeadEndTile && doors.size() != 1) {
            builder.addError("死胡同瓦片必须只有一个门");
        }

        // 检查拐角瓦片
        if (isCornerTile && doors.size() != 2) {
            builder.addError("拐角瓦片必须有两个门");
        } else if (isCornerTile && doors.size() == 2) {
            // 检查两个门是否相邻
            DoorDirection dir1 = doors.get(0).getDirection();
            DoorDirection dir2 = doors.get(1).getDirection();
            if (!areDirectionsAdjacent(dir1, dir2)) {
                builder.addError("拐角瓦片的两个门必须在相邻的方向");
            }
        }

        // 检查权重
        if (weight <= 0) {
            builder.addWarning("瓦片权重应该大于0");
        }

        // 检查方块数据
        if (blockData.isEmpty()) {
            builder.addWarning("瓦片没有方块数据，可能是空的");
        }
    }

    /**
     * 检查两个方向是否相邻
     *
     * @param dir1 方向1
     * @param dir2 方向2
     * @return 是否相邻
     */
    private boolean areDirectionsAdjacent(DoorDirection dir1, DoorDirection dir2) {
        return Math.abs(dir1.getAngle() - dir2.getAngle()) == 90 ||
               Math.abs(dir1.getAngle() - dir2.getAngle()) == 270;
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
               .setFunctionCount(0); // 瓦片通常不包含功能点

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

        // 添加建议
        addSuggestions(builder);

        return builder.build();
    }

    /**
     * 计算瓦片复杂度
     *
     * @return 复杂度分数
     */
    private double calculateComplexity() {
        double complexity = 0.0;

        // 基于尺寸
        complexity += (sizeX + sizeY + sizeZ) / 30.0;

        // 基于门的数量
        complexity += doors.size() * 0.5;

        // 基于材料种类
        Set<Material> uniqueMaterials = new HashSet<>(blockData.values());
        complexity += uniqueMaterials.size() * 0.1;

        // 基于方块密度
        double density = (double) blockData.size() / (sizeX * sizeY * sizeZ);
        complexity += density * 2.0;

        return complexity;
    }

    /**
     * 添加建议信息
     *
     * @param builder 预览信息构建器
     */
    private void addSuggestions(PreviewInfo.Builder builder) {
        // 尺寸建议
        if (sizeX > 16 || sizeZ > 16) {
            builder.addSuggestion("考虑减小瓦片尺寸以提高生成效率");
        }

        // 门的建议
        if (doors.size() == 1) {
            builder.addSuggestion("单门瓦片适合作为死胡同使用");
        } else if (doors.size() == 2) {
            builder.addSuggestion("双门瓦片适合作为走廊或拐角使用");
        }

        // 权重建议
        if (weight > 10) {
            builder.addSuggestion("高权重瓦片会更频繁地出现在生成中");
        }

        // 兼容性建议
        if (compatibleTiles.isEmpty()) {
            builder.addSuggestion("设置兼容瓦片可以创建更有趣的组合");
        }
    }

    @Override
    protected void saveTypeSpecificData(YamlConfiguration config) {
        // 保存瓦片特有数据
        config.set("tile.compatible-tiles", new ArrayList<>(compatibleTiles));
        config.set("tile.is-corner", isCornerTile);
        config.set("tile.is-dead-end", isDeadEndTile);
        config.set("tile.weight", weight);

        // 保存门限制
        if (!doorLimits.isEmpty()) {
            ConfigurationSection limitsSection = config.createSection("tile.door-limits");
            for (Map.Entry<DoorDirection, Integer> entry : doorLimits.entrySet()) {
                limitsSection.set(entry.getKey().name(), entry.getValue());
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
            // 加载瓦片特有数据
            List<String> compatibleList = config.getStringList("tile.compatible-tiles");
            compatibleTiles.clear();
            compatibleTiles.addAll(compatibleList);

            // 加载门限制
            ConfigurationSection limitsSection = config.getConfigurationSection("tile.door-limits");
            if (limitsSection != null) {
                doorLimits.clear();
                for (String key : limitsSection.getKeys(false)) {
                    try {
                        DoorDirection direction = DoorDirection.valueOf(key);
                        int limit = limitsSection.getInt(key);
                        doorLimits.put(direction, limit);
                    } catch (IllegalArgumentException e) {
                        logger.warning("无效的门方向: " + key);
                    }
                }
            }

            // 加载方块数据
            loadBlockData(config);

        } catch (Exception e) {
            logger.severe("加载瓦片蓝图自定义数据失败: " + e.getMessage());
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

    // ==================== Getter 方法 ====================

    /**
     * 获取兼容的瓦片ID集合
     *
     * @return 兼容瓦片ID集合的副本
     */
    public Set<String> getCompatibleTiles() {
        return new HashSet<>(compatibleTiles);
    }

    /**
     * 获取门限制
     *
     * @return 门限制的副本
     */
    public Map<DoorDirection, Integer> getDoorLimits() {
        return new HashMap<>(doorLimits);
    }

    /**
     * 是否为拐角瓦片
     *
     * @return 是否为拐角瓦片
     */
    public boolean isCornerTile() {
        return isCornerTile;
    }

    /**
     * 是否为死胡同瓦片
     *
     * @return 是否为死胡同瓦片
     */
    public boolean isDeadEndTile() {
        return isDeadEndTile;
    }

    /**
     * 获取瓦片权重
     *
     * @return 权重值
     */
    public int getWeight() {
        return weight;
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
     * 检查是否与指定瓦片兼容
     *
     * @param tileId 瓦片ID
     * @return 是否兼容
     */
    public boolean isCompatibleWith(String tileId) {
        return compatibleTiles.isEmpty() || compatibleTiles.contains(tileId);
    }

    /**
     * 获取指定方向的门限制
     *
     * @param direction 门方向
     * @return 门限制数量，无限制返回-1
     */
    public int getDoorLimit(DoorDirection direction) {
        return doorLimits.getOrDefault(direction, -1);
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

    @Override
    public boolean place(org.bukkit.World world, Location location, int rotation) {
        // TODO: 实现瓦片放置逻辑
        // 这里应该根据blockData在世界中放置方块
        logger.info("放置瓦片蓝图: " + name + " 在位置: " + location);
        return true;
    }

    @Override
    public Blueprint clone() {
        Builder builder = builder(name + "_copy", sizeX, sizeY, sizeZ)
            .setDescription(description)
            .setAuthor(author)
            .setCategory(category)
            .setCornerTile(isCornerTile)
            .setDeadEndTile(isDeadEndTile)
            .setWeight(weight);

        // 复制门信息
        for (DoorInfo door : doors) {
            builder.addDoor(door);
        }

        // 复制兼容瓦片
        for (String tile : compatibleTiles) {
            builder.addCompatibleTile(tile);
        }

        // 复制门限制
        for (Map.Entry<DoorDirection, Integer> entry : doorLimits.entrySet()) {
            builder.setDoorLimit(entry.getKey(), entry.getValue());
        }

        // 复制方块数据
        builder.setBlockData(blockData);

        return builder.build();
    }

    // ==================== 构建器类 ====================

    /**
     * 瓦片蓝图构建器
     */
    public static class Builder {
        private final String name;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;

        private String description = "";
        private String author = "";
        private String category = "default";

        private final List<DoorInfo> doors = new ArrayList<>();
        private final Set<String> compatibleTiles = new HashSet<>();
        private final Map<DoorDirection, Integer> doorLimits = new HashMap<>();
        private boolean isCornerTile = false;
        private boolean isDeadEndTile = false;
        private int weight = 1;

        private final Map<Vector, Material> blockData = new HashMap<>();
        private final Set<Vector> airBlocks = new HashSet<>();

        public Builder(String name, int sizeX, int sizeY, int sizeZ) {
            this.name = name;
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

        public Builder addCompatibleTile(String tileId) {
            if (tileId != null && !tileId.trim().isEmpty()) {
                this.compatibleTiles.add(tileId);
            }
            return this;
        }

        public Builder setDoorLimit(DoorDirection direction, int limit) {
            if (direction != null && limit > 0) {
                this.doorLimits.put(direction, limit);
            }
            return this;
        }

        public Builder setCornerTile(boolean isCorner) {
            this.isCornerTile = isCorner;
            return this;
        }

        public Builder setDeadEndTile(boolean isDeadEnd) {
            this.isDeadEndTile = isDeadEnd;
            return this;
        }

        public Builder setWeight(int weight) {
            this.weight = Math.max(1, weight);
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

        public TileBlueprint build() {
            return new TileBlueprint(this);
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
            // 这里应该实现实际的瓦片构建逻辑
            // 暂时返回true表示构建成功
            logger.info("构建瓦片蓝图: " + getName() + " 在位置: " + formatLocation(location));

            // TODO: 实现实际的方块放置逻辑
            // 1. 根据蓝图数据构建瓦片结构
            // 2. 放置门的位置
            // 3. 处理瓦片连接

            return true;

        } catch (Exception e) {
            logger.severe("构建瓦片蓝图时发生异常: " + getName() + " - " + e.getMessage());
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
