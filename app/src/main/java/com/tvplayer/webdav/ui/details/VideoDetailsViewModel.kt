package com.tvplayer.webdav.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.data.model.Actor
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.tmdb.TmdbClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 视频详情页面ViewModel
 * 管理视频详情数据和业务逻辑
 */
@HiltViewModel
class VideoDetailsViewModel @Inject constructor(
    private val tmdbClient: TmdbClient
) : ViewModel() {

    private val _mediaItem = MutableLiveData<MediaItem>()
    val mediaItem: LiveData<MediaItem> = _mediaItem

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actors = MutableLiveData<List<Actor>>()
    val actors: LiveData<List<Actor>> = _actors

    /**
     * 加载视频详情
     */
    fun loadVideoDetails(mediaItem: MediaItem) {
        _mediaItem.value = mediaItem
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // 从TMDB加载详细信息和演员数据
                when (mediaItem.mediaType) {
                    com.tvplayer.webdav.data.model.MediaType.MOVIE -> {
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
                                    genre = tmdbMovie.genres?.map { it.name } ?: emptyList()
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
                    
                    com.tvplayer.webdav.data.model.MediaType.TV_EPISODE -> {
                        // 提取电视剧ID
                        val tvId = mediaItem.seriesId?.removePrefix("tv_")?.toIntOrNull()
                        if (tvId != null) {
                            // 获取电视剧详细信息和演员
                            val tvDetails = tmdbClient.getTVShowDetailsWithCast(tvId)
                            if (tvDetails != null) {
                                val (tmdbTV, cast) = tvDetails
                                
                                // 更新MediaItem信息
                                val updatedMediaItem = mediaItem.copy(
                                    originalTitle = tmdbTV.originalName,
                                    overview = tmdbTV.overview ?: "暂无简介",
                                    rating = tmdbTV.voteAverage,
                                    genre = tmdbTV.genres?.map { it.name } ?: emptyList()
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
}
