package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.AttendanceQrDto;
import com.signlab1.dto.AttendanceStatsDto;
import com.signlab1.dto.CourseInfoDto;
import com.signlab1.entity.AttendanceRecord;
import com.signlab1.entity.Class;
import com.signlab1.entity.Course;
import com.signlab1.entity.StudentDocument;
import com.signlab1.mapper.AttendanceRecordMapper;
import com.signlab1.mapper.ClassMapper;
import com.signlab1.mapper.CourseMapper;
import com.signlab1.mapper.StudentDocumentMapper;
import com.signlab1.util.QrCodeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 老师端课程管理服务
 */
@Service
public class TeacherService {
    
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final StudentDocumentMapper studentDocumentMapper;
    private final QrCodeUtil qrCodeUtil;
    
    public TeacherService(CourseMapper courseMapper, ClassMapper classMapper, 
                         AttendanceRecordMapper attendanceRecordMapper, 
                         StudentDocumentMapper studentDocumentMapper, 
                         QrCodeUtil qrCodeUtil) {
        this.courseMapper = courseMapper;
        this.classMapper = classMapper;
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.studentDocumentMapper = studentDocumentMapper;
        this.qrCodeUtil = qrCodeUtil;
    }
    
    /**
     * 获取老师今日课程
     */
    public List<CourseInfoDto> getTodayCourses(String teacherCode) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return getCoursesByDate(teacherCode, today);
    }
    
    /**
     * 根据日期获取老师课程
     */
    public List<CourseInfoDto> getCoursesByDate(String teacherCode, String date) {
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_code", teacherCode);
        queryWrapper.eq("course_date", date);
        queryWrapper.orderByAsc("time_slot");
        
        List<Course> courses = courseMapper.selectList(queryWrapper);
        List<CourseInfoDto> result = new ArrayList<>();
        
        for (Course course : courses) {
            CourseInfoDto dto = new CourseInfoDto();
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setTimeSlot(course.getTimeSlot());
            dto.setLocation(course.getLocation());
            dto.setCourseDate(course.getCourseDate());
            
            // 获取班级名称
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_code", course.getClassCode());
            Class clazz = classMapper.selectOne(classQuery);
            dto.setClassName(clazz != null ? clazz.getClassName() : course.getClassCode());
            
            // 检查是否可以发起签到（在课程时间段内）
            dto.setCanStartAttendance(isInCourseTime(course.getTimeSlot()));
            
            // 获取学生文档数量
            QueryWrapper<StudentDocument> docQuery = new QueryWrapper<>();
            docQuery.eq("course_id", course.getCourseId());
            int docCount = studentDocumentMapper.selectCount(docQuery).intValue();
            dto.setDocumentCount(docCount);
            dto.setCanViewDocuments(docCount > 0);
            
            result.add(dto);
        }
        
        return result;
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
        
        // 检查是否在课程时间段内
        if (!isInCourseTime(course.getTimeSlot())) {
            throw new RuntimeException("未到上课时间，无法发起签到");
        }
        
        // 生成二维码内容
        long timestamp = System.currentTimeMillis() / 1000; // 秒级时间戳
        String qrContent = qrCodeUtil.generateAttendanceQrContent(
            courseId, course.getTeacherCode(), course.getClassCode(), timestamp);
        
        // 生成二维码图片
        String qrImage = qrCodeUtil.generateQrCodeBase64(qrContent, 300, 300);
        
        AttendanceQrDto dto = new AttendanceQrDto();
        dto.setQrContent(qrContent);
        dto.setQrImage(qrImage);
        dto.setCourseId(courseId);
        dto.setTimestamp(timestamp);
        dto.setRemainingTime(10); // 10秒有效期
        
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
        dto.setTotalCount(totalCount);
        dto.setAttendedCount(attendedCount);
        dto.setAbsentCount(absentCount);
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
