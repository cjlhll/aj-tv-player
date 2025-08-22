package com.tvplayer.webdav.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.storage.PlaybackStateManager
import com.tvplayer.webdav.data.model.PlaybackState
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.SubtitleConfig
import com.tvplayer.webdav.data.subtitle.SubtitleManager
import com.tvplayer.webdav.ui.player.SubtitleController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Video playback Activity using GSYVideoPlayer with WebDAV support.
 * Supports both local files and WebDAV URLs with authentication.
 */
@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var playbackStateManager: PlaybackStateManager
    
    @Inject
    lateinit var subtitleManager: SubtitleManager
    
    @Inject
    lateinit var subtitleController: SubtitleController
    
    private lateinit var videoPlayer: StandardGSYVideoPlayer
    private var mediaId: String? = null
    private var mediaTitle: String? = null
    private var startPosition: Long = 0L // 秒为单位
    private var currentMediaItem: MediaItem? = null
    private var subtitleConfig: SubtitleConfig = SubtitleConfig.getDefault()
    
    // 协程作用域
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // 进度追踪相关变量
    private val progressHandler = Handler(Looper.getMainLooper())
    private val progressUpdateRunnable = object : Runnable {
        override fun run() {
            updatePlaybackProgress()
            progressHandler.postDelayed(this, 5000) // 每5秒更新一次
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // 切换到ExoPlayer内核以获得更好的WebDAV支持
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        Log.d("PlayerActivity", "Switched to ExoPlayer kernel for better WebDAV support")

        videoPlayer = findViewById<StandardGSYVideoPlayer>(R.id.video_player)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val uriString = intent.getStringExtra(EXTRA_URI)
        val uri = uriString?.let { Uri.parse(it) }
        
        // 获取WebDAV认证信息
        val webdavUsername = intent.getStringExtra("webdav_username")
        val webdavPassword = intent.getStringExtra("webdav_password")
        
        // 获取播放位置和媒体信息
        startPosition = intent.getLongExtra("start_position", 0L)
        mediaId = intent.getStringExtra("media_id")
        mediaTitle = intent.getStringExtra("media_title") ?: title

        if (uri == null) {
            Log.e("PlayerActivity", "No URI provided")
            finish()
            return
        }

        Log.d("PlayerActivity", "Playing video: $uri")
        Log.d("PlayerActivity", "Video title: $title")
        Log.d("PlayerActivity", "WebDAV auth: ${webdavUsername != null}")
        Log.d("PlayerActivity", "Start position: ${startPosition}s")
        Log.d("PlayerActivity", "Media ID: $mediaId")
        
        // 直接使用传入的URI，因为已经包含了身份验证信息
        Log.d("PlayerActivity", "Final URI for playback: $uri")

        // Setup GSYVideoPlayer with the URI - 禁用缓存以支持WebDAV
        videoPlayer.setUp(uri.toString(), false, title) // 第二个参数设为false禁用缓存
        
        // 为WebDAV播放配置特殊选项
        configureForWebDAV(uri, webdavUsername, webdavPassword)
        
        // 设置播放监听器
        setupPlaybackListeners()
        
        // 初始化字幕控制器
        subtitleController.initialize(videoPlayer, subtitleConfig)

        // 初始化字幕控制UI
        setupSubtitleControlUI()

        // 自动搜索和加载字幕
        autoLoadSubtitles()
        
        // Optional: handle back button in player UI to finish activity.
        videoPlayer.backButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        videoPlayer.startPlayLogic()
        
        // 如果有起始位置，在播放器准备好后跳转到指定位置
        if (startPosition > 0) {
            // 延迟跳转，等待播放器初始化完成
            progressHandler.postDelayed({
                seekToPosition(startPosition)
            }, 2000) // 2秒后跳转
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!GSYVideoManager.backFromWindowFull(this@PlayerActivity)) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onVideoResume()
        // 开始进度追踪
        startProgressTracking()
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
        // 停止进度追踪并保存当前进度
        stopProgressTracking()
        saveCurrentProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止进度追踪并保存当前进度
        stopProgressTracking()
        saveCurrentProgress()
        
        // 释放字幕控制器
        subtitleController.release()
        
        // 取消协程作用域
        activityScope.cancel()
        
        GSYVideoManager.releaseAllVideos()
    }
    
    /**
     * 为WebDAV播放配置特殊选项
     */
    private fun configureForWebDAV(uri: Uri, username: String?, password: String?) {
        try {
            // 检查是否为WebDAV URL
            if (isWebDAVUrl(uri)) {
                Log.d("PlayerActivity", "Configuring for WebDAV playback")
                
                // 禁用全局缓存服务器
                try {
                    val proxyCacheManagerClass = Class.forName("com.danikula.videocache.ProxyCacheManager")
                    val shutdownMethod = proxyCacheManagerClass.getMethod("shutdown")
                    shutdownMethod.invoke(null)
                    Log.d("PlayerActivity", "ProxyCache disabled for WebDAV")
                } catch (e: Exception) {
                    Log.w("PlayerActivity", "Could not disable ProxyCache: ${e.message}")
                }
                
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
    
    /**
     * 检查是否为WebDAV URL
     */
    private fun isWebDAVUrl(uri: Uri): Boolean {
        val scheme = uri.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") && 
               uri.host != null && 
               uri.host != "localhost" && 
               uri.host != "127.0.0.1"
    }
    
    /**
     * 设置播放监听器
     */
    private fun setupPlaybackListeners() {
        // GSYVideoPlayer的监听器设置
        try {
            // 注意：GSYVideoPlayer的监听器设置可能因版本而异，这里使用默认处理
            Log.d("PlayerActivity", "Playback listeners configured")
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error setting up playback listeners", e)
        }
    }
    
    /**
     * 跳转到指定位置
     */
    private fun seekToPosition(positionSeconds: Long) {
        try {
            if (videoPlayer.currentPlayer != null) {
                val positionMs = positionSeconds * 1000L
                videoPlayer.seekTo(positionMs)
                Log.d("PlayerActivity", "Seeked to position: ${positionSeconds}s (${positionMs}ms)")
            } else {
                Log.w("PlayerActivity", "Player not ready, retrying seek in 1 second")
                progressHandler.postDelayed({
                    seekToPosition(positionSeconds)
                }, 1000)
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error seeking to position: ${positionSeconds}s", e)
        }
    }
    
    /**
     * 开始进度追踪
     */
    private fun startProgressTracking() {
        stopProgressTracking() // 防止重复启动
        progressHandler.post(progressUpdateRunnable)
        Log.d("PlayerActivity", "Started progress tracking")
    }
    
    /**
     * 停止进度追踪
     */
    private fun stopProgressTracking() {
        progressHandler.removeCallbacks(progressUpdateRunnable)
        Log.d("PlayerActivity", "Stopped progress tracking")
    }
    
    /**
     * 更新播放进度
     */
    private fun updatePlaybackProgress() {
        try {
            if (mediaId.isNullOrBlank()) {
                return
            }
            
            val currentPosition = videoPlayer.currentPositionWhenPlaying / 1000L // 转换为秒
            val duration = videoPlayer.duration / 1000L // 转换为秒
            
            if (currentPosition > 0 && duration > 0) {
                // 更新播放状态
                updatePlaybackState(currentPosition, duration)
                Log.d("PlayerActivity", "Progress updated: ${currentPosition}s / ${duration}s")
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error updating playback progress", e)
        }
    }
    
    /**
     * 更新播放状态
     */
    private fun updatePlaybackState(currentPosition: Long, duration: Long) {
        try {
            val currentState = playbackStateManager.getPlaybackState(mediaId!!)
            
            if (currentState != null) {
                // 更新现有状态
                val updatedState = currentState.updateProgress(currentPosition, duration)
                playbackStateManager.savePlaybackState(updatedState)
            } else {
                // 创建新的播放状态（电影类型）
                val newState = PlaybackState(
                    seriesId = mediaId!!,
                    currentSeasonNumber = 1, // 电影不需要季数
                    currentEpisodeNumber = 1, // 电影不需要集数
                    playbackProgress = currentPosition,
                    totalDuration = duration
                )
                playbackStateManager.savePlaybackState(newState)
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error updating playback state", e)
        }
    }
    
    /**
     * 保存当前进度
     */
    private fun saveCurrentProgress() {
        try {
            if (mediaId.isNullOrBlank()) {
                return
            }
            
            val currentPosition = videoPlayer.currentPositionWhenPlaying / 1000L // 转换为秒
            val duration = videoPlayer.duration / 1000L // 转换为秒
            
            if (currentPosition > 0 && duration > 0) {
                updatePlaybackState(currentPosition, duration)
                Log.d("PlayerActivity", "Saved current progress: ${currentPosition}s / ${duration}s")
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error saving current progress", e)
        }
    }
    
    /**
     * 自动加载字幕
     */
    private fun autoLoadSubtitles() {
        activityScope.launch {
            try {
                val mediaId = mediaId ?: return@launch
                val mediaTitle = mediaTitle ?: return@launch

                Log.d("PlayerActivity", "Auto loading subtitles for: $mediaTitle")

                // 创建MediaItem用于字幕搜索
                val mediaItem = createMediaItemFromIntent()
                if (mediaItem != null) {
                    currentMediaItem = mediaItem

                    // 延迟3秒开始，让播放器先启动
                    delay(3000)

                    // 第一步：检测内嵌字幕
                    Log.d("PlayerActivity", "Detecting embedded subtitles...")
                    val embeddedTracks = subtitleController.detectEmbeddedSubtitles()

                    if (embeddedTracks.isNotEmpty()) {
                        Log.d("PlayerActivity", "Found ${embeddedTracks.size} embedded subtitle tracks")

                        // 检查是否有中文内嵌字幕
                        val bestChineseTrack = subtitleController.getBestChineseEmbeddedTrack()
                        if (bestChineseTrack != null) {
                            Log.d("PlayerActivity", "Found Chinese embedded subtitle: ${bestChineseTrack.getDisplayName()}")

                            // 自动切换到中文内嵌字幕
                            val switchSuccess = subtitleController.switchToEmbeddedSubtitle(bestChineseTrack)
                            if (switchSuccess) {
                                Log.d("PlayerActivity", "Auto-switched to Chinese embedded subtitle")
                                runOnUiThread {
                                    showSubtitleStatus("已自动启用内嵌字幕: ${bestChineseTrack.getDisplayName()}")
                                    updateSubtitleStatusDisplay()
                                }
                                return@launch // 有中文内嵌字幕，不需要搜索外挂字幕
                            }
                        } else {
                            Log.d("PlayerActivity", "No Chinese embedded subtitles found, will search for external subtitles")
                        }
                    } else {
                        Log.d("PlayerActivity", "No embedded subtitles found")
                    }

                    // 第二步：检查本地缓存的外挂字幕
                    val cachedSubtitle = checkCachedSubtitles(mediaItem)
                    if (cachedSubtitle != null) {
                        Log.d("PlayerActivity", "Found cached external subtitle: ${cachedSubtitle.title}")
                        val loadSuccess = subtitleController.loadSubtitle(cachedSubtitle)
                        if (loadSuccess) {
                            Log.d("PlayerActivity", "Cached external subtitle loaded successfully")
                            return@launch
                        }
                    }

                    // 第三步：在线搜索外挂字幕
                    Log.d("PlayerActivity", "Searching online external subtitles...")
                    val searchRequest = com.tvplayer.webdav.data.model.SubtitleSearchRequest.fromMediaItem(mediaItem, subtitleConfig)
                    val searchResult = subtitleManager.searchSubtitles(searchRequest, subtitleConfig)

                    if (searchResult.hasResults) {
                        // 按优先级排序（中文优先）
                        val sortedSubtitles = sortSubtitlesByPriority(searchResult.subtitles)
                        val bestSubtitle = selectBestSubtitleAutomatically(sortedSubtitles)

                        if (bestSubtitle != null) {
                            Log.d("PlayerActivity", "Auto-selected external subtitle: ${bestSubtitle.title} (${bestSubtitle.language})")

                            // 下载并加载字幕
                            val downloadResult = subtitleManager.downloadSubtitle(bestSubtitle, mediaItem.id)
                            downloadResult.fold(
                                onSuccess = { downloadedSubtitle ->
                                    val loadSuccess = subtitleController.loadSubtitle(downloadedSubtitle)
                                    if (loadSuccess) {
                                        Log.d("PlayerActivity", "Auto external subtitle loaded successfully: ${downloadedSubtitle.title}")

                                        // 显示提示信息
                                        runOnUiThread {
                                            showSubtitleStatus("已自动加载外挂字幕: ${downloadedSubtitle.title}")
                                            updateSubtitleStatusDisplay()
                                        }
                                    }
                                },
                                onFailure = { error ->
                                    Log.e("PlayerActivity", "Failed to download auto subtitle: ${error.message}")
                                }
                            )
                        } else {
                            Log.d("PlayerActivity", "No suitable external subtitle found for auto-loading")
                        }
                    } else {
                        Log.d("PlayerActivity", "No external subtitles found online")
                    }
                }

            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error auto loading subtitles", e)
            }
        }
    }

    /**
     * 检查本地缓存的字幕
     */
    private suspend fun checkCachedSubtitles(mediaItem: MediaItem): com.tvplayer.webdav.data.model.Subtitle? {
        return try {
            // 这里应该调用SubtitleCache来检查缓存
            // 目前返回null，表示没有缓存
            null
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error checking cached subtitles", e)
            null
        }
    }

    /**
     * 自动选择最佳字幕
     */
    private fun selectBestSubtitleAutomatically(subtitles: List<com.tvplayer.webdav.data.model.Subtitle>): com.tvplayer.webdav.data.model.Subtitle? {
        if (subtitles.isEmpty()) return null

        // 优先选择简体中文字幕
        val chineseSimplified = subtitles.find { subtitle ->
            val lang = subtitle.language?.lowercase()
            lang == "zh-cn" || lang == "zh_cn" || lang?.contains("simplified") == true
        }
        if (chineseSimplified != null) return chineseSimplified

        // 其次选择繁体中文字幕
        val chineseTraditional = subtitles.find { subtitle ->
            val lang = subtitle.language?.lowercase()
            lang == "zh-tw" || lang == "zh_tw" || lang?.contains("traditional") == true
        }
        if (chineseTraditional != null) return chineseTraditional

        // 再次选择其他中文字幕
        val chinese = subtitles.find { subtitle ->
            val lang = subtitle.language?.lowercase()
            lang == "zh" || lang == "chinese" || lang?.contains("中文") == true
        }
        if (chinese != null) return chinese

        // 最后选择评分最高的字幕
        return subtitles.maxByOrNull { subtitle ->
            val rating = subtitle.metadata["rating"]?.toFloatOrNull() ?: 0f
            val downloads = subtitle.metadata["downloads"]?.toIntOrNull() ?: 0
            rating + downloads * 0.001f
        }
    }
    
    /**
     * 从 Intent 创建 MediaItem
     */
    private fun createMediaItemFromIntent(): MediaItem? {
        return try {
            val title = mediaTitle ?: return null
            val uriString = intent.getStringExtra(EXTRA_URI) ?: return null
            val uri = Uri.parse(uriString)
            
            // 从文件名提取信息
            val fileName = uri.lastPathSegment ?: title
            val year = extractYearFromTitle(title)
            val (season, episode) = extractSeasonEpisodeFromTitle(title)
            
            MediaItem(
                id = mediaId ?: title.hashCode().toString(),
                title = title,
                filePath = uriString,
                mediaType = if (season > 0) com.tvplayer.webdav.data.model.MediaType.TV_EPISODE else com.tvplayer.webdav.data.model.MediaType.MOVIE,
                seasonNumber = season.takeIf { it > 0 },
                episodeNumber = episode.takeIf { it > 0 },
                fileSize = 0L, // 无法获取文件大小
                duration = 0L, // 无法获取时长
                releaseDate = if (year > 0) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, 0, 1)
                    calendar.time
                } else null
            )
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error creating MediaItem from intent", e)
            null
        }
    }
    
    /**
     * 从标题提取年份
     */
    private fun extractYearFromTitle(title: String): Int {
        val yearRegex = """(\d{4})""".toRegex()
        return yearRegex.find(title)?.value?.toIntOrNull() ?: 0
    }
    
    /**
     * 从标题提取季集信息
     */
    private fun extractSeasonEpisodeFromTitle(title: String): Pair<Int, Int> {
        val seRegex = """[Ss](\d+)[Ee](\d+)""".toRegex()
        val match = seRegex.find(title)
        return if (match != null) {
            val season = match.groupValues[1].toIntOrNull() ?: 0
            val episode = match.groupValues[2].toIntOrNull() ?: 0
            Pair(season, episode)
        } else {
            Pair(0, 0)
        }
    }
    
    /**
     * 设置字幕控制UI
     */
    private fun setupSubtitleControlUI() {
        // 获取字幕控制覆盖层
        val subtitleControlOverlay = findViewById<android.view.View>(R.id.subtitle_control_overlay)
        val progressLayout = findViewById<android.view.View>(R.id.layout_subtitle_progress)
        val progressText = findViewById<android.widget.TextView>(R.id.tv_subtitle_progress)

        // 字幕搜索按钮
        val btnSubtitleSearch = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_search)
        btnSubtitleSearch.setOnClickListener {
            searchAndSelectSubtitle()
        }

        // 字幕开关按钮
        val btnSubtitleToggle = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_toggle)
        btnSubtitleToggle.setOnClickListener {
            toggleSubtitleVisibility()
            updateSubtitleToggleButton(btnSubtitleToggle)
        }

        // 字幕设置按钮
        val btnSubtitleSettings = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_settings)
        btnSubtitleSettings.setOnClickListener {
            showSubtitleSettingsDialog()
        }

        // 字幕同步按钮
        val btnSubtitleSync = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_sync)
        btnSubtitleSync.setOnClickListener {
            toggleSubtitleSyncControls()
        }

        // 字幕轨道选择按钮
        val btnSubtitleTrack = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_track)
        btnSubtitleTrack.setOnClickListener {
            showSubtitleTrackSelectionDialog()
        }

        // 字幕时间偏移控制
        val btnOffsetMinus = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_offset_minus)
        val btnOffsetPlus = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_offset_plus)
        val tvOffset = findViewById<android.widget.TextView>(R.id.tv_subtitle_offset)

        btnOffsetMinus.setOnClickListener {
            adjustSubtitleOffset(-0.5f)
            updateOffsetDisplay(tvOffset)
        }

        btnOffsetPlus.setOnClickListener {
            adjustSubtitleOffset(0.5f)
            updateOffsetDisplay(tvOffset)
        }

        // 初始化UI状态
        updateSubtitleToggleButton(btnSubtitleToggle)
        updateOffsetDisplay(tvOffset)

        // 设置控制覆盖层的显示/隐藏逻辑
        setupControlOverlayVisibility(subtitleControlOverlay)
    }

    /**
     * 设置控制覆盖层的显示/隐藏逻辑
     */
    private fun setupControlOverlayVisibility(overlay: android.view.View) {
        // 点击播放器区域时显示/隐藏控制覆盖层
        videoPlayer.setOnClickListener {
            if (overlay.visibility == android.view.View.VISIBLE) {
                overlay.visibility = android.view.View.GONE
            } else {
                overlay.visibility = android.view.View.VISIBLE
                // 3秒后自动隐藏
                progressHandler.postDelayed({
                    overlay.visibility = android.view.View.GONE
                }, 3000)
            }
        }

        // 初始状态隐藏
        overlay.visibility = android.view.View.GONE
    }

    /**
     * 更新字幕开关按钮状态
     */
    private fun updateSubtitleToggleButton(button: android.widget.ImageButton) {
        val isEnabled = subtitleController.isSubtitleEnabled()
        if (isEnabled) {
            button.setImageResource(R.drawable.ic_subtitle_on)
            button.contentDescription = "关闭字幕"
        } else {
            button.setImageResource(R.drawable.ic_subtitle_off)
            button.contentDescription = "开启字幕"
        }
    }

    /**
     * 更新时间偏移显示
     */
    private fun updateOffsetDisplay(textView: android.widget.TextView) {
        val offsetMs = subtitleController.getCurrentConfig().globalOffsetMs
        val offsetSeconds = offsetMs / 1000.0f
        textView.text = String.format("%.1fs", offsetSeconds)
    }

    /**
     * 切换字幕同步控制的显示
     */
    private fun toggleSubtitleSyncControls() {
        val offsetLayout = findViewById<android.view.View>(R.id.layout_subtitle_offset)
        if (offsetLayout.visibility == android.view.View.VISIBLE) {
            offsetLayout.visibility = android.view.View.GONE
        } else {
            offsetLayout.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * 显示字幕设置对话框
     */
    private fun showSubtitleSettingsDialog() {
        val dialog = com.tvplayer.webdav.ui.dialog.SubtitleSettingsDialog(
            context = this,
            currentConfig = subtitleController.getCurrentConfig(),
            onConfigChanged = { newConfig ->
                subtitleController.applySubtitleConfig(newConfig)
                // 更新UI显示
                val tvOffset = findViewById<android.widget.TextView>(R.id.tv_subtitle_offset)
                updateOffsetDisplay(tvOffset)
            }
        )
        dialog.show()
    }

    /**
     * 显示字幕选择对话框
     */
    private fun showSubtitleSelectionDialog(subtitles: List<com.tvplayer.webdav.data.model.Subtitle>) {
        val dialog = SubtitleSelectionDialog(
            context = this,
            onSubtitleSelected = { subtitle ->
                downloadAndLoadSubtitle(subtitle)
            },
            onSearchSubtitles = {
                searchAndSelectSubtitle()
            },
            onConfigSubtitles = {
                showSubtitleSettingsDialog()
            }
        )
        dialog.updateSubtitles(subtitles)
        dialog.show()
    }

    /**
     * 下载并加载字幕
     */
    private fun downloadAndLoadSubtitle(subtitle: com.tvplayer.webdav.data.model.Subtitle) {
        activityScope.launch {
            try {
                val mediaItem = currentMediaItem ?: return@launch

                showSubtitleSearchProgress("正在下载字幕...")

                val downloadResult = subtitleManager.downloadSubtitle(subtitle, mediaItem.id)
                downloadResult.fold(
                    onSuccess = { downloadedSubtitle ->
                        showSubtitleSearchProgress("正在加载字幕...")
                        val loadSuccess = subtitleController.loadSubtitle(downloadedSubtitle)
                        if (loadSuccess) {
                            Toast.makeText(this@PlayerActivity, "字幕加载成功: ${downloadedSubtitle.title}", Toast.LENGTH_SHORT).show()

                            // 更新字幕开关按钮状态
                            val btnToggle = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_toggle)
                            updateSubtitleToggleButton(btnToggle)
                        } else {
                            showRetryDialog("字幕加载失败", "字幕文件可能已损坏，是否重新下载？") {
                                downloadAndLoadSubtitle(subtitle)
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.e("PlayerActivity", "Failed to download subtitle: ${error.message}")
                        showRetryDialog("字幕下载失败", "网络连接可能有问题，是否重试？") {
                            downloadAndLoadSubtitle(subtitle)
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error downloading subtitle", e)
                showRetryDialog("操作失败", "发生未知错误，是否重试？") {
                    downloadAndLoadSubtitle(subtitle)
                }
            } finally {
                hideSubtitleSearchProgress()
            }
        }
    }

    /**
     * 显示重试对话框
     */
    private fun showRetryDialog(title: String, message: String, onRetry: () -> Unit) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("重试") { _, _ ->
                onRetry()
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()
    }

    /**
     * 显示错误提示
     */
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * 显示字幕轨道选择对话框
     */
    private fun showSubtitleTrackSelectionDialog() {
        val embeddedTracks = subtitleController.getEmbeddedSubtitleTracks()
        val currentSubtitle = subtitleController.getCurrentSubtitle()
        val currentEmbeddedTrack = subtitleController.getCurrentEmbeddedTrack()

        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        // 添加"无字幕"选项
        options.add("无字幕")
        actions.add {
            subtitleController.hideSubtitle()
            subtitleController.switchToEmbeddedSubtitle(null)
            showSubtitleStatus("已关闭字幕")
            updateSubtitleStatusDisplay()
        }

        // 添加内嵌字幕轨道选项
        if (embeddedTracks.isNotEmpty()) {
            for (track in embeddedTracks) {
                val displayName = "内嵌: ${track.getDisplayName()}"
                val isSelected = currentEmbeddedTrack?.trackIndex == track.trackIndex
                val finalDisplayName = if (isSelected) "✓ $displayName" else displayName

                options.add(finalDisplayName)
                actions.add {
                    val success = subtitleController.switchToEmbeddedSubtitle(track)
                    if (success) {
                        showSubtitleStatus("已切换到: ${track.getDisplayName()}")
                        updateSubtitleStatusDisplay()
                    } else {
                        showSubtitleStatus("切换失败")
                    }
                }
            }
        }

        // 添加外挂字幕选项
        if (currentSubtitle != null) {
            val displayName = "外挂: ${currentSubtitle.title}"
            val isSelected = !subtitleController.isUsingEmbeddedSubtitle() && subtitleController.isSubtitleEnabled()
            val finalDisplayName = if (isSelected) "✓ $displayName" else displayName

            options.add(finalDisplayName)
            actions.add {
                subtitleController.switchToEmbeddedSubtitle(null) // 禁用内嵌字幕
                subtitleController.showSubtitle() // 显示外挂字幕
                showSubtitleStatus("已切换到外挂字幕")
                updateSubtitleStatusDisplay()
            }
        }

        // 添加"搜索外挂字幕"选项
        options.add("搜索外挂字幕")
        actions.add {
            searchAndSelectSubtitle()
        }

        // 显示选择对话框
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("选择字幕轨道")
            .setItems(options.toTypedArray()) { _, which ->
                if (which < actions.size) {
                    actions[which]()
                }
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()
    }

    /**
     * 显示字幕搜索进度
     */
    private fun showSubtitleSearchProgress(message: String) {
        val progressLayout = findViewById<android.view.View>(R.id.layout_subtitle_progress)
        val progressText = findViewById<android.widget.TextView>(R.id.tv_subtitle_progress)

        progressText.text = message
        progressLayout.visibility = android.view.View.VISIBLE
    }

    /**
     * 隐藏字幕搜索进度
     */
    private fun hideSubtitleSearchProgress() {
        val progressLayout = findViewById<android.view.View>(R.id.layout_subtitle_progress)
        progressLayout.visibility = android.view.View.GONE
    }

    /**
     * 更新字幕状态显示
     */
    private fun updateSubtitleStatusDisplay() {
        val statusTextView = findViewById<android.widget.TextView>(R.id.tv_subtitle_status)

        val currentEmbeddedTrack = subtitleController.getCurrentEmbeddedTrack()
        val currentSubtitle = subtitleController.getCurrentSubtitle()
        val isUsingEmbedded = subtitleController.isUsingEmbeddedSubtitle()
        val isSubtitleEnabled = subtitleController.isSubtitleEnabled()

        when {
            isUsingEmbedded && currentEmbeddedTrack != null -> {
                statusTextView.text = "内嵌: ${currentEmbeddedTrack.getDisplayName()}"
                statusTextView.visibility = android.view.View.VISIBLE

                // 3秒后自动隐藏
                progressHandler.removeCallbacksAndMessages(null)
                progressHandler.postDelayed({
                    statusTextView.visibility = android.view.View.GONE
                }, 3000)
            }
            !isUsingEmbedded && currentSubtitle != null && isSubtitleEnabled -> {
                statusTextView.text = "外挂: ${currentSubtitle.title}"
                statusTextView.visibility = android.view.View.VISIBLE

                // 3秒后自动隐藏
                progressHandler.removeCallbacksAndMessages(null)
                progressHandler.postDelayed({
                    statusTextView.visibility = android.view.View.GONE
                }, 3000)
            }
            else -> {
                statusTextView.visibility = android.view.View.GONE
            }
        }
    }

    /**
     * 显示字幕状态（临时显示）
     */
    private fun showSubtitleStatus(message: String, duration: Long = 2000) {
        val statusTextView = findViewById<android.widget.TextView>(R.id.tv_subtitle_status)
        statusTextView.text = message
        statusTextView.visibility = android.view.View.VISIBLE

        progressHandler.removeCallbacksAndMessages(null)
        progressHandler.postDelayed({
            statusTextView.visibility = android.view.View.GONE
        }, duration)
    }

    /**
     * 手动搜索和选择字幕
     */
    fun searchAndSelectSubtitle() {
        activityScope.launch {
            try {
                val mediaItem = currentMediaItem ?: return@launch

                // 显示搜索进度
                showSubtitleSearchProgress("正在搜索字幕...")

                Log.d("PlayerActivity", "Searching subtitles for: ${mediaItem.title}")

                // 搜索字幕
                val searchRequest = com.tvplayer.webdav.data.model.SubtitleSearchRequest.fromMediaItem(mediaItem, subtitleConfig)
                val searchResult = subtitleManager.searchSubtitles(searchRequest, subtitleConfig)

                if (searchResult.hasResults) {
                    // 按优先级排序字幕（中文优先）
                    val sortedSubtitles = sortSubtitlesByPriority(searchResult.subtitles)

                    if (sortedSubtitles.isNotEmpty()) {
                        // 选择最佳字幕（第一个）
                        val bestSubtitle = sortedSubtitles.first()

                        // 更新进度提示
                        showSubtitleSearchProgress("正在下载字幕...")

                        // 下载字幕
                        val downloadResult = subtitleManager.downloadSubtitle(bestSubtitle, mediaItem.id)

                        downloadResult.fold(
                            onSuccess = { downloadedSubtitle ->
                                showSubtitleSearchProgress("正在加载字幕...")
                                val loadSuccess = subtitleController.loadSubtitle(downloadedSubtitle)
                                if (loadSuccess) {
                                    Log.d("PlayerActivity", "Subtitle downloaded and loaded: ${downloadedSubtitle.title}")
                                    Toast.makeText(this@PlayerActivity, "字幕加载成功: ${downloadedSubtitle.title}", Toast.LENGTH_SHORT).show()

                                    // 更新字幕开关按钮状态
                                    val btnToggle = findViewById<android.widget.ImageButton>(R.id.btn_subtitle_toggle)
                                    updateSubtitleToggleButton(btnToggle)
                                } else {
                                    Toast.makeText(this@PlayerActivity, "字幕加载失败", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFailure = { error ->
                                Log.e("PlayerActivity", "Failed to download subtitle: ${error.message}")
                                Toast.makeText(this@PlayerActivity, "字幕下载失败: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else {
                    Log.d("PlayerActivity", "No subtitles found")
                    Toast.makeText(this@PlayerActivity, "未找到字幕", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error searching subtitles", e)
                Toast.makeText(this@PlayerActivity, "字幕搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                // 隐藏进度提示
                hideSubtitleSearchProgress()
            }
        }
    }

    /**
     * 按优先级排序字幕（中文优先）
     */
    private fun sortSubtitlesByPriority(subtitles: List<com.tvplayer.webdav.data.model.Subtitle>): List<com.tvplayer.webdav.data.model.Subtitle> {
        return subtitles.sortedWith { a, b ->
            // 优先级：简体中文 > 繁体中文 > 其他中文 > 英文 > 其他语言
            val aPriority = getLanguagePriority(a.language)
            val bPriority = getLanguagePriority(b.language)

            when {
                aPriority != bPriority -> aPriority.compareTo(bPriority)
                else -> {
                    // 相同语言按评分和下载量排序
                    val aScore = (a.metadata["rating"]?.toFloatOrNull() ?: 0f) +
                                (a.metadata["downloads"]?.toIntOrNull() ?: 0) * 0.001f
                    val bScore = (b.metadata["rating"]?.toFloatOrNull() ?: 0f) +
                                (b.metadata["downloads"]?.toIntOrNull() ?: 0) * 0.001f
                    bScore.compareTo(aScore) // 降序排列
                }
            }
        }
    }

    /**
     * 获取语言优先级（数字越小优先级越高）
     */
    private fun getLanguagePriority(language: String?): Int {
        return when (language?.lowercase()) {
            "zh-cn", "zh_cn", "chinese (simplified)", "简体中文" -> 1
            "zh-tw", "zh_tw", "chinese (traditional)", "繁体中文" -> 2
            "zh", "chinese", "中文" -> 3
            "en", "english", "英文" -> 4
            else -> 5
        }
    }
    
    /**
     * 切换字幕显示状态
     */
    fun toggleSubtitleVisibility() {
        val isEnabled = subtitleController.isSubtitleEnabled()
        subtitleController.setSubtitleEnabled(!isEnabled)
        
        Log.d("PlayerActivity", "Subtitle visibility toggled: ${!isEnabled}")
    }
    
    /**
     * 调整字幕时间偏移
     * @param offsetSeconds 时间偏移（秒）
     */
    fun adjustSubtitleOffset(offsetSeconds: Float) {
        val currentConfig = subtitleController.getCurrentConfig()
        val currentOffsetSeconds = currentConfig.globalOffsetMs / 1000.0f
        val newOffsetSeconds = currentOffsetSeconds + offsetSeconds

        // 限制偏移范围在 -30 到 +30 秒之间
        val clampedOffsetSeconds = newOffsetSeconds.coerceIn(-30f, 30f)
        val clampedOffsetMs = (clampedOffsetSeconds * 1000).toLong()

        val newConfig = currentConfig.copy(globalOffsetMs = clampedOffsetMs)
        subtitleController.applySubtitleConfig(newConfig)

        Log.d("PlayerActivity", "Subtitle offset adjusted: ${clampedOffsetSeconds}s")
        Toast.makeText(this, "字幕偏移: ${String.format("%.1f", clampedOffsetSeconds)}秒", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示字幕选择对话框
     */
    fun showSubtitleSelectionDialog() {
        activityScope.launch {
            try {
                val mediaItem = currentMediaItem ?: return@launch
                
                val dialog = SubtitleSelectionDialog(
                    this@PlayerActivity,
                    onSubtitleSelected = { subtitle ->
                        // 用户选择了字幕
                        activityScope.launch {
                            if (subtitle.isDownloaded) {
                                // 直接加载已下载的字幕
                                val loadSuccess = subtitleController.loadSubtitle(subtitle)
                                if (loadSuccess) {
                                    Log.d("PlayerActivity", "Subtitle loaded: ${subtitle.title}")
                                    Toast.makeText(this@PlayerActivity, "字幕加载成功", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@PlayerActivity, "字幕加载失败", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // 需要下载字幕
                                Toast.makeText(this@PlayerActivity, "正在下载字幕...", Toast.LENGTH_SHORT).show()
                                
                                val downloadResult = subtitleManager.downloadSubtitle(subtitle, mediaItem.id)
                                downloadResult.fold(
                                    onSuccess = { downloadedSubtitle ->
                                        val loadSuccess = subtitleController.loadSubtitle(downloadedSubtitle)
                                        if (loadSuccess) {
                                            Log.d("PlayerActivity", "Subtitle downloaded and loaded: ${downloadedSubtitle.title}")
                                            Toast.makeText(this@PlayerActivity, "字幕下载并加载成功", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this@PlayerActivity, "字幕加载失败", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onFailure = { error ->
                                        Log.e("PlayerActivity", "Failed to download subtitle", error)
                                        Toast.makeText(this@PlayerActivity, "字幕下载失败: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        }
                    },
                    onSearchSubtitles = {
                        // 搜索新字幕
                        searchAndSelectSubtitle()
                    },
                    onConfigSubtitles = {
                        // 打开设置对话框
                        showSubtitleConfigDialog()
                    }
                )
                
                // 加载已有的字幕
                val availableSubtitles = subtitleManager.getAvailableSubtitles(mediaItem, subtitleConfig)
                dialog.updateSubtitles(availableSubtitles)
                
                dialog.show()
                
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error showing subtitle selection dialog", e)
            }
        }
    }
    
    /**
     * 显示字幕配置对话框
     */
    fun showSubtitleConfigDialog() {
        try {
            val dialog = SubtitleConfigDialog(
                this,
                subtitleController.getCurrentConfig(),
                onConfigChanged = { newConfig ->
                    subtitleConfig = newConfig
                    subtitleController.applySubtitleConfig(newConfig)
                    
                    // 保存配置到本地（可选）
                    saveSubtitleConfig(newConfig)
                    
                    Log.d("PlayerActivity", "Subtitle config updated")
                    Toast.makeText(this, "字幕设置已保存", Toast.LENGTH_SHORT).show()
                }
            )
            
            dialog.show()
            
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error showing subtitle config dialog", e)
        }
    }
    
    /**
     * 保存字幕配置
     */
    private fun saveSubtitleConfig(config: SubtitleConfig) {
        try {
            val prefs = getSharedPreferences("subtitle_config", MODE_PRIVATE)
            val gson = com.google.gson.Gson()
            prefs.edit()
                .putString("config", gson.toJson(config))
                .apply()
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error saving subtitle config", e)
        }
    }
    
    /**
     * 加载字幕配置
     */
    private fun loadSubtitleConfig(): SubtitleConfig {
        return try {
            val prefs = getSharedPreferences("subtitle_config", MODE_PRIVATE)
            val configJson = prefs.getString("config", null)
            if (configJson != null) {
                val gson = com.google.gson.Gson()
                gson.fromJson(configJson, SubtitleConfig::class.java)
            } else {
                SubtitleConfig.getDefault()
            }
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error loading subtitle config", e)
            SubtitleConfig.getDefault()
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_START_POSITION = "start_position"
        const val EXTRA_MEDIA_ID = "media_id"
        const val EXTRA_MEDIA_TITLE = "media_title"

        fun intentFor(context: android.content.Context, title: String, uri: Uri): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_URI, uri.toString())
            }
        }
        
        /**
         * 创建带有播放位置的Intent
         */
        fun intentForWithPosition(
            context: android.content.Context, 
            title: String, 
            uri: Uri, 
            startPosition: Long,
            mediaId: String
        ): Intent {
            return Intent(context, PlayerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_URI, uri.toString())
                putExtra(EXTRA_START_POSITION, startPosition)
                putExtra(EXTRA_MEDIA_ID, mediaId)
                putExtra(EXTRA_MEDIA_TITLE, title)
            }
        }
    }
}

