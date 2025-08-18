package com.tvplayer.webdav.data.scanner;

import android.util.Log;
import com.tvplayer.webdav.data.model.MediaItem;
import com.tvplayer.webdav.data.tmdb.TmdbClient;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import com.tvplayer.webdav.data.model.WebDAVFile;
import kotlinx.coroutines.Dispatchers;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 媒体扫描器
 * 智能扫描WebDAV目录，识别和刮削媒体文件
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u0000 \u001c2\u00020\u0001:\u0003\u001c\u001d\u001eB\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010\fJ\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000bH\u0082@\u00a2\u0006\u0002\u0010\fJ\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u000bH\u0002J>\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\b2\u0006\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\u0013\u001a\u00020\u000f2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J$\u0010\u0019\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u001a\u001a\u00020\t2\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0082@\u00a2\u0006\u0002\u0010\u001bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/tvplayer/webdav/data/scanner/MediaScanner;", "", "webdavClient", "Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "tmdbClient", "Lcom/tvplayer/webdav/data/tmdb/TmdbClient;", "(Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;Lcom/tvplayer/webdav/data/tmdb/TmdbClient;)V", "getAllFilesRecursively", "", "Lcom/tvplayer/webdav/data/model/WebDAVFile;", "path", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFilesInDirectory", "isVideoFile", "", "fileName", "scanDirectory", "Lcom/tvplayer/webdav/data/model/MediaItem;", "recursive", "callback", "Lcom/tvplayer/webdav/data/scanner/MediaScanner$ScanProgressCallback;", "modeHint", "Lcom/tvplayer/webdav/data/scanner/MediaScanner$ModeHint;", "(Ljava/lang/String;ZLcom/tvplayer/webdav/data/scanner/MediaScanner$ScanProgressCallback;Lcom/tvplayer/webdav/data/scanner/MediaScanner$ModeHint;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "scrapeMediaFile", "file", "(Lcom/tvplayer/webdav/data/model/WebDAVFile;Lcom/tvplayer/webdav/data/scanner/MediaScanner$ModeHint;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "ModeHint", "ScanProgressCallback", "app_debug"})
public final class MediaScanner {
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.tmdb.TmdbClient tmdbClient = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "MediaScanner";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> VIDEO_EXTENSIONS = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.util.regex.Pattern> TV_SEASON_EPISODE_PATTERNS = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.String> TV_DIRECTORY_KEYWORDS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.data.scanner.MediaScanner.Companion Companion = null;
    
    @javax.inject.Inject()
    public MediaScanner(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.tmdb.TmdbClient tmdbClient) {
        super();
    }
    
    /**
     * 扫描WebDAV目录
     * @param path 要扫描的路径
     * @param recursive 是否递归扫描子目录
     * @param callback 进度回调
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object scanDirectory(@org.jetbrains.annotations.NotNull()
    java.lang.String path, boolean recursive, @org.jetbrains.annotations.Nullable()
    com.tvplayer.webdav.data.scanner.MediaScanner.ScanProgressCallback callback, @org.jetbrains.annotations.Nullable()
    com.tvplayer.webdav.data.scanner.MediaScanner.ModeHint modeHint, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.tvplayer.webdav.data.model.MediaItem>> $completion) {
        return null;
    }
    
    /**
     * 递归获取所有文件
     */
    private final java.lang.Object getAllFilesRecursively(java.lang.String path, kotlin.coroutines.Continuation<? super java.util.List<com.tvplayer.webdav.data.model.WebDAVFile>> $completion) {
        return null;
    }
    
    /**
     * 获取目录中的文件
     */
    private final java.lang.Object getFilesInDirectory(java.lang.String path, kotlin.coroutines.Continuation<? super java.util.List<com.tvplayer.webdav.data.model.WebDAVFile>> $completion) {
        return null;
    }
    
    /**
     * 刮削单个媒体文件
     */
    private final java.lang.Object scrapeMediaFile(com.tvplayer.webdav.data.model.WebDAVFile file, com.tvplayer.webdav.data.scanner.MediaScanner.ModeHint modeHint, kotlin.coroutines.Continuation<? super com.tvplayer.webdav.data.model.MediaItem> $completion) {
        return null;
    }
    
    /**
     * 检查是否为视频文件
     */
    private final boolean isVideoFile(java.lang.String fileName) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\"\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0010\u0012\f\u0012\n \n*\u0004\u0018\u00010\t0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/tvplayer/webdav/data/scanner/MediaScanner$Companion;", "", "()V", "TAG", "", "TV_DIRECTORY_KEYWORDS", "", "TV_SEASON_EPISODE_PATTERNS", "", "Ljava/util/regex/Pattern;", "kotlin.jvm.PlatformType", "VIDEO_EXTENSIONS", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/tvplayer/webdav/data/scanner/MediaScanner$ModeHint;", "", "(Ljava/lang/String;I)V", "MOVIE", "TV", "app_debug"})
    public static enum ModeHint {
        /*public static final*/ MOVIE /* = new MOVIE() */,
        /*public static final*/ TV /* = new TV() */;
        
        ModeHint() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.tvplayer.webdav.data.scanner.MediaScanner.ModeHint> getEntries() {
            return null;
        }
    }
    
    /**
     * 扫描进度回调
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H&J\u0010\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH&J \u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\tH&\u00a8\u0006\u000f"}, d2 = {"Lcom/tvplayer/webdav/data/scanner/MediaScanner$ScanProgressCallback;", "", "onComplete", "", "scannedItems", "", "Lcom/tvplayer/webdav/data/model/MediaItem;", "onError", "error", "", "onProgress", "current", "", "total", "currentFile", "app_debug"})
    public static abstract interface ScanProgressCallback {
        
        public abstract void onProgress(int current, int total, @org.jetbrains.annotations.NotNull()
        java.lang.String currentFile);
        
        public abstract void onComplete(@org.jetbrains.annotations.NotNull()
        java.util.List<com.tvplayer.webdav.data.model.MediaItem> scannedItems);
        
        public abstract void onError(@org.jetbrains.annotations.NotNull()
        java.lang.String error);
    }
}