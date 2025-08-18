package com.tvplayer.webdav.ui.home;

import android.view.View;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 边缘item处理助手
 * 解决边缘item焦点放大时边框被裁剪的问题
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bJ\u0016\u0010\n\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/tvplayer/webdav/ui/home/EdgeItemHelper;", "", "()V", "applyEdgeFocusEffect", "", "view", "Landroid/view/View;", "isEdge", "", "hasFocus", "isEdgeItem", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "position", "", "setupEdgePadding", "layoutManager", "Landroidx/recyclerview/widget/RecyclerView$LayoutManager;", "app_debug"})
public final class EdgeItemHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.home.EdgeItemHelper INSTANCE = null;
    
    private EdgeItemHelper() {
        super();
    }
    
    /**
     * 为RecyclerView设置适当的padding，确保边缘item的焦点效果不被裁剪
     */
    public final void setupEdgePadding(@org.jetbrains.annotations.NotNull()
    androidx.recyclerview.widget.RecyclerView recyclerView, @org.jetbrains.annotations.NotNull()
    androidx.recyclerview.widget.RecyclerView.LayoutManager layoutManager) {
    }
    
    /**
     * 检查item是否在边缘位置
     */
    public final boolean isEdgeItem(@org.jetbrains.annotations.NotNull()
    androidx.recyclerview.widget.RecyclerView recyclerView, int position) {
        return false;
    }
    
    /**
     * 为边缘item应用特殊的焦点效果
     */
    public final void applyEdgeFocusEffect(@org.jetbrains.annotations.NotNull()
    android.view.View view, boolean isEdge, boolean hasFocus) {
    }
}