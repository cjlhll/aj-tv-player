package com.tvplayer.webdav.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.data.model.Actor
import com.tvplayer.webdav.data.model.Episode
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.MediaType
import com.tvplayer.webdav.data.storage.MediaCache
import com.tvplayer.webdav.data.tmdb.TmdbClient
import com.tvplayer.webdav.data.tmdb.TmdbSeason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * 视频详情页面ViewModel
 * 管理视频详情数据和业务逻辑
 */
@HiltViewModel
class VideoDetailsViewModel @Inject constructor(
    private val tmdbClient: TmdbClient,
    private val mediaCache: MediaCache
) : ViewModel() {

    private val _mediaItem = MutableLiveData<MediaItem>()
    val mediaItem: LiveData<MediaItem> = _mediaItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actors = MutableLiveData<List<Actor>>()
    val actors: LiveData<List<Actor>> = _actors

    // TV系列相关数据
    private val _seasons = MutableLiveData<List<TmdbSeason>>()
    val seasons: LiveData<List<TmdbSeason>> = _seasons

    private val _currentSeason = MutableLiveData<Int>()
    val currentSeason: LiveData<Int> = _currentSeason

    private val _episodes = MutableLiveData<List<Episode>>()
    val episodes: LiveData<List<Episode>> = _episodes

    private val _isLoadingEpisodes = MutableLiveData<Boolean>()
    val isLoadingEpisodes: LiveData<Boolean> = _isLoadingEpisodes

    // 电视剧集数（TMDB总集数 和 本地可用集数）
    private val _tmdbTotalEpisodes = MutableLiveData<Int>()
    val tmdbTotalEpisodes: LiveData<Int> = _tmdbTotalEpisodes

    private val _localAvailableEpisodes = MutableLiveData<Int>()
    val localAvailableEpisodes: LiveData<Int> = _localAvailableEpisodes

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * 加载视频详情
     */
    fun loadVideoDetails(mediaItem: MediaItem) {
        android.util.Log.d("VideoDetailsViewModel", "loadVideoDetails: mediaItem = $mediaItem")
        _mediaItem.value = mediaItem

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // 从TMDB加载详细信息和演员数据
                when (mediaItem.mediaType) {
                    MediaType.MOVIE -> {
                        // 提取电影ID
                        val movieId = mediaItem.id.removePrefix("movie_").toIntOrNull()
                        if (movieId != null) {
                            // 获取电影详细信息和演员
                            val movieDetails = tmdbClient.getMovieDetailsWithCast(movieId)
                            if (movieDetails != null) {
                                val (tmdbMovie, cast) = movieDetails

                                // 更新MediaItem信息
                                val updatedMediaItem = mediaItem.copy(
                                    title = tmdbMovie.title,
                                    originalTitle = tmdbMovie.originalTitle,
                                    overview = tmdbMovie.overview ?: "暂无简介",
                                    rating = tmdbMovie.voteAverage,
                                    duration = (tmdbMovie.runtime ?: 0) * 60L,
                                    genre = normalizeGenres(tmdbMovie.genres?.map { it.name } ?: emptyList())
                                )
                                _mediaItem.value = updatedMediaItem

                                // 转换演员信息
                                val actors = cast.map { tmdbCast ->
                                    Actor(
                                        id = tmdbCast.id.toString(),
                                        name = tmdbCast.name,
                                        role = tmdbCast.character,
                                        avatarUrl = tmdbCast.profilePath?.let {
                                            "${com.tvplayer.webdav.data.tmdb.TmdbApiService.IMAGE_BASE_URL}${com.tvplayer.webdav.data.tmdb.TmdbApiService.POSTER_SIZE_W500}$it"
                                        },
                                        isDirector = false
                                    )
                                }
                                _actors.value = actors
                            }
                        }
                    }

                    MediaType.TV_EPISODE -> {
                        // 提取电视剧ID
                        val tvId = mediaItem.seriesId?.removePrefix("tv_")?.toIntOrNull()
                        if (tvId != null) {
                            // 获取电视剧详细信息和演员
                            val tvDetails = tmdbClient.getTVShowDetailsWithCast(tvId)
                            if (tvDetails != null) {
                                val (tmdbTV, cast) = tvDetails

                                // 记录TMDB总集数 和 本地可用集数
                                val seriesId = mediaItem.seriesId ?: "tv_$tvId"
                                val tmdbTotal = tmdbTV.numberOfEpisodes ?: 0
                                val localAvailable = getTotalAvailableEpisodesForSeries(seriesId)
                                _tmdbTotalEpisodes.value = tmdbTotal
                                _localAvailableEpisodes.value = localAvailable

                                // 计算本地所有剧集总大小
                                val totalFileSize = getTotalFileSizeForSeries(seriesId)

                                // 更新MediaItem信息（保持电影相同字段；文件大小使用聚合总大小）
                                val updatedMediaItem = mediaItem.copy(
                                    originalTitle = tmdbTV.originalName,
                                    overview = tmdbTV.overview ?: "暂无简介",
                                    rating = tmdbTV.voteAverage,
                                    fileSize = totalFileSize,
                                    genre = normalizeGenres(tmdbTV.genres?.map { it.name } ?: emptyList())
                                )
                                _mediaItem.value = updatedMediaItem

                                // 转换演员信息
                                val actors = cast.map { tmdbCast ->
                                    Actor(
                                        id = tmdbCast.id.toString(),
                                        name = tmdbCast.name,
                                        role = tmdbCast.character,
                                        avatarUrl = tmdbCast.profilePath?.let {
                                            "${com.tvplayer.webdav.data.tmdb.TmdbApiService.IMAGE_BASE_URL}${com.tvplayer.webdav.data.tmdb.TmdbApiService.POSTER_SIZE_W500}$it"
                                        },
                                        isDirector = false
                                    )
                                }
                                _actors.value = actors

                                // 加载季信息
                                loadTVSeasons(tvId)

                                // 记录可用剧集总数（已通过LiveData记录）
                                val totalAvailable = _localAvailableEpisodes.value ?: 0
                                android.util.Log.d("VideoDetailsViewModel", "Total available episodes for series: $totalAvailable; TMDB total=${_tmdbTotalEpisodes.value}")

                                // 设置当前季（如果有季数信息）
                                mediaItem.seasonNumber?.let { seasonNum ->
                                    _currentSeason.value = seasonNum
                                    loadEpisodes(tvId, seasonNum)
                                }
                            }
                        }
                    }

                    else -> {
                        // 其他类型暂时不做处理
                    }
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * 开始播放视频
     */
    fun startPlayback() {
        val item = _mediaItem.value ?: return
        // TODO: 启动播放器逻辑
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite() {
        val item = _mediaItem.value ?: return
        viewModelScope.launch {
            try {
                // TODO: 更新收藏状态到数据库
                val updatedItem = item.copy(isFavorite = !item.isFavorite)
                _mediaItem.value = updatedItem
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * 加载电视剧季信息（仅显示有可用剧集的季）
     */
    private fun loadTVSeasons(tvId: Int) {
        viewModelScope.launch {
            try {
                val allSeasons = tmdbClient.getAllSeasonsForTVShow(tvId)
                if (allSeasons != null) {
                    // 获取当前系列的所有可用MediaItem文件
                    val currentMediaItem = _mediaItem.value
                    val seriesId = currentMediaItem?.seriesId ?: "tv_$tvId"
                    val allAvailableItems = mediaCache.getItems().filter { mediaItem ->
                        mediaItem.mediaType == MediaType.TV_EPISODE &&
                        mediaItem.seriesId == seriesId &&
                        mediaItem.filePath.isNotEmpty()
                    }

                    // 只保留有可用剧集的季
                    val availableSeasons = allSeasons.filter { season ->
                        val hasEpisodes = allAvailableItems.any { it.seasonNumber == season.seasonNumber }
                        android.util.Log.d("VideoDetailsViewModel", "Season ${season.seasonNumber}: hasEpisodes = $hasEpisodes")
                        hasEpisodes
                    }

                    android.util.Log.d("VideoDetailsViewModel", "Found ${availableSeasons.size} seasons with available episodes out of ${allSeasons.size} total seasons")
                    _seasons.value = availableSeasons
                } else {
                    _seasons.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("VideoDetailsViewModel", "Error loading TV seasons", e)
                _error.value = e.message
            }
        }
    }

    /**
     * 选择季并加载剧集
     */
    fun selectSeason(seasonNumber: Int) {
        val mediaItem = _mediaItem.value ?: return
        val tvId = mediaItem.seriesId?.removePrefix("tv_")?.toIntOrNull() ?: return

        _currentSeason.value = seasonNumber
        loadEpisodes(tvId, seasonNumber)
    }

    /**
     * 加载指定季的剧集列表（仅显示有对应WebDAV文件的剧集）
     */
    private fun loadEpisodes(tvId: Int, seasonNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoadingEpisodes.value = true

                // 获取当前系列的所有可用MediaItem文件
                val currentMediaItem = _mediaItem.value
                val seriesId = currentMediaItem?.seriesId ?: "tv_$tvId"
                val availableMediaItems = getAvailableEpisodesForSeries(seriesId, seasonNumber)

                android.util.Log.d("VideoDetailsViewModel", "Found ${availableMediaItems.size} available episodes for series $seriesId season $seasonNumber")

                val seasonDetails = tmdbClient.getSeasonDetailsWithImages(tvId, seasonNumber)
                if (seasonDetails != null) {
                    // 只创建有对应MediaItem文件的Episode对象
                    val episodes = seasonDetails.episodes.mapNotNull { tmdbEpisode ->
                        // 查找匹配的MediaItem文件
                        val matchingMediaItem = findMatchingMediaItem(availableMediaItems, tmdbEpisode.episodeNumber)

                        if (matchingMediaItem != null) {
                            android.util.Log.d("VideoDetailsViewModel", "Creating episode ${tmdbEpisode.episodeNumber} with file: ${matchingMediaItem.filePath}")
                            Episode(
                                id = "tv_${tvId}_s${seasonNumber}_e${tmdbEpisode.episodeNumber}",
                                episodeNumber = tmdbEpisode.episodeNumber,
                                name = tmdbEpisode.name,
                                overview = tmdbEpisode.overview,
                                stillPath = tmdbClient.getEpisodeStillUrl(tmdbEpisode.stillPath),
                                airDate = try {
                                    tmdbEpisode.airDate?.let { dateFormat.parse(it) }
                                } catch (e: Exception) {
                                    null
                                },
                                rating = tmdbEpisode.voteAverage,
                                runtime = tmdbEpisode.runtime,
                                seasonNumber = seasonNumber,
                                tvId = tvId,
                                watchedProgress = matchingMediaItem.watchedProgress,
                                isWatched = matchingMediaItem.isWatched,
                                lastWatchedTime = matchingMediaItem.lastWatchedTime,
                                mediaItem = matchingMediaItem
                            )
                        } else {
                            android.util.Log.d("VideoDetailsViewModel", "Skipping episode ${tmdbEpisode.episodeNumber} - no matching file found")
                            null
                        }
                    }

                    android.util.Log.d("VideoDetailsViewModel", "Created ${episodes.size} episodes with available files")
                    _episodes.value = episodes
                } else {
                    android.util.Log.w("VideoDetailsViewModel", "No season details found for TV $tvId season $seasonNumber")
                    _episodes.value = emptyList()
                }

                _isLoadingEpisodes.value = false
            } catch (e: Exception) {
                android.util.Log.e("VideoDetailsViewModel", "Error loading episodes", e)
                _error.value = e.message
                _isLoadingEpisodes.value = false
            }
        }
    }

    /**
     * 检查是否为TV系列内容
     */
    fun isTVSeries(): Boolean {
        val mediaItem = _mediaItem.value
        android.util.Log.d("VideoDetailsViewModel", "isTVSeries: mediaItem = $mediaItem")
        if (mediaItem == null) {
            android.util.Log.d("VideoDetailsViewModel", "isTVSeries: mediaItem is null, returning false")
            return false
        }
        val result = mediaItem.mediaType == MediaType.TV_EPISODE || mediaItem.mediaType == MediaType.TV_SERIES
        android.util.Log.d("VideoDetailsViewModel", "isTVSeries: mediaType = ${mediaItem.mediaType}, result = $result")
        return result
    }

    /**
     * 获取指定系列和季数的可用剧集文件
     */
    private fun getAvailableEpisodesForSeries(seriesId: String, seasonNumber: Int): List<MediaItem> {
        val allItems = mediaCache.getItems()

        // 调试日志：显示所有TV剧集
        val allTVEpisodes = allItems.filter { it.mediaType == MediaType.TV_EPISODE }
        android.util.Log.d("VideoDetailsViewModel", "All TV episodes in cache: ${allTVEpisodes.size}")
        allTVEpisodes.forEach { episode ->
            android.util.Log.d("VideoDetailsViewModel", "  Episode: seriesId=${episode.seriesId}, season=${episode.seasonNumber}, episode=${episode.episodeNumber}, file=${episode.filePath}")
        }

        val filteredItems = allItems.filter { mediaItem ->
            mediaItem.mediaType == MediaType.TV_EPISODE &&
            mediaItem.seriesId == seriesId &&
            mediaItem.seasonNumber == seasonNumber &&
            mediaItem.filePath.isNotEmpty()
        }

        android.util.Log.d("VideoDetailsViewModel", "Filtered episodes for seriesId=$seriesId, season=$seasonNumber: ${filteredItems.size}")
        return filteredItems
    }

    /**
     * 将TMDB的类型名称标准化为中文，统一显示格式
     */
    private fun normalizeGenres(raw: List<String>): List<String> {
        if (raw.isEmpty()) return emptyList()
        val mapping = mapOf(
            // Movies & TV common
            "Action" to "动作",
            "Adventure" to "冒险",
            "Animation" to "动画",
            "Comedy" to "喜剧",
            "Crime" to "犯罪",
            "Documentary" to "纪录片",
            "Drama" to "剧情",
            "Family" to "家庭",
            "Fantasy" to "奇幻",
            "History" to "历史",
            "Horror" to "恐怖",
            "Music" to "音乐",
            "Mystery" to "悬疑",
            "Romance" to "爱情",
            "Science Fiction" to "科幻",
            "Sci-Fi & Fantasy" to "科幻",
            "TV Movie" to "电视电影",
            "Thriller" to "惊悚",
            "War" to "战争",
            "Western" to "西部",
            // TV specific
            "Action & Adventure" to "动作冒险",
            "Kids" to "儿童",
            "News" to "新闻",
            "Reality" to "真人秀",
            "Sci-Fi & Fantasy" to "科幻",
            "Soap" to "肥皂剧",
            "Talk" to "脱口秀",
            "War & Politics" to "战争与政治"
        )
        // 不拆分复合类型，直接按映射转换（例如 Sci-Fi & Fantasy -> 科幻）
        val translated = raw.map { eng ->
            when (eng) {
                "Politics" -> "政治"
                else -> mapping[eng] ?: eng
            }
        }
        // Remove duplicates and keep order
        return translated.distinct()
    }
/**
     * 根据集数匹配MediaItem文件
     */
    private fun findMatchingMediaItem(availableItems: List<MediaItem>, episodeNumber: Int): MediaItem? {
        return availableItems.find { it.episodeNumber == episodeNumber }
    }

    /**
     * 获取指定系列的所有可用剧集总数
     */
    private fun getTotalAvailableEpisodesForSeries(seriesId: String): Int {
        val allItems = mediaCache.getItems()
        return allItems.count { mediaItem ->
            mediaItem.mediaType == MediaType.TV_EPISODE &&
            mediaItem.seriesId == seriesId &&
            mediaItem.filePath.isNotEmpty()
        }
    }

    /**
     * 获取指定系列的所有剧集文件总大小
     */
    private fun getTotalFileSizeForSeries(seriesId: String): Long {
        val allItems = mediaCache.getItems()
        return allItems.filter { mediaItem ->
            mediaItem.mediaType == MediaType.TV_EPISODE &&
            mediaItem.seriesId == seriesId &&
            mediaItem.filePath.isNotEmpty()
        }.sumOf { it.fileSize }
    }
}
