package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.snhuan.huanDungeonRandom.function.Function;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * 蓝图抽象基类 - 定义所有蓝图的通用属性和行为
 *
 * 蓝图是地牢的基本构建单元，包含：
 * - 建筑结构数据
 * - 门的位置和连接信息
 * - 功能点配置
 * - 元数据信息
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public abstract class Blueprint {

    protected static final Logger logger = Logger.getLogger(Blueprint.class.getName());

    // 基本属性
    protected String name;
    protected String description;
    protected BlueprintType type;
    protected String author;
    protected long createdTime;
    protected long modifiedTime;

    // 尺寸信息
    protected int sizeX;
    protected int sizeY;
    protected int sizeZ;

    // 门的位置信息
    protected List<DoorInfo> doors;

    // 功能点列表
    protected List<Function> functions;

    // 方块数据
    protected Map<Location, Material> blockData;

    // 配置文件
    protected File configFile;
    protected YamlConfiguration config;

    // 标签和分类
    protected Set<String> tags;
    protected String category;

    /**
     * 构造函数
     *
     * @param name 蓝图名称
     * @param type 蓝图类型
     */
    protected Blueprint(String name, BlueprintType type) {
        this.name = name;
        this.type = type;
        this.createdTime = System.currentTimeMillis();
        this.modifiedTime = this.createdTime;

        this.doors = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.blockData = new HashMap<>();
        this.tags = new HashSet<>();

        this.description = "";
        this.author = "Unknown";
        this.category = "default";
    }

    /**
     * 验证蓝图的有效性
     *
     * @return 验证结果
     */
    public ValidationResult validate() {
        ValidationResult.Builder builder = ValidationResult.builder()
            .setMessage("蓝图基础验证");

        // 验证基本属性
        if (name == null || name.trim().isEmpty()) {
            builder.addError("蓝图名称不能为空");
        }

        if (type == null) {
            builder.addError("蓝图类型不能为空");
        }

        // 验证尺寸
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            builder.addError("蓝图尺寸必须大于0");
        }

        // 验证门信息
        if (doors == null) {
            builder.addError("门信息列表不能为null");
        } else {
            for (DoorInfo door : doors) {
                if (door == null) {
                    builder.addWarning("发现null门信息");
                    continue;
                }

                // 检查门是否在蓝图范围内
                if (door.getX() < 0 || door.getX() >= sizeX ||
                    door.getY() < 0 || door.getY() >= sizeY ||
                    door.getZ() < 0 || door.getZ() >= sizeZ) {
                    builder.addError("门位置超出蓝图范围: " + door);
                }
            }
        }

        return builder.build();
    }

    /**
     * 抽象方法：在世界中放置蓝图
     *
     * @param world 目标世界
     * @param location 放置位置
     * @param rotation 旋转角度（0, 90, 180, 270）
     * @return 是否放置成功
     */
    public abstract boolean place(World world, Location location, int rotation);

    /**
     * 抽象方法：获取蓝图的预览信息
     *
     * @return 预览信息
     */
    public abstract PreviewInfo getPreviewInfo();

    /**
     * 保存蓝图到文件
     *
     * @param file 目标文件
     * @return 是否保存成功
     */
    public boolean save(File file) {
        if (file == null) {
            logger.warning("保存文件不能为空");
            return false;
        }

        try {
            YamlConfiguration saveConfig = new YamlConfiguration();

            // 保存基本信息
            saveConfig.set("name", name);
            saveConfig.set("description", description);
            saveConfig.set("type", type.name());
            saveConfig.set("author", author);
            saveConfig.set("created-time", createdTime);
            saveConfig.set("modified-time", System.currentTimeMillis());
            saveConfig.set("category", category);

            // 保存尺寸信息
            saveConfig.set("size.x", sizeX);
            saveConfig.set("size.y", sizeY);
            saveConfig.set("size.z", sizeZ);

            // 保存门信息
            List<Map<String, Object>> doorList = new ArrayList<>();
            for (DoorInfo door : doors) {
                Map<String, Object> doorMap = new HashMap<>();
                doorMap.put("x", door.getX());
                doorMap.put("y", door.getY());
                doorMap.put("z", door.getZ());
                doorMap.put("direction", door.getDirection().name());
                doorMap.put("id", door.getId());
                doorList.add(doorMap);
            }
            saveConfig.set("doors", doorList);

            // 保存标签
            saveConfig.set("tags", new ArrayList<>(tags));

            // 保存特定类型的数据
            saveTypeSpecificData(saveConfig);

            saveConfig.save(file);
            this.configFile = file;
            this.config = saveConfig;
            this.modifiedTime = System.currentTimeMillis();

            logger.info("蓝图 " + name + " 保存成功");
            return true;

        } catch (Exception e) {
            logger.severe("保存蓝图失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从文件加载蓝图
     *
     * @param file 源文件
     * @return 是否加载成功
     */
    public boolean load(File file) {
        if (file == null || !file.exists()) {
            logger.warning("蓝图文件不存在: " + (file != null ? file.getPath() : "null"));
            return false;
        }

        try {
            YamlConfiguration loadConfig = YamlConfiguration.loadConfiguration(file);

            // 加载基本信息
            this.name = loadConfig.getString("name", "Unknown");
            this.description = loadConfig.getString("description", "");
            this.author = loadConfig.getString("author", "Unknown");
            this.createdTime = loadConfig.getLong("created-time", System.currentTimeMillis());
            this.modifiedTime = loadConfig.getLong("modified-time", System.currentTimeMillis());
            this.category = loadConfig.getString("category", "default");

            // 加载尺寸信息
            this.sizeX = loadConfig.getInt("size.x", 16);
            this.sizeY = loadConfig.getInt("size.y", 8);
            this.sizeZ = loadConfig.getInt("size.z", 16);

            // 加载门信息
            this.doors.clear();
            List<Map<?, ?>> doorList = loadConfig.getMapList("doors");
            for (Map<?, ?> doorMap : doorList) {
                int x = (Integer) doorMap.get("x");
                int y = (Integer) doorMap.get("y");
                int z = (Integer) doorMap.get("z");
                String directionStr = (String) doorMap.get("direction");
                String id = (String) doorMap.get("id");

                DoorDirection direction = DoorDirection.valueOf(directionStr);
                DoorInfo door = new DoorInfo(x, y, z, direction, id);
                this.doors.add(door);
            }

            // 加载标签
            List<String> tagList = loadConfig.getStringList("tags");
            this.tags.clear();
            this.tags.addAll(tagList);

            // 加载特定类型的数据
            loadTypeSpecificData(loadConfig);

            this.configFile = file;
            this.config = loadConfig;

            logger.info("蓝图 " + name + " 加载成功");
            return true;

        } catch (Exception e) {
            logger.severe("加载蓝图失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存类型特定的数据（由子类实现）
     *
     * @param config 配置对象
     */
    protected abstract void saveTypeSpecificData(YamlConfiguration config);

    /**
     * 加载类型特定的数据（由子类实现）
     *
     * @param config 配置对象
     */
    protected abstract void loadTypeSpecificData(YamlConfiguration config);

    /**
     * 添加门
     *
     * @param door 门信息
     */
    public void addDoor(DoorInfo door) {
        if (door != null && !doors.contains(door)) {
            doors.add(door);
            updateModifiedTime();
        }
    }

    /**
     * 移除门
     *
     * @param door 门信息
     */
    public void removeDoor(DoorInfo door) {
        if (doors.remove(door)) {
            updateModifiedTime();
        }
    }

    /**
     * 添加功能
     *
     * @param function 功能对象
     */
    public void addFunction(Function function) {
        if (function != null && !functions.contains(function)) {
            functions.add(function);
            updateModifiedTime();
        }
    }

    /**
     * 移除功能
     *
     * @param function 功能对象
     */
    public void removeFunction(Function function) {
        if (functions.remove(function)) {
            updateModifiedTime();
        }
    }

    /**
     * 添加标签
     *
     * @param tag 标签
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            if (tags.add(tag.trim().toLowerCase())) {
                updateModifiedTime();
            }
        }
    }

    /**
     * 移除标签
     *
     * @param tag 标签
     */
    public void removeTag(String tag) {
        if (tag != null && tags.remove(tag.trim().toLowerCase())) {
            updateModifiedTime();
        }
    }

    /**
     * 检查是否包含标签
     *
     * @param tag 标签
     * @return 是否包含
     */
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim().toLowerCase());
    }

    /**
     * 更新修改时间
     */
    protected void updateModifiedTime() {
        this.modifiedTime = System.currentTimeMillis();
    }

    /**
     * 克隆蓝图
     *
     * @return 克隆的蓝图
     */
    public abstract Blueprint clone();

    // ==================== Getter 和 Setter 方法 ====================

    public String getId() { return name; } // 使用名称作为ID
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateModifiedTime();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        updateModifiedTime();
    }

    public BlueprintType getType() { return type; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) {
        this.author = author;
        updateModifiedTime();
    }

    public long getCreatedTime() { return createdTime; }
    public long getModifiedTime() { return modifiedTime; }

    public int getSizeX() { return sizeX; }
    public int getSizeY() { return sizeY; }
    public int getSizeZ() { return sizeZ; }

    public void setSize(int x, int y, int z) {
        this.sizeX = x;
        this.sizeY = y;
        this.sizeZ = z;
        updateModifiedTime();
    }

    public List<DoorInfo> getDoors() { return new ArrayList<>(doors); }
    public List<Function> getFunctions() { return new ArrayList<>(functions); }
    public Set<String> getTags() { return new HashSet<>(tags); }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
        updateModifiedTime();
    }

    public File getConfigFile() { return configFile; }
    public YamlConfiguration getConfig() { return config; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Blueprint blueprint = (Blueprint) obj;
        return Objects.equals(name, blueprint.name) && type == blueprint.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return String.format("Blueprint{name='%s', type=%s, size=%dx%dx%d}",
            name, type, sizeX, sizeY, sizeZ);
    }

    /**
     * 检查蓝图是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        // 基础验证
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return false;
        }
        if (type == null) {
            return false;
        }

        // 子类可以重写此方法添加更多验证
        return validateSpecific();
    }

    /**
     * 特定类型的验证（由子类实现）
     *
     * @return 是否通过验证
     */
    protected boolean validateSpecific() {
        return true; // 默认通过
    }

    /**
     * 构建蓝图到指定位置
     *
     * @param location 构建位置
     * @return 是否构建成功
     */
    public boolean build(org.bukkit.Location location) {
        if (!isValid()) {
            logger.warning("蓝图无效，无法构建: " + name);
            return false;
        }

        if (location == null) {
            logger.warning("构建位置不能为null");
            return false;
        }

        try {
            return buildSpecific(location);
        } catch (Exception e) {
            logger.severe("构建蓝图时发生异常: " + name + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 特定类型的构建逻辑（由子类实现）
     *
     * @param location 构建位置
     * @return 是否构建成功
     */
    protected abstract boolean buildSpecific(org.bukkit.Location location);
}
