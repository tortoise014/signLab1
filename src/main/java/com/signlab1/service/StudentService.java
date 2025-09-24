package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.*;
import com.signlab1.entity.AttendanceRecord;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.StudentClassRelation;
import com.signlab1.entity.User;
import com.signlab1.mapper.*;
import com.signlab1.util.QrCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生服务类
 */
@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final StudentClassRelationMapper studentClassRelationMapper;
    private final ClassMapper classMapper;
    private final QrCodeUtil qrCodeUtil;
    
    /**
     * 扫码签到
     */
    public AttendanceResultDto scanAttendance(String studentCode, String qrData) {
        try {
            // 1. 解析二维码内容
            Map<String, String> qrContent = qrCodeUtil.parseAttendanceQrContent(qrData);
            String courseId = qrContent.get("courseId");
            String teacherCode = qrContent.get("teacherCode");
            String classCode = qrContent.get("classCode");
            String timestampStr = qrContent.get("timestamp");
            
            // 2. 验证二维码时效性（10秒有效期）
            Long timestamp = Long.parseLong(timestampStr);
            
            // 如果是测试课程，跳过时间验证
            if ("TEST_COURSE".equals(courseId)) {
                // 跳过时间验证，继续执行
            } else {
                // 正常的时间验证
                if (!qrCodeUtil.isQrCodeValid(timestamp, 10)) {
                    throw new RuntimeException("二维码已过期，请重新扫描");
                }
            }
            
            // 3. 验证课程是否存在
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 4. 验证学生是否属于该班级
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode)
                        .eq("class_code", classCode);
            StudentClassRelation relation = studentClassRelationMapper.selectOne(relationQuery);
            if (relation == null) {
                throw new RuntimeException("您不属于该课程班级，无法签到");
            }
            
            // 5. 检查是否已签到
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.eq("course_id", courseId)
                          .eq("student_username", studentCode);
            AttendanceRecord existingRecord = attendanceRecordMapper.selectOne(attendanceQuery);
            if (existingRecord != null) {
                throw new RuntimeException("您已经签到过了");
            }
            
            // 6. 创建签到记录
            AttendanceRecord record = new AttendanceRecord();
            record.setCourseId(courseId);
            record.setStudentUsername(studentCode);
            record.setAttendanceTime(LocalDateTime.now());
            record.setAttendanceStatus(1); // 1-已签到
            record.setIpAddress("127.0.0.1"); // 暂时写死，后续可以从请求中获取
            
            attendanceRecordMapper.insert(record);
            
            // 6. 获取老师信息
            QueryWrapper<User> teacherQuery = new QueryWrapper<>();
            teacherQuery.eq("username", teacherCode);
            User teacher = userMapper.selectOne(teacherQuery);
            
            // 7. 构建返回结果
            AttendanceResultDto result = new AttendanceResultDto();
            result.setCourseName(course.getCourseName());
            result.setTeacherName(teacher != null ? teacher.getName() : "未知老师");
            result.setAttendanceTime(record.getAttendanceTime());
            result.setLocation(course.getLocation());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("签到失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到记录
     */
    public List<AttendanceRecordDto> getAttendanceRecords(String studentCode) {
        try {
            // 查询学生的签到记录
            QueryWrapper<AttendanceRecord> query = new QueryWrapper<>();
            query.eq("student_username", studentCode)
                .orderByDesc("attendance_time");
            
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(query);
            
            // 转换为DTO
            return records.stream().map(record -> {
                AttendanceRecordDto dto = new AttendanceRecordDto();
                dto.setCourseId(record.getCourseId());
                dto.setAttendanceTime(record.getAttendanceTime());
                dto.setStatus(record.getAttendanceStatus());
                
                // 获取课程信息
                QueryWrapper<Course> courseQuery = new QueryWrapper<>();
                courseQuery.eq("course_id", record.getCourseId());
                Course course = courseMapper.selectOne(courseQuery);
                if (course != null) {
                    dto.setCourseName(course.getCourseName());
                    dto.setLocation(course.getLocation());
                    
                    // 获取老师信息
                    QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("username", course.getTeacherUsername());
                    User teacher = userMapper.selectOne(teacherQuery);
                    dto.setTeacherName(teacher != null ? teacher.getName() : "未知老师");
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取签到记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取签到统计
     */
    public AttendanceStatsDto getAttendanceStats(String studentCode) {
        try {
            // 查询学生的签到记录
            QueryWrapper<AttendanceRecord> query = new QueryWrapper<>();
            query.eq("student_username", studentCode);
            
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(query);
            
            // 计算统计信息
            AttendanceStatsDto stats = new AttendanceStatsDto();
            stats.setTotalAttendance(records.size());
            
            // 计算签到率（这里简化处理，假设学生应该参与所有已绑定的课程）
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            if (relations.size() > 0) {
                double rate = (double) records.size() / relations.size() * 100;
                stats.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
            } else {
                stats.setAttendanceRate(0.0);
            }
            
            return stats;
            
        } catch (Exception e) {
            throw new RuntimeException("获取签到统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 绑定班级
     */
    public void bindClass(String studentCode, String verificationCode) {
        try {
            System.out.println("=== 绑定班级调试信息 ===");
            System.out.println("学生学号: " + studentCode);
            System.out.println("验证码: " + verificationCode);
            
            // 1. 验证学生是否存在
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.eq("username", studentCode);
            System.out.println("查询学生SQL: " + userQuery.getTargetSql());
            
            User student = userMapper.selectOne(userQuery);
            System.out.println("查询到的学生: " + (student != null ? student.toString() : "null"));
            
            if (student == null) {
                throw new RuntimeException("学生不存在: " + studentCode);
            }
            
            // 2. 验证班级验证码
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("verification_code", verificationCode);
            System.out.println("查询班级SQL: " + classQuery.getTargetSql());
            
            Class clazz = classMapper.selectOne(classQuery);
            System.out.println("查询到的班级: " + (clazz != null ? clazz.toString() : "null"));
            
            if (clazz == null) {
                throw new RuntimeException("班级验证码无效: " + verificationCode);
            }
            
            // 3. 检查是否已经绑定
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode)
                        .eq("class_code", clazz.getClassCode());
            System.out.println("查询绑定关系SQL: " + relationQuery.getTargetSql());
            
            StudentClassRelation existingRelation = studentClassRelationMapper.selectOne(relationQuery);
            System.out.println("查询到的绑定关系: " + (existingRelation != null ? existingRelation.toString() : "null"));
            
            if (existingRelation != null) {
                throw new RuntimeException("您已经绑定过该班级");
            }
            
            // 4. 创建绑定关系
            StudentClassRelation relation = new StudentClassRelation();
            relation.setStudentUsername(studentCode);
            relation.setClassCode(clazz.getClassCode());
            relation.setBindTime(LocalDateTime.now());
            
            System.out.println("准备插入绑定关系: " + relation.toString());
            studentClassRelationMapper.insert(relation);
            System.out.println("绑定关系插入成功");
            
        } catch (Exception e) {
            System.out.println("绑定班级异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("绑定班级失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生已绑定的班级列表
     */
    public List<ClassInfoDto> getStudentClasses(String studentCode) {
        try {
            // 查询学生的班级绑定关系
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            // 转换为DTO
            return relations.stream().map(relation -> {
                ClassInfoDto dto = new ClassInfoDto();
                dto.setClassCode(relation.getClassCode());
                dto.setBindTime(relation.getBindTime());
                
                // 获取班级信息
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", relation.getClassCode());
                Class clazz = classMapper.selectOne(classQuery);
                if (clazz != null) {
                    dto.setClassName(clazz.getClassName());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取班级列表失败: " + e.getMessage());
        }
    }
}
