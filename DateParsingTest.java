import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateParsingTest {
    public static void main(String[] args) {
        // 测试日期解析
        String[] testDates = {
            "9/28/24",      // 你的Excel格式
            "2024/9/28",    // 另一种格式
            "09/28/2024",   // 完整格式
            "2024-09-28",   // 标准格式
            "9-28-24"       // 横线分隔
        };
        
        System.out.println("=== 日期解析测试 ===");
        for (String dateStr : testDates) {
            String result = parseDateString(dateStr);
            System.out.println("输入: " + dateStr + " -> 输出: " + result);
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
            "MM-dd-yyyy"   // 09-28-2024
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate date = LocalDate.parse(dateStr, formatter);
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，返回原字符串
        System.err.println("无法解析日期格式: " + dateStr);
        return dateStr;
    }
}
