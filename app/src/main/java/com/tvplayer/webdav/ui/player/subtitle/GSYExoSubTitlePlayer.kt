package com.tvplayer.webdav.ui.player.subtitle

import android.content.Context
import android.net.Uri
import android.view.Surface
import androidx.annotation.Nullable
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import tv.danmaku.ijk.media.player.AbstractMediaPlayer
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.MediaInfo
import java.io.File

/**
 * 字幕播放器 - 基于官方实现
 */
class GSYExoSubTitlePlayer(private val context: Context) : AbstractMediaPlayer() {

    private var exoPlayer: ExoPlayer? = null
    private var subtitlePath: String? = null
    private var textOutput: Player.Listener? = null
    private var isPreview = false
    private var cacheEnabled = false
    private var cacheDir: File? = null
    private var overrideExtension: String? = null
    private val textOutputs = mutableListOf<Player.Listener>()

    init {
        initPlayer()
    }

    private fun initPlayer() {
        exoPlayer = ExoPlayer.Builder(context).build()
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        notifyOnPrepared()
                    }
                    Player.STATE_ENDED -> {
                        notifyOnCompletion()
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
            }
        })
    }

    fun setSubTitile(subtitlePath: String) {
        this.subtitlePath = subtitlePath
        android.util.Log.i("GSYExoSubTitlePlayer", "设置字幕路径: $subtitlePath")
    }

    fun setTextOutput(textOutput: Player.Listener) {
        this.textOutput = textOutput
        exoPlayer?.addListener(textOutput)
        textOutputs.add(textOutput)
    }

    fun addTextOutputPlaying(textOutput: Player.Listener) {
        exoPlayer?.addListener(textOutput)
        if (!textOutputs.contains(textOutput)) {
            textOutputs.add(textOutput)
        }
    }

    fun removeTextOutput(textOutput: Player.Listener) {
        exoPlayer?.removeListener(textOutput)
        textOutputs.remove(textOutput)
    }

    fun setPreview(preview: Boolean) {
        this.isPreview = preview
    }

    fun setCache(cache: Boolean) {
        this.cacheEnabled = cache
    }

    fun setCacheDir(cacheDir: File?) {
        this.cacheDir = cacheDir
    }

    fun setOverrideExtension(extension: String?) {
        this.overrideExtension = extension
    }

    fun setSeekParameter(@Nullable seekParameters: SeekParameters?) {
        exoPlayer?.setSeekParameters(seekParameters ?: SeekParameters.DEFAULT)
    }

    override fun setDataSource(context: Context, uri: Uri) {
        setDataSource(context, uri, null)
    }

    override fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?) {
        try {
            // 创建数据源工厂
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(headers ?: emptyMap())
            
            val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

            // 创建媒体源工厂
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            // 创建 MediaItem
            val mediaItemBuilder = MediaItem.Builder().setUri(uri)
            
            // 如果有字幕，添加字幕配置
            subtitlePath?.let { subtitlePath ->
                val subtitleUri = if (subtitlePath.startsWith("http")) {
                    Uri.parse(subtitlePath)
                } else {
                    Uri.fromFile(File(subtitlePath))
                }

                val mimeType = getMimeTypeFromPath(subtitlePath)
                android.util.Log.i("GSYExoSubTitlePlayer", "字幕文件: $subtitlePath, MIME类型: $mimeType")

                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                    .setMimeType(mimeType)
                    .setLanguage("zh")
                    .setLabel("中文字幕")
                    .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                    .setRoleFlags(androidx.media3.common.C.ROLE_FLAG_SUBTITLE)
                    .build()

                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
                android.util.Log.i("GSYExoSubTitlePlayer", "添加字幕配置: $subtitleUri")
            }

            val mediaItem = mediaItemBuilder.build()
            val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
            
            exoPlayer?.setMediaSource(mediaSource)
            android.util.Log.i("GSYExoSubTitlePlayer", "设置媒体源成功")
        } catch (e: Exception) {
            android.util.Log.e("GSYExoSubTitlePlayer", "设置数据源失败", e)
            notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0)
        }
    }



    private fun getMimeTypeFromPath(path: String): String {
        return when {
            path.endsWith(".srt", true) -> MimeTypes.APPLICATION_SUBRIP
            path.endsWith(".ass", true) || path.endsWith(".ssa", true) -> {
                // 使用正确的ASS MIME类型
                android.util.Log.i("GSYExoSubTitlePlayer", "使用原生ASS字幕支持")
                MimeTypes.TEXT_SSA  // 这是ExoPlayer支持的正确ASS MIME类型
            }
            path.endsWith(".vtt", true) -> MimeTypes.TEXT_VTT
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }

    override fun setDataSource(fd: java.io.FileDescriptor) {
        throw UnsupportedOperationException("FileDescriptor not supported")
    }

    override fun setDataSource(path: String) {
        setDataSource(context, Uri.parse(path), null)
    }

    override fun getDataSource(): String? {
        return subtitlePath
    }

    override fun setKeepInBackground(keepInBackground: Boolean) {
        // ExoPlayer 不需要实现
    }

    override fun setWakeMode(context: Context, mode: Int) {
        // ExoPlayer 不需要实现
    }

    override fun setDisplay(sh: android.view.SurfaceHolder?) {
        setSurface(sh?.surface)
    }

    override fun prepareAsync() {
        exoPlayer?.prepare()
    }

    override fun start() {
        exoPlayer?.play()
    }

    override fun stop() {
        exoPlayer?.stop()
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        // ExoPlayer 不需要实现
    }

    override fun getVideoWidth(): Int {
        return exoPlayer?.videoFormat?.width ?: 0
    }

    override fun getVideoHeight(): Int {
        return exoPlayer?.videoFormat?.height ?: 0
    }

    override fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    override fun seekTo(msec: Long) {
        exoPlayer?.seekTo(msec)
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    override fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    override fun release() {
        textOutputs.forEach { listener ->
            exoPlayer?.removeListener(listener)
        }
        textOutputs.clear()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun reset() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
    }

    override fun setAudioStreamType(streamtype: Int) {
        // ExoPlayer 自动处理
    }

    override fun setLooping(looping: Boolean) {
        exoPlayer?.repeatMode = if (looping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    override fun isLooping(): Boolean {
        return exoPlayer?.repeatMode == Player.REPEAT_MODE_ALL
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        exoPlayer?.volume = (leftVolume + rightVolume) / 2
    }

    override fun getAudioSessionId(): Int {
        return 0 // ExoPlayer 不提供此信息
    }

    override fun getMediaInfo(): MediaInfo? {
        return null // 简化实现
    }

    override fun setLogEnabled(enable: Boolean) {
        // 简化实现
    }

    override fun isPlayable(): Boolean {
        return true
    }

    override fun setSurface(surface: Surface?) {
        exoPlayer?.setVideoSurface(surface)
    }

    fun setSpeed(speed: Float, pitch: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun getBufferedPercentage(): Int {
        return exoPlayer?.bufferedPercentage ?: 0
    }

    override fun getVideoSarNum(): Int = 1
    override fun getVideoSarDen(): Int = 1

    override fun getTrackInfo(): Array<tv.danmaku.ijk.media.player.misc.ITrackInfo>? {
        return null // 简化实现
    }
}
