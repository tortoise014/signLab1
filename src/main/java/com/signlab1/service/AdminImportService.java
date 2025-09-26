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
import java.time.LocalDateTime;
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
                    
                    // 根据班级名称查找班级编号，作为课程代码
                    String className = getCellValue(row.getCell(0)); // 班级名称
                    QueryWrapper<Class> classQuery = new QueryWrapper<>();
                    classQuery.eq("class_name", className);
                    Class clazz = classMapper.selectOne(classQuery);
                    
                    if (clazz != null) {
                        course.setClassCode(clazz.getClassCode()); // 使用班级编号作为课程代码
                    } else {
                        // 如果班级不存在，创建新班级
                        Class newClass = new Class();
                        newClass.setClassName(className);
                        newClass.setClassCode(generateClassCode());
                        newClass.setVerificationCode(generateVerificationCode());
                        newClass.setStudentCount(0);
                        newClass.setCreateTime(LocalDateTime.now());
                        newClass.setUpdateTime(LocalDateTime.now());
                        newClass.setIsDeleted(0);
                        
                        classMapper.insert(newClass);
                        course.setClassCode(newClass.getClassCode());
                        System.out.println("创建新班级: " + className + ", 班级编号: " + newClass.getClassCode());
                    }
                    
                    course.setCourseName(getCellValue(row.getCell(1))); // 课程名称
                    String teacherUsername = getCellValue(row.getCell(2)); // 教师工号
                    String teacherName = getCellValue(row.getCell(3)); // 任课教师姓名
                    course.setTeacherUsername(teacherUsername);
                    course.setCourseDate(getCellValue(row.getCell(4))); // 上课日期
                    course.setTimeSlot(getCellValue(row.getCell(5))); // 时间段
                    course.setLocation(getCellValue(row.getCell(6))); // 上课地点
                    
                    // 检查并创建教师用户
                    if (!teacherUsername.isEmpty()) {
                        QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                        teacherQuery.eq("username", teacherUsername);
                        User existingTeacher = userMapper.selectOne(teacherQuery);
                        
                        if (existingTeacher == null) {
                            // 创建新教师用户
                            User newTeacher = new User();
                            newTeacher.setUsername(teacherUsername);
                            newTeacher.setName(teacherName.isEmpty() ? "教师" + teacherUsername : teacherName);
                            newTeacher.setRole("teacher");
                            
                            // 设置密码为后四位
                            String password = teacherUsername.length() >= 4 ? 
                                teacherUsername.substring(teacherUsername.length() - 4) : teacherUsername;
                            newTeacher.setPassword(password);
                            newTeacher.setPasswordSet(1); // 已设置密码
                            
                            newTeacher.setCreateTime(LocalDateTime.now());
                            newTeacher.setUpdateTime(LocalDateTime.now());
                            newTeacher.setIsDeleted(0);
                            
                            userMapper.insert(newTeacher);
                            System.out.println("创建新教师用户: " + teacherUsername + " - " + newTeacher.getName() + " - 密码: " + password);
                        }
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
                    String courseName = getCellValue(row.getCell(0));
                    String studentCode = getCellValue(row.getCell(1));
                    String studentName = getCellValue(row.getCell(2));
                    String department = getCellValue(row.getCell(3));
                    String major = getCellValue(row.getCell(4));
                    String teachers = getCellValue(row.getCell(5));
                    String schedule = getCellValue(row.getCell(6));
                    
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
                        student.setPassword(studentCode.length() >= 4 ? 
                            studentCode.substring(studentCode.length() - 4) : "1234");
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
                    
                    // 解析课表信息
                    if (!schedule.isEmpty() && !processedSchedules.contains(schedule)) {
                        try {
                            List<Course> parsedSchedules = scheduleParserService.parseSchedule(
                                schedule, courseName, teacherCode, course.getClassCode()
                            );
                            
                            for (Course courseSchedule : parsedSchedules) {
                                QueryWrapper<Course> scheduleQuery = new QueryWrapper<>();
                                scheduleQuery.eq("course_id", courseSchedule.getCourseId());
                                Course existingSchedule = courseMapper.selectOne(scheduleQuery);
                                
                                if (existingSchedule == null) {
                                    courseSchedules.add(courseSchedule);
                                }
                            }
                            
                            processedSchedules.add(schedule);
                        } catch (Exception e) {
                            System.err.println("解析课表失败: " + e.getMessage());
                        }
                    }
                    
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
            
            // 批量插入课程时间安排
            if (!courseSchedules.isEmpty()) {
                for (Course courseSchedule : courseSchedules) {
                    courseMapper.insert(courseSchedule);
                }
            }
            
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
            result.append("• 创建课程：").append(courseCount).append("门\n");
            result.append("• 生成课程安排：").append(courseSchedules.size()).append("条\n");
            result.append("• 其他错误：").append(errorCount).append("条");
            
            return result.toString();
            
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
                    String className = getCellValue(row.getCell(0)); // 班级名称
                    String courseName = getCellValue(row.getCell(1));
                    String teacherEmployeeId = getCellValue(row.getCell(2));
                    String teacherName = getCellValue(row.getCell(3)); // 任课教师姓名
                    String courseDate = getCellValue(row.getCell(4));
                    String timeSlot = getCellValue(row.getCell(5));
                    String location = getCellValue(row.getCell(6));
                    
                    if (courseName.isEmpty() || className.isEmpty() || courseDate.isEmpty() || timeSlot.isEmpty()) {
                        errorCount++;
                        continue;
                    }
                    
                    // 根据班级名称查找班级编号
                    QueryWrapper<Class> classQuery = new QueryWrapper<>();
                    classQuery.eq("class_name", className);
                    Class clazz = classMapper.selectOne(classQuery);
                    
                    String classCode;
                    if (clazz != null) {
                        classCode = clazz.getClassCode(); // 使用现有班级编号
                    } else {
                        // 如果班级不存在，创建新班级
                        Class newClass = new Class();
                        newClass.setClassName(className);
                        newClass.setClassCode(generateClassCode());
                        newClass.setVerificationCode(generateVerificationCode());
                        newClass.setStudentCount(0);
                        newClass.setCreateTime(LocalDateTime.now());
                        newClass.setUpdateTime(LocalDateTime.now());
                        newClass.setIsDeleted(0);
                        
                        classMapper.insert(newClass);
                        classCode = newClass.getClassCode();
                        System.out.println("创建新班级: " + className + ", 班级编号: " + classCode);
                    }
                    
                    // 检查并创建教师用户
                    if (!teacherEmployeeId.isEmpty()) {
                        QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                        teacherQuery.eq("username", teacherEmployeeId);
                        User existingTeacher = userMapper.selectOne(teacherQuery);
                        
                        if (existingTeacher == null) {
                            // 创建新教师用户
                            User newTeacher = new User();
                            newTeacher.setUsername(teacherEmployeeId);
                            newTeacher.setName(teacherName.isEmpty() ? "教师" + teacherEmployeeId : teacherName);
                            newTeacher.setRole("teacher");
                            
                            // 设置密码为后四位
                            String password = teacherEmployeeId.length() >= 4 ? 
                                teacherEmployeeId.substring(teacherEmployeeId.length() - 4) : teacherEmployeeId;
                            newTeacher.setPassword(password);
                            newTeacher.setPasswordSet(1); // 已设置密码
                            
                            newTeacher.setCreateTime(LocalDateTime.now());
                            newTeacher.setUpdateTime(LocalDateTime.now());
                            newTeacher.setIsDeleted(0);
                            
                            userMapper.insert(newTeacher);
                            System.out.println("创建新教师用户: " + teacherEmployeeId + " - " + newTeacher.getName() + " - 密码: " + password);
                        }
                    }
                    
                    // 生成课程ID
                    String courseId = generateCourseId();
                    
                    Course course = new Course();
                    course.setCourseId(courseId);
                    course.setCourseName(courseName);
                    course.setTeacherUsername(teacherEmployeeId);
                    course.setClassCode(classCode); // 存储班级编号，不是班级名称
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
     * 获取字符串后四位
     */
    private String RIGHT(String str, int length) {
        if (str == null || str.length() < length) {
            return str;
        }
        return str.substring(str.length() - length);
    }
}

