#!/bin/bash

# SignLab1 启动脚本 - 支持本地开发和生产环境
# 使用方法: ./start.sh [start|stop|restart|status|logs] [dev|prod]

set -e

# 默认环境为开发环境
ENVIRONMENT=${2:-dev}
APP_NAME="SignLab1"

# 根据环境设置不同的配置
if [ "$ENVIRONMENT" = "prod" ]; then
    # 生产环境配置
    APP_DIR="/opt/signlab1"
    JAR_FILE="signLab1-0.0.1-SNAPSHOT.jar"
    PID_FILE="$APP_DIR/signlab1.pid"
    LOG_FILE="$APP_DIR/logs/app.log"
    PROFILE="prod"
    echo "🏭 使用生产环境配置"
else
    # 开发环境配置
    APP_DIR="$(pwd)"
    JAR_FILE="target/signLab1-0.0.1-SNAPSHOT.jar"
    PID_FILE="$APP_DIR/signlab1-dev.pid"
    LOG_FILE="$APP_DIR/logs/app-dev.log"
    PROFILE="dev"
    echo "💻 使用开发环境配置"
fi

# 创建必要目录
mkdir -p "$APP_DIR/logs"

# 检查JAR文件是否存在
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        echo "❌ JAR文件不存在: $JAR_FILE"
        if [ "$ENVIRONMENT" = "dev" ]; then
            echo "💡 请先运行: ./mvnw clean package -DskipTests"
        else
            echo "💡 请确保JAR文件已部署到: $APP_DIR"
        fi
        exit 1
    fi
}

# 启动应用
start() {
    check_jar
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "$APP_NAME 已经在运行 (PID: $PID)"
            return 1
        else
            rm -f "$PID_FILE"
        fi
    fi
    
    echo "🚀 启动 $APP_NAME ($ENVIRONMENT 环境)..."
    echo "📁 工作目录: $APP_DIR"
    echo "📦 JAR文件: $JAR_FILE"
    echo "📋 配置文件: $PROFILE"
    
    cd "$APP_DIR"
    
    # 设置JVM参数
    JVM_OPTS="-Xms512m -Xmx1024m"
    if [ "$ENVIRONMENT" = "prod" ]; then
        JVM_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC"
    fi
    
    # 启动应用
    nohup java $JVM_OPTS -jar "$JAR_FILE" --spring.profiles.active="$PROFILE" > "$LOG_FILE" 2>&1 &
    echo $! > "$PID_FILE"
    
    echo "✅ $APP_NAME 启动成功 (PID: $!)"
    echo "📊 日志文件: $LOG_FILE"
    
    # 等待应用启动
    echo "⏳ 等待应用启动..."
    sleep 5
    
    # 健康检查
    if [ "$ENVIRONMENT" = "dev" ]; then
        HEALTH_URL="http://localhost:8085/api/health"
    else
        HEALTH_URL="http://localhost:8085/api/health"
    fi
    
    for i in {1..30}; do
        if curl -f "$HEALTH_URL" > /dev/null 2>&1; then
            echo "✅ 应用启动成功！"
            echo "🌐 访问地址: http://localhost:8085"
            return 0
        fi
        echo "⏳ 等待中... ($i/30)"
        sleep 2
    done
    
    echo "⚠️  应用可能启动失败，请检查日志: $LOG_FILE"
    return 1
}

# 停止应用
stop() {
    if [ ! -f "$PID_FILE" ]; then
        echo "$APP_NAME 未运行"
        return 1
    fi
    
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "🛑 停止 $APP_NAME (PID: $PID)..."
        kill "$PID"
        
        # 等待进程正常关闭
        for i in {1..10}; do
            if ! ps -p "$PID" > /dev/null 2>&1; then
                echo "✅ $APP_NAME 已停止"
                rm -f "$PID_FILE"
                return 0
            fi
            echo "⏳ 等待中... ($i/10)"
            sleep 1
        done
        
        # 强制关闭
        echo "⚠️  强制关闭进程..."
        kill -9 "$PID"
        sleep 1
        
        if ! ps -p "$PID" > /dev/null 2>&1; then
            echo "✅ $APP_NAME 已强制停止"
            rm -f "$PID_FILE"
        else
            echo "❌ 无法停止进程 $PID"
            return 1
        fi
    else
        echo "$APP_NAME 未运行"
        rm -f "$PID_FILE"
    fi
}

# 重启应用
restart() {
    echo "🔄 重启 $APP_NAME..."
    stop
    sleep 2
    start
}

# 查看状态
status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "✅ $APP_NAME 正在运行 (PID: $PID)"
            echo "📊 内存使用: $(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}')"
            echo "⏰ 运行时间: $(ps -p $PID -o etime=)"
            echo "🌐 访问地址: http://localhost:8085"
            
            # 检查端口
            if netstat -tuln 2>/dev/null | grep -q ":8085 "; then
                echo "✅ 端口8085正在监听"
            else
                echo "⚠️  端口8085未监听"
            fi
        else
            echo "❌ $APP_NAME 未运行"
            rm -f "$PID_FILE"
        fi
    else
        echo "❌ $APP_NAME 未运行"
    fi
}

# 查看日志
logs() {
    if [ -f "$LOG_FILE" ]; then
        echo "📋 显示日志: $LOG_FILE"
        tail -f "$LOG_FILE"
    else
        echo "❌ 日志文件不存在: $LOG_FILE"
    fi
}

# 构建应用
build() {
    echo "🔨 构建应用..."
    if [ "$ENVIRONMENT" = "dev" ]; then
        ./mvnw clean package -DskipTests
        echo "✅ 构建完成"
    else
        echo "❌ 生产环境不支持构建，请使用开发环境"
        exit 1
    fi
}

# 清理
clean() {
    echo "🧹 清理应用..."
    stop
    
    # 清理日志文件
    if [ -f "$LOG_FILE" ]; then
        echo "🗑️  清理日志文件..."
        > "$LOG_FILE"
    fi
    
    # 清理PID文件
    rm -f "$PID_FILE"
    
    echo "✅ 清理完成"
}

# 主逻辑
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    build)
        build
        ;;
    clean)
        clean
        ;;
    *)
        echo "使用方法: $0 {start|stop|restart|status|logs|build|clean} [dev|prod]"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动应用"
        echo "  stop    - 停止应用"
        echo "  restart - 重启应用"
        echo "  status  - 查看状态"
        echo "  logs    - 查看日志"
        echo "  build   - 构建应用 (仅开发环境)"
        echo "  clean   - 清理应用"
        echo ""
        echo "环境参数:"
        echo "  dev     - 开发环境 (默认)"
        echo "  prod    - 生产环境"
        echo ""
        echo "示例:"
        echo "  $0 start dev     # 启动开发环境"
        echo "  $0 start prod    # 启动生产环境"
        echo "  $0 build         # 构建应用"
        echo "  $0 status        # 查看状态"
        echo ""
        echo "💡 提示: 也可以使用 ./stop.sh 直接关闭应用"
        exit 1
        ;;
esac

exit 0