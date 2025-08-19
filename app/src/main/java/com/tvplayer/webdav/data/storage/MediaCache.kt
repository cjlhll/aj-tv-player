package com.tvplayer.webdav.data.storage

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.TVSeriesSummary
import com.tvplayer.webdav.data.model.MediaType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaCache @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_CACHE_ALL = "media_cache_all_items"
    }

    private val gson = Gson()

    private fun loadPersistedItems(): List<MediaItem> {
        return try {
            val json = prefs.getString(KEY_CACHE_ALL, null) ?: return emptyList()
            val type = object : TypeToken<List<MediaItem>>() {}.type
            gson.fromJson<List<MediaItem>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun persistItems(items: List<MediaItem>) {
        try {
            val json = gson.toJson(items)
            prefs.edit().putString(KEY_CACHE_ALL, json).apply()
        } catch (_: Exception) { }
    }

    private val _allItems = MutableLiveData<List<MediaItem>>(loadPersistedItems())

    fun setItems(items: List<MediaItem>) {
        _allItems.postValue(items)
        persistItems(items)
    }

    fun getItems(): List<MediaItem> {
        return _allItems.value ?: emptyList()
    }

    fun allItems(): LiveData<List<MediaItem>> = _allItems

    fun movies(): LiveData<List<MediaItem>> {
        val live = MediatorLiveData<List<MediaItem>>()
        live.addSource(_allItems) { items ->
            live.value = items.filter { it.mediaType.name.startsWith("MOVIE") }
        }
        return live
    }

    fun tvShows(): LiveData<List<MediaItem>> {
        val live = MediatorLiveData<List<MediaItem>>()
        live.addSource(_allItems) { items ->
            live.value = items.filter { it.mediaType.name.startsWith("TV") }
        }
        return live
    }

    fun tvSeriesSummaries(): LiveData<List<TVSeriesSummary>> {
        val live = MediatorLiveData<List<TVSeriesSummary>>()
        live.addSource(_allItems) { items ->
            live.value = groupTVEpisodesBySeries(items)
        }
        return live
    }

    private fun groupTVEpisodesBySeries(items: List<MediaItem>): List<TVSeriesSummary> {
        val tvEpisodes = items.filter {
            it.mediaType == MediaType.TV_EPISODE || it.mediaType == MediaType.TV_SERIES
        }

        val groupedBySeries = tvEpisodes.groupBy { episode ->
            episode.seriesId ?: episode.seriesTitle ?: episode.title
        }

        return groupedBySeries.map { (seriesKey, episodes) ->
            val firstEpisode = episodes.first()
            val seasons = episodes.mapNotNull { it.seasonNumber }.distinct()
            val totalSeasons = seasons.size
            val totalEpisodes = episodes.size
            val watchedEpisodes = episodes.count { it.watchedProgress > 0.9f } // 90%以上算看完
            val lastWatchedTime = episodes.mapNotNull { it.lastWatchedTime }.maxOrNull()

            TVSeriesSummary(
                seriesId = firstEpisode.seriesId ?: "series_${seriesKey.hashCode()}",
                seriesTitle = firstEpisode.seriesTitle ?: firstEpisode.title,
                posterPath = firstEpisode.posterPath,
                backdropPath = firstEpisode.backdropPath,
                overview = firstEpisode.overview,
                rating = firstEpisode.rating,
                releaseDate = firstEpisode.releaseDate,
                genre = firstEpisode.genre,
                totalSeasons = totalSeasons,
                totalEpisodes = totalEpisodes,
                watchedEpisodes = watchedEpisodes,
                lastWatchedTime = lastWatchedTime,
                episodes = episodes
            )
        }.sortedByDescending { it.lastWatchedTime ?: it.releaseDate }
    }

    fun recentlyAdded(): LiveData<List<MediaItem>> {
        val live = MediatorLiveData<List<MediaItem>>()
        live.addSource(_allItems) { items ->
            // 只显示电影作为最近添加，避免显示大量TV剧集
            live.value = items.filter { it.mediaType == MediaType.MOVIE }.take(10)
        }
        return live
    }
}

