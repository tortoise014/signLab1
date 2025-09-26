package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.*;
import com.signlab1.entity.AttendanceRecord;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.StudentClassRelation;
import com.signlab1.entity.StudentDocument;
import com.signlab1.entity.User;
import com.signlab1.mapper.*;
import com.signlab1.util.QrCodeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
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
    private final QrCodeUtil qrCodeUtil;
    
    public TeacherService(CourseMapper courseMapper, ClassMapper classMapper, 
                         AttendanceRecordMapper attendanceRecordMapper, 
                         StudentDocumentMapper studentDocumentMapper,
                         StudentClassRelationMapper studentClassRelationMapper,
                         UserMapper userMapper,
                         QrCodeUtil qrCodeUtil) {
        this.courseMapper = courseMapper;
        this.classMapper = classMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentDocumentMapper = studentDocumentMapper;
        this.studentClassRelationMapper = studentClassRelationMapper;
        this.userMapper = userMapper;
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
            queryWrapper.orderByDesc("course_date").orderByAsc("time_slot");
            
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
        dto.setRemainingTime(qrCodeUtil.getRemainingTime(timestamp, 10)); // 动态计算剩余时间
        
        return dto;
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
        
        int absentCount = totalCount - attendedCount;
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
        try {
            // 1. 获取老师的所有课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("teacher_username", teacherCode);
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            if (courses.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 2. 获取这些课程对应的所有课程代码
            List<String> courseCodes = courses.stream()
                    .map(Course::getClassCode)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 3. 获取这些课程中绑定的学生
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", courseCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 4. 获取这些课程的所有签到学生
            List<String> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .collect(Collectors.toList());
            
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.in("course_id", courseIds);
            List<AttendanceRecord> attendanceRecords = attendanceRecordMapper.selectList(attendanceQuery);
            
            // 5. 合并两类学生（去重）
            Set<String> studentCodes = new HashSet<>();
            
            // 添加班级中的学生
            relations.forEach(relation -> studentCodes.add(relation.getStudentUsername()));
            
            // 添加签到过的学生
            attendanceRecords.forEach(record -> studentCodes.add(record.getStudentUsername()));
            
            // 6. 转换为DTO
            return studentCodes.stream()
                    .map(studentCode -> {
                        StudentInfoDto dto = new StudentInfoDto();
                        dto.setStudentCode(studentCode);
                        
                        // 获取学生信息
                        QueryWrapper<User> userQuery = new QueryWrapper<>();
                        userQuery.eq("username", studentCode);
                        User user = userMapper.selectOne(userQuery);
                        dto.setStudentName(user != null ? user.getName() : "未知学生");
                        
                        // 优先从班级绑定关系获取班级信息
                        StudentClassRelation relation = relations.stream()
                                .filter(r -> r.getStudentUsername().equals(studentCode))
                                .findFirst()
                                .orElse(null);
                        
                        if (relation != null) {
                            dto.setClassCode(relation.getClassCode());
                            
                            // 从课程信息获取班级信息
                            Course course = courses.stream()
                                    .filter(c -> c.getClassCode().equals(relation.getClassCode()))
                                    .findFirst()
                                    .orElse(null);
                            
                            if (course != null) {
                                // 获取班级信息
                                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                                classQuery.eq("class_code", course.getClassCode());
                                Class clazz = classMapper.selectOne(classQuery);
                                dto.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                            } else {
                                dto.setClassName("未知班级");
                            }
                        } else {
                            // 如果没有班级绑定，从课程信息推断班级
                            AttendanceRecord record = attendanceRecords.stream()
                                    .filter(r -> r.getStudentUsername().equals(studentCode))
                                    .findFirst()
                                    .orElse(null);
                            
                            if (record != null) {
                                Course course = courses.stream()
                                        .filter(c -> c.getCourseId().equals(record.getCourseId()))
                                        .findFirst()
                                        .orElse(null);
                                
                                if (course != null) {
                                    dto.setClassCode(course.getClassCode());
                                    
                                    // 获取班级信息
                                    QueryWrapper<Class> classQuery = new QueryWrapper<>();
                                    classQuery.eq("class_code", course.getClassCode());
                                    Class clazz = classMapper.selectOne(classQuery);
                                    dto.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                                } else {
                                    dto.setClassCode("未知");
                                    dto.setClassName("未知班级");
                                }
                            } else {
                                dto.setClassCode("未知");
                                dto.setClassName("未知班级");
                            }
                        }
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取学生列表失败: " + e.getMessage());
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
    
    /**
     * 检查是否在课程时间段内
     */
    private boolean isInCourseTime(String timeSlot) {
        try {
            String[] times = timeSlot.split("-");
            if (times.length != 2) return false;
            
            LocalTime startTime = LocalTime.parse(times[0]);
            LocalTime endTime = LocalTime.parse(times[1]);
            LocalTime now = LocalTime.now();
            
            return now.isAfter(startTime) && now.isBefore(endTime);
        } catch (Exception e) {
            return false;
        }
    }
}
