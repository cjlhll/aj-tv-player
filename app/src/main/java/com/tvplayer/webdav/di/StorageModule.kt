package com.tvplayer.webdav.di

import android.content.Context
import android.content.SharedPreferences
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("webdav_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideWebDAVServerStorage(prefs: SharedPreferences): WebDAVServerStorage {
        return WebDAVServerStorage(prefs)
    }
}

