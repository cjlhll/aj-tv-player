package com.tvplayer.webdav.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tvplayer.webdav.data.storage.SubtitleCache
import com.tvplayer.webdav.data.subtitle.OpenSubtitlesService
import com.tvplayer.webdav.data.subtitle.SubtitleManager
import com.tvplayer.webdav.data.subtitle.SubtitleMatcher
import com.tvplayer.webdav.di.WebDAVClient
import com.tvplayer.webdav.ui.player.SubtitleController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * 字幕功能依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object SubtitleModule {
    
    /**
     * 提供Gson实例用于JSON序列化
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create()
    }
    
    /**
     * 提供字幕缓存管理器
     */
    @Provides
    @Singleton
    fun provideSubtitleCache(
        @ApplicationContext context: Context,
        gson: Gson
    ): SubtitleCache {
        return SubtitleCache(context, gson)
    }
    
    /**
     * 提供OpenSubtitles服务
     */
    @Provides
    @Singleton
    fun provideOpenSubtitlesService(
        @WebDAVClient httpClient: OkHttpClient
    ): OpenSubtitlesService {
        return OpenSubtitlesService(httpClient)
    }
    
    /**
     * 提供字幕匹配器
     */
    @Provides
    @Singleton
    fun provideSubtitleMatcher(): SubtitleMatcher {
        return SubtitleMatcher()
    }
    
    /**
     * 提供字幕管理器
     */
    @Provides
    @Singleton
    fun provideSubtitleManager(
        @ApplicationContext context: Context,
        openSubtitlesService: OpenSubtitlesService,
        subtitleCache: SubtitleCache
    ): SubtitleManager {
        return SubtitleManager(context, openSubtitlesService, subtitleCache)
    }
    
    /**
     * 提供字幕控制器
     */
    @Provides
    @Singleton
    fun provideSubtitleController(): SubtitleController {
        return SubtitleController()
    }
}