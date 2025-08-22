package com.tvplayer.webdav.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvplayer.webdav.data.model.Subtitle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 字幕缓存管理器
 * 负责字幕信息的本地存储、检索和清理
 */
@Singleton
class SubtitleCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "SubtitleCache"
        private const val PREFS_NAME = "subtitle_cache"
        private const val KEY_SUBTITLES = "cached_subtitles"
        private const val KEY_MEDIA_MAPPING = "media_subtitle_mapping"
        private const val KEY_LAST_CLEANUP = "last_cleanup"
        private const val SUBTITLE_DIR = "subtitles"
        private const val MAX_CACHE_SIZE = 100 * 1024 * 1024L // 100MB
        private const val CLEANUP_INTERVAL_DAYS = 7
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val cacheMutex = Mutex()

    // 协程作用域用于后台清理任务
    private val cacheScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 内存缓存
    private val subtitleMemoryCache = ConcurrentHashMap<String, Subtitle>()
    private val mediaMappingCache = ConcurrentHashMap<String, MutableSet<String>>()
    
    private val subtitleDir: File by lazy {
        File(context.cacheDir, SUBTITLE_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    init {
        // 启动时加载缓存
        loadCacheFromPreferences()
        
        // 检查是否需要清理
        checkAndCleanupIfNeeded()
    }
    
    /**
     * 保存字幕到缓存
     * @param subtitle 字幕信息
     * @param mediaId 关联的媒体ID（可选）
     */
    suspend fun saveSubtitle(subtitle: Subtitle, mediaId: String? = null) {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    // 保存到内存缓存
                    subtitleMemoryCache[subtitle.id] = subtitle
                    
                    // 如果有关联媒体，建立映射关系
                    mediaId?.let { id ->
                        val subtitleIds = mediaMappingCache.getOrPut(id) { mutableSetOf() }
                        subtitleIds.add(subtitle.id)
                    }
                    
                    // 持久化到SharedPreferences
                    persistCacheToPreferences()
                    
                    Log.d(TAG, "Subtitle cached: ${subtitle.id} for media: $mediaId")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving subtitle to cache", e)
                }
            }
        }
    }
    
    /**
     * 获取媒体相关的字幕
     * @param mediaId 媒体ID
     * @return 字幕列表
     */
    fun getSubtitles(mediaId: String): List<Subtitle> {
        return try {
            val subtitleIds = mediaMappingCache[mediaId] ?: return emptyList()
            subtitleIds.mapNotNull { subtitleMemoryCache[it] }
                .filter { it.isAvailable() } // 只返回可用的字幕
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subtitles for media: $mediaId", e)
            emptyList()
        }
    }
    
    /**
     * 根据ID获取字幕
     * @param subtitleId 字幕ID
     * @return 字幕信息，如果不存在返回null
     */
    fun getSubtitle(subtitleId: String): Subtitle? {
        return subtitleMemoryCache[subtitleId]
    }
    
    /**
     * 搜索字幕
     * @param query 搜索关键词
     * @param language 语言过滤（可选）
     * @return 匹配的字幕列表
     */
    fun searchSubtitles(query: String, language: String? = null): List<Subtitle> {
        return try {
            val lowerQuery = query.lowercase()
            subtitleMemoryCache.values.filter { subtitle ->
                val matchesQuery = subtitle.title.lowercase().contains(lowerQuery) ||
                                 subtitle.languageName.lowercase().contains(lowerQuery)
                
                val matchesLanguage = language?.let { lang ->
                    subtitle.language.equals(lang, ignoreCase = true) ||
                    subtitle.language.startsWith(lang, ignoreCase = true)
                } ?: true
                
                matchesQuery && matchesLanguage && subtitle.isAvailable()
            }.sortedByDescending { it.rating }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching subtitles", e)
            emptyList()
        }
    }
    
    /**
     * 删除字幕
     * @param subtitleId 字幕ID
     */
    suspend fun removeSubtitle(subtitleId: String) {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    val subtitle = subtitleMemoryCache[subtitleId]
                    
                    // 删除本地文件
                    subtitle?.let { sub ->
                        if (sub.isDownloaded && sub.localPath.isNotEmpty()) {
                            val file = File(sub.localPath)
                            if (file.exists() && file.delete()) {
                                Log.d(TAG, "Deleted subtitle file: ${sub.localPath}")
                            }
                        }
                    }
                    
                    // 从内存缓存移除
                    subtitleMemoryCache.remove(subtitleId)
                    
                    // 从媒体映射中移除
                    mediaMappingCache.values.forEach { subtitleIds ->
                        subtitleIds.remove(subtitleId)
                    }
                    
                    // 持久化更改
                    persistCacheToPreferences()
                    
                    Log.d(TAG, "Subtitle removed from cache: $subtitleId")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing subtitle from cache", e)
                }
            }
        }
    }
    
    /**
     * 清理过期字幕
     * @param expireDays 过期天数
     */
    suspend fun cleanExpiredSubtitles(expireDays: Int = 30) {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    val expireTime = System.currentTimeMillis() - (expireDays * 24 * 60 * 60 * 1000L)
                    val expiredSubtitleIds = mutableListOf<String>()
                    
                    subtitleMemoryCache.values.forEach { subtitle ->
                        if (subtitle.uploadDate > 0 && subtitle.uploadDate < expireTime) {
                            expiredSubtitleIds.add(subtitle.id)
                        }
                    }
                    
                    // 删除过期字幕
                    for (subtitleId in expiredSubtitleIds) {
                        try {
                            val subtitle = subtitleMemoryCache[subtitleId]
                            
                            // 删除本地文件
                            subtitle?.let { sub ->
                                if (sub.isDownloaded && sub.localPath.isNotEmpty()) {
                                    val file = File(sub.localPath)
                                    if (file.exists() && file.delete()) {
                                        Log.d(TAG, "Deleted subtitle file: ${sub.localPath}")
                                    }
                                }
                            }
                            
                            // 从内存缓存移除
                            subtitleMemoryCache.remove(subtitleId)
                            
                            // 从媒体映射中移除
                            mediaMappingCache.values.forEach { subtitleIds ->
                                subtitleIds.remove(subtitleId)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error removing expired subtitle: $subtitleId", e)
                        }
                    }
                    
                    // 持久化更改
                    persistCacheToPreferences()
                    
                    // 清理孤立的字幕文件
                    cleanOrphanedFiles()
                    
                    // 更新最后清理时间
                    preferences.edit()
                        .putLong(KEY_LAST_CLEANUP, System.currentTimeMillis())
                        .apply()
                    
                    Log.d(TAG, "Cleaned ${expiredSubtitleIds.size} expired subtitles")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning expired subtitles", e)
                }
            }
        }
    }
    
    /**
     * 清理缓存以释放空间
     * @param targetSize 目标大小（字节）
     */
    suspend fun cleanupToSize(targetSize: Long = MAX_CACHE_SIZE) {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    val currentSize = getCacheSize()
                    if (currentSize <= targetSize) {
                        return@withLock
                    }
                    
                    Log.d(TAG, "Cache size: $currentSize bytes, target: $targetSize bytes")
                    
                    // 按最后访问时间排序，删除最老的文件
                    val subtitleFiles = subtitleDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withLock
                    var freedSize = 0L
                    
                    for (file in subtitleFiles) {
                        if (currentSize - freedSize <= targetSize) {
                            break
                        }
                        
                        val fileSize = file.length()
                        if (file.delete()) {
                            freedSize += fileSize
                            
                            // 从缓存中移除对应的字幕记录（非异步方式）
                            val subtitleToRemove = subtitleMemoryCache.values.find { 
                                it.localPath == file.absolutePath 
                            }
                            subtitleToRemove?.let { subtitle ->
                                // 直接从内存缓存移除，避免suspend函数调用
                                subtitleMemoryCache.remove(subtitle.id)
                                
                                // 从媒体映射中移除
                                mediaMappingCache.values.forEach { subtitleIds ->
                                    subtitleIds.remove(subtitle.id)
                                }
                            }
                        }
                    }
                    
                    Log.d(TAG, "Freed $freedSize bytes from cache")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error cleaning up cache", e)
                }
            }
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return try {
            val totalSubtitles = subtitleMemoryCache.size
            val downloadedSubtitles = subtitleMemoryCache.values.count { it.isDownloaded }
            val cacheSize = getCacheSize()
            val lastCleanup = preferences.getLong(KEY_LAST_CLEANUP, 0L)
            
            CacheStats(
                totalSubtitles = totalSubtitles,
                downloadedSubtitles = downloadedSubtitles,
                cacheSize = cacheSize,
                lastCleanup = lastCleanup
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache stats", e)
            CacheStats()
        }
    }
    
    /**
     * 清空所有缓存
     */
    suspend fun clearAllCache() {
        withContext(Dispatchers.IO) {
            cacheMutex.withLock {
                try {
                    // 删除所有字幕文件
                    subtitleDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                    
                    // 清空内存缓存
                    subtitleMemoryCache.clear()
                    mediaMappingCache.clear()
                    
                    // 清空持久化数据
                    preferences.edit().clear().apply()
                    
                    Log.d(TAG, "All cache cleared")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing cache", e)
                }
            }
        }
    }
    
    // 私有辅助方法
    
    private fun loadCacheFromPreferences() {
        try {
            // 加载字幕数据
            val subtitlesJson = preferences.getString(KEY_SUBTITLES, "")
            if (!subtitlesJson.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, Subtitle>>() {}.type
                val subtitles: Map<String, Subtitle> = gson.fromJson(subtitlesJson, type)
                subtitleMemoryCache.putAll(subtitles)
            }
            
            // 加载媒体映射
            val mappingJson = preferences.getString(KEY_MEDIA_MAPPING, "")
            if (!mappingJson.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, Set<String>>>() {}.type
                val mapping: Map<String, Set<String>> = gson.fromJson(mappingJson, type)
                mapping.forEach { (mediaId, subtitleIds) ->
                    mediaMappingCache[mediaId] = subtitleIds.toMutableSet()
                }
            }
            
            Log.d(TAG, "Loaded ${subtitleMemoryCache.size} subtitles from cache")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cache from preferences", e)
        }
    }
    
    private fun persistCacheToPreferences() {
        try {
            val editor = preferences.edit()
            
            // 保存字幕数据
            val subtitlesJson = gson.toJson(subtitleMemoryCache)
            editor.putString(KEY_SUBTITLES, subtitlesJson)
            
            // 保存媒体映射
            val mappingJson = gson.toJson(mediaMappingCache)
            editor.putString(KEY_MEDIA_MAPPING, mappingJson)
            
            editor.apply()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting cache to preferences", e)
        }
    }
    
    private fun getCacheSize(): Long {
        return try {
            subtitleDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun cleanOrphanedFiles() {
        try {
            val cachedPaths = subtitleMemoryCache.values
                .mapNotNull { it.localPath.takeIf { path -> path.isNotEmpty() } }
                .toSet()
            
            subtitleDir.listFiles()?.forEach { file ->
                if (file.absolutePath !in cachedPaths) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted orphaned file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error cleaning orphaned files: ${e.message}")
        }
    }
    
    private fun checkAndCleanupIfNeeded() {
        try {
            val lastCleanup = preferences.getLong(KEY_LAST_CLEANUP, 0L)
            val daysSinceCleanup = (System.currentTimeMillis() - lastCleanup) / (24 * 60 * 60 * 1000L)

            if (daysSinceCleanup >= CLEANUP_INTERVAL_DAYS) {
                // 使用专用的协程作用域执行后台清理任务
                cacheScope.launch {
                    try {
                        cleanExpiredSubtitles()
                        cleanupToSize()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error during background cleanup: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking cleanup: ${e.message}")
        }
    }
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val totalSubtitles: Int = 0,
    val downloadedSubtitles: Int = 0,
    val cacheSize: Long = 0L,
    val lastCleanup: Long = 0L
) {
    val cacheSizeMB: Float
        get() = cacheSize / (1024f * 1024f)
        
    val daysSinceCleanup: Long
        get() = if (lastCleanup > 0) {
            (System.currentTimeMillis() - lastCleanup) / (24 * 60 * 60 * 1000L)
        } else {
            -1L
        }
}