package com.tvplayer.webdav.data.subtitle

import android.util.Log
import com.tvplayer.webdav.data.model.*
import kotlinx.coroutines.delay
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OpenSubtitles API服务实现
 * 使用OpenSubtitles REST API v1
 */
@Singleton
class OpenSubtitlesService @Inject constructor(
    private val httpClient: OkHttpClient
) : SubtitleSearchService {
    
    companion object {
        private const val TAG = "OpenSubtitlesService"
        private const val BASE_URL = "https://api.opensubtitles.com/api/v1"
        private const val USER_AGENT = "AndroidTVPlayer v1.0"
        
        // API端点
        private const val ENDPOINT_LOGIN = "/login"
        private const val ENDPOINT_SEARCH = "/subtitles"
        private const val ENDPOINT_DOWNLOAD = "/download"
        
        // 语言映射
        private val LANGUAGE_MAP = mapOf(
            "zh" to "zh-cn",
            "zh-cn" to "zh-cn",
            "zh-tw" to "zh-tw",
            "en" to "en",
            "en-us" to "en",
            "ja" to "ja",
            "ko" to "ko",
            "fr" to "fr",
            "de" to "de",
            "es" to "es",
            "ru" to "ru"
        )
    }
    
    override val supportedSource = SubtitleSource.OPENSUBTITLES
    
    private var authToken: String? = null
    private var tokenExpiresAt: Long = 0L
    private var apiLimits = ApiLimitInfo()
    
    private val client = httpClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    
    override suspend fun isAvailable(): Boolean {
        return try {
            ensureAuthenticated()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Service not available: ${e.message}")
            false
        }
    }
    
    override suspend fun searchSubtitles(request: SubtitleSearchRequest): Result<List<Subtitle>> {
        return try {
            Log.d(TAG, "Searching subtitles for: ${request.title}")
            
            ensureAuthenticated()
            
            val searchParams = buildSearchParams(request)
            val searchUrl = "$BASE_URL$ENDPOINT_SEARCH?$searchParams"
            
            val httpRequest = Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build()
            
            val response = client.newCall(httpRequest).execute()
            updateApiLimits(response)
            
            if (!response.isSuccessful) {
                throw IOException("Search failed: ${response.code} ${response.message}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val subtitles = parseSearchResponse(responseBody)
            
            Log.d(TAG, "Found ${subtitles.size} subtitles for ${request.title}")
            Result.success(subtitles)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error searching subtitles", e)
            Result.failure(e)
        }
    }
    
    override suspend fun downloadSubtitle(subtitle: Subtitle, localPath: String): Result<String> {
        return try {
            Log.d(TAG, "Downloading subtitle: ${subtitle.id}")
            
            ensureAuthenticated()
            
            // 构建下载请求
            val downloadRequest = JSONObject().apply {
                put("file_id", subtitle.id)
                put("sub_format", "srt") // 统一下载为SRT格式
            }
            
            val requestBody = downloadRequest.toString()
                .toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url("$BASE_URL$ENDPOINT_DOWNLOAD")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(httpRequest).execute()
            updateApiLimits(response)
            
            if (!response.isSuccessful) {
                throw IOException("Download failed: ${response.code} ${response.message}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val downloadInfo = parseDownloadResponse(responseBody)
            
            // 下载实际的字幕文件
            val actualPath = downloadSubtitleFile(downloadInfo.link, localPath)
            
            Log.d(TAG, "Subtitle downloaded to: $actualPath")
            Result.success(actualPath)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading subtitle", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getSubtitleDetails(subtitleId: String): Result<Subtitle> {
        return try {
            ensureAuthenticated()
            
            val detailsUrl = "$BASE_URL$ENDPOINT_SEARCH?id=$subtitleId"
            
            val httpRequest = Request.Builder()
                .url(detailsUrl)
                .addHeader("Authorization", "Bearer $authToken")
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .build()
            
            val response = client.newCall(httpRequest).execute()
            updateApiLimits(response)
            
            if (!response.isSuccessful) {
                throw IOException("Get details failed: ${response.code} ${response.message}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val subtitles = parseSearchResponse(responseBody)
            
            if (subtitles.isNotEmpty()) {
                Result.success(subtitles.first())
            } else {
                Result.failure(Exception("Subtitle not found"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subtitle details", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getApiLimits(): ApiLimitInfo {
        return apiLimits
    }
    
    /**
     * 确保已经认证
     */
    private suspend fun ensureAuthenticated() {
        if (authToken == null || System.currentTimeMillis() > tokenExpiresAt) {
            authenticate()
        }
    }
    
    /**
     * 进行认证（匿名登录）
     */
    private suspend fun authenticate() {
        try {
            Log.d(TAG, "Authenticating with OpenSubtitles...")
            
            val loginRequest = JSONObject().apply {
                put("username", "")
                put("password", "")
            }
            
            val requestBody = loginRequest.toString()
                .toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url("$BASE_URL$ENDPOINT_LOGIN")
                .post(requestBody)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(httpRequest).execute()
            
            if (!response.isSuccessful) {
                throw IOException("Authentication failed: ${response.code} ${response.message}")
            }
            
            val responseBody = response.body?.string() ?: ""
            val loginResponse = JSONObject(responseBody)
            
            authToken = loginResponse.optString("token")
            
            if (authToken.isNullOrEmpty()) {
                throw IOException("No auth token received")
            }
            
            // 设置token过期时间（通常24小时）
            tokenExpiresAt = System.currentTimeMillis() + (23 * 60 * 60 * 1000L)
            
            Log.d(TAG, "Authentication successful")
            
        } catch (e: Exception) {
            Log.e(TAG, "Authentication failed", e)
            throw e
        }
    }
    
    /**
     * 构建搜索参数
     */
    private fun buildSearchParams(request: SubtitleSearchRequest): String {
        val params = mutableListOf<String>()
        
        // 基本搜索参数
        if (request.title.isNotEmpty()) {
            params.add("query=${URLEncoder.encode(request.title, "UTF-8")}")
        }
        
        if (request.imdbId.isNotEmpty()) {
            params.add("imdb_id=${request.imdbId}")
        }
        
        if (request.tmdbId > 0) {
            params.add("tmdb_id=${request.tmdbId}")
        }
        
        if (request.year > 0) {
            params.add("year=${request.year}")
        }
        
        // 剧集参数
        if (request.isTVShow()) {
            params.add("type=episode")
            if (request.seasonNumber > 0) {
                params.add("season_number=${request.seasonNumber}")
            }
            if (request.episodeNumber > 0) {
                params.add("episode_number=${request.episodeNumber}")
            }
        } else {
            params.add("type=movie")
        }
        
        // 语言参数
        val languages = request.languages.mapNotNull { LANGUAGE_MAP[it] }.distinct()
        if (languages.isNotEmpty()) {
            params.add("languages=${languages.joinToString(",")}")
        }
        
        // 其他参数
        params.add("order_by=download_count")
        params.add("order_direction=desc")
        
        if (request.maxResults > 0) {
            params.add("limit=${request.maxResults}")
        }
        
        if (request.onlyHD) {
            params.add("moviehash_match=only")
        }
        
        return params.joinToString("&")
    }
    
    /**
     * 解析搜索响应
     */
    private fun parseSearchResponse(responseBody: String): List<Subtitle> {
        return try {
            val jsonResponse = JSONObject(responseBody)
            val dataArray = jsonResponse.optJSONArray("data") ?: JSONArray()
            
            val subtitles = mutableListOf<Subtitle>()
            
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val attributes = item.optJSONObject("attributes") ?: continue
                
                val subtitle = parseSubtitleFromJson(item.optString("id"), attributes)
                subtitles.add(subtitle)
            }
            
            subtitles
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing search response", e)
            emptyList()
        }
    }
    
    /**
     * 从JSON解析字幕信息
     */
    private fun parseSubtitleFromJson(id: String, attributes: JSONObject): Subtitle {
        val language = attributes.optString("language", "")
        val files = attributes.optJSONArray("files")
        val file = files?.optJSONObject(0)
        
        return Subtitle(
            id = id,
            title = attributes.optString("release", ""),
            language = language,
            languageName = getLanguageDisplayName(language),
            format = SubtitleFormat.SRT, // OpenSubtitles主要提供SRT
            downloadUrl = file?.optString("link", "") ?: "",
            fileSize = file?.optLong("file_size", 0L) ?: 0L,
            source = SubtitleSource.OPENSUBTITLES,
            rating = attributes.optDouble("ratings", 0.0).toFloat(),
            downloadCount = attributes.optInt("download_count", 0),
            uploadDate = parseUploadDate(attributes.optString("upload_date", "")),
            uploader = attributes.optString("uploader", ""),
            metadata = mapOf(
                "release" to attributes.optString("release", ""),
                "comments" to attributes.optString("comments", ""),
                "hearing_impaired" to attributes.optBoolean("hearing_impaired", false).toString()
            )
        )
    }
    
    /**
     * 解析下载响应
     */
    private fun parseDownloadResponse(responseBody: String): DownloadInfo {
        val jsonResponse = JSONObject(responseBody)
        return DownloadInfo(
            link = jsonResponse.optString("link", ""),
            fileName = jsonResponse.optString("file_name", ""),
            requests = jsonResponse.optInt("requests", 0),
            remaining = jsonResponse.optInt("remaining", 0)
        )
    }
    
    /**
     * 下载字幕文件
     */
    private suspend fun downloadSubtitleFile(downloadUrl: String, localPath: String): String {
        val httpRequest = Request.Builder()
            .url(downloadUrl)
            .addHeader("User-Agent", USER_AGENT)
            .build()
        
        val response = client.newCall(httpRequest).execute()
        
        if (!response.isSuccessful) {
            throw IOException("File download failed: ${response.code}")
        }
        
        val file = File(localPath)
        file.parentFile?.mkdirs()
        
        response.body?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return localPath
    }
    
    /**
     * 更新API限制信息
     */
    private fun updateApiLimits(response: Response) {
        try {
            val requests = response.headers["X-RateLimit-Requests-InTimeWindow"]?.toIntOrNull() ?: 0
            val remaining = response.headers["X-RateLimit-Requests-Remaining"]?.toIntOrNull() ?: 200
            val reset = response.headers["X-RateLimit-Reset"]?.toLongOrNull() ?: 0L
            
            apiLimits = ApiLimitInfo(
                requestsPerDay = 200,
                requestsUsed = requests,
                remainingRequests = remaining,
                resetTime = reset,
                isLimited = remaining <= 0
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update API limits: ${e.message}")
        }
    }
    
    /**
     * 获取语言显示名称
     */
    private fun getLanguageDisplayName(language: String): String {
        return when (language) {
            "zh-cn" -> "简体中文"
            "zh-tw" -> "繁体中文"
            "en" -> "English"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "es" -> "Español"
            "ru" -> "Русский"
            else -> language.uppercase()
        }
    }
    
    /**
     * 解析上传日期
     */
    private fun parseUploadDate(dateString: String): Long {
        return try {
            // 简单的日期解析，可以根据实际格式调整
            if (dateString.isNotEmpty()) {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                    .parse(dateString)?.time ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 下载信息数据类
     */
    private data class DownloadInfo(
        val link: String,
        val fileName: String,
        val requests: Int,
        val remaining: Int
    )
}