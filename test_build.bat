@echo off
echo 🔧 测试构建环境...

echo 当前目录: %CD%
echo.

echo 检查文件是否存在:
if exist gradlew.bat (
    echo ✅ gradlew.bat 存在
) else (
    echo ❌ gradlew.bat 不存在
)

if exist app\build.gradle (
    echo ✅ app\build.gradle 存在
) else (
    echo ❌ app\build.gradle 不存在
)

if exist gradle\wrapper\gradle-wrapper.properties (
    echo ✅ gradle-wrapper.properties 存在
) else (
    echo ❌ gradle-wrapper.properties 不存在
)

echo.
echo 尝试运行 gradlew tasks...
gradlew.bat tasks --console=plain

echo.
echo 测试完成！
pause
