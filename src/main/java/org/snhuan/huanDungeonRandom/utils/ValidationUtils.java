package org.snhuan.huanDungeonRandom.utils;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.snhuan.huanDungeonRandom.blueprint.*;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 验证工具类 - 提供各种验证功能
 * 
 * 包含以下验证功能：
 * - 字符串验证（ID、名称等）
 * - 数值范围验证
 * - 坐标和尺寸验证
 * - 蓝图特定验证
 * - 门连接验证
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class ValidationUtils {
    
    // 正则表达式模式
    private static final Pattern VALID_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[\\w\\s\\u4e00-\\u9fa5-]+$");
    
    // 常量限制
    private static final int MAX_ID_LENGTH = 64;
    private static final int MAX_NAME_LENGTH = 128;
    private static final int MAX_DESCRIPTION_LENGTH = 512;
    private static final int MAX_BLUEPRINT_SIZE = 256;
    private static final int MIN_BLUEPRINT_SIZE = 1;
    
    /**
     * 私有构造函数，防止实例化
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    
    // ==================== 字符串验证 ====================
    
    /**
     * 验证ID格式
     * 
     * @param id 要验证的ID
     * @return 是否有效
     */
    public static boolean isValidId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        String trimmedId = id.trim();
        return trimmedId.length() <= MAX_ID_LENGTH && VALID_ID_PATTERN.matcher(trimmedId).matches();
    }
    
    /**
     * 验证名称格式
     * 
     * @param name 要验证的名称
     * @return 是否有效
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmedName = name.trim();
        return trimmedName.length() <= MAX_NAME_LENGTH && VALID_NAME_PATTERN.matcher(trimmedName).matches();
    }
    
    /**
     * 验证描述长度
     * 
     * @param description 要验证的描述
     * @return 是否有效
     */
    public static boolean isValidDescription(String description) {
        if (description == null) {
            return true; // 描述可以为空
        }
        
        return description.length() <= MAX_DESCRIPTION_LENGTH;
    }
    
    /**
     * 验证字符串是否非空
     * 
     * @param str 要验证的字符串
     * @return 是否非空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    // ==================== 数值验证 ====================
    
    /**
     * 验证数值是否在指定范围内
     * 
     * @param value 要验证的值
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 是否在范围内
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 验证数值是否在指定范围内
     * 
     * @param value 要验证的值
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 是否在范围内
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * 验证数值是否为正数
     * 
     * @param value 要验证的值
     * @return 是否为正数
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
    
    /**
     * 验证数值是否为非负数
     * 
     * @param value 要验证的值
     * @return 是否为非负数
     */
    public static boolean isNonNegative(int value) {
        return value >= 0;
    }
    
    // ==================== 坐标和尺寸验证 ====================
    
    /**
     * 验证蓝图尺寸
     * 
     * @param sizeX X轴尺寸
     * @param sizeY Y轴尺寸
     * @param sizeZ Z轴尺寸
     * @return 验证结果
     */
    public static ValidationResult validateBlueprintSize(int sizeX, int sizeY, int sizeZ) {
        ValidationResult.Builder builder = ValidationResult.builder()
            .setMessage("蓝图尺寸验证");
        
        // 检查最小尺寸
        if (sizeX < MIN_BLUEPRINT_SIZE) {
            builder.addError("X轴尺寸不能小于 " + MIN_BLUEPRINT_SIZE);
        }
        if (sizeY < MIN_BLUEPRINT_SIZE) {
            builder.addError("Y轴尺寸不能小于 " + MIN_BLUEPRINT_SIZE);
        }
        if (sizeZ < MIN_BLUEPRINT_SIZE) {
            builder.addError("Z轴尺寸不能小于 " + MIN_BLUEPRINT_SIZE);
        }
        
        // 检查最大尺寸
        if (sizeX > MAX_BLUEPRINT_SIZE) {
            builder.addError("X轴尺寸不能大于 " + MAX_BLUEPRINT_SIZE);
        }
        if (sizeY > MAX_BLUEPRINT_SIZE) {
            builder.addError("Y轴尺寸不能大于 " + MAX_BLUEPRINT_SIZE);
        }
        if (sizeZ > MAX_BLUEPRINT_SIZE) {
            builder.addError("Z轴尺寸不能大于 " + MAX_BLUEPRINT_SIZE);
        }
        
        // 性能警告
        int totalBlocks = sizeX * sizeY * sizeZ;
        if (totalBlocks > 100000) {
            builder.addWarning("蓝图体积过大，可能影响性能");
        }
        
        return builder.build();
    }
    
    /**
     * 验证坐标是否在蓝图范围内
     * 
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param sizeX 蓝图X轴尺寸
     * @param sizeY 蓝图Y轴尺寸
     * @param sizeZ 蓝图Z轴尺寸
     * @return 是否在范围内
     */
    public static boolean isCoordinateInBounds(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        return x >= 0 && x < sizeX && 
               y >= 0 && y < sizeY && 
               z >= 0 && z < sizeZ;
    }
    
    /**
     * 验证向量坐标是否在蓝图范围内
     * 
     * @param position 位置向量
     * @param sizeX 蓝图X轴尺寸
     * @param sizeY 蓝图Y轴尺寸
     * @param sizeZ 蓝图Z轴尺寸
     * @return 是否在范围内
     */
    public static boolean isPositionInBounds(Vector position, int sizeX, int sizeY, int sizeZ) {
        if (position == null) {
            return false;
        }
        
        return isCoordinateInBounds(
            position.getBlockX(), 
            position.getBlockY(), 
            position.getBlockZ(), 
            sizeX, sizeY, sizeZ
        );
    }
    
    // ==================== 蓝图特定验证 ====================
    
    /**
     * 验证门的位置
     * 
     * @param doors 门列表
     * @param sizeX 蓝图X轴尺寸
     * @param sizeY 蓝图Y轴尺寸
     * @param sizeZ 蓝图Z轴尺寸
     * @return 验证结果
     */
    public static ValidationResult validateDoorPositions(List<DoorInfo> doors, int sizeX, int sizeY, int sizeZ) {
        ValidationResult.Builder builder = ValidationResult.builder()
            .setMessage("门位置验证");
        
        if (doors == null || doors.isEmpty()) {
            builder.addWarning("没有定义门");
            return builder.build();
        }
        
        for (int i = 0; i < doors.size(); i++) {
            DoorInfo door = doors.get(i);
            if (door == null) {
                builder.addError("门 " + i + " 为空");
                continue;
            }
            
            // 检查门是否在边界上
            if (!isDoorOnBoundary(door, sizeX, sizeY, sizeZ)) {
                builder.addError("门 " + door.getId() + " 不在蓝图边界上");
            }
            
            // 检查门的坐标是否有效
            if (!isCoordinateInBounds(door.getX(), door.getY(), door.getZ(), sizeX, sizeY, sizeZ)) {
                builder.addError("门 " + door.getId() + " 的坐标超出蓝图范围");
            }
        }
        
        // 检查门ID重复
        Set<String> doorIds = new java.util.HashSet<>();
        for (DoorInfo door : doors) {
            if (door != null && door.getId() != null) {
                if (!doorIds.add(door.getId())) {
                    builder.addError("门ID重复: " + door.getId());
                }
            }
        }
        
        return builder.build();
    }
    
    /**
     * 检查门是否在蓝图边界上
     * 
     * @param door 门信息
     * @param sizeX 蓝图X轴尺寸
     * @param sizeY 蓝图Y轴尺寸
     * @param sizeZ 蓝图Z轴尺寸
     * @return 是否在边界上
     */
    private static boolean isDoorOnBoundary(DoorInfo door, int sizeX, int sizeY, int sizeZ) {
        int x = door.getX();
        int y = door.getY();
        int z = door.getZ();
        
        // 门必须在某个面的边界上
        boolean onXBoundary = (x == 0 || x == sizeX - 1);
        boolean onZBoundary = (z == 0 || z == sizeZ - 1);
        boolean onYBoundary = (y == 0 || y == sizeY - 1);
        
        // 门通常在侧面，不在顶部或底部
        return (onXBoundary || onZBoundary) && !onYBoundary;
    }
    
    /**
     * 验证材料是否有效
     * 
     * @param material 材料
     * @return 是否有效
     */
    public static boolean isValidMaterial(Material material) {
        return material != null && material != Material.AIR && material.isBlock();
    }
    
    /**
     * 验证蓝图类型是否支持门
     * 
     * @param type 蓝图类型
     * @param doorCount 门的数量
     * @return 验证结果
     */
    public static ValidationResult validateBlueprintTypeDoors(BlueprintType type, int doorCount) {
        ValidationResult.Builder builder = ValidationResult.builder()
            .setMessage("蓝图类型门验证");
        
        if (type == null) {
            builder.addError("蓝图类型不能为空");
            return builder.build();
        }
        
        if (type.requiresDoors() && doorCount == 0) {
            builder.addError(type.getDisplayName() + " 类型的蓝图必须有门");
        }
        
        if (!type.requiresDoors() && doorCount > 0) {
            builder.addWarning(type.getDisplayName() + " 类型的蓝图通常不需要门");
        }
        
        return builder.build();
    }
    
    // ==================== 集合验证 ====================
    
    /**
     * 验证列表是否非空
     * 
     * @param list 要验证的列表
     * @return 是否非空
     */
    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
    
    /**
     * 验证集合是否非空
     * 
     * @param set 要验证的集合
     * @return 是否非空
     */
    public static boolean isNotEmpty(Set<?> set) {
        return set != null && !set.isEmpty();
    }
    
    /**
     * 验证列表大小是否在指定范围内
     * 
     * @param list 要验证的列表
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return 是否在范围内
     */
    public static boolean isListSizeInRange(List<?> list, int minSize, int maxSize) {
        if (list == null) {
            return minSize <= 0;
        }
        
        int size = list.size();
        return size >= minSize && size <= maxSize;
    }
}
