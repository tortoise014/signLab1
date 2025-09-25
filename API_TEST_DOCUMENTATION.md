# SignLab1 API 接口测试文档

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

## 1. 认证相关接口 (AuthController)

### 1.1 用户登录
**接口**: `POST /api/auth/login`  
**描述**: 用户登录获取JWT Token

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
    "userId": 1,
    "username": "202401001",
    "name": "张三",
    "role": "student",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isFirstLogin": false
  },
  "timestamp": 1727265075000
}
```

### 1.2 设置密码
**接口**: `POST /api/auth/set-password`  
**描述**: 设置用户密码

#### 请求参数
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
  "message": "密码设置成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 1.3 检查用户是否存在
**接口**: `GET /api/auth/check-user/{username}`  
**描述**: 检查用户是否存在

#### 路径参数
- `username`: 用户名

#### 返回结果
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1727265075000
}
```

### 1.4 检查用户状态
**接口**: `GET /api/auth/check-user-status/{username}`  
**描述**: 检查用户状态（是否存在、是否已设置密码）

#### 路径参数
- `username`: 用户名

#### 返回结果
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "exists": true,
    "passwordSet": true,
    "role": "student"
  },
  "timestamp": 1727265075000
}
```

## 2. 学生端接口 (StudentController)

### 2.1 扫码签到
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

### 2.2 获取签到记录
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
      "location": "教学楼A101"
    }
  ],
  "timestamp": 1727265075000
}
```

### 2.3 获取签到统计
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

### 2.4 绑定班级
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

#### 返回结果
```json
{
  "code": 200,
  "message": "绑定班级成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 2.5 获取已绑定的班级列表
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
      "bindTime": "2024-09-25T08:00:00"
    }
  ],
  "timestamp": 1727265075000
}
```

### 2.6 上传课堂照片
**接口**: `POST /api/student/photo/upload`  
**描述**: 学生上传课堂照片

#### 请求头
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

#### 请求参数
- `file`: 照片文件 (MultipartFile)
- `courseId`: 课程ID (String)
- `remark`: 备注 (String, 可选)

#### 返回结果
```json
{
  "code": 200,
  "message": "照片上传成功",
  "data": {
    "photoId": 1,
    "photoName": "class_photo_20240925.jpg",
    "photoUrl": "/api/student/photo/1",
    "fileSize": 1024000,
    "uploadTime": "2024-09-25T08:05:30"
  },
  "timestamp": 1727265075000
}
```

### 2.7 获取学生的课堂照片列表
**接口**: `GET /api/student/photos`  
**描述**: 获取学生的所有课堂照片

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取照片列表成功",
  "data": [
    {
      "photoId": 1,
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "photoName": "class_photo_20240925.jpg",
      "photoUrl": "/api/student/photo/1",
      "fileSize": 1024000,
      "uploadTime": "2024-09-25T08:05:30",
      "remark": "课堂笔记"
    }
  ],
  "timestamp": 1727265075000
}
```

### 2.8 根据课程ID获取学生的课堂照片
**接口**: `GET /api/student/photos/course/{courseId}`  
**描述**: 获取指定课程的学生照片

#### 请求头
```
Authorization: Bearer {token}
```

#### 路径参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "获取课程照片成功",
  "data": [
    {
      "photoId": 1,
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "photoName": "class_photo_20240925.jpg",
      "photoUrl": "/api/student/photo/1",
      "fileSize": 1024000,
      "uploadTime": "2024-09-25T08:05:30",
      "remark": "课堂笔记"
    }
  ],
  "timestamp": 1727265075000
}
```

### 2.9 删除课堂照片
**接口**: `DELETE /api/student/photo/{photoId}`  
**描述**: 删除指定的课堂照片

#### 请求头
```
Authorization: Bearer {token}
```

#### 路径参数
- `photoId`: 照片ID

#### 返回结果
```json
{
  "code": 200,
  "message": "照片删除成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 2.10 查看照片（返回照片文件）
**接口**: `GET /api/student/photo/{photoId}`  
**描述**: 查看照片文件（返回二进制数据）

#### 请求头
```
Authorization: Bearer {token}
```

#### 路径参数
- `photoId`: 照片ID

#### 返回结果
- **成功**: 返回照片的二进制数据
- **失败**: 返回相应的HTTP状态码

## 3. 老师端接口 (TeacherController)

### 3.1 获取老师今日课程
**接口**: `GET /api/teacher/courses`  
**描述**: 获取老师今天的课程安排

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取今日课程成功，共2门课程",
  "data": [
    {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "className": "计算机科学与技术1班",
      "timeSlot": "08:00-09:40",
      "location": "教学楼A101",
      "courseDate": "2024-09-25",
      "canStartAttendance": true,
      "documentCount": 5,
      "canViewDocuments": true
    }
  ],
  "timestamp": 1727265075000
}
```

### 3.2 获取老师所有课程
**接口**: `GET /api/teacher/courses/all`  
**描述**: 获取老师的所有课程

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "获取所有课程成功，共10门课程",
  "data": [
    {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "className": "计算机科学与技术1班",
      "timeSlot": "08:00-09:40",
      "location": "教学楼A101",
      "courseDate": "2024-09-25",
      "canStartAttendance": true,
      "documentCount": 5,
      "canViewDocuments": true
    }
  ],
  "timestamp": 1727265075000
}
```

### 3.3 根据日期获取老师课程
**接口**: `GET /api/teacher/courses/by-date`  
**描述**: 根据指定日期获取老师的课程

#### 请求头
```
Authorization: Bearer {token}
```

#### 查询参数
- `date`: 日期 (格式: yyyy-MM-dd)

#### 返回结果
```json
{
  "code": 200,
  "message": "获取2024-09-25课程成功，共2门课程",
  "data": [
    {
      "courseId": "KC241234567",
      "courseName": "Java程序设计",
      "className": "计算机科学与技术1班",
      "timeSlot": "08:00-09:40",
      "location": "教学楼A101",
      "courseDate": "2024-09-25",
      "canStartAttendance": true,
      "documentCount": 5,
      "canViewDocuments": true
    }
  ],
  "timestamp": 1727265075000
}
```

### 3.4 生成签到二维码
**接口**: `POST /api/teacher/attendance/qr`  
**描述**: 为指定课程生成签到二维码

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/x-www-form-urlencoded
```

#### 请求参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "生成二维码成功",
  "data": {
    "qrCodeData": "courseId=KC241234567&teacherCode=T001&classCode=202401&timestamp=1727265075000",
    "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "expireTime": "2024-09-25T08:05:40"
  },
  "timestamp": 1727265075000
}
```

### 3.5 获取签到统计
**接口**: `GET /api/teacher/attendance/stats`  
**描述**: 获取指定课程的签到统计

#### 请求头
```
Authorization: Bearer {token}
```

#### 查询参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "获取统计成功",
  "data": {
    "totalStudents": 30,
    "attendedStudents": 25,
    "attendanceRate": 83.33,
    "absentStudents": 5
  },
  "timestamp": 1727265075000
}
```

### 3.6 获取未签到学生名单
**接口**: `GET /api/teacher/attendance/absent-students`  
**描述**: 获取指定课程的未签到学生名单

#### 请求头
```
Authorization: Bearer {token}
```

#### 查询参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "获取未签到学生成功",
  "data": [
    "202401001",
    "202401002",
    "202401003"
  ],
  "timestamp": 1727265075000
}
```

### 3.7 查看课程签到情况
**接口**: `GET /api/teacher/attendance/course/{courseId}`  
**描述**: 查看指定课程的详细签到情况

#### 请求头
```
Authorization: Bearer {token}
```

#### 路径参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "获取课程签到情况成功",
  "data": [
    {
      "studentCode": "202401001",
      "studentName": "张三",
      "className": "计算机科学与技术1班",
      "attendanceStatus": 1,
      "attendanceTime": "2024-09-25T08:05:30",
      "ipAddress": "192.168.1.100"
    }
  ],
  "timestamp": 1727265075000
}
```

### 3.8 修改学生签到状态
**接口**: `PUT /api/teacher/attendance/update`  
**描述**: 修改学生的签到状态

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

#### 返回结果
```json
{
  "code": 200,
  "message": "签到状态更新成功",
  "data": null,
  "timestamp": 1727265075000
}
```

### 3.9 导入学生数据
**接口**: `POST /api/teacher/import/students`  
**描述**: 导入学生数据Excel文件

#### 请求头
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

#### 请求参数
- `file`: Excel文件 (MultipartFile)

#### 返回结果
```json
{
  "code": 200,
  "message": "学生数据导入成功",
  "data": "成功导入30名学生数据",
  "timestamp": 1727265075000
}
```

### 3.10 导入课程数据
**接口**: `POST /api/teacher/import/courses`  
**描述**: 导入课程数据Excel文件

#### 请求头
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

#### 请求参数
- `file`: Excel文件 (MultipartFile)

#### 返回结果
```json
{
  "code": 200,
  "message": "课程数据导入成功",
  "data": "成功导入10门课程数据",
  "timestamp": 1727265075000
}
```

### 3.11 下载学生模板
**接口**: `GET /api/teacher/template/students`  
**描述**: 下载学生数据导入模板

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
- **成功**: 返回Excel文件
- **失败**: 返回错误信息文件

### 3.12 下载课程模板
**接口**: `GET /api/teacher/template/courses`  
**描述**: 下载课程数据导入模板

#### 请求头
```
Authorization: Bearer {token}
```

#### 返回结果
- **成功**: 返回Excel文件
- **失败**: 返回错误信息文件

### 3.13 从学生数据生成课程模板
**接口**: `POST /api/teacher/template/courses-from-student`  
**描述**: 基于学生数据生成课程模板

#### 请求头
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

#### 请求参数
- `file`: 学生数据Excel文件 (MultipartFile)

#### 返回结果
- **成功**: 返回生成的课程模板Excel文件
- **失败**: 返回500错误

### 3.14 获取老师的学生列表
**接口**: `GET /api/teacher/students`  
**描述**: 获取老师所教的所有学生

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
    }
  ],
  "timestamp": 1727265075000
}
```

### 3.15 获取详细签到统计
**接口**: `GET /api/teacher/attendance/detailed-stats/{courseId}`  
**描述**: 获取课程的详细签到统计信息

#### 请求头
```
Authorization: Bearer {token}
```

#### 路径参数
- `courseId`: 课程ID

#### 返回结果
```json
{
  "code": 200,
  "message": "获取详细签到统计成功",
  "data": {
    "courseId": "KC241234567",
    "courseName": "Java程序设计",
    "totalStudents": 30,
    "attendedStudents": 25,
    "absentStudents": 5,
    "attendanceRate": 83.33,
    "attendanceDetails": [
      {
        "studentCode": "202401001",
        "studentName": "张三",
        "attendanceStatus": 1,
        "attendanceTime": "2024-09-25T08:05:30"
      }
    ]
  },
  "timestamp": 1727265075000
}
```

### 3.16 更新学生信息
**接口**: `PUT /api/teacher/students/update`  
**描述**: 更新学生信息

#### 请求头
```
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求参数
```json
{
  "studentCode": "202401001",
  "studentName": "张三",
  "classCode": "202401"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "学生信息更新成功",
  "data": null,
  "timestamp": 1727265075000
}
```

## 4. 健康检查接口 (HealthController)

### 4.1 GET健康检查
**接口**: `GET /api/health/check`  
**描述**: 简单的健康检查

#### 返回结果
```
success
```

### 4.2 POST健康检查
**接口**: `POST /api/health/check`  
**描述**: POST方式的健康检查

#### 请求参数
```json
{
  "test": "data"
}
```

#### 返回结果
```json
{
  "code": 200,
  "message": "系统运行正常",
  "data": "POST健康检查成功 - 2024-09-25 08:05:30",
  "timestamp": 1727265075000
}
```

### 4.3 Excel功能测试
**接口**: `GET /api/health/test-excel`  
**描述**: 测试Excel功能是否正常

#### 返回结果
```json
{
  "code": 200,
  "message": "POI依赖加载成功",
  "data": "Excel功能正常",
  "timestamp": 1727265075000
}
```

## 5. 错误码说明

### 5.1 成功状态码
- `200`: 操作成功

### 5.2 客户端错误
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `405`: 请求方法不允许
- `409`: 资源冲突
- `422`: 参数校验失败

### 5.3 服务器错误
- `500`: 服务器内部错误
- `503`: 服务不可用

### 5.4 业务错误
- `1001`: 用户不存在
- `1002`: 用户已存在
- `1003`: 密码错误
- `1004`: 用户已被禁用
- `2001`: 课程不存在
- `2002`: 课程已存在
- `3001`: 签到记录不存在
- `3002`: 签到记录已存在
- `3003`: 二维码已过期
- `3004`: 二维码无效
- `4001`: 文件上传失败
- `4002`: 文件不存在
- `5001`: Excel导入失败
- `5002`: Excel导出失败

## 6. 测试工具推荐

### 6.1 Postman
- 支持环境变量
- 支持Bearer Token认证
- 支持文件上传测试

### 6.2 curl命令示例
```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"202401001","password":"123456"}'

# 获取今日课程（需要先获取token）
curl -X GET http://localhost:8080/api/teacher/courses \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 扫码签到
curl -X POST http://localhost:8080/api/student/attendance/scan \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"qrData":"courseId=KC241234567&teacherCode=T001&classCode=202401&timestamp=1727265075000"}'
```

## 7. 注意事项

1. **认证**: 除了登录和健康检查接口外，所有接口都需要JWT Token认证
2. **时间格式**: 所有时间字段使用ISO 8601格式 (yyyy-MM-ddTHH:mm:ss)
3. **文件上传**: 照片上传接口使用multipart/form-data格式
4. **文件下载**: 模板下载和照片查看接口返回二进制数据
5. **错误处理**: 所有错误都会返回统一的ApiResponse格式
6. **跨域**: 所有接口都支持CORS跨域访问

## 8. 更新日志

- **2024-09-25**: 移除学生班级绑定验证，学生可以签到任何课程
- **2024-09-25**: 统一API响应格式为code、message、data、timestamp
