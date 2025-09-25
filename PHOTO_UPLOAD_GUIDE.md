# 学生课堂照片上传功能实现说明

## 📁 文件存储方案

### 存储路径配置
- **开发环境**: `D:/uploads/signlab/photos/`
- **生产环境**: `./uploads/photos/`

### 目录结构
```
uploads/signlab/photos/
├── 2024/01/15/          # 按日期分目录
│   ├── KC24000001/      # 按课程ID分目录
│   │   ├── S001_20240115_143022_abc12345.jpg  # 学生学号_日期_时间戳_随机码.扩展名
│   │   └── S002_20240115_143045_def67890.jpg
│   └── KC24000002/
└── 2024/01/16/
```

## 🔧 核心功能

### 1. 照片上传
- **接口**: `POST /api/student/photo/upload`
- **参数**: 
  - `file`: 照片文件 (MultipartFile)
  - `courseId`: 课程ID (String)
  - `remark`: 照片备注 (String, 可选)
- **限制**: 
  - 文件大小: 最大10MB
  - 支持格式: jpg, jpeg, png, gif
  - 权限验证: 只能上传自己班级课程的照片

### 2. 照片查看
- **接口**: `GET /api/student/photo/{photoId}`
- **功能**: 直接返回照片文件内容
- **权限**: 只能查看自己的照片

### 3. 照片列表
- **接口**: `GET /api/student/photos`
- **功能**: 获取学生的所有照片列表

### 4. 按课程查看照片
- **接口**: `GET /api/student/photos/course/{courseId}`
- **功能**: 获取指定课程的照片列表

### 5. 删除照片
- **接口**: `DELETE /api/student/photo/{photoId}`
- **功能**: 删除照片（同时删除物理文件和数据库记录）
- **权限**: 只能删除自己的照片

## 🛡️ 安全特性

### 文件验证
- 文件类型检查（只允许图片格式）
- 文件大小限制（10MB）
- 文件扩展名验证

### 权限控制
- 学生只能上传自己班级课程的照片
- 学生只能查看和删除自己的照片
- JWT Token认证

### 文件命名
- 使用学生学号、时间戳、随机码生成唯一文件名
- 防止文件名冲突和恶意上传

## 📝 使用示例

### 前端上传照片
```javascript
// 创建FormData
const formData = new FormData();
formData.append('file', photoFile);
formData.append('courseId', 'KC24000001');
formData.append('remark', '课堂笔记');

// 发送请求
fetch('/api/student/photo/upload', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + token
    },
    body: formData
})
.then(response => response.json())
.then(data => {
    if (data.success) {
        console.log('上传成功:', data.data);
    } else {
        console.error('上传失败:', data.message);
    }
});
```

### 查看照片
```javascript
// 直接使用img标签显示照片
<img src="/api/student/photo/123" alt="课堂照片" />
```

### 获取照片列表
```javascript
fetch('/api/student/photos', {
    headers: {
        'Authorization': 'Bearer ' + token
    }
})
.then(response => response.json())
.then(data => {
    if (data.success) {
        data.data.forEach(photo => {
            console.log('照片:', photo.photoName, photo.uploadTime);
        });
    }
});
```

## 🔄 数据库表结构

### class_photos 表
```sql
CREATE TABLE class_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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
);
```

## ⚙️ 配置说明

### application.yml
```yaml
# 文件上传配置
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# 本地存储路径配置
file:
  upload:
    path: D:/uploads/signlab/
    photo:
      path: ${file.upload.path}photos/
```

### application-prod.yml
```yaml
# 生产环境配置
file:
  upload:
    path: ./uploads/
    photo:
      path: ${file.upload.path}photos/
```

## 🚀 部署注意事项

1. **目录权限**: 确保应用有写入上传目录的权限
2. **磁盘空间**: 监控上传目录的磁盘使用情况
3. **备份策略**: 定期备份上传的照片文件
4. **访问控制**: 配置Web服务器禁止直接访问上传目录
5. **清理策略**: 定期清理过期的照片文件

## 🔧 扩展功能建议

1. **图片压缩**: 上传时自动压缩图片以节省存储空间
2. **缩略图**: 生成不同尺寸的缩略图
3. **云存储**: 集成阿里云OSS、腾讯云COS等云存储服务
4. **批量操作**: 支持批量上传、删除照片
5. **照片分类**: 按课程、日期等维度分类管理照片

