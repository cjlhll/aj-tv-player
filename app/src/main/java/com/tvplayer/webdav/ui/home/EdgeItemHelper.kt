package com.tvplayer.webdav.ui.home

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 边缘item处理助手
 * 解决边缘item焦点放大时边框被裁剪的问题
 */
object EdgeItemHelper {
    
    /**
     * 为RecyclerView设置适当的padding，确保边缘item的焦点效果不被裁剪
     */
    fun setupEdgePadding(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager) {
        when (layoutManager) {
            is GridLayoutManager -> {
                // 网格布局需要更多的左右padding
                recyclerView.setPadding(24, 16, 24, 16)
            }
            is LinearLayoutManager -> {
                if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
                    // 水平滚动需要左右padding
                    recyclerView.setPadding(24, 8, 24, 8)
                } else {
                    // 垂直滚动需要上下padding
                    recyclerView.setPadding(8, 16, 8, 16)
                }
            }
            else -> {
                // 默认padding
                recyclerView.setPadding(16, 16, 16, 16)
            }
        }
        
        // 确保padding不会影响滚动
        recyclerView.clipToPadding = false
    }
    
    /**
     * 检查item是否在边缘位置
     */
    fun isEdgeItem(recyclerView: RecyclerView, position: Int): Boolean {
        val layoutManager = recyclerView.layoutManager ?: return false
        val itemCount = recyclerView.adapter?.itemCount ?: 0
        
        return when (layoutManager) {
            is GridLayoutManager -> {
                val spanCount = layoutManager.spanCount
                // 检查是否在第一列或最后一列
                val column = position % spanCount
                column == 0 || column == spanCount - 1
            }
            is LinearLayoutManager -> {
                if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) {
                    // 水平滚动：第一个或最后一个
                    position == 0 || position == itemCount - 1
                } else {
                    // 垂直滚动：第一个或最后一个
                    position == 0 || position == itemCount - 1
                }
            }
            else -> false
        }
    }
    
    /**
     * 为边缘item应用特殊的焦点效果
     */
    fun applyEdgeFocusEffect(view: View, isEdge: Boolean, hasFocus: Boolean) {
        if (!isEdge) {
            // 非边缘item使用正常的焦点效果
            FocusHighlightHelper.setupFocusHighlight(view)
            return
        }
        
        // 边缘item使用稍小的缩放比例
        val scale = if (hasFocus) 1.04f else 1.0f
        val elevation = if (hasFocus) 12f else 8f
        
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .translationZ(elevation)
            .setDuration(250L)
            .start()
    }
}
