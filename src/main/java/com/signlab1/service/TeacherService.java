package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.*;
import com.signlab1.entity.AttendanceRecord;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.MultiClassCourse;
import com.signlab1.entity.StudentClassRelation;
import com.signlab1.entity.StudentDocument;
import com.signlab1.entity.User;
import com.signlab1.mapper.*;
import com.signlab1.util.QrCodeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 老师端课程管理服务
 */
@Service
public class TeacherService {
    
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StudentDocumentMapper studentDocumentMapper;
    private final StudentClassRelationMapper studentClassRelationMapper;
    private final UserMapper userMapper;
    private final MultiClassCourseMapper multiClassCourseMapper;
    private final QrCodeUtil qrCodeUtil;
    
    public TeacherService(CourseMapper courseMapper, ClassMapper classMapper, 
                         AttendanceRecordMapper attendanceRecordMapper, 
                         StudentDocumentMapper studentDocumentMapper,
                         StudentClassRelationMapper studentClassRelationMapper,
                         UserMapper userMapper,
                         MultiClassCourseMapper multiClassCourseMapper,
                         QrCodeUtil qrCodeUtil) {
        this.courseMapper = courseMapper;
        this.classMapper = classMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentDocumentMapper = studentDocumentMapper;
        this.studentClassRelationMapper = studentClassRelationMapper;
        this.userMapper = userMapper;
        this.multiClassCourseMapper = multiClassCourseMapper;
        this.qrCodeUtil = qrCodeUtil;
    }
    
    /**
     * 获取老师今日课程
     */
    public List<CourseInfoDto> getTodayCourses(String teacherUsername) {
        try {
            // 先验证教师用户是否存在
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.eq("username", teacherUsername);
            userQuery.eq("role", "teacher");
            User teacher = userMapper.selectOne(userQuery);
            
            if (teacher == null) {
                throw new RuntimeException("权限不足，无法访问该资源");
            }
            
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return getCoursesByDate(teacherUsername, today);
        } catch (Exception e) {
            System.err.println("获取今日课程失败: " + e.getMessage());
            throw e; // 重新抛出异常，让Controller处理
        }
    }
    
    /**
     * 获取老师所有课程
     */
    public List<CourseInfoDto> getAllCourses(String teacherCode) {
        try {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teacher_username", teacherCode);
            queryWrapper.orderByAsc("course_date").orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(queryWrapper);
            List<CourseInfoDto> result = new ArrayList<>();
            
            for (Course course : courses) {
                try {
                    CourseInfoDto dto = new CourseInfoDto();
                    dto.setCourseId(course.getCourseId());
                    dto.setCourseName(course.getCourseName());
                    dto.setTimeSlot(course.getTimeSlot());
                    dto.setLocation(course.getLocation());
                    dto.setCourseDate(course.getCourseDate());
                    
                    // 获取班级名称
                    try {
                        QueryWrapper<Class> classQuery = new QueryWrapper<>();
                        classQuery.eq("class_code", course.getClassCode());
                        Class clazz = classMapper.selectOne(classQuery);
                        dto.setClassName(clazz != null ? clazz.getClassName() : course.getClassCode());
                    } catch (Exception e) {
                        dto.setClassName(course.getClassCode());
                    }
                    
                    // 移除时间限制，允许所有课程都可以发起签到
                    dto.setCanStartAttendance(true);
                    
                    // 获取学生文档数量
                    try {
                        QueryWrapper<StudentDocument> docQuery = new QueryWrapper<>();
                        docQuery.eq("course_id", course.getCourseId());
                        int docCount = studentDocumentMapper.selectCount(docQuery).intValue();
                        dto.setDocumentCount(docCount);
                        dto.setCanViewDocuments(docCount > 0);
                    } catch (Exception e) {
                        dto.setDocumentCount(0);
                        dto.setCanViewDocuments(false);
                    }
                    
                    result.add(dto);
                } catch (Exception e) {
                    System.err.println("处理课程 " + course.getCourseId() + " 时出错: " + e.getMessage());
                    // 继续处理下一个课程
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("查询所有课程失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 根据日期获取老师课程
     */
    public List<CourseInfoDto> getCoursesByDate(String teacherCode, String date) {
        try {
            QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teacher_username", teacherCode);
            queryWrapper.eq("course_date", date);
            queryWrapper.orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(queryWrapper);
            List<CourseInfoDto> result = new ArrayList<>();
            
            for (Course course : courses) {
                try {
                    CourseInfoDto dto = new CourseInfoDto();
                    dto.setCourseId(course.getCourseId());
                    dto.setCourseName(course.getCourseName());
                    dto.setTimeSlot(course.getTimeSlot());
                    dto.setLocation(course.getLocation());
                    dto.setCourseDate(course.getCourseDate());
                    
                    // 获取班级名称
                    try {
                        QueryWrapper<Class> classQuery = new QueryWrapper<>();
                        classQuery.eq("class_code", course.getClassCode());
                        Class clazz = classMapper.selectOne(classQuery);
                        dto.setClassName(clazz != null ? clazz.getClassName() : course.getClassCode());
                    } catch (Exception e) {
                        dto.setClassName(course.getClassCode());
                    }
                    
                    // 移除时间限制，允许所有课程都可以发起签到
                    dto.setCanStartAttendance(true);
                    
                    // 获取学生文档数量
                    try {
                        QueryWrapper<StudentDocument> docQuery = new QueryWrapper<>();
                        docQuery.eq("course_id", course.getCourseId());
                        int docCount = studentDocumentMapper.selectCount(docQuery).intValue();
                        dto.setDocumentCount(docCount);
                        dto.setCanViewDocuments(docCount > 0);
                    } catch (Exception e) {
                        dto.setDocumentCount(0);
                        dto.setCanViewDocuments(false);
                    }
                    
                    result.add(dto);
                } catch (Exception e) {
                    System.err.println("处理课程 " + course.getCourseId() + " 时出错: " + e.getMessage());
                    // 继续处理下一个课程
                }
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("查询课程失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 生成签到二维码
     */
    public AttendanceQrDto generateAttendanceQr(String courseId) {
        // 获取课程信息
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        Course course = courseMapper.selectOne(queryWrapper);
        
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 移除时间限制，允许在上课前就生成二维码
        // if (!isInCourseTime(course.getTimeSlot())) {
        //     throw new RuntimeException("未到上课时间，无法发起签到");
        // }
        
        // 生成二维码内容
        long timestamp = System.currentTimeMillis() / 1000; // 秒级时间戳
        String qrContent = qrCodeUtil.generateAttendanceQrContent(
            courseId, course.getTeacherUsername(), course.getClassCode(), timestamp);
        
        // 生成二维码URL（用于微信扫码跳转）
        String qrUrl = qrCodeUtil.generateAttendanceQrUrl(
            courseId, course.getTeacherUsername(), course.getClassCode(), timestamp);
        
        // 生成二维码图片（使用URL格式）
        String qrImage = qrCodeUtil.generateQrCodeBase64(qrUrl, 300, 300);
        
        AttendanceQrDto dto = new AttendanceQrDto();
        dto.setQrContent(qrContent);
        dto.setQrImage(qrImage);
        dto.setCourseId(courseId);
        dto.setTimestamp(timestamp);
        dto.setRemainingTime(qrCodeUtil.getRemainingTime(timestamp, 30)); // 动态计算剩余时间
        
        return dto;
    }
    
    /**
     * 生成通用签到二维码（支持跨班签到）
     */
    public AttendanceQrDto generateUniversalAttendanceQr(String courseId) {
        // 获取课程信息
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        Course course = courseMapper.selectOne(queryWrapper);
        
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 查找同一老师同一时间的其他课程
        QueryWrapper<Course> sameTimeQuery = new QueryWrapper<>();
        sameTimeQuery.eq("teacher_username", course.getTeacherUsername())
                    .eq("course_date", course.getCourseDate())
                    .eq("time_slot", course.getTimeSlot());
        List<Course> sameTimeCourses = courseMapper.selectList(sameTimeQuery);
        
        // 生成通用二维码内容（使用老师信息，不限制班级）
        long timestamp = System.currentTimeMillis() / 1000; // 秒级时间戳
        String qrContent = qrCodeUtil.generateMultiClassAttendanceQrContent(
            courseId, course.getTeacherUsername(), timestamp);
        
        // 生成通用二维码URL（用于微信扫码跳转）
        String qrUrl = qrCodeUtil.generateMultiClassAttendanceQrUrl(
            courseId, course.getTeacherUsername(), timestamp);
        
        // 生成二维码图片（使用URL格式）
        String qrImage = qrCodeUtil.generateQrCodeBase64(qrUrl, 300, 300);
        
        AttendanceQrDto dto = new AttendanceQrDto();
        dto.setQrContent(qrContent);
        dto.setQrImage(qrImage);
        dto.setCourseId(courseId);
        dto.setTimestamp(timestamp);
        dto.setRemainingTime(qrCodeUtil.getRemainingTime(timestamp, 30)); // 动态计算剩余时间
        
        return dto;
    }
    
    /**
     * 配置多班级课程
     */
    public void configureMultiClassCourse(MultiClassCourseRequest request) {
        try {
            // 验证课程是否存在
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", request.getCourseId());
            Course course = courseMapper.selectOne(courseQuery);
            
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 验证教师权限
            if (!course.getTeacherUsername().equals(request.getTeacherUsername())) {
                throw new RuntimeException("无权限配置该课程");
            }
            
            // 先删除现有的多班级配置
            QueryWrapper<MultiClassCourse> deleteQuery = new QueryWrapper<>();
            deleteQuery.eq("course_id", request.getCourseId());
            multiClassCourseMapper.delete(deleteQuery);
            
            // 添加新的多班级配置
            for (String classCode : request.getClassCodes()) {
                // 验证班级是否存在
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", classCode);
                Class classEntity = classMapper.selectOne(classQuery);
                
                if (classEntity == null) {
                    throw new RuntimeException("班级不存在: " + classCode);
                }
                
                // 创建多班级课程关联
                MultiClassCourse multiClassCourse = new MultiClassCourse();
                multiClassCourse.setCourseId(request.getCourseId());
                multiClassCourse.setClassCode(classCode);
                multiClassCourse.setTeacherUsername(request.getTeacherUsername());
                
                multiClassCourseMapper.insert(multiClassCourse);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("配置多班级课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取多班级课程信息
     */
    public MultiClassCourseInfoDto getMultiClassCourseInfo(String courseId) {
        try {
            // 获取课程基本信息
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            MultiClassCourseInfoDto dto = new MultiClassCourseInfoDto();
            dto.setCourseId(courseId);
            dto.setCourseName(course.getCourseName());
            
            // 获取教师信息
            QueryWrapper<User> teacherQuery = new QueryWrapper<>();
            teacherQuery.eq("username", course.getTeacherUsername());
            User teacher = userMapper.selectOne(teacherQuery);
            dto.setTeacherName(teacher != null ? teacher.getName() : "未知老师");
            
            // 获取多班级配置
            QueryWrapper<MultiClassCourse> multiClassQuery = new QueryWrapper<>();
            multiClassQuery.eq("course_id", courseId);
            List<MultiClassCourse> multiClassCourses = multiClassCourseMapper.selectList(multiClassQuery);
            
            dto.setIsMultiClass(!multiClassCourses.isEmpty());
            
            // 获取班级信息
            List<ClassInfoDto> classInfos = new ArrayList<>();
            for (MultiClassCourse multiClassCourse : multiClassCourses) {
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", multiClassCourse.getClassCode());
                Class classEntity = classMapper.selectOne(classQuery);
                
                if (classEntity != null) {
                    ClassInfoDto classInfo = new ClassInfoDto();
                    classInfo.setClassCode(classEntity.getClassCode());
                    classInfo.setClassName(classEntity.getClassName());
                    classInfos.add(classInfo);
                }
            }
            
            dto.setClasses(classInfos);
            
            return dto;
            
        } catch (Exception e) {
            throw new RuntimeException("获取多班级课程信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除多班级课程配置
     */
    public void deleteMultiClassCourse(String courseId) {
        try {
            QueryWrapper<MultiClassCourse> query = new QueryWrapper<>();
            query.eq("course_id", courseId);
            multiClassCourseMapper.delete(query);
        } catch (Exception e) {
            throw new RuntimeException("删除多班级课程配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    public AttendanceStatsDto getAttendanceStats(String courseId) {
        // 获取课程信息
        QueryWrapper<Course> courseQuery = new QueryWrapper<>();
        courseQuery.eq("course_id", courseId);
        Course course = courseMapper.selectOne(courseQuery);
        
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 获取班级人数
        QueryWrapper<Class> classQuery = new QueryWrapper<>();
        classQuery.eq("class_code", course.getClassCode());
        Class clazz = classMapper.selectOne(classQuery);
        
        int totalCount = clazz != null ? clazz.getStudentCount() : 0;
        
        // 获取已签到人数
        QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
        attendanceQuery.eq("course_id", courseId);
        int attendedCount = attendanceRecordMapper.selectCount(attendanceQuery).intValue();
        
        double attendanceRate = totalCount > 0 ? (double) attendedCount / totalCount * 100 : 0;
        
        AttendanceStatsDto dto = new AttendanceStatsDto();
        dto.setTotalAttendance(attendedCount);
        dto.setAttendanceRate(attendanceRate);
        
        return dto;
    }
    
    /**
     * 获取未签到学生名单
     */
    public List<String> getAbsentStudents(String courseId) {
        // 这里需要根据实际需求实现
        // 暂时返回空列表，后续可以完善
        return new ArrayList<>();
    }
    
    /**
     * 查看课程签到情况
     */
    public List<StudentAttendanceDto> getCourseAttendance(String courseId) {
        try {
            // 1. 获取课程信息
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 2. 获取该班级的所有学生
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("class_code", course.getClassCode());
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            List<StudentAttendanceDto> result = new ArrayList<>();
            
            for (StudentClassRelation relation : relations) {
                StudentAttendanceDto dto = new StudentAttendanceDto();
                dto.setStudentCode(relation.getStudentUsername());
                
                // 获取学生姓名
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.eq("username", relation.getStudentUsername());
                User user = userMapper.selectOne(userQuery);
                dto.setStudentName(user != null ? user.getName() : "未知学生");
                
                // 检查签到状态
                QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
                attendanceQuery.eq("course_id", courseId)
                              .eq("student_username", relation.getStudentUsername());
                AttendanceRecord record = attendanceRecordMapper.selectOne(attendanceQuery);
                
                if (record != null) {
                    dto.setAttendanceStatus(1); // 已签到
                    dto.setAttendanceTime(record.getAttendanceTime());
                } else {
                    dto.setAttendanceStatus(0); // 未签到
                    dto.setAttendanceTime(null);
                }
                
                result.add(dto);
            }
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("获取课程签到情况失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改学生签到状态
     */
    public void updateStudentAttendance(String courseId, String studentCode, Integer status) {
        try {
            // 1. 验证课程是否存在
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 2. 查找现有签到记录
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.eq("course_id", courseId)
                          .eq("student_username", studentCode);
            AttendanceRecord existingRecord = attendanceRecordMapper.selectOne(attendanceQuery);
            
            if (status == 1) {
                // 设置为已签到
                if (existingRecord == null) {
                    // 创建新记录
                    AttendanceRecord record = new AttendanceRecord();
                    record.setCourseId(courseId);
                    record.setStudentUsername(studentCode);
                    record.setAttendanceTime(LocalDateTime.now());
                    record.setAttendanceStatus(1);
                    record.setIpAddress("127.0.0.1");
                    attendanceRecordMapper.insert(record);
                } else {
                    // 更新现有记录
                    existingRecord.setAttendanceStatus(1);
                    existingRecord.setAttendanceTime(LocalDateTime.now());
                    attendanceRecordMapper.updateById(existingRecord);
                }
            } else {
                // 设置为未签到
                if (existingRecord != null) {
                    attendanceRecordMapper.deleteById(existingRecord.getId());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("修改签到状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师的学生列表（班级中的学生 + 签到过的学生）
     */
    public List<StudentInfoDto> getTeacherStudents(String teacherCode) {
        return getTeacherStudents(teacherCode, null, null);
    }
    
    /**
     * 获取老师的学生列表（支持过滤条件）
     * @param teacherCode 老师工号
     * @param classCode 班级代码（可选，用于过滤特定班级）
     * @param studentType 学生类型（可选：CLASS_STUDENT, CROSS_CLASS_ATTENDEE）
     */
    public List<StudentInfoDto> getTeacherStudents(String teacherCode, String classCode, String studentType) {
        try {
            // 1. 获取老师的所有课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("teacher_username", teacherCode);
            if (classCode != null && !classCode.trim().isEmpty()) {
                courseQuery.eq("class_code", classCode);
            }
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            if (courses.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 获取这些课程对应的所有班级代码
            Set<String> classCodes = courses.stream()
                    .map(Course::getClassCode)
                    .collect(Collectors.toSet());
            
            // 3. 获取这些班级中绑定的学生（本班学生）
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", classCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 4. 获取这些课程的所有签到学生（包括跨班签到学生）
            List<String> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.in("course_id", courseIds);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            // 5. 性能优化：批量查询学生信息
            Set<String> allStudentCodes = new HashSet<>();
            relations.forEach(relation -> allStudentCodes.add(relation.getStudentUsername()));
            attendanceRecords.forEach(record -> allStudentCodes.add(record.getStudentUsername()));
            
            Map<String, User> studentMap = new HashMap<>();
            if (!allStudentCodes.isEmpty()) {
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.in("username", allStudentCodes);
                List<User> users = userMapper.selectList(userQuery);
                studentMap = users.stream()
                        .collect(Collectors.toMap(User::getUsername, user -> user));
            }
            
            // 6. 性能优化：批量查询班级信息
            Map<String, Class> classMap = new HashMap<>();
            if (!classCodes.isEmpty()) {
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.in("class_code", classCodes);
                List<Class> classes = classMapper.selectList(classQuery);
                classMap = classes.stream()
                        .collect(Collectors.toMap(Class::getClassCode, clazz -> clazz));
            }
            
            // 7. 创建课程映射
            Map<String, Course> courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getCourseId, course -> course));
            
            // 8. 构建学生信息映射
            Map<String, StudentInfoDto> studentInfoMap = new HashMap<>();
            
            // 8.1 处理本班学生
            for (StudentClassRelation relation : relations) {
                String studentCode = relation.getStudentUsername();
                User user = studentMap.get(studentCode);
                Class clazz = classMap.get(relation.getClassCode());
                
                StudentInfoDto dto = new StudentInfoDto();
                dto.setStudentCode(studentCode);
                dto.setStudentName(user != null ? user.getName() : "未知学生");
                dto.setClassCode(relation.getClassCode());
                dto.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                dto.setStudentType("CLASS_STUDENT");
                
                // 查找该学生最近的签到记录
                AttendanceRecord recentRecord = attendanceRecords.stream()
                        .filter(record -> record.getStudentUsername().equals(studentCode))
                        .max(Comparator.comparing(AttendanceRecord::getAttendanceTime))
                        .orElse(null);
                
                if (recentRecord != null) {
                    Course course = courseMap.get(recentRecord.getCourseId());
                    dto.setLastAttendanceTime(recentRecord.getAttendanceTime());
                    dto.setAttendanceStatus(recentRecord.getAttendanceStatus());
                    dto.setCourseId(recentRecord.getCourseId());
                    dto.setCourseName(course != null ? course.getCourseName() : "未知课程");
                } else {
                    dto.setAttendanceStatus(0);
                }
                
                studentInfoMap.put(studentCode, dto);
            }
            
            // 8.2 处理跨班签到学生（不在本班但已签到）
            for (AttendanceRecord record : attendanceRecords) {
                String studentCode = record.getStudentUsername();
                
                // 如果这个学生不在本班学生列表中，则添加为跨班签到学生
                if (!studentInfoMap.containsKey(studentCode)) {
                    User user = studentMap.get(studentCode);
                    Course course = courseMap.get(record.getCourseId());
                    
                    // 查询该学生自己的班级信息（不是签到课程的班级）
                    QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
                    studentClassQuery.eq("student_username", studentCode);
                    StudentClassRelation studentRelation = studentClassRelationMapper.selectOne(studentClassQuery);
                    
                    String studentClassCode = "未绑定";
                    String studentClassName = "未绑定班级";
                    
                    if (studentRelation != null) {
                        studentClassCode = studentRelation.getClassCode();
                        // 查询学生自己的班级信息
                        QueryWrapper<Class> studentClassInfoQuery = new QueryWrapper<>();
                        studentClassInfoQuery.eq("class_code", studentRelation.getClassCode());
                        Class studentClass = classMapper.selectOne(studentClassInfoQuery);
                        if (studentClass != null) {
                            studentClassName = studentClass.getClassName();
                        }
                    }
                    
                    StudentInfoDto dto = new StudentInfoDto();
                    dto.setStudentCode(studentCode);
                    dto.setStudentName(user != null ? user.getName() : "未知学生");
                    dto.setClassCode(studentClassCode);
                    dto.setClassName(studentClassName);
                    dto.setStudentType("CROSS_CLASS_ATTENDEE");
                    dto.setLastAttendanceTime(record.getAttendanceTime());
                    dto.setAttendanceStatus(record.getAttendanceStatus());
                    dto.setCourseId(record.getCourseId());
                    dto.setCourseName(course != null ? course.getCourseName() : "未知课程");
                    
                    studentInfoMap.put(studentCode, dto);
                }
            }
            
            // 9. 应用过滤条件
            List<StudentInfoDto> filteredStudents = studentInfoMap.values().stream()
                    .filter(dto -> {
                        // 按学生类型过滤
                        if (studentType != null && !studentType.trim().isEmpty()) {
                            return studentType.equals(dto.getStudentType());
                        }
                        return true;
                    })
                    .sorted((a, b) -> {
                        // 按签到时间降序排列，未签到的排在后面
                        if (a.getLastAttendanceTime() == null && b.getLastAttendanceTime() == null) {
                            return a.getStudentName().compareTo(b.getStudentName()); // 按姓名排序
                        }
                        if (a.getLastAttendanceTime() == null) return 1;
                        if (b.getLastAttendanceTime() == null) return -1;
                        return b.getLastAttendanceTime().compareTo(a.getLastAttendanceTime());
                    })
                    .collect(Collectors.toList());
            
            return filteredStudents;
            
        } catch (Exception e) {
            throw new RuntimeException("获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师班级中的学生列表（仅本班学生）
     */
    public List<StudentInfoDto> getTeacherClassStudents(String teacherCode) {
        try {
            // 1. 获取老师的所有课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("teacher_username", teacherCode);
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            if (courses.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 获取这些课程对应的所有班级代码
            Set<String> classCodes = courses.stream()
                    .map(Course::getClassCode)
                    .collect(Collectors.toSet());
            
            // 3. 获取这些班级中绑定的学生（本班学生）
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", classCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            if (relations.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 4. 批量查询学生信息
            Set<String> studentCodes = relations.stream()
                    .map(StudentClassRelation::getStudentUsername)
                    .collect(Collectors.toSet());
            
            Map<String, User> studentMap = new HashMap<>();
            if (!studentCodes.isEmpty()) {
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.in("username", studentCodes);
                List<User> users = userMapper.selectList(userQuery);
                studentMap = users.stream()
                        .collect(Collectors.toMap(User::getUsername, user -> user));
            }
            
            // 5. 批量查询班级信息
            Map<String, Class> classMap = new HashMap<>();
            if (!classCodes.isEmpty()) {
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.in("class_code", classCodes);
                List<Class> classes = classMapper.selectList(classQuery);
                classMap = classes.stream()
                        .collect(Collectors.toMap(Class::getClassCode, clazz -> clazz));
            }
            
            // 6. 获取这些课程的所有签到记录（用于查找本班学生的签到情况）
            List<String> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.in("course_id", courseIds);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            // 7. 创建课程映射
            Map<String, Course> courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getCourseId, course -> course));
            
            // 8. 构建本班学生信息
            List<StudentInfoDto> result = new ArrayList<>();
            
            for (StudentClassRelation relation : relations) {
                String studentCode = relation.getStudentUsername();
                User user = studentMap.get(studentCode);
                Class clazz = classMap.get(relation.getClassCode());
                
                StudentInfoDto dto = new StudentInfoDto();
                dto.setStudentCode(studentCode);
                dto.setStudentName(user != null ? user.getName() : "未知学生");
                dto.setClassCode(relation.getClassCode());
                dto.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                dto.setStudentType("CLASS_STUDENT");
                
                // 查找该学生最近的签到记录
                AttendanceRecord recentRecord = attendanceRecords.stream()
                        .filter(record -> record.getStudentUsername().equals(studentCode))
                        .max(Comparator.comparing(AttendanceRecord::getAttendanceTime))
                        .orElse(null);
                
                if (recentRecord != null) {
                    Course course = courseMap.get(recentRecord.getCourseId());
                    dto.setLastAttendanceTime(recentRecord.getAttendanceTime());
                    dto.setAttendanceStatus(recentRecord.getAttendanceStatus());
                    dto.setCourseId(recentRecord.getCourseId());
                    dto.setCourseName(course != null ? course.getCourseName() : "未知课程");
                } else {
                    dto.setAttendanceStatus(0);
                }
                
                result.add(dto);
            }
            
            // 9. 按签到时间降序排列
            result.sort((a, b) -> {
                if (a.getLastAttendanceTime() == null && b.getLastAttendanceTime() == null) {
                    return a.getStudentName().compareTo(b.getStudentName());
                }
                if (a.getLastAttendanceTime() == null) return 1;
                if (b.getLastAttendanceTime() == null) return -1;
                return b.getLastAttendanceTime().compareTo(a.getLastAttendanceTime());
            });
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("获取本班学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取跨班签到学生列表（不是本班学生但已签到）
     */
    public List<StudentInfoDto> getCrossClassAttendeeStudents(String teacherCode) {
        try {
            // 1. 获取老师的所有课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("teacher_username", teacherCode);
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            if (courses.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 获取这些课程对应的所有班级代码
            Set<String> classCodes = courses.stream()
                    .map(Course::getClassCode)
                    .collect(Collectors.toSet());
            
            // 3. 获取这些班级中绑定的学生（本班学生）
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", classCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 4. 获取本班学生的学号集合
            Set<String> classStudentCodes = relations.stream()
                    .map(StudentClassRelation::getStudentUsername)
                    .collect(Collectors.toSet());
            
            // 5. 获取这些课程的所有签到记录
            List<String> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.in("course_id", courseIds);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            // 6. 过滤出跨班签到学生（不在本班学生列表中但已签到）
            List<AttendanceRecord> crossClassRecords = attendanceRecords.stream()
                    .filter(record -> !classStudentCodes.contains(record.getStudentUsername()))
                    .collect(Collectors.toList());
            
            if (crossClassRecords.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 7. 批量查询跨班签到学生信息
            Set<String> crossClassStudentCodes = crossClassRecords.stream()
                    .map(AttendanceRecord::getStudentUsername)
                    .collect(Collectors.toSet());
            
            Map<String, User> studentMap = new HashMap<>();
            if (!crossClassStudentCodes.isEmpty()) {
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.in("username", crossClassStudentCodes);
                List<User> users = userMapper.selectList(userQuery);
                studentMap = users.stream()
                        .collect(Collectors.toMap(User::getUsername, user -> user));
            }
            
            // 8. 批量查询班级信息（用于本班学生）
            Map<String, Class> classMap = new HashMap<>();
            if (!classCodes.isEmpty()) {
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.in("class_code", classCodes);
                List<Class> classes = classMapper.selectList(classQuery);
                classMap = classes.stream()
                        .collect(Collectors.toMap(Class::getClassCode, clazz -> clazz));
            }
            
            // 9. 创建课程映射
            Map<String, Course> courseMap = courses.stream()
                    .collect(Collectors.toMap(Course::getCourseId, course -> course));
            
            // 10. 构建跨班签到学生信息
            List<StudentInfoDto> result = new ArrayList<>();
            
            for (AttendanceRecord record : crossClassRecords) {
                String studentCode = record.getStudentUsername();
                User user = studentMap.get(studentCode);
                Course course = courseMap.get(record.getCourseId());
                
                // 查询该学生自己的班级信息（不是签到课程的班级）
                QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
                studentClassQuery.eq("student_username", studentCode);
                StudentClassRelation studentRelation = studentClassRelationMapper.selectOne(studentClassQuery);
                
                String studentClassCode = "未绑定";
                String studentClassName = "未绑定班级";
                
                if (studentRelation != null) {
                    studentClassCode = studentRelation.getClassCode();
                    // 查询学生自己的班级信息
                    QueryWrapper<Class> studentClassInfoQuery = new QueryWrapper<>();
                    studentClassInfoQuery.eq("class_code", studentRelation.getClassCode());
                    Class studentClass = classMapper.selectOne(studentClassInfoQuery);
                    if (studentClass != null) {
                        studentClassName = studentClass.getClassName();
                    }
                }
                
                StudentInfoDto dto = new StudentInfoDto();
                dto.setStudentCode(studentCode);
                dto.setStudentName(user != null ? user.getName() : "未知学生");
                dto.setClassCode(studentClassCode);
                dto.setClassName(studentClassName);
                dto.setStudentType("CROSS_CLASS_ATTENDEE");
                dto.setLastAttendanceTime(record.getAttendanceTime());
                dto.setAttendanceStatus(record.getAttendanceStatus());
                dto.setCourseId(record.getCourseId());
                dto.setCourseName(course != null ? course.getCourseName() : "未知课程");
                
                result.add(dto);
            }
            
            // 11. 按签到时间降序排列
            result.sort((a, b) -> {
                if (a.getLastAttendanceTime() == null && b.getLastAttendanceTime() == null) {
                    return a.getStudentName().compareTo(b.getStudentName());
                }
                if (a.getLastAttendanceTime() == null) return 1;
                if (b.getLastAttendanceTime() == null) return -1;
                return b.getLastAttendanceTime().compareTo(a.getLastAttendanceTime());
            });
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("获取跨班签到学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生信息
     */
    public void updateStudent(UpdateStudentRequest request) {
        try {
            // 更新学生基本信息
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.eq("username", request.getStudentCode());
            User user = userMapper.selectOne(userQuery);
            
            if (user == null) {
                throw new RuntimeException("学生不存在");
            }
            
            user.setName(request.getStudentName());
            userMapper.updateById(user);
            
            // 注意：不再强制更新班级关系，因为学生可以签到任何课程
            // 班级绑定现在是可选的功能
            
        } catch (Exception e) {
            throw new RuntimeException("更新学生信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取详细签到统计
     */
    public DetailedAttendanceStatsDto getDetailedAttendanceStats(String courseId) {
        try {
            // 1. 获取课程信息
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 2. 获取班级信息
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_code", course.getClassCode());
            Class clazz = classMapper.selectOne(classQuery);
            
            // 3. 获取老师信息
            QueryWrapper<User> teacherQuery = new QueryWrapper<>();
            teacherQuery.eq("username", course.getTeacherUsername());
            User teacher = userMapper.selectOne(teacherQuery);
            
            // 4. 获取本班级所有学生（基于绑定关系）
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("class_code", course.getClassCode());
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 5. 获取该课程的所有签到记录
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.eq("course_id", courseId);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            // 6. 构建返回结果
            DetailedAttendanceStatsDto result = new DetailedAttendanceStatsDto();
            
            // 设置课程信息
            DetailedAttendanceStatsDto.CourseInfo courseInfo = new DetailedAttendanceStatsDto.CourseInfo();
            courseInfo.setCourseId(course.getCourseId());
            courseInfo.setCourseName(course.getCourseName());
            courseInfo.setTeacherName(teacher != null ? teacher.getName() : "未知老师");
            courseInfo.setLocation(course.getLocation());
            courseInfo.setCourseDate(course.getCourseDate());
            courseInfo.setTimeSlot(course.getTimeSlot());
            result.setCourseInfo(courseInfo);
            
            // 设置班级信息
            DetailedAttendanceStatsDto.ClassInfo classInfo = new DetailedAttendanceStatsDto.ClassInfo();
            classInfo.setClassCode(course.getClassCode());
            classInfo.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
            classInfo.setTotalStudentCount(relations.size());
            
            // 统计本班级签到情况
            Set<String> attendedStudents = attendanceRecords.stream()
                    .map(AttendanceRecord::getStudentUsername)
                    .collect(Collectors.toSet());
            
            int classAttendedCount = 0;
            for (StudentClassRelation relation : relations) {
                if (attendedStudents.contains(relation.getStudentUsername())) {
                    classAttendedCount++;
                }
            }
            
            classInfo.setAttendedCount(classAttendedCount);
            classInfo.setAbsentCount(relations.size() - classAttendedCount);
            result.setClassInfo(classInfo);
            
            // 构建本班级学生列表
            List<DetailedAttendanceStatsDto.StudentAttendanceInfo> classStudents = new ArrayList<>();
            for (StudentClassRelation relation : relations) {
                DetailedAttendanceStatsDto.StudentAttendanceInfo studentInfo = new DetailedAttendanceStatsDto.StudentAttendanceInfo();
                studentInfo.setStudentCode(relation.getStudentUsername());
                studentInfo.setClassCode(relation.getClassCode());
                studentInfo.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                studentInfo.setIsFromThisClass(true);
                
                // 获取学生姓名
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.eq("username", relation.getStudentUsername());
                User user = userMapper.selectOne(userQuery);
                studentInfo.setStudentName(user != null ? user.getName() : "未知学生");
                
                // 查找签到记录
                AttendanceRecord record = attendanceRecords.stream()
                        .filter(r -> r.getStudentUsername().equals(relation.getStudentUsername()))
                        .findFirst()
                        .orElse(null);
                
                if (record != null) {
                    studentInfo.setIsAttended(true);
                    studentInfo.setAttendanceTime(record.getAttendanceTime());
                    studentInfo.setAttendanceStatus(record.getAttendanceStatus());
                    studentInfo.setStatusText(getStatusText(record.getAttendanceStatus()));
                } else {
                    studentInfo.setIsAttended(false);
                    studentInfo.setAttendanceTime(null);
                    studentInfo.setAttendanceStatus(null);
                    studentInfo.setStatusText("未签到");
                }
                
                classStudents.add(studentInfo);
            }
            result.setClassStudents(classStudents);
            
            // 构建其他班级学生列表
            List<DetailedAttendanceStatsDto.StudentAttendanceInfo> otherClassStudents = new ArrayList<>();
            Set<String> classStudentCodes = relations.stream()
                    .map(StudentClassRelation::getStudentUsername)
                    .collect(Collectors.toSet());
            
            for (AttendanceRecord record : attendanceRecords) {
                if (!classStudentCodes.contains(record.getStudentUsername())) {
                    DetailedAttendanceStatsDto.StudentAttendanceInfo studentInfo = new DetailedAttendanceStatsDto.StudentAttendanceInfo();
                    studentInfo.setStudentCode(record.getStudentUsername());
                    studentInfo.setIsAttended(true);
                    studentInfo.setAttendanceTime(record.getAttendanceTime());
                    studentInfo.setAttendanceStatus(record.getAttendanceStatus());
                    studentInfo.setStatusText(getStatusText(record.getAttendanceStatus()));
                    studentInfo.setIsFromThisClass(false);
                    
                    // 获取学生信息
                    QueryWrapper<User> userQuery = new QueryWrapper<>();
                    userQuery.eq("username", record.getStudentUsername());
                    User user = userMapper.selectOne(userQuery);
                    studentInfo.setStudentName(user != null ? user.getName() : "未知学生");
                    
                    // 获取学生班级信息（如果有绑定关系）
                    QueryWrapper<StudentClassRelation> studentClassQuery = new QueryWrapper<>();
                    studentClassQuery.eq("student_username", record.getStudentUsername());
                    StudentClassRelation studentRelation = studentClassRelationMapper.selectOne(studentClassQuery);
                    
                    if (studentRelation != null) {
                        studentInfo.setClassCode(studentRelation.getClassCode());
                        QueryWrapper<Class> studentClassInfoQuery = new QueryWrapper<>();
                        studentClassInfoQuery.eq("class_code", studentRelation.getClassCode());
                        Class studentClass = classMapper.selectOne(studentClassInfoQuery);
                        studentInfo.setClassName(studentClass != null ? studentClass.getClassName() : "未知班级");
                    } else {
                        studentInfo.setClassCode("未绑定");
                        studentInfo.setClassName("未绑定班级");
                    }
                    
                    otherClassStudents.add(studentInfo);
                }
            }
            result.setOtherClassStudents(otherClassStudents);
            
            // 构建统计汇总
            DetailedAttendanceStatsDto.AttendanceSummary summary = new DetailedAttendanceStatsDto.AttendanceSummary();
            summary.setTotalAttended(attendanceRecords.size());
            summary.setClassAttended(classAttendedCount);
            summary.setOtherClassAttended(attendanceRecords.size() - classAttendedCount);
            summary.setClassAbsent(relations.size() - classAttendedCount);
            
            // 计算签到率
            if (relations.size() > 0) {
                double rate = (double) classAttendedCount / relations.size() * 100;
                summary.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
            } else {
                summary.setAttendanceRate(0.0);
            }
            
            // 统计签到状态
            int normalCount = 0, lateCount = 0, earlyLeaveCount = 0;
            for (AttendanceRecord record : attendanceRecords) {
                switch (record.getAttendanceStatus()) {
                    case 1: normalCount++; break;
                    case 2: lateCount++; break;
                    case 3: earlyLeaveCount++; break;
                }
            }
            summary.setNormalCount(normalCount);
            summary.setLateCount(lateCount);
            summary.setEarlyLeaveCount(earlyLeaveCount);
            
            result.setSummary(summary);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("获取详细签到统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取老师学生列表的调试信息
     */
    public String getTeacherStudentsDebugInfo(String teacherCode) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== 老师学生列表调试信息 ===\n");
        debug.append("老师工号: ").append(teacherCode).append("\n\n");
        
        try {
            // 1. 查询老师的课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("teacher_username", teacherCode);
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            debug.append("1. 老师课程信息:\n");
            debug.append("   课程总数: ").append(courses.size()).append("\n");
            for (Course course : courses) {
                debug.append("   - 课程ID: ").append(course.getCourseId())
                     .append(", 课程名: ").append(course.getCourseName())
                     .append(", 班级代码: ").append(course.getClassCode())
                     .append("\n");
            }
            debug.append("\n");
            
            if (courses.isEmpty()) {
                debug.append("❌ 没有找到老师的课程，无法查询学生信息\n");
                return debug.toString();
            }
            
            // 2. 获取班级代码
            Set<String> classCodes = courses.stream()
                    .map(Course::getClassCode)
                    .collect(Collectors.toSet());
            
            debug.append("2. 班级代码列表:\n");
            for (String classCode : classCodes) {
                debug.append("   - ").append(classCode).append("\n");
            }
            debug.append("\n");
            
            // 3. 查询班级绑定关系
            debug.append("3. 班级绑定关系查询:\n");
            debug.append("   查询条件: class_code IN ").append(classCodes).append("\n");
            
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", classCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            debug.append("   绑定关系总数: ").append(relations.size()).append("\n");
            for (StudentClassRelation relation : relations) {
                debug.append("   - 学生: ").append(relation.getStudentUsername())
                     .append(", 班级: ").append(relation.getClassCode())
                     .append(", 绑定时间: ").append(relation.getBindTime())
                     .append(", 是否删除: ").append(relation.getIsDeleted())
                     .append("\n");
            }
            debug.append("\n");
            
            // 4. 查询签到记录
            List<String> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.in("course_id", courseIds);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            debug.append("4. 签到记录:\n");
            debug.append("   签到记录总数: ").append(attendanceRecords.size()).append("\n");
            for (AttendanceRecord record : attendanceRecords) {
                debug.append("   - 学生: ").append(record.getStudentUsername())
                     .append(", 课程: ").append(record.getCourseId())
                     .append(", 签到时间: ").append(record.getAttendanceTime())
                     .append("\n");
            }
            debug.append("\n");
            
            // 5. 分析结果
            Set<String> classStudentCodes = relations.stream()
                    .map(StudentClassRelation::getStudentUsername)
                    .collect(Collectors.toSet());
            
            Set<String> attendanceStudentCodes = attendanceRecords.stream()
                    .map(AttendanceRecord::getStudentUsername)
                    .collect(Collectors.toSet());
            
            debug.append("5. 分析结果:\n");
            debug.append("   本班学生总数: ").append(classStudentCodes.size()).append("\n");
            debug.append("   签到学生总数: ").append(attendanceStudentCodes.size()).append("\n");
            
            Set<String> crossClassStudents = new HashSet<>(attendanceStudentCodes);
            crossClassStudents.removeAll(classStudentCodes);
            
            debug.append("   跨班签到学生数: ").append(crossClassStudents.size()).append("\n");
            debug.append("   跨班签到学生列表:\n");
            for (String studentCode : crossClassStudents) {
                debug.append("   - ").append(studentCode).append("\n");
            }
            
            if (classStudentCodes.isEmpty()) {
                debug.append("\n❌ 警告: 没有找到本班学生绑定关系！\n");
                debug.append("   可能原因:\n");
                debug.append("   1. 学生还没有绑定到班级\n");
                debug.append("   2. 班级代码不匹配\n");
                debug.append("   3. 数据表 student_class_relations 中没有相关记录\n");
            }
            
        } catch (Exception e) {
            debug.append("❌ 查询过程中出现错误: ").append(e.getMessage()).append("\n");
        }
        
        debug.append("\n=== 调试信息结束 ===");
        return debug.toString();
    }
    
    /**
     * 获取签到状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未签到";
        switch (status) {
            case 1: return "正常签到";
            case 2: return "迟到";
            case 3: return "早退";
            default: return "未知状态";
        }
    }
    
}
