@echo off
chcp 65001 >nul
:: ========================================
:: 桔桔波管理系统 - Windows 构建打包脚本
:: （将构建产物打包为部署包，上传到 Linux 服务器后再执行 deploy.sh）
:: ========================================

echo =========================================
echo   桔桔波管理系统 - Windows 构建打包
echo =========================================

set PROJECT_DIR=%~dp0..
cd /d %PROJECT_DIR%

:: 前端构建
echo.
echo [1/3] 构建前端...
cd vue3-admin
call npm install
call npm run build
if %errorlevel% neq 0 (
    echo 前端构建失败！
    pause
    exit /b 1
)
cd ..
echo 前端构建完成

:: 后端构建
echo.
echo [2/3] 构建后端...
cd springboot3-admin
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 后端构建失败！
    pause
    exit /b 1
)
cd ..
echo 后端构建完成

:: 打包部署文件
echo.
echo [3/3] 打包部署文件...
set DEPLOY_DIR=%PROJECT_DIR%\deploy-package
if exist "%DEPLOY_DIR%" rmdir /s /q "%DEPLOY_DIR%"
mkdir "%DEPLOY_DIR%"
mkdir "%DEPLOY_DIR%\dist"
mkdir "%DEPLOY_DIR%\backend"

xcopy /e /i vue3-admin\dist "%DEPLOY_DIR%\dist"
copy springboot3-admin\target\springboot3-admin-1.0.0.jar "%DEPLOY_DIR%\backend\"
copy deploy\nginx.conf "%DEPLOY_DIR%\"
copy deploy\deploy.sh "%DEPLOY_DIR%\"

echo.
echo =========================================
echo   构建打包完成！
echo   部署包位置: %DEPLOY_DIR%
echo.
echo   下一步 - 上传到 Linux 服务器:
echo   1. 将 deploy-package 目录上传到服务器
echo      scp -r deploy-package root@你的服务器IP:/opt/
echo   2. SSH 登录服务器执行部署脚本
echo      ssh root@你的服务器IP
echo      cd /opt/deploy-package
echo      chmod +x deploy.sh
echo      sudo ./deploy.sh
echo =========================================

pause
