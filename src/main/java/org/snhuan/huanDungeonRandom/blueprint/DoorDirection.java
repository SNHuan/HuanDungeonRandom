package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.util.Vector;

/**
 * 门朝向枚举 - 定义门的四个基本朝向
 * 
 * 门的朝向决定了：
 * - 门的开启方向
 * - 连接的可能性
 * - 旋转后的新朝向
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public enum DoorDirection {
    
    /**
     * 北方（负Z方向）
     */
    NORTH("北", 0, new Vector(0, 0, -1)),
    
    /**
     * 东方（正X方向）
     */
    EAST("东", 90, new Vector(1, 0, 0)),
    
    /**
     * 南方（正Z方向）
     */
    SOUTH("南", 180, new Vector(0, 0, 1)),
    
    /**
     * 西方（负X方向）
     */
    WEST("西", 270, new Vector(-1, 0, 0));
    
    private final String displayName;
    private final int angle;
    private final Vector directionVector;
    
    /**
     * 构造函数
     * 
     * @param displayName 显示名称
     * @param angle 角度（度）
     * @param directionVector 方向向量
     */
    DoorDirection(String displayName, int angle, Vector directionVector) {
        this.displayName = displayName;
        this.angle = angle;
        this.directionVector = directionVector.clone();
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
     * 获取角度
     * 
     * @return 角度（度）
     */
    public int getAngle() {
        return angle;
    }
    
    /**
     * 获取方向向量
     * 
     * @return 方向向量的副本
     */
    public Vector getDirectionVector() {
        return directionVector.clone();
    }
    
    /**
     * 获取相对的方向
     * 
     * @return 相对方向
     */
    public DoorDirection getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case EAST: return WEST;
            case SOUTH: return NORTH;
            case WEST: return EAST;
            default: return this;
        }
    }
    
    /**
     * 检查是否与另一个方向相对
     * 
     * @param other 另一个方向
     * @return 是否相对
     */
    public boolean isOpposite(DoorDirection other) {
        return other != null && this.getOpposite() == other;
    }
    
    /**
     * 顺时针旋转90度
     * 
     * @return 旋转后的方向
     */
    public DoorDirection rotateClockwise() {
        switch (this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default: return this;
        }
    }
    
    /**
     * 逆时针旋转90度
     * 
     * @return 旋转后的方向
     */
    public DoorDirection rotateCounterClockwise() {
        switch (this) {
            case NORTH: return WEST;
            case EAST: return NORTH;
            case SOUTH: return EAST;
            case WEST: return SOUTH;
            default: return this;
        }
    }
    
    /**
     * 根据角度旋转
     * 
     * @param rotationAngle 旋转角度（度，必须是90的倍数）
     * @return 旋转后的方向
     */
    public DoorDirection rotate(int rotationAngle) {
        // 标准化角度到0-360范围
        int normalizedAngle = ((rotationAngle % 360) + 360) % 360;
        
        // 计算旋转次数（每次90度）
        int rotations = normalizedAngle / 90;
        
        DoorDirection result = this;
        for (int i = 0; i < rotations; i++) {
            result = result.rotateClockwise();
        }
        
        return result;
    }
    
    /**
     * 获取左侧方向
     * 
     * @return 左侧方向
     */
    public DoorDirection getLeft() {
        return rotateCounterClockwise();
    }
    
    /**
     * 获取右侧方向
     * 
     * @return 右侧方向
     */
    public DoorDirection getRight() {
        return rotateClockwise();
    }
    
    /**
     * 根据两个位置计算方向
     * 
     * @param from 起始位置
     * @param to 目标位置
     * @return 最接近的方向
     */
    public static DoorDirection fromVector(Vector from, Vector to) {
        if (from == null || to == null) {
            return NORTH;
        }
        
        Vector direction = to.clone().subtract(from).normalize();
        
        // 找到最接近的方向
        DoorDirection closest = NORTH;
        double maxDot = -1;
        
        for (DoorDirection dir : values()) {
            double dot = direction.dot(dir.getDirectionVector());
            if (dot > maxDot) {
                maxDot = dot;
                closest = dir;
            }
        }
        
        return closest;
    }
    
    /**
     * 根据角度获取方向
     * 
     * @param angle 角度（度）
     * @return 最接近的方向
     */
    public static DoorDirection fromAngle(double angle) {
        // 标准化角度到0-360范围
        double normalizedAngle = ((angle % 360) + 360) % 360;
        
        if (normalizedAngle >= 315 || normalizedAngle < 45) {
            return NORTH;
        } else if (normalizedAngle >= 45 && normalizedAngle < 135) {
            return EAST;
        } else if (normalizedAngle >= 135 && normalizedAngle < 225) {
            return SOUTH;
        } else {
            return WEST;
        }
    }
    
    /**
     * 根据字符串获取方向
     * 
     * @param directionString 方向字符串
     * @return 方向，找不到返回NORTH
     */
    public static DoorDirection fromString(String directionString) {
        if (directionString == null || directionString.trim().isEmpty()) {
            return NORTH;
        }
        
        String normalized = directionString.trim().toUpperCase();
        
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // 尝试通过显示名称匹配
            for (DoorDirection direction : values()) {
                if (direction.getDisplayName().equals(directionString)) {
                    return direction;
                }
            }
            
            // 尝试通过英文简写匹配
            switch (normalized) {
                case "N": return NORTH;
                case "E": return EAST;
                case "S": return SOUTH;
                case "W": return WEST;
                default: return NORTH;
            }
        }
    }
    
    /**
     * 获取所有方向的数组
     * 
     * @return 所有方向
     */
    public static DoorDirection[] getAllDirections() {
        return values();
    }
    
    /**
     * 获取水平方向的数组（排除上下）
     * 
     * @return 水平方向
     */
    public static DoorDirection[] getHorizontalDirections() {
        return new DoorDirection[]{NORTH, EAST, SOUTH, WEST};
    }
    
    /**
     * 检查是否是有效的旋转角度
     * 
     * @param angle 角度
     * @return 是否有效
     */
    public static boolean isValidRotationAngle(int angle) {
        return angle % 90 == 0;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
