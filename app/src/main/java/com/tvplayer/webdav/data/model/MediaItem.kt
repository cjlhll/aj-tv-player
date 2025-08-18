package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * 媒体项目数据模型
 * 支持电影、电视剧、单集等不同类型
 */
@Parcelize
data class MediaItem(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: Date? = null,
    val rating: Float = 0f,
    val duration: Long = 0L, // 时长（秒）
    val mediaType: MediaType,
    val filePath: String,
    val fileSize: Long = 0L,
    val lastModified: Date? = null,
    
    // 电视剧相关
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val seriesId: String? = null,
    val seriesTitle: String? = null,
    
    // 播放相关
    val watchedProgress: Float = 0f, // 观看进度 0-1
    val isWatched: Boolean = false,
    val lastWatchedTime: Date? = null,
    
    // 收藏和标签
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val genre: List<String> = emptyList()
) : Parcelable {
    
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        fun deriveNameFromFile(): String {
            val base = filePath.substringAfterLast('/')
                .substringBeforeLast('.')
                .replace(Regex("[\\[\\(（【].*?[\\]\\)）】]"), " ")
                .replace(Regex("[._-]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            return if (base.isNotBlank()) base else "未命名"
        }
        return when (mediaType) {
            MediaType.TV_EPISODE -> {
                val composite = if (seasonNumber != null && episodeNumber != null && !seriesTitle.isNullOrBlank()) {
                    "$seriesTitle S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
                } else title
                (composite?.takeIf { it.isNotBlank() } ?: seriesTitle ?: deriveNameFromFile())
            }
            else -> (title.takeIf { it.isNotBlank() } ?: originalTitle?.takeIf { it.isNotBlank() } ?: deriveNameFromFile())
        }
    }
    
    /**
     * 获取副标题
     */
    fun getSubtitle(): String? {
        return when (mediaType) {
            MediaType.TV_EPISODE -> title.takeIf { it != getDisplayTitle() }
            MediaType.MOVIE -> {
                val dateStr = releaseDate?.let {
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(it)
                }
                val durationStr = if (duration > 0) "${duration / 60}分钟" else null
                listOfNotNull(dateStr, durationStr).joinToString(" • ")
            }
            else -> null
        }
    }
    
    /**
     * 获取格式化的文件大小
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSize < 1024 -> "${fileSize}B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024}KB"
            fileSize < 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024)}MB"
            else -> String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    /**
     * 获取观看进度百分比
     */
    fun getWatchedPercentage(): Int {
        return (watchedProgress * 100).toInt()
    }
    
    /**
     * 是否为新内容（最近添加）
     */
    fun isNew(): Boolean {
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
        return lastModified?.time ?: 0L > weekAgo
    }
}

/**
 * 媒体类型枚举
 */
enum class MediaType {
    MOVIE,          // 电影
    TV_SERIES,      // 电视剧（系列）
    TV_EPISODE,     // 电视剧单集
    DOCUMENTARY,    // 纪录片
    ANIMATION,      // 动画
    OTHER           // 其他
}

/**
 * 媒体分类
 */
data class MediaCategory(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconRes: Int? = null,
    val mediaType: MediaType? = null,
    val itemCount: Int = 0
)
