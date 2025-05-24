package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * 门信息类 - 存储蓝图中门的位置和属性信息
 *
 * 门是蓝图之间连接的关键点，包含：
 * - 位置坐标（相对于蓝图原点）
 * - 朝向方向
 * - 唯一标识符
 * - 连接状态
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class DoorInfo {

    private final int x;
    private final int y;
    private final int z;
    private final DoorDirection direction;
    private final String id;

    // 连接状态
    private boolean connected;
    private String connectedBlueprintId;
    private String connectedDoorId;

    // 门的属性
    private boolean isEntrance;
    private boolean isExit;
    private boolean isLocked;
    private String keyRequired;

    /**
     * 构造函数
     *
     * @param x X坐标（相对于蓝图原点）
     * @param y Y坐标（相对于蓝图原点）
     * @param z Z坐标（相对于蓝图原点）
     * @param direction 门的朝向
     * @param id 门的唯一标识符
     */
    public DoorInfo(int x, int y, int z, DoorDirection direction, String id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.id = id != null ? id : generateDefaultId();

        this.connected = false;
        this.isEntrance = false;
        this.isExit = false;
        this.isLocked = false;
    }

    /**
     * 简化构造函数（自动生成ID）
     *
     * @param x X坐标
     * @param y Y坐标
     * @param z Z坐标
     * @param direction 门的朝向
     */
    public DoorInfo(int x, int y, int z, DoorDirection direction) {
        this(x, y, z, direction, null);
    }

    /**
     * 生成默认ID
     *
     * @return 默认ID
     */
    private String generateDefaultId() {
        return String.format("door_%d_%d_%d_%s", x, y, z, direction.name().toLowerCase());
    }

    /**
     * 获取门在世界中的绝对位置
     *
     * @param blueprintLocation 蓝图在世界中的位置
     * @param rotation 蓝图的旋转角度（0, 90, 180, 270）
     * @return 门的绝对位置
     */
    public Location getWorldLocation(Location blueprintLocation, int rotation) {
        if (blueprintLocation == null) {
            return null;
        }

        Vector relativePos = new Vector(x, y, z);
        Vector rotatedPos = rotateVector(relativePos, rotation);

        return blueprintLocation.clone().add(rotatedPos);
    }

    /**
     * 获取门的朝向（考虑蓝图旋转）
     *
     * @param rotation 蓝图的旋转角度
     * @return 旋转后的门朝向
     */
    public DoorDirection getRotatedDirection(int rotation) {
        return direction.rotate(rotation);
    }

    /**
     * 旋转向量
     *
     * @param vector 原始向量
     * @param rotation 旋转角度
     * @return 旋转后的向量
     */
    private Vector rotateVector(Vector vector, int rotation) {
        double radians = Math.toRadians(rotation);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double newX = vector.getX() * cos - vector.getZ() * sin;
        double newZ = vector.getX() * sin + vector.getZ() * cos;

        return new Vector(newX, vector.getY(), newZ);
    }

    /**
     * 检查门是否可以与另一个门连接
     *
     * @param other 另一个门
     * @param maxDistance 最大连接距离
     * @return 是否可以连接
     */
    public boolean canConnectTo(DoorInfo other, double maxDistance) {
        if (other == null || this.equals(other)) {
            return false;
        }

        // 检查朝向是否相对
        if (!this.direction.isOpposite(other.direction)) {
            return false;
        }

        // 检查距离（这里简化处理，实际应该考虑世界坐标）
        double distance = Math.sqrt(
            Math.pow(this.x - other.x, 2) +
            Math.pow(this.y - other.y, 2) +
            Math.pow(this.z - other.z, 2)
        );

        return distance <= maxDistance;
    }

    /**
     * 连接到另一个门
     *
     * @param blueprintId 目标蓝图ID
     * @param doorId 目标门ID
     */
    public void connectTo(String blueprintId, String doorId) {
        this.connected = true;
        this.connectedBlueprintId = blueprintId;
        this.connectedDoorId = doorId;
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        this.connected = false;
        this.connectedBlueprintId = null;
        this.connectedDoorId = null;
    }

    /**
     * 获取门的相对位置向量
     *
     * @return 相对位置向量
     */
    public Vector getRelativePosition() {
        return new Vector(x, y, z);
    }

    /**
     * 获取门前方的位置（玩家站立位置）
     *
     * @param blueprintLocation 蓝图位置
     * @param rotation 旋转角度
     * @return 门前方的位置
     */
    public Location getFrontLocation(Location blueprintLocation, int rotation) {
        Location doorLocation = getWorldLocation(blueprintLocation, rotation);
        if (doorLocation == null) {
            return null;
        }

        DoorDirection rotatedDirection = getRotatedDirection(rotation);
        Vector offset = rotatedDirection.getOpposite().getDirectionVector();

        return doorLocation.add(offset);
    }

    /**
     * 克隆门信息
     *
     * @return 克隆的门信息
     */
    public DoorInfo clone() {
        DoorInfo cloned = new DoorInfo(x, y, z, direction, id);
        cloned.connected = this.connected;
        cloned.connectedBlueprintId = this.connectedBlueprintId;
        cloned.connectedDoorId = this.connectedDoorId;
        cloned.isEntrance = this.isEntrance;
        cloned.isExit = this.isExit;
        cloned.isLocked = this.isLocked;
        cloned.keyRequired = this.keyRequired;
        return cloned;
    }

    // ==================== Getter 和 Setter 方法 ====================

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public DoorDirection getDirection() { return direction; }
    public String getId() { return id; }

    /**
     * 获取门的相对位置（作为Location对象）
     *
     * @return 相对位置的Location对象（world为null）
     */
    public Location getLocation() {
        return new Location(null, x, y, z);
    }

    public boolean isConnected() { return connected; }
    public String getConnectedBlueprintId() { return connectedBlueprintId; }
    public String getConnectedDoorId() { return connectedDoorId; }

    public boolean isEntrance() { return isEntrance; }
    public void setEntrance(boolean entrance) { this.isEntrance = entrance; }

    public boolean isExit() { return isExit; }
    public void setExit(boolean exit) { this.isExit = exit; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }

    public String getKeyRequired() { return keyRequired; }
    public void setKeyRequired(String keyRequired) { this.keyRequired = keyRequired; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DoorInfo doorInfo = (DoorInfo) obj;
        return x == doorInfo.x &&
               y == doorInfo.y &&
               z == doorInfo.z &&
               direction == doorInfo.direction &&
               Objects.equals(id, doorInfo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, direction, id);
    }

    @Override
    public String toString() {
        return String.format("Door{id='%s', pos=(%d,%d,%d), dir=%s, connected=%s}",
            id, x, y, z, direction, connected);
    }
}
