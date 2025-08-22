package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 字幕搜索请求模型
 */
@Parcelize
data class SubtitleSearchRequest(
    // 基本信息
    val mediaType: MediaType = MediaType.MOVIE, // 媒体类型
    val title: String = "", // 标题
    val originalTitle: String = "", // 原始标题
    val year: Int = 0, // 年份
    val imdbId: String = "", // IMDB ID
    val tmdbId: Int = 0, // TMDB ID
    
    // 剧集信息（仅TV类型）
    val seasonNumber: Int = 0, // 季数
    val episodeNumber: Int = 0, // 集数
    
    // 文件信息
    val fileName: String = "", // 文件名
    val fileSize: Long = 0L, // 文件大小
    val fileHash: String = "", // 文件哈希值
    val duration: Long = 0L, // 视频时长（毫秒）
    val videoResolution: String = "", // 视频分辨率 (1080p, 720p等)
    val videoCodec: String = "", // 视频编码
    val audioCodec: String = "", // 音频编码
    val releaseGroup: String = "", // 发布组
    
    // 搜索参数
    val languages: List<String> = listOf("zh", "en"), // 搜索语言列表
    val sources: Set<SubtitleSource> = setOf(SubtitleSource.OPENSUBTITLES), // 搜索源
    val maxResults: Int = 20, // 最大结果数
    val timeoutSeconds: Int = 10, // 超时时间
    val minRating: Float = 0.0f, // 最小评分过滤
    val onlyHD: Boolean = false, // 仅搜索高清字幕
    
    // 高级选项
    val fuzzyMatch: Boolean = true, // 模糊匹配
    val includeHearingImpaired: Boolean = false, // 包含听力障碍字幕
    val excludeMachineTranslated: Boolean = true, // 排除机器翻译
    val preferredFormats: List<SubtitleFormat> = listOf(SubtitleFormat.SRT, SubtitleFormat.ASS) // 偏好格式
) : Parcelable {
    
    /**
     * 从MediaItem创建搜索请求
     */
    companion object {
        fun fromMediaItem(mediaItem: MediaItem, config: SubtitleConfig): SubtitleSearchRequest {
            return SubtitleSearchRequest(
                mediaType = mediaItem.mediaType,
                title = mediaItem.title,
                originalTitle = mediaItem.originalTitle ?: "",
                year = extractYearFromDate(mediaItem.releaseDate),
                imdbId = "", // MediaItem中暂无此属性
                tmdbId = 0, // MediaItem中暂无此属性
                seasonNumber = mediaItem.seasonNumber ?: 0,
                episodeNumber = mediaItem.episodeNumber ?: 0,
                fileName = extractFileNameFromPath(mediaItem.filePath),
                fileSize = mediaItem.fileSize,
                duration = mediaItem.duration * 1000, // 转换为毫秒
                languages = listOf(config.primaryLanguage, config.fallbackLanguage).distinct(),
                sources = config.enabledSources,
                maxResults = config.maxResults,
                timeoutSeconds = config.searchTimeout,
                preferredFormats = listOf(SubtitleFormat.SRT, SubtitleFormat.ASS, SubtitleFormat.VTT)
            )
        }
        
        /**
         * 从文件名创建简单搜索请求
         */
        fun fromFileName(fileName: String, languages: List<String> = listOf("zh", "en")): SubtitleSearchRequest {
            val parsed = parseFileName(fileName)
            return SubtitleSearchRequest(
                title = parsed.title,
                year = parsed.year,
                seasonNumber = parsed.season,
                episodeNumber = parsed.episode,
                mediaType = if (parsed.season > 0) MediaType.TV_SERIES else MediaType.MOVIE,
                fileName = fileName,
                languages = languages,
                releaseGroup = parsed.releaseGroup,
                videoResolution = parsed.resolution
            )
        }
        
        /**
         * 从日期提取年份
         */
        private fun extractYearFromDate(date: java.util.Date?): Int {
            return date?.let {
                val calendar = java.util.Calendar.getInstance()
                calendar.time = it
                calendar.get(java.util.Calendar.YEAR)
            } ?: 0
        }
        
        /**
         * 从文件路径提取文件名
         */
        private fun extractFileNameFromPath(filePath: String): String {
            return filePath.substringAfterLast('/')
        }
        
        /**
         * 解析文件名获取信息
         */
        private fun parseFileName(fileName: String): FileNameInfo {
            val info = FileNameInfo()
            val cleanName = fileName.substringBeforeLast('.')
            
            // 解析年份
            val yearRegex = """(\d{4})""".toRegex()
            val yearMatch = yearRegex.find(cleanName)
            if (yearMatch != null) {
                info.year = yearMatch.value.toIntOrNull() ?: 0
            }
            
            // 解析季集信息
            val seasonEpisodeRegex = """[Ss](\d+)[Ee](\d+)""".toRegex()
            val seMatch = seasonEpisodeRegex.find(cleanName)
            if (seMatch != null) {
                info.season = seMatch.groupValues[1].toIntOrNull() ?: 0
                info.episode = seMatch.groupValues[2].toIntOrNull() ?: 0
            }
            
            // 解析分辨率
            val resolutionRegex = """(720p|1080p|1440p|2160p|4K)""".toRegex(RegexOption.IGNORE_CASE)
            val resMatch = resolutionRegex.find(cleanName)
            if (resMatch != null) {
                info.resolution = resMatch.value.uppercase()
            }
            
            // 解析发布组
            val groupRegex = """-(\w+)$""".toRegex()
            val groupMatch = groupRegex.find(cleanName)
            if (groupMatch != null) {
                info.releaseGroup = groupMatch.groupValues[1]
            }
            
            // 提取标题（移除年份、季集、分辨率等信息）
            var title = cleanName
                .replace(yearRegex, "")
                .replace(seasonEpisodeRegex, "")
                .replace(resolutionRegex, "")
                .replace(groupRegex, "")
                .replace("""[.\-_\s]+""".toRegex(), " ")
                .trim()
            
            info.title = title.ifEmpty { cleanName }
            
            return info
        }
        
        private data class FileNameInfo(
            var title: String = "",
            var year: Int = 0,
            var season: Int = 0,
            var episode: Int = 0,
            var resolution: String = "",
            var releaseGroup: String = ""
        )
    }
    
    /**
     * 检查请求是否有效
     */
    fun isValid(): Boolean {
        return title.isNotEmpty() && languages.isNotEmpty() && sources.isNotEmpty()
    }
    
    /**
     * 获取搜索关键词
     */
    fun getSearchKeywords(): List<String> {
        val keywords = mutableListOf<String>()
        if (title.isNotEmpty()) keywords.add(title)
        if (originalTitle.isNotEmpty() && originalTitle != title) keywords.add(originalTitle)
        if (year > 0) keywords.add(year.toString())
        if (releaseGroup.isNotEmpty()) keywords.add(releaseGroup)
        return keywords
    }
    
    /**
     * 获取文件标识符（用于缓存和匹配）
     */
    fun getFileIdentifier(): String {
        return when {
            fileHash.isNotEmpty() -> fileHash
            imdbId.isNotEmpty() -> imdbId
            tmdbId > 0 -> "tmdb_$tmdbId"
            else -> "${title}_${year}_${seasonNumber}_${episodeNumber}".hashCode().toString()
        }
    }
    
    /**
     * 是否为剧集搜索
     */
    fun isTVShow(): Boolean {
        return mediaType == MediaType.TV_SERIES || mediaType == MediaType.TV_EPISODE || seasonNumber > 0 || episodeNumber > 0
    }
    
    /**
     * 获取完整的剧集标识
     */
    fun getEpisodeIdentifier(): String {
        return if (isTVShow()) {
            "${title}_S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
        } else {
            "${title}_${year}"
        }
    }
}