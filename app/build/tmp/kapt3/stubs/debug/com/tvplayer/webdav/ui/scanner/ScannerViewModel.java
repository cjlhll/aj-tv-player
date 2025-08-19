package com.tvplayer.webdav.ui.scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tvplayer.webdav.data.model.MediaItem;
import com.tvplayer.webdav.data.model.MediaType;
import com.tvplayer.webdav.data.scanner.MediaScanner;
import com.tvplayer.webdav.data.model.WebDAVFile;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import com.tvplayer.webdav.data.storage.WebDAVServerStorage;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

/**
 * 扫描器ViewModel
 * 管理媒体扫描的状态和进度
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0010\u0002\n\u0002\b\u0013\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010(\u001a\u00020\u0013J\u0006\u0010)\u001a\u00020*J\u000e\u0010+\u001a\u00020*2\u0006\u0010,\u001a\u00020\u0011J\b\u0010-\u001a\u0004\u0018\u00010\rJ\b\u0010.\u001a\u0004\u0018\u00010\rJ\u0006\u0010/\u001a\u00020*J\u0006\u00100\u001a\u00020*J\u000e\u00101\u001a\u00020*2\u0006\u00102\u001a\u00020\rJ\u000e\u00103\u001a\u00020*2\u0006\u00102\u001a\u00020\rJ \u00104\u001a\u00020*2\u0006\u00102\u001a\u00020\r2\b\b\u0002\u00105\u001a\u00020\u0013H\u0086@\u00a2\u0006\u0002\u00106J\u0006\u00107\u001a\u00020*J\u001a\u00108\u001a\u00020*2\b\u00109\u001a\u0004\u0018\u00010\r2\b\u0010:\u001a\u0004\u0018\u00010\rJ\u0006\u0010;\u001a\u00020*J\u0006\u0010<\u001a\u00020*R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\u0015\u001a\u0016\u0012\u0012\u0012\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u0010\u0018\u00010\u00160\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001cR\u001d\u0010\u001f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001cR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00130\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001cR\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001cR%\u0010$\u001a\u0016\u0012\u0012\u0012\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u0010\u0018\u00010\u00160\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001cR\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001cR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006="}, d2 = {"Lcom/tvplayer/webdav/ui/scanner/ScannerViewModel;", "Landroidx/lifecycle/ViewModel;", "mediaScanner", "Lcom/tvplayer/webdav/data/scanner/MediaScanner;", "webdavClient", "Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "serverStorage", "Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;", "mediaCache", "Lcom/tvplayer/webdav/data/storage/MediaCache;", "(Lcom/tvplayer/webdav/data/scanner/MediaScanner;Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;Lcom/tvplayer/webdav/data/storage/MediaCache;)V", "_browseError", "Landroidx/lifecycle/MutableLiveData;", "", "_currentPath", "_directoryItems", "", "Lcom/tvplayer/webdav/data/model/WebDAVFile;", "_isScanning", "", "_scanProgress", "_scanResult", "Lkotlin/Result;", "Lcom/tvplayer/webdav/data/model/MediaItem;", "_scanStatus", "browseError", "Landroidx/lifecycle/LiveData;", "getBrowseError", "()Landroidx/lifecycle/LiveData;", "currentPath", "getCurrentPath", "directoryItems", "getDirectoryItems", "isScanning", "scanProgress", "getScanProgress", "scanResult", "getScanResult", "scanStatus", "getScanStatus", "canStartScan", "clearScanResult", "", "enterDirectory", "dir", "getMoviesDir", "getTvDir", "goUp", "loadCurrentDirectory", "setMoviesDir", "path", "setTvDir", "startScan", "recursive", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startScanMovies", "startScanMoviesAndTv", "moviesDir", "tvDir", "startScanTv", "stopScan", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ScannerViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.scanner.MediaScanner mediaScanner = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.storage.MediaCache mediaCache = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _currentPath = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> currentPath = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.WebDAVFile>> _directoryItems = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.WebDAVFile>> directoryItems = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _browseError = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> browseError = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Boolean> isScanning = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _scanProgress = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> scanProgress = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _scanStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> scanStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<kotlin.Result<java.util.List<com.tvplayer.webdav.data.model.MediaItem>>> _scanResult = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<kotlin.Result<java.util.List<com.tvplayer.webdav.data.model.MediaItem>>> scanResult = null;
    
    @javax.inject.Inject()
    public ScannerViewModel(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.scanner.MediaScanner mediaScanner, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.storage.MediaCache mediaCache) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getCurrentPath() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.WebDAVFile>> getDirectoryItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getBrowseError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isScanning() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getScanProgress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getScanStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<kotlin.Result<java.util.List<com.tvplayer.webdav.data.model.MediaItem>>> getScanResult() {
        return null;
    }
    
    public final boolean canStartScan() {
        return false;
    }
    
    /**
     * 扫描电影目录
     */
    public final void startScanMovies() {
    }
    
    /**
     * 扫描电视剧目录
     */
    public final void startScanTv() {
    }
    
    /**
     * 扫描电影和电视剧目录
     */
    public final void startScanMoviesAndTv(@org.jetbrains.annotations.Nullable()
    java.lang.String moviesDir, @org.jetbrains.annotations.Nullable()
    java.lang.String tvDir) {
    }
    
    /**
     * 开始扫描
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object startScan(@org.jetbrains.annotations.NotNull()
    java.lang.String path, boolean recursive, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 停止扫描
     */
    public final void stopScan() {
    }
    
    /**
     * 清除扫描结果
     */
    public final void clearScanResult() {
    }
    
    /**
     * 连接并列出当前路径
     */
    public final void loadCurrentDirectory() {
    }
    
    /**
     * 进入子目录
     */
    public final void enterDirectory(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.WebDAVFile dir) {
    }
    
    public final void setMoviesDir(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
    }
    
    public final void setTvDir(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMoviesDir() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTvDir() {
        return null;
    }
    
    /**
     * 返回上级目录
     */
    public final void goUp() {
    }
}