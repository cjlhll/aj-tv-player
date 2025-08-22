package com.tvplayer.webdav.data.subtitle

import android.content.Context
import android.util.Log
import com.tvplayer.webdav.data.model.*
import com.tvplayer.webdav.data.storage.SubtitleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 字幕管理器
 * 统一管理多个字幕源的搜索、下载和缓存
 */
@Singleton
class SubtitleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openSubtitlesService: OpenSubtitlesService,
    private val subtitleCache: SubtitleCache
) {
    
    companion object {
        private const val TAG = "SubtitleManager"
        private const val SUBTITLE_DIR = "subtitles"
    }
    
    private val subtitleServices: List<SubtitleSearchService> = listOf(
        openSubtitlesService
        // 可以添加更多字幕源服务
    )
    
    private val subtitleDir: File by lazy {
        File(context.cacheDir, SUBTITLE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 搜索字幕
     * @param request 搜索请求
     * @param config 字幕配置
     * @return 字幕搜索结果
     */
    suspend fun searchSubtitles(
        request: SubtitleSearchRequest,
        config: SubtitleConfig
    ): SubtitleSearchResult {
        Log.d(TAG, "Searching subtitles for: ${request.title}")
        
        return withContext(Dispatchers.IO) {
            try {
                // 首先检查缓存
                val cachedSubtitles = getCachedSubtitles(request)
                if (cachedSubtitles.isNotEmpty()) {
                    Log.d(TAG, "Found ${cachedSubtitles.size} cached subtitles")
                    return@withContext SubtitleSearchResult(
                        subtitles = cachedSubtitles,
                        totalCount = cachedSubtitles.size,
                        source = SubtitleSource.LOCAL
                    )
                }
                
                // 并发搜索所有启用的字幕源
                val enabledServices = subtitleServices.filter { service ->
                    request.sources.contains(service.supportedSource)
                }
                
                if (enabledServices.isEmpty()) {
                    return@withContext SubtitleSearchResult(
                        errors = listOf("No enabled subtitle sources")
                    )
                }
                
                val startTime = System.currentTimeMillis()
                val deferredResults = enabledServices.map { service ->
                    async {
                        try {
                            if (service.isAvailable()) {
                                service.searchSubtitles(request)
                            } else {
                                Result.failure(Exception("Service ${service.supportedSource} not available"))
                            }
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }
                }
                
                // 等待所有搜索完成
                val results = deferredResults.awaitAll()
                val searchTime = System.currentTimeMillis() - startTime
                
                // 合并所有结果
                val allSubtitles = mutableListOf<Subtitle>()
                val errors = mutableListOf<String>()
                
                results.forEach { result ->
                    result.fold(
                        onSuccess = { subtitles ->
                            allSubtitles.addAll(subtitles)
                        },
                        onFailure = { error ->
                            errors.add(error.message ?: "Unknown error")
                        }
                    )
                }
                
                // 排序和过滤
                val sortedSubtitles = sortAndFilterSubtitles(allSubtitles, config)
                
                // 缓存结果
                cacheSubtitles(request, sortedSubtitles)
                
                Log.d(TAG, "Search completed: ${sortedSubtitles.size} subtitles found in ${searchTime}ms")
                
                SubtitleSearchResult(
                    subtitles = sortedSubtitles,
                    totalCount = allSubtitles.size,
                    searchTime = searchTime,
                    source = if (enabledServices.size == 1) enabledServices.first().supportedSource else SubtitleSource.UNKNOWN,
                    errors = errors
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error searching subtitles", e)
                SubtitleSearchResult(
                    errors = listOf(e.message ?: "Search failed")
                )
            }
        }
    }
    
    /**
     * 下载字幕
     * @param subtitle 字幕信息
     * @param mediaId 媒体ID（用于组织文件）
     * @return 下载结果
     */
    suspend fun downloadSubtitle(subtitle: Subtitle, mediaId: String): Result<Subtitle> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Downloading subtitle: ${subtitle.id}")
                
                // 生成本地文件路径
                val fileName = generateSubtitleFileName(subtitle, mediaId)
                val localPath = File(subtitleDir, fileName).absolutePath
                
                // 检查是否已经下载
                if (File(localPath).exists()) {
                    Log.d(TAG, "Subtitle already downloaded: $localPath")
                    val downloadedSubtitle = subtitle.copy(
                        localPath = localPath,
                        isDownloaded = true
                    )
                    subtitleCache.saveSubtitle(downloadedSubtitle)
                    return@withContext Result.success(downloadedSubtitle)
                }
                
                // 查找对应的服务
                val service = subtitleServices.find { 
                    it.supportedSource == subtitle.source 
                } ?: return@withContext Result.failure(
                    Exception("No service found for source: ${subtitle.source}")
                )
                
                // 下载字幕文件
                val downloadResult = service.downloadSubtitle(subtitle, localPath)
                
                downloadResult.fold(
                    onSuccess = { downloadedPath ->
                        val downloadedSubtitle = subtitle.copy(
                            localPath = downloadedPath,
                            isDownloaded = true
                        )
                        
                        // 保存到缓存
                        subtitleCache.saveSubtitle(downloadedSubtitle)
                        
                        Log.d(TAG, "Subtitle downloaded successfully: $downloadedPath")
                        Result.success(downloadedSubtitle)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to download subtitle: ${error.message}")
                        Result.failure(error)
                    }
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading subtitle", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取媒体的所有可用字幕
     * @param mediaItem 媒体项
     * @param config 字幕配置
     * @return 可用字幕列表
     */
    suspend fun getAvailableSubtitles(
        mediaItem: MediaItem,
        config: SubtitleConfig
    ): List<Subtitle> {
        return withContext(Dispatchers.IO) {
            try {
                // 先查找本地字幕
                val localSubtitles = findLocalSubtitles(mediaItem)
                
                // 查找缓存的字幕
                val cachedSubtitles = getCachedSubtitles(
                    SubtitleSearchRequest.fromMediaItem(mediaItem, config)
                ).filter { it.isDownloaded && it.isAvailable() }
                
                // 合并并去重
                val allSubtitles = (localSubtitles + cachedSubtitles)
                    .distinctBy { "${it.language}_${it.source}_${it.hash}" }
                
                Log.d(TAG, "Found ${allSubtitles.size} available subtitles for ${mediaItem.title}")
                allSubtitles
                
            } catch (e: Exception) {
                Log.e(TAG, "Error getting available subtitles", e)
                emptyList()
            }
        }
    }
    
    /**
     * 自动选择最佳字幕
     * @param mediaItem 媒体项
     * @param config 字幕配置
     * @return 推荐的字幕，如果没有合适的返回null
     */
    suspend fun selectBestSubtitle(
        mediaItem: MediaItem,
        config: SubtitleConfig
    ): Subtitle? {
        return withContext(Dispatchers.IO) {
            try {
                val availableSubtitles = getAvailableSubtitles(mediaItem, config)
                
                if (availableSubtitles.isEmpty()) {
                    // 如果没有可用字幕，自动搜索
                    if (config.autoDownload) {
                        val searchRequest = SubtitleSearchRequest.fromMediaItem(mediaItem, config)
                        val searchResult = searchSubtitles(searchRequest, config)
                        
                        if (searchResult.hasResults) {
                            val bestSubtitle = searchResult.subtitles.first()
                            val downloadResult = downloadSubtitle(bestSubtitle, mediaItem.id)
                            
                            return@withContext downloadResult.getOrNull()
                        }
                    }
                    return@withContext null
                }
                
                // 根据配置选择最佳字幕
                selectBestFromAvailable(availableSubtitles, config)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting best subtitle", e)
                null
            }
        }
    }
    
    /**
     * 清理过期的字幕缓存
     */
    suspend fun cleanExpiredCache() {
        withContext(Dispatchers.IO) {
            try {
                val expiredFiles = subtitleDir.listFiles()?.filter { file ->
                    val ageInDays = (System.currentTimeMillis() - file.lastModified()) / (24 * 60 * 60 * 1000)
                    ageInDays > 30 // 删除30天前的文件
                } ?: emptyList()
                
                expiredFiles.forEach { file ->
                    if (file.delete()) {
                        Log.d(TAG, "Deleted expired subtitle: ${file.name}")
                    }
                }
                
                // 清理缓存数据库中的过期记录
                subtitleCache.cleanExpiredSubtitles(30)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning cache", e)
            }
        }
    }
    
    /**
     * 获取API限制信息
     */
    suspend fun getApiLimits(): Map<SubtitleSource, ApiLimitInfo> {
        return withContext(Dispatchers.IO) {
            subtitleServices.associate { service ->
                service.supportedSource to service.getApiLimits()
            }
        }
    }
    
    // 私有辅助方法
    
    private fun getCachedSubtitles(request: SubtitleSearchRequest): List<Subtitle> {
        return subtitleCache.getSubtitles(request.getFileIdentifier())
    }
    
    private suspend fun cacheSubtitles(request: SubtitleSearchRequest, subtitles: List<Subtitle>) {
        try {
            subtitles.forEach { subtitle ->
                subtitleCache.saveSubtitle(subtitle, request.getFileIdentifier())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache subtitles: ${e.message}")
        }
    }
    
    private fun sortAndFilterSubtitles(
        subtitles: List<Subtitle>,
        config: SubtitleConfig
    ): List<Subtitle> {
        return subtitles
            .filter { subtitle ->
                // 语言过滤
                val languages = listOf(config.primaryLanguage, config.fallbackLanguage)
                languages.any { lang -> subtitle.language.startsWith(lang, ignoreCase = true) }
            }
            .sortedWith(compareByDescending<Subtitle> { subtitle ->
                // 语言匹配优先级
                when {
                    subtitle.language.equals(config.primaryLanguage, ignoreCase = true) -> 3
                    subtitle.language.equals(config.fallbackLanguage, ignoreCase = true) -> 2
                    subtitle.language.startsWith(config.primaryLanguage, ignoreCase = true) -> 1
                    else -> 0
                }
            }.thenByDescending { subtitle ->
                // 质量评分
                when (config.downloadQuality) {
                    SubtitleQuality.BEST -> subtitle.rating
                    SubtitleQuality.MOST_DOWNLOADED -> subtitle.downloadCount.toFloat()
                    SubtitleQuality.LATEST -> subtitle.uploadDate.toFloat()
                    SubtitleQuality.ANY -> 1.0f
                }
            })
            .take(config.maxResults)
    }
    
    private fun findLocalSubtitles(mediaItem: MediaItem): List<Subtitle> {
        val subtitles = mutableListOf<Subtitle>()
        
        try {
            val mediaFile = File(mediaItem.filePath)
            val mediaDir = mediaFile.parentFile ?: return emptyList()
            val mediaBaseName = mediaFile.nameWithoutExtension
            
            // 查找同名字幕文件
            val subtitleFiles = mediaDir.listFiles { _, name ->
                val nameWithoutExt = name.substringBeforeLast('.')
                val extension = name.substringAfterLast('.', "")
                
                nameWithoutExt.startsWith(mediaBaseName, ignoreCase = true) &&
                SubtitleFormat.values().any { it.extension.equals(extension, ignoreCase = true) }
            } ?: emptyArray()
            
            subtitleFiles.forEach { file ->
                val language = extractLanguageFromFileName(file.name)
                val format = SubtitleFormat.fromFileName(file.name)
                
                val subtitle = Subtitle(
                    id = "local_${file.absolutePath.hashCode()}",
                    title = file.nameWithoutExtension,
                    language = language,
                    languageName = getLanguageDisplayName(language),
                    format = format,
                    localPath = file.absolutePath,
                    fileSize = file.length(),
                    source = SubtitleSource.LOCAL,
                    isDownloaded = true
                )
                
                subtitles.add(subtitle)
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error finding local subtitles: ${e.message}")
        }
        
        return subtitles
    }
    
    private fun extractLanguageFromFileName(fileName: String): String {
        val patterns = mapOf(
            "zh|chinese|chs|cn" to "zh-cn",
            "cht|tw|traditional" to "zh-tw",
            "en|english|eng" to "en",
            "ja|japanese|jp" to "ja",
            "ko|korean|kr" to "ko"
        )
        
        val lowerFileName = fileName.lowercase()
        
        for ((pattern, language) in patterns) {
            if (pattern.split("|").any { lowerFileName.contains(it) }) {
                return language
            }
        }
        
        return "unknown"
    }
    
    private fun selectBestFromAvailable(
        subtitles: List<Subtitle>,
        config: SubtitleConfig
    ): Subtitle? {
        return subtitles
            .filter { it.isAvailable() }
            .firstOrNull { it.language.equals(config.primaryLanguage, ignoreCase = true) }
            ?: subtitles
                .filter { it.isAvailable() }
                .firstOrNull { it.language.equals(config.fallbackLanguage, ignoreCase = true) }
            ?: subtitles.firstOrNull { it.isAvailable() }
    }
    
    private fun generateSubtitleFileName(subtitle: Subtitle, mediaId: String): String {
        val sanitizedTitle = subtitle.title
            .replace(Regex("[^a-zA-Z0-9\\-_.]"), "_")
            .take(50)
        
        return "${mediaId}_${subtitle.language}_${sanitizedTitle}.${subtitle.format.extension}"
    }
    
    private fun getLanguageDisplayName(language: String): String {
        return when (language.lowercase()) {
            "zh-cn", "zh", "chinese", "chs" -> "简体中文"
            "zh-tw", "cht", "traditional" -> "繁体中文"
            "en", "english", "eng" -> "English"
            "ja", "japanese", "jp" -> "日本語"
            "ko", "korean", "kr" -> "한국어"
            else -> language.uppercase()
        }
    }
}