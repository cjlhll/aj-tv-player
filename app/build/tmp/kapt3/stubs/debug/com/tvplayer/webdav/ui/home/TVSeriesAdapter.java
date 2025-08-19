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
import com.tvplayer.webdav.data.model.TVSeriesSummary;

/**
 * 电视剧系列适配器
 * 显示电视剧系列的海报墙，而不是单个剧集
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001:\u0002\u0011\u0012B1\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0016\b\u0002\u0010\u0007\u001a\u0010\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u0016J\u0018\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\fH\u0016R\u001c\u0010\u0007\u001a\u0010\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/tvplayer/webdav/ui/home/TVSeriesAdapter;", "Landroidx/recyclerview/widget/ListAdapter;", "Lcom/tvplayer/webdav/data/model/TVSeriesSummary;", "Lcom/tvplayer/webdav/ui/home/TVSeriesAdapter$TVSeriesViewHolder;", "onSeriesClick", "Lkotlin/Function1;", "", "onItemFocused", "(Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "onBindViewHolder", "holder", "position", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "TVSeriesDiffCallback", "TVSeriesViewHolder", "app_debug"})
public final class TVSeriesAdapter extends androidx.recyclerview.widget.ListAdapter<com.tvplayer.webdav.data.model.TVSeriesSummary, com.tvplayer.webdav.ui.home.TVSeriesAdapter.TVSeriesViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onSeriesClick = null;
    @org.jetbrains.annotations.Nullable()
    private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onItemFocused = null;
    
    public TVSeriesAdapter(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onSeriesClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onItemFocused) {
        super(null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tvplayer.webdav.ui.home.TVSeriesAdapter.TVSeriesViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.ui.home.TVSeriesAdapter.TVSeriesViewHolder holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0018\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016J\u0018\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00022\u0006\u0010\u0007\u001a\u00020\u0002H\u0016\u00a8\u0006\t"}, d2 = {"Lcom/tvplayer/webdav/ui/home/TVSeriesAdapter$TVSeriesDiffCallback;", "Landroidx/recyclerview/widget/DiffUtil$ItemCallback;", "Lcom/tvplayer/webdav/data/model/TVSeriesSummary;", "()V", "areContentsTheSame", "", "oldItem", "newItem", "areItemsTheSame", "app_debug"})
    static final class TVSeriesDiffCallback extends androidx.recyclerview.widget.DiffUtil.ItemCallback<com.tvplayer.webdav.data.model.TVSeriesSummary> {
        
        public TVSeriesDiffCallback() {
            super();
        }
        
        @java.lang.Override()
        public boolean areItemsTheSame(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.TVSeriesSummary oldItem, @org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.TVSeriesSummary newItem) {
            return false;
        }
        
        @java.lang.Override()
        public boolean areContentsTheSame(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.TVSeriesSummary oldItem, @org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.TVSeriesSummary newItem) {
            return false;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B7\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u0012\u0014\u0010\b\u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\u0014\u001a\u00020\u00072\u0006\u0010\u0015\u001a\u00020\u0006R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\b\u001a\u0010\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/tvplayer/webdav/ui/home/TVSeriesAdapter$TVSeriesViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "onSeriesClick", "Lkotlin/Function1;", "Lcom/tvplayer/webdav/data/model/TVSeriesSummary;", "", "onItemFocused", "(Landroid/view/View;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "ivPoster", "Landroid/widget/ImageView;", "layoutProgress", "progressBar", "Landroid/widget/ProgressBar;", "tvProgress", "Landroid/widget/TextView;", "tvRating", "tvSubtitle", "tvTitle", "bind", "series", "app_debug"})
    public static final class TVSeriesViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onSeriesClick = null;
        @org.jetbrains.annotations.Nullable()
        private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onItemFocused = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivPoster = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTitle = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvSubtitle = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvRating = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ProgressBar progressBar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvProgress = null;
        @org.jetbrains.annotations.NotNull()
        private final android.view.View layoutProgress = null;
        
        public TVSeriesViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onSeriesClick, @org.jetbrains.annotations.Nullable()
        kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.TVSeriesSummary, kotlin.Unit> onItemFocused) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.TVSeriesSummary series) {
        }
    }
}