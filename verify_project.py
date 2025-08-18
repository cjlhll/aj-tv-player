#!/usr/bin/env python3
"""
Android TV Player项目结构验证脚本
检查项目文件是否完整，代码是否有语法错误
"""

import os
import sys

def check_file_exists(file_path, description):
    """检查文件是否存在"""
    if os.path.exists(file_path):
        print(f"✅ {description}: {file_path}")
        return True
    else:
        print(f"❌ {description}: {file_path} - 文件不存在")
        return False

def check_directory_exists(dir_path, description):
    """检查目录是否存在"""
    if os.path.isdir(dir_path):
        print(f"✅ {description}: {dir_path}")
        return True
    else:
        print(f"❌ {description}: {dir_path} - 目录不存在")
        return False

def main():
    print("🔍 Android TV Player 项目结构验证")
    print("=" * 50)
    
    # 检查根目录文件
    root_files = [
        ("build.gradle", "根构建文件"),
        ("settings.gradle", "设置文件"),
        ("gradle.properties", "Gradle属性文件"),
        ("gradlew.bat", "Gradle Wrapper (Windows)"),
        ("README.md", "项目说明文件")
    ]
    
    # 检查应用模块文件
    app_files = [
        ("app/build.gradle", "应用构建文件"),
        ("app/proguard-rules.pro", "ProGuard规则文件"),
        ("app/src/main/AndroidManifest.xml", "应用清单文件")
    ]
    
    # 检查Kotlin源代码文件
    kotlin_files = [
        ("app/src/main/java/com/tvplayer/webdav/TVPlayerApplication.kt", "应用程序主类"),
        ("app/src/main/java/com/tvplayer/webdav/ui/main/MainActivity.kt", "主Activity"),
        ("app/src/main/java/com/tvplayer/webdav/ui/main/MainFragment.kt", "主Fragment"),
        ("app/src/main/java/com/tvplayer/webdav/ui/main/CardPresenter.kt", "卡片展示器")
    ]
    
    # 检查资源文件
    resource_files = [
        ("app/src/main/res/values/strings.xml", "字符串资源"),
        ("app/src/main/res/values/colors.xml", "颜色资源"),
        ("app/src/main/res/values/themes.xml", "主题资源"),
        ("app/src/main/res/values/dimens.xml", "尺寸资源"),
        ("app/src/main/res/layout/activity_main.xml", "主Activity布局"),
        ("app/src/main/res/drawable/app_banner.xml", "应用横幅"),
        ("app/src/main/res/drawable/ic_launcher.xml", "应用图标"),
        ("app/src/main/res/xml/data_extraction_rules.xml", "数据提取规则"),
        ("app/src/main/res/xml/backup_rules.xml", "备份规则")
    ]
    
    # 检查目录结构
    directories = [
        ("gradle/wrapper", "Gradle Wrapper目录"),
        ("app/src/main/java/com/tvplayer/webdav", "主包目录"),
        ("app/src/main/java/com/tvplayer/webdav/ui/main", "UI主模块目录"),
        ("app/src/main/res/values", "资源值目录"),
        ("app/src/main/res/layout", "布局目录"),
        ("app/src/main/res/drawable", "图标目录")
    ]
    
    all_checks_passed = True
    
    print("\n📁 检查目录结构:")
    for dir_path, description in directories:
        if not check_directory_exists(dir_path, description):
            all_checks_passed = False
    
    print("\n📄 检查根目录文件:")
    for file_path, description in root_files:
        if not check_file_exists(file_path, description):
            all_checks_passed = False
    
    print("\n📱 检查应用模块文件:")
    for file_path, description in app_files:
        if not check_file_exists(file_path, description):
            all_checks_passed = False
    
    print("\n🔧 检查Kotlin源代码:")
    for file_path, description in kotlin_files:
        if not check_file_exists(file_path, description):
            all_checks_passed = False
    
    print("\n🎨 检查资源文件:")
    for file_path, description in resource_files:
        if not check_file_exists(file_path, description):
            all_checks_passed = False
    
    print("\n" + "=" * 50)
    if all_checks_passed:
        print("🎉 项目结构验证通过！所有必要文件都存在。")
        print("\n📋 下一步建议:")
        print("1. 在Android Studio中打开项目")
        print("2. 等待Gradle同步完成")
        print("3. 连接Android TV设备或启动模拟器")
        print("4. 运行应用进行测试")
        return 0
    else:
        print("⚠️  项目结构验证失败！请检查缺失的文件。")
        return 1

if __name__ == "__main__":
    sys.exit(main())
