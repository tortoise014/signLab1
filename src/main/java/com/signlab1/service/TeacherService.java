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
import java.util.List;
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
                throw new RuntimeException("用户不存在: " + teacherUsername);
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
            
            // 2. 验证学生是否属于该课程班级
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode)
                        .eq("class_code", course.getClassCode());
            StudentClassRelation relation = studentClassRelationMapper.selectOne(relationQuery);
            if (relation == null) {
                throw new RuntimeException("学生不属于该课程班级");
            }
            
            // 3. 查找现有签到记录
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
     * 获取老师的学生列表
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
            
            // 2. 获取所有课程涉及的班级
            List<String> classCodes = courses.stream()
                    .map(Course::getClassCode)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 3. 获取这些班级的所有学生
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.in("class_code", classCodes);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 4. 转换为DTO
            return relations.stream().map(relation -> {
                StudentInfoDto dto = new StudentInfoDto();
                dto.setStudentCode(relation.getStudentUsername());
                dto.setClassCode(relation.getClassCode());
                
                // 获取学生信息
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.eq("username", relation.getStudentUsername());
                User user = userMapper.selectOne(userQuery);
                dto.setStudentName(user != null ? user.getName() : "未知学生");
                
                // 获取班级信息
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", relation.getClassCode());
                Class clazz = classMapper.selectOne(classQuery);
                dto.setClassName(clazz != null ? clazz.getClassName() : "未知班级");
                
                return dto;
            }).distinct().collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生信息
     */
    public void updateStudent(UpdateStudentRequest request) {
        try {
            // 1. 更新学生基本信息
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.eq("username", request.getStudentCode());
            User user = userMapper.selectOne(userQuery);
            
            if (user == null) {
                throw new RuntimeException("学生不存在");
            }
            
            user.setName(request.getStudentName());
            userMapper.updateById(user);
            
            // 2. 更新班级关系
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", request.getStudentCode());
            StudentClassRelation relation = studentClassRelationMapper.selectOne(relationQuery);
            
            if (relation != null) {
                relation.setClassCode(request.getClassCode());
                studentClassRelationMapper.updateById(relation);
            } else {
                // 如果不存在关系，创建新的关系
                relation = new StudentClassRelation();
                relation.setStudentUsername(request.getStudentCode());
                relation.setClassCode(request.getClassCode());
                relation.setBindTime(LocalDateTime.now());
                studentClassRelationMapper.insert(relation);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("更新学生信息失败: " + e.getMessage());
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
