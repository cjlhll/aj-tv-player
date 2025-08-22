package com.tvplayer.webdav.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Video playback Activity using GSYVideoPlayer with WebDAV support.
 * Supports both local files and WebDAV URLs with authentication.
 */
@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var playbackStateManager: PlaybackStateManager
    
    private lateinit var videoPlayer: StandardGSYVideoPlayer
    private var mediaId: String? = null
    private var mediaTitle: String? = null
    private var startPosition: Long = 0L // 秒为单位
    
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

