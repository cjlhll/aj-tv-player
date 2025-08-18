package com.tvplayer.webdav.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.MediaCategory;
import com.tvplayer.webdav.data.model.MediaItem;
import com.tvplayer.webdav.data.model.MediaType;
import com.tvplayer.webdav.data.storage.WebDAVServerStorage;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import com.tvplayer.webdav.data.scanner.MediaScanner;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.Date;
import javax.inject.Inject;

/**
 * 主界面ViewModel
 * 管理主界面的数据和状态
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\'\u001a\u00020(J\b\u0010)\u001a\u00020(H\u0002J\u0006\u0010*\u001a\u00020(J\b\u0010+\u001a\u00020(H\u0002J\u0006\u0010,\u001a\u00020(J\u0006\u0010-\u001a\u00020(R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001d\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001bR\u0019\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001bR\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00140\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001bR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001bR\u001d\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/tvplayer/webdav/ui/home/HomeViewModel;", "Landroidx/lifecycle/ViewModel;", "webdavClient", "Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "serverStorage", "Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;", "mediaScanner", "Lcom/tvplayer/webdav/data/scanner/MediaScanner;", "mediaCache", "Lcom/tvplayer/webdav/data/storage/MediaCache;", "(Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;Lcom/tvplayer/webdav/data/scanner/MediaScanner;Lcom/tvplayer/webdav/data/storage/MediaCache;)V", "_categories", "Landroidx/lifecycle/MutableLiveData;", "", "Lcom/tvplayer/webdav/data/model/MediaCategory;", "_continueWatching", "Lcom/tvplayer/webdav/data/model/MediaItem;", "_error", "", "_isLoading", "", "_movies", "_recentlyAdded", "_tvShows", "categories", "Landroidx/lifecycle/LiveData;", "getCategories", "()Landroidx/lifecycle/LiveData;", "continueWatching", "getContinueWatching", "error", "getError", "isLoading", "movies", "getMovies", "recentlyAdded", "getRecentlyAdded", "tvShows", "getTvShows", "clearError", "", "loadCategories", "loadHomeData", "loadSampleData", "refresh", "rescanAndScrape", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.scanner.MediaScanner mediaScanner = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.storage.MediaCache mediaCache = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaCategory>> _categories = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaCategory>> categories = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> _recentlyAdded = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> recentlyAdded = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> _continueWatching = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> continueWatching = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> _movies = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> movies = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> _tvShows = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> tvShows = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> error = null;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.scanner.MediaScanner mediaScanner, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.storage.MediaCache mediaCache) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaCategory>> getCategories() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> getRecentlyAdded() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> getContinueWatching() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> getMovies() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> getTvShows() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getError() {
        return null;
    }
    
    /**
     * 加载主界面数据
     */
    public final void loadHomeData() {
    }
    
    /**
     * 加载分类数据
     */
    private final void loadCategories() {
    }
    
    /**
     * 加载示例数据（用于演示）
     */
    private final void loadSampleData() {
    }
    
    /**
     * 刷新数据
     */
    public final void refresh() {
    }
    
    /**
     * 重新扫描并刮削媒体库，更新缓存供首页使用
     */
    public final void rescanAndScrape() {
    }
    
    /**
     * 清除错误状态
     */
    public final void clearError() {
    }
}