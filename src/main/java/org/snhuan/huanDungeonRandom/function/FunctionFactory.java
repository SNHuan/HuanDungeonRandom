package org.snhuan.huanDungeonRandom.function;

import org.bukkit.configuration.ConfigurationSection;
import org.snhuan.huanDungeonRandom.function.impl.MythicSkillFunction;

import java.util.logging.Logger;

/**
 * 功能工厂类 - 负责创建各种类型的功能实例
 * 
 * 支持的功能类型：
 * - MythicMobs技能功能
 * - 传送功能
 * - 宝箱功能
 * - 治疗功能
 * - 等等...
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class FunctionFactory {
    
    private final Logger logger;
    
    /**
     * 构造函数
     * 
     * @param logger 日志记录器
     */
    public FunctionFactory(Logger logger) {
        this.logger = logger;
    }
    
    /**
     * 创建功能实例
     * 
     * @param id 功能ID
     * @param type 功能类型
     * @return 功能实例，如果类型不支持则返回null
     */
    public Function createFunction(String id, FunctionType type) {
        switch (type) {
            case MYTHIC_SKILL:
                return new MythicSkillFunction(id, logger);
                
            case TELEPORT:
                // TODO: 实现传送功能
                logger.warning("传送功能尚未实现");
                return null;
                
            case CHEST:
                // TODO: 实现宝箱功能
                logger.warning("宝箱功能尚未实现");
                return null;
                
            case HEALING:
                // TODO: 实现治疗功能
                logger.warning("治疗功能尚未实现");
                return null;
                
            case MECHANISM:
                // TODO: 实现机关功能
                logger.warning("机关功能尚未实现");
                return null;
                
            case NPC:
                // TODO: 实现NPC功能
                logger.warning("NPC功能尚未实现");
                return null;
                
            case TRIGGER:
                // TODO: 实现触发器功能
                logger.warning("触发器功能尚未实现");
                return null;
                
            case SPAWNER:
                // TODO: 实现生成点功能
                logger.warning("生成点功能尚未实现");
                return null;
                
            case CHECKPOINT:
                // TODO: 实现检查点功能
                logger.warning("检查点功能尚未实现");
                return null;
                
            case SHOP:
                // TODO: 实现商店功能
                logger.warning("商店功能尚未实现");
                return null;
                
            case TRAP:
                // TODO: 实现陷阱功能
                logger.warning("陷阱功能尚未实现");
                return null;
                
            case SWITCH:
                // TODO: 实现开关功能
                logger.warning("开关功能尚未实现");
                return null;
                
            case CUSTOM:
                // TODO: 实现自定义功能
                logger.warning("自定义功能尚未实现");
                return null;
                
            default:
                logger.warning("不支持的功能类型: " + type);
                return null;
        }
    }
    
    /**
     * 从配置创建功能实例
     * 
     * @param id 功能ID
     * @param config 配置节
     * @return 功能实例，如果创建失败则返回null
     */
    public Function createFunctionFromConfig(String id, ConfigurationSection config) {
        try {
            // 获取功能类型
            String typeString = config.getString("type");
            if (typeString == null) {
                logger.warning("功能配置中缺少类型信息: " + id);
                return null;
            }
            
            FunctionType type;
            try {
                type = FunctionType.valueOf(typeString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 尝试通过配置键获取类型
                type = FunctionType.fromConfigKey(typeString);
                if (type == FunctionType.CUSTOM && !typeString.equals("custom")) {
                    logger.warning("未知的功能类型: " + typeString + "，使用自定义类型");
                }
            }
            
            // 创建功能实例
            Function function = createFunction(id, type);
            if (function == null) {
                logger.warning("无法创建功能实例: " + id + " (类型: " + type + ")");
                return null;
            }
            
            // 从配置加载数据
            function.loadFromConfig(config);
            
            logger.info("成功创建功能: " + id + " (类型: " + type.getDisplayName() + ")");
            return function;
            
        } catch (Exception e) {
            logger.severe("从配置创建功能时发生异常: " + id + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查功能类型是否受支持
     * 
     * @param type 功能类型
     * @return 是否受支持
     */
    public boolean isTypeSupported(FunctionType type) {
        switch (type) {
            case MYTHIC_SKILL:
                return true;
            // 其他类型暂时不支持
            default:
                return false;
        }
    }
    
    /**
     * 获取所有支持的功能类型
     * 
     * @return 支持的功能类型数组
     */
    public FunctionType[] getSupportedTypes() {
        return new FunctionType[] {
            FunctionType.MYTHIC_SKILL
            // 其他支持的类型将在这里添加
        };
    }
    
    /**
     * 创建MythicMobs技能功能的便捷方法
     * 
     * @param id 功能ID
     * @param skillName MythicMobs技能名称
     * @return MythicMobs技能功能实例
     */
    public MythicSkillFunction createMythicSkillFunction(String id, String skillName) {
        MythicSkillFunction function = new MythicSkillFunction(id, logger);
        function.setSkillName(skillName);
        function.setName("MythicMobs技能: " + skillName);
        function.setDescription("执行MythicMobs技能: " + skillName);
        return function;
    }
    
    /**
     * 创建MythicMobs技能功能的便捷方法（带参数）
     * 
     * @param id 功能ID
     * @param skillName MythicMobs技能名称
     * @param skillPower 技能威力
     * @param targetType 目标类型
     * @return MythicMobs技能功能实例
     */
    public MythicSkillFunction createMythicSkillFunction(String id, String skillName, 
                                                        double skillPower, 
                                                        MythicSkillFunction.TargetType targetType) {
        MythicSkillFunction function = createMythicSkillFunction(id, skillName);
        function.setSkillPower(skillPower);
        function.setTargetType(targetType);
        return function;
    }
    
    /**
     * 验证功能配置
     * 
     * @param config 配置节
     * @return 验证结果消息，null表示验证通过
     */
    public String validateFunctionConfig(ConfigurationSection config) {
        // 检查必需的字段
        if (!config.contains("type")) {
            return "缺少必需的字段: type";
        }
        
        String typeString = config.getString("type");
        FunctionType type;
        try {
            type = FunctionType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = FunctionType.fromConfigKey(typeString);
        }
        
        // 检查类型是否受支持
        if (!isTypeSupported(type)) {
            return "不支持的功能类型: " + typeString;
        }
        
        // 根据类型进行特定验证
        switch (type) {
            case MYTHIC_SKILL:
                return validateMythicSkillConfig(config);
            default:
                return null;
        }
    }
    
    /**
     * 验证MythicMobs技能功能配置
     * 
     * @param config 配置节
     * @return 验证结果消息，null表示验证通过
     */
    private String validateMythicSkillConfig(ConfigurationSection config) {
        if (!config.contains("skill-name") || config.getString("skill-name", "").isEmpty()) {
            return "MythicMobs技能功能缺少必需的字段: skill-name";
        }
        
        // 检查技能威力是否为有效数值
        if (config.contains("skill-power")) {
            try {
                double power = config.getDouble("skill-power");
                if (power < 0) {
                    return "技能威力不能为负数";
                }
            } catch (Exception e) {
                return "技能威力必须是有效的数值";
            }
        }
        
        // 检查目标类型是否有效
        if (config.contains("target-type")) {
            String targetType = config.getString("target-type");
            try {
                MythicSkillFunction.TargetType.valueOf(targetType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "无效的目标类型: " + targetType;
            }
        }
        
        return null;
    }
}
