package com.tvplayer.webdav.ui.player.subtitle

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.datasource.DataSink
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.TransferListener
import tv.danmaku.ijk.media.exo2.ExoSourceManager
import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener
import com.tvplayer.webdav.AppGlobals
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 通过 GSY 的 ExoSourceManager 拦截，向 MediaSource 注入外挂字幕。
 * 注意：仅在 Exo(Media3) 内核下有效。
 */
object SubtitleExoMount {
    private const val TAG = "SubtitleExoMount"

    data class SubInfo(val uri: Uri, val mimeType: String)

    // dataSource(url) -> SubInfo
    private val map: MutableMap<String, SubInfo> = ConcurrentHashMap()
    // dataSource(url) -> headers
    private val headersMap: MutableMap<String, Map<String, String>> = ConcurrentHashMap()
    private var inited = false

    /** 注册拦截器（只需一次） */
    fun ensureInit() {
        if (inited) return
        synchronized(this) {
            if (inited) return
            ExoSourceManager.setExoMediaSourceInterceptListener(object : ExoMediaSourceInterceptListener {
                override fun getMediaSource(
                    dataSource: String,
                    preview: Boolean,
                    cacheEnable: Boolean,
                    isLooping: Boolean,
                    cacheDir: java.io.File?
                ): MediaSource? {
                    val sub = map[dataSource]
                    if (sub == null) return null // 使用默认流程
                    return try {
                        // 使用带有字幕配置的 MediaItem
                        val videoItem = MediaItem.Builder()
                            .setUri(dataSource)
                            .setSubtitleConfigurations(
                                listOf(
                                    MediaItem.SubtitleConfiguration.Builder(sub.uri)
                                        .setMimeType(sub.mimeType)
                                        .setLanguage("zh")
                                        .setSelectionFlags(0)
                                        .build()
                                )
                            )
                            .build()
                        // 使用带有 Header 的 DataSourceFactory，确保 WebDAV 认证不丢失
                        val headers = headersMap[dataSource] ?: emptyMap()
                        val dataSourceFactory: DataSource.Factory =
                            ExoSourceManager.getDataSourceFactory(
                                AppGlobals.app,
                                preview,
                                "AndroidTVPlayer/1.0",
                                headers
                            )
                        DefaultMediaSourceFactory(dataSourceFactory)
                            .createMediaSource(videoItem)
                    } catch (e: Throwable) {
                        Log.e(TAG, "build merged mediasource error", e)
                        null
                    }
                }

                override fun getHttpDataSourceFactory(
                    userAgent: String,
                    listener: TransferListener?,
                    connectTimeoutMillis: Int,
                    readTimeoutMillis: Int,
                    mapHeadData: MutableMap<String, String>?,
                    allowCrossProtocolRedirects: Boolean
                ): DataSource.Factory? {
                    return null
                }

                override fun cacheWriteDataSinkFactory(
                    CachePath: String?,
                    url: String?
                ): DataSink.Factory? {
                    return null
                }
            })
            inited = true
            Log.i(TAG, "ExoMediaSource intercept listener inited for subtitles")
        }
    }

    fun register(dataSource: String, subFile: File, mimeType: String) {
        ensureInit()
        map[dataSource] = SubInfo(Uri.fromFile(subFile), normalizeMime(mimeType, subFile.name))
        Log.i(TAG, "subtitle registered for $dataSource -> ${subFile.name}")
    }

    fun registerHeaders(dataSource: String, headers: Map<String, String>) {
        headersMap[dataSource] = headers
        Log.i(TAG, "headers registered for $dataSource : ${headers.keys}")
    }

    private fun normalizeMime(mime: String, name: String): String {
        val m = mime.lowercase()
        val n = name.lowercase()
        return when {
            m.contains("subrip") || n.endsWith(".srt") -> MimeTypes.APPLICATION_SUBRIP
            m.contains("vtt") || n.endsWith(".vtt") -> MimeTypes.TEXT_VTT
            m.contains("ssa") || n.endsWith(".ass") || n.endsWith(".ssa") -> "text/x-ssa"
            else -> MimeTypes.APPLICATION_SUBRIP
        }
    }
}

