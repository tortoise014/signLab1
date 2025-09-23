-- 简化版数据库初始化脚本
-- 适用于快速测试

-- 创建数据库
CREATE DATABASE IF NOT EXISTS signlab1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE signlab1;

-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255),
    role ENUM('student', 'teacher', 'admin') NOT NULL,
    password_set TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- 班级表
CREATE TABLE classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_code VARCHAR(20) NOT NULL UNIQUE,
    class_name VARCHAR(100) NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    student_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- 课程表
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(200) NOT NULL,
    teacher_username VARCHAR(50) NOT NULL,
    class_code VARCHAR(20) NOT NULL,
    location VARCHAR(100),
    course_date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    week_number INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- 学生班级关联表
CREATE TABLE student_class_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(50) NOT NULL,
    class_code VARCHAR(20) NOT NULL,
    bind_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_student_class (student_username, class_code)
);

-- 签到记录表
CREATE TABLE attendance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(20) NOT NULL,
    student_username VARCHAR(50) NOT NULL,
    attendance_time DATETIME NOT NULL,
    attendance_status TINYINT DEFAULT 1,
    ip_address VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_course_student (course_id, student_username)
);

-- 课堂照片表
CREATE TABLE class_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(20) NOT NULL,
    student_username VARCHAR(50) NOT NULL,
    photo_name VARCHAR(200) NOT NULL,
    photo_path VARCHAR(500) NOT NULL,
    remark TEXT,
    file_size BIGINT,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- 学生文档表
CREATE TABLE student_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id VARCHAR(20) NOT NULL,
    student_username VARCHAR(50) NOT NULL,
    document_name VARCHAR(200) NOT NULL,
    document_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    export_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- 插入测试数据
INSERT INTO users (username, name, role, password_set) VALUES 
('T001', '张老师', 'teacher', 0),
('T002', '李老师', 'teacher', 0),
('S001', '张三', 'student', 0),
('S002', '李四', 'student', 0),
('admin', '管理员', 'admin', 0);

INSERT INTO classes (class_code, class_name, verification_code, student_count) VALUES 
('202101', '计算机2021-1班', '123456', 30),
('202102', '计算机2021-2班', '234567', 28);

INSERT INTO courses (course_id, course_name, teacher_username, class_code, location, course_date, time_slot, week_number) VALUES 
('KC24000001', '数据结构与算法', 'T001', '202101', '教学楼A101', CURDATE(), '08:00-09:40', 1),
('KC24000002', 'Java程序设计', 'T002', '202102', '教学楼B201', CURDATE(), '10:00-11:40', 1);

