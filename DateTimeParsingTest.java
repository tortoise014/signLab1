import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeParsingTest {
    public static void main(String[] args) {
        // 测试日期解析
        String[] testDates = {
            "9月30日",      // 你的Excel格式
            "10月14日",     // 另一种格式
            "10月21日",     // 完整格式
            "9/30/24",      // 标准格式
            "2024-09-30"    // 已有年份格式
        };
        
        System.out.println("=== 日期解析测试（强制2025年）===");
        for (String dateStr : testDates) {
            String result = parseDateString(dateStr);
            System.out.println("输入: " + dateStr + " -> 输出: " + result);
        }
        
        // 测试时间段解析
        String[] testTimeSlots = {
            "上午",          // 你的Excel格式
            "下午",          // 另一种格式
            "08:30-12:00",  // 已有具体时间
            "14:40-18:05"   // 已有具体时间
        };
        
        System.out.println("\n=== 时间段解析测试 ===");
        for (String timeSlot : testTimeSlots) {
            String result = parseTimeSlot(timeSlot);
            System.out.println("输入: " + timeSlot + " -> 输出: " + result);
        }
    }
    
    private static String parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim();
        
        // 定义多种可能的日期格式
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
            "MM-dd-yyyy",  // 09-28-2024
            "M月d日",      // 9月30日
            "MM月dd日"     // 09月30日
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
        
        // 如果所有格式都失败，返回原字符串
        System.err.println("无法解析日期格式: " + dateStr);
        return dateStr;
    }
    
    private static String parseTimeSlot(String timeSlotStr) {
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
}
