package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * 剧集数据模型
 * 用于在详情页面显示剧集列表
 */
@Parcelize
data class Episode(
    val id: String,
    val episodeNumber: Int,
    val name: String,
    val overview: String? = null,
    val stillPath: String? = null, // 剧集静态图片
    val airDate: Date? = null,
    val rating: Float = 0f,
    val runtime: Int? = null, // 时长（分钟）
    val seasonNumber: Int,
    val tvId: Int,
    
    // 播放相关
    val watchedProgress: Float = 0f, // 观看进度 0-1
    val isWatched: Boolean = false,
    val lastWatchedTime: Date? = null,
    
    // 关联的媒体文件（必须存在才显示该剧集）
    val mediaItem: MediaItem
) : Parcelable {
    
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return "第${episodeNumber}集"
    }
    
    /**
     * 获取副标题（剧集名称）
     */
    fun getSubtitle(): String? {
        return name.takeIf { it.isNotBlank() && it != getDisplayTitle() }
    }
    
    /**
     * 获取格式化的时长
     */
    fun getFormattedRuntime(): String? {
        return runtime?.let { "${it}分钟" }
    }
    
    /**
     * 获取观看进度百分比
     */
    fun getWatchedPercentage(): Int {
        return (watchedProgress * 100).toInt()
    }
    
    /**
     * 是否有观看进度
     */
    fun hasWatchedProgress(): Boolean {
        return watchedProgress > 0f
    }
}
