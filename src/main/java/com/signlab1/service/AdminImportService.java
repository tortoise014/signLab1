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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 管理员数据导入服务
 */
@Service
@RequiredArgsConstructor
public class AdminImportService {
    
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final ScheduleParserService scheduleParserService;
    
    // 用于格式化单元格值，保持原始格式
    private final DataFormatter dataFormatter = new DataFormatter();
    
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
                    user.setUsername(getCellValue(row.getCell(0))); // 用户名
                    user.setName(getCellValue(row.getCell(1))); // 姓名
                    user.setRole(getCellValue(row.getCell(2))); // 角色
                    user.setPasswordSet(0); // 初始状态未设置密码
                    user.setCreateTime(LocalDateTime.now());
                    user.setUpdateTime(LocalDateTime.now());
                    user.setIsDeleted(0);
                    
                    // 检查是否已存在
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("username", user.getUsername());
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
                    course.setTeacherUsername(getCellValue(row.getCell(1))); // 授课老师用户名
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
        
        // 使用DataFormatter获取单元格的显示值，这样可以保持原始格式
        // 对于工号00005642这样的格式，会保持前导零
        return dataFormatter.formatCellValue(cell).trim();
    }
    
    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * 老师导入学生数据（自动生成课程）
     */
    public String importStudentsForTeacher(MultipartFile file, String teacherCode) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<User> students = new ArrayList<>();
            List<Course> courses = new ArrayList<>();
            Set<String> processedSchedules = new HashSet<>(); // 避免重复解析相同的课表
            int successCount = 0;
            int errorCount = 0;
            String courseName = "工程实践B"; // 默认课程名称
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 根据新的Excel格式读取数据
                    String className = getCellValue(row.getCell(0));      // 班级名称
                    String studentCode = getCellValue(row.getCell(1));    // 学号
                    String studentName = getCellValue(row.getCell(2));    // 姓名
                    String department = getCellValue(row.getCell(3));     // 院系
                    String major = getCellValue(row.getCell(4));          // 专业
                    String teachers = getCellValue(row.getCell(5));       // 任课教师
                    String schedule = getCellValue(row.getCell(6));       // 上课时间地点
                    
                    if (studentCode.isEmpty() || studentName.isEmpty() || className.isEmpty()) {
                        errorCount++;
                        continue;
                    }
                    
                    // 检查学生是否已存在
                    QueryWrapper<User> userQuery = new QueryWrapper<>();
                    userQuery.eq("username", studentCode);
                    User existingUser = userMapper.selectOne(userQuery);
                    
                    if (existingUser == null) {
                        User student = new User();
                        student.setUsername(studentCode);
                        student.setName(studentName);
                        student.setPassword(studentCode.length() >= 4 ? studentCode.substring(studentCode.length() - 4) : "1234"); // 学号后四位作为密码
                        student.setRole("student");
                        student.setPasswordSet(1);
                        student.setCreateTime(LocalDateTime.now());
                        student.setUpdateTime(LocalDateTime.now());
                        
                        students.add(student);
                        successCount++;
                        
                        // 解析课表信息（避免重复解析相同的课表）
                        if (!schedule.isEmpty() && !processedSchedules.contains(schedule)) {
                            try {
                                // 从班级名称推断课程名称
                                String actualCourseName = className.contains("工程实践") ? "工程实践B" : className;
                                
                                List<Course> parsedCourses = scheduleParserService.parseSchedule(
                                    schedule, actualCourseName, teacherCode, className
                                );
                                
                                // 检查课程是否已存在，避免重复插入
                                for (Course course : parsedCourses) {
                                    QueryWrapper<Course> courseQuery = new QueryWrapper<>();
                                    courseQuery.eq("course_id", course.getCourseId());
                                    Course existingCourse = courseMapper.selectOne(courseQuery);
                                    
                                    if (existingCourse == null) {
                                        courses.add(course);
                                    }
                                }
                                
                                processedSchedules.add(schedule);
                            } catch (Exception e) {
                                System.err.println("解析课表失败: " + e.getMessage());
                            }
                        }
                    } else {
                        errorCount++;
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            // 批量插入学生
            if (!students.isEmpty()) {
                for (User student : students) {
                    userMapper.insert(student);
                }
            }
            
            // 批量插入课程
            if (!courses.isEmpty()) {
                for (Course course : courses) {
                    courseMapper.insert(course);
                }
            }
            
            workbook.close();
            String result = String.format("导入完成！学生：%d条，自动生成课程：%d条，失败：%d条", 
                successCount, courses.size(), errorCount);
            return result;
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 老师导入课程数据
     */
    public String importCoursesForTeacher(MultipartFile file, String teacherCode) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Course> courses = new ArrayList<>();
            int successCount = 0;
            int errorCount = 0;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    String courseName = getCellValue(row.getCell(0));
                    String classCode = getCellValue(row.getCell(1));
                    String location = getCellValue(row.getCell(2));
                    String courseDate = getCellValue(row.getCell(3));
                    String timeSlot = getCellValue(row.getCell(4));
                    
                    if (courseName.isEmpty() || classCode.isEmpty() || courseDate.isEmpty() || timeSlot.isEmpty()) {
                        errorCount++;
                        continue;
                    }
                    
                    // 生成课程ID
                    String courseId = generateCourseId();
                    
                    Course course = new Course();
                    course.setCourseId(courseId);
                    course.setCourseName(courseName);
                    course.setTeacherUsername(teacherCode);
                    course.setClassCode(classCode);
                    course.setLocation(location);
                    course.setCourseDate(courseDate);
                    course.setTimeSlot(timeSlot);
                    course.setCreateTime(LocalDateTime.now());
                    course.setUpdateTime(LocalDateTime.now());
                    
                    courses.add(course);
                    successCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                }
            }
            
            // 批量插入课程
            if (!courses.isEmpty()) {
                for (Course course : courses) {
                    courseMapper.insert(course);
                }
            }
            
            workbook.close();
            return String.format("导入完成！成功：%d条，失败：%d条", successCount, errorCount);
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 生成课程ID
     */
    private String generateCourseId() {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        return String.format("KC%s%06d", year, randomNum);
    }
    
    /**
     * 获取字符串后四位
     */
    private String RIGHT(String str, int length) {
        if (str == null || str.length() < length) {
            return str;
        }
        return str.substring(str.length() - length);
    }
}

