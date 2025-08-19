package com.tvplayer.webdav.data.storage;

import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tvplayer.webdav.data.model.MediaItem;
import com.tvplayer.webdav.data.model.TVSeriesSummary;
import com.tvplayer.webdav.data.model.MediaType;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0012\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\fJ\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u001c\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00072\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002J\u000e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002J\u0012\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\fJ\u0016\u0010\u0013\u001a\u00020\u00142\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u0002J\u0012\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\fJ\u0014\u0010\u0016\u001a\u00020\u00142\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u0012\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00070\fJ\u0012\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\fR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/tvplayer/webdav/data/storage/MediaCache;", "", "prefs", "Landroid/content/SharedPreferences;", "(Landroid/content/SharedPreferences;)V", "_allItems", "Landroidx/lifecycle/MutableLiveData;", "", "Lcom/tvplayer/webdav/data/model/MediaItem;", "gson", "Lcom/google/gson/Gson;", "allItems", "Landroidx/lifecycle/LiveData;", "getItems", "groupTVEpisodesBySeries", "Lcom/tvplayer/webdav/data/model/TVSeriesSummary;", "items", "loadPersistedItems", "movies", "persistItems", "", "recentlyAdded", "setItems", "tvSeriesSummaries", "tvShows", "Companion", "app_debug"})
public final class MediaCache {
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_CACHE_ALL = "media_cache_all_items";
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> _allItems = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.data.storage.MediaCache.Companion Companion = null;
    
    @javax.inject.Inject()
    public MediaCache(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences prefs) {
        super();
    }
    
    private final java.util.List<com.tvplayer.webdav.data.model.MediaItem> loadPersistedItems() {
        return null;
    }
    
    private final void persistItems(java.util.List<com.tvplayer.webdav.data.model.MediaItem> items) {
    }
    
    public final void setItems(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tvplayer.webdav.data.model.MediaItem> items) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tvplayer.webdav.data.model.MediaItem> getItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> allItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> movies() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> tvShows() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.TVSeriesSummary>> tvSeriesSummaries() {
        return null;
    }
    
    private final java.util.List<com.tvplayer.webdav.data.model.TVSeriesSummary> groupTVEpisodesBySeries(java.util.List<com.tvplayer.webdav.data.model.MediaItem> items) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.tvplayer.webdav.data.model.MediaItem>> recentlyAdded() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/tvplayer/webdav/data/storage/MediaCache$Companion;", "", "()V", "KEY_CACHE_ALL", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}