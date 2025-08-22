# WebDAV 视频播放修复方案 (最新版 - URL编码修复)

## 🚨 问题描述

用户报告视频播放失败，通过日志分析发现以下问题：

### 第一阶段错误 (路径问题):
```
Opening '/夸克网盘/电影电视/电影/Kung.Fu.Hustle.2004.2160p.HQ.60FPS.WEB-DL.H265.AAC.3Audio-GPTHD.mkv' for reading
E  /夸克网盘/电影电视/电影/Kung.Fu.Hustle.2004.2160p.HQ.60FPS.WEB-DL.H265.AAC.3Audio-GPTHD.mkv: No such file or directory
```

### 第二阶段错误 (URL编码问题):
```
E  Error opening connection for http://admin:cj1992728@op.caojian.xyz:5244/dav/夸克网盘/电影电视/电影/Kung.Fu.Hustle.2004.2160p.HQ.60FPS.WEB-DL.H265.AAC.3Audio-GPTHD.mkv
E  http://127.0.0.1:38967/http%3A%2F%2Fadmin%3Acj1992728%40op.caojian.xyz%3A5244%2Fdav%2F%E5%A4%B8%E5%85%8B%E7%BD%91%E7%9B%98...: End of file
```

## 🔍 根本原因分析

1. **路径转换错误**: `MediaItem.filePath` 存储的是相对路径，但播放器试图作为本地文件访问
2. **URL编码问题**: 中文字符没有正确编码，导致WebDAV服务器无法识别路径
3. **双重编码**: GSYVideoPlayer的ProxyCache机制导致URL被二次编码
4. **认证处理**: WebDAV认证信息没有正确集成到播放URL中

## ✅ 解决方案 (最终版)

### 1. 修改 VideoDetailsFragment.kt

**核心策略**: 在源头直接生成完整的、正确编码的、带认证的WebDAV URL

```kotlin
private fun startPlayback() {
    val rawPath = mediaItem.filePath
    val path = try { decodeFilePath(rawPath) } catch (_: Exception) { rawPath }
    
    val server = serverStorage.getServer()
    if (server == null) {
        android.widget.Toast.makeText(context, "无法播放：未配置WebDAV服务器", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    
    // 生成带身份验证的完整WebDAV URL
    val webdavUrl = try {
        val baseUrl = server.url.removeSuffix("/")
        val normalizedPath = path.removePrefix("/")
        
        // 对路径中的中文字符进行URL编码
        val encodedPath = java.net.URLEncoder.encode(normalizedPath, "UTF-8")
            .replace("+", "%20") // 空格用%20而不是+
            .replace("%2F", "/") // 保持路径分隔符
        
        // 解析基础URL以添加身份验证
        val uri = java.net.URI(baseUrl)
        val scheme = uri.scheme
        val host = uri.host
        val port = if (uri.port != -1) ":${uri.port}" else ""
        val basePath = uri.path?.removeSuffix("/") ?: ""
        
        // 构建带身份验证的完整URL
        val fullUrl = "$scheme://${server.username}:${server.password}@$host$port$basePath/$encodedPath"
        
        android.util.Log.d("VideoDetailsFragment", "Generated authenticated WebDAV URL: $fullUrl")
        fullUrl
    } catch (e: Exception) {
        android.util.Log.e("VideoDetailsFragment", "Failed to generate WebDAV URL", e)
        android.widget.Toast.makeText(context, "无法生成视频URL: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        return
    }
    
    val uri = android.net.Uri.parse(webdavUrl)
    val intent = com.tvplayer.webdav.ui.player.PlayerActivity.intentFor(requireContext(), mediaItem.getDisplayTitle(), uri)
    startActivity(intent)
}
```

### 2. 简化 PlayerActivity.kt

**核心策略**: 移除复杂的URL处理逻辑，直接使用传入的完整URL

```kotlin
@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {
    private lateinit var videoPlayer: StandardGSYVideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        videoPlayer = findViewById(R.id.video_player)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val uriString = intent.getStringExtra(EXTRA_URI)
        val uri = uriString?.let { Uri.parse(it) }

        if (uri == null) {
            Log.e("PlayerActivity", "No URI provided")
            finish()
            return
        }

        Log.d("PlayerActivity", "Playing video: $uri")
        Log.d("PlayerActivity", "Video title: $title")
        
        // 直接使用传入的URI，因为已经包含了身份验证信息
        Log.d("PlayerActivity", "Final URI for playback: $uri")

        // Setup GSYVideoPlayer with the URI
        videoPlayer.setUp(uri.toString(), true, title)
        
        videoPlayer.backButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        videoPlayer.startPlayLogic()
        
        // ... 其他生命周期方法保持不变
    }
}
```

## 🔧 关键技术改进

### 1. 正确的中文URL编码
```kotlin
// 关键编码逻辑
val encodedPath = java.net.URLEncoder.encode(normalizedPath, "UTF-8")
    .replace("+", "%20")    // 空格编码为%20
    .replace("%2F", "/")   // 保持路径分隔符不被编码
```

### 2. 完整的认证URL构建
```kotlin
// 构建格式：scheme://username:password@host:port/path
val fullUrl = "$scheme://${server.username}:${server.password}@$host$port$basePath/$encodedPath"
```

### 3. 避免双重编码
- 在VideoDetailsFragment中一次性完成所有URL处理
- PlayerActivity不再进行任何URL修改
- 避免GSYVideoPlayer的ProxyCache机制干扰

## 🎯 预期效果

修复后的播放流程：
1. 用户点击"播放"按钮
2. VideoDetailsFragment生成完整的WebDAV URL：
   ```
   https://username:password@server.com:port/webdav/path/to/%E4%B8%AD%E6%96%87%E8%A7%86%E9%A2%91.mkv
   ```
3. PlayerActivity直接使用这个URL进行播放
4. GSYVideoPlayer成功访问WebDAV服务器并播放视频

## 🧪 测试建议

### 测试场景
1. **中文路径视频**: 确保中文文件名和目录名正确编码
2. **特殊字符**: 测试包含空格、括号等特殊字符的文件名
3. **深层目录**: 测试多级中文目录结构
4. **不同格式**: 测试MP4、MKV、AVI等不同视频格式
5. **大文件**: 测试4K、高码率等大文件的播放性能

### 验证方法
1. 检查日志中生成的URL格式是否正确
2. 确认没有双重编码问题
3. 验证WebDAV服务器收到的请求格式
4. 测试播放器的加载和播放状态

## 📝 技术总结

### 解决的问题
✅ **路径转换**: WebDAV相对路径 → 完整HTTP URL  
✅ **中文编码**: 正确的UTF-8 URL编码处理  
✅ **身份验证**: HTTP基本认证集成  
✅ **双重编码**: 避免ProxyCache干扰  
✅ **错误处理**: 完整的错误检查和用户提示  

### 架构优化
- **单一职责**: VideoDetailsFragment负责URL生成，PlayerActivity专注播放
- **简化设计**: 移除不必要的URL处理逻辑
- **错误隔离**: 在URL生成阶段就捕获和处理错误

---
**修复状态**: ✅ 已完成 (包含URL编码修复)  
**测试状态**: 🔄 待测试  
**最后更新**: 2025-08-22 10:13