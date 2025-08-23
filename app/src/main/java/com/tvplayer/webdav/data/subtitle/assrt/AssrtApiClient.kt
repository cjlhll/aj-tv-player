package com.tvplayer.webdav.data.subtitle.assrt

import android.net.Uri
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ASSRT(射手网伪) 字幕 API 客户端
 * 文档: https://assrt.net/api/doc
 */
class AssrtApiClient(
    private val token: String,
) {
    private val TAG = "AssrtApiClient"

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    data class SearchItem(
        val id: Long,
        val nativeName: String?,
        val videoName: String?,
        val subtype: String?,
        val langDesc: String?,
    )

    data class FileEntry(
        val url: String,
        val name: String,
        val sizeText: String?,
    )

    data class Detail(
        val id: Long,
        val title: String?,
        val url: String?, // 压缩包下载地址（一般不直接用）
        val filelist: List<FileEntry>,
    )

    suspend fun search(q: String, isFileQuery: Boolean = true, maxCount: Int = 10): Result<List<SearchItem>> {
        return try {
            val base = "https://api.assrt.net/v1/sub/search"
            val url = buildString {
                append(base)
                append("?token=").append(token)
                append("&q=").append(Uri.encode(q))
                append("&cnt=").append(maxCount)
                if (isFileQuery) {
                    append("&no_muxer=1") // 同时会视为 is_file=1
                }
            }
            val req = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                ensureSuccess(resp)
                val bodyStr = resp.body?.string() ?: throw IOException("empty body")
                val json = JSONObject(bodyStr)
                if (json.optInt("status", -1) != 0) throw IOException("ASSRT search status!=0: $bodyStr")
                val sub = json.optJSONObject("sub")
                val subs = sub?.optJSONArray("subs")
                val list = mutableListOf<SearchItem>()
                if (subs != null) {
                    for (i in 0 until subs.length()) {
                        val it = subs.optJSONObject(i)
                        val id = it?.optLong("id") ?: continue
                        val nativeName = it.optString("native_name", null)
                        val videoName = it.optString("videoname", null)
                        val subtype = it.optString("subtype", null)
                        val lang = it.optJSONObject("lang")?.optString("desc", null)
                        list.add(SearchItem(id, nativeName, videoName, subtype, lang))
                    }
                }
                Result.success(list)
            }
        } catch (e: Exception) {
            Log.e(TAG, "search error", e)
            Result.failure(e)
        }
    }

    suspend fun detail(id: Long): Result<Detail> {
        return try {
            val base = "https://api.assrt.net/v1/sub/detail"
            val url = "$base?token=$token&id=$id"
            val req = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build()
            client.newCall(req).execute().use { resp ->
                ensureSuccess(resp)
                val bodyStr = resp.body?.string() ?: throw IOException("empty body")
                val json = JSONObject(bodyStr)
                if (json.optInt("status", -1) != 0) throw IOException("ASSRT detail status!=0: $bodyStr")
                val sub = json.optJSONObject("sub")
                val subs = sub?.optJSONArray("subs")
                if (subs == null || subs.length() == 0) throw IOException("no detail items")
                val it = subs.getJSONObject(0)
                val filelistArr = it.optJSONArray("filelist")
                val files = mutableListOf<FileEntry>()
                if (filelistArr != null) {
                    for (i in 0 until filelistArr.length()) {
                        val f = filelistArr.optJSONObject(i)
                        val urlF = f?.optString("url") ?: continue
                        val name = f.optString("f")
                        val size = f.optString("s", null)
                        files.add(FileEntry(urlF, name, size))
                    }
                }
                Result.success(
                    Detail(
                        id = it.optLong("id"),
                        title = it.optString("title", null),
                        url = it.optString("url", null),
                        filelist = files,
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "detail error", e)
            Result.failure(e)
        }
    }

    suspend fun downloadFile(url: String, destFile: File): Result<File> {
        return try {
            destFile.parentFile?.mkdirs()
            val req = Request.Builder()
                .url(url)
                .header("Accept", "*/*")
                .build()
            client.newCall(req).execute().use { resp ->
                ensureSuccess(resp)
                val body = resp.body ?: throw IOException("empty body")
                FileOutputStream(destFile).use { out ->
                    body.byteStream().use { input ->
                        input.copyTo(out)
                    }
                }
            }
            Result.success(destFile)
        } catch (e: Exception) {
            Log.e(TAG, "download error", e)
            Result.failure(e)
        }
    }

    private fun ensureSuccess(resp: Response) {
        if (!resp.isSuccessful) throw IOException("http ${resp.code} ${resp.message}")
    }
}

object AssrtUtils {
    fun chooseBestSubtitleFile(files: List<AssrtApiClient.FileEntry>): AssrtApiClient.FileEntry? {
        if (files.isEmpty()) return null
        
        val candidates = files.filter { f ->
            val lower = f.name.lowercase()
            lower.endsWith(".srt") || lower.endsWith(".ass") || lower.endsWith(".ssa") || lower.endsWith(".vtt")
        }
        if (candidates.isEmpty()) return null
        
        // 优先级排序：
        // 1. 中文字幕优先
        // 2. 文件格式优先级 (srt > ass > vtt > ssa)
        // 3. 文件大小适中（避免过小或过大的文件）
        // 4. 文件名长度（通常更详细的命名更准确）
        return candidates.sortedWith(compareBy(
            { !containsZhHint(it.name) }, // 中文字幕优先
            { !isPreferredQuality(it.name) }, // 优质字幕优先
            { extPriority(it.name) }, // 格式优先级
            { -getFileSizeScore(it.sizeText) }, // 文件大小评分（降序）
            { it.name.length } // 文件名长度
        )).firstOrNull()
    }

    private fun containsZhHint(name: String): Boolean {
        val n = name.lowercase()
        return listOf(
            "zh", "chs", "cht", "ch", "cn", 
            "简体", "简中", "繁体", "繁中", "中文", 
            "chinese", "mandarin", "cantonese"
        ).any { n.contains(it) }
    }

    private fun isPreferredQuality(name: String): Boolean {
        val n = name.lowercase()
        // 优先选择高质量字幕标识
        return listOf("blu-ray", "bluray", "web-dl", "hdrip", "1080p", "720p").any { n.contains(it) }
    }

    private fun extPriority(name: String): Int {
        val n = name.lowercase()
        return when {
            n.endsWith(".srt") -> 0  // SRT格式最通用
            n.endsWith(".ass") -> 1  // ASS格式支持样式
            n.endsWith(".vtt") -> 2  // WebVTT格式
            n.endsWith(".ssa") -> 3  // SSA格式较老
            else -> 4
        }
    }

    private fun getFileSizeScore(sizeText: String?): Int {
        if (sizeText.isNullOrBlank()) return 50 // 默认分数
        
        return try {
            val sizeNum = sizeText.replace(Regex("[^\\d.]"), "").toFloatOrNull() ?: return 50
            val unit = sizeText.lowercase()
            
            val sizeInKB = when {
                unit.contains("mb") -> sizeNum * 1024
                unit.contains("gb") -> sizeNum * 1024 * 1024
                unit.contains("kb") -> sizeNum
                unit.contains("b") && !unit.contains("kb") && !unit.contains("mb") -> sizeNum / 1024
                else -> sizeNum
            }
            
            // 理想的字幕文件大小在50KB-500KB之间
            when {
                sizeInKB < 10 -> 10 // 太小，可能不完整
                sizeInKB in 10f..50f -> 60
                sizeInKB in 50f..200f -> 100 // 理想大小
                sizeInKB in 200f..500f -> 80
                sizeInKB in 500f..1000f -> 60
                else -> 30 // 太大，可能包含多语言或其他内容
            }
        } catch (e: Exception) {
            50 // 解析失败，返回默认分数
        }
    }

    fun inferMimeTypeByExt(name: String): String {
        val n = name.lowercase()
        return when {
            n.endsWith(".srt") -> androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
            n.endsWith(".vtt") -> androidx.media3.common.MimeTypes.TEXT_VTT
            n.endsWith(".ass") || n.endsWith(".ssa") -> "text/x-ssa"
            else -> androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
        }
    }
}

