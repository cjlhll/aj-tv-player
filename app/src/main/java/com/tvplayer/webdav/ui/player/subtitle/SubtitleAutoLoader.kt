package com.tvplayer.webdav.ui.player.subtitle

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.tvplayer.webdav.data.subtitle.assrt.AssrtApiClient
import com.tvplayer.webdav.data.subtitle.assrt.AssrtUtils

/**
 * 自动字幕搜索与下载器（ASSRT）
 */
class SubtitleAutoLoader(
    private val context: Context,
    private val token: String,
    private val scope: CoroutineScope,
) {
    private val TAG = "SubtitleAutoLoader"
    private var job: Job? = null

    data class Result(
        val file: File,
        val mimeType: String,
        val displayName: String,
    )

    fun start(videoUri: Uri, displayTitle: String, callback: (Result?) -> Unit) {
        cancel()
        job = scope.launch(Dispatchers.IO) {
            try {
                // 尝试多种搜索策略
                val searchResult = tryMultipleSearchStrategies(videoUri, displayTitle)
                if (searchResult == null) {
                    Log.i(TAG, "No subtitle found after trying all strategies")
                    return@launch withContext(Dispatchers.Main) { callback(null) }
                }

                val mime = AssrtUtils.inferMimeTypeByExt(searchResult.name)
                withContext(Dispatchers.Main) {
                    callback(Result(searchResult, mime, searchResult.name))
                }
            } catch (e: Exception) {
                Log.e(TAG, "auto load error", e)
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }

    private suspend fun tryMultipleSearchStrategies(videoUri: Uri, displayTitle: String): File? {
        val api = AssrtApiClient(token)
        
        // 策略1: 使用清理后的文件名
        val cleanQuery = buildQuery(videoUri, displayTitle)
        Log.d(TAG, "Trying strategy 1 with query: $cleanQuery")
        val result1 = searchAndDownload(api, cleanQuery)
        if (result1 != null) return result1
        
        // 策略2: 使用原始文件名（不清理）
        val rawQuery = videoUri.lastPathSegment?.substringBeforeLast('.') ?: displayTitle
        if (rawQuery != cleanQuery) {
            Log.d(TAG, "Trying strategy 2 with query: $rawQuery")
            val result2 = searchAndDownload(api, rawQuery)
            if (result2 != null) return result2
        }
        
        // 策略3: 使用标题
        if (displayTitle != cleanQuery && displayTitle != rawQuery) {
            Log.d(TAG, "Trying strategy 3 with query: $displayTitle")
            val result3 = searchAndDownload(api, displayTitle)
            if (result3 != null) return result3
        }
        
        // 策略4: 提取可能的电影/剧集名称（去掉年份、季集信息等）
        val extractedName = extractMainTitle(cleanQuery)
        if (extractedName != cleanQuery) {
            Log.d(TAG, "Trying strategy 4 with query: $extractedName")
            val result4 = searchAndDownload(api, extractedName)
            if (result4 != null) return result4
        }
        
        return null
    }

    private suspend fun searchAndDownload(api: AssrtApiClient, query: String): File? {
        if (query.isBlank()) return null
        
        try {
            val searchRes = api.search(query, isFileQuery = true, maxCount = 15).getOrElse {
                Log.w(TAG, "Search failed for '$query': ${it.message}")
                return null
            }
            
            if (searchRes.isEmpty()) {
                Log.i(TAG, "No results found for: $query")
                return null
            }
            
            // 尝试前几个结果
            for (item in searchRes.take(3)) {
                try {
                    val detail = api.detail(item.id).getOrNull()
                    if (detail == null) {
                        Log.w(TAG, "Detail failed for id ${item.id}")
                        // 继续下一个
                    } else {
                        val chosen = AssrtUtils.chooseBestSubtitleFile(detail.filelist)
                        if (chosen == null) {
                            Log.i(TAG, "No usable file in package, id=${detail.id}")
                            // 继续下一个
                        } else {
                            val dest = buildSubtitleCacheFile(chosen.name)
                            val downloaded = api.downloadFile(chosen.url, dest).getOrNull()
                            if (downloaded == null) {
                                Log.w(TAG, "Download failed for ${chosen.name}")
                                // 继续下一个
                            } else {
                                Log.i(TAG, "Successfully downloaded subtitle: ${chosen.name}")
                                return downloaded
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing subtitle id ${item.id}", e)
                    // 继续下一个
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in searchAndDownload for '$query'", e)
        }
        
        return null
    }

    private fun extractMainTitle(title: String): String {
        // 移除年份、季节信息等，提取主要标题
        return title
            .replace(Regex("\\b\\d{4}\\b"), "") // 移除年份
            .replace(Regex("\\b(?:Season|S)\\s*\\d+\\b", RegexOption.IGNORE_CASE), "") // 移除季节信息
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun buildQuery(videoUri: Uri, title: String): String {
        // 优先使用最后的文件名（不带扩展名），否则回退到标题
        val last = videoUri.lastPathSegment ?: title
        val name = last.substringAfterLast('/')
        val noExt = name.substringBeforeLast('.', name)
        
        // 清理文件名：移除常见的视频文件标识符和分隔符
        val cleaned = noExt
            .replace(Regex("[\\[\\](){}]"), "") // 移除括号
            .replace(Regex("[-_.]+"), " ") // 替换连字符、下划线、点为空格
            .replace(Regex("\\b(?:1080p|720p|480p|4K|HD|BluRay|WEB-DL|BDRip|DVDRip|x264|x265|HEVC|AAC|AC3)\\b", RegexOption.IGNORE_CASE), "") // 移除视频质量标识
            .replace(Regex("\\b(?:S\\d+E\\d+|第\\d+集|EP\\d+)\\b", RegexOption.IGNORE_CASE), "") // 移除剧集标识
            .replace(Regex("\\s+"), " ") // 合并多个空格
            .trim()
            
        return cleaned.ifEmpty { title }
    }

    private fun buildSubtitleCacheFile(filename: String): File {
        val dir = File(context.cacheDir, "subtitles")
        if (!dir.exists()) dir.mkdirs()
        // 简单避免重名
        val safe = filename.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(dir, safe)
    }
}

