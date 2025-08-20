package com.tvplayer.webdav.data.storage

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvplayer.webdav.data.model.PlaybackState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 播放状态管理器
 * 负责TV系列播放状态的存储、检索和管理
 */
@Singleton
class PlaybackStateManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_PLAYBACK_STATES = "playback_states"
        private const val TAG = "PlaybackStateManager"
    }

    private val gson = Gson()
    
    // 内存中的播放状态缓存
    private val playbackStates = mutableMapOf<String, PlaybackState>()
    
    // LiveData for observing playback state changes
    private val _currentPlaybackState = MutableLiveData<PlaybackState?>()
    val currentPlaybackState: LiveData<PlaybackState?> = _currentPlaybackState

    init {
        loadPersistedStates()
    }

    /**
     * 从SharedPreferences加载持久化的播放状态
     */
    private fun loadPersistedStates() {
        try {
            val json = prefs.getString(KEY_PLAYBACK_STATES, null)
            if (!json.isNullOrEmpty()) {
                val type = object : TypeToken<Map<String, PlaybackState>>() {}.type
                val loadedStates = gson.fromJson<Map<String, PlaybackState>>(json, type)
                if (loadedStates != null) {
                    playbackStates.clear()
                    playbackStates.putAll(loadedStates)
                    android.util.Log.d(TAG, "Loaded ${playbackStates.size} playback states")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error loading playback states", e)
        }
    }

    /**
     * 将播放状态持久化到SharedPreferences
     */
    private fun persistStates() {
        try {
            val json = gson.toJson(playbackStates)
            prefs.edit().putString(KEY_PLAYBACK_STATES, json).apply()
            android.util.Log.d(TAG, "Persisted ${playbackStates.size} playback states")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error persisting playback states", e)
        }
    }

    /**
     * 获取指定系列的播放状态
     */
    fun getPlaybackState(seriesId: String): PlaybackState? {
        return playbackStates[seriesId]
    }

    /**
     * 保存播放状态
     */
    fun savePlaybackState(playbackState: PlaybackState) {
        android.util.Log.d(TAG, "Saving playback state for series ${playbackState.seriesId}: ${playbackState.getEpisodeIdentifier()}, progress: ${playbackState.getFormattedProgress()}")
        
        playbackStates[playbackState.seriesId] = playbackState
        persistStates()
        
        // 通知观察者状态变化
        _currentPlaybackState.postValue(playbackState)
    }

    /**
     * 更新播放进度
     */
    fun updatePlaybackProgress(seriesId: String, progress: Long, duration: Long = 0L) {
        val currentState = playbackStates[seriesId]
        if (currentState != null) {
            val updatedState = currentState.updateProgress(progress, duration)
            savePlaybackState(updatedState)
        } else {
            android.util.Log.w(TAG, "No existing playback state found for series: $seriesId")
        }
    }

    /**
     * 开始播放指定剧集
     */
    fun startPlayback(seriesId: String, seasonNumber: Int, episodeNumber: Int, duration: Long = 0L) {
        val playbackState = PlaybackState(
            seriesId = seriesId,
            currentSeasonNumber = seasonNumber,
            currentEpisodeNumber = episodeNumber,
            playbackProgress = 0L,
            totalDuration = duration
        )
        savePlaybackState(playbackState)
    }

    /**
     * 切换到下一集
     */
    fun switchToNextEpisode(seriesId: String, nextSeasonNumber: Int, nextEpisodeNumber: Int) {
        val currentState = playbackStates[seriesId]
        val nextState = if (currentState != null) {
            currentState.nextEpisode(nextSeasonNumber, nextEpisodeNumber)
        } else {
            PlaybackState(
                seriesId = seriesId,
                currentSeasonNumber = nextSeasonNumber,
                currentEpisodeNumber = nextEpisodeNumber,
                playbackProgress = 0L
            )
        }
        savePlaybackState(nextState)
    }

    /**
     * 删除指定系列的播放状态
     */
    fun clearPlaybackState(seriesId: String) {
        playbackStates.remove(seriesId)
        persistStates()
        
        // 如果清除的是当前播放状态，通知观察者
        val currentState = _currentPlaybackState.value
        if (currentState?.seriesId == seriesId) {
            _currentPlaybackState.postValue(null)
        }
    }

    /**
     * 获取所有播放状态
     */
    fun getAllPlaybackStates(): Map<String, PlaybackState> {
        return playbackStates.toMap()
    }

    /**
     * 检查指定系列是否有播放状态
     */
    fun hasPlaybackState(seriesId: String): Boolean {
        return playbackStates.containsKey(seriesId)
    }

    /**
     * 获取最近播放的系列
     */
    fun getRecentlyPlayedSeries(limit: Int = 10): List<PlaybackState> {
        return playbackStates.values
            .sortedByDescending { it.lastPlayedTimestamp }
            .take(limit)
    }
}
