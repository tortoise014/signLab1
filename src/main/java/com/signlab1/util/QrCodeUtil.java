package com.signlab1.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 */
@Component
public class QrCodeUtil {
    
    /**
     * 生成二维码图片的Base64字符串
     */
    public String generateQrCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] imageBytes = outputStream.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成签到二维码内容
     */
    public String generateAttendanceQrContent(String courseId, String teacherCode, String classCode, Long timestamp) {
        // 构建二维码内容：课程ID|老师工号|班级编号|时间戳|随机校验码
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|%s|%d|%s", courseId, teacherCode, classCode, timestamp, randomCode);
        
        // Base64编码
        return Base64.getEncoder().encodeToString(content.getBytes());
    }
    
    /**
     * 生成签到二维码URL（用于微信扫码跳转）
     */
    public String generateAttendanceQrUrl(String courseId, String teacherCode, String classCode, Long timestamp) {
        // 构建二维码内容：课程ID|老师工号|班级编号|时间戳|随机校验码
        String randomCode = String.valueOf(System.currentTimeMillis() % 10000);
        String content = String.format("%s|%s|%s|%d|%s", courseId, teacherCode, classCode, timestamp, randomCode);
        
        // Base64编码
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());

        // 生成URL格式的二维码内容
        return String.format("https://gdutsyjx.gdut.edu.cn/signlab/student/scan?qr=%s", encodedContent);
    }
    
    /**
     * 解析签到二维码内容
     */
    public Map<String, String> parseAttendanceQrContent(String qrContent) {
        try {
            // Base64解码
            String decodedContent = new String(Base64.getDecoder().decode(qrContent));
            String[] parts = decodedContent.split("\\|");
            
            Map<String, String> result = new HashMap<>();
            if (parts.length >= 4) {
                result.put("courseId", parts[0]);
                result.put("teacherCode", parts[1]);
                result.put("classCode", parts[2]);
                result.put("timestamp", parts[3]);
                if (parts.length > 4) {
                    result.put("randomCode", parts[4]);
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("二维码内容解析失败：" + e.getMessage());
        }
    }
    
    /**
     * 验证二维码是否过期
     * @param timestamp 二维码生成时间戳（秒）
     * @param validSeconds 有效时间（秒）
     * @return true-有效，false-过期
     */
    public boolean isQrCodeValid(Long timestamp, int validSeconds) {
        if (timestamp == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        long timeDiff = currentTime - timestamp;
        
        return timeDiff >= 0 && timeDiff <= validSeconds;
    }
    
    /**
     * 计算二维码剩余有效时间
     * @param timestamp 二维码生成时间戳（秒）
     * @param validSeconds 有效时间（秒）
     * @return 剩余有效时间（秒），如果已过期返回0
     */
    public int getRemainingTime(Long timestamp, int validSeconds) {
        if (timestamp == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        long timeDiff = currentTime - timestamp;
        
        if (timeDiff < 0) {
            return validSeconds; // 还未到生成时间
        } else if (timeDiff >= validSeconds) {
            return 0; // 已过期
        } else {
            return (int) (validSeconds - timeDiff); // 剩余时间
        }
    }
}

