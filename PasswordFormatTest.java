// 测试密码格式生成
public class PasswordFormatTest {
    public static void main(String[] args) {
        // 测试学生密码格式
        String studentCode1 = "3123004155";
        String studentCode2 = "123";
        String studentCode3 = "2024001";
        
        // 测试教师密码格式
        String teacherCode1 = "00005642";
        String teacherCode2 = "123";
        String teacherCode3 = "T2024001";
        
        System.out.println("=== 学生密码格式测试 ===");
        System.out.println("学号: " + studentCode1 + " -> 密码: " + generateStudentPassword(studentCode1));
        System.out.println("学号: " + studentCode2 + " -> 密码: " + generateStudentPassword(studentCode2));
        System.out.println("学号: " + studentCode3 + " -> 密码: " + generateStudentPassword(studentCode3));
        
        System.out.println("\n=== 教师密码格式测试 ===");
        System.out.println("工号: " + teacherCode1 + " -> 密码: " + generateTeacherPassword(teacherCode1));
        System.out.println("工号: " + teacherCode2 + " -> 密码: " + generateTeacherPassword(teacherCode2));
        System.out.println("工号: " + teacherCode3 + " -> 密码: " + generateTeacherPassword(teacherCode3));
    }
    
    private static String generateStudentPassword(String studentCode) {
        return "syjx@" + (studentCode.length() >= 4 ? 
            studentCode.substring(studentCode.length() - 4) : studentCode);
    }
    
    private static String generateTeacherPassword(String teacherCode) {
        return "syjx@" + (teacherCode.length() >= 4 ? 
            teacherCode.substring(teacherCode.length() - 4) : teacherCode);
    }
}
