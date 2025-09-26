package com.signlab1.service;

import com.signlab1.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课表解析服务
 * 从学生名单的"上课时间地点"字段中解析出详细的课程安排
 */
@Service
@RequiredArgsConstructor
public class ScheduleParserService {
    
    /**
     * 解析上课时间地点字符串，生成课程列表
     * @param scheduleText 上课时间地点文本
     * @param courseName 课程名称
     * @param teacherUsername 老师用户名
     * @param className 班级名称
     * @return 课程列表
     */
    public List<Course> parseSchedule(String scheduleText, String courseName, String teacherUsername, String className) {
        return parseSchedule(scheduleText, courseName, teacherUsername, className, null);
    }
    
    /**
     * 解析上课时间地点字符串，生成课程列表（支持自定义学期开始日期）
     * @param scheduleText 上课时间地点文本
     * @param courseName 课程名称
     * @param teacherUsername 老师用户名
     * @param className 班级名称
     * @param semesterStartDate 学期开始日期（可选）
     * @return 课程列表
     */
    public List<Course> parseSchedule(String scheduleText, String courseName, String teacherUsername, String className, LocalDate semesterStartDate) {
        List<Course> courses = new ArrayList<>();
        
        if (scheduleText == null || scheduleText.trim().isEmpty()) {
            return courses;
        }
        
        // 按分号分割不同的上课时间段
        String[] scheduleParts = scheduleText.split(";");
        
        for (String part : scheduleParts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            Course course = parseSchedulePart(part, courseName, teacherUsername, className, semesterStartDate);
            if (course != null) {
                courses.add(course);
            }
        }
        
        return courses;
    }
    
    /**
     * 解析单个上课时间段
     * 格式示例: 5周 星期二[6-9节]实验4-211
     */
    private Course parseSchedulePart(String schedulePart, String courseName, String teacherUsername, String className) {
        return parseSchedulePart(schedulePart, courseName, teacherUsername, className, null);
    }
    
    /**
     * 解析单个上课时间段（支持自定义学期开始日期）
     * 格式示例: 5周 星期二[6-9节]实验4-211
     */
    private Course parseSchedulePart(String schedulePart, String courseName, String teacherUsername, String className, LocalDate semesterStartDate) {
        try {
            // 正则表达式匹配
            Pattern pattern = Pattern.compile("(\\d+)周\\s*星期([一二三四五六日])\\[(\\d+)-(\\d+)节\\](.*)");
            Matcher matcher = pattern.matcher(schedulePart);
            
            if (!matcher.find()) {
                return null;
            }
            
            int weekNumber = Integer.parseInt(matcher.group(1));
            String dayOfWeek = matcher.group(2);
            int startLesson = Integer.parseInt(matcher.group(3));
            int endLesson = Integer.parseInt(matcher.group(4));
            String location = matcher.group(5).trim();
            
            // 转换星期
            String dayInEnglish = convertChineseDayToEnglish(dayOfWeek);
            if (dayInEnglish == null) {
                return null;
            }
            
            // 计算具体日期
            LocalDate courseDate = calculateCourseDate(weekNumber, dayInEnglish, semesterStartDate);
            if (courseDate == null) {
                return null;
            }
            
            // 转换节次为时间段
            String timeSlot = convertLessonToTimeSlot(startLesson, endLesson);
            
            // 生成课程ID
            String courseId = generateCourseId();
            
            // 创建课程对象
            Course course = new Course();
            course.setCourseId(courseId);
            course.setCourseName(courseName);
            course.setTeacherUsername(teacherUsername);
            course.setClassCode(className);
            course.setLocation(location);
            course.setCourseDate(courseDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            course.setTimeSlot(timeSlot);
            course.setCreateTime(java.time.LocalDateTime.now());
            course.setUpdateTime(java.time.LocalDateTime.now());
            
            return course;
            
        } catch (Exception e) {
            System.err.println("解析上课时间失败: " + schedulePart + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 中文星期转换为英文
     */
    private String convertChineseDayToEnglish(String chineseDay) {
        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("一", "MONDAY");
        dayMap.put("二", "TUESDAY");
        dayMap.put("三", "WEDNESDAY");
        dayMap.put("四", "THURSDAY");
        dayMap.put("五", "FRIDAY");
        dayMap.put("六", "SATURDAY");
        dayMap.put("日", "SUNDAY");
        
        return dayMap.get(chineseDay);
    }
    
    /**
     * 计算课程具体日期
     * 使用当前日期作为参考，计算相对周次
     */
    private LocalDate calculateCourseDate(int weekNumber, String dayOfWeek) {
        return calculateCourseDate(weekNumber, dayOfWeek, null);
    }
    
    /**
     * 计算课程具体日期（支持自定义学期开始日期）
     * @param weekNumber 周次
     * @param dayOfWeek 星期几
     * @param semesterStartDate 学期开始日期（可选）
     * @return 课程日期
     */
    private LocalDate calculateCourseDate(int weekNumber, String dayOfWeek, LocalDate semesterStartDate) {
        LocalDate baseDate;
        
        if (semesterStartDate != null) {
            // 使用指定的学期开始日期
            baseDate = semesterStartDate;
        } else {
            // 默认使用2025年9月1日作为学期开始日期
            baseDate = LocalDate.of(2025, 9, 1);
        }
        
        // 计算目标周次相对于基准日期的偏移
        int weekOffset = weekNumber - 1; // 第1周从基准日期开始
        
        // 计算目标日期
        LocalDate targetDate = baseDate.plusWeeks(weekOffset);
        
        // 计算目标星期几的日期
        java.time.DayOfWeek targetDayOfWeek = java.time.DayOfWeek.valueOf(dayOfWeek);
        
        // 找到目标星期几
        while (targetDate.getDayOfWeek() != targetDayOfWeek) {
            targetDate = targetDate.plusDays(1);
        }
        
        return targetDate;
    }
    
    /**
     * 节次转换为时间段
     * 根据实际作息时间表转换
     */
    private String convertLessonToTimeSlot(int startLesson, int endLesson) {
        // 根据实际作息时间表转换
        Map<Integer, String> lessonTimeMap = new HashMap<>();
        lessonTimeMap.put(1, "08:30");
        lessonTimeMap.put(2, "10:05");
        lessonTimeMap.put(3, "10:25");
        lessonTimeMap.put(4, "12:00");
        lessonTimeMap.put(5, "13:50");
        lessonTimeMap.put(6, "14:40");
        lessonTimeMap.put(7, "16:15");
        lessonTimeMap.put(8, "16:30");
        lessonTimeMap.put(9, "18:05");
        
        String startTime = lessonTimeMap.get(startLesson);
        String endTime = lessonTimeMap.get(endLesson);
        
        if (startTime != null && endTime != null) {
            return startTime + "-" + endTime;
        }
        
        return "08:30-10:05"; // 默认时间段
    }
    
    /**
     * 生成课程ID
     */
    private String generateCourseId() {
        String year = String.valueOf(java.time.LocalDateTime.now().getYear()).substring(2);
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        return String.format("KC%s%06d", year, randomNum);
    }
}

