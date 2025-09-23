-- 创建数据库
CREATE DATABASE IF NOT EXISTS signlab1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE signlab1;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    password VARCHAR(255) COMMENT '密码',
    role ENUM('student', 'teacher', 'admin') NOT NULL COMMENT '角色',
    password_set TINYINT DEFAULT 0 COMMENT '是否已设置密码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 班级表
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班级ID',
    class_code VARCHAR(20) NOT NULL UNIQUE COMMENT '班级编号',
    class_name VARCHAR(100) NOT NULL COMMENT '班级名称',
    verification_code VARCHAR(10) NOT NULL COMMENT '验证码',
    student_count INT DEFAULT 0 COMMENT '班级人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    course_id VARCHAR(20) NOT NULL UNIQUE COMMENT '课程ID',
    course_name VARCHAR(200) NOT NULL COMMENT '课程名称',
    teacher_username VARCHAR(50) NOT NULL COMMENT '授课老师用户名',
    class_code VARCHAR(20) NOT NULL COMMENT '上课班级编号',
    location VARCHAR(100) COMMENT '上课地点',
    course_date DATE NOT NULL COMMENT '课程日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '上课时间段',
    week_number INT COMMENT '课程周次',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 学生班级关联表
CREATE TABLE IF NOT EXISTS student_class_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    student_username VARCHAR(50) NOT NULL COMMENT '学生用户名',
    class_code VARCHAR(20) NOT NULL COMMENT '班级编号',
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_student_class (student_username, class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生班级关联表';

-- 签到记录表
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '签到记录ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    student_username VARCHAR(50) NOT NULL COMMENT '学生用户名',
    attendance_time DATETIME NOT NULL COMMENT '签到时间',
    attendance_status TINYINT DEFAULT 1 COMMENT '签到状态',
    ip_address VARCHAR(50) COMMENT '签到IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE KEY uk_course_student (course_id, student_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='签到记录表';

-- 课堂照片表
CREATE TABLE IF NOT EXISTS class_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    student_username VARCHAR(50) NOT NULL COMMENT '学生用户名',
    photo_name VARCHAR(200) NOT NULL COMMENT '照片文件名',
    photo_path VARCHAR(500) NOT NULL COMMENT '照片存储路径',
    remark TEXT COMMENT '照片备注',
    file_size BIGINT COMMENT '照片大小',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课堂照片表';

-- 学生文档表
CREATE TABLE IF NOT EXISTS student_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    course_id VARCHAR(20) NOT NULL COMMENT '课程ID',
    student_username VARCHAR(50) NOT NULL COMMENT '学生用户名',
    document_name VARCHAR(200) NOT NULL COMMENT '文档文件名',
    document_path VARCHAR(500) NOT NULL COMMENT '文档存储路径',
    file_size BIGINT COMMENT '文档大小',
    export_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生文档表';

-- 插入测试数据
-- 用户数据
INSERT INTO users (username, name, role, password_set) VALUES 
('T001', '张老师', 'teacher', 0),
('T002', '李老师', 'teacher', 0),
('S001', '张三', 'student', 0),
('S002', '李四', 'student', 0),
('S003', '王五', 'student', 0),
('admin', '管理员', 'admin', 0);

-- 班级数据
INSERT INTO classes (class_code, class_name, verification_code, student_count) VALUES 
('202101', '计算机2021-1班', '123456', 30),
('202102', '计算机2021-2班', '234567', 28),
('202201', '软件2022-1班', '345678', 32),
('202202', '软件2022-2班', '456789', 29);

-- 课程数据
INSERT INTO courses (course_id, course_name, teacher_username, class_code, location, course_date, time_slot, week_number) VALUES 
('KC24000001', '数据结构与算法', 'T001', '202101', '教学楼A101', CURDATE(), '08:00-09:40', 1),
('KC24000002', 'Java程序设计', 'T002', '202102', '教学楼B201', CURDATE(), '10:00-11:40', 1),
('KC24000003', '数据库原理', 'T001', '202201', '教学楼C301', CURDATE(), '14:00-15:40', 1),
('KC24000004', '操作系统', 'T002', '202202', '教学楼D401', CURDATE(), '16:00-17:40', 1);

-- 学生班级关联数据
INSERT INTO student_class_relations (student_username, class_code) VALUES 
('S001', '202101'),
('S002', '202101'),
('S003', '202102');

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_classes_class_code ON classes(class_code);
CREATE INDEX idx_courses_course_id ON courses(course_id);
CREATE INDEX idx_courses_teacher_username ON courses(teacher_username);
CREATE INDEX idx_courses_class_code ON courses(class_code);
CREATE INDEX idx_courses_course_date ON courses(course_date);
CREATE INDEX idx_attendance_course_id ON attendance_records(course_id);
CREATE INDEX idx_attendance_student_username ON attendance_records(student_username);
CREATE INDEX idx_photos_course_id ON class_photos(course_id);
CREATE INDEX idx_photos_student_username ON class_photos(student_username);
CREATE INDEX idx_documents_course_id ON student_documents(course_id);
CREATE INDEX idx_documents_student_username ON student_documents(student_username);

