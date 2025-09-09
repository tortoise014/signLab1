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
}

