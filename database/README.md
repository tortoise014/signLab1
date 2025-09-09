# 数据库初始化说明

## 📋 数据库脚本说明

### 1. 完整版脚本 (`signlab1_init.sql`)
- 包含完整的表结构、索引、注释
- 包含测试数据和示例数据
- 适用于生产环境

### 2. 快速版脚本 (`quick_init.sql`)
- 简化的表结构
- 基本的测试数据
- 适用于快速测试

## 🚀 使用方法

### 方法一：使用MySQL命令行
```bash
# 登录MySQL
mysql -u root -p

# 执行脚本
source database/signlab1_init.sql
# 或者
source database/quick_init.sql
```

### 方法二：使用MySQL Workbench
1. 打开MySQL Workbench
2. 连接到MySQL服务器
3. 打开 `database/signlab1_init.sql` 文件
4. 执行脚本

### 方法三：使用Navicat等工具
1. 打开数据库管理工具
2. 连接到MySQL服务器
3. 导入SQL文件
4. 执行脚本

## 📊 数据库结构

### 核心表
- **users** - 用户表（学生、老师、管理员）
- **classes** - 班级表
- **courses** - 课程表
- **student_class_relations** - 学生班级关联表
- **attendance_records** - 签到记录表
- **class_photos** - 课堂照片表
- **student_documents** - 学生文档表

### 测试账号
- **管理员**: admin
- **老师**: T001 (张老师), T002 (李老师)
- **学生**: S001 (张三), S002 (李四)

### 测试班级
- **202101** - 计算机2021-1班 (验证码: 123456)
- **202102** - 计算机2021-2班 (验证码: 234567)

### 测试课程
- **KC24000001** - 数据结构与算法 (T001老师)
- **KC24000002** - Java程序设计 (T002老师)

## ⚠️ 注意事项

1. **数据库名称**: signlab1
2. **字符集**: utf8mb4
3. **排序规则**: utf8mb4_unicode_ci
4. **存储引擎**: InnoDB

## 🔧 配置检查

确保 `application.yml` 中的数据库配置正确：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/signlab1?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: Wr20050305
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 🚀 启动应用

数据库初始化完成后，启动应用：
```bash
./mvnw spring-boot:run
```

访问：http://localhost:8080

