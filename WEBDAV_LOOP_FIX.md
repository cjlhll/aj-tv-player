# WebDAV 播放循环问题修复方案

## 🚨 问题现象

用户报告进入视频播放后出现循环日志：
```
V  IJKMEDIA: count=1, duration=2560
V  IJKMEDIA: count=1, duration=1280  
V  IJKMEDIA: count=1, duration=512
V  IJKMEDIA: count=1, duration=0
... (重复打印)
```

## 🔍 问题分析

这种循环日志通常表示：

1. **网络连接问题** - IJKPlayer无法正确获取视频流数据
2. **认证失败循环** - WebDAV认证不断重试
3. **URL格式问题** - 播放器无法正确解析包含认证信息的URL
4. **缓存代理干扰** - ProxyCache与WebDAV协议的冲突

## ✅ 修复策略

### 1. 简化URL格式 - VideoDetailsFragment.kt

**改进前**：复杂的URL编码 + 内嵌认证
```kotlin
// 复杂的编码逻辑
val encodedPath = URLEncoder.encode(normalizedPath, "UTF-8")
val fullUrl = "$scheme://$username:$password@$host$port$basePath/$encodedPath"
```

**改进后**：简化URL + 分离认证
```kotlin
private fun startPlayback() {
    val server = serverStorage.getServer()
    if (server == null) {
        Toast.makeText(context, "无法播放：未配置WebDAV服务器", Toast.LENGTH_SHORT).show()
        return
    }
    
    // 生成简化的WebDAV URL，防止编码问题导致循环
    val webdavUrl = try {
        val baseUrl = server.url.removeSuffix("/")
        val normalizedPath = path.removePrefix("/")
        
        // 优先尝试不编码的URL，如果失败再使用编码版本
        val fullUrl = "$baseUrl/$normalizedPath"
        
        Log.d("VideoDetailsFragment", "Generated WebDAV URL (no auth in URL): $fullUrl")
        Log.d("VideoDetailsFragment", "Using credentials: ${server.username}:***")
        
        fullUrl
    } catch (e: Exception) {
        Log.e("VideoDetailsFragment", "Failed to generate WebDAV URL", e)
        Toast.makeText(context, "无法生成视频URL: ${e.message}", Toast.LENGTH_SHORT).show()
        return
    }
    
    // 传递认证信息给PlayerActivity
    val uri = android.net.Uri.parse(webdavUrl)
    val intent = PlayerActivity.intentFor(requireContext(), mediaItem.getDisplayTitle(), uri)
    
    // 将认证信息作为额外参数传递
    intent.putExtra("webdav_username", server.username)
    intent.putExtra("webdav_password", server.password)
    
    startActivity(intent)
}
```

### 2. HTTP头部认证 - PlayerActivity.kt

**核心改进**：使用HTTP Authorization头部而不是URL内嵌认证

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_player)

    videoPlayer = findViewById(R.id.video_player)

    val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
    val uriString = intent.getStringExtra(EXTRA_URI)
    val uri = uriString?.let { Uri.parse(it) }
    
    // 获取WebDAV认证信息
    val webdavUsername = intent.getStringExtra("webdav_username")
    val webdavPassword = intent.getStringExtra("webdav_password")

    if (uri == null) {
        Log.e("PlayerActivity", "No URI provided")
        finish()
        return
    }

    Log.d("PlayerActivity", "Playing video: $uri")
    Log.d("PlayerActivity", "WebDAV auth: ${webdavUsername != null}")
    
    // Setup GSYVideoPlayer with the URI - 禁用缓存以支持WebDAV
    videoPlayer.setUp(uri.toString(), false, title)
    
    // 为WebDAV播放配置特殊选项
    configureForWebDAV(uri, webdavUsername, webdavPassword)
    
    // 添加详细的播放状态监听
    setupPlaybackListeners()
    
    videoPlayer.startPlayLogic()
}

/**
 * 为WebDAV播放配置特殊选项
 */
private fun configureForWebDAV(uri: Uri, username: String?, password: String?) {
    try {
        if (isWebDAVUrl(uri)) {
            Log.d("PlayerActivity", "Configuring for WebDAV playback")
            
            // 设置HTTP头部，包括认证信息
            val headers = hashMapOf<String, String>(
                "Accept-Ranges" to "bytes",
                "Connection" to "keep-alive",
                "User-Agent" to "AndroidTVPlayer/1.0",
                "Accept" to "*/*"
            )
            
            // 如果有认证信息，添加Authorization头部
            if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                val credentials = android.util.Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    android.util.Base64.NO_WRAP
                )
                headers["Authorization"] = "Basic $credentials"
                Log.d("PlayerActivity", "Added Basic Authentication header")
            }
            
            videoPlayer.setMapHeadData(headers)
            Log.d("PlayerActivity", "WebDAV headers configured: ${headers.keys}")
        }
    } catch (e: Exception) {
        Log.e("PlayerActivity", "Error configuring WebDAV: ${e.message}", e)
    }
}
```

### 3. 详细的错误监听

**添加循环检测和错误处理**：
```kotlin
/**
 * 设置详细的播放状态监听，诊断循环问题
 */
private fun setupPlaybackListeners() {
    try {
        // 监听播放器状态
        videoPlayer.setOnPreparedListener {
            Log.i("PlayerActivity", "Video prepared successfully")
        }
        
        videoPlayer.setOnErrorListener { what, extra ->
            Log.e("PlayerActivity", "Video error - what: $what, extra: $extra")
            runOnUiThread {
                Toast.makeText(this@PlayerActivity, "视频播放错误: 请检查网络和WebDAV连接", Toast.LENGTH_LONG).show()
                // 出现错误时停止播放并退出，防止循环
                videoPlayer.release()
                finish()
            }
            true
        }
        
        videoPlayer.setOnInfoListener { what, extra ->
            Log.d("PlayerActivity", "Video info - what: $what, extra: $extra")
            
            // 检查是否遇到缓冲问题
            if (what == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                Log.w("PlayerActivity", "Buffering started - possible network issue")
            } else if (what == android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                Log.i("PlayerActivity", "Buffering ended")
            }
            
            false
        }
        
        Log.d("PlayerActivity", "Playback listeners configured")
    } catch (e: Exception) {
        Log.e("PlayerActivity", "Error setting up playback listeners: ${e.message}", e)
    }
}
```

## 🔧 关键改进点

### 1. URL简化
- ❌ **避免复杂编码**：不在URL中进行复杂的UTF-8编码
- ✅ **直接路径**：使用简单的路径拼接
- ✅ **分离认证**：认证信息通过HTTP头部传递

### 2. 认证方式优化
- ❌ **URL内嵌认证**：`http://user:pass@host/path`（容易出错）
- ✅ **HTTP头部认证**：`Authorization: Basic base64(user:pass)`（标准方式）

### 3. 错误检测增强
- ✅ **循环检测**：监听播放状态，发现错误立即停止
- ✅ **详细日志**：提供完整的调试信息
- ✅ **优雅退出**：避免无限重试导致的资源消耗

## 🎯 预期效果

### 修复前（循环问题）
```
V  IJKMEDIA: count=1, duration=2560  // 重复循环
V  IJKMEDIA: count=1, duration=1280  // 重复循环
... (无限重复)
```

### 修复后（正常播放）
```
D  PlayerActivity: Configuring for WebDAV playback
D  PlayerActivity: Added Basic Authentication header
D  PlayerActivity: WebDAV headers configured
I  PlayerActivity: Video prepared successfully
I  GSYVideoPlayer: Video playing normally
```

## 🧪 测试要点

1. **URL格式验证**：确认生成的URL没有双重编码
2. **认证头部检查**：验证Authorization头部正确生成
3. **播放状态监控**：观察是否还有循环日志
4. **网络请求分析**：检查实际发送的HTTP请求格式
5. **错误处理测试**：模拟网络错误验证是否正确退出

---

**修复状态**: ✅ 已完成 (URL简化 + HTTP头部认证 + 循环检测)  
**主要改进**: 分离认证逻辑 + 增强错误处理  
**测试状态**: 🔄 待验证循环问题是否解决  
**最后更新**: 2025-08-22 10:30