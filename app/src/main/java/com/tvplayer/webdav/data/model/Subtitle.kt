package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * 字幕文件数据模型
 */
@Parcelize
data class Subtitle(
    val id: String = "", // 字幕唯一标识
    val title: String = "", // 字幕标题
    val language: String = "", // 语言代码 (zh, en, zh-cn, en-us)
    val languageName: String = "", // 语言显示名称
    val format: SubtitleFormat = SubtitleFormat.SRT, // 字幕格式
    val encoding: String = "UTF-8", // 字幕编码
    val downloadUrl: String = "", // 下载链接
    val localPath: String = "", // 本地文件路径
    val fileSize: Long = 0L, // 文件大小（字节）
    val hash: String = "", // 文件哈希值
    val source: SubtitleSource = SubtitleSource.UNKNOWN, // 字幕来源
    val rating: Float = 0.0f, // 评分 (0.0-10.0)
    val downloadCount: Int = 0, // 下载次数
    val uploadDate: Long = 0L, // 上传时间戳
    val uploader: String = "", // 上传者
    val isDownloaded: Boolean = false, // 是否已下载
    val syncInfo: SubtitleSyncInfo? = null, // 同步信息
    val metadata: Map<String, String> = emptyMap() // 额外元数据
) : Parcelable {
    
    /**
     * 获取本地字幕文件
     */
    fun getLocalFile(): File? {
        return if (isDownloaded && localPath.isNotEmpty()) {
            File(localPath).takeIf { it.exists() }
        } else null
    }
    
    /**
     * 检查字幕是否可用
     */
    fun isAvailable(): Boolean {
        return isDownloaded && getLocalFile() != null
    }
    
    /**
     * 获取字幕描述信息
     */
    fun getDescription(): String {
        val parts = mutableListOf<String>()
        if (languageName.isNotEmpty()) parts.add(languageName)
        if (rating > 0) parts.add("评分: %.1f".format(rating))
        if (downloadCount > 0) parts.add("下载: $downloadCount")
        return parts.joinToString(" | ")
    }
    
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return if (title.isNotEmpty()) title else "字幕 - $languageName"
    }
}

/**
 * 字幕格式枚举
 */
@Parcelize
enum class SubtitleFormat(val extension: String, val mimeType: String) : Parcelable {
    SRT("srt", "application/x-subrip"),
    ASS("ass", "text/x-ssa"),
    SSA("ssa", "text/x-ssa"),
    VTT("vtt", "text/vtt"),
    SUB("sub", "text/x-subviewer"),
    IDX("idx", "application/x-subtitle"),
    SMI("smi", "application/x-sami"),
    TXT("txt", "text/plain");
    
    companion object {
        fun fromExtension(extension: String): SubtitleFormat {
            return values().find { 
                it.extension.equals(extension, ignoreCase = true) 
            } ?: SRT
        }
        
        fun fromFileName(fileName: String): SubtitleFormat {
            val ext = fileName.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}

/**
 * 字幕来源枚举
 */
@Parcelize
enum class SubtitleSource(val displayName: String, val baseUrl: String) : Parcelable {
    OPENSUBTITLES("OpenSubtitles", "https://api.opensubtitles.com"),
    SUBSCENE("Subscene", "https://subscene.com"),
    YIFYSUBTITLES("YifySubtitles", "https://yifysubtitles.org"),
    SUBDL("SubDL", "https://subdl.com"),
    LOCAL("本地字幕", ""),
    MANUAL("手动导入", ""),
    UNKNOWN("未知来源", "");
    
    val isOnline: Boolean
        get() = baseUrl.isNotEmpty()
}

/**
 * 字幕同步信息
 */
@Parcelize
data class SubtitleSyncInfo(
    val offsetMs: Long = 0L, // 时间偏移（毫秒）
    val speedRatio: Float = 1.0f, // 播放速度比例
    val fps: Float = 0.0f, // 帧率
    val isAutoSynced: Boolean = false, // 是否自动同步
    val confidence: Float = 0.0f // 同步置信度 (0.0-1.0)
) : Parcelable {
    
    /**
     * 应用时间偏移
     */
    fun applyOffset(originalTimeMs: Long): Long {
        return ((originalTimeMs * speedRatio) + offsetMs).toLong()
    }
}