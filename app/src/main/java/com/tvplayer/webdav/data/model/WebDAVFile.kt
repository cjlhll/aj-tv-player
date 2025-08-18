package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * WebDAV文件/目录数据模型
 */
@Parcelize
data class WebDAVFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0L,
    val lastModified: Date? = null,
    val mimeType: String? = null,
    val displayName: String = name
) : Parcelable {
    
    /**
     * 获取文件扩展名
     */
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")
    
    /**
     * 是否为视频文件
     */
    val isVideoFile: Boolean
        get() = !isDirectory && videoExtensions.contains(extension.lowercase())
    
    /**
     * 是否为音频文件
     */
    val isAudioFile: Boolean
        get() = !isDirectory && audioExtensions.contains(extension.lowercase())
    
    /**
     * 是否为字幕文件
     */
    val isSubtitleFile: Boolean
        get() = !isDirectory && subtitleExtensions.contains(extension.lowercase())
    
    /**
     * 获取格式化的文件大小
     */
    fun getFormattedSize(): String {
        if (isDirectory) return ""
        
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)}MB"
            else -> "${size / (1024 * 1024 * 1024)}GB"
        }
    }
    
    companion object {
        private val videoExtensions = setOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", 
            "3gp", "ts", "m2ts", "mts", "vob", "rmvb", "rm", "asf"
        )
        
        private val audioExtensions = setOf(
            "mp3", "aac", "flac", "wav", "ogg", "m4a", "wma", "ac3", 
            "dts", "ape", "opus"
        )
        
        private val subtitleExtensions = setOf(
            "srt", "ass", "ssa", "vtt", "sub", "idx", "sup"
        )
    }
}
