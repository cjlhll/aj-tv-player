package com.tvplayer.webdav.di

import android.content.SharedPreferences
import com.tvplayer.webdav.data.storage.MediaCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {
    @Provides
    @Singleton
    fun provideMediaCache(prefs: SharedPreferences): MediaCache = MediaCache(prefs)
}

