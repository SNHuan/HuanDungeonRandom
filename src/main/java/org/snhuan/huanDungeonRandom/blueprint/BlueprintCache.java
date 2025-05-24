package org.snhuan.huanDungeonRandom.blueprint;

import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 蓝图缓存系统 - 提供高性能的蓝图缓存和预加载功能
 * 
 * 核心功能：
 * - LRU缓存机制
 * - 自动过期清理
 * - 预加载热门蓝图
 * - 内存使用监控
 * - 缓存命中率统计
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class BlueprintCache {
    
    private final Plugin plugin;
    private final Logger logger;
    
    // 缓存配置
    private final int maxCacheSize;
    private final long expireTimeMs;
    private final long cleanupIntervalMs;
    
    // 缓存存储
    private final ConcurrentHashMap<String, CacheEntry> cache;
    
    // 统计信息
    private long hitCount;
    private long missCount;
    private long totalRequests;
    
    // 清理任务
    private ScheduledExecutorService cleanupExecutor;
    private boolean initialized;
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final Blueprint blueprint;
        final long createTime;
        volatile long lastAccessTime;
        volatile int accessCount;
        
        CacheEntry(Blueprint blueprint) {
            this.blueprint = blueprint;
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = createTime;
            this.accessCount = 1;
        }
        
        void updateAccess() {
            this.lastAccessTime = System.currentTimeMillis();
            this.accessCount++;
        }
        
        boolean isExpired(long expireTimeMs) {
            return System.currentTimeMillis() - lastAccessTime > expireTimeMs;
        }
    }
    
    /**
     * 构造函数
     * 
     * @param plugin 插件实例
     */
    public BlueprintCache(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        
        // 从配置读取缓存参数，这里使用默认值
        this.maxCacheSize = 100; // 最大缓存100个蓝图
        this.expireTimeMs = 30 * 60 * 1000; // 30分钟过期
        this.cleanupIntervalMs = 5 * 60 * 1000; // 5分钟清理一次
        
        this.cache = new ConcurrentHashMap<>();
        this.hitCount = 0;
        this.missCount = 0;
        this.totalRequests = 0;
        this.initialized = false;
    }
    
    /**
     * 初始化缓存系统
     * 
     * @return 是否初始化成功
     */
    public boolean initialize() {
        if (initialized) {
            logger.warning("蓝图缓存系统已经初始化");
            return true;
        }
        
        try {
            // 启动清理任务
            cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "BlueprintCache-Cleanup");
                thread.setDaemon(true);
                return thread;
            });
            
            cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                cleanupIntervalMs,
                cleanupIntervalMs,
                TimeUnit.MILLISECONDS
            );
            
            this.initialized = true;
            logger.info("蓝图缓存系统初始化完成 (最大缓存: " + maxCacheSize + ", 过期时间: " + (expireTimeMs / 1000) + "秒)");
            
            return true;
            
        } catch (Exception e) {
            logger.severe("蓝图缓存系统初始化失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭缓存系统
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        try {
            // 关闭清理任务
            if (cleanupExecutor != null) {
                cleanupExecutor.shutdown();
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            }
            
            // 清理缓存
            cache.clear();
            
            this.initialized = false;
            logger.info("蓝图缓存系统已关闭");
            
        } catch (Exception e) {
            logger.severe("蓝图缓存系统关闭时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取蓝图
     * 
     * @param blueprintId 蓝图ID
     * @return 蓝图实例，不存在返回null
     */
    public Blueprint get(String blueprintId) {
        if (blueprintId == null || blueprintId.trim().isEmpty()) {
            return null;
        }
        
        totalRequests++;
        
        CacheEntry entry = cache.get(blueprintId);
        if (entry != null) {
            // 检查是否过期
            if (entry.isExpired(expireTimeMs)) {
                cache.remove(blueprintId);
                missCount++;
                return null;
            }
            
            // 更新访问信息
            entry.updateAccess();
            hitCount++;
            return entry.blueprint;
        }
        
        missCount++;
        return null;
    }
    
    /**
     * 添加蓝图到缓存
     * 
     * @param blueprint 要缓存的蓝图
     */
    public void put(Blueprint blueprint) {
        if (blueprint == null || blueprint.getId() == null) {
            return;
        }
        
        // 检查缓存大小限制
        if (cache.size() >= maxCacheSize) {
            evictLeastRecentlyUsed();
        }
        
        CacheEntry entry = new CacheEntry(blueprint);
        cache.put(blueprint.getId(), entry);
    }
    
    /**
     * 从缓存中移除蓝图
     * 
     * @param blueprintId 蓝图ID
     * @return 是否移除成功
     */
    public boolean remove(String blueprintId) {
        if (blueprintId == null) {
            return false;
        }
        
        return cache.remove(blueprintId) != null;
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        hitCount = 0;
        missCount = 0;
        totalRequests = 0;
    }
    
    /**
     * 检查缓存中是否包含指定蓝图
     * 
     * @param blueprintId 蓝图ID
     * @return 是否包含
     */
    public boolean contains(String blueprintId) {
        if (blueprintId == null) {
            return false;
        }
        
        CacheEntry entry = cache.get(blueprintId);
        if (entry != null && entry.isExpired(expireTimeMs)) {
            cache.remove(blueprintId);
            return false;
        }
        
        return entry != null;
    }
    
    /**
     * 清理过期条目
     */
    private void cleanupExpiredEntries() {
        try {
            long currentTime = System.currentTimeMillis();
            int removedCount = 0;
            
            for (String key : cache.keySet()) {
                CacheEntry entry = cache.get(key);
                if (entry != null && entry.isExpired(expireTimeMs)) {
                    cache.remove(key);
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                logger.fine("清理了 " + removedCount + " 个过期的缓存条目");
            }
            
        } catch (Exception e) {
            logger.warning("清理过期缓存条目时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 驱逐最少使用的条目
     */
    private void evictLeastRecentlyUsed() {
        if (cache.isEmpty()) {
            return;
        }
        
        String lruKey = null;
        long oldestAccessTime = Long.MAX_VALUE;
        
        for (String key : cache.keySet()) {
            CacheEntry entry = cache.get(key);
            if (entry != null && entry.lastAccessTime < oldestAccessTime) {
                oldestAccessTime = entry.lastAccessTime;
                lruKey = key;
            }
        }
        
        if (lruKey != null) {
            cache.remove(lruKey);
            logger.fine("驱逐最少使用的缓存条目: " + lruKey);
        }
    }
    
    /**
     * 获取缓存大小
     * 
     * @return 当前缓存的条目数量
     */
    public int size() {
        return cache.size();
    }
    
    /**
     * 获取缓存命中率
     * 
     * @return 命中率（0.0-1.0）
     */
    public double getHitRate() {
        return totalRequests > 0 ? (double) hitCount / totalRequests : 0.0;
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 格式化的统计信息
     */
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("缓存大小: ").append(size()).append("/").append(maxCacheSize).append("\n");
        sb.append("总请求数: ").append(totalRequests).append("\n");
        sb.append("命中次数: ").append(hitCount).append("\n");
        sb.append("未命中次数: ").append(missCount).append("\n");
        sb.append("命中率: ").append(String.format("%.2f%%", getHitRate() * 100)).append("\n");
        return sb.toString();
    }
    
    /**
     * 检查缓存系统是否已初始化
     * 
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
}
