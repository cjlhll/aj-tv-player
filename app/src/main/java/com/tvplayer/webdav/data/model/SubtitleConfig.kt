package com.tvplayer.webdav.data.model

import android.graphics.Color
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 字幕配置模型
 */
@Parcelize
data class SubtitleConfig(
    // 显示设置
    val isEnabled: Boolean = true, // 是否启用字幕
    val primaryLanguage: String = "zh", // 首选语言
    val fallbackLanguage: String = "en", // 备用语言
    val autoSelectLanguage: Boolean = true, // 自动选择语言
    
    // 样式设置
    val textSize: Float = 16.0f, // 字体大小 (sp)
    val textColor: Int = Color.WHITE, // 字体颜色
    val backgroundColor: Int = Color.TRANSPARENT, // 背景颜色
    val outlineColor: Int = Color.BLACK, // 描边颜色
    val outlineWidth: Float = 1.0f, // 描边宽度
    val shadowColor: Int = Color.BLACK, // 阴影颜色
    val shadowRadius: Float = 1.0f, // 阴影半径
    val shadowOffsetX: Float = 1.0f, // 阴影X偏移
    val shadowOffsetY: Float = 1.0f, // 阴影Y偏移
    
    // 位置设置
    val position: SubtitlePosition = SubtitlePosition.BOTTOM, // 字幕位置
    val marginHorizontal: Float = 32.0f, // 水平边距 (dp)
    val marginVertical: Float = 48.0f, // 垂直边距 (dp)
    val alignment: SubtitleAlignment = SubtitleAlignment.CENTER, // 对齐方式
    
    // 字体设置
    val fontFamily: String = "default", // 字体族
    val isBold: Boolean = false, // 是否粗体
    val isItalic: Boolean = false, // 是否斜体
    
    // 同步设置
    val globalOffsetMs: Long = 0L, // 全局时间偏移（毫秒）
    val autoSync: Boolean = true, // 自动同步
    val syncSensitivity: Float = 0.5f, // 同步敏感度 (0.0-1.0)
    
    // 下载设置
    val autoDownload: Boolean = true, // 自动下载字幕
    val downloadQuality: SubtitleQuality = SubtitleQuality.BEST, // 下载质量偏好
    val maxCacheSize: Long = 100 * 1024 * 1024L, // 最大缓存大小 (100MB)
    val cacheExpireDays: Int = 30, // 缓存过期天数
    
    // 搜索设置
    val enabledSources: Set<SubtitleSource> = setOf(
        SubtitleSource.OPENSUBTITLES,
        SubtitleSource.SUBSCENE
    ), // 启用的字幕源
    val searchTimeout: Int = 10, // 搜索超时时间（秒）
    val maxResults: Int = 20, // 最大搜索结果数
    
    // 高级设置
    val encoding: String = "UTF-8", // 默认编码
    val lineSpacing: Float = 1.2f, // 行间距
    val maxLines: Int = 3, // 最大行数
    val wordWrap: Boolean = true, // 自动换行
    val fadeInDuration: Long = 200L, // 淡入时长（毫秒）
    val fadeOutDuration: Long = 200L // 淡出时长（毫秒）
) : Parcelable {
    
    companion object {
        /**
         * 获取默认配置
         */
        fun getDefault(): SubtitleConfig = SubtitleConfig()
        
        /**
         * 获取简体中文优化配置
         */
        fun getChineseOptimized(): SubtitleConfig = SubtitleConfig(
            primaryLanguage = "zh-cn",
            fallbackLanguage = "zh",
            textSize = 18.0f,
            outlineWidth = 1.5f,
            marginVertical = 60.0f
        )
        
        /**
         * 获取英文优化配置
         */
        fun getEnglishOptimized(): SubtitleConfig = SubtitleConfig(
            primaryLanguage = "en",
            fallbackLanguage = "en-us",
            textSize = 16.0f,
            fontFamily = "sans-serif"
        )
    }
    
    /**
     * 检查配置是否有效
     */
    fun isValid(): Boolean {
        return textSize > 0 && 
               marginHorizontal >= 0 && 
               marginVertical >= 0 &&
               outlineWidth >= 0 &&
               maxCacheSize > 0 &&
               searchTimeout > 0 &&
               maxResults > 0
    }
    
    /**
     * 应用全局时间偏移
     */
    fun applyGlobalOffset(timeMs: Long): Long {
        return timeMs + globalOffsetMs
    }
}

/**
 * 字幕位置枚举
 */
@Parcelize
enum class SubtitlePosition : Parcelable {
    TOP, // 顶部
    CENTER, // 中央
    BOTTOM // 底部
}

/**
 * 字幕对齐方式枚举
 */
@Parcelize
enum class SubtitleAlignment : Parcelable {
    LEFT, // 左对齐
    CENTER, // 居中
    RIGHT // 右对齐
}

/**
 * 字幕质量偏好枚举
 */
@Parcelize
enum class SubtitleQuality : Parcelable {
    BEST, // 最佳质量（优先评分高的）
    MOST_DOWNLOADED, // 最多下载（优先下载量大的）
    LATEST, // 最新上传
    ANY // 任意质量
}