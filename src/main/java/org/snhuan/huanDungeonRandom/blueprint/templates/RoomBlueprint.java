package org.snhuan.huanDungeonRandom.blueprint.templates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.*;

import java.util.*;

/**
 * 房间蓝图 - 特殊的房间结构，通常包含特定功能
 *
 * 房间蓝图特点：
 * - 通常比瓦片更大
 * - 可能包含特殊功能点
 * - 支持门连接
 * - 可以设置为特殊房间类型（如宝藏室、BOSS房等）
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class RoomBlueprint extends Blueprint {

    // 房间特有属性
    private final RoomType roomType;
    private final int minConnections;
    private final int maxConnections;
    private final boolean isSpecialRoom;
    private final String requiredKey;
    private final int rarity;

    // 功能点位置
    private final List<Vector> functionPoints;
    private final Map<String, Vector> namedLocations;

    // 方块数据存储
    private final Map<Vector, Material> blockData;
    private final Set<Vector> airBlocks;

    /**
     * 房间类型枚举
     */
    public enum RoomType {
        NORMAL("普通房间", 1),
        TREASURE("宝藏室", 5),
        BOSS("BOSS房", 10),
        SPAWN("出生点", 1),
        EXIT("出口", 1),
        PUZZLE("谜题房", 3),
        SHOP("商店", 2),
        REST("休息室", 2);

        private final String displayName;
        private final int defaultRarity;

        RoomType(String displayName, int defaultRarity) {
            this.displayName = displayName;
            this.defaultRarity = defaultRarity;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDefaultRarity() {
            return defaultRarity;
        }
    }

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private RoomBlueprint(Builder builder) {
        super(builder.name, BlueprintType.ROOM);

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

        this.roomType = builder.roomType;
        this.minConnections = builder.minConnections;
        this.maxConnections = builder.maxConnections;
        this.isSpecialRoom = builder.isSpecialRoom;
        this.requiredKey = builder.requiredKey;
        this.rarity = builder.rarity;

        this.functionPoints = new ArrayList<>(builder.functionPoints);
        this.namedLocations = new HashMap<>(builder.namedLocations);

        this.blockData = new HashMap<>(builder.blockData);
        this.airBlocks = new HashSet<>(builder.airBlocks);
    }

    /**
     * 创建构建器
     *
     * @param name 房间名称
     * @param roomType 房间类型
     * @param sizeX X轴尺寸
     * @param sizeY Y轴尺寸
     * @param sizeZ Z轴尺寸
     * @return 构建器实例
     */
    public static Builder builder(String name, RoomType roomType, int sizeX, int sizeY, int sizeZ) {
        return new Builder(name, roomType, sizeX, sizeY, sizeZ);
    }

    @Override
    public ValidationResult validate() {
        ValidationResult.Builder resultBuilder = ValidationResult.builder()
            .setMessage("房间蓝图验证");

        // 基础验证
        ValidationResult baseValidation = super.validate();
        if (!baseValidation.isValid()) {
            return baseValidation;
        }

        // 房间特有验证
        validateRoomSpecific(resultBuilder);

        return resultBuilder.build();
    }

    /**
     * 房间特有验证
     *
     * @param builder 验证结果构建器
     */
    private void validateRoomSpecific(ValidationResult.Builder builder) {
        // 检查连接数量
        if (minConnections > maxConnections) {
            builder.addError("最小连接数不能大于最大连接数");
        }

        if (doors.size() < minConnections) {
            builder.addError("门的数量少于最小连接数要求");
        }

        if (doors.size() > maxConnections) {
            builder.addWarning("门的数量超过最大连接数");
        }

        // 检查特殊房间
        if (isSpecialRoom) {
            if (roomType == RoomType.NORMAL) {
                builder.addWarning("普通房间不应该标记为特殊房间");
            }

            if (functionPoints.isEmpty()) {
                builder.addWarning("特殊房间建议添加功能点");
            }
        }

        // 检查房间类型特定要求
        validateRoomTypeRequirements(builder);

        // 检查稀有度
        if (rarity <= 0) {
            builder.addError("稀有度必须大于0");
        }

        // 检查尺寸
        if (sizeX < 5 || sizeZ < 5) {
            builder.addWarning("房间尺寸较小，可能影响使用效果");
        }

        if (sizeX > 64 || sizeY > 64 || sizeZ > 64) {
            builder.addWarning("房间尺寸过大，可能影响性能");
        }
    }

    /**
     * 验证房间类型特定要求
     *
     * @param builder 验证结果构建器
     */
    private void validateRoomTypeRequirements(ValidationResult.Builder builder) {
        switch (roomType) {
            case BOSS:
                if (doors.size() != 1) {
                    builder.addWarning("BOSS房建议只有一个入口");
                }
                if (!namedLocations.containsKey("boss_spawn")) {
                    builder.addWarning("BOSS房建议设置boss_spawn位置");
                }
                break;

            case TREASURE:
                if (!namedLocations.containsKey("treasure_location")) {
                    builder.addWarning("宝藏室建议设置treasure_location位置");
                }
                break;

            case SPAWN:
                if (!namedLocations.containsKey("player_spawn")) {
                    builder.addError("出生点房间必须设置player_spawn位置");
                }
                break;

            case EXIT:
                if (!namedLocations.containsKey("exit_portal")) {
                    builder.addWarning("出口房间建议设置exit_portal位置");
                }
                break;

            case PUZZLE:
                if (functionPoints.isEmpty()) {
                    builder.addWarning("谜题房建议添加功能点");
                }
                break;

            case SHOP:
                if (!namedLocations.containsKey("shop_keeper")) {
                    builder.addWarning("商店建议设置shop_keeper位置");
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
               .setFunctionCount(functionPoints.size());

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

        // 添加建议和警告
        addSuggestionsAndWarnings(builder);

        return builder.build();
    }

    /**
     * 计算房间复杂度
     *
     * @return 复杂度分数
     */
    private double calculateComplexity() {
        double complexity = 0.0;

        // 基于尺寸
        complexity += (sizeX + sizeY + sizeZ) / 50.0;

        // 基于房间类型
        complexity += roomType.getDefaultRarity() * 0.3;

        // 基于功能点数量
        complexity += functionPoints.size() * 0.5;

        // 基于门的数量
        complexity += doors.size() * 0.3;

        // 基于材料种类
        Set<Material> uniqueMaterials = new HashSet<>(blockData.values());
        complexity += uniqueMaterials.size() * 0.1;

        // 基于命名位置数量
        complexity += namedLocations.size() * 0.2;

        return complexity;
    }

    /**
     * 添加建议和警告信息
     *
     * @param builder 预览信息构建器
     */
    private void addSuggestionsAndWarnings(PreviewInfo.Builder builder) {
        // 房间类型建议
        builder.addInfo("房间类型: " + roomType.getDisplayName());

        if (isSpecialRoom) {
            builder.addInfo("这是一个特殊房间");
        }

        // 连接建议
        if (doors.size() == 1) {
            builder.addSuggestion("单门房间适合作为终点房间");
        } else if (doors.size() > 4) {
            builder.addWarning("门数量过多可能影响房间的私密性");
        }

        // 功能点建议
        if (functionPoints.isEmpty() && roomType != RoomType.NORMAL) {
            builder.addSuggestion("考虑为特殊房间添加功能点");
        }

        // 稀有度建议
        if (rarity > 10) {
            builder.addInfo("高稀有度房间会很少出现");
        }

        // 尺寸建议
        int area = sizeX * sizeZ;
        if (area < 25) {
            builder.addSuggestion("考虑增大房间面积以提供更好的体验");
        } else if (area > 400) {
            builder.addWarning("房间面积过大可能影响性能");
        }
    }

    @Override
    protected void saveTypeSpecificData(YamlConfiguration config) {
        // 保存房间特有数据
        config.set("room.type", roomType.name());
        config.set("room.min-connections", minConnections);
        config.set("room.max-connections", maxConnections);
        config.set("room.is-special", isSpecialRoom);
        config.set("room.required-key", requiredKey);
        config.set("room.rarity", rarity);

        // 保存功能点
        if (!functionPoints.isEmpty()) {
            List<String> functionPointStrings = new ArrayList<>();
            for (Vector point : functionPoints) {
                functionPointStrings.add(point.getBlockX() + "," + point.getBlockY() + "," + point.getBlockZ());
            }
            config.set("room.function-points", functionPointStrings);
        }

        // 保存命名位置
        if (!namedLocations.isEmpty()) {
            ConfigurationSection locationsSection = config.createSection("room.named-locations");
            for (Map.Entry<String, Vector> entry : namedLocations.entrySet()) {
                Vector pos = entry.getValue();
                locationsSection.set(entry.getKey(), pos.getBlockX() + "," + pos.getBlockY() + "," + pos.getBlockZ());
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
            // 加载功能点
            functionPoints.clear();
            List<String> functionPointStrings = config.getStringList("room.function-points");
            for (String pointStr : functionPointStrings) {
                String[] coords = pointStr.split(",");
                if (coords.length == 3) {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    int z = Integer.parseInt(coords[2]);
                    functionPoints.add(new Vector(x, y, z));
                }
            }

            // 加载命名位置
            namedLocations.clear();
            ConfigurationSection locationsSection = config.getConfigurationSection("room.named-locations");
            if (locationsSection != null) {
                for (String name : locationsSection.getKeys(false)) {
                    String posStr = locationsSection.getString(name);
                    if (posStr != null) {
                        String[] coords = posStr.split(",");
                        if (coords.length == 3) {
                            int x = Integer.parseInt(coords[0]);
                            int y = Integer.parseInt(coords[1]);
                            int z = Integer.parseInt(coords[2]);
                            namedLocations.put(name, new Vector(x, y, z));
                        }
                    }
                }
            }

            // 加载方块数据
            loadBlockData(config);

        } catch (Exception e) {
            logger.severe("加载房间蓝图自定义数据失败: " + e.getMessage());
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
     * 获取房间类型
     *
     * @return 房间类型
     */
    public RoomType getRoomType() {
        return roomType;
    }

    /**
     * 获取最小连接数
     *
     * @return 最小连接数
     */
    public int getMinConnections() {
        return minConnections;
    }

    /**
     * 获取最大连接数
     *
     * @return 最大连接数
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * 是否为特殊房间
     *
     * @return 是否为特殊房间
     */
    public boolean isSpecialRoom() {
        return isSpecialRoom;
    }

    /**
     * 获取所需钥匙
     *
     * @return 所需钥匙，无要求返回null
     */
    public String getRequiredKey() {
        return requiredKey;
    }

    /**
     * 获取稀有度
     *
     * @return 稀有度值
     */
    public int getRarity() {
        return rarity;
    }

    /**
     * 获取功能点列表
     *
     * @return 功能点列表的副本
     */
    public List<Vector> getFunctionPoints() {
        return new ArrayList<>(functionPoints);
    }

    /**
     * 获取命名位置
     *
     * @return 命名位置的副本
     */
    public Map<String, Vector> getNamedLocations() {
        return new HashMap<>(namedLocations);
    }

    /**
     * 获取指定名称的位置
     *
     * @param name 位置名称
     * @return 位置向量，不存在返回null
     */
    public Vector getNamedLocation(String name) {
        return namedLocations.get(name);
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
        // TODO: 实现房间放置逻辑
        // 这里应该根据blockData在世界中放置方块
        logger.info("放置房间蓝图: " + name + " 在位置: " + location);
        return true;
    }

    @Override
    public Blueprint clone() {
        Builder builder = builder(name + "_copy", roomType, sizeX, sizeY, sizeZ)
            .setDescription(description)
            .setAuthor(author)
            .setCategory(category)
            .setConnections(minConnections, maxConnections)
            .setSpecialRoom(isSpecialRoom)
            .setRequiredKey(requiredKey)
            .setRarity(rarity);

        // 复制门信息
        for (DoorInfo door : doors) {
            builder.addDoor(door);
        }

        // 复制功能点
        for (Vector point : functionPoints) {
            builder.addFunctionPoint(point);
        }

        // 复制命名位置
        for (Map.Entry<String, Vector> entry : namedLocations.entrySet()) {
            builder.addNamedLocation(entry.getKey(), entry.getValue());
        }

        // 复制方块数据
        builder.setBlockData(blockData);

        return builder.build();
    }

    // ==================== 构建器类 ====================

    /**
     * 房间蓝图构建器
     */
    public static class Builder {
        private final String name;
        private final RoomType roomType;
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;

        private String description = "";
        private String author = "";
        private String category = "default";

        private final List<DoorInfo> doors = new ArrayList<>();
        private int minConnections = 1;
        private int maxConnections = 4;
        private boolean isSpecialRoom = false;
        private String requiredKey = null;
        private int rarity;

        private final List<Vector> functionPoints = new ArrayList<>();
        private final Map<String, Vector> namedLocations = new HashMap<>();

        private final Map<Vector, Material> blockData = new HashMap<>();
        private final Set<Vector> airBlocks = new HashSet<>();

        public Builder(String name, RoomType roomType, int sizeX, int sizeY, int sizeZ) {
            this.name = name;
            this.roomType = roomType;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.rarity = roomType.getDefaultRarity();
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

        public Builder setConnections(int min, int max) {
            this.minConnections = Math.max(1, min);
            this.maxConnections = Math.max(this.minConnections, max);
            return this;
        }

        public Builder setSpecialRoom(boolean isSpecial) {
            this.isSpecialRoom = isSpecial;
            return this;
        }

        public Builder setRequiredKey(String key) {
            this.requiredKey = key;
            return this;
        }

        public Builder setRarity(int rarity) {
            this.rarity = Math.max(1, rarity);
            return this;
        }

        public Builder addFunctionPoint(int x, int y, int z) {
            this.functionPoints.add(new Vector(x, y, z));
            return this;
        }

        public Builder addFunctionPoint(Vector point) {
            if (point != null) {
                this.functionPoints.add(point.clone());
            }
            return this;
        }

        public Builder addNamedLocation(String name, int x, int y, int z) {
            if (name != null && !name.trim().isEmpty()) {
                this.namedLocations.put(name, new Vector(x, y, z));
            }
            return this;
        }

        public Builder addNamedLocation(String name, Vector location) {
            if (name != null && !name.trim().isEmpty() && location != null) {
                this.namedLocations.put(name, location.clone());
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

        public RoomBlueprint build() {
            return new RoomBlueprint(this);
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
            // 这里应该实现实际的房间构建逻辑
            // 暂时返回true表示构建成功
            logger.info("构建房间蓝图: " + getName() + " 在位置: " + formatLocation(location));

            // TODO: 实现实际的方块放置逻辑
            // 1. 根据蓝图数据构建房间结构
            // 2. 放置门的位置
            // 3. 添加装饰和功能方块

            return true;

        } catch (Exception e) {
            logger.severe("构建房间蓝图时发生异常: " + getName() + " - " + e.getMessage());
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
