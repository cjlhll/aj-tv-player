package com.tvplayer.webdav.data.scanner

import android.util.Log
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.tmdb.TmdbClient
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import com.tvplayer.webdav.data.model.WebDAVFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        // 根据模式提示优化分类，减少误判
        return when (modeHint) {
            ModeHint.MOVIE -> tmdbClient.scrapeMovie(name, path, fileSize)
            ModeHint.TV -> {
                // 对于TV模式，始终使用目录名作为剧名来获取剧集封面
                val seriesName = file.path.trimEnd('/').substringBeforeLast('/').substringAfterLast('/')

                // 尝试从文件名提取季集信息
                val matcher = TV_SEASON_EPISODE_PATTERNS.firstOrNull { it.matcher(name).find() }?.matcher(name)
                val season = if (matcher != null && matcher.find()) matcher.group(1)?.toIntOrNull() else null
                val episode = if (matcher != null) matcher.group(2)?.toIntOrNull() else null

                tmdbClient.scrapeTVShow(seriesName, season, episode, path, fileSize)
            }
            null -> {
                val tvMatch = TV_SEASON_EPISODE_PATTERNS.firstOrNull { it.matcher(name).find() }
                if (tvMatch != null) {
                    val matcher = tvMatch.matcher(name)
                    if (matcher.find()) {
                        val season = matcher.group(1)?.toIntOrNull()
                        val episode = matcher.group(2)?.toIntOrNull()
                        val seriesName = name.substringBeforeLast('.')
                        tmdbClient.scrapeTVShow(seriesName, season, episode, path, fileSize)
                    } else null
                } else {
                    tmdbClient.scrapeMovie(name, path, fileSize)
                }
            }
        }
    }

    /**
     * 检查是否为视频文件
     */
    private fun isVideoFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in VIDEO_EXTENSIONS
    }
}
