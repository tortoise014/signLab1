package com.signlab1.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间段解析工具类
 */
@Slf4j
@Component
public class TimeSlotParser {
    
    // 时间段格式：08:00-09:40, 上午, 下午, 晚上等
    private static final Pattern TIME_RANGE_PATTERN = Pattern.compile("(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})");
    
    /**
     * 解析时间段字符串
     * @param timeSlot 时间段字符串，如 "08:00-09:40", "上午", "下午"
     * @return 时间段信息
     */
    public TimeSlotInfo parseTimeSlot(String timeSlot) {
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            return null;
        }
        
        timeSlot = timeSlot.trim();
        
        // 尝试解析具体时间范围
        Matcher matcher = TIME_RANGE_PATTERN.matcher(timeSlot);
        if (matcher.matches()) {
            try {
                int startHour = Integer.parseInt(matcher.group(1));
                int startMinute = Integer.parseInt(matcher.group(2));
                int endHour = Integer.parseInt(matcher.group(3));
                int endMinute = Integer.parseInt(matcher.group(4));
                
                LocalTime startTime = LocalTime.of(startHour, startMinute);
                LocalTime endTime = LocalTime.of(endHour, endMinute);
                
                return new TimeSlotInfo(startTime, endTime, timeSlot);
            } catch (Exception e) {
                log.warn("解析时间段失败: {}", timeSlot, e);
            }
        }
        
        // 解析描述性时间段
        return parseDescriptiveTimeSlot(timeSlot);
    }
    
    /**
     * 解析描述性时间段
     */
    private TimeSlotInfo parseDescriptiveTimeSlot(String timeSlot) {
        switch (timeSlot) {
            case "上午":
            case "上午1-2节":
                return new TimeSlotInfo(LocalTime.of(8, 0), LocalTime.of(9, 40), timeSlot);
            case "上午3-4节":
                return new TimeSlotInfo(LocalTime.of(10, 0), LocalTime.of(11, 40), timeSlot);
            case "下午":
            case "下午5-6节":
                return new TimeSlotInfo(LocalTime.of(14, 0), LocalTime.of(15, 40), timeSlot);
            case "下午7-8节":
                return new TimeSlotInfo(LocalTime.of(16, 0), LocalTime.of(17, 40), timeSlot);
            case "晚上":
            case "晚上9-10节":
                return new TimeSlotInfo(LocalTime.of(19, 0), LocalTime.of(20, 40), timeSlot);
            case "晚上11-12节":
                return new TimeSlotInfo(LocalTime.of(21, 0), LocalTime.of(22, 40), timeSlot);
            default:
                log.warn("未知的时间段格式: {}", timeSlot);
                return new TimeSlotInfo(LocalTime.of(8, 0), LocalTime.of(9, 40), timeSlot);
        }
    }
    
    /**
     * 判断课程是否正在进行
     * @param courseDate 课程日期
     * @param timeSlot 时间段
     * @return 课程状态信息
     */
    public CourseStatusInfo getCourseStatus(String courseDate, String timeSlot) {
        try {
            // 解析课程日期
            LocalDate date = LocalDate.parse(courseDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // 解析时间段
            TimeSlotInfo timeSlotInfo = parseTimeSlot(timeSlot);
            if (timeSlotInfo == null) {
                return new CourseStatusInfo(3, "时间段解析失败", 0);
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime courseStart = date.atTime(timeSlotInfo.getStartTime());
            LocalDateTime courseEnd = date.atTime(timeSlotInfo.getEndTime());
            
            // 判断课程状态
            if (now.isBefore(courseStart)) {
                // 即将开始
                long minutes = java.time.Duration.between(now, courseStart).toMinutes();
                return new CourseStatusInfo(1, "即将开始", (int) minutes);
            } else if (now.isAfter(courseEnd)) {
                // 已结束
                long minutes = java.time.Duration.between(courseEnd, now).toMinutes();
                return new CourseStatusInfo(3, "已结束", (int) minutes);
            } else {
                // 正在进行
                long minutes = java.time.Duration.between(now, courseEnd).toMinutes();
                return new CourseStatusInfo(2, "正在进行", (int) minutes);
            }
            
        } catch (DateTimeParseException e) {
            log.error("解析课程日期失败: {}", courseDate, e);
            return new CourseStatusInfo(3, "日期解析失败", 0);
        } catch (Exception e) {
            log.error("获取课程状态失败", e);
            return new CourseStatusInfo(3, "状态获取失败", 0);
        }
    }
    
    /**
     * 时间段信息
     */
    public static class TimeSlotInfo {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String originalText;
        
        public TimeSlotInfo(LocalTime startTime, LocalTime endTime, String originalText) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.originalText = originalText;
        }
        
        public LocalTime getStartTime() {
            return startTime;
        }
        
        public LocalTime getEndTime() {
            return endTime;
        }
        
        public String getOriginalText() {
            return originalText;
        }
    }
    
    /**
     * 课程状态信息
     */
    public static class CourseStatusInfo {
        private final int status; // 1-即将开始，2-正在进行，3-已结束
        private final String description;
        private final int minutesRemaining;
        
        public CourseStatusInfo(int status, String description, int minutesRemaining) {
            this.status = status;
            this.description = description;
            this.minutesRemaining = minutesRemaining;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getMinutesRemaining() {
            return minutesRemaining;
        }
    }
}
