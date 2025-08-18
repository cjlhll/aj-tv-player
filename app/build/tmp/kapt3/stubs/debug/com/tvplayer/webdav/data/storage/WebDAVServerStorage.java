package com.tvplayer.webdav.data.storage;

import android.content.SharedPreferences;
import com.tvplayer.webdav.data.model.WebDAVServer;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 使用SharedPreferences保存/读取WebDAV服务器配置
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006J\b\u0010\u0007\u001a\u0004\u0018\u00010\bJ\b\u0010\t\u001a\u0004\u0018\u00010\nJ\b\u0010\u000b\u001a\u0004\u0018\u00010\bJ\u000e\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\nJ\u0010\u0010\u000e\u001a\u00020\u00062\b\u0010\u000f\u001a\u0004\u0018\u00010\bJ\u0010\u0010\u0010\u001a\u00020\u00062\b\u0010\u000f\u001a\u0004\u0018\u00010\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;", "", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/content/SharedPreferences;)V", "clear", "", "getMoviesDir", "", "getServer", "Lcom/tvplayer/webdav/data/model/WebDAVServer;", "getTvDir", "saveServer", "server", "setMoviesDir", "path", "setTvDir", "Companion", "app_debug"})
public final class WebDAVServerStorage {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_NAME = "webdav_name";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_URL = "webdav_url";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_USERNAME = "webdav_username";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PASSWORD = "webdav_password";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_IS_DEFAULT = "webdav_is_default";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_IS_ACTIVE = "webdav_is_active";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_MOVIES_DIR = "webdav_movies_dir";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TV_DIR = "webdav_tv_dir";
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.data.storage.WebDAVServerStorage.Companion Companion = null;
    
    @javax.inject.Inject()
    public WebDAVServerStorage(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super();
    }
    
    public final void saveServer(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.WebDAVServer server) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tvplayer.webdav.data.model.WebDAVServer getServer() {
        return null;
    }
    
    public final void clear() {
    }
    
    public final void setMoviesDir(@org.jetbrains.annotations.Nullable()
    java.lang.String path) {
    }
    
    public final void setTvDir(@org.jetbrains.annotations.Nullable()
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage$Companion;", "", "()V", "KEY_IS_ACTIVE", "", "KEY_IS_DEFAULT", "KEY_MOVIES_DIR", "KEY_NAME", "KEY_PASSWORD", "KEY_TV_DIR", "KEY_URL", "KEY_USERNAME", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}