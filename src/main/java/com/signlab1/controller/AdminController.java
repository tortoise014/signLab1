package com.signlab1.controller;

import com.signlab1.dto.ApiResponse;
import com.signlab1.service.AdminImportService;
import com.signlab1.service.ExcelTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 管理员数据导入控制器
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminImportService adminImportService;
    private final ExcelTemplateService excelTemplateService;

    /**
     * 导入用户数据
     */
    @PostMapping("/import/users")
    public ApiResponse<String> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            String result = adminImportService.importUsers(file);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 导入班级数据
     */
    @PostMapping("/import/classes")
    public ApiResponse<String> importClasses(@RequestParam("file") MultipartFile file) {
        try {
            String result = adminImportService.importClasses(file);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 导入课程数据
     */
    @PostMapping("/import/courses")
    public ApiResponse<String> importCourses(@RequestParam("file") MultipartFile file) {
        try {
            String result = adminImportService.importCourses(file);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 下载用户数据模板
     */
    @GetMapping("/template/users")
    public ResponseEntity<byte[]> downloadUserTemplate() {
        try {
            byte[] template = excelTemplateService.generateUserTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("用户数据导入模板.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载班级数据模板
     */
    @GetMapping("/template/classes")
    public ResponseEntity<byte[]> downloadClassTemplate() {
        try {
            byte[] template = excelTemplateService.generateClassTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("班级数据导入模板.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载课程数据模板
     */
    @GetMapping("/template/courses")
    public ResponseEntity<byte[]> downloadCourseTemplate() {
        try {
            byte[] template = excelTemplateService.generateCourseTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("课程数据导入模板.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(template);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出用户数据
     */
    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers() {
        try {
            byte[] data = excelTemplateService.generateUserTemplate(); // 这里可以改为实际的导出逻辑

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("用户数据导出.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出班级数据
     */
    @GetMapping("/export/classes")
    public ResponseEntity<byte[]> exportClasses() {
        try {
            byte[] data = excelTemplateService.generateClassTemplate(); // 这里可以改为实际的导出逻辑

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("班级数据导出.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出课程数据
     */
    @GetMapping("/export/courses")
    public ResponseEntity<byte[]> exportCourses() {
        try {
            byte[] data = excelTemplateService.generateCourseTemplate(); // 这里可以改为实际的导出逻辑

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            String encodedFilename = URLEncoder.encode("课程数据导出.xlsx", StandardCharsets.UTF_8);
            headers.setContentDispositionFormData("attachment", encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
