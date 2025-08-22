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
     * @param fileSize 文件大小
     * @return 刮削的媒体信息
     */
    suspend fun scrapeMovie(fileName: String, filePath: String, fileSize: Long): MediaItem? {
        return withContext(Dispatchers.IO) {
            try {
                val year = extractYear(fileName)
                val candidates = generateMovieCandidates(fileName)
                Log.d(TAG, "Movie candidates: ${candidates.joinToString()} (year=$year)")

                for (candidate in candidates) {
                    // 先 zh-CN，若无结果再 en-US
                    Log.d(TAG, "Searching for movie candidate: $candidate (year=$year)")
                    val searchZh = apiService.searchMovies(
                        apiKey = API_KEY,
                        query = candidate,
                        year = year
                    )
                    var movies = if (searchZh.isSuccessful) {
                        Log.d(TAG, "Chinese search successful for: $candidate")
                        searchZh.body()?.results
                    } else {
                        Log.w(TAG, "Chinese search failed for: $candidate, code: ${searchZh.code()}")
                        null
                    }

                    if (movies.isNullOrEmpty()) {
                        Log.d(TAG, "Trying English search for: $candidate")
                        val searchEn = apiService.searchMovies(
                            apiKey = API_KEY,
                            query = candidate,
                            language = "en-US",
                            year = year
                        )
                        if (searchEn.isSuccessful) {
                            Log.d(TAG, "English search successful for: $candidate")
                            movies = searchEn.body()?.results
                        } else {
                            Log.w(TAG, "English search failed for: $candidate, code: ${searchEn.code()}")
                        }
                    }
                    if (!movies.isNullOrEmpty()) {
                        val movie = movies.first()
                        val detailsResponse = apiService.getMovieDetails(movie.id, API_KEY)
                        val movieDetails = detailsResponse.body() ?: movie

                        // 确保中文标题/概述：若当前语言非中文，尝试读取 zh-CN 翻译数据
                        val finalized = ensureChineseMovie(movie.id, movieDetails)
                        Log.d(TAG, "Movie matched by candidate: $candidate")
                        return@withContext convertTmdbMovieToMediaItem(finalized, filePath, fileSize)
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
     * @param fileSize 文件大小
     * @return 刮削的媒体信息
     */
    suspend fun scrapeTVShow(
        seriesName: String,
        seasonNumber: Int?,
        episodeNumber: Int?,
        filePath: String,
        fileSize: Long
    ): MediaItem? {
        return withContext(Dispatchers.IO) {
            try {
                val yearExtractionResult = extractTVYearAndTitle(seriesName)
                val cleanTitle = yearExtractionResult.first
                val year = yearExtractionResult.second
                val candidates = generateTVCandidates(cleanTitle)
                Log.d(TAG, "TV candidates: ${candidates.joinToString()} (year=$year) S${seasonNumber}E${episodeNumber}")

                var tvShows: List<TmdbTVShow>? = null
                var matchedCandidate: String? = null
                candidateLoop@ for (candidate in candidates) {
                    // 先 zh-CN，若无结果再 en-US
                    val searchZh = apiService.searchTVShows(
                        apiKey = API_KEY,
                        query = candidate,
                        firstAirDateYear = year
                    )
                    tvShows = if (searchZh.isSuccessful) searchZh.body()?.results else null
                    if (tvShows.isNullOrEmpty()) {
                        val searchEn = apiService.searchTVShows(
                            apiKey = API_KEY,
                            query = candidate,
                            language = "en-US",
                            firstAirDateYear = year
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
                        episodeTitle, episodeOverview, episodeRuntime, fileSize
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
     * 从电视剧标题中提取年份和清洁标题
     * 支持多种年份格式：
     * - 神话2025 (直接跟随)
     * - 神话.2025 (点分隔)
     * - 神话(2025) (英文括号)
     * - 神话（2025） (中文括号)
     * @param seriesName 原始电视剧名称
     * @return Pair<清洁标题, 年份>
     */
    private fun extractTVYearAndTitle(seriesName: String): Pair<String, Int?> {
        // 定义年份提取的正则模式，支持多种格式
        val yearPatterns = listOf(
            // 直接跟随年份：神话2025
            Regex("^(.+?)(19|20)\\d{2}$"),
            // 点分隔年份：神话.2025
            Regex("^(.+?)\\.(19|20)\\d{2}$"),
            // 英文括号年份：神话(2025)
            Regex("^(.+?)\\((19|20)\\d{2}\\)$"),
            // 中文括号年份：神话（2025）
            Regex("^(.+?)（(19|20)\\d{2}）$"),
            // 其他括号格式：神话 (2025)、神话[2025]等
            Regex("^(.+?)\\s*[\\(\\[（]\\s*(19|20)\\d{2}\\s*[\\)\\]）]$")
        )

        for (pattern in yearPatterns) {
            val match = pattern.find(seriesName.trim())
            if (match != null) {
                val titlePart = match.groupValues[1].trim()
                val yearString = match.value.filter { it.isDigit() }.takeLast(4)
                val year = yearString.toIntOrNull()

                // 验证年份是否合理（1900-2030）
                if (year != null && year in 1900..2030) {
                    Log.d(TAG, "Extracted TV year: $year from title: $seriesName -> clean title: $titlePart")
                    return Pair(titlePart, year)
                }
            }
        }

        // 如果没有找到年份，返回原标题和null
        return Pair(seriesName, null)
    }

    /**
     * 清理电视剧标题
     * 注意：年份应该已经在 extractTVYearAndTitle 中处理过了
     */
    private fun cleanTVTitle(seriesName: String): String {
        return seriesName
            .replace(Regex("\\b(Season|S)\\s*\\d+\\b", RegexOption.IGNORE_CASE), "")
            // 移除剩余的括号内容（但不包括年份，因为年份已经被提取）
            .replace(Regex("[\\[\\(（](?!\\d{4})[^\\]\\)）]*[\\]\\)）]"), "")
            .replace(Regex("[._-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * 转换TMDB电影数据为MediaItem
     */
    private fun convertTmdbMovieToMediaItem(movie: TmdbMovie, filePath: String, fileSize: Long): MediaItem {
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
            fileSize = fileSize,
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
        episodeRuntime: Long,
        fileSize: Long
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
            overview = tvShow.overview,
            posterPath = tvShow.posterPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.POSTER_SIZE_W500}$it" },
            backdropPath = tvShow.backdropPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.BACKDROP_SIZE_W1280}$it" },
            releaseDate = releaseDate,
            rating = tvShow.voteAverage,
            duration = episodeRuntime,
            mediaType = MediaType.TV_EPISODE,
            filePath = filePath,
            fileSize = fileSize,
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

    /**
     * 获取剧集静态图片URL
     */
    fun getEpisodeStillUrl(stillPath: String?): String? {
        return stillPath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.BACKDROP_SIZE_W780}$it" }
    }

    /**
     * 获取电影详细信息（包括演员）
     * @param movieId 电影ID
     * @return 电影详细信息
     */
    suspend fun getMovieDetailsWithCast(movieId: Int): Pair<TmdbMovie, List<TmdbCast>>? {
        return withContext(Dispatchers.IO) {
            try {
                // 获取电影详情
                val detailsResponse = apiService.getMovieDetails(movieId, API_KEY)
                val movieDetails = detailsResponse.body()
                
                if (movieDetails != null) {
                    // 获取演员信息
                    val castResponse = apiService.getMovieCredits(movieId, API_KEY)
                    val cast = castResponse.body()?.cast ?: emptyList()
                    
                    // 确保中文标题/概述
                    val finalizedMovie = ensureChineseMovie(movieId, movieDetails)
                    
                    Pair(finalizedMovie, cast)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting movie details with cast: $movieId", e)
                null
            }
        }
    }

    /**
     * 获取电视剧详细信息（包括演员）
     * @param tvId 电视剧ID
     * @return 电视剧详细信息和演员列表
     */
    suspend fun getTVShowDetailsWithCast(tvId: Int): Pair<TmdbTVShow, List<TmdbCast>>? {
        return withContext(Dispatchers.IO) {
            try {
                // 获取电视剧详情
                val detailsResponse = apiService.getTVShowDetails(tvId, API_KEY)
                val tvDetails = detailsResponse.body()

                if (tvDetails != null) {
                    // 获取演员信息
                    val castResponse = apiService.getTVCredits(tvId, API_KEY)
                    val cast = castResponse.body()?.cast ?: emptyList()

                    // 确保中文标题/概述
                    val finalizedTV = ensureChineseTV(tvId, tvDetails)

                    Pair(finalizedTV, cast)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting TV show details with cast: $tvId", e)
                null
            }
        }
    }

    /**
     * 获取电视剧季详情（包括剧集图片）
     * @param tvId 电视剧ID
     * @param seasonNumber 季数
     * @return 季详情信息
     */
    suspend fun getSeasonDetailsWithImages(tvId: Int, seasonNumber: Int): TmdbSeasonDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeasonDetails(tvId, seasonNumber, API_KEY)
                response.body()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting season details: $tvId S$seasonNumber", e)
                null
            }
        }
    }

    /**
     * 获取电视剧所有季的信息
     * @param tvId 电视剧ID
     * @return 所有季的列表
     */
    suspend fun getAllSeasonsForTVShow(tvId: Int): List<TmdbSeason>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTVShowDetails(tvId, API_KEY)
                val tvDetails = response.body()
                tvDetails?.seasons?.filter { it.seasonNumber > 0 } // 过滤掉特别篇（season 0）
            } catch (e: Exception) {
                Log.e(TAG, "Error getting seasons for TV show: $tvId", e)
                null
            }
        }
    }

    /**
     * 转换TMDB演员信息为Actor模型
     * @param cast TMDB演员信息
     * @return Actor模型
     */
    private fun convertTmdbCastToActor(cast: TmdbCast): com.tvplayer.webdav.data.model.Actor {
        return com.tvplayer.webdav.data.model.Actor(
            id = cast.id.toString(),
            name = cast.name,
            role = cast.character,
            avatarUrl = cast.profilePath?.let { "${TmdbApiService.IMAGE_BASE_URL}${TmdbApiService.POSTER_SIZE_W500}$it" },
            isDirector = false
        )
    }

    /**
     * 获取电影演员列表
     * @param movieId 电影ID
     * @return 演员列表
     */
    suspend fun getMovieActors(movieId: Int): List<com.tvplayer.webdav.data.model.Actor>? {
        return withContext(Dispatchers.IO) {
            try {
                val castResponse = apiService.getMovieCredits(movieId, API_KEY)
                val cast = castResponse.body()?.cast ?: emptyList()
                
                // 转换为Actor模型，只取前10个主要演员
                cast.sortedBy { it.order }
                    .take(10)
                    .map { convertTmdbCastToActor(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting movie actors: $movieId", e)
                null
            }
        }
    }

    /**
     * 获取电视剧演员列表
     * @param tvId 电视剧ID
     * @return 演员列表
     */
    suspend fun getTVShowActors(tvId: Int): List<com.tvplayer.webdav.data.model.Actor>? {
        return withContext(Dispatchers.IO) {
            try {
                val castResponse = apiService.getTVCredits(tvId, API_KEY)
                val cast = castResponse.body()?.cast ?: emptyList()
                
                // 转换为Actor模型，只取前10个主要演员
                cast.sortedBy { it.order }
                    .take(10)
                    .map { convertTmdbCastToActor(it) }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting TV show actors: $tvId", e)
                null
            }
        }
    }
}
