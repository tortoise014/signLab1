package com.signlab1.service;

import com.signlab1.dto.ClassPhotoDto;
import com.signlab1.entity.Course;
import com.signlab1.entity.User;
import com.signlab1.mapper.ClassPhotoMapper;
import com.signlab1.mapper.CourseMapper;
import com.signlab1.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Word文档生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordDocumentService {
    
    private final ClassPhotoMapper classPhotoMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    
    /**
     * 生成学生课堂笔记Word文档
     * @param studentCode 学生学号
     * @param courseId 课程ID
     * @return Word文档的字节数组
     */
    public byte[] generateClassNotesDocument(String studentCode, String courseId) {
        try {
            // 1. 获取学生信息
            QueryWrapper<User> userQuery = new QueryWrapper<>();
            userQuery.eq("username", studentCode);
            User student = userMapper.selectOne(userQuery);
            if (student == null) {
                throw new RuntimeException("学生不存在: " + studentCode);
            }
            
            // 2. 获取课程信息
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在: " + courseId);
            }
            
            // 3. 获取学生的课程照片
            List<ClassPhotoDto> photos = getStudentCoursePhotos(studentCode, courseId);
            
            // 4. 创建Word文档
            XWPFDocument document = new XWPFDocument();
            
            // 5. 添加文档标题
            addDocumentTitle(document, student, course);
            
            // 6. 添加课程信息
            addCourseInfo(document, course);
            
            // 7. 添加照片和备注
            addPhotosAndRemarks(document, photos);
            
            // 8. 添加文档结尾
            addDocumentFooter(document);
            
            // 9. 转换为字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.write(outputStream);
            document.close();
            
            log.info("Word文档生成成功: 学生={}, 课程={}, 照片数量={}", 
                    studentCode, courseId, photos.size());
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Word文档生成失败: {}", e.getMessage(), e);
            throw new RuntimeException("Word文档生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加文档标题
     */
    private void addDocumentTitle(XWPFDocument document, User student, Course course) {
        // 创建标题段落
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("课堂笔记");
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("宋体");
        
        // 添加空行
        document.createParagraph();
        
        // 创建副标题段落
        XWPFParagraph subtitleParagraph = document.createParagraph();
        subtitleParagraph.setAlignment(ParagraphAlignment.CENTER);
        
        XWPFRun subtitleRun = subtitleParagraph.createRun();
        subtitleRun.setText("学生：" + student.getName() + " (" + student.getUsername() + ")");
        subtitleRun.setFontSize(14);
        subtitleRun.setFontFamily("宋体");
        
        // 添加空行
        document.createParagraph();
    }
    
    /**
     * 添加课程信息
     */
    private void addCourseInfo(XWPFDocument document, Course course) {
        // 课程信息标题
        XWPFParagraph infoTitleParagraph = document.createParagraph();
        XWPFRun infoTitleRun = infoTitleParagraph.createRun();
        infoTitleRun.setText("课程信息");
        infoTitleRun.setBold(true);
        infoTitleRun.setFontSize(14);
        infoTitleRun.setFontFamily("宋体");
        
        // 课程信息内容
        XWPFParagraph infoParagraph = document.createParagraph();
        infoParagraph.setIndentationLeft(400); // 左缩进
        
        XWPFRun infoRun = infoParagraph.createRun();
        infoRun.setText("课程名称：" + course.getCourseName() + "\n");
        infoRun.setText("课程ID：" + course.getCourseId() + "\n");
        infoRun.setText("上课日期：" + course.getCourseDate() + "\n");
        infoRun.setText("上课时间：" + course.getTimeSlot() + "\n");
        infoRun.setText("上课地点：" + (course.getLocation() != null ? course.getLocation() : "未指定") + "\n");
        infoRun.setFontSize(12);
        infoRun.setFontFamily("宋体");
        
        // 添加空行
        document.createParagraph();
    }
    
    /**
     * 添加照片和备注
     */
    private void addPhotosAndRemarks(XWPFDocument document, List<ClassPhotoDto> photos) {
        if (photos.isEmpty()) {
            // 没有照片时的提示
            XWPFParagraph noPhotoParagraph = document.createParagraph();
            XWPFRun noPhotoRun = noPhotoParagraph.createRun();
            noPhotoRun.setText("本节课未上传照片");
            noPhotoRun.setFontSize(12);
            noPhotoRun.setFontFamily("宋体");
            noPhotoRun.setColor("666666");
            return;
        }
        
        // 照片和备注标题
        XWPFParagraph photosTitleParagraph = document.createParagraph();
        XWPFRun photosTitleRun = photosTitleParagraph.createRun();
        photosTitleRun.setText("课堂照片与笔记");
        photosTitleRun.setBold(true);
        photosTitleRun.setFontSize(14);
        photosTitleRun.setFontFamily("宋体");
        
        // 添加空行
        document.createParagraph();
        
        // 遍历照片
        for (int i = 0; i < photos.size(); i++) {
            ClassPhotoDto photo = photos.get(i);
            
            // 照片序号和上传时间
            XWPFParagraph photoInfoParagraph = document.createParagraph();
            XWPFRun photoInfoRun = photoInfoParagraph.createRun();
            photoInfoRun.setText("照片 " + (i + 1) + " - 上传时间：" + 
                    photo.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            photoInfoRun.setBold(true);
            photoInfoRun.setFontSize(12);
            photoInfoRun.setFontFamily("宋体");
            
            // 添加照片（如果文件存在）
            try {
                addPhotoToDocument(document, photo);
            } catch (Exception e) {
                log.warn("添加照片失败: {}", e.getMessage());
                // 添加照片加载失败的提示
                XWPFParagraph errorParagraph = document.createParagraph();
                XWPFRun errorRun = errorParagraph.createRun();
                errorRun.setText("[照片文件不存在或无法加载: " + photo.getPhotoName() + "]");
                errorRun.setFontSize(10);
                errorRun.setFontFamily("宋体");
                errorRun.setColor("FF0000");
            }
            
            // 添加备注
            if (photo.getRemark() != null && !photo.getRemark().trim().isEmpty()) {
                XWPFParagraph remarkParagraph = document.createParagraph();
                remarkParagraph.setIndentationLeft(400); // 左缩进
                
                XWPFRun remarkRun = remarkParagraph.createRun();
                remarkRun.setText("备注：" + photo.getRemark());
                remarkRun.setFontSize(12);
                remarkRun.setFontFamily("宋体");
                remarkRun.setColor("333333");
            }
            
            // 添加分隔线
            if (i < photos.size() - 1) {
                XWPFParagraph separatorParagraph = document.createParagraph();
                XWPFRun separatorRun = separatorParagraph.createRun();
                separatorRun.setText("────────────────────────────────────────");
                separatorRun.setFontSize(10);
                separatorRun.setFontFamily("宋体");
                separatorRun.setColor("CCCCCC");
            }
            
            // 添加空行
            document.createParagraph();
        }
    }
    
    /**
     * 添加照片到文档
     */
    private void addPhotoToDocument(XWPFDocument document, ClassPhotoDto photo) throws IOException, InvalidFormatException {
        // 优先使用压缩图，如果不存在则使用原图
        String imagePath = photo.getCompressedPhotoPath();
        if (imagePath == null || !Files.exists(Paths.get(imagePath))) {
            imagePath = photo.getPhotoPath();
        }
        
        // 检查照片文件是否存在
        Path photoPath = Paths.get(imagePath);
        if (!Files.exists(photoPath)) {
            throw new IOException("照片文件不存在: " + imagePath);
        }
        
        // 读取照片文件
        byte[] photoBytes = Files.readAllBytes(photoPath);
        
        // 创建段落
        XWPFParagraph photoParagraph = document.createParagraph();
        photoParagraph.setAlignment(ParagraphAlignment.CENTER);
        
        // 添加照片
        XWPFRun photoRun = photoParagraph.createRun();
        photoRun.addPicture(
                new java.io.ByteArrayInputStream(photoBytes),
                getPictureType(photo.getPhotoName()),
                photo.getPhotoName(),
                Units.toEMU(400), // 宽度
                Units.toEMU(300)  // 高度
        );
    }
    
    /**
     * 获取图片类型
     */
    private int getPictureType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return XWPFDocument.PICTURE_TYPE_JPEG;
            case "png":
                return XWPFDocument.PICTURE_TYPE_PNG;
            case "gif":
                return XWPFDocument.PICTURE_TYPE_GIF;
            default:
                return XWPFDocument.PICTURE_TYPE_JPEG;
        }
    }
    
    /**
     * 添加文档结尾
     */
    private void addDocumentFooter(XWPFDocument document) {
        // 添加空行
        document.createParagraph();
        document.createParagraph();
        
        // 创建结尾段落
        XWPFParagraph footerParagraph = document.createParagraph();
        footerParagraph.setAlignment(ParagraphAlignment.RIGHT);
        
        XWPFRun footerRun = footerParagraph.createRun();
        footerRun.setText("生成时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        footerRun.setFontSize(10);
        footerRun.setFontFamily("宋体");
        footerRun.setColor("666666");
    }
    
    /**
     * 获取学生的课程照片
     */
    private List<ClassPhotoDto> getStudentCoursePhotos(String studentCode, String courseId) {
        // 这里需要实现获取学生课程照片的逻辑
        // 由于ClassPhotoDto需要关联查询课程名称，这里简化处理
        QueryWrapper<com.signlab1.entity.ClassPhoto> query = new QueryWrapper<>();
        query.eq("student_username", studentCode)
             .eq("course_id", courseId)
             .orderByAsc("upload_time");
        
        List<com.signlab1.entity.ClassPhoto> photos = classPhotoMapper.selectList(query);
        
        // 转换为DTO
        return photos.stream().map(photo -> {
            ClassPhotoDto dto = new ClassPhotoDto();
            dto.setId(photo.getId());
            dto.setCourseId(photo.getCourseId());
            dto.setStudentUsername(photo.getStudentUsername());
            dto.setPhotoName(photo.getPhotoName());
            dto.setPhotoPath(photo.getPhotoPath());
            dto.setCompressedPhotoPath(photo.getCompressedPhotoPath());
            dto.setRemark(photo.getRemark());
            dto.setFileSize(photo.getFileSize());
            dto.setUploadTime(photo.getUploadTime());
            return dto;
        }).toList();
    }
}
