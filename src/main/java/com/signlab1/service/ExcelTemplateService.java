package com.signlab1.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel模板生成服务
 */
@Service
@RequiredArgsConstructor
public class ExcelTemplateService {
    
    /**
     * 生成课程数据导入模板
     */
    public byte[] generateCourseTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("课程数据");
        
        // 创建标题行 - 根据您提供的第二个表格格式
        Row headerRow = sheet.createRow(0);
        String[] headers = {"班级名称", "课程名称", "教师工号", "任课教师", "上课日期", "时间段", "上课地点"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加示例数据 - 基于您提供的第二个表格格式
        String[][] sampleData = {
            {"工程实践B01", "理论", "00005642", "梁祖红", "2024-09-30", "上午", "实验4-215"},
            {"工程实践B01", "物理2", "00006366", "庞玮", "2024-10-14", "上午", "实验4-510"},
            {"工程实践B01", "A组工训1", "", "欧伟程", "2024-10-21", "上午", "实验3-106"},
            {"工程实践B01", "B组工训2", "", "黄亚军", "2024-10-21", "下午", "实验3-106"},
            {"工程实践B01", "电工1", "00008528", "刘跃生", "2024-11-04", "下午", "实验4-205"},
            {"工程实践B01", "电工3", "", "曾思明", "2024-11-18", "下午", "实验3-313"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(sampleData[i][j]);
                
                // 设置数据样式
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256); // 班级名称
        sheet.setColumnWidth(1, 20 * 256); // 课程名称
        sheet.setColumnWidth(2, 15 * 256); // 教师工号
        sheet.setColumnWidth(3, 15 * 256); // 任课教师
        sheet.setColumnWidth(4, 15 * 256); // 上课日期
        sheet.setColumnWidth(5, 12 * 256); // 时间段
        sheet.setColumnWidth(6, 20 * 256); // 上课地点
        
        // 添加说明
        Row noteRow = sheet.createRow(sampleData.length + 3);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：");
        
        Row noteRow2 = sheet.createRow(sampleData.length + 4);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("1. 班级名称：如工程实践B01，系统会自动查找或创建对应班级");
        
        Row noteRow3 = sheet.createRow(sampleData.length + 5);
        Cell noteCell3 = noteRow3.createCell(0);
        noteCell3.setCellValue("2. 教师工号：如00005642，可以为空（系统会自动创建教师账号）");
        
        Row noteRow4 = sheet.createRow(sampleData.length + 6);
        Cell noteCell4 = noteRow4.createCell(0);
        noteCell4.setCellValue("3. 上课日期：格式为yyyy-MM-dd，如2024-09-30");
        
        Row noteRow5 = sheet.createRow(sampleData.length + 7);
        Cell noteCell5 = noteRow5.createCell(0);
        noteCell5.setCellValue("4. 时间段：上午、下午、晚上");
        
        Row noteRow6 = sheet.createRow(sampleData.length + 8);
        Cell noteCell6 = noteRow6.createCell(0);
        noteCell6.setCellValue("5. 系统会自动生成课程ID（格式：KC+年份后2位+6位自增数）");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 生成学生数据导入模板
     */
    public byte[] generateStudentTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生数据");
        
        // 创建标题行 - 根据您提供的第一个表格格式
        Row headerRow = sheet.createRow(0);
        String[] headers = {"班级代码", "学号", "姓名", "院系", "专业"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // 设置标题样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            cell.setCellStyle(headerStyle);
        }
        
        // 添加示例数据 - 基于您提供的第一个表格格式
        String[][] sampleData = {
            {"工程实践B01", "2112503179", "王琛", "信息工程学院", "085402 通信工程"},
            {"工程实践B01", "2112503180", "林佳鑫", "信息工程学院", "085402 通信工程"},
            {"工程实践B01", "2112503181", "秦子桐", "信息工程学院", "085402 通信工程"},
            {"工程实践B01", "2112503219", "邱振邦", "卓越工程师学院", "085402 通信工程"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(sampleData[i][j]);
                
                // 设置数据样式
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256); // 班级代码
        sheet.setColumnWidth(1, 15 * 256); // 学号
        sheet.setColumnWidth(2, 10 * 256); // 姓名
        sheet.setColumnWidth(3, 20 * 256); // 院系
        sheet.setColumnWidth(4, 25 * 256); // 专业
        
        // 添加说明
        Row noteRow = sheet.createRow(sampleData.length + 3);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：");
        
        Row noteRow2 = sheet.createRow(sampleData.length + 4);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("1. 班级代码：如工程实践B01，用于关联班级");
        
        Row noteRow3 = sheet.createRow(sampleData.length + 5);
        Cell noteCell3 = noteRow3.createCell(0);
        noteCell3.setCellValue("2. 学号：10位数字，如2112503179");
        
        Row noteRow4 = sheet.createRow(sampleData.length + 6);
        Cell noteCell4 = noteRow4.createCell(0);
        noteCell4.setCellValue("3. 院系：如信息工程学院、卓越工程师学院");
        
        Row noteRow5 = sheet.createRow(sampleData.length + 7);
        Cell noteCell5 = noteRow5.createCell(0);
        noteCell5.setCellValue("4. 专业：如085402 通信工程");
        
        Row noteRow6 = sheet.createRow(sampleData.length + 8);
        Cell noteCell6 = noteRow6.createCell(0);
        noteCell6.setCellValue("5. 导入学生数据时会自动生成对应的课程安排");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 从学生数据生成课程模板
     * @param studentFile 学生数据文件
     * @param teacherCode 老师工号
     * @return 课程模板Excel文件
     */
    public byte[] generateCourseTemplateFromStudentData(MultipartFile studentFile, String teacherCode) {
        try {
            InputStream inputStream = studentFile.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<CourseTemplateData> courseDataList = new ArrayList<>();
            Set<String> processedSchedules = new HashSet<>();
            
            // 读取学生数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    // 适配实际的学生模板格式
                    String className = getCellValue(row.getCell(0)); // A列：班级名称
                    String studentCode = getCellValue(row.getCell(1)); // B列：学号
                    String studentName = getCellValue(row.getCell(2)); // C列：姓名
                    String department = getCellValue(row.getCell(3)); // D列：院系
                    String major = getCellValue(row.getCell(4)); // E列：专业
                    String teachers = getCellValue(row.getCell(5)); // F列：任课教师
                    String schedule = getCellValue(row.getCell(6)); // G列：上课时间地点
                    
                    System.out.println("处理学生数据: " + studentCode + " " + studentName + " " + className);
                    System.out.println("课表信息: " + schedule);
                    
                    if (studentCode.isEmpty() || className.isEmpty() || schedule.isEmpty()) {
                        System.out.println("跳过空数据行: " + i);
                        continue;
                    }
                    
                    // 解析课表信息
                    if (!schedule.isEmpty() && !processedSchedules.contains(schedule)) {
                        try {
                            // 从班级名称推断课程名称
                            String courseName = className.contains("工程实践") ? "工程实践B" : className;
                            
                            // 解析课表，生成课程数据
                            List<CourseTemplateData> parsedCourses = parseScheduleToCourseTemplate(
                                schedule, courseName, teacherCode, className
                            );
                            
                            courseDataList.addAll(parsedCourses);
                            processedSchedules.add(schedule);
                            
                        } catch (Exception e) {
                            System.err.println("解析课表失败: " + e.getMessage());
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("处理学生数据失败: " + e.getMessage());
                }
            }
            
            workbook.close();
            
            // 生成课程模板Excel
            return generateCourseTemplateFromData(courseDataList);
            
        } catch (IOException e) {
            throw new RuntimeException("处理学生数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 解析课表信息，生成课程模板数据
     */
    private List<CourseTemplateData> parseScheduleToCourseTemplate(String scheduleText, String courseName, String teacherCode, String className) {
        List<CourseTemplateData> courseDataList = new ArrayList<>();
        
        if (scheduleText == null || scheduleText.trim().isEmpty()) {
            return courseDataList;
        }
        
        // 按分号分割不同的上课时间段
        String[] scheduleParts = scheduleText.split(";");
        
        for (String part : scheduleParts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            CourseTemplateData courseData = parseSchedulePartToTemplate(part, courseName, teacherCode, className);
            if (courseData != null) {
                courseDataList.add(courseData);
            }
        }
        
        return courseDataList;
    }
    
    /**
     * 解析单个上课时间段，生成课程模板数据
     */
    private CourseTemplateData parseSchedulePartToTemplate(String schedulePart, String courseName, String teacherCode, String className) {
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
            LocalDate courseDate = calculateCourseDate(weekNumber, dayInEnglish);
            if (courseDate == null) {
                return null;
            }
            
            // 转换节次为时间段
            String timeSlot = convertLessonToTimeSlot(startLesson, endLesson);
            
            // 创建课程模板数据
            CourseTemplateData courseData = new CourseTemplateData();
            courseData.setCourseName(courseName);
            courseData.setTeacherCode(teacherCode);
            courseData.setClassName(className);
            courseData.setLocation(location);
            courseData.setCourseDate(courseDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            courseData.setTimeSlot(timeSlot);
            
            return courseData;
            
        } catch (Exception e) {
            System.err.println("解析上课时间失败: " + schedulePart + ", 错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从课程数据生成课程模板Excel
     */
    private byte[] generateCourseTemplateFromData(List<CourseTemplateData> courseDataList) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("课程数据导入模板");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"课程名称", "授课老师工号", "上课班级", "上课地点", "课程日期", "上课时间段"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 添加课程数据
            for (int i = 0; i < courseDataList.size(); i++) {
                CourseTemplateData courseData = courseDataList.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(courseData.getCourseName());
                row.createCell(1).setCellValue(courseData.getTeacherCode());
                row.createCell(2).setCellValue(courseData.getClassName());
                row.createCell(3).setCellValue(courseData.getLocation());
                row.createCell(4).setCellValue(courseData.getCourseDate());
                row.createCell(5).setCellValue(courseData.getTimeSlot());
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] result = outputStream.toByteArray();
            System.out.println("生成的Excel文件大小: " + result.length + " bytes");
            System.out.println("课程数据条数: " + courseDataList.size());
            
            return result;
            
        } catch (IOException e) {
            throw new RuntimeException("生成课程模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 课程模板数据类
     */
    private static class CourseTemplateData {
        private String courseName;
        private String teacherCode;
        private String className;
        private String location;
        private String courseDate;
        private String timeSlot;
        
        // Getters and Setters
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        
        public String getTeacherCode() { return teacherCode; }
        public void setTeacherCode(String teacherCode) { this.teacherCode = teacherCode; }
        
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getCourseDate() { return courseDate; }
        public void setCourseDate(String courseDate) { this.courseDate = courseDate; }
        
        public String getTimeSlot() { return timeSlot; }
        public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
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
     */
    private LocalDate calculateCourseDate(int weekNumber, String dayOfWeek) {
        // 使用2025年9月1日作为学期开始日期
        LocalDate semesterStart = LocalDate.of(2025, 9, 1);
        
        // 计算目标周次相对于基准日期的偏移
        int weekOffset = weekNumber - 1; // 第1周从基准日期开始
        
        // 计算目标日期
        LocalDate targetDate = semesterStart.plusWeeks(weekOffset);
        
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
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        // 使用DataFormatter获取单元格的显示值，这样可以保持原始格式
        DataFormatter dataFormatter = new DataFormatter();
        return dataFormatter.formatCellValue(cell).trim();
    }
}

