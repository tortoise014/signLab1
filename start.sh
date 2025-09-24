#!/bin/bash

# SignLab1 启动脚本
# 使用方法: ./start.sh [start|stop|restart|status]

APP_NAME="SignLab1"
JAR_FILE="signLab1-0.0.1-SNAPSHOT.jar"
APP_DIR="/opt/signlab1"
PID_FILE="$APP_DIR/signlab1.pid"
LOG_FILE="$APP_DIR/logs/app.log"

# 创建必要目录
mkdir -p $APP_DIR/logs

# 启动应用
start() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "$APP_NAME 已经在运行 (PID: $PID)"
            return 1
        else
            rm -f $PID_FILE
        fi
    fi
    
    echo "启动 $APP_NAME..."
    cd $APP_DIR
    nohup java -jar $JAR_FILE --spring.profiles.active=prod > $LOG_FILE 2>&1 &
    echo $! > $PID_FILE
    echo "$APP_NAME 启动成功 (PID: $!)"
}

# 停止应用
stop() {
    if [ ! -f $PID_FILE ]; then
        echo "$APP_NAME 未运行"
        return 1
    fi
    
    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null 2>&1; then
        echo "停止 $APP_NAME (PID: $PID)..."
        kill $PID
        rm -f $PID_FILE
        echo "$APP_NAME 已停止"
    else
        echo "$APP_NAME 未运行"
        rm -f $PID_FILE
    fi
}

# 重启应用
restart() {
    stop
    sleep 2
    start
}

# 查看状态
status() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "$APP_NAME 正在运行 (PID: $PID)"
            echo "内存使用: $(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}')"
            echo "运行时间: $(ps -p $PID -o etime=)"
        else
            echo "$APP_NAME 未运行"
            rm -f $PID_FILE
        fi
    else
        echo "$APP_NAME 未运行"
    fi
}

# 查看日志
logs() {
    if [ -f $LOG_FILE ]; then
        tail -f $LOG_FILE
    else
        echo "日志文件不存在: $LOG_FILE"
    fi
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
    *)
        echo "使用方法: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动应用"
        echo "  stop    - 停止应用"
        echo "  restart - 重启应用"
        echo "  status  - 查看状态"
        echo "  logs    - 查看日志"
        echo ""
        echo "💡 提示: 也可以使用 ./stop.sh 直接关闭应用"
        exit 1
        ;;
esac

exit 0
