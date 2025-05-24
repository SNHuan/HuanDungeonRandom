package org.snhuan.huanDungeonRandom.api.blueprint;

import org.bukkit.Location;
import org.snhuan.huanDungeonRandom.blueprint.Blueprint;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintType;
import org.snhuan.huanDungeonRandom.blueprint.ValidationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 蓝图系统API - 提供蓝图相关的操作接口
 *
 * 主要功能：
 * - 注册和注销自定义蓝图
 * - 蓝图信息查询
 * - 蓝图加载和保存
 *
 * 使用示例：
 * ```java
 * BlueprintAPI api = HuanDungeonAPI.getInstance().getBlueprintAPI();
 * api.registerBlueprint(new MyCustomBlueprint());
 * Blueprint blueprint = api.getBlueprint("my_blueprint");
 * ```
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class BlueprintAPI {

    private static final Logger logger = Logger.getLogger("HuanDungeonRandom-BlueprintAPI");

    // 临时存储，实际应该连接到蓝图管理器
    private final Map<String, Blueprint> blueprints;

    /**
     * 构造函数
     */
    public BlueprintAPI() {
        this.blueprints = new HashMap<>();
    }

    // ==================== 蓝图注册和管理 ====================

    /**
     * 注册自定义蓝图
     *
     * @param blueprint 蓝图实例
     * @return 是否注册成功
     */
    public boolean registerBlueprint(Blueprint blueprint) {
        if (blueprint == null) {
            logger.warning("注册蓝图失败：蓝图实例不能为null");
            return false;
        }

        if (blueprint.getId() == null || blueprint.getId().isEmpty()) {
            logger.warning("注册蓝图失败：蓝图ID不能为空");
            return false;
        }

        try {
            if (blueprints.containsKey(blueprint.getId())) {
                logger.warning("蓝图已存在: " + blueprint.getId());
                return false;
            }

            blueprints.put(blueprint.getId(), blueprint);
            logger.info("通过API注册蓝图: " + blueprint.getId());
            return true;

        } catch (Exception e) {
            logger.severe("注册蓝图时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注销蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 是否注销成功
     */
    public boolean unregisterBlueprint(String blueprintId) {
        if (blueprintId == null) {
            return false;
        }

        try {
            Blueprint removed = blueprints.remove(blueprintId);

            if (removed != null) {
                logger.info("通过API注销蓝图: " + blueprintId);
                return true;
            }

            return false;

        } catch (Exception e) {
            logger.severe("注销蓝图时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 信息查询 ====================

    /**
     * 获取蓝图实例
     *
     * @param blueprintId 蓝图ID
     * @return 蓝图实例，不存在返回null
     */
    public Blueprint getBlueprint(String blueprintId) {
        return blueprints.get(blueprintId);
    }

    /**
     * 获取所有已注册的蓝图
     *
     * @return 蓝图实例集合
     */
    public Collection<Blueprint> getAllBlueprints() {
        return blueprints.values();
    }

    /**
     * 获取已注册蓝图数量
     *
     * @return 蓝图数量
     */
    public int getRegisteredBlueprintCount() {
        return blueprints.size();
    }

    /**
     * 获取指定类型的蓝图
     *
     * @param type 蓝图类型
     * @return 蓝图列表
     */
    public Collection<Blueprint> getBlueprintsByType(BlueprintType type) {
        return blueprints.values().stream()
            .filter(blueprint -> blueprint.getType() == type)
            .toList();
    }

    // ==================== 状态检查 ====================

    /**
     * 检查蓝图是否存在
     *
     * @param blueprintId 蓝图ID
     * @return 是否存在
     */
    public boolean blueprintExists(String blueprintId) {
        return getBlueprint(blueprintId) != null;
    }

    /**
     * 检查蓝图是否有效
     *
     * @param blueprintId 蓝图ID
     * @return 是否有效
     */
    public boolean isBlueprintValid(String blueprintId) {
        BlueprintValidationResult result = validateBlueprint(blueprintId);
        return result.isValid();
    }

    // ==================== 蓝图操作 ====================

    /**
     * 构建蓝图到指定位置
     *
     * @param blueprintId 蓝图ID
     * @param location 构建位置
     * @return 是否构建成功
     */
    public boolean buildBlueprint(String blueprintId, Location location) {
        Blueprint blueprint = getBlueprint(blueprintId);
        if (blueprint == null) {
            logger.warning("构建失败：蓝图不存在 - " + blueprintId);
            return false;
        }

        if (location == null) {
            logger.warning("构建失败：位置不能为null");
            return false;
        }

        try {
            boolean success = blueprint.build(location);

            if (success) {
                logger.info("通过API构建蓝图: " + blueprintId + " 在位置: " + formatLocation(location));
            } else {
                logger.warning("蓝图构建失败: " + blueprintId);
            }

            return success;

        } catch (Exception e) {
            logger.severe("构建蓝图时发生异常: " + blueprintId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 验证蓝图
     *
     * @param blueprintId 蓝图ID
     * @return 验证结果
     */
    public BlueprintValidationResult validateBlueprint(String blueprintId) {
        Blueprint blueprint = getBlueprint(blueprintId);
        if (blueprint == null) {
            return new BlueprintValidationResult(false, "蓝图不存在: " + blueprintId);
        }

        try {
            ValidationResult validationResult = blueprint.validate();
            
            // 使用详细的验证结果来构建BlueprintValidationResult
            return new BlueprintValidationResult(
                validationResult.isValid(),
                validationResult.getMessage(),
                validationResult.getErrors(),
                validationResult.getWarnings(),
                validationResult.getInfos()
            );

        } catch (Exception e) {
            logger.severe("验证蓝图时发生异常: " + blueprintId + " - " + e.getMessage());
            e.printStackTrace();
            return new BlueprintValidationResult(false, "验证异常: " + e.getMessage());
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取蓝图统计信息
     *
     * @return 统计信息字符串
     */
    public String getBlueprintStats() {
        int total = getRegisteredBlueprintCount();
        int valid = 0;

        for (Blueprint blueprint : getAllBlueprints()) {
            if (blueprint.isValid()) {
                valid++;
            }
        }

        return String.format(
            "已注册蓝图: %d, 有效: %d, 无效: %d",
            total, valid, total - valid
        );
    }

    /**
     * 格式化位置信息
     *
     * @param location 位置
     * @return 格式化字符串
     */
    private String formatLocation(Location location) {
        return String.format("%s: %.1f, %.1f, %.1f",
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ());
    }

    /**
     * 蓝图验证结果类
     */
    public static class BlueprintValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> errors;
        private final List<String> warnings;
        private final List<String> infos;

        public BlueprintValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.infos = new ArrayList<>();
            
            // 如果验证失败且有消息，将消息添加到错误列表
            if (!valid && message != null && !message.trim().isEmpty()) {
                this.errors.add(message);
            }
        }
        
        public BlueprintValidationResult(boolean valid, String message, 
                                        List<String> errors, List<String> warnings, List<String> infos) {
            this.valid = valid;
            this.message = message != null ? message : "";
            this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
            this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
            this.infos = infos != null ? new ArrayList<>(infos) : new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public List<String> getInfos() {
            return new ArrayList<>(infos);
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean hasInfos() {
            return !infos.isEmpty();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public int getWarningCount() {
            return warnings.size();
        }
        
        public int getInfoCount() {
            return infos.size();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("BlueprintValidationResult{valid=")
                .append(valid)
                .append(", message='").append(message).append('\'');
            
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors.size());
            }
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings.size());
            }
            if (!infos.isEmpty()) {
                sb.append(", infos=").append(infos.size());
            }
            
            sb.append('}');
            return sb.toString();
        }
    }
}
