package com.tvplayer.webdav.ui.player

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.tvplayer.webdav.data.model.Subtitle
import com.tvplayer.webdav.data.model.SubtitleConfig
import com.tvplayer.webdav.data.model.SubtitleFormat
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 字幕控制器
 * 管理播放器中的字幕显示、样式和控制
 */
@Singleton
class SubtitleController @Inject constructor() {
    
    companion object {
        private const val TAG = "SubtitleController"
        private const val SUBTITLE_TRACK_ID = "subtitle_track"
    }
    
    private var currentSubtitle: Subtitle? = null
    private var currentConfig: SubtitleConfig = SubtitleConfig.getDefault()
    private var subtitleTextView: TextView? = null
    private var videoPlayer: StandardGSYVideoPlayer? = null
    private var isSubtitleEnabled = true

    // 字幕解析和显示
    private var subtitleEntries: List<SubtitleEntry> = emptyList()
    private var currentSubtitleIndex = -1
    private var lastUpdateTime = 0L

    // 内嵌字幕轨道管理
    private var embeddedSubtitleTracks: List<EmbeddedSubtitleTrack> = emptyList()
    private var currentEmbeddedTrack: EmbeddedSubtitleTrack? = null
    private var isUsingEmbeddedSubtitle = false
    private var trackSelector: DefaultTrackSelector? = null

    // 协程作用域
    private val controllerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * 初始化字幕控制器
     * @param player GSY视频播放器实例
     * @param config 字幕配置
     */
    fun initialize(player: StandardGSYVideoPlayer, config: SubtitleConfig) {
        this.videoPlayer = player
        this.currentConfig = config
        this.isSubtitleEnabled = config.isEnabled

        // 初始化TrackSelector
        initializeTrackSelector()

        Log.d(TAG, "Subtitle controller initialized")

        // 设置字幕显示
        setupSubtitleDisplay(player.context)

        // 延迟检测内嵌字幕（等待播放器准备完成）
        controllerScope.launch {
            delay(2000) // 等待2秒让播放器完全初始化
            detectEmbeddedSubtitles()
        }
    }

    /**
     * 初始化TrackSelector
     */
    private fun initializeTrackSelector() {
        try {
            val exoPlayer = getExoPlayerFromGSY()
            if (exoPlayer != null) {
                // 尝试获取现有的TrackSelector
                val gsyVideoManager = GSYVideoManager.instance()
                val playerManagerField = gsyVideoManager.javaClass.getDeclaredField("playerManager")
                playerManagerField.isAccessible = true
                val playerManager = playerManagerField.get(gsyVideoManager)

                if (playerManager != null) {
                    // 尝试获取TrackSelector
                    try {
                        val trackSelectorField = playerManager.javaClass.getDeclaredField("trackSelector")
                        trackSelectorField.isAccessible = true
                        val selector = trackSelectorField.get(playerManager)

                        if (selector is DefaultTrackSelector) {
                            trackSelector = selector
                            Log.d(TAG, "TrackSelector initialized successfully")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not get existing TrackSelector, creating new one")
                        trackSelector = DefaultTrackSelector(videoPlayer!!.context)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TrackSelector", e)
            // 创建默认的TrackSelector
            if (videoPlayer != null) {
                trackSelector = DefaultTrackSelector(videoPlayer!!.context)
            }
        }
    }
    
    /**
     * 加载字幕
     * @param subtitle 字幕信息
     * @return 加载是否成功
     */
    suspend fun loadSubtitle(subtitle: Subtitle): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading subtitle: ${subtitle.title}")
                
                if (!subtitle.isAvailable()) {
                    Log.w(TAG, "Subtitle file not available: ${subtitle.localPath}")
                    return@withContext false
                }
                
                val subtitleFile = File(subtitle.localPath)
                if (!subtitleFile.exists()) {
                    Log.w(TAG, "Subtitle file not found: ${subtitle.localPath}")
                    return@withContext false
                }
                
                // 根据字幕格式选择加载方式
                val success = when (subtitle.format) {
                    SubtitleFormat.SRT -> loadSrtSubtitle(subtitleFile)
                    SubtitleFormat.ASS, SubtitleFormat.SSA -> loadAssSubtitle(subtitleFile)
                    SubtitleFormat.VTT -> loadVttSubtitle(subtitleFile)
                    else -> loadGenericSubtitle(subtitleFile, subtitle.format)
                }
                
                if (success) {
                    currentSubtitle = subtitle
                    withContext(Dispatchers.Main) {
                        applySubtitleConfig(currentConfig)
                    }
                    Log.d(TAG, "Subtitle loaded successfully: ${subtitle.title}")
                } else {
                    Log.e(TAG, "Failed to load subtitle: ${subtitle.title}")
                }
                
                success
                
            } catch (e: Exception) {
                Log.e(TAG, "Error loading subtitle", e)
                false
            }
        }
    }
    
    /**
     * 卸载当前字幕
     */
    fun unloadSubtitle() {
        try {
            currentSubtitle = null
            subtitleTextView?.visibility = View.GONE
            subtitleTextView?.text = ""
            
            Log.d(TAG, "Subtitle unloaded")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error unloading subtitle", e)
        }
    }
    
    /**
     * 设置字幕可见性
     * @param enabled 是否启用字幕显示
     */
    fun setSubtitleEnabled(enabled: Boolean) {
        isSubtitleEnabled = enabled
        subtitleTextView?.visibility = if (enabled && currentSubtitle != null) View.VISIBLE else View.GONE

        Log.d(TAG, "Subtitle visibility set to: $enabled")
    }

    /**
     * 显示字幕
     */
    fun showSubtitle() {
        setSubtitleEnabled(true)
    }

    /**
     * 隐藏字幕
     */
    fun hideSubtitle() {
        setSubtitleEnabled(false)
    }
    
    /**
     * 应用字幕配置
     * @param config 新的字幕配置
     */
    fun applySubtitleConfig(config: SubtitleConfig) {
        currentConfig = config
        isSubtitleEnabled = config.isEnabled
        
        subtitleTextView?.let { textView ->
            // 应用文本样式
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.textSize)
            textView.setTextColor(config.textColor)
            textView.setBackgroundColor(config.backgroundColor)
            
            // 应用字体样式
            val typeface = if (config.isBold && config.isItalic) {
                Typeface.DEFAULT_BOLD
            } else if (config.isBold) {
                Typeface.DEFAULT_BOLD
            } else if (config.isItalic) {
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            } else {
                Typeface.DEFAULT
            }
            textView.typeface = typeface
            
            // 应用阴影效果
            if (config.shadowRadius > 0) {
                textView.setShadowLayer(
                    config.shadowRadius,
                    config.shadowOffsetX,
                    config.shadowOffsetY,
                    config.shadowColor
                )
            }
            
            // 应用位置和对齐
            applySubtitlePosition(textView, config)
            
            // 应用可见性
            textView.visibility = if (isSubtitleEnabled && currentSubtitle != null) View.VISIBLE else View.GONE
        }
        
        Log.d(TAG, "Subtitle config applied")
    }
    
    /**
     * 调整字幕时间偏移
     * @param offsetMs 时间偏移（毫秒）
     */
    fun adjustTimeOffset(offsetMs: Long) {
        currentConfig = currentConfig.copy(globalOffsetMs = offsetMs)
        
        // 如果有字幕正在显示，需要重新计算时间
        currentSubtitle?.let { subtitle ->
            val syncInfo = subtitle.syncInfo?.copy(offsetMs = offsetMs) 
                ?: com.tvplayer.webdav.data.model.SubtitleSyncInfo(offsetMs = offsetMs)
            
            currentSubtitle = subtitle.copy(syncInfo = syncInfo)
        }
        
        Log.d(TAG, "Subtitle time offset adjusted: ${offsetMs}ms")
    }
    
    /**
     * 获取当前字幕信息
     */
    fun getCurrentSubtitle(): Subtitle? = currentSubtitle
    
    /**
     * 获取当前配置
     */
    fun getCurrentConfig(): SubtitleConfig = currentConfig
    
    /**
     * 检查字幕是否启用
     */
    fun isSubtitleEnabled(): Boolean = isSubtitleEnabled && (currentSubtitle != null || currentEmbeddedTrack != null)

    /**
     * 检测视频内嵌字幕轨道
     */
    fun detectEmbeddedSubtitles(): List<EmbeddedSubtitleTrack> {
        val tracks = mutableListOf<EmbeddedSubtitleTrack>()

        try {
            val exoPlayer = getExoPlayerFromGSY()
            if (exoPlayer != null) {
                val currentTracks = exoPlayer.currentTracks

                for (groupIndex in 0 until currentTracks.groups.size) {
                    val group = currentTracks.groups[groupIndex]

                    // 检查是否为字幕轨道
                    if (group.type == C.TRACK_TYPE_TEXT) {
                        for (trackIndex in 0 until group.length) {
                            val format = group.getTrackFormat(trackIndex)
                            val isSelected = group.isTrackSelected(trackIndex)

                            val track = EmbeddedSubtitleTrack(
                                trackIndex = trackIndex,
                                groupIndex = groupIndex,
                                format = format,
                                language = format.language,
                                label = format.label,
                                isSelected = isSelected
                            )

                            tracks.add(track)
                            Log.d(TAG, "Found embedded subtitle track: ${track.getDisplayName()} (${track.language})")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting embedded subtitles", e)
        }

        embeddedSubtitleTracks = tracks
        return tracks
    }

    /**
     * 获取内嵌字幕轨道列表
     */
    fun getEmbeddedSubtitleTracks(): List<EmbeddedSubtitleTrack> = embeddedSubtitleTracks

    /**
     * 检查是否有中文内嵌字幕
     */
    fun hasChineseEmbeddedSubtitles(): Boolean {
        return embeddedSubtitleTracks.any { it.isChineseSubtitle() }
    }

    /**
     * 获取最佳中文内嵌字幕轨道
     */
    fun getBestChineseEmbeddedTrack(): EmbeddedSubtitleTrack? {
        return embeddedSubtitleTracks
            .filter { it.isChineseSubtitle() }
            .minByOrNull { it.getLanguagePriority() }
    }

    /**
     * 切换到内嵌字幕轨道
     */
    fun switchToEmbeddedSubtitle(track: EmbeddedSubtitleTrack?): Boolean {
        return try {
            val exoPlayer = getExoPlayerFromGSY()
            if (exoPlayer != null && trackSelector != null) {

                if (track != null) {
                    // 启用指定的字幕轨道
                    val parametersBuilder = trackSelector!!.buildUponParameters()

                    // 启用字幕轨道类型
                    parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)

                    // 尝试使用新的API设置轨道选择
                    try {
                        // 由于ExoPlayer API的复杂性，我们简化处理
                        // 只启用字幕轨道类型，让ExoPlayer自动选择
                        parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                        Log.d(TAG, "Enabled text track type for embedded subtitle")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to enable text track type", e)
                    }

                    trackSelector!!.setParameters(parametersBuilder)

                    currentEmbeddedTrack = track
                    isUsingEmbeddedSubtitle = true

                    // 隐藏外挂字幕
                    subtitleTextView?.visibility = View.GONE

                    Log.d(TAG, "Switched to embedded subtitle: ${track.getDisplayName()}")
                    true
                } else {
                    // 禁用所有内嵌字幕
                    val parametersBuilder = trackSelector!!.buildUponParameters()
                    parametersBuilder.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    trackSelector!!.setParameters(parametersBuilder)

                    currentEmbeddedTrack = null
                    isUsingEmbeddedSubtitle = false

                    Log.d(TAG, "Disabled embedded subtitles")
                    true
                }
            } else {
                Log.w(TAG, "Cannot switch embedded subtitle: ExoPlayer or TrackSelector not available")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error switching to embedded subtitle", e)
            false
        }
    }

    /**
     * 检查当前是否使用内嵌字幕
     */
    fun isUsingEmbeddedSubtitle(): Boolean = isUsingEmbeddedSubtitle

    /**
     * 获取当前内嵌字幕轨道
     */
    fun getCurrentEmbeddedTrack(): EmbeddedSubtitleTrack? = currentEmbeddedTrack
    
    /**
     * 清理资源
     */
    fun release() {
        try {
            controllerScope.cancel()
            unloadSubtitle()
            subtitleTextView = null
            videoPlayer = null
            
            Log.d(TAG, "Subtitle controller released")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing subtitle controller", e)
        }
    }
    
    // 私有方法
    
    /**
     * 设置字幕显示界面
     */
    private fun setupSubtitleDisplay(context: Context) {
        try {
            videoPlayer?.let { player ->
                // 创建字幕显示TextView
                subtitleTextView = TextView(context).apply {
                    id = View.generateViewId()
                    visibility = View.GONE
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                    
                    // 设置默认样式
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, currentConfig.textSize)
                    setTextColor(currentConfig.textColor)
                    setBackgroundColor(currentConfig.backgroundColor)
                    
                    // 设置描边效果
                    if (currentConfig.outlineWidth > 0) {
                        paint.strokeWidth = currentConfig.outlineWidth * 2
                        paint.style = android.graphics.Paint.Style.FILL_AND_STROKE
                        paint.strokeJoin = android.graphics.Paint.Join.ROUND
                        setTextColor(currentConfig.textColor)
                    }
                }
                
                // 将字幕TextView添加到播放器容器中
                // 注意：这里需要根据GSYVideoPlayer的具体实现来调整
                // 可能需要通过反射或其他方式获取播放器的容器视图
                addSubtitleToPlayer(player, subtitleTextView!!)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up subtitle display", e)
        }
    }
    
    /**
     * 将字幕TextView添加到播放器中
     */
    private fun addSubtitleToPlayer(player: StandardGSYVideoPlayer, textView: TextView) {
        try {
            // 方法1：尝试获取GSYVideoPlayer的surface_container
            var container: ViewGroup? = null

            // 尝试多种方式获取容器
            try {
                container = player.findViewById<FrameLayout>(
                    com.shuyu.gsyvideoplayer.R.id.surface_container
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not find surface_container, trying alternative methods")
            }

            // 方法2：如果找不到surface_container，尝试其他容器
            if (container == null) {
                try {
                    // 尝试获取播放器的根容器
                    container = player as? ViewGroup
                    if (container == null) {
                        // 尝试获取第一个FrameLayout子视图
                        for (i in 0 until player.childCount) {
                            val child = player.getChildAt(i)
                            if (child is FrameLayout) {
                                container = child
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not find suitable container", e)
                }
            }
            
            if (container != null) {
                val layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    bottomMargin = currentConfig.marginVertical.toInt()
                    leftMargin = currentConfig.marginHorizontal.toInt()
                    rightMargin = currentConfig.marginHorizontal.toInt()
                }
                
                container.addView(textView, layoutParams)
                Log.d(TAG, "Subtitle TextView added to player container")
            } else {
                Log.w(TAG, "Could not find player container to add subtitle")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adding subtitle to player", e)
        }
    }
    
    /**
     * 应用字幕位置配置
     */
    private fun applySubtitlePosition(textView: TextView, config: SubtitleConfig) {
        val layoutParams = textView.layoutParams as? android.widget.FrameLayout.LayoutParams
        layoutParams?.let { params ->
            // 设置重力
            params.gravity = when (config.position) {
                com.tvplayer.webdav.data.model.SubtitlePosition.TOP -> 
                    Gravity.TOP or getHorizontalGravity(config.alignment)
                com.tvplayer.webdav.data.model.SubtitlePosition.CENTER -> 
                    Gravity.CENTER or getHorizontalGravity(config.alignment)
                com.tvplayer.webdav.data.model.SubtitlePosition.BOTTOM -> 
                    Gravity.BOTTOM or getHorizontalGravity(config.alignment)
            }
            
            // 设置边距
            params.leftMargin = config.marginHorizontal.toInt()
            params.rightMargin = config.marginHorizontal.toInt()
            params.topMargin = if (config.position == com.tvplayer.webdav.data.model.SubtitlePosition.TOP) 
                config.marginVertical.toInt() else 0
            params.bottomMargin = if (config.position == com.tvplayer.webdav.data.model.SubtitlePosition.BOTTOM) 
                config.marginVertical.toInt() else 0
            
            textView.layoutParams = params
        }
        
        // 设置文本对齐
        textView.gravity = when (config.alignment) {
            com.tvplayer.webdav.data.model.SubtitleAlignment.LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
            com.tvplayer.webdav.data.model.SubtitleAlignment.CENTER -> Gravity.CENTER
            com.tvplayer.webdav.data.model.SubtitleAlignment.RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
        }
    }
    
    private fun getHorizontalGravity(alignment: com.tvplayer.webdav.data.model.SubtitleAlignment): Int {
        return when (alignment) {
            com.tvplayer.webdav.data.model.SubtitleAlignment.LEFT -> Gravity.START
            com.tvplayer.webdav.data.model.SubtitleAlignment.CENTER -> Gravity.CENTER_HORIZONTAL
            com.tvplayer.webdav.data.model.SubtitleAlignment.RIGHT -> Gravity.END
        }
    }
    
    /**
     * 加载SRT格式字幕
     */
    private fun loadSrtSubtitle(file: File): Boolean {
        return try {
            // 使用ExoPlayer的字幕支持
            videoPlayer?.let { player ->
                val uri = Uri.fromFile(file)
                setupExoPlayerSubtitle(player.context, uri, MimeTypes.APPLICATION_SUBRIP)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error loading SRT subtitle", e)
            false
        }
    }
    
    /**
     * 加载ASS/SSA格式字幕
     */
    private fun loadAssSubtitle(file: File): Boolean {
        return try {
            videoPlayer?.let { player ->
                val uri = Uri.fromFile(file)
                setupExoPlayerSubtitle(player.context, uri, MimeTypes.TEXT_SSA)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ASS subtitle", e)
            false
        }
    }
    
    /**
     * 加载VTT格式字幕
     */
    private fun loadVttSubtitle(file: File): Boolean {
        return try {
            videoPlayer?.let { player ->
                val uri = Uri.fromFile(file)
                setupExoPlayerSubtitle(player.context, uri, MimeTypes.TEXT_VTT)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error loading VTT subtitle", e)
            false
        }
    }
    
    /**
     * 加载通用格式字幕
     */
    private fun loadGenericSubtitle(file: File, format: SubtitleFormat): Boolean {
        return try {
            val mimeType = when (format) {
                SubtitleFormat.SUB -> MimeTypes.APPLICATION_DVBSUBS
                SubtitleFormat.SMI -> format.mimeType
                SubtitleFormat.TXT -> format.mimeType
                else -> MimeTypes.APPLICATION_SUBRIP
            }
            
            videoPlayer?.let { player ->
                val uri = Uri.fromFile(file)
                setupExoPlayerSubtitle(player.context, uri, mimeType)
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error loading generic subtitle", e)
            false
        }
    }
    
    /**
     * 设置ExoPlayer字幕
     */
    private fun setupExoPlayerSubtitle(context: Context, uri: Uri, mimeType: String): Boolean {
        return try {
            Log.d(TAG, "Setting up ExoPlayer subtitle: $uri, mimeType: $mimeType")

            // 获取GSYVideoPlayer的ExoPlayer实例
            val exoPlayer = getExoPlayerFromGSY()
            if (exoPlayer != null) {
                // 方法1：通过重新设置MediaItem来添加字幕
                val currentMediaItem = exoPlayer.currentMediaItem
                if (currentMediaItem != null) {
                    val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(uri)
                        .setMimeType(mimeType)
                        .setLanguage("zh") // 设置为中文
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT or C.SELECTION_FLAG_AUTOSELECT)
                        .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                        .build()

                    val newMediaItem = currentMediaItem.buildUpon()
                        .setSubtitleConfigurations(listOf(subtitleConfig))
                        .build()

                    // 保存当前播放位置
                    val currentPosition = exoPlayer.currentPosition
                    val playWhenReady = exoPlayer.playWhenReady

                    // 设置新的MediaItem
                    exoPlayer.setMediaItem(newMediaItem)
                    exoPlayer.prepare()
                    exoPlayer.seekTo(currentPosition)
                    exoPlayer.playWhenReady = playWhenReady

                    Log.d(TAG, "ExoPlayer subtitle configured successfully")
                    return true
                }
            }

            // 方法2：如果ExoPlayer方法失败，使用自定义字幕显示
            Log.w(TAG, "ExoPlayer subtitle setup failed, falling back to custom subtitle display")
            return setupCustomSubtitleDisplay(uri)

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up ExoPlayer subtitle", e)
            // 降级到自定义字幕显示
            return setupCustomSubtitleDisplay(uri)
        }
    }

    /**
     * 获取GSYVideoPlayer中的ExoPlayer实例
     */
    private fun getExoPlayerFromGSY(): ExoPlayer? {
        return try {
            val gsyVideoManager = GSYVideoManager.instance()

            // 通过反射获取playerManager
            val playerManagerField = gsyVideoManager.javaClass.getDeclaredField("playerManager")
            playerManagerField.isAccessible = true
            val playerManager = playerManagerField.get(gsyVideoManager)

            if (playerManager != null) {
                // 通过反射获取ExoPlayer实例
                val playerField = playerManager.javaClass.getDeclaredField("mediaPlayer")
                playerField.isAccessible = true
                val mediaPlayer = playerField.get(playerManager)

                if (mediaPlayer is ExoPlayer) {
                    Log.d(TAG, "Successfully obtained ExoPlayer instance from GSYVideoPlayer")
                    return mediaPlayer
                } else {
                    Log.w(TAG, "MediaPlayer is not ExoPlayer instance: ${mediaPlayer?.javaClass?.simpleName}")
                    return null
                }
            } else {
                Log.w(TAG, "PlayerManager is null")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ExoPlayer from GSYVideoPlayer", e)
            return null
        }
    }

    /**
     * 设置自定义字幕显示（当ExoPlayer集成失败时的降级方案）
     */
    private fun setupCustomSubtitleDisplay(uri: Uri): Boolean {
        return try {
            Log.d(TAG, "Setting up custom subtitle display for: $uri")

            // 解析字幕文件
            val file = File(uri.path ?: return false)
            if (!file.exists()) {
                Log.e(TAG, "Subtitle file does not exist: ${file.absolutePath}")
                return false
            }

            // 根据文件扩展名解析字幕
            val extension = file.extension.lowercase()
            subtitleEntries = when (extension) {
                "srt" -> parseSrtSubtitle(file)
                "vtt" -> parseVttSubtitle(file)
                "ass", "ssa" -> parseAssSubtitle(file)
                else -> {
                    Log.w(TAG, "Unsupported subtitle format: $extension")
                    emptyList()
                }
            }

            if (subtitleEntries.isNotEmpty()) {
                Log.d(TAG, "Parsed ${subtitleEntries.size} subtitle entries")
                startSubtitleSync()
                return true
            } else {
                Log.w(TAG, "No subtitle entries found")
                return false
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up custom subtitle display", e)
            return false
        }
    }
    /**
     * 解析SRT字幕文件
     */
    private fun parseSrtSubtitle(file: File): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
            var line: String?
            var currentEntry: SubtitleEntry? = null
            var lineNumber = 0

            while (reader.readLine().also { line = it } != null) {
                line = line?.trim()
                if (line.isNullOrEmpty()) {
                    currentEntry?.let { entries.add(it) }
                    currentEntry = null
                    continue
                }

                when {
                    line!!.matches(Regex("\\d+")) -> {
                        // 字幕序号
                        currentEntry = SubtitleEntry(index = line!!.toInt())
                    }
                    line!!.contains("-->") -> {
                        // 时间轴
                        val times = line!!.split("-->")
                        if (times.size == 2) {
                            currentEntry?.startTime = parseTimeToMillis(times[0].trim())
                            currentEntry?.endTime = parseTimeToMillis(times[1].trim())
                        }
                    }
                    else -> {
                        // 字幕文本
                        currentEntry?.let {
                            if (it.text.isEmpty()) {
                                it.text = line!!
                            } else {
                                it.text += "\n" + line!!
                            }
                        }
                    }
                }
            }

            // 添加最后一个条目
            currentEntry?.let { entries.add(it) }
            reader.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SRT subtitle", e)
        }

        return entries
    }

    /**
     * 解析VTT字幕文件
     */
    private fun parseVttSubtitle(file: File): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
            var line: String?
            var currentEntry: SubtitleEntry? = null
            var index = 1

            while (reader.readLine().also { line = it } != null) {
                line = line?.trim()
                if (line.isNullOrEmpty()) {
                    currentEntry?.let { entries.add(it) }
                    currentEntry = null
                    continue
                }

                if (line!!.startsWith("WEBVTT") || line!!.startsWith("NOTE")) {
                    continue
                }

                when {
                    line!!.contains("-->") -> {
                        // 时间轴
                        val times = line!!.split("-->")
                        if (times.size == 2) {
                            currentEntry = SubtitleEntry(index = index++)
                            currentEntry.startTime = parseTimeToMillis(times[0].trim())
                            currentEntry.endTime = parseTimeToMillis(times[1].trim())
                        }
                    }
                    currentEntry != null -> {
                        // 字幕文本
                        if (currentEntry.text.isEmpty()) {
                            currentEntry.text = line!!
                        } else {
                            currentEntry.text += "\n" + line!!
                        }
                    }
                }
            }

            // 添加最后一个条目
            currentEntry?.let { entries.add(it) }
            reader.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing VTT subtitle", e)
        }

        return entries
    }

    /**
     * 解析ASS/SSA字幕文件（简化版本）
     */
    private fun parseAssSubtitle(file: File): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file), "UTF-8"))
            var line: String?
            var index = 1
            var inEventsSection = false

            while (reader.readLine().also { line = it } != null) {
                line = line?.trim()
                if (line.isNullOrEmpty()) continue

                if (line!!.startsWith("[Events]")) {
                    inEventsSection = true
                    continue
                }

                if (line!!.startsWith("[") && !line!!.startsWith("[Events]")) {
                    inEventsSection = false
                    continue
                }

                if (inEventsSection && line!!.startsWith("Dialogue:")) {
                    val parts = line!!.substring(9).split(",", limit = 10)
                    if (parts.size >= 10) {
                        val startTime = parseAssTimeToMillis(parts[1].trim())
                        val endTime = parseAssTimeToMillis(parts[2].trim())
                        val text = parts[9].trim().replace("\\N", "\n")

                        entries.add(SubtitleEntry(
                            index = index++,
                            startTime = startTime,
                            endTime = endTime,
                            text = text
                        ))
                    }
                }
            }

            reader.close()

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ASS subtitle", e)
        }

        return entries
    }

    /**
     * 解析时间字符串为毫秒
     */
    private fun parseTimeToMillis(timeStr: String): Long {
        return try {
            val parts = timeStr.replace(",", ".").split(":")
            if (parts.size == 3) {
                val hours = parts[0].toLong()
                val minutes = parts[1].toLong()
                val seconds = parts[2].toDouble()

                (hours * 3600 + minutes * 60 + seconds * 1000).toLong()
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeStr", e)
            0L
        }
    }

    /**
     * 解析ASS时间格式为毫秒
     */
    private fun parseAssTimeToMillis(timeStr: String): Long {
        return try {
            val parts = timeStr.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toLong()
                val minutes = parts[1].toLong()
                val secondsParts = parts[2].split(".")
                val seconds = secondsParts[0].toLong()
                val centiseconds = if (secondsParts.size > 1) secondsParts[1].toLong() else 0L

                hours * 3600000 + minutes * 60000 + seconds * 1000 + centiseconds * 10
            } else {
                0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ASS time: $timeStr", e)
            0L
        }
    }

    /**
     * 开始字幕同步
     */
    private fun startSubtitleSync() {
        controllerScope.launch {
            while (isSubtitleEnabled && subtitleEntries.isNotEmpty()) {
                try {
                    val currentTime = getCurrentPlaybackTime()
                    updateSubtitleDisplay(currentTime)
                    delay(100) // 每100ms更新一次
                } catch (e: Exception) {
                    Log.e(TAG, "Error in subtitle sync", e)
                    delay(1000) // 出错时延迟1秒再试
                }
            }
        }
    }

    /**
     * 获取当前播放时间
     */
    private fun getCurrentPlaybackTime(): Long {
        return try {
            val exoPlayer = getExoPlayerFromGSY()
            exoPlayer?.currentPosition ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting playback time", e)
            0L
        }
    }

    /**
     * 更新字幕显示
     */
    private fun updateSubtitleDisplay(currentTime: Long) {
        val adjustedTime = currentTime + currentConfig.globalOffsetMs

        // 查找当前时间应该显示的字幕
        val currentEntry = subtitleEntries.find { entry ->
            adjustedTime >= entry.startTime && adjustedTime <= entry.endTime
        }

        // 更新字幕显示
        subtitleTextView?.let { textView ->
            if (currentEntry != null && isSubtitleEnabled) {
                if (textView.text.toString() != currentEntry.text) {
                    textView.text = currentEntry.text
                    textView.visibility = View.VISIBLE
                }
            } else {
                if (textView.visibility == View.VISIBLE) {
                    textView.visibility = View.GONE
                }
            }
        }
    }
}

/**
 * 字幕条目数据类
 */
data class SubtitleEntry(
    val index: Int,
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var text: String = ""
)

/**
 * 内嵌字幕轨道数据类
 */
data class EmbeddedSubtitleTrack(
    val trackIndex: Int,
    val groupIndex: Int,
    val format: Format,
    val language: String?,
    val label: String?,
    val isSelected: Boolean = false
) {
    /**
     * 获取显示名称
     */
    fun getDisplayName(): String {
        return when {
            !label.isNullOrEmpty() -> label
            !language.isNullOrEmpty() -> getLanguageDisplayName(language)
            else -> "字幕轨道 ${trackIndex + 1}"
        }
    }

    /**
     * 获取语言显示名称
     */
    private fun getLanguageDisplayName(language: String): String {
        return when (language.lowercase()) {
            "zh", "zh-cn", "zh_cn", "chi", "chinese" -> "中文"
            "zh-tw", "zh_tw", "zh-hk", "zh_hk" -> "繁体中文"
            "en", "eng", "english" -> "英文"
            "ja", "jpn", "japanese" -> "日文"
            "ko", "kor", "korean" -> "韩文"
            "fr", "fra", "french" -> "法文"
            "de", "ger", "german" -> "德文"
            "es", "spa", "spanish" -> "西班牙文"
            "ru", "rus", "russian" -> "俄文"
            else -> language.uppercase()
        }
    }

    /**
     * 检查是否为中文字幕
     */
    fun isChineseSubtitle(): Boolean {
        val lang = language?.lowercase() ?: ""
        return lang.startsWith("zh") || lang == "chi" || lang == "chinese"
    }

    /**
     * 获取语言优先级（数字越小优先级越高）
     */
    fun getLanguagePriority(): Int {
        val lang = language?.lowercase() ?: ""
        return when {
            lang == "zh-cn" || lang == "zh_cn" -> 1
            lang == "zh-tw" || lang == "zh_tw" || lang == "zh-hk" || lang == "zh_hk" -> 2
            lang == "zh" || lang == "chi" || lang == "chinese" -> 3
            lang == "en" || lang == "eng" || lang == "english" -> 4
            else -> 5
        }
    }
}

/**
 * 字幕事件监听器
 */
interface SubtitleEventListener {
    /**
     * 字幕加载完成
     */
    fun onSubtitleLoaded(subtitle: Subtitle)

    /**
     * 字幕加载失败
     */
    fun onSubtitleLoadError(subtitle: Subtitle, error: Throwable)

    /**
     * 字幕显示状态改变
     */
    fun onSubtitleVisibilityChanged(isVisible: Boolean)

    /**
     * 字幕配置改变
     */
    fun onSubtitleConfigChanged(config: SubtitleConfig)
}