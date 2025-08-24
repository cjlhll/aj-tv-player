package com.tvplayer.webdav.ui.home

import android.content.SharedPreferences
import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 焦点状态管理器
 * 负责保存和恢复首页RecyclerView的焦点位置和滚动状态
 */
@Singleton
class FocusStateManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_CONTINUE_WATCHING_FOCUS = "home_continue_watching_focus"
        private const val KEY_PLAYBACK_HISTORY_FOCUS = "home_playback_history_focus"
        private const val KEY_MOVIES_FOCUS = "home_movies_focus"
        private const val KEY_TV_SHOWS_FOCUS = "home_tv_shows_focus"
        private const val KEY_CONTINUE_WATCHING_SCROLL = "home_continue_watching_scroll"
        private const val KEY_PLAYBACK_HISTORY_SCROLL = "home_playback_history_scroll"
        private const val KEY_MOVIES_SCROLL = "home_movies_scroll"
        private const val KEY_TV_SHOWS_SCROLL = "home_tv_shows_scroll"
        private const val KEY_LAST_FOCUSED_RECYCLER = "home_last_focused_recycler"
        private const val TAG = "FocusStateManager"
    }

    /**
     * RecyclerView标识符
     */
    enum class RecyclerViewType {
        CONTINUE_WATCHING,
        PLAYBACK_HISTORY,
        MOVIES,
        TV_SHOWS
    }

    /**
     * 焦点状态数据类
     */
    @Parcelize
    data class FocusState(
        val focusedPosition: Int = -1,
        val scrollX: Int = 0,
        val scrollY: Int = 0
    ) : Parcelable

    /**
     * 保存RecyclerView的焦点状态
     */
    fun saveFocusState(recyclerView: RecyclerView, type: RecyclerViewType) {
        try {
            val layoutManager = recyclerView.layoutManager
            val focusedChild = recyclerView.focusedChild
            
            // 获取当前焦点位置
            val focusedPosition = if (focusedChild != null) {
                recyclerView.getChildAdapterPosition(focusedChild)
            } else {
                -1
            }
            
            // 获取滚动位置
            val scrollX = recyclerView.scrollX
            val scrollY = recyclerView.scrollY
            
            val focusState = FocusState(focusedPosition, scrollX, scrollY)
            
            // 保存到SharedPreferences
            val focusKey = getFocusKey(type)
            val scrollKey = getScrollKey(type)
            
            prefs.edit()
                .putInt(focusKey, focusedPosition)
                .putInt(scrollKey + "_x", scrollX)
                .putInt(scrollKey + "_y", scrollY)
                .apply()
                
            // 如果有焦点，记录最后聚焦的RecyclerView
            if (focusedPosition >= 0) {
                prefs.edit()
                    .putString(KEY_LAST_FOCUSED_RECYCLER, type.name)
                    .apply()
            }
            
            android.util.Log.d(TAG, "Saved focus state for $type: position=$focusedPosition, scroll=($scrollX, $scrollY)")
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving focus state for $type", e)
        }
    }

    /**
     * 恢复RecyclerView的焦点状态
     */
    fun restoreFocusState(recyclerView: RecyclerView, type: RecyclerViewType) {
        try {
            val focusKey = getFocusKey(type)
            val scrollKey = getScrollKey(type)
            
            val focusedPosition = prefs.getInt(focusKey, -1)
            val scrollX = prefs.getInt(scrollKey + "_x", 0)
            val scrollY = prefs.getInt(scrollKey + "_y", 0)
            
            if (focusedPosition >= 0) {
                // 延迟恢复，确保RecyclerView已经完成布局
                recyclerView.post {
                    restoreFocusStateInternal(recyclerView, type, focusedPosition, scrollX, scrollY)
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error restoring focus state for $type", e)
        }
    }

    /**
     * 内部恢复焦点状态的方法
     */
    private fun restoreFocusStateInternal(
        recyclerView: RecyclerView, 
        type: RecyclerViewType, 
        focusedPosition: Int, 
        scrollX: Int, 
        scrollY: Int
    ) {
        try {
            val adapter = recyclerView.adapter
            if (adapter == null || focusedPosition >= adapter.itemCount) {
                android.util.Log.w(TAG, "Cannot restore focus for $type: invalid position $focusedPosition")
                return
            }
            
            // 先滚动到目标位置
            val layoutManager = recyclerView.layoutManager
            when (layoutManager) {
                is LinearLayoutManager -> {
                    layoutManager.scrollToPositionWithOffset(focusedPosition, 0)
                }
                is GridLayoutManager -> {
                    layoutManager.scrollToPositionWithOffset(focusedPosition, 0)
                }
            }
            
            // 延迟请求焦点，确保视图已经可见
            recyclerView.postDelayed({
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(focusedPosition)
                if (viewHolder != null) {
                    val itemView = viewHolder.itemView
                    itemView.requestFocus()
                    
                    // 手动触发焦点动画效果
                    triggerFocusAnimation(itemView, true)
                    
                    android.util.Log.d(TAG, "Restored focus for $type at position $focusedPosition")
                } else {
                    android.util.Log.w(TAG, "ViewHolder not found for position $focusedPosition in $type")
                }
            }, 100)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in restoreFocusStateInternal for $type", e)
        }
    }

    /**
     * 触发焦点动画效果
     */
    private fun triggerFocusAnimation(itemView: View, hasFocus: Boolean) {
        try {
            // 使用PosterFocusAnimator的手动触发方法
            PosterFocusAnimator.triggerFocusStateSync(itemView, hasFocus)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error triggering focus animation", e)
        }
    }

    /**
     * 获取最后聚焦的RecyclerView类型
     */
    fun getLastFocusedRecyclerView(): RecyclerViewType? {
        val typeName = prefs.getString(KEY_LAST_FOCUSED_RECYCLER, null)
        return if (typeName != null) {
            try {
                RecyclerViewType.valueOf(typeName)
            } catch (e: IllegalArgumentException) {
                null
            }
        } else {
            null
        }
    }

    /**
     * 清除所有焦点状态
     */
    fun clearAllFocusStates() {
        prefs.edit()
            .remove(KEY_CONTINUE_WATCHING_FOCUS)
            .remove(KEY_PLAYBACK_HISTORY_FOCUS)
            .remove(KEY_MOVIES_FOCUS)
            .remove(KEY_TV_SHOWS_FOCUS)
            .remove(KEY_CONTINUE_WATCHING_SCROLL)
            .remove(KEY_PLAYBACK_HISTORY_SCROLL)
            .remove(KEY_MOVIES_SCROLL)
            .remove(KEY_TV_SHOWS_SCROLL)
            .remove(KEY_LAST_FOCUSED_RECYCLER)
            .apply()

        android.util.Log.d(TAG, "Cleared all focus states")
    }

    /**
     * 获取焦点位置的SharedPreferences键
     */
    private fun getFocusKey(type: RecyclerViewType): String {
        return when (type) {
            RecyclerViewType.CONTINUE_WATCHING -> KEY_CONTINUE_WATCHING_FOCUS
            RecyclerViewType.PLAYBACK_HISTORY -> KEY_PLAYBACK_HISTORY_FOCUS
            RecyclerViewType.MOVIES -> KEY_MOVIES_FOCUS
            RecyclerViewType.TV_SHOWS -> KEY_TV_SHOWS_FOCUS
        }
    }

    /**
     * 获取滚动位置的SharedPreferences键
     */
    private fun getScrollKey(type: RecyclerViewType): String {
        return when (type) {
            RecyclerViewType.CONTINUE_WATCHING -> KEY_CONTINUE_WATCHING_SCROLL
            RecyclerViewType.PLAYBACK_HISTORY -> KEY_PLAYBACK_HISTORY_SCROLL
            RecyclerViewType.MOVIES -> KEY_MOVIES_SCROLL
            RecyclerViewType.TV_SHOWS -> KEY_TV_SHOWS_SCROLL
        }
    }

    /**
     * 保存所有RecyclerView的焦点状态
     */
    fun saveAllFocusStates(
        continueWatchingRv: RecyclerView?,
        playbackHistoryRv: RecyclerView?,
        moviesRv: RecyclerView?,
        tvShowsRv: RecyclerView?
    ) {
        continueWatchingRv?.let { saveFocusState(it, RecyclerViewType.CONTINUE_WATCHING) }
        playbackHistoryRv?.let { saveFocusState(it, RecyclerViewType.PLAYBACK_HISTORY) }
        moviesRv?.let { saveFocusState(it, RecyclerViewType.MOVIES) }
        tvShowsRv?.let { saveFocusState(it, RecyclerViewType.TV_SHOWS) }
    }

    /**
     * 恢复所有RecyclerView的焦点状态
     */
    fun restoreAllFocusStates(
        continueWatchingRv: RecyclerView?,
        playbackHistoryRv: RecyclerView?,
        moviesRv: RecyclerView?,
        tvShowsRv: RecyclerView?
    ) {
        continueWatchingRv?.let { restoreFocusState(it, RecyclerViewType.CONTINUE_WATCHING) }
        playbackHistoryRv?.let { restoreFocusState(it, RecyclerViewType.PLAYBACK_HISTORY) }
        moviesRv?.let { restoreFocusState(it, RecyclerViewType.MOVIES) }
        tvShowsRv?.let { restoreFocusState(it, RecyclerViewType.TV_SHOWS) }
    }
}
