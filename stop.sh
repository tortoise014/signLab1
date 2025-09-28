#!/bin/bash

# SignLab1 关闭脚本 - 支持本地开发和生产环境
# 使用方法: ./stop.sh [dev|prod]

set -e

# 默认环境为开发环境
ENVIRONMENT=${1:-dev}
APP_NAME="SignLab1"

# 根据环境设置不同的配置
if [ "$ENVIRONMENT" = "prod" ]; then
    # 生产环境配置
    APP_DIR="/opt/signlab1"
    PID_FILE="$APP_DIR/signlab1.pid"
    echo "🏭 关闭生产环境 $APP_NAME..."
else
    # 开发环境配置
    APP_DIR="$(pwd)"
    PID_FILE="$APP_DIR/signlab1-dev.pid"
    echo "💻 关闭开发环境 $APP_NAME..."
fi

echo "🛑 正在关闭 $APP_NAME..."

# 检查PID文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "❌ PID文件不存在: $PID_FILE"
    echo "💡 尝试通过进程名查找并关闭..."
    
    # 通过进程名查找Java进程
    PIDS=$(pgrep -f "signLab1-0.0.1-SNAPSHOT.jar")
    if [ -z "$PIDS" ]; then
        echo "❌ 未找到运行中的 $APP_NAME 进程"
        exit 1
    else
        echo "🔍 找到进程: $PIDS"
        for PID in $PIDS; do
            echo "🛑 关闭进程 $PID..."
            kill "$PID"
            sleep 2
            
            # 检查进程是否还在运行
            if ps -p "$PID" > /dev/null 2>&1; then
                echo "⚠️  进程 $PID 仍在运行，强制关闭..."
                kill -9 "$PID"
                sleep 1
                
                if ps -p "$PID" > /dev/null 2>&1; then
                    echo "❌ 无法关闭进程 $PID"
                else
                    echo "✅ 进程 $PID 已关闭"
                fi
            else
                echo "✅ 进程 $PID 已关闭"
            fi
        done
    fi
else
    # 从PID文件读取进程ID
    PID=$(cat "$PID_FILE")
    echo "📋 从PID文件读取进程ID: $PID"
    
    # 检查进程是否存在
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "🛑 关闭进程 $PID..."
        kill "$PID"
        
        # 等待进程正常关闭
        echo "⏳ 等待进程正常关闭..."
        for i in {1..10}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                echo "✅ 进程已正常关闭"
                rm -f "$PID_FILE"
                exit 0
            fi
            echo "⏳ 等待中... ($i/10)"
            sleep 1
        done
        
        # 如果进程仍在运行，强制关闭
        echo "⚠️  进程仍在运行，强制关闭..."
        kill -9 "$PID"
        sleep 2
        
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "❌ 无法关闭进程 $PID"
            exit 1
        else
            echo "✅ 进程已强制关闭"
            rm -f "$PID_FILE"
        fi
    else
        echo "❌ 进程 $PID 不存在"
        rm -f "$PID_FILE"
    fi
fi

# 清理可能残留的进程
echo "🧹 清理残留进程..."
REMAINING_PIDS=$(pgrep -f "signLab1-0.0.1-SNAPSHOT.jar")
if [ ! -z "$REMAINING_PIDS" ]; then
    echo "⚠️  发现残留进程: $REMAINING_PIDS"
    echo "🛑 强制关闭残留进程..."
    kill -9 $REMAINING_PIDS
    sleep 1
fi

# 最终检查
FINAL_CHECK=$(pgrep -f "signLab1-0.0.1-SNAPSHOT.jar")
if [ -z "$FINAL_CHECK" ]; then
    echo "✅ $APP_NAME 已完全关闭"
    echo "📊 端口8085现在应该可以重新使用"
else
    echo "❌ 仍有进程在运行: $FINAL_CHECK"
    exit 1
fi

echo "🎉 关闭完成！"