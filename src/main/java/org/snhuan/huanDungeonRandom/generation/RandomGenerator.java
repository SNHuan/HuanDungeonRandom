package org.snhuan.huanDungeonRandom.generation;

import org.snhuan.huanDungeonRandom.blueprint.Blueprint;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintType;
import org.snhuan.huanDungeonRandom.blueprint.templates.RoomBlueprint;
import org.snhuan.huanDungeonRandom.blueprint.templates.TileBlueprint;
import org.snhuan.huanDungeonRandom.blueprint.templates.CorridorBlueprint;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 随机生成算法类 - 提供各种随机选择和生成算法
 * 
 * 核心功能：
 * - 权重随机选择
 * - 蓝图随机选择
 * - 地牢布局生成
 * - 连接路径计算
 * - 随机种子管理
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class RandomGenerator {
    
    private final Random random;
    private final long seed;
    
    /**
     * 构造函数
     * 
     * @param seed 随机种子
     */
    public RandomGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    /**
     * 构造函数（使用当前时间作为种子）
     */
    public RandomGenerator() {
        this(System.currentTimeMillis());
    }
    
    /**
     * 获取随机种子
     * 
     * @return 随机种子
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * 获取随机数生成器
     * 
     * @return 随机数生成器
     */
    public Random getRandom() {
        return random;
    }
    
    // ==================== 基础随机方法 ====================
    
    /**
     * 生成指定范围内的随机整数
     * 
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public int randomInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * 生成指定范围内的随机双精度数
     * 
     * @param min 最小值
     * @param max 最大值
     * @return 随机双精度数
     */
    public double randomDouble(double min, double max) {
        if (min >= max) {
            return min;
        }
        return random.nextDouble() * (max - min) + min;
    }
    
    /**
     * 根据概率返回布尔值
     * 
     * @param probability 概率（0.0-1.0）
     * @return 随机布尔值
     */
    public boolean randomBoolean(double probability) {
        return random.nextDouble() < probability;
    }
    
    /**
     * 从列表中随机选择一个元素
     * 
     * @param list 列表
     * @param <T> 元素类型
     * @return 随机选择的元素，列表为空返回null
     */
    public <T> T randomChoice(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }
    
    /**
     * 从数组中随机选择一个元素
     * 
     * @param array 数组
     * @param <T> 元素类型
     * @return 随机选择的元素，数组为空返回null
     */
    @SafeVarargs
    public final <T> T randomChoice(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[random.nextInt(array.length)];
    }
    
    /**
     * 从集合中随机选择一个元素
     * 
     * @param collection 集合
     * @param <T> 元素类型
     * @return 随机选择的元素，集合为空返回null
     */
    public <T> T randomChoice(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        
        if (collection instanceof List) {
            return randomChoice((List<T>) collection);
        }
        
        List<T> list = new ArrayList<>(collection);
        return randomChoice(list);
    }
    
    // ==================== 权重随机选择 ====================
    
    /**
     * 根据权重随机选择元素
     * 
     * @param items 元素列表
     * @param weights 对应的权重列表
     * @param <T> 元素类型
     * @return 随机选择的元素
     */
    public <T> T weightedChoice(List<T> items, List<Double> weights) {
        if (items == null || weights == null || items.isEmpty() || weights.isEmpty()) {
            return null;
        }
        
        if (items.size() != weights.size()) {
            throw new IllegalArgumentException("元素列表和权重列表大小不匹配");
        }
        
        double totalWeight = weights.stream().mapToDouble(Double::doubleValue).sum();
        if (totalWeight <= 0) {
            return randomChoice(items);
        }
        
        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;
        
        for (int i = 0; i < items.size(); i++) {
            currentWeight += weights.get(i);
            if (randomValue <= currentWeight) {
                return items.get(i);
            }
        }
        
        // 理论上不应该到达这里，但为了安全起见
        return items.get(items.size() - 1);
    }
    
    /**
     * 根据权重映射随机选择元素
     * 
     * @param weightMap 权重映射
     * @param <T> 元素类型
     * @return 随机选择的元素
     */
    public <T> T weightedChoice(Map<T, Double> weightMap) {
        if (weightMap == null || weightMap.isEmpty()) {
            return null;
        }
        
        List<T> items = new ArrayList<>(weightMap.keySet());
        List<Double> weights = items.stream()
            .map(weightMap::get)
            .collect(Collectors.toList());
        
        return weightedChoice(items, weights);
    }
    
    // ==================== 蓝图随机选择 ====================
    
    /**
     * 根据权重随机选择瓦片蓝图
     * 
     * @param tileBlueprints 瓦片蓝图列表
     * @return 随机选择的瓦片蓝图
     */
    public TileBlueprint randomTileBlueprint(List<TileBlueprint> tileBlueprints) {
        if (tileBlueprints == null || tileBlueprints.isEmpty()) {
            return null;
        }
        
        Map<TileBlueprint, Double> weightMap = new HashMap<>();
        for (TileBlueprint tile : tileBlueprints) {
            weightMap.put(tile, (double) tile.getWeight());
        }
        
        return weightedChoice(weightMap);
    }
    
    /**
     * 根据稀有度随机选择房间蓝图
     * 
     * @param roomBlueprints 房间蓝图列表
     * @return 随机选择的房间蓝图
     */
    public RoomBlueprint randomRoomBlueprint(List<RoomBlueprint> roomBlueprints) {
        if (roomBlueprints == null || roomBlueprints.isEmpty()) {
            return null;
        }
        
        Map<RoomBlueprint, Double> weightMap = new HashMap<>();
        for (RoomBlueprint room : roomBlueprints) {
            // 稀有度越高，权重越低
            double weight = 1.0 / room.getRarity();
            weightMap.put(room, weight);
        }
        
        return weightedChoice(weightMap);
    }
    
    /**
     * 根据优先级随机选择走廊蓝图
     * 
     * @param corridorBlueprints 走廊蓝图列表
     * @return 随机选择的走廊蓝图
     */
    public CorridorBlueprint randomCorridorBlueprint(List<CorridorBlueprint> corridorBlueprints) {
        if (corridorBlueprints == null || corridorBlueprints.isEmpty()) {
            return null;
        }
        
        Map<CorridorBlueprint, Double> weightMap = new HashMap<>();
        for (CorridorBlueprint corridor : corridorBlueprints) {
            weightMap.put(corridor, (double) corridor.getPriority());
        }
        
        return weightedChoice(weightMap);
    }
    
    /**
     * 根据类型和主题随机选择蓝图
     * 
     * @param blueprints 蓝图列表
     * @param type 蓝图类型
     * @param theme 地牢主题（可选）
     * @return 随机选择的蓝图
     */
    public Blueprint randomBlueprint(List<Blueprint> blueprints, BlueprintType type, DungeonTheme theme) {
        if (blueprints == null || blueprints.isEmpty()) {
            return null;
        }
        
        // 过滤指定类型的蓝图
        List<Blueprint> filteredBlueprints = blueprints.stream()
            .filter(blueprint -> blueprint.getType() == type)
            .collect(Collectors.toList());
        
        if (filteredBlueprints.isEmpty()) {
            return null;
        }
        
        // 如果有主题，优先选择匹配主题的蓝图
        if (theme != null) {
            List<Blueprint> themeMatchedBlueprints = filteredBlueprints.stream()
                .filter(blueprint -> isThemeCompatible(blueprint, theme))
                .collect(Collectors.toList());
            
            if (!themeMatchedBlueprints.isEmpty()) {
                filteredBlueprints = themeMatchedBlueprints;
            }
        }
        
        return randomChoice(filteredBlueprints);
    }
    
    /**
     * 检查蓝图是否与主题兼容
     * 
     * @param blueprint 蓝图
     * @param theme 主题
     * @return 是否兼容
     */
    private boolean isThemeCompatible(Blueprint blueprint, DungeonTheme theme) {
        // 这里可以根据蓝图的分类和主题进行匹配
        // 目前简单地检查分类名称是否包含主题相关关键词
        String category = blueprint.getCategory().toLowerCase();
        String themeId = theme.getId().toLowerCase();
        
        return category.contains(themeId) || category.equals("default") || category.equals("universal");
    }
    
    // ==================== 地牢生成参数 ====================
    
    /**
     * 根据主题生成地牢参数
     * 
     * @param theme 地牢主题
     * @return 生成参数
     */
    public GenerationParameters generateDungeonParameters(DungeonTheme theme) {
        DungeonTheme.GenerationConfig config = theme.getGenerationConfig();
        
        int roomCount = randomInt(config.getMinRooms(), config.getMaxRooms());
        int corridorCount = randomInt(config.getMinCorridors(), config.getMaxCorridors());
        
        // 根据房间密度调整布局
        boolean compactLayout = randomBoolean(config.getRoomDensity());
        
        // 根据装饰概率决定是否添加装饰
        boolean addDecorations = randomBoolean(config.getDecorationChance());
        
        return new GenerationParameters(
            roomCount,
            corridorCount,
            compactLayout,
            addDecorations,
            config.isAllowBranching(),
            config.getMaxDepth()
        );
    }
    
    /**
     * 生成参数类
     */
    public static class GenerationParameters {
        private final int roomCount;
        private final int corridorCount;
        private final boolean compactLayout;
        private final boolean addDecorations;
        private final boolean allowBranching;
        private final int maxDepth;
        
        public GenerationParameters(int roomCount, int corridorCount, boolean compactLayout,
                                  boolean addDecorations, boolean allowBranching, int maxDepth) {
            this.roomCount = roomCount;
            this.corridorCount = corridorCount;
            this.compactLayout = compactLayout;
            this.addDecorations = addDecorations;
            this.allowBranching = allowBranching;
            this.maxDepth = maxDepth;
        }
        
        // Getters
        public int getRoomCount() { return roomCount; }
        public int getCorridorCount() { return corridorCount; }
        public boolean isCompactLayout() { return compactLayout; }
        public boolean isAddDecorations() { return addDecorations; }
        public boolean isAllowBranching() { return allowBranching; }
        public int getMaxDepth() { return maxDepth; }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 打乱列表顺序
     * 
     * @param list 要打乱的列表
     * @param <T> 元素类型
     */
    public <T> void shuffle(List<T> list) {
        Collections.shuffle(list, random);
    }
    
    /**
     * 从列表中随机选择多个不重复的元素
     * 
     * @param list 源列表
     * @param count 选择数量
     * @param <T> 元素类型
     * @return 随机选择的元素列表
     */
    public <T> List<T> randomSample(List<T> list, int count) {
        if (list == null || list.isEmpty() || count <= 0) {
            return new ArrayList<>();
        }
        
        count = Math.min(count, list.size());
        List<T> shuffled = new ArrayList<>(list);
        shuffle(shuffled);
        
        return shuffled.subList(0, count);
    }
}
