package com.tvplayer.webdav.ui.details;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.Actor;
import com.tvplayer.webdav.data.model.MediaItem;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * 视频详情页面Fragment
 * 根据设计图实现视频详情页面布局
 */
@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\b\u0007\u0018\u0000 42\u00020\u0001:\u00014B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020\u0004H\u0002J\b\u0010#\u001a\u00020!H\u0002J\b\u0010$\u001a\u00020!H\u0002J\u0012\u0010%\u001a\u00020!2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J&\u0010(\u001a\u0004\u0018\u00010\u00042\u0006\u0010)\u001a\u00020*2\b\u0010+\u001a\u0004\u0018\u00010,2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J\u001a\u0010-\u001a\u00020!2\u0006\u0010\"\u001a\u00020\u00042\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J\b\u0010.\u001a\u00020!H\u0002J\b\u0010/\u001a\u00020!H\u0002J\b\u00100\u001a\u00020!H\u0002J\u0010\u00101\u001a\u00020!2\u0006\u0010\"\u001a\u00020\u0004H\u0002J\b\u00102\u001a\u00020!H\u0002J\b\u00103\u001a\u00020!H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u001a\u001a\u00020\u001b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001e\u0010\u001f\u001a\u0004\b\u001c\u0010\u001d\u00a8\u00065"}, d2 = {"Lcom/tvplayer/webdav/ui/details/VideoDetailsFragment;", "Landroidx/fragment/app/Fragment;", "()V", "btnBackToTop", "Landroid/view/View;", "btnPlay", "Landroid/widget/Button;", "ivBackdrop", "Landroid/widget/ImageView;", "mediaItem", "Lcom/tvplayer/webdav/data/model/MediaItem;", "movieInfoContainer", "Landroid/widget/LinearLayout;", "rvActors", "Landroidx/recyclerview/widget/RecyclerView;", "tvDuration", "Landroid/widget/TextView;", "tvDurationSize", "tvFileSize", "tvFilename", "tvGenre", "tvOverview", "tvRating", "tvSourcePath", "tvTitle", "tvYear", "viewModel", "Lcom/tvplayer/webdav/ui/details/VideoDetailsViewModel;", "getViewModel", "()Lcom/tvplayer/webdav/ui/details/VideoDetailsViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "initViews", "", "view", "loadBackdropImage", "observeViewModel", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onCreateView", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "onViewCreated", "scrollToTop", "setupActorsIfAvailable", "setupData", "setupListeners", "setupVideoDetailsIfAvailable", "startPlayback", "Companion", "app_debug"})
public final class VideoDetailsFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.tvplayer.webdav.data.model.MediaItem mediaItem;
    private android.widget.ImageView ivBackdrop;
    private android.widget.TextView tvTitle;
    private android.widget.Button btnPlay;
    private android.widget.TextView tvRating;
    private android.widget.TextView tvYear;
    private android.widget.TextView tvDuration;
    private android.widget.TextView tvGenre;
    private android.widget.TextView tvFileSize;
    private android.widget.TextView tvOverview;
    private android.widget.LinearLayout movieInfoContainer;
    @org.jetbrains.annotations.Nullable()
    private androidx.recyclerview.widget.RecyclerView rvActors;
    @org.jetbrains.annotations.Nullable()
    private android.widget.TextView tvFilename;
    @org.jetbrains.annotations.Nullable()
    private android.widget.TextView tvSourcePath;
    @org.jetbrains.annotations.Nullable()
    private android.widget.TextView tvDurationSize;
    @org.jetbrains.annotations.Nullable()
    private android.view.View btnBackToTop;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ARG_MEDIA_ITEM = "media_item";
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.details.VideoDetailsFragment.Companion Companion = null;
    
    public VideoDetailsFragment() {
        super();
    }
    
    private final com.tvplayer.webdav.ui.details.VideoDetailsViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initViews(android.view.View view) {
    }
    
    private final void setupData() {
    }
    
    private final void loadBackdropImage() {
    }
    
    private final void setupListeners(android.view.View view) {
    }
    
    private final void observeViewModel() {
    }
    
    private final void startPlayback() {
    }
    
    private final void setupActorsIfAvailable() {
    }
    
    private final void setupVideoDetailsIfAvailable() {
    }
    
    private final void scrollToTop() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/tvplayer/webdav/ui/details/VideoDetailsFragment$Companion;", "", "()V", "ARG_MEDIA_ITEM", "", "newInstance", "Lcom/tvplayer/webdav/ui/details/VideoDetailsFragment;", "mediaItem", "Lcom/tvplayer/webdav/data/model/MediaItem;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.tvplayer.webdav.ui.details.VideoDetailsFragment newInstance(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.MediaItem mediaItem) {
            return null;
        }
    }
}