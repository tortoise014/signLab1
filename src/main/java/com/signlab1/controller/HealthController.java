package com.signlab1.controller;

import org.springframework.web.bind.annotation.*;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    
    /**
     * 健康检查接口
     */
    @GetMapping("/check")
    public String healthCheck() {
        return "success";
    }
}
