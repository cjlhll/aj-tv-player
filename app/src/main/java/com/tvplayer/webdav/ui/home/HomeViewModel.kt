package com.tvplayer.webdav.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaCategory
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.MediaType
import com.tvplayer.webdav.data.model.TVSeriesSummary
import com.tvplayer.webdav.data.model.PlaybackState
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
import com.tvplayer.webdav.data.storage.PlaybackStateManager
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import com.tvplayer.webdav.data.scanner.MediaScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * 主界面ViewModel
 * 管理主界面的数据和状态
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val webdavClient: SimpleWebDAVClient,
    private val serverStorage: WebDAVServerStorage,
    private val mediaScanner: MediaScanner,
    private val mediaCache: com.tvplayer.webdav.data.storage.MediaCache,
    private val playbackStateManager: PlaybackStateManager
) : ViewModel() {

    private val _categories = MutableLiveData<List<MediaCategory>>()
    val categories: LiveData<List<MediaCategory>> = _categories

    private val _recentlyAdded = MutableLiveData<List<MediaItem>>()
    val recentlyAdded: LiveData<List<MediaItem>> = _recentlyAdded

    private val _playbackHistory = MutableLiveData<List<MediaItem>>()
    val playbackHistory: LiveData<List<MediaItem>> = _playbackHistory

    private val _movies = MutableLiveData<List<MediaItem>>()
    val movies: LiveData<List<MediaItem>> = _movies

    private val _tvShows = MutableLiveData<List<TVSeriesSummary>>()
    val tvShows: LiveData<List<TVSeriesSummary>> = _tvShows

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCategories()
        // 订阅缓存到首页
        _movies.value = emptyList()
        _tvShows.value = emptyList()
        _recentlyAdded.value = emptyList()
        _playbackHistory.value = emptyList()
        mediaCache.movies().observeForever { _movies.postValue(it) }
        mediaCache.tvSeriesSummaries().observeForever { _tvShows.postValue(it) }
        mediaCache.recentlyAdded().observeForever { _recentlyAdded.postValue(it) }
        // 加载播放历史
        loadPlaybackHistory()
    }

    /**
     * 加载主界面数据
     */
    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 首页优先依赖扫描页写入的缓存，不再扫描根目录
                val server = serverStorage.getServer()
                if (server == null) {
                    // 无服务器时，展示示例数据
                    loadSampleData()
                    _error.value = null
                } else {
                    // 已配置服务器：等待/使用缓存，避免与扫描页冲突
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载分类数据
     */
    private fun loadCategories() {
        val categories = listOf(
            MediaCategory(
                id = "movies",
                name = "电影",
                description = "所有电影",
                mediaType = MediaType.MOVIE,
                itemCount = 0
            ),
            MediaCategory(
                id = "tv_shows",
                name = "电视剧",
                description = "所有电视剧",
                mediaType = MediaType.TV_SERIES,
                itemCount = 0
            ),
            MediaCategory(
                id = "recently_added",
                name = "最近添加",
                description = "新增内容",
                itemCount = 0
            ),
            MediaCategory(
                id = "continue_watching",
                name = "继续观看",
                description = "未看完的内容",
                itemCount = 0
            ),
            MediaCategory(
                id = "favorites",
                name = "收藏",
                description = "我的收藏",
                itemCount = 0
            ),
            MediaCategory(
                id = "settings",
                name = "设置",
                description = "应用设置",
                itemCount = 0
            )
        )
        _categories.value = categories
    }

    /**
     * 加载示例数据（用于演示）
     */
    private fun loadSampleData() {
        // 示例电影数据
        val sampleMovies = listOf(
            MediaItem(
                id = "movie_1",
                title = "复仇者联盟：终局之战",
                originalTitle = "Avengers: Endgame",
                overview = "超级英雄们为了拯救宇宙而进行的最终战斗...",
                releaseDate = Date(),
                rating = 8.4f,
                duration = 181 * 60, // 181分钟
                mediaType = MediaType.MOVIE,
                filePath = "/movies/avengers_endgame.mkv",
                fileSize = 4L * 1024 * 1024 * 1024, // 4GB
                genre = listOf("动作", "科幻", "冒险")
            ),
            MediaItem(
                id = "movie_2",
                title = "肖申克的救赎",
                originalTitle = "The Shawshank Redemption",
                overview = "一个关于希望、友谊和救赎的故事...",
                releaseDate = Date(),
                rating = 9.3f,
                duration = 142 * 60,
                mediaType = MediaType.MOVIE,
                filePath = "/movies/shawshank_redemption.mp4",
                fileSize = 3L * 1024 * 1024 * 1024,
                genre = listOf("剧情")
            ),
            MediaItem(
                id = "movie_3",
                title = "盗梦空间",
                originalTitle = "Inception",
                overview = "在梦境中植入想法的科幻惊悚片...",
                releaseDate = Date(),
                rating = 8.8f,
                duration = 148 * 60,
                mediaType = MediaType.MOVIE,
                filePath = "/movies/inception.mkv",
                fileSize = 5L * 1024 * 1024 * 1024,
                genre = listOf("科幻", "惊悚", "动作")
            )
        )

        // 示例电视剧系列数据（使用真实的TMDB ID）
        val sampleTVShows = listOf(
            TVSeriesSummary(
                seriesId = "tv_1399", // 权力的游戏的TMDB ID
                seriesTitle = "权力的游戏",
                overview = "在维斯特洛大陆上，七个王国为了争夺铁王座而展开的史诗级斗争...",
                rating = 9.0f,
                releaseDate = Date(),
                genre = listOf("奇幻", "剧情", "动作"),
                totalSeasons = 8,
                totalEpisodes = 73,
                watchedEpisodes = 15,
                episodes = emptyList()
            ),
            TVSeriesSummary(
                seriesId = "tv_1668", // 老友记的TMDB ID
                seriesTitle = "老友记",
                overview = "六个朋友在纽约的生活、爱情和友谊的温馨喜剧...",
                rating = 8.9f,
                releaseDate = Date(),
                genre = listOf("喜剧", "爱情"),
                totalSeasons = 10,
                totalEpisodes = 236,
                watchedEpisodes = 50,
                episodes = emptyList()
            ),
            TVSeriesSummary(
                seriesId = "tv_1396", // 绝命毒师的TMDB ID
                seriesTitle = "绝命毒师",
                overview = "一个高中化学老师因为癌症诊断而开始制毒的犯罪剧...",
                rating = 9.5f,
                releaseDate = Date(),
                genre = listOf("犯罪", "剧情", "惊悚"),
                totalSeasons = 5,
                totalEpisodes = 62,
                watchedEpisodes = 0,
                episodes = emptyList()
            )
        )

        // 最近添加（只包含电影，因为TV shows现在是不同的数据类型）
        val recentlyAddedItems = sampleMovies.take(4)

        // 创建示例播放历史数据
        createSamplePlaybackHistory(sampleMovies, sampleTVShows)

        _movies.value = sampleMovies
        _tvShows.value = sampleTVShows
        _recentlyAdded.value = recentlyAddedItems
    }

    /**
     * 创建示例播放历史数据
     */
    private fun createSamplePlaybackHistory(sampleMovies: List<MediaItem>, sampleTVShows: List<TVSeriesSummary>) {
        val currentTime = Date()
        val oneHourAgo = Date(currentTime.time - 3600 * 1000)
        val twoDaysAgo = Date(currentTime.time - 2 * 24 * 3600 * 1000)
        val threeDaysAgo = Date(currentTime.time - 3 * 24 * 3600 * 1000)

        // 为电影创建播放状态
        val moviePlaybackStates = listOf(
            PlaybackState(
                seriesId = "movie_1", // 复仇者联盟：终局之战
                currentSeasonNumber = 1,
                currentEpisodeNumber = 1,
                playbackProgress = 3600L, // 播放了1小时
                totalDuration = 10800L, // 总长3小时
                lastPlayedTimestamp = oneHourAgo
            ),
            PlaybackState(
                seriesId = "movie_2", // 肖申克的救赎
                currentSeasonNumber = 1,
                currentEpisodeNumber = 1,
                playbackProgress = 5400L, // 播放了1.5小时
                totalDuration = 8400L, // 总长2.33小时
                lastPlayedTimestamp = twoDaysAgo
            )
        )

        // 为电视剧创建播放状态
        val tvPlaybackStates = listOf(
            PlaybackState(
                seriesId = "tv_1396", // 绝命毒师
                currentSeasonNumber = 1,
                currentEpisodeNumber = 3,
                playbackProgress = 1800L, // 播放了30分钟
                totalDuration = 2700L, // 总长45分钟
                lastPlayedTimestamp = currentTime
            ),
            PlaybackState(
                seriesId = "movie_3", // 盗梦空间
                currentSeasonNumber = 1,
                currentEpisodeNumber = 1,
                playbackProgress = 7200L, // 播放了2小时
                totalDuration = 8880L, // 总长2.47小时
                lastPlayedTimestamp = threeDaysAgo
            )
        )

        // 保存播放状态到PlaybackStateManager
        (moviePlaybackStates + tvPlaybackStates).forEach { playbackState ->
            playbackStateManager.savePlaybackState(playbackState)
        }

        // 立即加载播放历史
        loadPlaybackHistory()
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadHomeData()
    }

    /**
     * 重新扫描并刮削媒体库，更新缓存供首页使用
     */
    fun rescanAndScrape() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val server = serverStorage.getServer()
                if (server == null) {
                    loadSampleData()
                } else {
                    val all = mutableListOf<MediaItem>()
                    val moviesDir = serverStorage.getMoviesDir() ?: "/"
                    val tvDir = serverStorage.getTvDir() ?: "/"

                    if (!moviesDir.isNullOrEmpty()) {
                        val items = mediaScanner.scanDirectory(moviesDir, recursive = true, modeHint = MediaScanner.ModeHint.MOVIE, callback = null)
                        all.addAll(items)
                    }
                    if (!tvDir.isNullOrEmpty()) {
                        val items = mediaScanner.scanDirectory(tvDir, recursive = true, modeHint = MediaScanner.ModeHint.TV, callback = null)
                        all.addAll(items)
                    }
                    mediaCache.setItems(all)
                }
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 加载播放历史记录
     */
    fun loadPlaybackHistory() {
        viewModelScope.launch {
            try {
                val playbackStates = playbackStateManager.getRecentlyPlayedSeries(20) // 获取最近20个播放记录
                val historyItems = convertPlaybackStatesToMediaItems(playbackStates)
                _playbackHistory.postValue(historyItems)
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error loading playback history", e)
                _playbackHistory.postValue(emptyList())
            }
        }
    }

    /**
     * 将播放状态转换为MediaItem列表
     */
    private fun convertPlaybackStatesToMediaItems(playbackStates: List<PlaybackState>): List<MediaItem> {
        val allMediaItems = mediaCache.getItems()
        val historyItems = mutableListOf<MediaItem>()

        for (playbackState in playbackStates) {
            val mediaItem = findMediaItemForPlaybackState(playbackState, allMediaItems)
            if (mediaItem != null) {
                // 创建带有播放进度的MediaItem副本
                val historyItem = mediaItem.copy(
                    watchedProgress = playbackState.getProgressPercentage(),
                    lastWatchedTime = playbackState.lastPlayedTimestamp,
                    isWatched = playbackState.isNearlyCompleted()
                )
                historyItems.add(historyItem)
            }
        }

        return historyItems
    }

    /**
     * 根据播放状态查找对应的MediaItem
     */
    private fun findMediaItemForPlaybackState(playbackState: PlaybackState, allMediaItems: List<MediaItem>): MediaItem? {
        // 首先尝试通过seriesId匹配电视剧
        val tvSeriesMatch = allMediaItems.find { mediaItem ->
            mediaItem.seriesId == playbackState.seriesId &&
            mediaItem.mediaType == MediaType.TV_EPISODE &&
            mediaItem.seasonNumber == playbackState.currentSeasonNumber &&
            mediaItem.episodeNumber == playbackState.currentEpisodeNumber
        }

        if (tvSeriesMatch != null) {
            return tvSeriesMatch
        }

        // 如果没找到电视剧匹配，尝试通过文件路径匹配电影
        val movieMatch = allMediaItems.find { mediaItem ->
            mediaItem.id == playbackState.seriesId || // 电影使用文件路径作为seriesId
            mediaItem.filePath == playbackState.seriesId
        }

        if (movieMatch != null) {
            return movieMatch
        }

        // 如果还是没找到，尝试通过seriesId匹配电视剧系列的第一集
        val seriesFirstEpisode = allMediaItems
            .filter { it.seriesId == playbackState.seriesId && it.mediaType == MediaType.TV_EPISODE }
            .minByOrNull { (it.seasonNumber ?: 1) * 1000 + (it.episodeNumber ?: 1) }

        return seriesFirstEpisode
    }

    /**
     * 刷新播放历史
     */
    fun refreshPlaybackHistory() {
        loadPlaybackHistory()
    }
}
