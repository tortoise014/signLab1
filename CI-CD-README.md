# GitHub Actions CI/CD 配置说明

## 🔐 需要在GitHub仓库中配置的Secrets

### 1. Docker Hub配置
- `DOCKER_USERNAME`: Docker Hub用户名
- `DOCKER_PASSWORD`: Docker Hub密码或访问令牌

### 2. 服务器配置
- `SERVER_HOST`: 服务器IP地址或域名
- `SERVER_USERNAME`: 服务器SSH用户名
- `SERVER_SSH_KEY`: 服务器SSH私钥
- `SERVER_PORT`: SSH端口（默认22）

### 3. 数据库配置
- `DATABASE_URL`: 生产环境数据库连接URL
- `DATABASE_USERNAME`: 数据库用户名
- `DATABASE_PASSWORD`: 数据库密码

### 4. 应用配置
- `JWT_SECRET`: JWT密钥（生产环境）

### 5. 通知配置（可选）
- `SLACK_WEBHOOK`: Slack通知Webhook URL

## 📋 配置步骤

### 1. 在GitHub仓库中设置Secrets
1. 进入你的GitHub仓库
2. 点击 `Settings` → `Secrets and variables` → `Actions`
3. 点击 `New repository secret`
4. 添加上述所有Secrets

### 2. 服务器准备
```bash
# 安装Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 创建应用目录
sudo mkdir -p /opt/signlab1/{uploads,exports}
sudo chown -R $USER:$USER /opt/signlab1

# 配置SSH密钥
ssh-keygen -t rsa -b 4096 -C "github-actions"
# 将公钥添加到服务器的 ~/.ssh/authorized_keys
```

### 3. 数据库准备
```sql
-- 创建生产数据库
CREATE DATABASE signlab1_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'signlab1_prod'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON signlab1_prod.* TO 'signlab1_prod'@'%';
FLUSH PRIVILEGES;
```

## 🚀 使用说明

### 1. 本地开发
```bash
# 启动本地环境
docker-compose up -d

# 查看日志
docker-compose logs -f app
```

### 2. 手动部署
```bash
# 给脚本执行权限
chmod +x deploy.sh

# 部署到不同环境
./deploy.sh dev    # 开发环境
./deploy.sh test   # 测试环境
./deploy.sh prod   # 生产环境
```

### 3. CI/CD流程
- **Push到main分支**: 自动构建、测试、部署到生产环境
- **Push到develop分支**: 自动构建、测试、部署到测试环境
- **Pull Request**: 只执行代码质量检查和测试

## 🔧 故障排除

### 1. 构建失败
- 检查Maven依赖是否正确
- 确认Java版本为17
- 查看GitHub Actions日志

### 2. 部署失败
- 检查服务器SSH连接
- 确认Docker Hub凭据
- 验证服务器端口是否开放

### 3. 应用启动失败
- 检查数据库连接
- 确认环境变量配置
- 查看容器日志: `docker logs signlab1-app`

## 📊 监控和日志

### 1. 健康检查
- 端点: `http://your-server:8080/actuator/health`
- 状态: UP/DOWN

### 2. 应用指标
- 端点: `http://your-server:8080/actuator/metrics`
- 内存、CPU、请求统计等

### 3. 日志查看
```bash
# 查看容器日志
docker logs -f signlab1-app

# 查看系统日志
journalctl -u docker.service -f
```

## 🔄 回滚策略

### 1. 自动回滚
如果健康检查失败，GitHub Actions会自动回滚到上一个版本

### 2. 手动回滚
```bash
# 停止当前容器
docker stop signlab1-app

# 启动上一个版本
docker run -d --name signlab1-app -p 8080:8080 your-username/signlab1:previous-tag
```

## 📈 性能优化

### 1. Docker镜像优化
- 使用多阶段构建
- 优化依赖下载
- 减少镜像层数

### 2. 应用优化
- 启用JVM参数调优
- 配置连接池
- 启用缓存

### 3. 数据库优化
- 配置索引
- 优化查询
- 启用慢查询日志
