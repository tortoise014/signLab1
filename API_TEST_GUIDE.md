# SignLab1 API 测试文档

## 概述
本文档包含 SignLab1 项目的所有API接口测试说明，包括请求参数、测试数据和返回结果示例。

## 基础信息
- **基础URL**: `http://localhost:8080`
- **认证方式**: JWT Token (Bearer Token)
- **响应格式**: 统一使用 `ApiResponse` 格式

### 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1727265075000
}
```

## 学生端接口

### 1. 学生登录
**接口**: `POST /api/auth/login`  
**描述**: 学生登录获取JWT Token

#### 请求参数
```json
{
  "username": "202401001",
  "password": "123456"
}
```

#### 测试数据
```json
{
  "username": "202401001",
  "password": "123456"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "username": "202401001",
      "name": "张三",
      "role": "student"
    }
  },
  "timestamp": 1727265075000
}
```

### 2. 扫码签到
**接口**: `POST /api/student/attendance/scan`  
**描述**: 学生扫码签到

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求参数
```json
{
  "qrData": "courseId=KC241234567&teacherCode=T001&classCode=202401&timestamp=1727265075000"
}
```

#### 测试数据
```json
{
  "qrData": "courseId=KC241234567&teacherCode=T001&classCode=202401&timestamp=1727265075000"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "courseName": "Java程序设计",
    "teacherName": "张老师",
    "attendanceTime": "2024-09-25T08:05:30",
    "location": "教学楼A101"
  },
  "timestamp": 1727265075000
}
```

### 3. 获取签到记录
**接口**: `GET /api/student/attendance/records`  
**描述**: 获取学生的签到记录

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取签到记录成功",
  "data": [
    {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "teacherName": "张老师",
      "attendanceTime": "2024-09-25T08:05:30",
      "attendanceStatus": 1,
      "statusText": "正常签到"
    },
    {
      "courseId": "KC241234568",
      "courseName": "数据结构",
      "teacherName": "李老师",
      "attendanceTime": "2024-09-25T10:15:20",
      "attendanceStatus": 2,
      "statusText": "迟到"
    }
  ],
  "timestamp": 1727265075000
}
```

### 4. 获取签到统计
**接口**: `GET /api/student/attendance/stats`  
**描述**: 获取学生的签到统计

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取签到统计成功",
  "data": {
    "totalAttendance": 15,
    "attendanceRate": 100.0
  },
  "timestamp": 1727265075000
}
```

### 5. 绑定班级
**接口**: `POST /api/student/bind-class`  
**描述**: 学生绑定班级（可选功能）

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求参数
```json
{
  "verificationCode": "ABC123"
}
```

#### 测试数据
```json
{
  "verificationCode": "ABC123"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "绑定班级成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 6. 获取已绑定班级
**接口**: `GET /api/student/classes`  
**描述**: 获取学生已绑定的班级列表

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取班级列表成功",
  "data": [
    {
      "classCode": "202401",
      "className": "计算机科学与技术1班",
      "bindTime": "2024-09-20T10:30:00"
    }
  ],
  "timestamp": 1727265075000
}
```

## 老师端接口

### 1. 老师登录
**接口**: `POST /api/auth/login`  
**描述**: 老师登录获取JWT Token

#### 请求参数
```json
{
  "username": "T001",
  "password": "123456"
}
```

#### 测试数据
```json
{
  "username": "T001",
  "password": "123456"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "username": "T001",
      "name": "张老师",
      "role": "teacher"
    }
  },
  "timestamp": 1727265075000
}
```

### 2. 获取课程列表
**接口**: `GET /api/teacher/courses`  
**描述**: 获取老师的课程列表

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取课程列表成功",
  "data": [
    {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "classCode": "202401",
      "className": "计算机科学与技术1班",
      "location": "教学楼A101",
      "courseDate": "2024-09-25",
      "timeSlot": "08:00-09:40",
      "weekNumber": 5
    }
  ],
  "timestamp": 1727265075000
}
```

### 3. 生成签到二维码
**接口**: `POST /api/teacher/qr/generate`  
**描述**: 生成课程签到二维码

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求参数
```json
{
  "courseId": "KC241234567"
}
```

#### 测试数据
```json
{
  "courseId": "KC241234567"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "二维码生成成功",
  "data": {
    "qrData": "courseId=KC241234567&teacherCode=T001&classCode=202401&timestamp=1727265075000",
    "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  },
  "timestamp": 1727265075000
}
```

### 4. 获取课程签到情况
**接口**: `GET /api/teacher/attendance/{courseId}`  
**描述**: 获取指定课程的签到情况

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取课程签到情况成功",
  "data": [
    {
      "studentCode": "202401001",
      "studentName": "张三",
      "classCode": "202401",
      "className": "计算机科学与技术1班",
      "attendanceTime": "2024-09-25T08:05:30",
      "attendanceStatus": 1,
      "statusText": "正常签到"
    },
    {
      "studentCode": "202401002",
      "studentName": "李四",
      "classCode": "202401",
      "className": "计算机科学与技术1班",
      "attendanceTime": "2024-09-25T08:15:20",
      "attendanceStatus": 2,
      "statusText": "迟到"
    }
  ],
  "timestamp": 1727265075000
}
```

### 5. 获取详细签到统计 ⭐ 新接口
**接口**: `GET /api/teacher/attendance/detailed-stats/{courseId}`  
**描述**: 获取课程的详细签到统计，包含本班级和其他班级学生信息

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取详细签到统计成功",
  "data": {
    "courseInfo": {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "teacherName": "张老师",
      "location": "教学楼A101",
      "courseDate": "2024-09-25",
      "timeSlot": "08:00-09:40",
      "weekNumber": 5
    },
    "classInfo": {
      "classCode": "202401",
      "className": "计算机科学与技术1班",
      "totalStudentCount": 30,
      "attendedCount": 25,
      "absentCount": 5
    },
    "classStudents": [
      {
        "studentCode": "202401001",
        "studentName": "张三",
        "classCode": "202401",
        "className": "计算机科学与技术1班",
        "isAttended": true,
        "attendanceTime": "2024-09-25T08:05:30",
        "attendanceStatus": 1,
        "statusText": "正常签到",
        "isFromThisClass": true
      },
      {
        "studentCode": "202401002",
        "studentName": "李四",
        "classCode": "202401",
        "className": "计算机科学与技术1班",
        "isAttended": true,
        "attendanceTime": "2024-09-25T08:15:20",
        "attendanceStatus": 2,
        "statusText": "迟到",
        "isFromThisClass": true
      },
      {
        "studentCode": "202401003",
        "studentName": "王五",
        "classCode": "202401",
        "className": "计算机科学与技术1班",
        "isAttended": false,
        "attendanceTime": null,
        "attendanceStatus": null,
        "statusText": "未签到",
        "isFromThisClass": true
      }
    ],
    "otherClassStudents": [
      {
        "studentCode": "202402001",
        "studentName": "赵六",
        "classCode": "202402",
        "className": "计算机科学与技术2班",
        "isAttended": true,
        "attendanceTime": "2024-09-25T08:03:15",
        "attendanceStatus": 1,
        "statusText": "正常签到",
        "isFromThisClass": false
      },
      {
        "studentCode": "202403001",
        "studentName": "钱七",
        "classCode": "202403",
        "className": "软件工程1班",
        "isAttended": true,
        "attendanceTime": "2024-09-25T08:12:45",
        "attendanceStatus": 2,
        "statusText": "迟到",
        "isFromThisClass": false
      }
    ],
    "summary": {
      "totalAttended": 27,
      "classAttended": 25,
      "otherClassAttended": 2,
      "classAbsent": 5,
      "attendanceRate": 83.33,
      "normalCount": 24,
      "lateCount": 3,
      "earlyLeaveCount": 0
    }
  },
  "timestamp": 1727265075000
}
```

### 6. 修改学生签到状态
**接口**: `PUT /api/teacher/attendance/update`  
**描述**: 老师手动修改学生签到状态

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求参数
```json
{
  "courseId": "KC241234567",
  "studentCode": "202401001",
  "status": 1
}
```

#### 测试数据
```json
{
  "courseId": "KC241234567",
  "studentCode": "202401001",
  "status": 1
}
```

#### 状态码说明
- `1`: 正常签到
- `2`: 迟到
- `3`: 早退

#### 返回结果
```json
{
  "code": 200,
  "message": "签到状态更新成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 7. 获取学生列表
**接口**: `GET /api/teacher/students`  
**描述**: 获取老师的学生列表

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取学生列表成功",
  "data": [
    {
      "studentCode": "202401001",
      "studentName": "张三",
      "classCode": "202401",
      "className": "计算机科学与技术1班"
    },
    {
      "studentCode": "202401002",
      "studentName": "李四",
      "classCode": "202401",
      "className": "计算机科学与技术1班"
    }
  ],
  "timestamp": 1727265075000
}
```

## 测试数据准备

### 用户数据
```sql
-- 学生用户
INSERT INTO users (username, name, password, role, password_set) VALUES
('202401001', '张三', '$2a$10$...', 'student', 1),
('202401002', '李四', '$2a$10$...', 'student', 1),
('202401003', '王五', '$2a$10$...', 'student', 1),
('202402001', '赵六', '$2a$10$...', 'student', 1),
('202403001', '钱七', '$2a$10$...', 'student', 1);

-- 老师用户
INSERT INTO users (username, name, password, role, password_set) VALUES
('T001', '张老师', '$2a$10$...', 'teacher', 1),
('T002', '李老师', '$2a$10$...', 'teacher', 1);
```

### 班级数据
```sql
INSERT INTO classes (class_code, class_name, verification_code, student_count) VALUES
('202401', '计算机科学与技术1班', 'ABC123', 30),
('202402', '计算机科学与技术2班', 'DEF456', 28),
('202403', '软件工程1班', 'GHI789', 25);
```

### 课程数据
```sql
INSERT INTO courses (course_id, course_name, teacher_username, class_code, location, course_date, time_slot, week_number) VALUES
('KC241234567', 'Java程序设计', 'T001', '202401', '教学楼A101', '2024-09-25', '08:00-09:40', 5),
('KC241234568', '数据结构', 'T002', '202401', '教学楼A102', '2024-09-25', '10:00-11:40', 5);
```

### 学生班级绑定数据
```sql
INSERT INTO student_class_relations (student_username, class_code) VALUES
('202401001', '202401'),
('202401002', '202401'),
('202401003', '202401'),
('202402001', '202402'),
('202403001', '202403');
```

### 签到记录数据
```sql
INSERT INTO attendance_records (course_id, student_username, attendance_time, attendance_status, ip_address) VALUES
('KC241234567', '202401001', '2024-09-25 08:05:30', 1, '127.0.0.1'),
('KC241234567', '202401002', '2024-09-25 08:15:20', 2, '127.0.0.1'),
('KC241234567', '202402001', '2024-09-25 08:03:15', 1, '127.0.0.1'),
('KC241234567', '202403001', '2024-09-25 08:12:45', 2, '127.0.0.1');
```

## 错误响应示例

### 认证失败
```json
{
  "code": 401,
  "message": "未授权",
  "data": null,
  "timestamp": 1727265075000
}
```

### 参数错误
```json
{
  "code": 400,
  "message": "请求参数错误",
  "data": null,
  "timestamp": 1727265075000
}
```

### 业务错误
```json
{
  "code": 500,
  "message": "签到失败: 二维码已过期，请重新扫描",
  "data": null,
  "timestamp": 1727265075000
}
```

## 测试工具推荐

1. **Postman**: 图形化API测试工具
2. **curl**: 命令行测试工具
3. **Insomnia**: 轻量级API测试工具

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer {token}`
2. 二维码数据格式: `courseId=xxx&teacherCode=xxx&classCode=xxx&timestamp=xxx`
3. 时间格式统一使用 ISO 8601 格式: `2024-09-25T08:05:30`
4. 签到状态: 1-正常签到, 2-迟到, 3-早退
5. 学生现在可以签到任何课程，无需先绑定班级
6. 班级绑定功能现在是可选的，主要用于查看班级信息

## 更新日志

- **2024-09-25**: 移除学生班级绑定限制，学生可以签到任何课程
- **2024-09-25**: 新增详细签到统计接口，提供完整的班级签到情况
- **2024-09-25**: 统一所有接口使用 `ApiResponse` 格式
