package org.snhuan.huanDungeonRandom.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 位置工具类 - 提供位置相关的计算和操作方法
 * 
 * 功能包括：
 * - 位置的序列化和反序列化
 * - 距离计算
 * - 区域检测
 * - 坐标转换
 * - 安全位置查找
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class LocationUtils {
    
    private static final Logger logger = Logger.getLogger(LocationUtils.class.getName());
    
    /**
     * 私有构造函数，防止实例化工具类
     */
    private LocationUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    
    /**
     * 将位置序列化为字符串
     * 格式: "world,x,y,z,yaw,pitch"
     * 
     * @param location 位置对象
     * @return 序列化字符串，失败返回null
     */
    public static String serializeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
    }
    
    /**
     * 从字符串反序列化位置
     * 
     * @param serialized 序列化字符串
     * @return 位置对象，失败返回null
     */
    public static Location deserializeLocation(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = serialized.split(",");
            if (parts.length != 6) {
                logger.warning("位置字符串格式错误: " + serialized);
                return null;
            }
            
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) {
                logger.warning("世界不存在: " + parts[0]);
                return null;
            }
            
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
            
        } catch (NumberFormatException e) {
            logger.warning("位置数值解析失败: " + serialized);
            return null;
        }
    }
    
    /**
     * 计算两个位置之间的距离
     * 
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 距离，如果位置无效返回-1
     */
    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return -1;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return -1;
        }
        
        return loc1.distance(loc2);
    }
    
    /**
     * 计算两个位置之间的平面距离（忽略Y轴）
     * 
     * @param loc1 位置1
     * @param loc2 位置2
     * @return 平面距离，如果位置无效返回-1
     */
    public static double getDistance2D(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            return -1;
        }
        
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return -1;
        }
        
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * 检查位置是否在指定区域内
     * 
     * @param location 要检查的位置
     * @param corner1 区域角点1
     * @param corner2 区域角点2
     * @return 是否在区域内
     */
    public static boolean isLocationInRegion(Location location, Location corner1, Location corner2) {
        if (location == null || corner1 == null || corner2 == null) {
            return false;
        }
        
        if (!location.getWorld().equals(corner1.getWorld()) || 
            !location.getWorld().equals(corner2.getWorld())) {
            return false;
        }
        
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    /**
     * 获取位置周围的所有方块
     * 
     * @param center 中心位置
     * @param radius 半径
     * @return 方块列表
     */
    public static List<Block> getBlocksInRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        
        if (center == null || center.getWorld() == null || radius < 0) {
            return blocks;
        }
        
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    Location blockLoc = new Location(world, x, y, z);
                    if (getDistance(center, blockLoc) <= radius) {
                        blocks.add(world.getBlockAt(x, y, z));
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * 查找安全的传送位置
     * 
     * @param location 原始位置
     * @return 安全位置，找不到返回原位置
     */
    public static Location findSafeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return location;
        }
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int z = location.getBlockZ();
        
        // 从当前Y坐标开始向上查找
        for (int y = location.getBlockY(); y < world.getMaxHeight() - 2; y++) {
            Location checkLoc = new Location(world, x + 0.5, y, z + 0.5);
            if (isSafeLocation(checkLoc)) {
                return checkLoc;
            }
        }
        
        // 向下查找
        for (int y = location.getBlockY() - 1; y > world.getMinHeight(); y--) {
            Location checkLoc = new Location(world, x + 0.5, y, z + 0.5);
            if (isSafeLocation(checkLoc)) {
                return checkLoc;
            }
        }
        
        // 找不到安全位置，返回原位置
        return location;
    }
    
    /**
     * 检查位置是否安全（可以站立）
     * 
     * @param location 位置
     * @return 是否安全
     */
    public static boolean isSafeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // 检查脚下是否有实体方块
        Block ground = world.getBlockAt(x, y - 1, z);
        if (ground.getType().isAir()) {
            return false;
        }
        
        // 检查身体和头部位置是否为空气
        Block body = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        
        return body.getType().isAir() && head.getType().isAir();
    }
    
    /**
     * 获取位置的中心坐标（方块中心）
     * 
     * @param location 原始位置
     * @return 中心位置
     */
    public static Location getCenterLocation(Location location) {
        if (location == null) {
            return null;
        }
        
        return new Location(
            location.getWorld(),
            location.getBlockX() + 0.5,
            location.getBlockY(),
            location.getBlockZ() + 0.5,
            location.getYaw(),
            location.getPitch()
        );
    }
    
    /**
     * 创建边界框
     * 
     * @param corner1 角点1
     * @param corner2 角点2
     * @return 边界框
     */
    public static BoundingBox createBoundingBox(Location corner1, Location corner2) {
        if (corner1 == null || corner2 == null) {
            return null;
        }
        
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * 检查两个边界框是否重叠
     * 
     * @param box1 边界框1
     * @param box2 边界框2
     * @return 是否重叠
     */
    public static boolean isOverlapping(BoundingBox box1, BoundingBox box2) {
        if (box1 == null || box2 == null) {
            return false;
        }
        
        return box1.overlaps(box2);
    }
    
    /**
     * 计算向量的方向角度
     * 
     * @param from 起始位置
     * @param to 目标位置
     * @return 方向角度（度）
     */
    public static float getYawBetweenLocations(Location from, Location to) {
        if (from == null || to == null) {
            return 0.0f;
        }
        
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        return (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
    }
    
    /**
     * 获取位置的字符串表示（用于显示）
     * 
     * @param location 位置
     * @return 格式化的位置字符串
     */
    public static String getLocationString(Location location) {
        if (location == null) {
            return "null";
        }
        
        if (location.getWorld() == null) {
            return "无效世界";
        }
        
        return String.format("%s (%.1f, %.1f, %.1f)",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ()
        );
    }
}
