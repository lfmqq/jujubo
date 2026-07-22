#!/bin/bash
# ========================================
# 桔桔波管理系统 - 一键部署脚本（Linux）
# ========================================
set -e

echo "========================================="
echo "  桔桔波管理系统 - 生产环境部署"
echo "========================================="

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ---------- 配置（按需修改） ----------
SERVER_DIR="/opt/jujubo-system"
BACKEND_DIR="$SERVER_DIR/backend"
UPLOAD_DIR="$SERVER_DIR/uploads"
JAR_NAME="springboot3-admin-1.0.0.jar"
BACKEND_PORT=8080
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
# ---------------------------------------

# 获取脚本所在目录（项目根目录）
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
echo -e "${YELLOW}项目目录: $PROJECT_DIR${NC}"

# 1. 前端构建
echo ""
echo -e "${GREEN}[1/5] 构建前端...${NC}"
cd "$PROJECT_DIR/vue3-admin"
npm install
npm run build
echo -e "${GREEN}前端构建完成: $PROJECT_DIR/vue3-admin/dist${NC}"

# 2. 后端构建
echo ""
echo -e "${GREEN}[2/5] 构建后端...${NC}"
cd "$PROJECT_DIR/springboot3-admin"
mvn clean package -DskipTests
echo -e "${GREEN}后端构建完成: $PROJECT_DIR/springboot3-admin/target/$JAR_NAME${NC}"

# 3. 创建/清空部署目录
echo ""
echo -e "${GREEN}[3/5] 准备部署目录...${NC}"
sudo mkdir -p "$SERVER_DIR" "$BACKEND_DIR" "$UPLOAD_DIR"

# 4. 部署文件
echo ""
echo -e "${GREEN}[4/5] 复制文件到服务器...${NC}"

# 前端资源
sudo rm -rf "$SERVER_DIR/dist"
sudo cp -r "$PROJECT_DIR/vue3-admin/dist" "$SERVER_DIR/dist"
echo "  前端 dist -> $SERVER_DIR/dist"

# 后端 JAR
sudo cp "$PROJECT_DIR/springboot3-admin/target/$JAR_NAME" "$BACKEND_DIR/"
echo "  后端 JAR -> $BACKEND_DIR/$JAR_NAME"

# Nginx 配置
if [ -f "$PROJECT_DIR/deploy/nginx.conf" ]; then
    sudo cp "$PROJECT_DIR/deploy/nginx.conf" /etc/nginx/sites-available/jujubo-system
    if [ ! -f /etc/nginx/sites-enabled/jujubo-system ]; then
        sudo ln -sf /etc/nginx/sites-available/jujubo-system /etc/nginx/sites-enabled/jujubo-system
    fi
    echo "  Nginx 配置已更新"
fi

# 5. 重启服务
echo ""
echo -e "${GREEN}[5/5] 重启服务...${NC}"

# 停止旧的后端进程
if [ -f "$BACKEND_DIR/app.pid" ]; then
    OLD_PID=$(cat "$BACKEND_DIR/app.pid")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "  停止旧的后端进程 (PID: $OLD_PID)..."
        sudo kill "$OLD_PID"
        sleep 2
    fi
fi

# 启动后端
echo "  启动后端服务..."
cd "$BACKEND_DIR"
nohup java $JAVA_OPTS -jar "$JAR_NAME" --spring.profiles.active=prod > app.log 2>&1 &
echo $! > "$BACKEND_DIR/app.pid"
echo -e "  后端已启动 (PID: $(cat $BACKEND_DIR/app.pid))"

# 重载 Nginx
echo "  重载 Nginx..."
sudo nginx -t && sudo nginx -s reload
echo "  Nginx 已重载"

# 等待后端启动
echo ""
echo "  等待后端启动..."
for i in $(seq 1 30); do
    if curl -s http://127.0.0.1:$BACKEND_PORT/api/auth/captcha > /dev/null 2>&1; then
        echo -e "${GREEN}  后端启动成功！${NC}"
        break
    fi
    sleep 2
done

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}  访问地址: http://你的服务器IP${NC}"
echo -e "${GREEN}  API文档: http://你的服务器IP/api/doc.html${NC}"
echo -e "${GREEN}=========================================${NC}"

# 查看后端日志的命令提示
echo ""
echo -e "${YELLOW}常用命令:${NC}"
echo "  查看后端日志: tail -f $BACKEND_DIR/app.log"
echo "  停止后端:     kill \$(cat $BACKEND_DIR/app.pid)"
echo "  查看后端状态: ps aux | grep $JAR_NAME"
