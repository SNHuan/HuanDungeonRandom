package org.snhuan.huanDungeonRandom.blueprint;

/**
 * 蓝图类型枚举 - 定义所有支持的蓝图类型
 *
 * 不同类型的蓝图有不同的用途和特性：
 * - TILE: 瓦片蓝图，用于随机生成的基本单元
 * - ROOM: 房间蓝图，特殊的房间结构
 * - CORRIDOR: 走廊蓝图，连接房间的通道
 * - STRUCTURE: 结构蓝图，装饰性建筑
 * - DUNGEON: 完整地牢蓝图
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public enum BlueprintType {

    /**
     * 瓦片蓝图 - 用于地牢随机生成的基本单元
     * 特点：
     * - 必须定义门的位置
     * - 支持旋转
     * - 可以与其他瓦片连接
     */
    TILE("瓦片", "用于随机生成的基本建筑单元", true, true),

    /**
     * 房间蓝图 - 特殊的房间结构
     * 特点：
     * - 通常比瓦片更大
     * - 可能包含特殊功能
     * - 支持门连接
     */
    ROOM("房间", "特殊的房间结构", true, true),

    /**
     * 走廊蓝图 - 连接房间的通道
     * 特点：
     * - 用于连接两个房间或瓦片
     * - 通常是线性结构
     * - 两端必须有门
     */
    CORRIDOR("走廊", "连接房间的通道", true, true),

    /**
     * 结构蓝图 - 装饰性建筑
     * 特点：
     * - 不需要门连接
     * - 用于装饰和丰富地牢
     * - 可以随机放置
     */
    STRUCTURE("结构", "装饰性建筑结构", false, true),

    /**
     * 完整地牢蓝图 - 预设计的完整地牢
     * 特点：
     * - 包含完整的地牢布局
     * - 不支持旋转
     * - 直接使用，不参与随机生成
     */
    DUNGEON("地牢", "完整的地牢布局", false, false);

    private final String displayName;
    private final String description;
    private final boolean requiresDoors;
    private final boolean supportsRotation;

    /**
     * 构造函数
     *
     * @param displayName 显示名称
     * @param description 描述
     * @param requiresDoors 是否需要门
     * @param supportsRotation 是否支持旋转
     */
    BlueprintType(String displayName, String description, boolean requiresDoors, boolean supportsRotation) {
        this.displayName = displayName;
        this.description = description;
        this.requiresDoors = requiresDoors;
        this.supportsRotation = supportsRotation;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 是否需要门
     *
     * @return 是否需要门
     */
    public boolean requiresDoors() {
        return requiresDoors;
    }

    /**
     * 是否支持旋转
     *
     * @return 是否支持旋转
     */
    public boolean supportsRotation() {
        return supportsRotation;
    }

    /**
     * 是否可以用于随机生成
     *
     * @return 是否可以用于随机生成
     */
    public boolean canBeUsedInGeneration() {
        return this == TILE || this == ROOM || this == CORRIDOR;
    }

    /**
     * 是否是连接类型（需要门连接）
     *
     * @return 是否是连接类型
     */
    public boolean isConnectable() {
        return requiresDoors;
    }

    /**
     * 获取推荐的最小尺寸
     *
     * @return 推荐的最小尺寸 [x, y, z]
     */
    public int[] getRecommendedMinSize() {
        switch (this) {
            case TILE:
                return new int[]{8, 4, 8};
            case ROOM:
                return new int[]{12, 6, 12};
            case CORRIDOR:
                return new int[]{4, 4, 8};
            case STRUCTURE:
                return new int[]{4, 4, 4};
            case DUNGEON:
                return new int[]{32, 16, 32};
            default:
                return new int[]{8, 4, 8};
        }
    }

    /**
     * 获取推荐的最大尺寸
     *
     * @return 推荐的最大尺寸 [x, y, z]
     */
    public int[] getRecommendedMaxSize() {
        switch (this) {
            case TILE:
                return new int[]{16, 8, 16};
            case ROOM:
                return new int[]{32, 12, 32};
            case CORRIDOR:
                return new int[]{8, 6, 32};
            case STRUCTURE:
                return new int[]{16, 16, 16};
            case DUNGEON:
                return new int[]{128, 32, 128};
            default:
                return new int[]{16, 8, 16};
        }
    }

    /**
     * 验证尺寸是否合理
     *
     * @param sizeX X轴尺寸
     * @param sizeY Y轴尺寸
     * @param sizeZ Z轴尺寸
     * @return 验证结果
     */
    public ValidationResult validateSize(int sizeX, int sizeY, int sizeZ) {
        int[] minSize = getRecommendedMinSize();
        int[] maxSize = getRecommendedMaxSize();

        if (sizeX < minSize[0] || sizeY < minSize[1] || sizeZ < minSize[2]) {
            return new ValidationResult(false,
                String.format("尺寸过小，最小推荐尺寸为 %dx%dx%d", minSize[0], minSize[1], minSize[2]));
        }

        if (sizeX > maxSize[0] || sizeY > maxSize[1] || sizeZ > maxSize[2]) {
            return new ValidationResult(false,
                String.format("尺寸过大，最大推荐尺寸为 %dx%dx%d", maxSize[0], maxSize[1], maxSize[2]));
        }

        return new ValidationResult(true, "尺寸合理");
    }

    /**
     * 根据字符串获取蓝图类型
     *
     * @param typeString 类型字符串
     * @return 蓝图类型，找不到返回null
     */
    public static BlueprintType fromString(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return null;
        }

        try {
            return valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 尝试通过显示名称匹配
            for (BlueprintType type : values()) {
                if (type.getDisplayName().equals(typeString)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 获取所有可用于生成的类型
     *
     * @return 可用于生成的类型数组
     */
    public static BlueprintType[] getGenerationTypes() {
        return new BlueprintType[]{TILE, ROOM, CORRIDOR};
    }

    /**
     * 获取所有装饰类型
     *
     * @return 装饰类型数组
     */
    public static BlueprintType[] getDecorationTypes() {
        return new BlueprintType[]{STRUCTURE};
    }

    @Override
    public String toString() {
        return displayName;
    }
}
