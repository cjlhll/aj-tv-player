package com.tvplayer.webdav.data.scanner

import android.util.Log
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.tmdb.TmdbApiService
import com.tvplayer.webdav.data.tmdb.TmdbClient
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import com.tvplayer.webdav.data.model.WebDAVFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 媒体扫描器
 * 智能扫描WebDAV目录，识别和刮削媒体文件
 */
@Singleton
class MediaScanner @Inject constructor(
    private val webdavClient: SimpleWebDAVClient,
    private val tmdbClient: TmdbClient
) {
    enum class ModeHint { MOVIE, TV }
    companion object {
        private const val TAG = "MediaScanner"

        // 支持的视频文件扩展名
        private val VIDEO_EXTENSIONS = setOf(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "3gp", "ts", "m2ts"
        )

        // 电视剧季集识别正则
        private val TV_SEASON_EPISODE_PATTERNS = listOf(
            Pattern.compile("S(\\d+)E(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Season\\s*(\\d+).*Episode\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+)x(\\d+)", Pattern.CASE_INSENSITIVE)
        )

        // 电视剧目录识别关键词
        private val TV_DIRECTORY_KEYWORDS = setOf(
            "season", "s01", "s02", "s03", "s04", "s05", "s06", "s07", "s08", "s09", "s10",
            "第一季", "第二季", "第三季", "第四季", "第五季"
        )
    }

    /**
     * 扫描进度回调
     */
    interface ScanProgressCallback {
        fun onProgress(current: Int, total: Int, currentFile: String)
        fun onComplete(scannedItems: List<MediaItem>)
        fun onError(error: String)
    }

    /**
     * 扫描WebDAV目录
     * @param path 要扫描的路径
     * @param recursive 是否递归扫描子目录
     * @param callback 进度回调
     */
    suspend fun scanDirectory(
        path: String,
        recursive: Boolean = true,
        callback: ScanProgressCallback? = null,
        modeHint: ModeHint? = null
    ): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            val scannedItems = mutableListOf<MediaItem>()

            try {
                Log.d(TAG, "Starting scan of directory: $path")

                val files: List<WebDAVFile> = if (recursive) {
                    getAllFilesRecursively(path)
                } else {
                    getFilesInDirectory(path)
                }

                val videoFiles = files.filter { !it.isDirectory && isVideoFile(it.name) }

                val total = videoFiles.size.coerceAtLeast(1)
                var current = 0

                for (file in videoFiles) {
                    current += 1
                    callback?.onProgress(current, total, file.path)

                    val mediaItem = scrapeMediaFile(file, modeHint)
                    if (mediaItem != null) {
                        scannedItems.add(mediaItem)
                    }
                }

                callback?.onComplete(scannedItems)
                Log.d(TAG, "Scan completed. Found ${scannedItems.size} media items")

            } catch (e: Exception) {
                Log.e(TAG, "Error during scan", e)
                callback?.onError(e.message ?: "Unknown error")
            }

            scannedItems
        }
    }

    /**
     * 递归获取所有文件
     */
    private suspend fun getAllFilesRecursively(path: String): List<WebDAVFile> {
        val result = mutableListOf<WebDAVFile>()
        val stack = ArrayDeque<String>()
        stack.add(path)
        while (stack.isNotEmpty()) {
            val currentPath = stack.removeFirst()
            val res = webdavClient.listFiles(currentPath)
            if (res.isSuccess) {
                val items = res.getOrNull().orEmpty()
                for (item in items) {
                    if (item.isDirectory) {
                        stack.add(item.path)
                    } else {
                        result.add(item)
                    }
                }
            }
        }
        return result
    }

    /**
     * 获取目录中的文件
     */
    private suspend fun getFilesInDirectory(path: String): List<WebDAVFile> {
        val res = webdavClient.listFiles(path)
        return if (res.isSuccess) res.getOrNull().orEmpty() else emptyList()
    }

    /**
     * 刮削单个媒体文件
     */
    private suspend fun scrapeMediaFile(file: WebDAVFile, modeHint: ModeHint? = null): MediaItem? {
        val name = file.name
        val path = file.path
        val fileSize = file.size

        return try {
            // 确定媒体类型
            val mediaType = when (modeHint) {
                ModeHint.MOVIE -> com.tvplayer.webdav.data.model.MediaType.MOVIE
                ModeHint.TV -> com.tvplayer.webdav.data.model.MediaType.TV_EPISODE
                null -> {
                    // 简单判断：如果文件名包含S\d+E\d+模式，则认为是电视剧
                    if (TV_SEASON_EPISODE_PATTERNS.any { it.matcher(name).find() }) {
                        com.tvplayer.webdav.data.model.MediaType.TV_EPISODE
                    } else {
                        com.tvplayer.webdav.data.model.MediaType.MOVIE
                    }
                }
            }

            // 尝试从TMDB获取元数据
            val scrapedItem = when (mediaType) {
                com.tvplayer.webdav.data.model.MediaType.MOVIE -> {
                    Log.d(TAG, "Attempting to scrape movie: $name")
                    val result = tmdbClient.scrapeMovie(name, path, fileSize)
                    if (result != null) {
                        Log.d(TAG, "Successfully scraped movie: ${result.title} with poster: ${result.posterPath}")
                    } else {
                        Log.w(TAG, "Failed to scrape movie: $name")
                    }
                    result
                }
                com.tvplayer.webdav.data.model.MediaType.TV_EPISODE -> {
                    // 解析电视剧信息
                    val (seasonNumber, episodeNumber) = parseSeasonEpisode(name)
                    val seriesName = extractSeriesName(name, path)
                    Log.d(TAG, "Attempting to scrape TV show: $seriesName S${seasonNumber}E${episodeNumber}")
                    val result = tmdbClient.scrapeTVShow(seriesName, seasonNumber, episodeNumber, path, fileSize)
                    if (result != null) {
                        Log.d(TAG, "Successfully scraped TV show: ${result.title} with poster: ${result.posterPath}")
                    } else {
                        Log.w(TAG, "Failed to scrape TV show: $seriesName")
                    }
                    result
                }
                else -> null
            }

            // 如果TMDB刮削失败，创建基本的MediaItem
            scrapedItem ?: createBasicMediaItem(name, path, fileSize, mediaType)

        } catch (e: Exception) {
            Log.e(TAG, "Error scraping media file: $path", e)
            // 发生错误时创建基本的MediaItem
            createBasicMediaItem(name, path, fileSize, com.tvplayer.webdav.data.model.MediaType.MOVIE)
        }
    }

    /**
     * 检查是否为视频文件
     */
    private fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in VIDEO_EXTENSIONS
    }

    /**
     * 解析季集信息
     */
    private fun parseSeasonEpisode(fileName: String): Pair<Int?, Int?> {
        for (pattern in TV_SEASON_EPISODE_PATTERNS) {
            val matcher = pattern.matcher(fileName)
            if (matcher.find()) {
                val season = matcher.group(1)?.toIntOrNull()
                val episode = matcher.group(2)?.toIntOrNull()
                return Pair(season, episode)
            }
        }
        return Pair(null, null)
    }

    /**
     * 提取电视剧系列名称
     */
    private fun extractSeriesName(fileName: String, filePath: String): String {
        // 尝试从路径中提取系列名称（通常是父目录名）
        val pathParts = filePath.split("/")
        if (pathParts.size >= 2) {
            val parentDir = pathParts[pathParts.size - 2]
            // 如果父目录包含季信息，再往上一级
            if (TV_DIRECTORY_KEYWORDS.any { parentDir.lowercase().contains(it) }) {
                if (pathParts.size >= 3) {
                    return cleanSeriesName(pathParts[pathParts.size - 3])
                }
            } else {
                return cleanSeriesName(parentDir)
            }
        }

        // 如果无法从路径提取，从文件名提取
        return cleanSeriesName(fileName.substringBeforeLast('.'))
    }

    /**
     * 清理系列名称
     */
    private fun cleanSeriesName(name: String): String {
        return name
            .replace(Regex("[\\[\\(（【].*?[\\]\\)）】]"), " ") // 移除括号内容
            .replace(Regex("S\\d+E\\d+", RegexOption.IGNORE_CASE), " ") // 移除季集信息
            .replace(Regex("[._-]+"), " ") // 替换分隔符为空格
            .replace(Regex("\\s+"), " ") // 合并多个空格
            .trim()
    }

    /**
     * 创建基本的MediaItem（当TMDB刮削失败时使用）
     */
    private fun createBasicMediaItem(
        fileName: String,
        filePath: String,
        fileSize: Long,
        mediaType: com.tvplayer.webdav.data.model.MediaType
    ): MediaItem {
        val (seasonNumber, episodeNumber) = if (mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE) {
            parseSeasonEpisode(fileName)
        } else {
            Pair(null, null)
        }

        return MediaItem(
            id = filePath.hashCode().toString(),
            title = fileName.substringBeforeLast('.'),
            originalTitle = null,
            overview = null,
            posterPath = null,
            backdropPath = null,
            releaseDate = null,
            rating = 0f,
            duration = 0L,
            mediaType = mediaType,
            filePath = filePath,
            fileSize = fileSize,
            lastModified = null,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            seriesTitle = if (mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE) {
                extractSeriesName(fileName, filePath)
            } else null
        )
    }
}
