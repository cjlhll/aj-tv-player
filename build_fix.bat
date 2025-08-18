@echo off
echo 🔧 修复构建问题...

echo 1. 清理项目...
call gradlew clean

echo 2. 删除构建缓存...
rmdir /s /q .gradle 2>nul
rmdir /s /q app\build 2>nul

echo 3. 重新构建...
call gradlew build --no-daemon --stacktrace

echo ✅ 构建完成！
pause
