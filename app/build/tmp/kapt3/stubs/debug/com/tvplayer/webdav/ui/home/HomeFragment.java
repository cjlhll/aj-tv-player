package com.tvplayer.webdav.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.MediaCategory;
import com.tvplayer.webdav.data.model.MediaType;
import com.tvplayer.webdav.ui.settings.SettingsFragment;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * 主界面Fragment - 类似Infuse的海报墙设计
 * 包含分类导航和媒体内容展示
 */
@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 .2\u00020\u0001:\u0001.B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u0019H\u0002J\b\u0010\u001d\u001a\u00020\u0019H\u0002J\u0010\u0010\u001e\u001a\u00020\u00192\u0006\u0010\u001f\u001a\u00020 H\u0002J&\u0010!\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\"\u001a\u00020#2\b\u0010$\u001a\u0004\u0018\u00010%2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J\u0010\u0010(\u001a\u00020\u00192\u0006\u0010)\u001a\u00020*H\u0002J\u0010\u0010+\u001a\u00020\u00192\u0006\u0010)\u001a\u00020*H\u0002J\u001a\u0010,\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010&\u001a\u0004\u0018\u00010\'H\u0016J\b\u0010-\u001a\u00020\u0019H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0012\u001a\u00020\u00138BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0016\u0010\u0017\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006/"}, d2 = {"Lcom/tvplayer/webdav/ui/home/HomeFragment;", "Landroidx/fragment/app/Fragment;", "()V", "categoryAdapter", "Lcom/tvplayer/webdav/ui/home/CategoryAdapter;", "continueWatchingAdapter", "Lcom/tvplayer/webdav/ui/home/MediaPosterAdapter;", "ivBackdrop", "Landroid/widget/ImageView;", "moviesAdapter", "recentlyAddedAdapter", "rvCategories", "Landroidx/recyclerview/widget/RecyclerView;", "rvContinueWatching", "rvMovies", "rvRecentlyAdded", "rvTVShows", "tvShowsAdapter", "viewModel", "Lcom/tvplayer/webdav/ui/home/HomeViewModel;", "getViewModel", "()Lcom/tvplayer/webdav/ui/home/HomeViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "initViews", "", "view", "Landroid/view/View;", "navigateToSettings", "observeViewModel", "onCategoryClick", "category", "Lcom/tvplayer/webdav/data/model/MediaCategory;", "onCreateView", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onMediaItemClick", "mediaItem", "Lcom/tvplayer/webdav/data/model/MediaItem;", "onPosterFocused", "onViewCreated", "setupAdapters", "Companion", "app_debug"})
public final class HomeFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private androidx.recyclerview.widget.RecyclerView rvCategories;
    private androidx.recyclerview.widget.RecyclerView rvRecentlyAdded;
    private androidx.recyclerview.widget.RecyclerView rvContinueWatching;
    private androidx.recyclerview.widget.RecyclerView rvMovies;
    private androidx.recyclerview.widget.RecyclerView rvTVShows;
    @org.jetbrains.annotations.Nullable()
    private android.widget.ImageView ivBackdrop;
    private com.tvplayer.webdav.ui.home.CategoryAdapter categoryAdapter;
    private com.tvplayer.webdav.ui.home.MediaPosterAdapter recentlyAddedAdapter;
    private com.tvplayer.webdav.ui.home.MediaPosterAdapter continueWatchingAdapter;
    private com.tvplayer.webdav.ui.home.MediaPosterAdapter moviesAdapter;
    private com.tvplayer.webdav.ui.home.MediaPosterAdapter tvShowsAdapter;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.home.HomeFragment.Companion Companion = null;
    
    public HomeFragment() {
        super();
    }
    
    private final com.tvplayer.webdav.ui.home.HomeViewModel getViewModel() {
        return null;
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
    
    private final void setupAdapters() {
    }
    
    private final void observeViewModel() {
    }
    
    private final void onCategoryClick(com.tvplayer.webdav.data.model.MediaCategory category) {
    }
    
    private final void onMediaItemClick(com.tvplayer.webdav.data.model.MediaItem mediaItem) {
    }
    
    private final void onPosterFocused(com.tvplayer.webdav.data.model.MediaItem mediaItem) {
    }
    
    private final void navigateToSettings() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/tvplayer/webdav/ui/home/HomeFragment$Companion;", "", "()V", "newInstance", "Lcom/tvplayer/webdav/ui/home/HomeFragment;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.tvplayer.webdav.ui.home.HomeFragment newInstance() {
            return null;
        }
    }
}