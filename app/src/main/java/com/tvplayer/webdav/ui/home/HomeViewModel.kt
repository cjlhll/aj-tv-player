package com.tvplayer.webdav.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaCategory
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.MediaType
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
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
    private val mediaCache: com.tvplayer.webdav.data.storage.MediaCache
) : ViewModel() {

    private val _categories = MutableLiveData<List<MediaCategory>>()
    val categories: LiveData<List<MediaCategory>> = _categories

    private val _recentlyAdded = MutableLiveData<List<MediaItem>>()
    val recentlyAdded: LiveData<List<MediaItem>> = _recentlyAdded

    private val _continueWatching = MutableLiveData<List<MediaItem>>()
    val continueWatching: LiveData<List<MediaItem>> = _continueWatching

    private val _movies = MutableLiveData<List<MediaItem>>()
    val movies: LiveData<List<MediaItem>> = _movies

    private val _tvShows = MutableLiveData<List<MediaItem>>()
    val tvShows: LiveData<List<MediaItem>> = _tvShows

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
        mediaCache.movies().observeForever { _movies.postValue(it) }
        mediaCache.tvShows().observeForever { _tvShows.postValue(it) }
        mediaCache.recentlyAdded().observeForever { _recentlyAdded.postValue(it) }
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

        // 示例电视剧数据
        val sampleTVShows = listOf(
            MediaItem(
                id = "tv_1_s1_e1",
                title = "第一集：冬日将至",
                seriesTitle = "权力的游戏",
                seasonNumber = 1,
                episodeNumber = 1,
                seriesId = "got",
                overview = "史塔克家族的故事开始...",
                releaseDate = Date(),
                rating = 9.0f,
                duration = 62 * 60,
                mediaType = MediaType.TV_EPISODE,
                filePath = "/tv/game_of_thrones/s01/e01.mkv",
                fileSize = 2L * 1024 * 1024 * 1024,
                genre = listOf("奇幻", "剧情", "动作")
            ),
            MediaItem(
                id = "tv_2_s1_e1",
                title = "第一集：试播集",
                seriesTitle = "老友记",
                seasonNumber = 1,
                episodeNumber = 1,
                seriesId = "friends",
                overview = "六个朋友的故事开始...",
                releaseDate = Date(),
                rating = 8.9f,
                duration = 22 * 60,
                mediaType = MediaType.TV_EPISODE,
                filePath = "/tv/friends/s01/e01.mp4",
                fileSize = 500L * 1024 * 1024,
                genre = listOf("喜剧", "爱情")
            )
        )

        // 继续观看（有观看进度的内容）
        val continueWatchingItems = sampleMovies.take(2).map { movie ->
            movie.copy(
                watchedProgress = 0.3f, // 观看了30%
                lastWatchedTime = Date()
            )
        }

        // 最近添加
        val recentlyAddedItems = (sampleMovies + sampleTVShows).take(4)

        _movies.value = sampleMovies
        _tvShows.value = sampleTVShows
        _continueWatching.value = continueWatchingItems
        _recentlyAdded.value = recentlyAddedItems
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
}
