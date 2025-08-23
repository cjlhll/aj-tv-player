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
                val processedSubtitleUri = if (subtitlePath.startsWith("http")) {
                    Uri.parse(subtitlePath)
                } else {
                    // 对ASS文件进行特殊处理
                    if (subtitlePath.endsWith(".ass", true) || subtitlePath.endsWith(".ssa", true)) {
                        processAssSubtitle(subtitlePath)
                    } else {
                        Uri.fromFile(File(subtitlePath))
                    }
                }

                val mimeType = getMimeTypeFromPath(subtitlePath)
                android.util.Log.i("GSYExoSubTitlePlayer", "字幕文件: $subtitlePath, MIME类型: $mimeType")

                val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(processedSubtitleUri)
                    .setMimeType(mimeType)
                    .setLanguage("zh")
                    .setLabel("中文字幕")
                    .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
                    .setRoleFlags(androidx.media3.common.C.ROLE_FLAG_SUBTITLE)
                    .build()

                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
                android.util.Log.i("GSYExoSubTitlePlayer", "添加字幕配置: $processedSubtitleUri")
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

    /**
     * 处理ASS字幕文件，转换为SRT格式
     */
    private fun processAssSubtitle(assPath: String): Uri {
        try {
            val assFile = File(assPath)
            if (!assFile.exists()) {
                android.util.Log.e("GSYExoSubTitlePlayer", "ASS文件不存在: $assPath")
                return Uri.fromFile(assFile)
            }

            // 创建转换后的SRT文件
            val srtFile = File(assFile.parent, assFile.nameWithoutExtension + "_converted.srt")

            android.util.Log.i("GSYExoSubTitlePlayer", "开始转换ASS字幕: $assPath -> ${srtFile.absolutePath}")

            val assContent = assFile.readText(Charsets.UTF_8)
            val srtContent = convertAssToSrt(assContent)

            srtFile.writeText(srtContent, Charsets.UTF_8)
            android.util.Log.i("GSYExoSubTitlePlayer", "ASS字幕转换完成，SRT文件大小: ${srtFile.length()} bytes")

            return Uri.fromFile(srtFile)

        } catch (e: Exception) {
            android.util.Log.e("GSYExoSubTitlePlayer", "ASS字幕处理失败", e)
            return Uri.fromFile(File(assPath))
        }
    }

    /**
     * 将ASS格式转换为SRT格式
     */
    private fun convertAssToSrt(assContent: String): String {
        val srtBuilder = StringBuilder()
        val lines = assContent.split("\n")
        var subtitleIndex = 1

        for (line in lines) {
            if (line.startsWith("Dialogue:")) {
                try {
                    val parts = line.split(",", limit = 10)
                    if (parts.size >= 10) {
                        val startTime = convertAssTimeToSrt(parts[1].trim())
                        val endTime = convertAssTimeToSrt(parts[2].trim())
                        var text = parts[9]

                        // 清理ASS格式化标签
                        text = cleanAssFormatting(text)

                        if (text.isNotBlank()) {
                            srtBuilder.append("$subtitleIndex\n")
                            srtBuilder.append("$startTime --> $endTime\n")
                            srtBuilder.append("$text\n\n")
                            subtitleIndex++
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("GSYExoSubTitlePlayer", "转换ASS行失败: $line", e)
                }
            }
        }

        val result = srtBuilder.toString()
        android.util.Log.i("GSYExoSubTitlePlayer", "ASS转SRT完成，生成 ${subtitleIndex-1} 条字幕")
        return result
    }

    /**
     * 转换ASS时间格式为SRT时间格式
     */
    private fun convertAssTimeToSrt(assTime: String): String {
        try {
            // ASS格式: 0:00:20.00
            // SRT格式: 00:00:20,000
            val parts = assTime.split(":")
            val hours = parts[0].padStart(2, '0')
            val minutes = parts[1].padStart(2, '0')
            val secondsParts = parts[2].split(".")
            val seconds = secondsParts[0].padStart(2, '0')
            val centiseconds = if (secondsParts.size > 1) {
                secondsParts[1].padEnd(2, '0').take(2)
            } else "00"
            val milliseconds = (centiseconds.toInt() * 10).toString().padStart(3, '0')

            return "$hours:$minutes:$seconds,$milliseconds"
        } catch (e: Exception) {
            android.util.Log.w("GSYExoSubTitlePlayer", "ASS时间转换失败: $assTime", e)
            return "00:00:00,000"
        }
    }

    /**
     * 清理ASS格式化标签
     */
    private fun cleanAssFormatting(text: String): String {
        var cleanText = text

        // 移除常见的ASS格式化标签
        cleanText = cleanText
            .replace("\\N", "\n")                    // 换行符
            .replace("\\n", "\n")                    // 小写换行符
            .replace("\\h", " ")                     // 硬空格
            .replace(Regex("\\{[^}]*\\}"), "")       // 移除所有 {} 包围的标签
            .replace(Regex("\\\\[a-zA-Z]+\\([^)]*\\)"), "") // 移除函数式标签如 \move()
            .replace(Regex("\\\\[a-zA-Z]+[0-9]*"), "") // 移除简单标签如 \b1, \i1
            .replace(Regex("\\\\[0-9]+"), "")        // 移除数字标签
            .replace("\\\\", "\\")                   // 处理转义的反斜杠
            .trim()

        return cleanText
    }

    private fun getMimeTypeFromPath(path: String): String {
        return when {
            path.endsWith(".srt", true) -> MimeTypes.APPLICATION_SUBRIP
            path.endsWith(".ass", true) || path.endsWith(".ssa", true) -> {
                // ASS格式转换为SRT后使用SRT的MIME类型
                android.util.Log.i("GSYExoSubTitlePlayer", "ASS字幕将转换为SRT格式")
                MimeTypes.APPLICATION_SUBRIP
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
