package com.signlab1.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Excel模板生成服务
 */
@Service
@RequiredArgsConstructor
public class ExcelTemplateService {
    
    /**
     * 生成用户数据导入模板
     */
    public byte[] generateUserTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("用户数据");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"学号/工号", "姓名", "角色"};
        String[] roleOptions = {"student", "teacher", "admin"};
        
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
        
        // 添加示例数据
        String[][] sampleData = {
            {"T001", "张老师", "teacher"},
            {"T002", "李老师", "teacher"},
            {"S001", "张三", "student"},
            {"S002", "李四", "student"},
            {"S003", "王五", "student"},
            {"admin", "管理员", "admin"}
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
        sheet.setColumnWidth(0, 15 * 256); // 学号/工号
        sheet.setColumnWidth(1, 20 * 256); // 姓名
        sheet.setColumnWidth(2, 15 * 256); // 角色
        
        // 添加说明
        Row noteRow = sheet.createRow(sampleData.length + 3);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：");
        
        Row noteRow2 = sheet.createRow(sampleData.length + 4);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("1. 学号/工号：唯一标识，不能重复");
        
        Row noteRow3 = sheet.createRow(sampleData.length + 5);
        Cell noteCell3 = noteRow3.createCell(0);
        noteCell3.setCellValue("2. 角色：student(学生)、teacher(老师)、admin(管理员)");
        
        Row noteRow4 = sheet.createRow(sampleData.length + 6);
        Cell noteCell4 = noteRow4.createCell(0);
        noteCell4.setCellValue("3. 导入后用户初始密码为空，首次登录需设置密码");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 生成班级数据导入模板
     */
    public byte[] generateClassTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("班级数据");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"班级编号", "班级名称", "班级人数"};
        
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
        
        // 添加示例数据
        String[][] sampleData = {
            {"202101", "计算机2021-1班", "30"},
            {"202102", "计算机2021-2班", "28"},
            {"202201", "软件2022-1班", "32"},
            {"202202", "软件2022-2班", "29"},
            {"202301", "网络2023-1班", "31"}
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
        sheet.setColumnWidth(0, 15 * 256); // 班级编号
        sheet.setColumnWidth(1, 25 * 256); // 班级名称
        sheet.setColumnWidth(2, 15 * 256); // 班级人数
        
        // 添加说明
        Row noteRow = sheet.createRow(sampleData.length + 3);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：");
        
        Row noteRow2 = sheet.createRow(sampleData.length + 4);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("1. 班级编号：6位数字，唯一标识");
        
        Row noteRow3 = sheet.createRow(sampleData.length + 5);
        Cell noteCell3 = noteRow3.createCell(0);
        noteCell3.setCellValue("2. 班级人数：用于签到统计");
        
        Row noteRow4 = sheet.createRow(sampleData.length + 6);
        Cell noteCell4 = noteRow4.createCell(0);
        noteCell4.setCellValue("3. 系统会自动生成6位验证码用于学生绑定");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 生成课程数据导入模板
     */
    public byte[] generateCourseTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("课程数据");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"课程名称", "授课老师工号", "上课班级", "上课地点", "课程日期", "上课时间段", "课程周次"};
        
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
        
        // 添加示例数据
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String[][] sampleData = {
            {"数据结构与算法", "T001", "202101", "教学楼A101", today, "08:00-09:40", "1"},
            {"Java程序设计", "T002", "202102", "教学楼B201", today, "10:00-11:40", "1"},
            {"数据库原理", "T001", "202201", "教学楼C301", today, "14:00-15:40", "1"},
            {"操作系统", "T003", "202202", "教学楼D401", today, "16:00-17:40", "1"},
            {"计算机网络", "T004", "202301", "教学楼E501", today, "19:00-20:40", "1"}
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
        sheet.setColumnWidth(0, 25 * 256); // 课程名称
        sheet.setColumnWidth(1, 18 * 256); // 授课老师工号
        sheet.setColumnWidth(2, 15 * 256); // 上课班级
        sheet.setColumnWidth(3, 20 * 256); // 上课地点
        sheet.setColumnWidth(4, 15 * 256); // 课程日期
        sheet.setColumnWidth(5, 18 * 256); // 上课时间段
        sheet.setColumnWidth(6, 15 * 256); // 课程周次
        
        // 添加说明
        Row noteRow = sheet.createRow(sampleData.length + 3);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：");
        
        Row noteRow2 = sheet.createRow(sampleData.length + 4);
        Cell noteCell2 = noteRow2.createCell(0);
        noteCell2.setCellValue("1. 授课老师工号：必须是已导入的老师工号");
        
        Row noteRow3 = sheet.createRow(sampleData.length + 5);
        Cell noteCell3 = noteRow3.createCell(0);
        noteCell3.setCellValue("2. 上课班级：必须是已导入的班级编号");
        
        Row noteRow4 = sheet.createRow(sampleData.length + 6);
        Cell noteCell4 = noteRow4.createCell(0);
        noteCell4.setCellValue("3. 课程日期：格式为yyyy-MM-dd，如2024-01-15");
        
        Row noteRow5 = sheet.createRow(sampleData.length + 7);
        Cell noteCell5 = noteRow5.createCell(0);
        noteCell5.setCellValue("4. 上课时间段：格式为HH:mm-HH:mm，如08:00-09:40");
        
        Row noteRow6 = sheet.createRow(sampleData.length + 8);
        Cell noteCell6 = noteRow6.createCell(0);
        noteCell6.setCellValue("5. 系统会自动生成课程ID（格式：KC+年份后2位+6位自增数）");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
}
