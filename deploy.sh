#!/bin/bash

# 部署脚本
# 使用方法: ./deploy.sh [environment]
# 环境: dev, test, prod

set -e

ENVIRONMENT=${1:-dev}
APP_NAME="signlab1"
DOCKER_IMAGE="$APP_NAME:$ENVIRONMENT"

echo "🚀 开始部署 $APP_NAME 到 $ENVIRONMENT 环境..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 停止旧容器
echo "🛑 停止旧容器..."
docker stop $APP_NAME-$ENVIRONMENT 2>/dev/null || true
docker rm $APP_NAME-$ENVIRONMENT 2>/dev/null || true

# 构建新镜像
echo "🔨 构建Docker镜像..."
docker build -t $DOCKER_IMAGE .

# 创建必要的目录
echo "📁 创建必要目录..."
mkdir -p ./uploads ./exports

# 启动新容器
echo "🚀 启动新容器..."
docker run -d \
    --name $APP_NAME-$ENVIRONMENT \
    --restart unless-stopped \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=$ENVIRONMENT \
    -v $(pwd)/uploads:/app/uploads \
    -v $(pwd)/exports:/app/exports \
    $DOCKER_IMAGE

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 30

# 健康检查
echo "🔍 执行健康检查..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ 部署成功！应用运行在 http://localhost:8080"
else
    echo "❌ 健康检查失败，请检查日志"
    docker logs $APP_NAME-$ENVIRONMENT
    exit 1
fi

# 显示容器状态
echo "📊 容器状态："
docker ps | grep $APP_NAME-$ENVIRONMENT

echo "🎉 部署完成！"
