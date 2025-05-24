# HuanDungeonRandom

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/Paper-Latest-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

🏰 **高性能地牢随机生成插件** - 为 Minecraft 1.20.1 Paper 服务器设计的动态地牢生成系统

## 📋 项目简介

HuanDungeonRandom 是一个功能强大的 Minecraft 插件，专为创建动态、可定制的地牢体验而设计。插件采用模块化架构，支持异步生成，提供完整的 API 接口，是服务器管理员和开发者的理想选择。

### 🎯 核心特性

- **🏗️ 动态地牢生成**: 基于蓝图系统的随机地牢生成
- **⚡ 异步处理**: 所有耗时操作异步执行，保证服务器性能
- **🎨 多主题支持**: 内置5种主题（默认、火焰、冰霜、森林、深海）
- **🔧 模块化设计**: 功能系统、触发器系统、蓝图系统独立运行
- **🚀 高性能**: 支持100+并发地牢实例，TPS保持18+
- **🔌 完整API**: 为第三方插件提供完整的集成接口
- **🎮 MythicMobs集成**: 支持MythicMobs技能系统
- **📚 中文支持**: 完整的中文本地化

## 🚀 快速开始

### 安装要求

- **Minecraft版本**: 1.20.1
- **服务端**: Paper (推荐) 或 Spigot
- **Java版本**: 17 或更高
- **内存**: 建议4GB以上

### 安装步骤

1. 下载最新版本的插件jar文件
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑生成的配置文件（可选）
5. 使用 `/hdr reload` 重载配置

### 基础使用

```bash
# 创建地牢
/hdr dungeon create default

# 查看地牢列表
/hdr dungeon list

# 传送到地牢
/hdr dungeon tp <地牢ID>

# 查看帮助
/hdr help
```

## 📁 项目结构

```
HuanDungeonRandom/
├── src/main/java/org/snhuan/huanDungeonRandom/
│   ├── api/                    # API接口
│   ├── blueprint/              # 蓝图系统
│   ├── commands/               # 命令系统
│   ├── config/                 # 配置管理
│   ├── core/                   # 核心管理器
│   ├── dungeon/                # 地牢系统
│   ├── function/               # 功能系统
│   ├── generation/             # 生成算法
│   ├── listeners/              # 事件监听器
│   ├── trigger/                # 触发器系统
│   └── utils/                  # 工具类
├── src/main/resources/
│   ├── config.yml              # 主配置文件
│   ├── themes.yml              # 主题配置
│   ├── blueprints.yml          # 蓝图配置
│   ├── functions.yml           # 功能配置
│   ├── triggers.yml            # 触发器配置
│   ├── messages.yml            # 消息配置
│   └── blueprints/             # 示例蓝图
├── 使用说明.md                  # 详细使用说明
├── 快速参考.md                  # 快速参考指南
├── 开发文档.md                  # 开发文档
└── README.md                   # 项目说明
```

## 🎨 系统架构

### 蓝图系统
- **TILE**: 瓦片蓝图 - 地牢生成的基本单元
- **ROOM**: 房间蓝图 - 特殊房间结构
- **CORRIDOR**: 走廊蓝图 - 连接房间的通道
- **STRUCTURE**: 结构蓝图 - 装饰性建筑
- **DUNGEON**: 完整地牢蓝图

### 功能系统
支持13种功能类型：传送、宝箱、治疗、机关、NPC、生成点、检查点、商店、陷阱、开关、MythicMobs技能等

### 触发器系统
支持14种触发器类型：玩家进入/离开、交互、移动、方块操作、时间触发、条件满足、手动触发等

## 🔧 配置示例

### 创建自定义主题
```yaml
themes:
  my_theme:
    name: "我的主题"
    materials:
      wall: "STONE_BRICKS"
      floor: "STONE"
      ceiling: "STONE_BRICKS"
    generation:
      min_rooms: 5
      max_rooms: 15
      corridor_chance: 0.7
```

### 添加自定义功能
```yaml
predefined_functions:
  my_healing:
    type: "HEALING"
    name: "自定义治疗"
    config:
      heal_amount: 20
      cooldown: 30000
```

## 🔌 API 使用

```java
// 获取API实例
HuanDungeonAPI api = HuanDungeonAPI.getInstance();

// 创建地牢
DungeonInstance dungeon = api.getDungeonAPI()
    .createDungeon("test", theme, location, playerId);

// 执行功能
api.getFunctionAPI().executeFunction("heal", player);

// 手动触发
api.getTriggerAPI().manualTrigger("boss", player);
```

## 📊 性能特性

- **异步生成**: 所有地牢生成操作异步执行
- **多级缓存**: 蓝图缓存、地牢缓存、配置缓存
- **线程池管理**: 可配置的线程池大小
- **批量操作**: 批量方块放置和处理
- **内存优化**: 智能内存管理和垃圾回收

## 🤝 贡献指南

我们欢迎社区贡献！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 支持与反馈

- **问题报告**: [GitHub Issues](https://github.com/snhuan/HuanDungeonRandom/issues)
- **功能请求**: [GitHub Discussions](https://github.com/snhuan/HuanDungeonRandom/discussions)
- **文档**: 查看项目中的详细文档

## 🏆 致谢

感谢所有为这个项目做出贡献的开发者和测试者！

---

**⭐ 如果这个项目对您有帮助，请给我们一个星标！**
