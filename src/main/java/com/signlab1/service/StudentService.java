package com.signlab1.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.signlab1.dto.*;
import com.signlab1.entity.AttendanceRecord;
import com.signlab1.entity.Class;
import com.signlab1.entity.ClassPhoto;
import com.signlab1.entity.Course;
import com.signlab1.entity.StudentClassRelation;
import com.signlab1.entity.User;
import com.signlab1.mapper.*;
import com.signlab1.util.FileUploadUtil;
import com.signlab1.util.QrCodeUtil;
import com.signlab1.util.TimeSlotParser;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ClassPhotoMapper classPhotoMapper;
    private final QrCodeUtil qrCodeUtil;
    private final FileUploadUtil fileUploadUtil;
    private final WordDocumentService wordDocumentService;
    private final TimeSlotParser timeSlotParser;
    
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
            String isMultiClass = qrContent.get("isMultiClass");
            String timestampStr = qrContent.get("timestamp");
            
            // 2. 验证二维码时效性（30秒有效期）
            Long timestamp = Long.parseLong(timestampStr);
            
            // 如果是测试课程，跳过时间验证
            if ("TEST_COURSE".equals(courseId)) {
                // 跳过时间验证，继续执行
            } else {
                // 正常的时间验证
                if (!qrCodeUtil.isQrCodeValid(timestamp, 30)) {
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
            
            // 4. 智能签到逻辑：优先记录到学生自己的课程，否则记录为跨班签到
            String actualCourseId = courseId; // 默认记录到当前课程
            String actualClassCode = classCode; // 默认记录到当前班级
            boolean isCrossClassAttendance = false;
            
            // 查找学生是否有同一老师同一时间的课程
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode).eq("is_deleted", 0);
            List<StudentClassRelation> studentRelations = studentClassRelationMapper.selectList(relationQuery);
            
            if (!studentRelations.isEmpty()) {
                Set<String> studentClassCodes = studentRelations.stream()
                    .map(StudentClassRelation::getClassCode)
                    .collect(Collectors.toSet());
                
                // 查找同一老师同一时间的课程
                QueryWrapper<Course> sameTimeQuery = new QueryWrapper<>();
                sameTimeQuery.eq("teacher_username", course.getTeacherUsername())
                            .eq("course_date", course.getCourseDate())
                            .eq("time_slot", course.getTimeSlot());
                List<Course> sameTimeCourses = courseMapper.selectList(sameTimeQuery);
                
                // 检查学生是否有自己的课程
                for (Course sameTimeCourse : sameTimeCourses) {
                    if (studentClassCodes.contains(sameTimeCourse.getClassCode())) {
                        // 找到学生自己的课程，记录到自己的课程
                        actualCourseId = sameTimeCourse.getCourseId();
                        actualClassCode = sameTimeCourse.getClassCode();
                        isCrossClassAttendance = false;
                        break;
                    }
                }
                
                // 如果没找到学生自己的课程，则记录为跨班签到
                if (actualCourseId.equals(courseId)) {
                    isCrossClassAttendance = true;
                    // 使用学生的第一个班级作为实际班级
                    actualClassCode = studentRelations.get(0).getClassCode();
                }
            } else {
                // 学生没有绑定班级，不允许签到
                throw new RuntimeException("您尚未绑定班级，无法签到");
            }
            
            // 5. 检查是否已签到（检查实际要记录的课程）
            QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
            attendanceQuery.eq("course_id", actualCourseId)
                          .eq("student_username", studentCode);
            AttendanceRecord existingRecord = attendanceRecordMapper.selectOne(attendanceQuery);
            if (existingRecord != null) {
                throw new RuntimeException("您已经签到过了");
            }
            
            // 6. 创建签到记录
            AttendanceRecord record = new AttendanceRecord();
            record.setCourseId(actualCourseId); // 使用实际的课程ID
            record.setStudentUsername(studentCode);
            record.setAttendanceTime(LocalDateTime.now());
            record.setAttendanceStatus(1); // 1-已签到
            
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
            
            // 添加签到类型信息
            if (isCrossClassAttendance) {
                result.setMessage("跨班签到成功！您的签到记录已保存到您自己的课程中。");
            } else {
                result.setMessage("签到成功！");
            }
            
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
            
            // 简化处理：不再计算签到率，因为学生可以签到任何课程
            // 签到率设为100%，表示已签到的课程都是有效的
            stats.setAttendanceRate(100.0);
            
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
    
    /**
     * 上传课堂照片
     */
    public PhotoUploadResponse uploadClassPhoto(String studentCode, String courseId, MultipartFile file, String remark) {
        try {
            // 1. 验证课程是否存在
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", courseId);
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                throw new RuntimeException("课程不存在");
            }
            
            // 2. 注释掉班级验证，允许任何学生上传照片
            // 这样设计更灵活，支持跨班课程、旁听生、助教等场景
            // QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            // relationQuery.eq("student_username", studentCode)
            //             .eq("class_code", course.getClassCode());
            // StudentClassRelation relation = studentClassRelationMapper.selectOne(relationQuery);
            // if (relation == null) {
            //     throw new RuntimeException("您不属于该课程班级，无法上传照片");
            // }
            
            // 3. 上传文件
            FileUploadUtil.FileUploadResult uploadResult = fileUploadUtil.uploadClassPhoto(file, courseId, studentCode, remark);
            
            // 4. 保存照片记录到数据库
            ClassPhoto classPhoto = new ClassPhoto();
            classPhoto.setCourseId(courseId);
            classPhoto.setStudentUsername(studentCode);
            classPhoto.setPhotoName(uploadResult.getFileName());
            classPhoto.setPhotoPath(uploadResult.getFilePath());
            classPhoto.setRemark(remark);
            classPhoto.setFileSize(uploadResult.getFileSize());
            classPhoto.setUploadTime(uploadResult.getUploadTime());
            
            classPhotoMapper.insert(classPhoto);
            
            // 5. 构建返回结果
            PhotoUploadResponse response = new PhotoUploadResponse();
            response.setPhotoId(classPhoto.getId());
            response.setPhotoName(uploadResult.getFileName());
            response.setPhotoUrl("/api/student/photo/" + classPhoto.getId()); // 照片访问URL
            response.setFileSize(uploadResult.getFileSize());
            response.setUploadTime(uploadResult.getUploadTime());
            response.setRemark(remark);
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("照片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生的课堂照片列表
     */
    public List<ClassPhotoDto> getStudentPhotos(String studentCode) {
        try {
            QueryWrapper<ClassPhoto> query = new QueryWrapper<>();
            query.eq("student_username", studentCode)
                .orderByDesc("upload_time");
            
            List<ClassPhoto> photos = classPhotoMapper.selectList(query);
            
            return photos.stream().map(photo -> {
                ClassPhotoDto dto = new ClassPhotoDto();
                dto.setId(photo.getId());
                dto.setCourseId(photo.getCourseId());
                dto.setStudentUsername(photo.getStudentUsername());
                dto.setPhotoName(photo.getPhotoName());
                dto.setPhotoUrl("/api/student/photo/" + photo.getId());
                dto.setRemark(photo.getRemark());
                dto.setFileSize(photo.getFileSize());
                dto.setUploadTime(photo.getUploadTime());
                
                // 获取课程信息
                QueryWrapper<Course> courseQuery = new QueryWrapper<>();
                courseQuery.eq("course_id", photo.getCourseId());
                Course course = courseMapper.selectOne(courseQuery);
                if (course != null) {
                    dto.setCourseName(course.getCourseName());
                }
                
                // 获取学生信息
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.eq("username", photo.getStudentUsername());
                User student = userMapper.selectOne(userQuery);
                if (student != null) {
                    dto.setStudentName(student.getName());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取照片列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID获取学生的课堂照片
     */
    public List<ClassPhotoDto> getStudentPhotosByCourse(String studentCode, String courseId) {
        try {
            QueryWrapper<ClassPhoto> query = new QueryWrapper<>();
            query.eq("student_username", studentCode)
                .eq("course_id", courseId)
                .orderByDesc("upload_time");
            
            List<ClassPhoto> photos = classPhotoMapper.selectList(query);
            
            return photos.stream().map(photo -> {
                ClassPhotoDto dto = new ClassPhotoDto();
                dto.setId(photo.getId());
                dto.setCourseId(photo.getCourseId());
                dto.setStudentUsername(photo.getStudentUsername());
                dto.setPhotoName(photo.getPhotoName());
                dto.setPhotoUrl("/api/student/photo/" + photo.getId());
                dto.setRemark(photo.getRemark());
                dto.setFileSize(photo.getFileSize());
                dto.setUploadTime(photo.getUploadTime());
                
                // 获取课程信息
                QueryWrapper<Course> courseQuery = new QueryWrapper<>();
                courseQuery.eq("course_id", photo.getCourseId());
                Course course = courseMapper.selectOne(courseQuery);
                if (course != null) {
                    dto.setCourseName(course.getCourseName());
                }
                
                // 获取学生信息
                QueryWrapper<User> userQuery = new QueryWrapper<>();
                userQuery.eq("username", photo.getStudentUsername());
                User student = userMapper.selectOne(userQuery);
                if (student != null) {
                    dto.setStudentName(student.getName());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取课程照片失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课堂照片
     */
    public void deleteClassPhoto(String studentCode, Long photoId) {
        try {
            // 1. 查询照片记录
            ClassPhoto photo = classPhotoMapper.selectById(photoId);
            if (photo == null) {
                throw new RuntimeException("照片不存在");
            }
            
            // 2. 验证权限（只能删除自己的照片）
            if (!studentCode.equals(photo.getStudentUsername())) {
                throw new RuntimeException("您只能删除自己的照片");
            }
            
            // 3. 删除物理文件
            fileUploadUtil.deleteFile(photo.getPhotoPath());
            
            // 4. 删除数据库记录
            classPhotoMapper.deleteById(photoId);
            
        } catch (Exception e) {
            throw new RuntimeException("删除照片失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取照片信息
     */
    public ClassPhoto getClassPhotoById(Long photoId) {
        return classPhotoMapper.selectById(photoId);
    }
    
    /**
     * 生成课堂笔记Word文档
     */
    public byte[] generateClassNotesDocument(String studentCode, String courseId) {
        return wordDocumentService.generateClassNotesDocument(studentCode, courseId);
    }
    
    /**
     * 获取正在进行的课程
     */
    public List<CurrentCourseDto> getCurrentCourses() {
        try {
            // 获取今天的课程
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            QueryWrapper<Course> query = new QueryWrapper<>();
            query.eq("course_date", todayStr)
                 .orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(query);
            
            return courses.stream().map(course -> {
                CurrentCourseDto dto = new CurrentCourseDto();
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setTeacherUsername(course.getTeacherUsername());
                dto.setClassCode(course.getClassCode());
                dto.setLocation(course.getLocation());
                dto.setCourseDate(course.getCourseDate());
                dto.setTimeSlot(course.getTimeSlot());
                
                // 获取课程状态
                TimeSlotParser.CourseStatusInfo statusInfo = timeSlotParser.getCourseStatus(
                    course.getCourseDate(), course.getTimeSlot());
                dto.setCourseStatus(statusInfo.getStatus());
                dto.setStatusDescription(statusInfo.getDescription());
                dto.setMinutesRemaining(statusInfo.getMinutesRemaining());
                
                // 获取教师信息
                QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                teacherQuery.eq("username", course.getTeacherUsername());
                User teacher = userMapper.selectOne(teacherQuery);
                if (teacher != null) {
                    dto.setTeacherName(teacher.getName());
                }
                
                // 获取班级信息
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", course.getClassCode());
                Class clazz = classMapper.selectOne(classQuery);
                if (clazz != null) {
                    dto.setClassName(clazz.getClassName());
                    dto.setTotalStudents(clazz.getStudentCount());
                }
                
                // 获取签到人数
                QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
                attendanceQuery.eq("course_id", course.getCourseId());
                long attendanceCount = attendanceRecordMapper.selectCount(attendanceQuery);
                dto.setAttendanceCount((int) attendanceCount);
                
                // 计算签到率
                if (dto.getTotalStudents() != null && dto.getTotalStudents() > 0) {
                    double rate = (double) dto.getAttendanceCount() / dto.getTotalStudents() * 100;
                    dto.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
                } else {
                    dto.setAttendanceRate(0.0);
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取当前课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取最近的课程（今天和昨天）
     */
    public List<RecentCourseDto> getRecentCourses() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String yesterdayStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            QueryWrapper<Course> query = new QueryWrapper<>();
            query.in("course_date", todayStr, yesterdayStr)
                 .orderByAsc("course_date")
                 .orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(query);
            
            return courses.stream().map(course -> {
                RecentCourseDto dto = new RecentCourseDto();
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setTeacherUsername(course.getTeacherUsername());
                dto.setClassCode(course.getClassCode());
                dto.setLocation(course.getLocation());
                dto.setCourseDate(course.getCourseDate());
                dto.setTimeSlot(course.getTimeSlot());
                
                // 获取课程状态
                TimeSlotParser.CourseStatusInfo statusInfo = timeSlotParser.getCourseStatus(
                    course.getCourseDate(), course.getTimeSlot());
                dto.setCourseStatus(statusInfo.getStatus());
                dto.setStatusDescription(statusInfo.getDescription());
                
                // 获取教师信息
                QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                teacherQuery.eq("username", course.getTeacherUsername());
                User teacher = userMapper.selectOne(teacherQuery);
                if (teacher != null) {
                    dto.setTeacherName(teacher.getName());
                }
                
                // 获取班级信息
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", course.getClassCode());
                Class clazz = classMapper.selectOne(classQuery);
                if (clazz != null) {
                    dto.setClassName(clazz.getClassName());
                    dto.setTotalStudents(clazz.getStudentCount());
                }
                
                // 获取签到人数
                QueryWrapper<AttendanceRecord> attendanceQuery = new QueryWrapper<>();
                attendanceQuery.eq("course_id", course.getCourseId());
                long attendanceCount = attendanceRecordMapper.selectCount(attendanceQuery);
                dto.setAttendanceCount((int) attendanceCount);
                
                // 计算签到率
                if (dto.getTotalStudents() != null && dto.getTotalStudents() > 0) {
                    double rate = (double) dto.getAttendanceCount() / dto.getTotalStudents() * 100;
                    dto.setAttendanceRate(Math.round(rate * 100.0) / 100.0);
                } else {
                    dto.setAttendanceRate(0.0);
                }
                
                // 设置时间描述
                LocalDate courseDate = LocalDate.parse(course.getCourseDate());
                if (courseDate.equals(today)) {
                    dto.setTimeDescription("今天");
                } else if (courseDate.equals(yesterday)) {
                    dto.setTimeDescription("昨天");
                } else {
                    dto.setTimeDescription(course.getCourseDate());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取最近课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生的所有课程
     */
    public List<CourseInfoDto> getStudentCourses(String studentCode) {
        try {
            // 获取学生绑定的班级
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            if (relations.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 获取班级代码列表
            List<String> classCodes = relations.stream()
                    .map(StudentClassRelation::getClassCode)
                    .collect(Collectors.toList());
            
            // 查询这些班级的课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.in("class_code", classCodes)
                    .orderByAsc("course_date")
                    .orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            // 性能优化：批量查询班级和教师信息
            Set<String> courseClassCodes = courses.stream()
                    .map(Course::getClassCode)
                    .collect(Collectors.toSet());
            Set<String> teacherUsernames = courses.stream()
                    .map(Course::getTeacherUsername)
                    .filter(username -> username != null && !username.isEmpty())
                    .collect(Collectors.toSet());
            
            // 批量查询班级信息
            Map<String, String> classNameMap = new HashMap<>();
            if (!courseClassCodes.isEmpty()) {
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.in("class_code", courseClassCodes);
                List<Class> classes = classMapper.selectList(classQuery);
                classNameMap = classes.stream()
                        .collect(Collectors.toMap(Class::getClassCode, Class::getClassName));
            }
            
            // 批量查询教师信息
            Map<String, String> teacherNameMap = new HashMap<>();
            if (!teacherUsernames.isEmpty()) {
                QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                teacherQuery.in("username", teacherUsernames);
                List<User> teachers = userMapper.selectList(teacherQuery);
                teacherNameMap = teachers.stream()
                        .collect(Collectors.toMap(User::getUsername, User::getName));
            }
            
            // 构建最终结果
            final Map<String, String> finalClassNameMap = classNameMap;
            final Map<String, String> finalTeacherNameMap = teacherNameMap;
            
            return courses.stream().map(course -> {
                CourseInfoDto dto = new CourseInfoDto();
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setTimeSlot(course.getTimeSlot());
                dto.setLocation(course.getLocation());
                dto.setCourseDate(course.getCourseDate());
                dto.setTeacherUsername(course.getTeacherUsername());
                dto.setClassCode(course.getClassCode());
                
                // 使用缓存的班级名称
                dto.setClassName(finalClassNameMap.getOrDefault(course.getClassCode(), course.getClassCode()));
                
                // 使用缓存的教师姓名
                String teacherName = finalTeacherNameMap.get(course.getTeacherUsername());
                dto.setTeacherName(teacherName != null ? teacherName : course.getTeacherUsername());
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取学生课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 按日期获取学生的课程
     */
    public List<CourseInfoDto> getStudentCoursesByDate(String studentCode, String date) {
        try {
            // 获取学生绑定的班级
            QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
            relationQuery.eq("student_username", studentCode);
            List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
            
            if (relations.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 获取班级代码列表
            List<String> classCodes = relations.stream()
                    .map(StudentClassRelation::getClassCode)
                    .collect(Collectors.toList());
            
            // 查询指定日期的课程
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.in("class_code", classCodes)
                    .eq("course_date", date)
                    .orderByAsc("time_slot");
            
            List<Course> courses = courseMapper.selectList(courseQuery);
            
            return courses.stream().map(course -> {
                CourseInfoDto dto = new CourseInfoDto();
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setTimeSlot(course.getTimeSlot());
                dto.setLocation(course.getLocation());
                dto.setCourseDate(course.getCourseDate());
                dto.setTeacherUsername(course.getTeacherUsername());
                dto.setClassCode(course.getClassCode());
                
                // 获取班级名称
                QueryWrapper<Class> classQuery = new QueryWrapper<>();
                classQuery.eq("class_code", course.getClassCode());
                Class clazz = classMapper.selectOne(classQuery);
                dto.setClassName(clazz != null ? clazz.getClassName() : course.getClassCode());
                
                // 获取教师姓名
                if (course.getTeacherUsername() != null && !course.getTeacherUsername().isEmpty()) {
                    QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("username", course.getTeacherUsername());
                    User teacher = userMapper.selectOne(teacherQuery);
                    dto.setTeacherName(teacher != null ? teacher.getName() : course.getTeacherUsername());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("按日期获取学生课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生最近一次签到记录的课程
     */
    public LastAttendanceDto getLastAttendanceCourse(String studentCode) {
        try {
            // 查询学生最近的签到记录
            QueryWrapper<AttendanceRecord> query = new QueryWrapper<>();
            query.eq("student_username", studentCode)
                .orderByDesc("attendance_time")
                .last("LIMIT 1");
            
            AttendanceRecord lastRecord = attendanceRecordMapper.selectOne(query);
            if (lastRecord == null) {
                return null; // 没有签到记录
            }
            
            // 获取课程信息
            QueryWrapper<Course> courseQuery = new QueryWrapper<>();
            courseQuery.eq("course_id", lastRecord.getCourseId());
            Course course = courseMapper.selectOne(courseQuery);
            if (course == null) {
                return null; // 课程不存在
            }
            
            // 构建返回DTO
            LastAttendanceDto dto = new LastAttendanceDto();
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setTeacherUsername(course.getTeacherUsername());
            dto.setClassCode(course.getClassCode());
            dto.setLocation(course.getLocation());
            dto.setCourseDate(course.getCourseDate());
            dto.setTimeSlot(course.getTimeSlot());
            dto.setAttendanceTime(lastRecord.getAttendanceTime());
            dto.setAttendanceStatus(lastRecord.getAttendanceStatus());
            dto.setIpAddress(lastRecord.getIpAddress());
            
            // 设置签到状态描述
            String statusDescription;
            switch (lastRecord.getAttendanceStatus()) {
                case 1:
                    statusDescription = "正常签到";
                    break;
                case 2:
                    statusDescription = "迟到";
                    break;
                case 3:
                    statusDescription = "早退";
                    break;
                default:
                    statusDescription = "未知状态";
                    break;
            }
            dto.setStatusDescription(statusDescription);
            
            // 获取教师信息
            QueryWrapper<User> teacherQuery = new QueryWrapper<>();
            teacherQuery.eq("username", course.getTeacherUsername());
            User teacher = userMapper.selectOne(teacherQuery);
            if (teacher != null) {
                dto.setTeacherName(teacher.getName());
            }
            
            // 获取班级信息
            QueryWrapper<Class> classQuery = new QueryWrapper<>();
            classQuery.eq("class_code", course.getClassCode());
            Class clazz = classMapper.selectOne(classQuery);
            if (clazz != null) {
                dto.setClassName(clazz.getClassName());
            }
            
            // 设置时间描述
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime attendanceTime = lastRecord.getAttendanceTime();
            long minutesAgo = java.time.Duration.between(attendanceTime, now).toMinutes();
            
            if (minutesAgo < 1) {
                dto.setTimeDescription("刚刚");
            } else if (minutesAgo < 60) {
                dto.setTimeDescription(minutesAgo + "分钟前");
            } else if (minutesAgo < 1440) { // 24小时
                long hoursAgo = minutesAgo / 60;
                dto.setTimeDescription(hoursAgo + "小时前");
            } else {
                long daysAgo = minutesAgo / 1440;
                dto.setTimeDescription(daysAgo + "天前");
            }
            
            return dto;
            
        } catch (Exception e) {
            throw new RuntimeException("获取最近签到课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学生所有已签到的课程
     */
    public List<AttendedCourseDto> getAttendedCourses(String studentCode) {
        try {
            // 查询学生的所有签到记录
            QueryWrapper<AttendanceRecord> query = new QueryWrapper<>();
            query.eq("student_username", studentCode)
                .eq("attendance_status", 1) // 只查询已签到的记录
                .orderByDesc("attendance_time");
            
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(query);
            
            // 按课程ID分组，统计签到次数
            Map<String, List<AttendanceRecord>> courseAttendanceMap = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getCourseId));
            
            // 转换为DTO
            return courseAttendanceMap.entrySet().stream().map(entry -> {
                String courseId = entry.getKey();
                List<AttendanceRecord> courseRecords = entry.getValue();
                
                AttendedCourseDto dto = new AttendedCourseDto();
                dto.setCourseId(courseId);
                dto.setAttendanceCount(courseRecords.size());
                
                // 获取最新的签到时间
                AttendanceRecord latestRecord = courseRecords.get(0);
                dto.setAttendanceTime(latestRecord.getAttendanceTime());
                
                // 获取课程信息
                QueryWrapper<Course> courseQuery = new QueryWrapper<>();
                courseQuery.eq("course_id", courseId);
                Course course = courseMapper.selectOne(courseQuery);
                
                if (course != null) {
                    dto.setCourseName(course.getCourseName());
                    dto.setLocation(course.getLocation());
                    dto.setCourseDate(course.getCourseDate());
                    
                    // 解析时间段
                    String timeSlot = course.getTimeSlot();
                    if (timeSlot != null && timeSlot.contains("-")) {
                        String[] timeParts = timeSlot.split("-");
                        if (timeParts.length == 2) {
                            dto.setStartTime(timeParts[0].trim());
                            dto.setEndTime(timeParts[1].trim());
                        }
                    } else {
                        dto.setStartTime(timeSlot);
                        dto.setEndTime(timeSlot);
                    }
                    
                    // 获取老师信息
                    QueryWrapper<User> teacherQuery = new QueryWrapper<>();
                    teacherQuery.eq("username", course.getTeacherUsername());
                    User teacher = userMapper.selectOne(teacherQuery);
                    dto.setTeacherName(teacher != null ? teacher.getName() : "未知老师");
                    
                    // 判断是否为跨班签到
                    QueryWrapper<StudentClassRelation> relationQuery = new QueryWrapper<>();
                    relationQuery.eq("student_username", studentCode)
                        .eq("is_deleted", 0);
                    List<StudentClassRelation> relations = studentClassRelationMapper.selectList(relationQuery);
                    
                    Set<String> studentClassCodes = relations.stream()
                        .map(StudentClassRelation::getClassCode)
                        .collect(Collectors.toSet());
                    
                    // 如果学生的班级代码与课程的班级代码不匹配，则为跨班签到
                    dto.setIsCrossClass(!studentClassCodes.contains(course.getClassCode()));
                    
                    // 如果是跨班签到，获取课程所属班级信息
                    if (dto.getIsCrossClass()) {
                        QueryWrapper<Class> classQuery = new QueryWrapper<>();
                        classQuery.eq("class_code", course.getClassCode());
                        Class courseClass = classMapper.selectOne(classQuery);
                        dto.setClassName(courseClass != null ? courseClass.getClassName() : "未知班级");
                    } else {
                        // 获取学生自己的班级信息
                        if (!relations.isEmpty()) {
                            String studentClassCode = relations.get(0).getClassCode();
                            QueryWrapper<Class> classQuery = new QueryWrapper<>();
                            classQuery.eq("class_code", studentClassCode);
                            Class studentClass = classMapper.selectOne(classQuery);
                            dto.setClassName(studentClass != null ? studentClass.getClassName() : "未知班级");
                        }
                    }
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("获取已签到课程失败: " + e.getMessage());
        }
    }
}
