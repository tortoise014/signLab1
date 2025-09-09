package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.User;
import com.signlab1.mapper.ClassMapper;
import com.signlab1.mapper.CourseMapper;
import com.signlab1.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 管理员数据导入服务
 */
@Service
@RequiredArgsConstructor
public class AdminImportService {
    
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    
    /**
     * 导入用户数据（学生和老师）
     */
    public String importUsers(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<User> users = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    User user = new User();
                    user.setUserCode(getCellValue(row.getCell(0))); // 学号/工号
                    user.setName(getCellValue(row.getCell(1))); // 姓名
                    user.setRole(getCellValue(row.getCell(2))); // 角色
                    user.setPasswordSet(0); // 初始状态未设置密码
                    user.setCreateTime(LocalDateTime.now());
                    user.setUpdateTime(LocalDateTime.now());
                    user.setIsDeleted(0);
                    
                    // 检查是否已存在
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_code", user.getUserCode());
                    if (userMapper.selectOne(queryWrapper) == null) {
                        userMapper.insert(user);
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            workbook.close();
            return String.format("导入完成！成功：%d条，失败：%d条", successCount, errorCount);
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 导入班级数据
     */
    public String importClasses(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            int successCount = 0;
            int errorCount = 0;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Class clazz = new Class();
                    clazz.setClassCode(getCellValue(row.getCell(0))); // 班级编号
                    clazz.setClassName(getCellValue(row.getCell(1))); // 班级名称
                    clazz.setStudentCount(Integer.parseInt(getCellValue(row.getCell(2)))); // 班级人数
                    clazz.setVerificationCode(generateVerificationCode()); // 生成验证码
                    clazz.setCreateTime(LocalDateTime.now());
                    clazz.setUpdateTime(LocalDateTime.now());
                    clazz.setIsDeleted(0);
                    
                    // 检查是否已存在
                    QueryWrapper<Class> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("class_code", clazz.getClassCode());
                    if (classMapper.selectOne(queryWrapper) == null) {
                        classMapper.insert(clazz);
                        successCount++;
                    } else {
                        errorCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            workbook.close();
            return String.format("导入完成！成功：%d条，失败：%d条", successCount, errorCount);
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 导入课程数据
     */
    public String importCourses(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            int successCount = 0;
            int errorCount = 0;
            int courseIdCounter = 1;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Course course = new Course();
                    course.setCourseName(getCellValue(row.getCell(0))); // 课程名称
                    course.setTeacherCode(getCellValue(row.getCell(1))); // 授课老师工号
                    course.setClassCode(getCellValue(row.getCell(2))); // 上课班级
                    course.setLocation(getCellValue(row.getCell(3))); // 上课地点
                    course.setCourseDate(getCellValue(row.getCell(4))); // 课程日期
                    course.setTimeSlot(getCellValue(row.getCell(5))); // 上课时间段
                    course.setWeekNumber(Integer.parseInt(getCellValue(row.getCell(6)))); // 课程周次
                    
                    // 生成课程ID：KC + 年份后2位 + 6位自增数
                    String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
                    course.setCourseId(String.format("KC%s%06d", year, courseIdCounter++));
                    
                    course.setCreateTime(LocalDateTime.now());
                    course.setUpdateTime(LocalDateTime.now());
                    course.setIsDeleted(0);
                    
                    courseMapper.insert(course);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            workbook.close();
            return String.format("导入完成！成功：%d条，失败：%d条", successCount, errorCount);
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}

