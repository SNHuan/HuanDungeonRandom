package org.snhuan.huanDungeonRandom.dungeon;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * 地牢主题类 - 定义地牢的视觉风格和材料配置
 *
 * 地牢主题包含：
 * - 主要建筑材料配置
 * - 装饰材料配置
 * - 环境设置（光照、音效等）
 * - 生成参数配置
 * - 特殊效果配置
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DungeonTheme {

    private final String id;
    private final String name;
    private final String description;

    // 材料配置
    private final Map<MaterialType, Material> primaryMaterials;
    private final Map<MaterialType, List<Material>> alternativeMaterials;
    private final Map<String, Material> specialMaterials;

    // 环境配置
    private final int lightLevel;
    private final boolean allowNaturalSpawning;
    private final Set<String> ambientSounds;
    private final String weatherType;

    // 生成配置
    private final GenerationConfig generationConfig;

    // 稀有度和权重
    private final int rarity;
    private final double weight;

    /**
     * 材料类型枚举
     */
    public enum MaterialType {
        WALL("墙壁"),
        FLOOR("地板"),
        CEILING("天花板"),
        DOOR("门"),
        PILLAR("柱子"),
        DECORATION("装饰"),
        LIGHT_SOURCE("光源"),
        LIQUID("液体");

        private final String displayName;

        MaterialType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 生成配置类
     */
    public static class GenerationConfig {
        private final int minRooms;
        private final int maxRooms;
        private final int minCorridors;
        private final int maxCorridors;
        private final double roomDensity;
        private final double decorationChance;
        private final boolean allowBranching;
        private final int maxDepth;

        public GenerationConfig(int minRooms, int maxRooms, int minCorridors, int maxCorridors,
                              double roomDensity, double decorationChance, boolean allowBranching, int maxDepth) {
            this.minRooms = minRooms;
            this.maxRooms = maxRooms;
            this.minCorridors = minCorridors;
            this.maxCorridors = maxCorridors;
            this.roomDensity = roomDensity;
            this.decorationChance = decorationChance;
            this.allowBranching = allowBranching;
            this.maxDepth = maxDepth;
        }

        // Getters
        public int getMinRooms() { return minRooms; }
        public int getMaxRooms() { return maxRooms; }
        public int getMinCorridors() { return minCorridors; }
        public int getMaxCorridors() { return maxCorridors; }
        public double getRoomDensity() { return roomDensity; }
        public double getDecorationChance() { return decorationChance; }
        public boolean isAllowBranching() { return allowBranching; }
        public int getMaxDepth() { return maxDepth; }
    }

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private DungeonTheme(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;

        this.primaryMaterials = new HashMap<>(builder.primaryMaterials);
        this.alternativeMaterials = new HashMap<>(builder.alternativeMaterials);
        this.specialMaterials = new HashMap<>(builder.specialMaterials);

        this.lightLevel = builder.lightLevel;
        this.allowNaturalSpawning = builder.allowNaturalSpawning;
        this.ambientSounds = new HashSet<>(builder.ambientSounds);
        this.weatherType = builder.weatherType;

        this.generationConfig = builder.generationConfig;
        this.rarity = builder.rarity;
        this.weight = builder.weight;
    }

    /**
     * 创建构建器
     *
     * @param id 主题ID
     * @param name 主题名称
     * @return 构建器实例
     */
    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    /**
     * 从配置文件加载主题
     *
     * @param config 配置对象
     * @return 地牢主题实例
     */
    public static DungeonTheme fromConfig(YamlConfiguration config) {
        String id = config.getString("id", "unknown");
        String name = config.getString("name", "Unknown Theme");

        Builder builder = builder(id, name)
            .setDescription(config.getString("description", ""))
            .setLightLevel(config.getInt("environment.light-level", 7))
            .setAllowNaturalSpawning(config.getBoolean("environment.allow-natural-spawning", false))
            .setWeatherType(config.getString("environment.weather", "clear"))
            .setRarity(config.getInt("rarity", 1))
            .setWeight(config.getDouble("weight", 1.0));

        // 加载主要材料
        ConfigurationSection materialsSection = config.getConfigurationSection("materials.primary");
        if (materialsSection != null) {
            for (String key : materialsSection.getKeys(false)) {
                try {
                    MaterialType type = MaterialType.valueOf(key.toUpperCase());
                    Material material = Material.valueOf(materialsSection.getString(key));
                    builder.setPrimaryMaterial(type, material);
                } catch (IllegalArgumentException e) {
                    // 忽略无效的材料类型或材料
                }
            }
        }

        // 加载环境音效
        List<String> sounds = config.getStringList("environment.ambient-sounds");
        for (String sound : sounds) {
            builder.addAmbientSound(sound);
        }

        // 加载生成配置
        ConfigurationSection genSection = config.getConfigurationSection("generation");
        if (genSection != null) {
            GenerationConfig genConfig = new GenerationConfig(
                genSection.getInt("min-rooms", 3),
                genSection.getInt("max-rooms", 10),
                genSection.getInt("min-corridors", 2),
                genSection.getInt("max-corridors", 8),
                genSection.getDouble("room-density", 0.6),
                genSection.getDouble("decoration-chance", 0.3),
                genSection.getBoolean("allow-branching", true),
                genSection.getInt("max-depth", 5)
            );
            builder.setGenerationConfig(genConfig);
        }

        return builder.build();
    }

    /**
     * 保存主题到配置文件
     *
     * @param config 配置对象
     */
    public void saveToConfig(YamlConfiguration config) {
        config.set("id", id);
        config.set("name", name);
        config.set("description", description);
        config.set("rarity", rarity);
        config.set("weight", weight);

        // 保存环境配置
        config.set("environment.light-level", lightLevel);
        config.set("environment.allow-natural-spawning", allowNaturalSpawning);
        config.set("environment.weather", weatherType);
        config.set("environment.ambient-sounds", new ArrayList<>(ambientSounds));

        // 保存主要材料
        ConfigurationSection materialsSection = config.createSection("materials.primary");
        for (Map.Entry<MaterialType, Material> entry : primaryMaterials.entrySet()) {
            materialsSection.set(entry.getKey().name().toLowerCase(), entry.getValue().name());
        }

        // 保存生成配置
        if (generationConfig != null) {
            ConfigurationSection genSection = config.createSection("generation");
            genSection.set("min-rooms", generationConfig.getMinRooms());
            genSection.set("max-rooms", generationConfig.getMaxRooms());
            genSection.set("min-corridors", generationConfig.getMinCorridors());
            genSection.set("max-corridors", generationConfig.getMaxCorridors());
            genSection.set("room-density", generationConfig.getRoomDensity());
            genSection.set("decoration-chance", generationConfig.getDecorationChance());
            genSection.set("allow-branching", generationConfig.isAllowBranching());
            genSection.set("max-depth", generationConfig.getMaxDepth());
        }
    }

    /**
     * 获取指定类型的主要材料
     *
     * @param type 材料类型
     * @return 材料，不存在返回null
     */
    public Material getPrimaryMaterial(MaterialType type) {
        return primaryMaterials.get(type);
    }

    /**
     * 获取指定类型的替代材料列表
     *
     * @param type 材料类型
     * @return 替代材料列表
     */
    public List<Material> getAlternativeMaterials(MaterialType type) {
        return alternativeMaterials.getOrDefault(type, new ArrayList<>());
    }

    /**
     * 获取随机材料（主要材料或替代材料之一）
     *
     * @param type 材料类型
     * @param random 随机数生成器
     * @return 随机选择的材料
     */
    public Material getRandomMaterial(MaterialType type, Random random) {
        Material primary = getPrimaryMaterial(type);
        List<Material> alternatives = getAlternativeMaterials(type);

        if (primary == null && alternatives.isEmpty()) {
            return getDefaultMaterial(type);
        }

        if (alternatives.isEmpty()) {
            return primary;
        }

        // 70% 概率使用主要材料，30% 概率使用替代材料
        if (primary != null && random.nextDouble() < 0.7) {
            return primary;
        }

        return alternatives.get(random.nextInt(alternatives.size()));
    }

    /**
     * 获取默认材料
     *
     * @param type 材料类型
     * @return 默认材料
     */
    private Material getDefaultMaterial(MaterialType type) {
        switch (type) {
            case WALL: return Material.STONE_BRICKS;
            case FLOOR: return Material.STONE;
            case CEILING: return Material.STONE;
            case DOOR: return Material.OAK_DOOR;
            case PILLAR: return Material.STONE_BRICK_STAIRS;
            case DECORATION: return Material.COBWEB;
            case LIGHT_SOURCE: return Material.TORCH;
            case LIQUID: return Material.WATER;
            default: return Material.STONE;
        }
    }

    // ==================== Getter 方法 ====================

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getLightLevel() { return lightLevel; }
    public boolean isAllowNaturalSpawning() { return allowNaturalSpawning; }
    public Set<String> getAmbientSounds() { return new HashSet<>(ambientSounds); }
    public String getWeatherType() { return weatherType; }
    public GenerationConfig getGenerationConfig() { return generationConfig; }
    public int getRarity() { return rarity; }
    public double getWeight() { return weight; }

    public Map<MaterialType, Material> getPrimaryMaterials() {
        return new HashMap<>(primaryMaterials);
    }

    public Material getSpecialMaterial(String key) {
        return specialMaterials.get(key);
    }

    @Override
    public String toString() {
        return String.format("DungeonTheme{id='%s', name='%s', rarity=%d}", id, name, rarity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DungeonTheme that = (DungeonTheme) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ==================== 构建器类 ====================

    /**
     * 地牢主题构建器
     */
    public static class Builder {
        private final String id;
        private final String name;
        private String description = "";

        private final Map<MaterialType, Material> primaryMaterials = new HashMap<>();
        private final Map<MaterialType, List<Material>> alternativeMaterials = new HashMap<>();
        private final Map<String, Material> specialMaterials = new HashMap<>();

        private int lightLevel = 7;
        private boolean allowNaturalSpawning = false;
        private final Set<String> ambientSounds = new HashSet<>();
        private String weatherType = "clear";

        private GenerationConfig generationConfig;
        private int rarity = 1;
        private double weight = 1.0;

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;

            // 设置默认生成配置
            this.generationConfig = new GenerationConfig(3, 10, 2, 8, 0.6, 0.3, true, 5);
        }

        public Builder setDescription(String description) {
            this.description = description != null ? description : "";
            return this;
        }

        public Builder setPrimaryMaterial(MaterialType type, Material material) {
            if (type != null && material != null) {
                this.primaryMaterials.put(type, material);
            }
            return this;
        }

        public Builder addAlternativeMaterial(MaterialType type, Material material) {
            if (type != null && material != null) {
                this.alternativeMaterials.computeIfAbsent(type, k -> new ArrayList<>()).add(material);
            }
            return this;
        }

        public Builder setSpecialMaterial(String key, Material material) {
            if (key != null && !key.trim().isEmpty() && material != null) {
                this.specialMaterials.put(key, material);
            }
            return this;
        }

        public Builder setLightLevel(int lightLevel) {
            this.lightLevel = Math.max(0, Math.min(15, lightLevel));
            return this;
        }

        public Builder setAllowNaturalSpawning(boolean allowNaturalSpawning) {
            this.allowNaturalSpawning = allowNaturalSpawning;
            return this;
        }

        public Builder addAmbientSound(String sound) {
            if (sound != null && !sound.trim().isEmpty()) {
                this.ambientSounds.add(sound);
            }
            return this;
        }

        public Builder setWeatherType(String weatherType) {
            this.weatherType = weatherType != null ? weatherType : "clear";
            return this;
        }

        public Builder setGenerationConfig(GenerationConfig config) {
            this.generationConfig = config;
            return this;
        }

        public Builder setRarity(int rarity) {
            this.rarity = Math.max(1, rarity);
            return this;
        }

        public Builder setWeight(double weight) {
            this.weight = Math.max(0.1, weight);
            return this;
        }

        /**
         * 设置石质主题的默认材料
         *
         * @return 构建器
         */
        public Builder setStoneTheme() {
            setPrimaryMaterial(MaterialType.WALL, Material.STONE_BRICKS);
            setPrimaryMaterial(MaterialType.FLOOR, Material.STONE);
            setPrimaryMaterial(MaterialType.CEILING, Material.STONE);
            setPrimaryMaterial(MaterialType.DOOR, Material.IRON_DOOR);
            setPrimaryMaterial(MaterialType.PILLAR, Material.STONE_BRICK_STAIRS);
            setPrimaryMaterial(MaterialType.DECORATION, Material.COBWEB);
            setPrimaryMaterial(MaterialType.LIGHT_SOURCE, Material.TORCH);

            addAlternativeMaterial(MaterialType.WALL, Material.COBBLESTONE);
            addAlternativeMaterial(MaterialType.WALL, Material.MOSSY_STONE_BRICKS);

            return this;
        }

        /**
         * 设置地狱主题的默认材料
         *
         * @return 构建器
         */
        public Builder setNetherTheme() {
            setPrimaryMaterial(MaterialType.WALL, Material.NETHER_BRICKS);
            setPrimaryMaterial(MaterialType.FLOOR, Material.NETHERRACK);
            setPrimaryMaterial(MaterialType.CEILING, Material.NETHER_BRICKS);
            setPrimaryMaterial(MaterialType.DOOR, Material.IRON_DOOR);
            setPrimaryMaterial(MaterialType.PILLAR, Material.NETHER_BRICK_STAIRS);
            setPrimaryMaterial(MaterialType.DECORATION, Material.SOUL_SAND);
            setPrimaryMaterial(MaterialType.LIGHT_SOURCE, Material.GLOWSTONE);
            setPrimaryMaterial(MaterialType.LIQUID, Material.LAVA);

            setLightLevel(5);
            addAmbientSound("ambient.nether_wastes.loop");

            return this;
        }

        /**
         * 设置冰雪主题的默认材料
         *
         * @return 构建器
         */
        public Builder setIceTheme() {
            setPrimaryMaterial(MaterialType.WALL, Material.PACKED_ICE);
            setPrimaryMaterial(MaterialType.FLOOR, Material.ICE);
            setPrimaryMaterial(MaterialType.CEILING, Material.PACKED_ICE);
            setPrimaryMaterial(MaterialType.DOOR, Material.IRON_DOOR);
            setPrimaryMaterial(MaterialType.PILLAR, Material.BLUE_ICE);
            setPrimaryMaterial(MaterialType.DECORATION, Material.SNOW);
            setPrimaryMaterial(MaterialType.LIGHT_SOURCE, Material.SEA_LANTERN);

            addAlternativeMaterial(MaterialType.WALL, Material.BLUE_ICE);
            addAlternativeMaterial(MaterialType.DECORATION, Material.POWDER_SNOW);

            setWeatherType("snow");

            return this;
        }

        public DungeonTheme build() {
            return new DungeonTheme(this);
        }
    }
}
