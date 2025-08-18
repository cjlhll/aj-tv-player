package com.tvplayer.webdav.data.model;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;
import java.util.Date;

/**
 * 媒体项目数据模型
 * 支持电影、电视剧、单集等不同类型
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b<\n\u0002\u0010\u0000\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u0085\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u000e\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0015\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0019\u001a\u00020\f\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\n\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u001b\u0012\u000e\b\u0002\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\u001f\u0012\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00030\u001f\u00a2\u0006\u0002\u0010!J\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\t\u0010A\u001a\u00020\u0010H\u00c6\u0003J\t\u0010B\u001a\u00020\u0003H\u00c6\u0003J\t\u0010C\u001a\u00020\u000eH\u00c6\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u0010\u0010E\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010\'J\u0010\u0010F\u001a\u0004\u0018\u00010\u0015H\u00c6\u0003\u00a2\u0006\u0002\u0010\'J\u000b\u0010G\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010H\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\fH\u00c6\u0003J\t\u0010J\u001a\u00020\u001bH\u00c6\u0003J\t\u0010K\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010L\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\t\u0010M\u001a\u00020\u001bH\u00c6\u0003J\u000f\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00030\u001fH\u00c6\u0003J\u000f\u0010O\u001a\b\u0012\u0004\u0012\u00020\u00030\u001fH\u00c6\u0003J\u000b\u0010P\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010Q\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010R\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010S\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010T\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\t\u0010U\u001a\u00020\fH\u00c6\u0003J\t\u0010V\u001a\u00020\u000eH\u00c6\u0003J\u0096\u0002\u0010W\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00032\b\b\u0002\u0010\u0012\u001a\u00020\u000e2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00152\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0019\u001a\u00020\f2\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\n2\b\b\u0002\u0010\u001d\u001a\u00020\u001b2\u000e\b\u0002\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\u001f2\u000e\b\u0002\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00030\u001fH\u00c6\u0001\u00a2\u0006\u0002\u0010XJ\t\u0010Y\u001a\u00020\u0015H\u00d6\u0001J\u0013\u0010Z\u001a\u00020\u001b2\b\u0010[\u001a\u0004\u0018\u00010\\H\u00d6\u0003J\u0006\u0010]\u001a\u00020\u0003J\u0006\u0010^\u001a\u00020\u0003J\b\u0010_\u001a\u0004\u0018\u00010\u0003J\u0006\u0010`\u001a\u00020\u0015J\t\u0010a\u001a\u00020\u0015H\u00d6\u0001J\u0006\u0010b\u001a\u00020\u001bJ\t\u0010c\u001a\u00020\u0003H\u00d6\u0001J\u0019\u0010d\u001a\u00020e2\u0006\u0010f\u001a\u00020g2\u0006\u0010h\u001a\u00020\u0015H\u00d6\u0001R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\n\n\u0002\u0010(\u001a\u0004\b&\u0010\'R\u0011\u0010\u0011\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010#R\u0011\u0010\u0012\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010%R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00030\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010,R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010#R\u0011\u0010\u001d\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010.R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010.R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0013\u0010\u001c\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00100R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010#R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010#R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010#R\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00108R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u00100R\u0015\u0010\u0014\u001a\u0004\u0018\u00010\u0015\u00a2\u0006\n\n\u0002\u0010(\u001a\u0004\b:\u0010\'R\u0013\u0010\u0017\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010#R\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010#R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00030\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010,R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010#R\u0011\u0010\u0019\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u00108\u00a8\u0006i"}, d2 = {"Lcom/tvplayer/webdav/data/model/MediaItem;", "Landroid/os/Parcelable;", "id", "", "title", "originalTitle", "overview", "posterPath", "backdropPath", "releaseDate", "Ljava/util/Date;", "rating", "", "duration", "", "mediaType", "Lcom/tvplayer/webdav/data/model/MediaType;", "filePath", "fileSize", "lastModified", "seasonNumber", "", "episodeNumber", "seriesId", "seriesTitle", "watchedProgress", "isWatched", "", "lastWatchedTime", "isFavorite", "tags", "", "genre", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Date;FJLcom/tvplayer/webdav/data/model/MediaType;Ljava/lang/String;JLjava/util/Date;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;FZLjava/util/Date;ZLjava/util/List;Ljava/util/List;)V", "getBackdropPath", "()Ljava/lang/String;", "getDuration", "()J", "getEpisodeNumber", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getFilePath", "getFileSize", "getGenre", "()Ljava/util/List;", "getId", "()Z", "getLastModified", "()Ljava/util/Date;", "getLastWatchedTime", "getMediaType", "()Lcom/tvplayer/webdav/data/model/MediaType;", "getOriginalTitle", "getOverview", "getPosterPath", "getRating", "()F", "getReleaseDate", "getSeasonNumber", "getSeriesId", "getSeriesTitle", "getTags", "getTitle", "getWatchedProgress", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Date;FJLcom/tvplayer/webdav/data/model/MediaType;Ljava/lang/String;JLjava/util/Date;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;FZLjava/util/Date;ZLjava/util/List;Ljava/util/List;)Lcom/tvplayer/webdav/data/model/MediaItem;", "describeContents", "equals", "other", "", "getDisplayTitle", "getFormattedFileSize", "getSubtitle", "getWatchedPercentage", "hashCode", "isNew", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "app_debug"})
@kotlinx.parcelize.Parcelize()
public final class MediaItem implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String originalTitle = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String overview = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String posterPath = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String backdropPath = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.Date releaseDate = null;
    private final float rating = 0.0F;
    private final long duration = 0L;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.model.MediaType mediaType = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String filePath = null;
    private final long fileSize = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.util.Date lastModified = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer seasonNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer episodeNumber = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String seriesId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String seriesTitle = null;
    private final float watchedProgress = 0.0F;
    private final boolean isWatched = false;
    @org.jetbrains.annotations.Nullable()
    private final java.util.Date lastWatchedTime = null;
    private final boolean isFavorite = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> tags = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> genre = null;
    
    public MediaItem(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String originalTitle, @org.jetbrains.annotations.Nullable()
    java.lang.String overview, @org.jetbrains.annotations.Nullable()
    java.lang.String posterPath, @org.jetbrains.annotations.Nullable()
    java.lang.String backdropPath, @org.jetbrains.annotations.Nullable()
    java.util.Date releaseDate, float rating, long duration, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.MediaType mediaType, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, long fileSize, @org.jetbrains.annotations.Nullable()
    java.util.Date lastModified, @org.jetbrains.annotations.Nullable()
    java.lang.Integer seasonNumber, @org.jetbrains.annotations.Nullable()
    java.lang.Integer episodeNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String seriesId, @org.jetbrains.annotations.Nullable()
    java.lang.String seriesTitle, float watchedProgress, boolean isWatched, @org.jetbrains.annotations.Nullable()
    java.util.Date lastWatchedTime, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> genre) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getOriginalTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getOverview() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPosterPath() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getBackdropPath() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getReleaseDate() {
        return null;
    }
    
    public final float getRating() {
        return 0.0F;
    }
    
    public final long getDuration() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tvplayer.webdav.data.model.MediaType getMediaType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFilePath() {
        return null;
    }
    
    public final long getFileSize() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getLastModified() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getSeasonNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getEpisodeNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSeriesId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSeriesTitle() {
        return null;
    }
    
    public final float getWatchedProgress() {
        return 0.0F;
    }
    
    public final boolean isWatched() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date getLastWatchedTime() {
        return null;
    }
    
    public final boolean isFavorite() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getTags() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getGenre() {
        return null;
    }
    
    /**
     * 获取显示标题
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayTitle() {
        return null;
    }
    
    /**
     * 获取副标题
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSubtitle() {
        return null;
    }
    
    /**
     * 获取格式化的文件大小
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedFileSize() {
        return null;
    }
    
    /**
     * 获取观看进度百分比
     */
    public final int getWatchedPercentage() {
        return 0;
    }
    
    /**
     * 是否为新内容（最近添加）
     */
    public final boolean isNew() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tvplayer.webdav.data.model.MediaType component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    public final long component12() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component17() {
        return null;
    }
    
    public final float component18() {
        return 0.0F;
    }
    
    public final boolean component19() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component20() {
        return null;
    }
    
    public final boolean component21() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component22() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.Date component7() {
        return null;
    }
    
    public final float component8() {
        return 0.0F;
    }
    
    public final long component9() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tvplayer.webdav.data.model.MediaItem copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.Nullable()
    java.lang.String originalTitle, @org.jetbrains.annotations.Nullable()
    java.lang.String overview, @org.jetbrains.annotations.Nullable()
    java.lang.String posterPath, @org.jetbrains.annotations.Nullable()
    java.lang.String backdropPath, @org.jetbrains.annotations.Nullable()
    java.util.Date releaseDate, float rating, long duration, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.MediaType mediaType, @org.jetbrains.annotations.NotNull()
    java.lang.String filePath, long fileSize, @org.jetbrains.annotations.Nullable()
    java.util.Date lastModified, @org.jetbrains.annotations.Nullable()
    java.lang.Integer seasonNumber, @org.jetbrains.annotations.Nullable()
    java.lang.Integer episodeNumber, @org.jetbrains.annotations.Nullable()
    java.lang.String seriesId, @org.jetbrains.annotations.Nullable()
    java.lang.String seriesTitle, float watchedProgress, boolean isWatched, @org.jetbrains.annotations.Nullable()
    java.util.Date lastWatchedTime, boolean isFavorite, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> genre) {
        return null;
    }
    
    @java.lang.Override()
    public int describeContents() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public void writeToParcel(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel, int flags) {
    }
}