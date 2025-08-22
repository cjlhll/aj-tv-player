package com.tvplayer.webdav.data.subtitle

import android.util.Log
import com.tvplayer.webdav.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * 智能字幕匹配器
 * 基于多种算法自动匹配最适合的字幕
 */
@Singleton
class SubtitleMatcher @Inject constructor() {
    
    companion object {
        private const val TAG = "SubtitleMatcher"
        
        // 匹配权重
        private const val WEIGHT_EXACT_MATCH = 100.0f
        private const val WEIGHT_LANGUAGE = 30.0f
        private const val WEIGHT_QUALITY = 20.0f
        private const val WEIGHT_FILE_NAME = 25.0f
        private const val WEIGHT_RELEASE_GROUP = 15.0f
        private const val WEIGHT_RESOLUTION = 10.0f
        private const val WEIGHT_YEAR = 10.0f
        private const val WEIGHT_EPISODE = 15.0f
        
        // 相似度阈值
        private const val MIN_SIMILARITY_THRESHOLD = 0.3f
        private const val GOOD_MATCH_THRESHOLD = 0.7f
        private const val EXCELLENT_MATCH_THRESHOLD = 0.9f
    }
    
    /**
     * 为媒体项匹配最佳字幕
     * @param mediaItem 媒体项
     * @param subtitles 候选字幕列表
     * @param config 字幕配置
     * @return 匹配结果列表，按匹配度排序
     */
    suspend fun matchSubtitles(
        mediaItem: MediaItem,
        subtitles: List<Subtitle>,
        config: SubtitleConfig
    ): List<SubtitleMatch> {
        return withContext(Dispatchers.Default) {
            try {
                Log.d(TAG, "Matching subtitles for: ${mediaItem.title}")
                
                if (subtitles.isEmpty()) {
                    return@withContext emptyList()
                }
                
                val matches = subtitles.map { subtitle ->
                    val similarity = calculateSimilarity(mediaItem, subtitle, config)
                    SubtitleMatch(
                        subtitle = subtitle,
                        similarity = similarity,
                        confidence = calculateConfidence(similarity, mediaItem, subtitle),
                        matchReasons = generateMatchReasons(mediaItem, subtitle, similarity)
                    )
                }.filter { it.similarity >= MIN_SIMILARITY_THRESHOLD }
                .sortedByDescending { it.similarity }
                
                Log.d(TAG, "Found ${matches.size} matching subtitles (similarity >= $MIN_SIMILARITY_THRESHOLD)")
                matches
                
            } catch (e: Exception) {
                Log.e(TAG, "Error matching subtitles", e)
                emptyList()
            }
        }
    }
    
    /**
     * 快速匹配最佳字幕
     * @param mediaItem 媒体项
     * @param subtitles 候选字幕列表
     * @param config 字幕配置
     * @return 最佳匹配的字幕，如果没有合适的返回null
     */
    suspend fun findBestMatch(
        mediaItem: MediaItem,
        subtitles: List<Subtitle>,
        config: SubtitleConfig
    ): SubtitleMatch? {
        val matches = matchSubtitles(mediaItem, subtitles, config)
        return matches.firstOrNull { it.similarity >= GOOD_MATCH_THRESHOLD }
            ?: matches.firstOrNull()
    }
    
    /**
     * 基于文件名匹配字幕
     * @param fileName 媒体文件名
     * @param subtitles 候选字幕列表
     * @param languages 偏好语言列表
     * @return 匹配结果列表
     */
    suspend fun matchByFileName(
        fileName: String,
        subtitles: List<Subtitle>,
        languages: List<String> = listOf("zh", "en")
    ): List<SubtitleMatch> {
        return withContext(Dispatchers.Default) {
            try {
                val fileInfo = parseFileName(fileName)
                
                val matches = subtitles.map { subtitle ->
                    val similarity = calculateFileNameSimilarity(fileInfo, subtitle, languages)
                    SubtitleMatch(
                        subtitle = subtitle,
                        similarity = similarity,
                        confidence = min(similarity, 0.8f), // 基于文件名的匹配置信度稍低
                        matchReasons = generateFileNameMatchReasons(fileInfo, subtitle)
                    )
                }.filter { it.similarity >= MIN_SIMILARITY_THRESHOLD }
                .sortedByDescending { it.similarity }
                
                Log.d(TAG, "File name matching found ${matches.size} results for: $fileName")
                matches
                
            } catch (e: Exception) {
                Log.e(TAG, "Error matching by file name", e)
                emptyList()
            }
        }
    }
    
    /**
     * 验证字幕与媒体的兼容性
     * @param mediaItem 媒体项
     * @param subtitle 字幕
     * @return 兼容性检查结果
     */
    fun validateCompatibility(mediaItem: MediaItem, subtitle: Subtitle): CompatibilityResult {
        val issues = mutableListOf<CompatibilityIssue>()
        
        try {
            // 检查剧集信息匹配
            if (mediaItem.mediaType == MediaType.TV_EPISODE || mediaItem.mediaType == MediaType.TV_SERIES) {
                if (subtitle.metadata["season_number"]?.toIntOrNull() != mediaItem.seasonNumber) {
                    issues.add(CompatibilityIssue.SEASON_MISMATCH)
                }
                if (subtitle.metadata["episode_number"]?.toIntOrNull() != mediaItem.episodeNumber) {
                    issues.add(CompatibilityIssue.EPISODE_MISMATCH)
                }
            }
            
            // 检查年份匹配
            val subtitleYear = subtitle.metadata["year"]?.toIntOrNull()
            val mediaYear = getYearFromMediaItem(mediaItem)
            if (subtitleYear != null && mediaYear > 0 && abs(subtitleYear - mediaYear) > 2) {
                issues.add(CompatibilityIssue.YEAR_MISMATCH)
            }
            
            // 检查时长匹配（如果可用）
            val subtitleDuration = subtitle.metadata["duration"]?.toLongOrNull()
            if (subtitleDuration != null && mediaItem.duration > 0) {
                val durationDiff = abs(subtitleDuration - mediaItem.duration) / 1000.0 // 转换为秒
                if (durationDiff > 300) { // 超过5分钟差异
                    issues.add(CompatibilityIssue.DURATION_MISMATCH)
                }
            }
            
            // 检查文件大小合理性
            if (subtitle.fileSize > 0 && mediaItem.fileSize > 0) {
                val sizeRatio = subtitle.fileSize.toDouble() / mediaItem.fileSize
                if (sizeRatio > 0.1) { // 字幕文件不应该超过视频文件的10%
                    issues.add(CompatibilityIssue.SIZE_SUSPICIOUS)
                }
            }
            
            val severity = when {
                issues.any { it.isCritical } -> CompatibilitySeverity.CRITICAL
                issues.isNotEmpty() -> CompatibilitySeverity.WARNING
                else -> CompatibilitySeverity.NONE
            }
            
            return CompatibilityResult(
                isCompatible = severity != CompatibilitySeverity.CRITICAL,
                severity = severity,
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating compatibility", e)
            return CompatibilityResult(
                isCompatible = false,
                severity = CompatibilitySeverity.CRITICAL,
                issues = listOf(CompatibilityIssue.VALIDATION_ERROR)
            )
        }
    }
    
    // 私有辅助方法
    
    /**
     * 计算媒体项和字幕的相似度
     */
    private fun calculateSimilarity(
        mediaItem: MediaItem,
        subtitle: Subtitle,
        config: SubtitleConfig
    ): Float {
        var totalScore = 0.0f
        var totalWeight = 0.0f
        
        // 语言匹配
        val languageScore = calculateLanguageScore(subtitle.language, config)
        totalScore += languageScore * WEIGHT_LANGUAGE
        totalWeight += WEIGHT_LANGUAGE
        
        // 标题匹配
        val titleScore = calculateTextSimilarity(mediaItem.title, subtitle.title)
        totalScore += titleScore * WEIGHT_FILE_NAME
        totalWeight += WEIGHT_FILE_NAME
        
        // 年份匹配
        val yearScore = calculateYearScore(getYearFromMediaItem(mediaItem), subtitle.metadata["year"]?.toIntOrNull() ?: 0)
        totalScore += yearScore * WEIGHT_YEAR
        totalWeight += WEIGHT_YEAR
        
        // 剧集匹配（如果适用）
        if (mediaItem.mediaType == MediaType.TV_EPISODE || mediaItem.mediaType == MediaType.TV_SERIES) {
            val episodeScore = calculateEpisodeScore(mediaItem, subtitle)
            totalScore += episodeScore * WEIGHT_EPISODE
            totalWeight += WEIGHT_EPISODE
        }
        
        // 质量评分
        val qualityScore = calculateQualityScore(subtitle)
        totalScore += qualityScore * WEIGHT_QUALITY
        totalWeight += WEIGHT_QUALITY
        
        // 发布组匹配
        val releaseGroupScore = calculateReleaseGroupScore(getFileNameFromMediaItem(mediaItem), subtitle.title)
        totalScore += releaseGroupScore * WEIGHT_RELEASE_GROUP
        totalWeight += WEIGHT_RELEASE_GROUP

        // 分辨率匹配
        val resolutionScore = calculateResolutionScore(getFileNameFromMediaItem(mediaItem), subtitle.title)
        totalScore += resolutionScore * WEIGHT_RESOLUTION
        totalWeight += WEIGHT_RESOLUTION
        
        return if (totalWeight > 0) totalScore / totalWeight else 0.0f
    }
    
    /**
     * 计算语言匹配分数
     */
    private fun calculateLanguageScore(subtitleLanguage: String, config: SubtitleConfig): Float {
        return when {
            subtitleLanguage.equals(config.primaryLanguage, ignoreCase = true) -> 1.0f
            subtitleLanguage.startsWith(config.primaryLanguage, ignoreCase = true) -> 0.9f
            subtitleLanguage.equals(config.fallbackLanguage, ignoreCase = true) -> 0.7f
            subtitleLanguage.startsWith(config.fallbackLanguage, ignoreCase = true) -> 0.6f
            else -> 0.2f
        }
    }
    
    /**
     * 计算文本相似度
     */
    private fun calculateTextSimilarity(text1: String, text2: String): Float {
        if (text1.isEmpty() || text2.isEmpty()) return 0.0f
        
        val t1 = normalizeText(text1)
        val t2 = normalizeText(text2)
        
        // 完全匹配
        if (t1.equals(t2, ignoreCase = true)) return 1.0f
        
        // 包含关系
        if (t1.contains(t2, ignoreCase = true) || t2.contains(t1, ignoreCase = true)) {
            return 0.8f
        }
        
        // 编辑距离
        val editDistance = calculateEditDistance(t1, t2)
        val maxLength = max(t1.length, t2.length)
        val similarity = 1.0f - (editDistance.toFloat() / maxLength)
        
        return max(0.0f, similarity)
    }
    
    /**
     * 计算年份匹配分数
     */
    private fun calculateYearScore(mediaYear: Int, subtitleYear: Int): Float {
        if (mediaYear <= 0 || subtitleYear <= 0) return 0.5f
        
        val yearDiff = abs(mediaYear - subtitleYear)
        return when {
            yearDiff == 0 -> 1.0f
            yearDiff == 1 -> 0.8f
            yearDiff == 2 -> 0.6f
            yearDiff <= 5 -> 0.4f
            else -> 0.1f
        }
    }
    
    /**
     * 计算剧集匹配分数
     */
    private fun calculateEpisodeScore(mediaItem: MediaItem, subtitle: Subtitle): Float {
        val subtitleSeason = subtitle.metadata["season_number"]?.toIntOrNull()
        val subtitleEpisode = subtitle.metadata["episode_number"]?.toIntOrNull()
        
        if (subtitleSeason == null || subtitleEpisode == null) {
            // 尝试从标题中提取季集信息
            val titleMatch = extractSeasonEpisode(subtitle.title)
            if (titleMatch != null) {
                return if (titleMatch.first == mediaItem.seasonNumber && 
                          titleMatch.second == mediaItem.episodeNumber) 1.0f else 0.0f
            }
            return 0.3f // 没有明确的季集信息
        }
        
        return if (subtitleSeason == mediaItem.seasonNumber && 
                  subtitleEpisode == mediaItem.episodeNumber) 1.0f else 0.0f
    }
    
    /**
     * 计算质量分数
     */
    private fun calculateQualityScore(subtitle: Subtitle): Float {
        var score = 0.0f
        
        // 评分权重
        if (subtitle.rating > 0) {
            score += (subtitle.rating / 10.0f) * 0.4f
        }
        
        // 下载次数权重
        if (subtitle.downloadCount > 0) {
            val downloadScore = min(1.0f, subtitle.downloadCount / 1000.0f)
            score += downloadScore * 0.3f
        }
        
        // 来源权重
        score += when (subtitle.source) {
            SubtitleSource.OPENSUBTITLES -> 0.3f
            SubtitleSource.SUBSCENE -> 0.25f
            SubtitleSource.LOCAL -> 0.2f
            else -> 0.1f
        }
        
        return min(1.0f, score)
    }
    
    /**
     * 计算发布组匹配分数
     */
    private fun calculateReleaseGroupScore(fileName: String, subtitleTitle: String): Float {
        val fileGroup = extractReleaseGroup(fileName)
        val subtitleGroup = extractReleaseGroup(subtitleTitle)
        
        if (fileGroup.isEmpty() || subtitleGroup.isEmpty()) return 0.3f
        
        return if (fileGroup.equals(subtitleGroup, ignoreCase = true)) 1.0f else 0.1f
    }
    
    /**
     * 计算分辨率匹配分数
     */
    private fun calculateResolutionScore(fileName: String, subtitleTitle: String): Float {
        val fileResolution = extractResolution(fileName)
        val subtitleResolution = extractResolution(subtitleTitle)
        
        if (fileResolution.isEmpty() || subtitleResolution.isEmpty()) return 0.5f
        
        return if (fileResolution.equals(subtitleResolution, ignoreCase = true)) 1.0f else 0.2f
    }
    
    /**
     * 计算置信度
     */
    private fun calculateConfidence(similarity: Float, mediaItem: MediaItem, subtitle: Subtitle): Float {
        var confidence = similarity
        
        // 降低置信度的因素
        if (subtitle.source == SubtitleSource.UNKNOWN) confidence *= 0.8f
        if (subtitle.rating < 5.0f && subtitle.downloadCount < 100) confidence *= 0.9f
        if ((mediaItem.mediaType == MediaType.TV_EPISODE || mediaItem.mediaType == MediaType.TV_SERIES) && !hasEpisodeInfo(subtitle)) confidence *= 0.7f
        
        // 提高置信度的因素
        if (subtitle.rating > 8.0f) confidence *= 1.1f
        if (subtitle.downloadCount > 1000) confidence *= 1.05f
        if (subtitle.source == SubtitleSource.OPENSUBTITLES) confidence *= 1.02f
        
        return min(1.0f, confidence)
    }
    
    /**
     * 生成匹配原因
     */
    private fun generateMatchReasons(
        mediaItem: MediaItem,
        subtitle: Subtitle,
        similarity: Float
    ): List<MatchReason> {
        val reasons = mutableListOf<MatchReason>()
        
        if (similarity >= EXCELLENT_MATCH_THRESHOLD) {
            reasons.add(MatchReason.EXCELLENT_MATCH)
        } else if (similarity >= GOOD_MATCH_THRESHOLD) {
            reasons.add(MatchReason.GOOD_MATCH)
        }
        
        // 具体匹配原因
        if (calculateTextSimilarity(mediaItem.title, subtitle.title) > 0.8f) {
            reasons.add(MatchReason.TITLE_MATCH)
        }
        
        val mediaYear = getYearFromMediaItem(mediaItem)
        if (mediaYear > 0 && subtitle.metadata["year"]?.toIntOrNull() == mediaYear) {
            reasons.add(MatchReason.YEAR_MATCH)
        }
        
        if ((mediaItem.mediaType == MediaType.TV_EPISODE || mediaItem.mediaType == MediaType.TV_SERIES) && calculateEpisodeScore(mediaItem, subtitle) == 1.0f) {
            reasons.add(MatchReason.EPISODE_MATCH)
        }
        
        if (subtitle.rating > 8.0f) {
            reasons.add(MatchReason.HIGH_RATING)
        }
        
        if (subtitle.downloadCount > 1000) {
            reasons.add(MatchReason.POPULAR)
        }
        
        return reasons
    }
    
    // 工具方法
    
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    private fun calculateEditDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        
        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        
        return dp[m][n]
    }
    
    private fun extractReleaseGroup(text: String): String {
        val regex = """-(\w+)$""".toRegex()
        return regex.find(text)?.groupValues?.get(1) ?: ""
    }
    
    private fun extractResolution(text: String): String {
        val regex = """(720p|1080p|1440p|2160p|4K)""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(text)?.value ?: ""
    }
    
    private fun extractSeasonEpisode(text: String): Pair<Int, Int>? {
        val regex = """[Ss](\d+)[Ee](\d+)""".toRegex()
        val match = regex.find(text) ?: return null
        
        val season = match.groupValues[1].toIntOrNull() ?: return null
        val episode = match.groupValues[2].toIntOrNull() ?: return null
        
        return Pair(season, episode)
    }
    
    private fun hasEpisodeInfo(subtitle: Subtitle): Boolean {
        return subtitle.metadata.containsKey("season_number") ||
               subtitle.metadata.containsKey("episode_number") ||
               extractSeasonEpisode(subtitle.title) != null
    }
    
    private fun parseFileName(fileName: String): FileNameInfo {
        val cleanName = fileName.substringBeforeLast('.')
        
        return FileNameInfo(
            title = extractTitle(cleanName),
            year = extractYear(cleanName),
            season = extractSeasonEpisode(cleanName)?.first ?: 0,
            episode = extractSeasonEpisode(cleanName)?.second ?: 0,
            resolution = extractResolution(cleanName),
            releaseGroup = extractReleaseGroup(cleanName)
        )
    }
    
    private fun extractTitle(fileName: String): String {
        return fileName
            .replace(Regex("""\d{4}"""), "") // 移除年份
            .replace(Regex("""[Ss]\d+[Ee]\d+"""), "") // 移除季集
            .replace(Regex("""(720p|1080p|1440p|2160p|4K)""", RegexOption.IGNORE_CASE), "") // 移除分辨率
            .replace(Regex("""-\w+$"""), "") // 移除发布组
            .replace(Regex("""[.\-_\s]+"""), " ") // 规范化分隔符
            .trim()
    }
    
    private fun extractYear(fileName: String): Int {
        val regex = """(\d{4})""".toRegex()
        return regex.find(fileName)?.value?.toIntOrNull() ?: 0
    }
    
    private fun calculateFileNameSimilarity(
        fileInfo: FileNameInfo,
        subtitle: Subtitle,
        languages: List<String>
    ): Float {
        var score = 0.0f
        var weight = 0.0f
        
        // 标题匹配
        val titleScore = calculateTextSimilarity(fileInfo.title, subtitle.title)
        score += titleScore * 40
        weight += 40
        
        // 年份匹配
        if (fileInfo.year > 0) {
            val yearScore = calculateYearScore(fileInfo.year, subtitle.metadata["year"]?.toIntOrNull() ?: 0)
            score += yearScore * 20
            weight += 20
        }
        
        // 季集匹配
        if (fileInfo.season > 0 && fileInfo.episode > 0) {
            val episodeMatch = extractSeasonEpisode(subtitle.title)
            val episodeScore = if (episodeMatch != null && 
                                   episodeMatch.first == fileInfo.season && 
                                   episodeMatch.second == fileInfo.episode) 1.0f else 0.0f
            score += episodeScore * 25
            weight += 25
        }
        
        // 语言匹配
        val languageScore = if (languages.any { subtitle.language.startsWith(it, ignoreCase = true) }) 1.0f else 0.3f
        score += languageScore * 15
        weight += 15
        
        return if (weight > 0) score / weight else 0.0f
    }
    
    private fun generateFileNameMatchReasons(fileInfo: FileNameInfo, subtitle: Subtitle): List<MatchReason> {
        val reasons = mutableListOf<MatchReason>()
        
        if (calculateTextSimilarity(fileInfo.title, subtitle.title) > 0.8f) {
            reasons.add(MatchReason.TITLE_MATCH)
        }
        
        if (fileInfo.year > 0 && subtitle.metadata["year"]?.toIntOrNull() == fileInfo.year) {
            reasons.add(MatchReason.YEAR_MATCH)
        }
        
        val episodeMatch = extractSeasonEpisode(subtitle.title)
        if (episodeMatch != null && fileInfo.season > 0 && fileInfo.episode > 0 &&
            episodeMatch.first == fileInfo.season && episodeMatch.second == fileInfo.episode) {
            reasons.add(MatchReason.EPISODE_MATCH)
        }
        
        return reasons
    }
    
    private data class FileNameInfo(
        val title: String,
        val year: Int,
        val season: Int,
        val episode: Int,
        val resolution: String,
        val releaseGroup: String
    )
}

/**
 * 字幕匹配结果
 */
data class SubtitleMatch(
    val subtitle: Subtitle,
    val similarity: Float, // 相似度 (0.0-1.0)
    val confidence: Float, // 置信度 (0.0-1.0)
    val matchReasons: List<MatchReason> = emptyList()
) {
    val isExcellentMatch: Boolean
        get() = similarity >= 0.9f
        
    val isGoodMatch: Boolean
        get() = similarity >= 0.7f
        
    val qualityLevel: MatchQuality
        get() = when {
            similarity >= 0.9f -> MatchQuality.EXCELLENT
            similarity >= 0.7f -> MatchQuality.GOOD
            similarity >= 0.5f -> MatchQuality.FAIR
            else -> MatchQuality.POOR
        }
}

/**
 * 匹配质量等级
 */
enum class MatchQuality {
    EXCELLENT, // 优秀匹配
    GOOD,      // 良好匹配
    FAIR,      // 一般匹配
    POOR       // 较差匹配
}

/**
 * 匹配原因
 */
enum class MatchReason {
    EXCELLENT_MATCH,  // 完美匹配
    GOOD_MATCH,       // 良好匹配
    TITLE_MATCH,      // 标题匹配
    YEAR_MATCH,       // 年份匹配
    EPISODE_MATCH,    // 剧集匹配
    LANGUAGE_MATCH,   // 语言匹配
    HIGH_RATING,      // 高评分
    POPULAR,          // 热门字幕
    RELEASE_GROUP,    // 发布组匹配
    RESOLUTION_MATCH  // 分辨率匹配
}

/**
 * 兼容性检查结果
 */
data class CompatibilityResult(
    val isCompatible: Boolean,
    val severity: CompatibilitySeverity,
    val issues: List<CompatibilityIssue>
) {
    val hasWarnings: Boolean
        get() = issues.isNotEmpty()
        
    val hasCriticalIssues: Boolean
        get() = issues.any { it.isCritical }
}

/**
 * 兼容性问题严重程度
 */
enum class CompatibilitySeverity {
    NONE,     // 无问题
    WARNING,  // 警告
    CRITICAL  // 严重问题
}

/**
 * 兼容性问题类型
 */
enum class CompatibilityIssue(val isCritical: Boolean, val description: String) {
    SEASON_MISMATCH(true, "季数不匹配"),
    EPISODE_MISMATCH(true, "集数不匹配"),
    YEAR_MISMATCH(false, "年份差异较大"),
    DURATION_MISMATCH(false, "时长差异较大"),
    SIZE_SUSPICIOUS(false, "文件大小异常"),
    VALIDATION_ERROR(true, "验证过程出错")
}

/**
 * 从MediaItem获取年份
 */
private fun getYearFromMediaItem(mediaItem: MediaItem): Int {
    return mediaItem.releaseDate?.let { date ->
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.get(java.util.Calendar.YEAR)
    } ?: 0
}

/**
 * 从MediaItem获取文件名
 */
private fun getFileNameFromMediaItem(mediaItem: MediaItem): String {
    return mediaItem.filePath.substringAfterLast('/')
}