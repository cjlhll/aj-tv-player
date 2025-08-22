package com.tvplayer.webdav.data.subtitle

import com.tvplayer.webdav.data.model.Subtitle
import com.tvplayer.webdav.data.model.SubtitleSearchRequest
import com.tvplayer.webdav.data.model.SubtitleSource

/**
 * 字幕搜索服务接口
 */
interface SubtitleSearchService {
    
    /**
     * 获取支持的字幕源
     */
    val supportedSource: SubtitleSource
    
    /**
     * 检查服务是否可用
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * 搜索字幕
     * @param request 搜索请求
     * @return 字幕列表
     */
    suspend fun searchSubtitles(request: SubtitleSearchRequest): Result<List<Subtitle>>
    
    /**
     * 下载字幕文件
     * @param subtitle 字幕信息
     * @param localPath 本地保存路径
     * @return 下载结果
     */
    suspend fun downloadSubtitle(subtitle: Subtitle, localPath: String): Result<String>
    
    /**
     * 获取字幕详细信息
     * @param subtitleId 字幕ID
     * @return 字幕详细信息
     */
    suspend fun getSubtitleDetails(subtitleId: String): Result<Subtitle>
    
    /**
     * 检查API限制状态
     * @return API限制信息
     */
    suspend fun getApiLimits(): ApiLimitInfo
}

/**
 * API限制信息
 */
data class ApiLimitInfo(
    val requestsPerDay: Int = 200, // 每日请求限制
    val requestsUsed: Int = 0, // 已使用请求数
    val remainingRequests: Int = 200, // 剩余请求数
    val resetTime: Long = 0L, // 重置时间戳
    val isLimited: Boolean = false // 是否受限
) {
    val canMakeRequest: Boolean
        get() = !isLimited && remainingRequests > 0
}

/**
 * 搜索结果
 */
data class SubtitleSearchResult(
    val subtitles: List<Subtitle> = emptyList(),
    val totalCount: Int = 0,
    val searchTime: Long = 0L, // 搜索耗时（毫秒）
    val source: SubtitleSource = SubtitleSource.UNKNOWN,
    val errors: List<String> = emptyList()
) {
    val isSuccess: Boolean
        get() = errors.isEmpty()
        
    val hasResults: Boolean
        get() = subtitles.isNotEmpty()
}