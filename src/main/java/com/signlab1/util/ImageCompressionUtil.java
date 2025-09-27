package com.signlab1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图像压缩工具类
 */
@Slf4j
@Component
public class ImageCompressionUtil {
    
    // 压缩参数配置
    private static final int COMPRESSED_WIDTH = 800;
    private static final int COMPRESSED_HEIGHT = 600;
    private static final float COMPRESSED_QUALITY = 0.8f;
    
    /**
     * 压缩图像
     * @param originalImage 原图像
     * @return 压缩后的图像
     */
    public BufferedImage compressImage(BufferedImage originalImage) {
        try {
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 计算压缩后的尺寸
            Dimension newSize = calculateNewSize(originalWidth, originalHeight);
            
            // 创建压缩后的图像
            BufferedImage compressedImage = new BufferedImage(
                newSize.width, 
                newSize.height, 
                BufferedImage.TYPE_INT_RGB
            );
            
            // 绘制压缩后的图像
            Graphics2D g2d = compressedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalImage, 0, 0, newSize.width, newSize.height, null);
            g2d.dispose();
            
            log.info("图像压缩完成: {}x{} -> {}x{}", 
                    originalWidth, originalHeight, newSize.width, newSize.height);
            
            return compressedImage;
            
        } catch (Exception e) {
            log.error("图像压缩失败: {}", e.getMessage(), e);
            throw new RuntimeException("图像压缩失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算压缩后的尺寸
     */
    private Dimension calculateNewSize(int originalWidth, int originalHeight) {
        // 如果原图已经很小，不需要压缩
        if (originalWidth <= COMPRESSED_WIDTH && originalHeight <= COMPRESSED_HEIGHT) {
            return new Dimension(originalWidth, originalHeight);
        }
        
        // 计算压缩比例
        double widthRatio = (double) COMPRESSED_WIDTH / originalWidth;
        double heightRatio = (double) COMPRESSED_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new Dimension(newWidth, newHeight);
    }
    
    /**
     * 保存压缩图像到文件
     * @param compressedImage 压缩后的图像
     * @param outputPath 输出路径
     * @param format 图像格式 (jpg, png等)
     */
    public void saveCompressedImage(BufferedImage compressedImage, String outputPath, String format) {
        try {
            File outputFile = new File(outputPath);
            // 确保父目录存在
            outputFile.getParentFile().mkdirs();
            
            ImageIO.write(compressedImage, format, outputFile);
            log.info("压缩图像保存成功: {}", outputPath);
            
        } catch (IOException e) {
            log.error("保存压缩图像失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存压缩图像失败: " + e.getMessage());
        }
    }
    
    /**
     * 将图像转换为字节数组
     * @param image 图像
     * @param format 图像格式
     * @return 字节数组
     */
    public byte[] imageToBytes(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("图像转字节数组失败: {}", e.getMessage(), e);
            throw new RuntimeException("图像转字节数组失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取图像格式
     * @param fileName 文件名
     * @return 图像格式
     */
    public String getImageFormat(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "gif":
                return "gif";
            default:
                return "jpg";
        }
    }
    
    /**
     * 检查是否需要压缩
     * @param originalImage 原图像
     * @return 是否需要压缩
     */
    public boolean needsCompression(BufferedImage originalImage) {
        return originalImage.getWidth() > COMPRESSED_WIDTH || 
               originalImage.getHeight() > COMPRESSED_HEIGHT;
    }
    
    /**
     * 获取压缩参数信息
     */
    public String getCompressionInfo() {
        return String.format("压缩参数: 最大尺寸 %dx%d, 质量 %.1f%%", 
                COMPRESSED_WIDTH, COMPRESSED_HEIGHT, COMPRESSED_QUALITY * 100);
    }
}


