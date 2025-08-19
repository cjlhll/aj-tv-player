# Android TV WebDAV Player

一个专为Android TV设计的视频播放器，支持WebDAV协议，提供简洁美观的用户界面。

## 功能特性

### 核心功能
- ✅ Android TV适配（遥控器导航）
- 🔄 WebDAV协议支持（开发中）
- 🎥 视频播放功能（开发中）
- 📁 文件浏览器（开发中）
- ⚙️ 设置配置（开发中）

### 技术栈
- **开发语言**: Kotlin
- **UI框架**: Android TV Leanback Support Library
- **播放器**: ExoPlayer（待集成）
- **网络库**: OkHttp + Retrofit
- **WebDAV**: Sardine库
- **架构模式**: MVVM + Repository Pattern
- **依赖注入**: Hilt
- **数据存储**: Room Database（待集成）

## 项目结构

```
app/
├── src/main/
│   ├── java/com/tvplayer/webdav/
│   │   ├── TVPlayerApplication.kt          # 应用程序主类
│   │   └── ui/
│   │       └── main/
│   │           ├── MainActivity.kt         # 主Activity
│   │           ├── MainFragment.kt         # 主浏览Fragment
│   │           └── CardPresenter.kt        # 卡片展示器
│   ├── res/
│   │   ├── layout/                         # 布局文件
│   │   ├── values/                         # 资源值
│   │   ├── drawable/                       # 图标资源
│   │   └── xml/                           # XML配置
│   └── AndroidManifest.xml                # 应用清单
└── build.gradle                           # 模块构建配置
```

## 开发计划

### 第一阶段：项目基础 ✅
- [x] 项目初始化和环境搭建
- [x] 基础UI框架搭建
- [x] Android TV适配

### 第二阶段：核心功能（进行中）
- [x] 基础架构搭建（MVVM + Hilt）
- [x] WebDAV连接模块
- [ ] 文件浏览器UI
- [ ] 视频播放器集成

### 第三阶段：功能完善
- [ ] 播放器UI优化
- [ ] 数据存储和缓存
- [ ] 设置和配置界面
- [ ] 测试和优化

## 构建说明

1. 确保安装了Android Studio和Android SDK
2. 克隆项目到本地
3. 在Android Studio中打开项目
4. 等待Gradle同步完成
5. 连接Android TV设备或使用模拟器
6. 运行项目

## 系统要求

- **最低Android版本**: API 21 (Android 5.0)
- **目标Android版本**: API 34 (Android 14)
- **设备类型**: Android TV / Android TV Box

## 许可证

本项目采用MIT许可证，详见LICENSE文件。
