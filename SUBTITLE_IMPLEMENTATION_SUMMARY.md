# ASSRT 自动字幕功能实现总结

## 📋 功能概述

成功为您的Android TV播放器实现了完整的ASSRT自动字幕功能，支持基于GSYVideoPlayer和ExoPlayer内核的字幕自动搜索、下载和挂载。

## ✅ 已实现的功能

### 1. ASSRT API 集成
- **API客户端**: `AssrtApiClient.kt`
- **API Token**: `0k4uATEWYFeuaEleVJvzTFXlBBCTvP1A`
- **支持功能**:
  - 字幕搜索 (支持文件名和标题搜索)
  - 字幕详情获取
  - 字幕文件下载
  - 网络重试和超时处理

### 2. 智能字幕搜索算法
- **多策略搜索**: 
  - 策略1: 清理后的文件名搜索
  - 策略2: 原始文件名搜索  
  - 策略3: 视频标题搜索
  - 策略4: 提取主要标题搜索
- **文件名清理**:
  - 移除视频质量标识 (1080p, 720p, 4K等)
  - 移除编码格式标识 (x264, x265, HEVC等)
  - 移除剧集标识 (S01E01, 第1集等)
  - 清理特殊字符和分隔符

### 3. 优化的字幕选择逻辑
- **优先级排序**:
  1. 中文字幕优先 (支持简体、繁体、各种中文标识)
  2. 高质量字幕优先 (BluRay, WEB-DL等)
  3. 格式优先级 (SRT > ASS > VTT > SSA)
  4. 文件大小评分 (50KB-500KB为理想大小)
  5. 文件名详细程度

### 4. GSYVideoPlayer 字幕挂载
- **ExoPlayer集成**: 通过`ExoSourceManager`拦截器实现
- **字幕格式支持**:
  - SRT (SubRip)
  - ASS/SSA (Advanced SubStation Alpha)
  - VTT (WebVTT)
- **WebDAV支持**: 保持HTTP认证头部，支持WebDAV视频的字幕挂载

### 5. 自动字幕加载器
- **异步处理**: 使用Kotlin协程实现非阻塞加载
- **错误处理**: 完善的异常处理和重试机制
- **缓存管理**: 字幕文件本地缓存，避免重复下载
- **状态反馈**: 实时显示字幕搜索和加载状态

### 6. 用户界面集成
- **状态提示**: Toast消息显示字幕搜索进度
- **自动挂载**: 字幕下载成功后自动重启播放器挂载
- **位置恢复**: 挂载字幕时保持播放位置
- **开关控制**: 支持字幕功能的启用/禁用

## 🛠️ 技术实现细节

### 核心组件

1. **AssrtApiClient.kt**
   - ASSRT API的完整封装
   - 支持搜索、详情、下载功能
   - 网络超时和重试处理

2. **SubtitleAutoLoader.kt**
   - 多策略字幕搜索引擎
   - 异步处理和错误恢复
   - 智能文件名解析

3. **SubtitleExoMount.kt**
   - GSYVideoPlayer字幕挂载桥接
   - ExoPlayer MediaSource拦截
   - 多种字幕格式支持

4. **PlayerActivity.kt**
   - 字幕功能的UI集成
   - 播放状态管理
   - 用户反馈界面

### 工作流程

```
视频开始播放 
    ↓
启用字幕功能检查
    ↓
提取视频文件名/标题
    ↓
多策略搜索ASSRT字幕
    ↓
选择最佳匹配字幕
    ↓
下载字幕到本地缓存
    ↓
注册字幕到ExoPlayer
    ↓
重启播放器挂载字幕
    ↓
恢复播放位置
    ↓
显示成功状态
```

## 📊 功能特性

### 支持的字幕格式
- ✅ SRT (SubRip) - 最通用格式
- ✅ ASS (Advanced SubStation Alpha) - 支持样式
- ✅ SSA (SubStation Alpha) - 传统格式
- ✅ VTT (WebVTT) - Web标准格式

### 支持的视频源
- ✅ 本地视频文件
- ✅ WebDAV远程视频
- ✅ HTTP/HTTPS视频流
- ✅ 各种视频编码格式

### 智能匹配算法
- ✅ 中文字幕优先识别
- ✅ 视频质量标识清理
- ✅ 多种搜索策略
- ✅ 文件大小评分
- ✅ 格式优先级排序

## 🔧 配置说明

### API配置
```kotlin
// ASSRT API Token (已配置)
private val token = "0k4uATEWYFeuaEleVJvzTFXlBBCTvP1A"

// 字幕功能开关
private var subtitleEnabled = true
```

### 缓存配置
```kotlin
// 字幕缓存目录
private val cacheDir = File(context.cacheDir, "subtitles")
```

## 🚀 使用方法

1. **自动启用**: 播放视频时自动搜索和加载字幕
2. **状态监控**: 通过Toast消息查看字幕加载状态
3. **格式支持**: 自动识别和挂载多种字幕格式
4. **缓存优化**: 已下载的字幕会被缓存，避免重复下载

## 📝 日志输出

系统会输出详细的日志信息，便于调试：

```
I/PlayerActivity: 开始搜索字幕: 电影标题
I/SubtitleAutoLoader: Trying strategy 1 with query: 清理后的查询
I/SubtitleAutoLoader: Successfully downloaded subtitle: 字幕文件名.srt
I/PlayerActivity: 字幕准备就绪: 字幕文件名.srt, 重新启动播放器挂载字幕
```

## ✨ 优势特点

1. **智能搜索**: 多种搜索策略确保找到最匹配的字幕
2. **中文优化**: 专门优化中文字幕的识别和选择
3. **无缝集成**: 与GSYVideoPlayer完美集成，支持WebDAV
4. **错误处理**: 完善的错误处理和用户反馈
5. **性能优化**: 异步处理，不影响视频播放性能
6. **缓存机制**: 智能缓存，减少网络请求

## 🎯 测试验证

- ✅ 编译测试通过
- ✅ 多种字幕格式支持
- ✅ WebDAV视频字幕挂载
- ✅ 错误处理机制
- ✅ 用户界面集成

## 📱 兼容性

- **Android版本**: API 21+
- **播放内核**: ExoPlayer (推荐)
- **视频格式**: 支持GSYVideoPlayer的所有格式
- **网络协议**: HTTP/HTTPS/WebDAV

---

**实现完成时间**: 2025年1月
**开发状态**: ✅ 完成并测试通过
**功能状态**: 🚀 可立即使用

您的Android TV播放器现在具备了完整的ASSRT自动字幕功能！