package org.snhuan.huanDungeonRandom.generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.snhuan.huanDungeonRandom.blueprint.BlueprintManager;
import org.snhuan.huanDungeonRandom.dungeon.DungeonInstance;
import org.snhuan.huanDungeonRandom.dungeon.DungeonTheme;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 异步地牢生成器 - 在后台线程中生成地牢，避免阻塞主线程
 *
 * 功能特点：
 * - 异步生成地牢，不影响服务器TPS
 * - 支持生成进度回调
 * - 自动管理线程池
 * - 支持生成超时处理
 * - 提供生成状态监控
 *
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class AsyncGenerator {

    private final JavaPlugin plugin;
    private final DungeonGenerator dungeonGenerator;
    private final BlueprintManager blueprintManager;
    private final Logger logger;

    // 线程池管理
    private final ExecutorService generationExecutor;
    private final int maxConcurrentGenerations;
    private volatile int activeGenerations = 0;

    // 配置参数
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_MAX_CONCURRENT = 5;

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     * @param dungeonGenerator 地牢生成器
     * @param blueprintManager 蓝图管理器
     */
    public AsyncGenerator(JavaPlugin plugin, DungeonGenerator dungeonGenerator, BlueprintManager blueprintManager) {
        this.plugin = plugin;
        this.dungeonGenerator = dungeonGenerator;
        this.blueprintManager = blueprintManager;
        this.logger = plugin.getLogger();

        // 从配置获取最大并发数
        this.maxConcurrentGenerations = plugin.getConfig().getInt("performance.max-concurrent-generations", DEFAULT_MAX_CONCURRENT);

        // 创建线程池
        this.generationExecutor = Executors.newFixedThreadPool(maxConcurrentGenerations, r -> {
            Thread thread = new Thread(r, "DungeonGenerator-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });

        logger.info("异步生成器初始化完成，最大并发数: " + maxConcurrentGenerations);
    }

    /**
     * 异步生成地牢
     *
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param world 目标世界
     * @param origin 生成位置
     * @param createdBy 创建者
     * @return CompletableFuture包装的地牢实例
     */
    public CompletableFuture<DungeonInstance> generateAsync(String dungeonId, DungeonTheme theme,
                                                           World world, Location origin, UUID createdBy) {
        return generateAsync(dungeonId, theme, world, origin, createdBy, DEFAULT_TIMEOUT_SECONDS, null);
    }

    /**
     * 异步生成地牢（带进度回调）
     *
     * @param dungeonId 地牢ID
     * @param theme 地牢主题
     * @param world 目标世界
     * @param origin 生成位置
     * @param createdBy 创建者
     * @param timeoutSeconds 超时时间（秒）
     * @param progressCallback 进度回调
     * @return CompletableFuture包装的地牢实例
     */
    public CompletableFuture<DungeonInstance> generateAsync(String dungeonId, DungeonTheme theme,
                                                           World world, Location origin, UUID createdBy,
                                                           int timeoutSeconds, ProgressCallback progressCallback) {

        // 检查并发限制
        if (activeGenerations >= maxConcurrentGenerations) {
            CompletableFuture<DungeonInstance> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("已达到最大并发生成数限制: " + maxConcurrentGenerations));
            return failedFuture;
        }

        // 创建异步任务
        CompletableFuture<DungeonInstance> future = CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                activeGenerations++;
            }

            try {
                logger.info("开始异步生成地牢: " + dungeonId + " 在位置: " + origin);

                // 更新进度：开始生成
                if (progressCallback != null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                        progressCallback.onProgress(0, "开始生成地牢..."));
                }

                // 执行地牢生成
                DungeonInstance instance = dungeonGenerator.generateDungeon(dungeonId, theme, world, origin, createdBy);

                if (instance != null) {
                    // 更新进度：生成完成
                    if (progressCallback != null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                            progressCallback.onProgress(100, "地牢生成完成"));
                    }

                    logger.info("地牢生成成功: " + dungeonId);
                    return instance;
                } else {
                    throw new RuntimeException("地牢生成失败: " + dungeonId);
                }

            } catch (Exception e) {
                logger.severe("异步生成地牢失败: " + e.getMessage());

                // 更新进度：生成失败
                if (progressCallback != null) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                        progressCallback.onError("生成失败: " + e.getMessage()));
                }

                throw new RuntimeException("地牢生成异常", e);

            } finally {
                synchronized (this) {
                    activeGenerations--;
                }
            }

        }, generationExecutor);

        // 设置超时
        CompletableFuture<DungeonInstance> timeoutFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!future.isDone()) {
                future.cancel(true);
                timeoutFuture.completeExceptionally(new RuntimeException("地牢生成超时: " + timeoutSeconds + "秒"));
            }
        }, timeoutSeconds * 20L); // 转换为tick

        // 返回带超时的Future
        return CompletableFuture.anyOf(future, timeoutFuture)
            .thenCompose(result -> {
                if (result instanceof DungeonInstance) {
                    return CompletableFuture.completedFuture((DungeonInstance) result);
                } else {
                    return CompletableFuture.failedFuture(new RuntimeException("未知错误"));
                }
            });
    }

    /**
     * 批量异步生成地牢（用于预缓存）
     *
     * @param requests 生成请求列表
     * @return CompletableFuture包装的生成结果列表
     */
    public CompletableFuture<java.util.List<DungeonInstance>> generateBatchAsync(
            java.util.List<GenerationRequest> requests) {

        logger.info("开始批量异步生成 " + requests.size() + " 个地牢");

        // 创建所有异步任务
        java.util.List<CompletableFuture<DungeonInstance>> futures = new java.util.ArrayList<>();

        for (GenerationRequest request : requests) {
            CompletableFuture<DungeonInstance> future = generateAsync(
                request.getDungeonId(),
                request.getTheme(),
                request.getWorld(),
                request.getOrigin(),
                request.getCreatedBy(),
                request.getTimeoutSeconds(),
                request.getProgressCallback()
            );
            futures.add(future);
        }

        // 等待所有任务完成
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                java.util.List<DungeonInstance> results = new java.util.ArrayList<>();
                for (CompletableFuture<DungeonInstance> future : futures) {
                    try {
                        DungeonInstance instance = future.get();
                        if (instance != null) {
                            results.add(instance);
                        }
                    } catch (Exception e) {
                        logger.warning("批量生成中的某个地牢失败: " + e.getMessage());
                    }
                }
                logger.info("批量生成完成，成功: " + results.size() + "/" + requests.size());
                return results;
            });
    }

    /**
     * 获取当前活跃的生成任务数
     *
     * @return 活跃任务数
     */
    public int getActiveGenerations() {
        return activeGenerations;
    }

    /**
     * 获取最大并发生成数
     *
     * @return 最大并发数
     */
    public int getMaxConcurrentGenerations() {
        return maxConcurrentGenerations;
    }

    /**
     * 检查是否可以开始新的生成任务
     *
     * @return 是否可以生成
     */
    public boolean canGenerate() {
        return activeGenerations < maxConcurrentGenerations;
    }

    /**
     * 关闭异步生成器
     */
    public void shutdown() {
        logger.info("正在关闭异步生成器...");

        generationExecutor.shutdown();
        try {
            if (!generationExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warning("异步生成器未能在10秒内正常关闭，强制关闭");
                generationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warning("等待异步生成器关闭时被中断");
            generationExecutor.shutdownNow();
        }

        logger.info("异步生成器已关闭");
    }

    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        /**
         * 进度更新
         *
         * @param progress 进度百分比 (0-100)
         * @param message 进度消息
         */
        void onProgress(int progress, String message);

        /**
         * 生成出错
         *
         * @param error 错误消息
         */
        void onError(String error);
    }

    /**
     * 获取生成器状态信息
     *
     * @return 状态信息字符串
     */
    public String getStatusInfo() {
        StringBuilder status = new StringBuilder();
        status.append("异步生成器状态:\n");
        status.append("- 活跃任务数: ").append(activeGenerations).append("/").append(maxConcurrentGenerations).append("\n");
        status.append("- 线程池状态: ").append(generationExecutor.isShutdown() ? "已关闭" : "运行中").append("\n");
        status.append("- 可用性: ").append(canGenerate() ? "可用" : "已满").append("\n");
        return status.toString();
    }

    /**
     * 生成请求类
     */
    public static class GenerationRequest {
        private final String dungeonId;
        private final DungeonTheme theme;
        private final World world;
        private final Location origin;
        private final UUID createdBy;
        private final int timeoutSeconds;
        private final ProgressCallback progressCallback;

        public GenerationRequest(String dungeonId, DungeonTheme theme, World world, Location origin,
                               UUID createdBy, int timeoutSeconds, ProgressCallback progressCallback) {
            this.dungeonId = dungeonId;
            this.theme = theme;
            this.world = world;
            this.origin = origin;
            this.createdBy = createdBy;
            this.timeoutSeconds = timeoutSeconds;
            this.progressCallback = progressCallback;
        }

        /**
         * 创建简单的生成请求
         */
        public static GenerationRequest simple(String dungeonId, DungeonTheme theme, World world,
                                             Location origin, UUID createdBy) {
            return new GenerationRequest(dungeonId, theme, world, origin, createdBy,
                                       DEFAULT_TIMEOUT_SECONDS, null);
        }

        /**
         * 创建带回调的生成请求
         */
        public static GenerationRequest withCallback(String dungeonId, DungeonTheme theme, World world,
                                                   Location origin, UUID createdBy, ProgressCallback callback) {
            return new GenerationRequest(dungeonId, theme, world, origin, createdBy,
                                       DEFAULT_TIMEOUT_SECONDS, callback);
        }

        // Getter方法
        public String getDungeonId() { return dungeonId; }
        public DungeonTheme getTheme() { return theme; }
        public World getWorld() { return world; }
        public Location getOrigin() { return origin; }
        public UUID getCreatedBy() { return createdBy; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public ProgressCallback getProgressCallback() { return progressCallback; }
    }
}
