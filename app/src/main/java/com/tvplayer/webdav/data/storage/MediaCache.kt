package com.tvplayer.webdav.data.storage

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvplayer.webdav.data.model.MediaItem
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

    fun recentlyAdded(): LiveData<List<MediaItem>> {
        val live = MediatorLiveData<List<MediaItem>>()
        live.addSource(_allItems) { items ->
            live.value = items.take(10)
        }
        return live
    }
}

