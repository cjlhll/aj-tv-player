package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * 播放状态数据模型
 * 用于跟踪TV系列的当前播放进度和状态
 */
@Parcelize
data class PlaybackState(
    val seriesId: String,
    val currentSeasonNumber: Int,
    val currentEpisodeNumber: Int,
    val playbackProgress: Long, // 播放进度（秒）
    val totalDuration: Long = 0L, // 总时长（秒）
    val lastPlayedTimestamp: Date = Date(),
    val isCompleted: Boolean = false // 是否已完成播放
) : Parcelable {
    
    /**
     * 获取播放进度百分比
     */
    fun getProgressPercentage(): Float {
        return if (totalDuration > 0) {
            (playbackProgress.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * 检查是否接近完成（90%以上算完成）
     */
    fun isNearlyCompleted(): Boolean {
        return getProgressPercentage() >= 0.9f
    }
    
    /**
     * 获取格式化的播放进度时间
     */
    fun getFormattedProgress(): String {
        val hours = playbackProgress / 3600
        val minutes = (playbackProgress % 3600) / 60
        val seconds = playbackProgress % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * 获取格式化的总时长
     */
    fun getFormattedDuration(): String {
        val hours = totalDuration / 3600
        val minutes = (totalDuration % 3600) / 60
        val seconds = totalDuration % 60
        
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
    
    /**
     * 获取剧集标识符
     */
    fun getEpisodeIdentifier(): String {
        return "S${currentSeasonNumber.toString().padStart(2, '0')}E${currentEpisodeNumber.toString().padStart(2, '0')}"
    }
    
    /**
     * 创建更新的播放状态
     */
    fun updateProgress(newProgress: Long, newDuration: Long = totalDuration): PlaybackState {
        return copy(
            playbackProgress = newProgress,
            totalDuration = if (newDuration > 0) newDuration else totalDuration,
            lastPlayedTimestamp = Date(),
            isCompleted = newProgress > 0 && newDuration > 0 && (newProgress.toFloat() / newDuration.toFloat()) >= 0.9f
        )
    }
    
    /**
     * 创建切换到下一集的播放状态
     */
    fun nextEpisode(nextSeasonNumber: Int = currentSeasonNumber, nextEpisodeNumber: Int = currentEpisodeNumber + 1): PlaybackState {
        return copy(
            currentSeasonNumber = nextSeasonNumber,
            currentEpisodeNumber = nextEpisodeNumber,
            playbackProgress = 0L,
            totalDuration = 0L,
            lastPlayedTimestamp = Date(),
            isCompleted = false
        )
    }
}
