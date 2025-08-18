package com.tvplayer.webdav.data.model;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;
import java.util.Date;

/**
 * 媒体类型枚举
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Lcom/tvplayer/webdav/data/model/MediaType;", "", "(Ljava/lang/String;I)V", "MOVIE", "TV_SERIES", "TV_EPISODE", "DOCUMENTARY", "ANIMATION", "OTHER", "app_debug"})
public enum MediaType {
    /*public static final*/ MOVIE /* = new MOVIE() */,
    /*public static final*/ TV_SERIES /* = new TV_SERIES() */,
    /*public static final*/ TV_EPISODE /* = new TV_EPISODE() */,
    /*public static final*/ DOCUMENTARY /* = new DOCUMENTARY() */,
    /*public static final*/ ANIMATION /* = new ANIMATION() */,
    /*public static final*/ OTHER /* = new OTHER() */;
    
    MediaType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.tvplayer.webdav.data.model.MediaType> getEntries() {
        return null;
    }
}