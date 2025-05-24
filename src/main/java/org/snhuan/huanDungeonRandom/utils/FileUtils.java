package org.snhuan.huanDungeonRandom.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类 - 提供文件操作的通用方法
 * 
 * 功能包括：
 * - 文件和目录的创建、删除、复制
 * - YAML文件的读写操作
 * - 文件压缩和解压缩
 * - 文件安全性验证
 * 
 * @author HuanDungeonRandom
 * @version 1.0
 * @since 2024-01-01
 */
public class FileUtils {
    
    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());
    
    /**
     * 私有构造函数，防止实例化工具类
     */
    private FileUtils() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }
    
    /**
     * 安全地创建目录
     * 
     * @param directory 要创建的目录
     * @return 是否创建成功
     */
    public static boolean createDirectory(File directory) {
        if (directory == null) {
            return false;
        }
        
        if (directory.exists()) {
            return directory.isDirectory();
        }
        
        try {
            return directory.mkdirs();
        } catch (SecurityException e) {
            logger.severe("创建目录失败，权限不足: " + directory.getPath());
            return false;
        }
    }
    
    /**
     * 安全地删除文件或目录
     * 
     * @param file 要删除的文件或目录
     * @return 是否删除成功
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        
        try {
            if (file.isDirectory()) {
                return deleteDirectory(file);
            } else {
                return file.delete();
            }
        } catch (SecurityException e) {
            logger.severe("删除文件失败，权限不足: " + file.getPath());
            return false;
        }
    }
    
    /**
     * 递归删除目录及其所有内容
     * 
     * @param directory 要删除的目录
     * @return 是否删除成功
     */
    private static boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!deleteFile(file)) {
                    return false;
                }
            }
        }
        return directory.delete();
    }
    
    /**
     * 复制文件
     * 
     * @param source 源文件
     * @param target 目标文件
     * @return 是否复制成功
     */
    public static boolean copyFile(File source, File target) {
        if (source == null || target == null) {
            return false;
        }
        
        if (!source.exists() || !source.isFile()) {
            logger.warning("源文件不存在或不是文件: " + source.getPath());
            return false;
        }
        
        try {
            // 确保目标目录存在
            File targetDir = target.getParentFile();
            if (targetDir != null && !targetDir.exists()) {
                createDirectory(targetDir);
            }
            
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
            
        } catch (IOException e) {
            logger.severe("复制文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 读取文本文件内容
     * 
     * @param file 要读取的文件
     * @return 文件内容，读取失败返回null
     */
    public static String readTextFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            logger.severe("读取文件失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 写入文本文件
     * 
     * @param file 要写入的文件
     * @param content 文件内容
     * @return 是否写入成功
     */
    public static boolean writeTextFile(File file, String content) {
        if (file == null || content == null) {
            return false;
        }
        
        try {
            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                createDirectory(parentDir);
            }
            
            Files.writeString(file.toPath(), content);
            return true;
            
        } catch (IOException e) {
            logger.severe("写入文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 加载YAML配置文件
     * 
     * @param file YAML文件
     * @return YamlConfiguration对象，加载失败返回null
     */
    public static YamlConfiguration loadYamlFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        
        try {
            return YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            logger.severe("加载YAML文件失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 保存YAML配置文件
     * 
     * @param config YAML配置对象
     * @param file 目标文件
     * @return 是否保存成功
     */
    public static boolean saveYamlFile(YamlConfiguration config, File file) {
        if (config == null || file == null) {
            return false;
        }
        
        try {
            // 确保父目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                createDirectory(parentDir);
            }
            
            config.save(file);
            return true;
            
        } catch (IOException e) {
            logger.severe("保存YAML文件失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取目录下所有指定扩展名的文件
     * 
     * @param directory 目录
     * @param extension 文件扩展名（不包含点号）
     * @return 文件列表
     */
    public static List<File> getFilesByExtension(File directory, String extension) {
        List<File> result = new ArrayList<>();
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return result;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return result;
        }
        
        String targetExtension = "." + extension.toLowerCase();
        
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(targetExtension)) {
                result.add(file);
            }
        }
        
        return result;
    }
    
    /**
     * 验证文件名是否安全（防止路径遍历攻击）
     * 
     * @param filename 文件名
     * @return 是否安全
     */
    public static boolean isFilenameSafe(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含危险字符
        String[] dangerousPatterns = {"..", "/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
        
        for (String pattern : dangerousPatterns) {
            if (filename.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取文件大小（字节）
     * 
     * @param file 文件
     * @return 文件大小，获取失败返回-1
     */
    public static long getFileSize(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return -1;
        }
        
        try {
            return Files.size(file.toPath());
        } catch (IOException e) {
            logger.warning("获取文件大小失败: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * 检查文件是否为空
     * 
     * @param file 文件
     * @return 是否为空
     */
    public static boolean isFileEmpty(File file) {
        return getFileSize(file) == 0;
    }
    
    /**
     * 创建备份文件
     * 
     * @param originalFile 原始文件
     * @return 备份文件，创建失败返回null
     */
    public static File createBackup(File originalFile) {
        if (originalFile == null || !originalFile.exists()) {
            return null;
        }
        
        String backupName = originalFile.getName() + ".backup." + System.currentTimeMillis();
        File backupFile = new File(originalFile.getParent(), backupName);
        
        if (copyFile(originalFile, backupFile)) {
            return backupFile;
        }
        
        return null;
    }
    
    /**
     * 压缩文件到ZIP
     * 
     * @param sourceFile 源文件
     * @param zipFile 目标ZIP文件
     * @return 是否压缩成功
     */
    public static boolean zipFile(File sourceFile, File zipFile) {
        if (sourceFile == null || zipFile == null || !sourceFile.exists()) {
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(sourceFile)) {
            
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
            return true;
            
        } catch (IOException e) {
            logger.severe("压缩文件失败: " + e.getMessage());
            return false;
        }
    }
}
