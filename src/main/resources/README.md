# HuanDungeonRandom Resources 文件说明

## 📁 文件结构

```
src/main/resources/
├── plugin.yml              # 插件主配置文件
├── config.yml              # 插件运行配置
├── blueprints.yml           # 蓝图系统配置
├── themes.yml               # 地牢主题配置
├── functions.yml            # 功能系统配置
├── triggers.yml             # 触发器系统配置
├── messages.yml             # 消息文本配置
├── blueprints/              # 蓝图文件目录
│   ├── example_room.yml     # 示例房间蓝图
│   └── example_corridor.yml # 示例走廊蓝图
└── README.md               # 本说明文件
```

## 📋 配置文件说明

### plugin.yml
- **用途**: Bukkit/Spigot 插件描述文件
- **内容**: 插件基本信息、命令定义、权限配置
- **重要性**: 必需文件，插件无法运行如果缺失

### config.yml
- **用途**: 插件主配置文件
- **内容**: 
  - 插件基础设置 (调试模式、语言等)
  - 地牢系统设置 (最大实例数、超时时间等)
  - 性能优化设置 (异步处理、缓存配置等)
  - 数据存储设置 (存储类型、备份配置等)
  - 消息系统设置 (前缀、声音效果等)
  - 集成设置 (MythicMobs、WorldEdit等)
  - 安全设置 (权限检查、操作日志等)

### blueprints.yml
- **用途**: 蓝图系统配置
- **内容**:
  - 蓝图缓存设置
  - 蓝图目录配置
  - 各类型蓝图的尺寸限制
  - 门系统配置
  - 材料映射和验证
  - 预览系统设置
  - 导入导出配置

### themes.yml
- **用途**: 地牢主题配置
- **内容**:
  - 预定义主题 (默认、火焰、冰霜、森林、深海)
  - 主题材料配置
  - 生成参数设置
  - 环境效果配置
  - 蓝图权重配置
  - 生成规则定义

### functions.yml
- **用途**: 功能系统配置
- **内容**:
  - 功能系统基础设置
  - 各类型功能配置 (传送、宝箱、治疗等)
  - 预定义功能 (欢迎消息、治疗喷泉等)
  - 执行条件配置
  - 目标类型设置
  - 效果系统配置

### triggers.yml
- **用途**: 触发器系统配置
- **内容**:
  - 触发器系统基础设置
  - 各类型触发器配置 (玩家进入、交互、时间等)
  - 预定义触发器 (地牢入口、Boss房间等)
  - 触发条件配置
  - 区域检测设置
  - 性能优化配置

### messages.yml
- **用途**: 消息文本配置
- **内容**:
  - 通用消息 (权限、错误等)
  - 地牢相关消息 (创建、进入、删除等)
  - 蓝图相关消息 (加载、验证等)
  - 功能相关消息 (执行、冷却等)
  - 触发器相关消息 (触发、条件等)
  - 队伍相关消息 (创建、邀请等)
  - 游戏内提示消息
  - 帮助消息

## 🎨 蓝图文件说明

### blueprints/ 目录
- **用途**: 存放蓝图文件
- **格式**: YAML 格式
- **类型**: 
  - TILE: 瓦片蓝图 (基本单元)
  - ROOM: 房间蓝图 (特殊房间)
  - CORRIDOR: 走廊蓝图 (连接通道)
  - STRUCTURE: 结构蓝图 (装饰建筑)
  - DUNGEON: 完整地牢蓝图

### 示例蓝图文件

#### example_room.yml
- **类型**: ROOM (房间蓝图)
- **尺寸**: 10x5x10
- **特点**: 
  - 包含宝箱功能
  - 有进入触发器
  - 支持主题适应
  - 包含装饰元素

#### example_corridor.yml
- **类型**: CORRIDOR (走廊蓝图)
- **尺寸**: 3x4x10
- **特点**:
  - 简单直线走廊
  - 支持长度变化
  - 包含照明系统
  - 可选陷阱功能

## 🔧 配置修改指南

### 1. 修改插件基础设置
编辑 `config.yml` 中的 `plugin` 部分：
```yaml
plugin:
  debug: false          # 是否启用调试模式
  language: zh_CN       # 语言设置
  auto_save_interval: 600  # 自动保存间隔
```

### 2. 调整性能参数
编辑 `config.yml` 中的 `performance` 部分：
```yaml
performance:
  async_generation: true    # 异步生成
  thread_pool_size: 4      # 线程池大小
  cache:
    dungeon_cache_size: 50 # 地牢缓存大小
```

### 3. 创建自定义主题
在 `themes.yml` 中添加新主题：
```yaml
themes:
  my_custom_theme:
    name: "我的主题"
    materials:
      wall: "STONE_BRICKS"
      floor: "STONE"
    generation:
      min_rooms: 5
      max_rooms: 15
```

### 4. 添加自定义功能
在 `functions.yml` 中添加新功能：
```yaml
predefined_functions:
  my_custom_function:
    type: "HEALING"
    name: "自定义治疗"
    config:
      heal_amount: 10
```

### 5. 创建自定义蓝图
在 `blueprints/` 目录下创建新的 `.yml` 文件，参考示例蓝图的格式。

## ⚠️ 注意事项

1. **备份配置**: 修改配置前请备份原文件
2. **YAML格式**: 注意YAML文件的缩进和格式
3. **重载配置**: 修改后使用 `/hdr reload` 重载配置
4. **权限设置**: 确保权限配置正确
5. **依赖检查**: 使用MythicMobs功能需要安装对应插件

## 🔄 配置版本

所有配置文件都包含 `config_version` 字段，用于版本控制和自动迁移：
```yaml
config_version: "1.0"
```

插件会自动检测配置版本并在需要时提示更新。

## 📞 技术支持

如果在配置过程中遇到问题：
1. 检查控制台错误信息
2. 启用调试模式查看详细日志
3. 验证YAML文件格式
4. 查看插件文档和示例
