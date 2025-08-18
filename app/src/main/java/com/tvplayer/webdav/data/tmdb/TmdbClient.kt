package com.tvplayer.webdav.data.tmdb

import android.util.Log
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TMDB客户端
 * 处理媒体信息刮削逻辑
 */
@Singleton
class TmdbClient @Inject constructor(
    private val apiService: TmdbApiService
) {
    companion object {
        private const val TAG = "TmdbClient"

        // ⚠️ 重要：请替换为你的TMDB API密钥
        // 1. 访问 https://www.themoviedb.org/
        // 2. 注册账户并申请API密钥
        // 3. 将下面的 "YOUR_TMDB_API_KEY" 替换为实际的API密钥
        private const val API_KEY = "e5ea1ff22ac53933400bc0251fff5943"

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    /**
     * 刮削电影信息
     * @param fileName 电影文件名
     * @param filePath 文件路径
     * @return 刮削的媒体信息
     */
    suspend fun scrapeMovie(fileName: String, filePath: String): MediaItem? {
        return withContext(Dispatchers.IO) {
            try {
                val year = extractYear(fileName)
                val candidates = generateMovieCandidates(fileName)
                Log.d(TAG, "Movie candidates: ${candidates.joinToString()} (year=$year)")

                for (candidate in candidates) {
                    // 先 zh-CN，若无结果再 en-US
                    val searchZh = apiService.searchMovies(
                        apiKey = API_KEY,
                        query = candidate,
                        year = year
                    )
                    var movies = if (searchZh.isSuccessful) searchZh.body()?.results else null
                    if (movies.isNullOrEmpty()) {
                        val searchEn = apiService.searchMovies(
                            apiKey = API_KEY,
                            query = candidate,
                            language = "en-US",
                            year = year
                        )
                        if (searchEn.isSuccessful) {
                            movies = searchEn.body()?.results
                        }
                    }
                    if (!movies.isNullOrEmpty()) {
                        val movie = movies.first()
                        val detailsResponse = apiService.getMovieDetails(movie.id, API_KEY)
                        val movieDetails = detailsResponse.body() ?: movie

                        // 确保中文标题/概述：若当前语言非中文，尝试读取 zh-CN 翻译数据
                        val finalized = ensureChineseMovie(movie.id, movieDetails)
                        Log.d(TAG, "Movie matched by candidate: $candidate")
                        return@withContext convertTmdbMovieToMediaItem(finalized, filePath)
                    }
                }

                Log.w(TAG, "No movie found for any candidate of: $fileName")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error scraping movie: $fileName", e)
                null
            }
        }
    }

    /**
     * 刮削电视剧信息
     * @param seriesName 电视剧名称（通常是目录名）
     * @param seasonNumber 季数
     * @param episodeNumber 集数
     * @param filePath 文件路径
     * @return 刮削的媒体信息
     */
    suspend fun scrapeTVShow(
        seriesName: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        filePath: String
    ): MediaItem? {
        return withContext(Dispatchers.IO) {
            try {
                val candidates = generateTVCandidates(seriesName)
                Log.d(TAG, "TV candidates: ${candidates.joinToString()} S${seasonNumber}E${episodeNumber}")

                var tvShows: List<TmdbTVShow>? = null
                var matchedCandidate: String? = null
                candidateLoop@ for (candidate in candidates) {
                    // 先 zh-CN，若无结果再 en-US
                    val searchZh = apiService.searchTVShows(
                        apiKey = API_KEY,
                        query = candidate
                    )
                    tvShows = if (searchZh.isSuccessful) searchZh.body()?.results else null
                    if (tvShows.isNullOrEmpty()) {
                        val searchEn = apiService.searchTVShows(
                            apiKey = API_KEY,
                            query = candidate,
                            language = "en-US"
                        )
                        if (searchEn.isSuccessful) {
                            tvShows = searchEn.body()?.results
                        }
                    }
                    if (!tvShows.isNullOrEmpty()) { matchedCandidate = candidate; break@candidateLoop }
                }

                if (!tvShows.isNullOrEmpty()) {
                    val tvShow = tvShows.first()

                    // 获取详细信息（保持 zh-CN），若缺中文再用 translations 兜底
                    val detailsResponse = apiService.getTVShowDetails(tvShow.id, API_KEY)
                    val tvDetails = ensureChineseTV(tvShow.id, detailsResponse.body() ?: tvShow)

                    // 如果有季和集信息，获取具体剧集信息
                    var episodeTitle: String? = null
                    var episodeOverview: String? = null
                    var episodeRuntime: Long = 0L

                    if (seasonNumber != null && episodeNumber != null) {
                        try {
                            val seasonResponse = apiService.getSeasonDetails(
                                tvShow.id, seasonNumber, API_KEY
                            )
                            val season = seasonResponse.body()
                            val episode = season?.episodes?.find { it.episodeNumber == episodeNumber }

                            episodeTitle = episode?.name
                            episodeOverview = episode?.overview
                            episodeRuntime = (episode?.runtime ?: 0) * 60L // 转换为秒
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to get episode details", e)
                        }
                    }

                    return@withContext convertTmdbTVToMediaItem(
                        tvDetails, filePath, seasonNumber, episodeNumber,
                        episodeTitle, episodeOverview, episodeRuntime
                    )
                }

                Log.w(TAG, "No TV show found for: $seriesName")
                null
            } catch (e: Exception) {
                Log.e(TAG, "Error scraping TV show: $seriesName", e)
                null
            }
        }
    }

    /**
     * 生成电影搜索候选：
     * 1) 清洗标题（去扩展名/噪声/分隔符）
     * 2) 基于 tokens 逐步组合（单词→前缀组合）
     */
    private fun generateMovieCandidates(fileName: String): List<String> {
        // 先清洗，再按“最长→逐步减词”生成候选，避免无意义请求
        val base = cleanMovieTitle(fileName)
        val tokens = base.split(Regex("[\\s._-]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return listOf(base)
        val candidates = mutableListOf<String>()
        for (i in tokens.size downTo 1) {
            candidates.add(tokens.subList(0, i).joinToString(" "))
        }
        return candidates.distinct().take(15)
    }

    /**
     * 生成电视剧搜索候选：基于目录名清洗 + token 前缀组合
     */
    private fun generateTVCandidates(seriesName: String): List<String> {
        // 先清洗（去 Season/Sxx 等），再按“最长→逐步减词”生成候选
        val base = cleanTVTitle(seriesName)
        val tokens = base.split(Regex("[\\s._-]+")).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return listOf(base)
        val candidates = mutableListOf<String>()
        for (i in tokens.size downTo 1) {
            candidates.add(tokens.subList(0, i).joinToString(" "))
        }
        return candidates.distinct().take(15)
    }

    private fun cleanMovieTitle(fileName: String): String {
        return fileName
            .substringBeforeLast('.') // 移除扩展名
            .replace(Regex("\\.(\\d{4})"), " $1") // 处理年份
            .replace(Regex("[\\[\\(].*?[\\]\\)]"), "") // 移除括号内容
            .replace(Regex("\\b(1080p|720p|480p|4K|UHD|HDR|BluRay|WEB-DL|WEBRip|DVDRip)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\b(x264|x265|H264|H265|HEVC)\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[._-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }


    private fun extractYear(fileName: String): Int? {
        // 提取 1900-2099 年份
        val match = Regex("(?:\\D|^)(19|20)\\d{2}(?:\\D|$)").find(fileName)
        return match?.value?.filter { it.isDigit() }?.takeLast(4)?.toIntOrNull()
    }

    /**
     * 清理电视剧标题
     */
    private fun cleanTVTitle(seriesName: String): String {
        return seriesName
            .replace(Regex("\\b(Season|S)\\s*\\d+\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[\\[\\(].*?[\\]\\)]"), "")
            .replace(Regex("[._-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * 转换TMDB电影数据为MediaItem
     */
    private fun convertTmdbMovieToMediaItem(movie: TmdbMovie, filePath: String): MediaItem {
        val releaseDate = try {
            movie.releaseDate?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }

        return MediaItem(
            id = "movie_${movie.id}",
            title = movie.title,
            originalTitle = movie.originalTitle,
            overview = movie.overview,
            posterPath = movie.posterPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.POSTER_SIZE_W500}$it" },
            backdropPath = movie.backdropPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.BACKDROP_SIZE_W1280}$it" },
            releaseDate = releaseDate,
            rating = movie.voteAverage,
            duration = (movie.runtime ?: 0) * 60L, // 转换为秒
            mediaType = MediaType.MOVIE,
            filePath = filePath,
            genre = movie.genres?.map { it.name } ?: emptyList()
        )
    }

    /**
     * 转换TMDB电视剧数据为MediaItem
     */
    private fun convertTmdbTVToMediaItem(
        tvShow: TmdbTVShow,
        filePath: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        episodeOverview: String?,
        episodeRuntime: Long
    ): MediaItem {
        val releaseDate = try {
            tvShow.firstAirDate?.let { dateFormat.parse(it) }
        } catch (e: Exception) {
            null
        }

        return MediaItem(
            id = "tv_${tvShow.id}_s${seasonNumber}_e${episodeNumber}",
            title = episodeTitle ?: "第${episodeNumber}集",
            originalTitle = tvShow.originalName,
            overview = episodeOverview ?: tvShow.overview,
            posterPath = tvShow.posterPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.POSTER_SIZE_W500}$it" },
            backdropPath = tvShow.backdropPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.BACKDROP_SIZE_W1280}$it" },
            releaseDate = releaseDate,
            rating = tvShow.voteAverage,
            duration = episodeRuntime,
            mediaType = MediaType.TV_EPISODE,
            filePath = filePath,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            seriesId = "tv_${tvShow.id}",
            seriesTitle = tvShow.name,
            genre = tvShow.genres?.map { it.name } ?: emptyList()
        )
    }

    /**
     * 确保返回中文（zh-CN）字段：title/name/overview
     * 若 getDetails 已经是 zh-CN，则直接返回；否则从 translations 中找 zh-CN 数据覆盖
     */
    private suspend fun ensureChineseMovie(movieId: Int, details: TmdbMovie): TmdbMovie {
        return withContext(Dispatchers.IO) {
            try {
                val tr = apiService.getMovieTranslations(movieId, API_KEY)
                val cn = tr.body()?.translations?.firstOrNull { it.iso_639_1?.equals("zh", true) == true }
                if (cn?.data != null) {
                    val zhTitle = cn.data.title
                    val zhOverview = cn.data.overview
                    details.copy(
                        title = if (!zhTitle.isNullOrBlank()) zhTitle else details.title,
                        overview = if (!zhOverview.isNullOrBlank()) zhOverview else details.overview
                    )
                } else details
            } catch (e: Exception) {
                Log.w(TAG, "ensureChineseMovie failed", e)
                details
            }
        }
    }

    private suspend fun ensureChineseTV(tvId: Int, details: TmdbTVShow): TmdbTVShow {
        return withContext(Dispatchers.IO) {
            try {
                val tr = apiService.getTVTranslations(tvId, API_KEY)
                val cn = tr.body()?.translations?.firstOrNull { it.iso_639_1?.equals("zh", true) == true }
                if (cn?.data != null) {
                    val zhName = cn.data.name
                    val zhOverview = cn.data.overview
                    details.copy(
                        name = if (!zhName.isNullOrBlank()) zhName else details.name,
                        overview = if (!zhOverview.isNullOrBlank()) zhOverview else details.overview
                    )
                } else details
            } catch (e: Exception) {
                Log.w(TAG, "ensureChineseTV failed", e)
                details
            }
        }
    }

    /**
     * 获取完整的图片URL
     */
    fun getFullImageUrl(imagePath: String?, size: String = TmdbApiService.POSTER_SIZE_W500): String? {
        return imagePath?.let { "${TmdbApiService.IMAGE_BASE_URL}$size$it" }
    }
}
