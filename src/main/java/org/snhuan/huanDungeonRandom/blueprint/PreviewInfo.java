package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * 预览信息类 - 存储蓝图的预览和展示信息
 *
 * 用于在编辑器中显示蓝图的基本信息，包含：
 * - 蓝图的基本统计信息
 * - 可视化预览数据
 * - 材料使用统计
 * - 复杂度评估
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class PreviewInfo {

    private final String blueprintName;
    private final BlueprintType blueprintType;
    private final Vector size;

    // 统计信息
    private final int totalBlocks;
    private final int airBlocks;
    private final int solidBlocks;
    private final int doorCount;
    private final int functionCount;

    // 材料统计
    private final Map<Material, Integer> materialCounts;
    private final Set<Material> uniqueMaterials;

    // 复杂度信息
    private final ComplexityLevel complexity;
    private final double complexityScore;

    // 预览图像数据（简化的2D表示）
    private final char[][] topViewMap;
    private final char[][] sideViewMap;

    // 建议和警告
    private final List<String> suggestions;
    private final List<String> warnings;

    /**
     * 构造函数
     *
     * @param builder 构建器实例
     */
    private PreviewInfo(Builder builder) {
        this.blueprintName = builder.blueprintName;
        this.blueprintType = builder.blueprintType;
        this.size = builder.size.clone();
        this.totalBlocks = builder.totalBlocks;
        this.airBlocks = builder.airBlocks;
        this.solidBlocks = builder.solidBlocks;
        this.doorCount = builder.doorCount;
        this.functionCount = builder.functionCount;
        this.materialCounts = new HashMap<>(builder.materialCounts);
        this.uniqueMaterials = new HashSet<>(builder.uniqueMaterials);
        this.complexity = builder.complexity;
        this.complexityScore = builder.complexityScore;
        this.topViewMap = cloneCharArray(builder.topViewMap);
        this.sideViewMap = cloneCharArray(builder.sideViewMap);
        this.suggestions = new ArrayList<>(builder.suggestions);
        this.warnings = new ArrayList<>(builder.warnings);
    }

    /**
     * 创建构建器
     *
     * @param blueprintName 蓝图名称
     * @param blueprintType 蓝图类型
     * @param size 蓝图尺寸
     * @return 构建器实例
     */
    public static Builder builder(String blueprintName, BlueprintType blueprintType, Vector size) {
        return new Builder(blueprintName, blueprintType, size);
    }

    /**
     * 克隆字符数组
     *
     * @param original 原始数组
     * @return 克隆的数组
     */
    private char[][] cloneCharArray(char[][] original) {
        if (original == null) {
            return null;
        }

        char[][] cloned = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                cloned[i] = original[i].clone();
            }
        }
        return cloned;
    }

    /**
     * 获取蓝图名称
     *
     * @return 蓝图名称
     */
    public String getBlueprintName() {
        return blueprintName;
    }

    /**
     * 获取蓝图类型
     *
     * @return 蓝图类型
     */
    public BlueprintType getBlueprintType() {
        return blueprintType;
    }

    /**
     * 获取蓝图尺寸
     *
     * @return 尺寸向量的副本
     */
    public Vector getSize() {
        return size.clone();
    }

    /**
     * 获取总方块数
     *
     * @return 总方块数
     */
    public int getTotalBlocks() {
        return totalBlocks;
    }

    /**
     * 获取空气方块数
     *
     * @return 空气方块数
     */
    public int getAirBlocks() {
        return airBlocks;
    }

    /**
     * 获取实体方块数
     *
     * @return 实体方块数
     */
    public int getSolidBlocks() {
        return solidBlocks;
    }

    /**
     * 获取门的数量
     *
     * @return 门的数量
     */
    public int getDoorCount() {
        return doorCount;
    }

    /**
     * 获取功能点数量
     *
     * @return 功能点数量
     */
    public int getFunctionCount() {
        return functionCount;
    }

    /**
     * 获取材料统计
     *
     * @return 材料统计的副本
     */
    public Map<Material, Integer> getMaterialCounts() {
        return new HashMap<>(materialCounts);
    }

    /**
     * 获取唯一材料集合
     *
     * @return 唯一材料集合的副本
     */
    public Set<Material> getUniqueMaterials() {
        return new HashSet<>(uniqueMaterials);
    }

    /**
     * 获取复杂度等级
     *
     * @return 复杂度等级
     */
    public ComplexityLevel getComplexity() {
        return complexity;
    }

    /**
     * 获取复杂度分数
     *
     * @return 复杂度分数
     */
    public double getComplexityScore() {
        return complexityScore;
    }

    /**
     * 获取俯视图
     *
     * @return 俯视图的副本
     */
    public char[][] getTopViewMap() {
        return cloneCharArray(topViewMap);
    }

    /**
     * 获取侧视图
     *
     * @return 侧视图的副本
     */
    public char[][] getSideViewMap() {
        return cloneCharArray(sideViewMap);
    }

    /**
     * 获取建议列表
     *
     * @return 建议列表的副本
     */
    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }

    /**
     * 获取警告列表
     *
     * @return 警告列表的副本
     */
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /**
     * 获取实体方块占比
     *
     * @return 实体方块占比（0.0-1.0）
     */
    public double getSolidBlockRatio() {
        return totalBlocks > 0 ? (double) solidBlocks / totalBlocks : 0.0;
    }

    /**
     * 获取材料多样性分数
     *
     * @return 材料多样性分数
     */
    public double getMaterialDiversityScore() {
        if (uniqueMaterials.isEmpty()) {
            return 0.0;
        }

        // 计算香农熵作为多样性指标
        double entropy = 0.0;
        for (int count : materialCounts.values()) {
            if (count > 0) {
                double probability = (double) count / solidBlocks;
                entropy -= probability * Math.log(probability) / Math.log(2);
            }
        }

        return entropy;
    }

    /**
     * 获取格式化的统计信息
     *
     * @return 格式化的统计信息
     */
    public String getFormattedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(blueprintName).append(" 预览信息 ===\n");
        sb.append("类型: ").append(blueprintType.getDisplayName()).append("\n");
        sb.append("尺寸: ").append((int)size.getX()).append("x")
          .append((int)size.getY()).append("x").append((int)size.getZ()).append("\n");
        sb.append("总方块数: ").append(totalBlocks).append("\n");
        sb.append("实体方块: ").append(solidBlocks).append(" (")
          .append(String.format("%.1f%%", getSolidBlockRatio() * 100)).append(")\n");
        sb.append("门数量: ").append(doorCount).append("\n");
        sb.append("功能点: ").append(functionCount).append("\n");
        sb.append("材料种类: ").append(uniqueMaterials.size()).append("\n");
        sb.append("复杂度: ").append(complexity.getDisplayName()).append(" (")
          .append(String.format("%.2f", complexityScore)).append(")\n");

        if (!warnings.isEmpty()) {
            sb.append("\n警告:\n");
            for (String warning : warnings) {
                sb.append("  - ").append(warning).append("\n");
            }
        }

        if (!suggestions.isEmpty()) {
            sb.append("\n建议:\n");
            for (String suggestion : suggestions) {
                sb.append("  - ").append(suggestion).append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 复杂度等级枚举
     */
    public enum ComplexityLevel {
        SIMPLE("简单", 0.0, 2.0),
        MODERATE("中等", 2.0, 4.0),
        COMPLEX("复杂", 4.0, 6.0),
        VERY_COMPLEX("非常复杂", 6.0, Double.MAX_VALUE);

        private final String displayName;
        private final double minScore;
        private final double maxScore;

        ComplexityLevel(String displayName, double minScore, double maxScore) {
            this.displayName = displayName;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static ComplexityLevel fromScore(double score) {
            for (ComplexityLevel level : values()) {
                if (score >= level.minScore && score < level.maxScore) {
                    return level;
                }
            }
            return VERY_COMPLEX;
        }
    }

    /**
     * 预览信息构建器
     */
    public static class Builder {
        private final String blueprintName;
        private final BlueprintType blueprintType;
        private final Vector size;

        private int totalBlocks = 0;
        private int airBlocks = 0;
        private int solidBlocks = 0;
        private int doorCount = 0;
        private int functionCount = 0;

        private final Map<Material, Integer> materialCounts = new HashMap<>();
        private final Set<Material> uniqueMaterials = new HashSet<>();

        private ComplexityLevel complexity = ComplexityLevel.SIMPLE;
        private double complexityScore = 0.0;

        private char[][] topViewMap;
        private char[][] sideViewMap;

        private final List<String> suggestions = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        public Builder(String blueprintName, BlueprintType blueprintType, Vector size) {
            this.blueprintName = blueprintName;
            this.blueprintType = blueprintType;
            this.size = size.clone();
        }

        public Builder setBlockCounts(int total, int air, int solid) {
            this.totalBlocks = total;
            this.airBlocks = air;
            this.solidBlocks = solid;
            return this;
        }

        public Builder setDoorCount(int doorCount) {
            this.doorCount = doorCount;
            return this;
        }

        public Builder setFunctionCount(int functionCount) {
            this.functionCount = functionCount;
            return this;
        }

        public Builder addMaterial(Material material, int count) {
            if (material != null && count > 0) {
                materialCounts.put(material, count);
                uniqueMaterials.add(material);
            }
            return this;
        }

        public Builder setComplexity(double score) {
            this.complexityScore = score;
            this.complexity = ComplexityLevel.fromScore(score);
            return this;
        }

        public Builder setTopViewMap(char[][] topViewMap) {
            this.topViewMap = topViewMap;
            return this;
        }

        public Builder setSideViewMap(char[][] sideViewMap) {
            this.sideViewMap = sideViewMap;
            return this;
        }

        public Builder addSuggestion(String suggestion) {
            if (suggestion != null && !suggestion.trim().isEmpty()) {
                suggestions.add(suggestion);
            }
            return this;
        }

        public Builder addWarning(String warning) {
            if (warning != null && !warning.trim().isEmpty()) {
                warnings.add(warning);
            }
            return this;
        }

        public Builder addInfo(String info) {
            if (info != null && !info.trim().isEmpty()) {
                suggestions.add(info);
            }
            return this;
        }

        public PreviewInfo build() {
            return new PreviewInfo(this);
        }
    }
}
