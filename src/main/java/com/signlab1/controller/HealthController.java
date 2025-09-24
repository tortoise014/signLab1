package com.signlab1.controller;

import com.signlab1.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    
    /**
     * GET健康检查接口
     */
    @GetMapping("/check")
    public String healthCheck() {
        return "success";
    }
    
    /**
     * POST健康检查接口
     */
    @PostMapping("/check")
    public ApiResponse<String> healthCheckPost(@RequestBody(required = false) Object requestData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return ApiResponse.success("POST健康检查成功 - " + timestamp, "系统运行正常");
    }
    
    /**
     * Excel功能测试接口
     */
    @GetMapping("/test-excel")
    public ApiResponse<String> testExcel() {
        try {
            // 测试POI依赖是否正常
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            workbook.close();
            return ApiResponse.success("Excel功能正常", "POI依赖加载成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "Excel功能异常: " + e.getMessage());
        }
    }
}
