package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.User;
import com.signlab1.entity.StudentClassRelation;
import com.signlab1.mapper.ClassMapper;
import com.signlab1.mapper.CourseMapper;
import com.signlab1.mapper.UserMapper;
import com.signlab1.mapper.StudentClassRelationMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final StudentClassRelationMapper studentClassRelationMapper;
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
                    
                    // 直接按列索引读取，避免变量名混淆
                    String col0 = getCellValue(row.getCell(0)); // 班级
                    String col1 = getCellValue(row.getCell(1)); // 人数
                    String col2 = getCellValue(row.getCell(2)); // 课程
                    String col3 = getCellValue(row.getCell(3)); // 实验
                    String col4 = getCellValue(row.getCell(4)); // 工号
                    String col5 = getCellValue(row.getCell(5)); // 任课教师
                    String col6 = getCellValue(row.getCell(6)); // 上课时间
                    String col7 = getCellValue(row.getCell(7)); // 上课地点
                    
                    // 跳过空行或说明行
                    if (col0.isEmpty() || col0.contains("说明") || col0.contains("1.") || col0.contains("2.")) {
                        continue;
                    }
                    
                    // 验证工号格式（8位数字）
                    if (col4 == null || col4.trim().isEmpty() || !col4.trim().matches("\\d{8}")) {
                        System.err.println("工号格式错误，必须是8位数字: '" + col4 + "'");
                        errorCount++;
                        continue;
                    }
                    
                    // 查找或创建班级
                    QueryWrapper<Class> classQuery = new QueryWrapper<>();
                    classQuery.eq("class_name", col0);
                    Class clazz = classMapper.selectOne(classQuery);
                    
                    if (clazz != null) {
                        course.setClassCode(clazz.getClassCode());
                    } else {
                        // 创建新班级
                        Class newClass = new Class();
                        newClass.setClassName(col0);
                        newClass.setClassCode(generateClassCode());
                        newClass.setVerificationCode(generateVerificationCode());
                        newClass.setStudentCount(col1.isEmpty() ? 0 : Integer.parseInt(col1));
                        newClass.setCreateTime(LocalDateTime.now());
                        newClass.setUpdateTime(LocalDateTime.now());
                        newClass.setIsDeleted(0);
                        
                        classMapper.insert(newClass);
                        course.setClassCode(newClass.getClassCode());
                    }
                    
                    // 设置课程信息
                    course.setCourseName(col3); // 使用实验列作为课程名称
                    course.setTeacherUsername(col4.trim());
                    
                    // 解析日期
                    String[] dateTimeParts = col6.split("(上午|下午)");
                    if (dateTimeParts.length >= 1) {
                        String datePart = dateTimeParts[0].trim();
                        String parsedDate = parseDateString(datePart);
                        if (parsedDate != null) {
                            course.setCourseDate(parsedDate);
                        } else {
                            System.err.println("日期解析失败: " + datePart);
                            errorCount++;
                            continue;
                        }
                    } else {
                        System.err.println("无法解析时间格式: " + col6);
                        errorCount++;
                        continue;
                    }
                    
                    // 解析时间段
                    String parsedTimeSlot = parseTimeSlot(col6);
                    course.setTimeSlot(parsedTimeSlot);
                    course.setLocation(col7);
                    
                    // 创建教师用户
                    QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("username", col4.trim());
                    User existingTeacher = userMapper.selectOne(teacherQuery);
                    
                    if (existingTeacher == null) {
                        User newTeacher = new User();
                        newTeacher.setUsername(col4.trim());
                        newTeacher.setName(col5.isEmpty() ? "教师" + col4.trim() : col5);
                        newTeacher.setRole("teacher");
                        
                        String password = "syjx@" + (col4.trim().length() >= 4 ? 
                            col4.trim().substring(col4.trim().length() - 4) : col4.trim());
                        newTeacher.setPassword(password);
                        newTeacher.setPasswordSet(1);
                        
                        newTeacher.setCreateTime(LocalDateTime.now());
                        newTeacher.setUpdateTime(LocalDateTime.now());
                        newTeacher.setIsDeleted(0);
                        
                        userMapper.insert(newTeacher);
                    }
                    
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
     * 老师导入学生数据（自动生成课程并创建学生课程关联）
     */
    public String importStudentsForTeacher(MultipartFile file, String teacherCode) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<User> newStudents = new ArrayList<>();
            List<StudentClassRelation> studentCourseRelations = new ArrayList<>();
            List<Course> courseSchedules = new ArrayList<>();
            Map<String, Class> processedCourses = new HashMap<>();
            Set<String> processedSchedules = new HashSet<>();
            
            int newStudentCount = 0;
            int existingStudentCount = 0;
            int bindSuccessCount = 0;
            int bindFailCount = 0;
            int errorCount = 0;
            int courseCount = 0;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 只读取学生基本信息，忽略老师信息和上课时间
                    String courseName = getCellValue(row.getCell(0));
                    String studentCode = getCellValue(row.getCell(1));
                    String studentName = getCellValue(row.getCell(2));
                    String department = getCellValue(row.getCell(3));
                    String major = getCellValue(row.getCell(4));
                    
                    if (studentCode.isEmpty() || studentName.isEmpty() || courseName.isEmpty()) {
                        errorCount++;
                        continue;
                    }
                    
                    // 检查学生是否已存在
                    QueryWrapper<User> userQuery = new QueryWrapper<>();
                    userQuery.eq("username", studentCode);
                    User existingUser = userMapper.selectOne(userQuery);
                    
                    // 处理课程信息
                    Class course = processedCourses.get(courseName);
                    if (course == null) {
                        QueryWrapper<Class> courseQuery = new QueryWrapper<>();
                        courseQuery.eq("class_name", courseName);
                        course = classMapper.selectOne(courseQuery);
                        
                        if (course == null) {
                            course = new Class();
                            course.setClassName(courseName);
                            course.setClassCode(generateClassCode());
                            course.setVerificationCode(generateVerificationCode());
                            course.setStudentCount(0);
                            course.setCreateTime(LocalDateTime.now());
                            course.setUpdateTime(LocalDateTime.now());
                            
                            classMapper.insert(course);
                            courseCount++;
                            System.out.println("创建新班级: " + courseName + ", 班级编号: " + course.getClassCode());
                        }
                        
                        processedCourses.put(courseName, course);
                    }
                    
                    // 检查学生课程关联是否已存在
                    QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
                    relationQuery.eq("student_username", studentCode)
                               .eq("class_code", course.getClassCode());
                    StudentClassRelation existingRelation = studentClassRelationMapper.selectOne(relationQuery);
                    
                    if (existingUser == null) {
                        // 学生不存在，创建新学生
                        User student = new User();
                        student.setUsername(studentCode);
                        student.setName(studentName);
                        // 设置密码为 syjx@ + 学号后四位
                        student.setPassword("syjx@" + (studentCode.length() >= 4 ? 
                            studentCode.substring(studentCode.length() - 4) : studentCode));
                        student.setRole("student");
                        student.setPasswordSet(1);
                        student.setCreateTime(LocalDateTime.now());
                        student.setUpdateTime(LocalDateTime.now());
                        
                        newStudents.add(student);
                        newStudentCount++;
                        
                        // 创建学生课程关联
                        if (existingRelation == null) {
                            StudentClassRelation relation = new StudentClassRelation();
                            relation.setStudentUsername(studentCode);
                            relation.setClassCode(course.getClassCode());
                            relation.setBindTime(LocalDateTime.now());
                            studentCourseRelations.add(relation);
                            bindSuccessCount++;
                        } else {
                            bindFailCount++;
                        }
                        
                    } else {
                        // 学生已存在
                        existingStudentCount++;
                        
                        if (existingRelation == null) {
                            // 创建学生课程关联
                            StudentClassRelation relation = new StudentClassRelation();
                            relation.setStudentUsername(studentCode);
                            relation.setClassCode(course.getClassCode());
                            relation.setBindTime(LocalDateTime.now());
                            studentCourseRelations.add(relation);
                            bindSuccessCount++;
                        } else {
                            bindFailCount++;
                        }
                    }
                    
                    // 跳过课表解析，专注于学生信息导入
                    
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("处理第" + i + "行数据失败: " + e.getMessage());
                }
            }
            
            // 批量插入新学生
            if (!newStudents.isEmpty()) {
                for (User student : newStudents) {
                    userMapper.insert(student);
                }
            }
            
            // 跳过课程时间安排插入，专注于学生信息
            
            // 批量插入学生课程关联
            if (!studentCourseRelations.isEmpty()) {
                for (StudentClassRelation relation : studentCourseRelations) {
                    studentClassRelationMapper.insert(relation);
                }
            }
            
            // 更新课程选课人数统计
            for (Class course : processedCourses.values()) {
                QueryWrapper<StudentClassRelation> countQuery = new QueryWrapper<>();
                countQuery.eq("class_code", course.getClassCode());
                long studentCount = studentClassRelationMapper.selectCount(countQuery);
                
                course.setStudentCount((int) studentCount);
                classMapper.updateById(course);
            }
            
            workbook.close();
            
            // 构建详细的返回结果
            StringBuilder result = new StringBuilder();
            result.append("导入完成！\n");
            result.append("📊 统计信息：\n");
            result.append("• 新创建学生：").append(newStudentCount).append("人\n");
            result.append("• 已存在学生：").append(existingStudentCount).append("人\n");
            result.append("• 成功选课：").append(bindSuccessCount).append("人\n");
            result.append("• 选课失败（已选课）：").append(bindFailCount).append("人\n");
            result.append("• 创建班级：").append(courseCount).append("个\n");
            // 跳过课程安排统计
            result.append("• 其他错误：").append(errorCount).append("条");
            
            return result.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 老师导入课程数据 - 重新实现
     * 支持字段：班级 | 人数 | 课程 | 实验 | 工号 | 任课教师 | 上课时间 | 上课地点
     */
    public String importCoursesForTeacher(MultipartFile file, String teacherCode) {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<Course> courses = new ArrayList<>();
            Map<String, Class> createdClasses = new HashMap<>();
            Map<String, User> createdTeachers = new HashMap<>();
            
            int successCount = 0;
            int errorCount = 0;
            int newClassCount = 0;
            int newTeacherCount = 0;
            
            // 跳过标题行，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 读取Excel数据 - 严格按照模板字段顺序
                    String className = getCellValue(row.getCell(0)); // 班级
                    String studentCount = getCellValue(row.getCell(1)); // 人数
                    String courseName = getCellValue(row.getCell(2)); // 课程
                    String experimentName = getCellValue(row.getCell(3)); // 实验
                    String teacherEmployeeId = getCellValue(row.getCell(4)); // 工号
                    String teacherName = getCellValue(row.getCell(5)); // 任课教师
                    String courseDateTime = getCellValue(row.getCell(6)); // 上课时间
                    String location = getCellValue(row.getCell(7)); // 上课地点
                    
                    // 调试信息
                    System.out.println("第" + (i + 1) + "行数据: " + className + " | " + experimentName + " | " + courseDateTime);
                    
                    // 跳过空行
                    if (className.isEmpty()) {
                        continue;
                    }
                    
                    // 简单跳过明显的说明行
                    if (className.contains("说明") || className.contains("注意")) {
                        continue;
                    }
                    
                    // 验证必填字段
                    if (className.isEmpty() || experimentName.isEmpty() || courseDateTime.isEmpty()) {
                        System.err.println("第" + (i + 1) + "行数据不完整，跳过处理 - 班级:" + className + " 实验:" + experimentName + " 时间:" + courseDateTime);
                        errorCount++;
                        continue;
                    }
                    
                    // 处理工号（不限制格式，只要不为空即可）
                    if (teacherEmployeeId != null && !teacherEmployeeId.trim().isEmpty()) {
                        teacherEmployeeId = teacherEmployeeId.trim();
                    } else {
                        // 如果没有提供工号，使用当前教师工号
                        teacherEmployeeId = teacherCode;
                    }
                    
                    // 处理班级信息
                    Class clazz = getOrCreateClass(className, studentCount, createdClasses);
                    if (clazz == null) {
                        System.err.println("第" + (i + 1) + "行班级处理失败: " + className);
                        errorCount++;
                        continue;
                    }
                    
                    if (createdClasses.containsKey(className)) {
                        newClassCount++;
                    }
                    
                    // 处理教师信息
                    User teacher = getOrCreateTeacher(teacherEmployeeId, teacherName, createdTeachers);
                    if (teacher == null) {
                        System.err.println("第" + (i + 1) + "行教师处理失败: " + teacherEmployeeId);
                        errorCount++;
                        continue;
                    }
                    
                    if (createdTeachers.containsKey(teacherEmployeeId)) {
                        newTeacherCount++;
                    }
                    
                    // 解析日期和时间段
                    String parsedDate = parseDateString(courseDateTime);
                    if (parsedDate == null) {
                        System.err.println("第" + (i + 1) + "行日期解析失败: " + courseDateTime);
                        errorCount++;
                        continue;
                    }
                    
                    String parsedTimeSlot = parseTimeSlot(courseDateTime);
                    
                    // 创建课程对象
                    Course course = new Course();
                    course.setCourseId(generateCourseId());
                    course.setCourseName(experimentName); // 使用实验列作为课程名称
                    course.setTeacherUsername(teacherEmployeeId);
                    course.setClassCode(clazz.getClassCode());
                    course.setLocation(location);
                    course.setCourseDate(parsedDate);
                    course.setTimeSlot(parsedTimeSlot);
                    course.setCreateTime(LocalDateTime.now());
                    course.setUpdateTime(LocalDateTime.now());
                    course.setIsDeleted(0);
                    
                    courses.add(course);
                    successCount++;
                    
                } catch (Exception e) {
                    System.err.println("第" + (i + 1) + "行数据处理异常: " + e.getMessage());
                    errorCount++;
                }
            }
            
            // 批量插入新创建的班级
            for (Class clazz : createdClasses.values()) {
                classMapper.insert(clazz);
            }
            
            // 批量插入新创建的教师
            for (User teacher : createdTeachers.values()) {
                userMapper.insert(teacher);
            }
            
            // 批量插入课程
            for (Course course : courses) {
                courseMapper.insert(course);
            }
            
            workbook.close();
            
            // 构建详细的返回结果
            StringBuilder result = new StringBuilder();
            result.append("课程数据导入完成！\n");
            result.append("📊 统计信息：\n");
            result.append("• 成功导入课程：").append(successCount).append("条\n");
            result.append("• 创建新班级：").append(newClassCount).append("个\n");
            result.append("• 创建新教师：").append(newTeacherCount).append("人\n");
            result.append("• 处理失败：").append(errorCount).append("条");
            
            return result.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取或创建班级
     */
    private Class getOrCreateClass(String className, String studentCount, Map<String, Class> createdClasses) {
        try {
            // 先检查已创建的班级
            if (createdClasses.containsKey(className)) {
                return createdClasses.get(className);
            }
            
            // 检查数据库中是否已存在
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_name", className);
            Class existingClass = classMapper.selectOne(classQuery);
            
            if (existingClass != null) {
                // 更新现有班级的学生人数（如果Excel中有提供人数信息）
                if (!studentCount.isEmpty()) {
                    try {
                        int newStudentCount = Integer.parseInt(studentCount);
                        if (existingClass.getStudentCount() != newStudentCount) {
                            int oldCount = existingClass.getStudentCount();
                            existingClass.setStudentCount(newStudentCount);
                            existingClass.setUpdateTime(LocalDateTime.now());
                            classMapper.updateById(existingClass);
                            System.out.println("更新班级 " + className + " 的学生人数: " + oldCount + " -> " + newStudentCount);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("班级 " + className + " 的学生人数格式错误: " + studentCount);
                    }
                }
                return existingClass;
            }
            
            // 创建新班级
            Class newClass = new Class();
            newClass.setClassName(className);
            newClass.setClassCode(generateClassCode());
            newClass.setVerificationCode(generateVerificationCode());
            newClass.setStudentCount(studentCount.isEmpty() ? 0 : Integer.parseInt(studentCount));
            newClass.setCreateTime(LocalDateTime.now());
            newClass.setUpdateTime(LocalDateTime.now());
            newClass.setIsDeleted(0);
            
            // 标记为新创建的班级
            createdClasses.put(className, newClass);
            
            return newClass;
            
        } catch (Exception e) {
            System.err.println("创建班级失败: " + className + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取或创建教师
     */
    private User getOrCreateTeacher(String teacherEmployeeId, String teacherName, Map<String, User> createdTeachers) {
        try {
            // 先检查已创建的教师
            if (createdTeachers.containsKey(teacherEmployeeId)) {
                return createdTeachers.get(teacherEmployeeId);
            }
            
            // 检查数据库中是否已存在
            QueryWrapper<User> teacherQuery = new QueryWrapper<>();
            teacherQuery.eq("username", teacherEmployeeId);
            User existingTeacher = userMapper.selectOne(teacherQuery);
            
            if (existingTeacher != null) {
                return existingTeacher;
            }
            
            // 创建新教师
            User newTeacher = new User();
            newTeacher.setUsername(teacherEmployeeId);
            newTeacher.setName(teacherName.isEmpty() ? "教师" + teacherEmployeeId : teacherName);
            newTeacher.setRole("teacher");
            
            // 设置密码为 syjx@ + 工号后四位
            String password = "syjx@" + (teacherEmployeeId.length() >= 4 ? 
                teacherEmployeeId.substring(teacherEmployeeId.length() - 4) : teacherEmployeeId);
            newTeacher.setPassword(password);
            newTeacher.setPasswordSet(1);
            
            newTeacher.setCreateTime(LocalDateTime.now());
            newTeacher.setUpdateTime(LocalDateTime.now());
            newTeacher.setIsDeleted(0);
            
            // 标记为新创建的教师
            createdTeachers.put(teacherEmployeeId, newTeacher);
            
            return newTeacher;
            
        } catch (Exception e) {
            System.err.println("创建教师失败: " + teacherEmployeeId + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 判断是否为说明行
     */
    private boolean isDescriptionRow(String className, String experimentName, String courseDateTime) {
        // 如果班级名称是说明文字特征
        if (className.contains("说明") || className.contains("注意") || className.contains("格式")) {
            return true;
        }
        
        // 如果班级名称是纯数字序号（如"1."、"2."等），且其他关键字段为空或也是说明文字
        if (className.matches("\\d+\\..*")) {
            // 检查其他关键字段是否也是说明文字
            if (experimentName.isEmpty() || courseDateTime.isEmpty() || 
                experimentName.contains("说明") || courseDateTime.contains("说明")) {
                return true;
            }
        }
        
        // 如果关键字段（实验名称、上课时间）都为空，可能是说明行
        if (experimentName.isEmpty() && courseDateTime.isEmpty()) {
            return true;
        }
        
        return false;
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
     * 生成班级编号
     */
    private String generateClassCode() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestamp);
        String last6Digits = timestampStr.substring(timestampStr.length() - 6);
        
        Random random = new Random();
        int randomNum = random.nextInt(100);
        
        return "CL" + last6Digits + String.format("%02d", randomNum);
    }
    
    /**
     * 解析日期字符串为标准的 yyyy-MM-dd 格式，强制年份为2025
     */
    private String parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim();
        
        // 检查是否为纯数字（可能是工号），如果是则直接返回null
        if (dateStr.matches("\\d+")) {
            System.err.println("检测到纯数字，可能是工号而非日期: " + dateStr);
            return null;
        }
        
        // 检查是否为8位数字（工号格式），如果是则直接返回null
        if (dateStr.matches("\\d{8}")) {
            System.err.println("检测到8位数字，可能是工号而非日期: " + dateStr);
            return null;
        }
        
        // 检查是否包含中文日期标识符
        if (!dateStr.contains("月") && !dateStr.contains("日") && 
            !dateStr.contains("/") && !dateStr.contains("-") && 
            !dateStr.contains("年")) {
            System.err.println("不包含日期标识符，可能不是日期: " + dateStr);
            return null;
        }
        
        // 特殊处理中文日期格式，如 "9月30日上午"、"10月14日上午"
        if (dateStr.contains("月") && dateStr.contains("日")) {
            try {
                // 提取日期部分，去掉"上午"、"下午"等时间标识
                String datePart = dateStr.replaceAll("[上下]午.*", "").trim();
                System.out.println("原始日期: " + dateStr + " -> 提取后: " + datePart);
                
                // 特殊处理：如果日期是"9月30日"这样的格式，直接解析
                if (datePart.matches("\\d+月\\d+日")) {
                    try {
                        // 使用更宽松的解析方式
                        String[] parts = datePart.split("月|日");
                        if (parts.length >= 2) {
                            int month = Integer.parseInt(parts[0]);
                            int day = Integer.parseInt(parts[1]);
                            LocalDate date = LocalDate.of(2025, month, day);
                            String result = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            System.out.println("直接解析成功: " + datePart + " -> " + result);
                            return result;
                        }
                    } catch (Exception e) {
                        System.out.println("直接解析失败: " + e.getMessage());
                    }
                }
                
                // 定义中文日期格式（支持一位和两位月份/日期）
                String[] chinesePatterns = {
                    "M月d日",      // 9月3日
                    "M月dd日",     // 9月30日
                    "MM月d日",     // 09月3日  
                    "MM月dd日"     // 09月30日
                };
                
                for (String pattern : chinesePatterns) {
                    try {
                        System.out.println("尝试格式: " + pattern + " 解析: " + datePart);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                        LocalDate date = LocalDate.parse(datePart, formatter);
                        // 强制设置为2025年
                        LocalDate date2025 = date.withYear(2025);
                        String result = date2025.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        System.out.println("解析成功: " + datePart + " -> " + result);
                        return result;
                    } catch (DateTimeParseException e) {
                        System.out.println("格式 " + pattern + " 解析失败: " + e.getMessage());
                        // 继续尝试下一个格式
                    }
                }
            } catch (Exception e) {
                System.err.println("解析中文日期格式失败: " + dateStr + ", 错误: " + e.getMessage());
            }
        }
        
        // 定义其他可能的日期格式
        String[] patterns = {
            "M/d/yy",      // 9/28/24
            "M/d/yyyy",    // 9/28/2024
            "MM/dd/yy",    // 09/28/24
            "MM/dd/yyyy",  // 09/28/2024
            "yyyy-MM-dd",  // 2024-09-28
            "yyyy/MM/dd",  // 2024/09/28
            "M-d-yy",      // 9-28-24
            "M-d-yyyy",    // 9-28-2024
            "MM-dd-yy",    // 09-28-24
            "MM-dd-yyyy"   // 09-28-2024
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate date = LocalDate.parse(dateStr, formatter);
                // 强制设置为2025年
                LocalDate date2025 = date.withYear(2025);
                return date2025.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，返回null
        System.err.println("无法解析日期格式: " + dateStr);
        return null;
    }
    
    /**
     * 解析时间段字符串，将"上午"和"下午"转换为具体时间
     * 上午：08:30-12:00
     * 下午：14:40-18:05
     */
    private String parseTimeSlot(String timeSlotStr) {
        if (timeSlotStr == null || timeSlotStr.trim().isEmpty()) {
            return "08:30-12:00"; // 默认上午时间
        }
        
        timeSlotStr = timeSlotStr.trim();
        
        if (timeSlotStr.contains("上午")) {
            return "08:30-12:00";
        } else if (timeSlotStr.contains("下午")) {
            return "14:40-18:05";
        } else {
            // 如果已经是具体时间格式，直接返回
            return timeSlotStr;
        }
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

