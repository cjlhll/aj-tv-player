package com.tvplayer.webdav.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.MediaItem;

/**
 * 媒体海报适配器
 * 显示电影和电视剧的海报墙
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001:\u0002\u0011\u0012B1\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0016\b\u0002\u0010\u0007\u001a\u0010\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u0018\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\fH\u0016R\u001c\u0010\u0007\u001a\u0010\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/tvplayer/webdav/ui/home/MediaPosterAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/tvplayer/webdav/data/model/MediaItem;", "Lcom/tvplayer/webdav/ui/home/MediaPosterAdapter$MediaViewHolder;", "onMediaClick", "Lkotlin/Function1;", "", "onItemFocused", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "onBindViewHolder", "holder", "position", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "MediaDiffCallback", "MediaViewHolder", "app_debug"})
public final class MediaPosterAdapter extends androidx.recyclerview.widget.ListAdapter<com.tvplayer.webdav.data.model.MediaItem, com.tvplayer.webdav.ui.home.MediaPosterAdapter.MediaViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onMediaClick = null;
    @org.jetbrains.annotations.Nullable()
    private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onItemFocused = null;
    
    public MediaPosterAdapter(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onMediaClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onItemFocused) {
        super(null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tvplayer.webdav.ui.home.MediaPosterAdapter.MediaViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.ui.home.MediaPosterAdapter.MediaViewHolder holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016J\u0018\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/tvplayer/webdav/ui/home/MediaPosterAdapter$MediaDiffCallback;", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/tvplayer/webdav/data/model/MediaItem;", "()V", "areContentsTheSame", "", "oldItem", "newItem", "areItemsTheSame", "app_debug"})
    static final class MediaDiffCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.tvplayer.webdav.data.model.MediaItem> {
        
        public MediaDiffCallback() {
            super();
        }
        
        @java.lang.Override()
        public boolean areItemsTheSame(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem oldItem, @org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem newItem) {
            return false;
        }
        
        @java.lang.Override()
        public boolean areContentsTheSame(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem oldItem, @org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem newItem) {
            return false;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B9\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u0012\u0016\b\u0002\u0010\b\u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\u0015\u001a\u00020\u00072\u0006\u0010\u0016\u001a\u00020\u0006R\u000e\u0010\n\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\b\u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/tvplayer/webdav/ui/home/MediaPosterAdapter$MediaViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "onMediaClick", "Lkotlin/Function1;", "Lcom/tvplayer/webdav/data/model/MediaItem;", "", "onItemFocused", "(Landroid/view/View;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "bottomInfo", "ivPoster", "Landroid/widget/ImageView;", "layoutProgress", "progressBar", "Landroid/widget/ProgressBar;", "tvProgress", "Landroid/widget/TextView;", "tvRating", "tvSubtitle", "tvTitle", "bind", "mediaItem", "app_debug"})
    public static final class MediaViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onMediaClick = null;
        @org.jetbrains.annotations.Nullable()
        private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onItemFocused = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivPoster = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTitle = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvSubtitle = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ProgressBar progressBar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvProgress = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvRating = null;
        @org.jetbrains.annotations.NotNull()
        private final android.view.View layoutProgress = null;
        @org.jetbrains.annotations.NotNull()
        private final android.view.View bottomInfo = null;
        
        public MediaViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onMediaClick, @org.jetbrains.annotations.Nullable()
        kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.MediaItem, kotlin.Unit> onItemFocused) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem mediaItem) {
        }
    }
}