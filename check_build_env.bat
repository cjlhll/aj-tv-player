@echo off
echo 正在检查Android TV Player项目构建环境...
echo.

REM 检查Java环境
echo [1/4] 检查Java环境...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装JDK并配置JAVA_HOME
    pause
    exit /b 1
)
echo Java环境检查通过!
echo.

REM 检查Android SDK
echo [2/4] 检查Android SDK...
if not defined ANDROID_HOME (
    echo 警告: 未设置ANDROID_HOME环境变量
) else (
    echo ANDROID_HOME: %ANDROID_HOME%
)
echo.

REM 检查Gradle Wrapper
echo [3/4] 检查Gradle Wrapper...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Gradle Wrapper JAR 文件存在
) else (
    echo 错误: 缺少gradle-wrapper.jar文件
    echo 解决方案:
    echo 1. 在Android Studio中打开项目 ^(推荐^)
    echo 2. 或执行: gradle wrapper --gradle-version 8.9
    echo 3. 或从其他Android项目复制gradle-wrapper.jar文件
)
echo.

REM 检查项目文件
echo [4/4] 检查项目文件...
if exist "app\build.gradle" (
    echo 项目构建文件存在
) else (
    echo 错误: 项目构建文件不存在
)

if exist "app\src\main\AndroidManifest.xml" (
    echo Android清单文件存在
) else (
    echo 错误: Android清单文件不存在
)
echo.

echo 字幕功能文件检查:
if exist "app\src\main\java\com\tvplayer\webdav\data\subtitle\SubtitleManager.kt" (
    echo ✓ 字幕管理器
) else (
    echo ✗ 字幕管理器缺失
)

if exist "app\src\main\java\com\tvplayer\webdav\ui\player\SubtitleController.kt" (
    echo ✓ 字幕控制器
) else (
    echo ✗ 字幕控制器缺失
)

if exist "app\src\main\res\layout\dialog_subtitle_selection.xml" (
    echo ✓ 字幕选择界面
) else (
    echo ✗ 字幕选择界面缺失
)

echo.
echo 检查完成！
echo.
echo 建议的解决步骤:
echo 1. 使用Android Studio打开项目 ^(这会自动解决Gradle Wrapper问题^)
echo 2. 等待Gradle同步完成
echo 3. 构建并运行项目
echo 4. 测试字幕功能
echo.
pause