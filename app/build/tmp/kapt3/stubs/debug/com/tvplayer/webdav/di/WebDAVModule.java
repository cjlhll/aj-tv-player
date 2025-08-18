package com.tvplayer.webdav.di;

import com.tvplayer.webdav.data.tmdb.TmdbApiService;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * 网络和API相关的依赖注入模块
 */
@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\b\u0010\u0007\u001a\u00020\bH\u0007J\u0012\u0010\t\u001a\u00020\u00062\b\b\u0001\u0010\n\u001a\u00020\bH\u0007J\u0012\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\n\u001a\u00020\bH\u0007J\b\u0010\r\u001a\u00020\bH\u0007\u00a8\u0006\u000e"}, d2 = {"Lcom/tvplayer/webdav/di/WebDAVModule;", "", "()V", "provideTmdbApiService", "Lcom/tvplayer/webdav/data/tmdb/TmdbApiService;", "retrofit", "Lretrofit2/Retrofit;", "provideTmdbOkHttpClient", "Lokhttp3/OkHttpClient;", "provideTmdbRetrofit", "okHttpClient", "provideWebDAVClient", "Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "provideWebDAVOkHttpClient", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class WebDAVModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.di.WebDAVModule INSTANCE = null;
    
    private WebDAVModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @WebDAVClient()
    @org.jetbrains.annotations.NotNull()
    public final okhttp3.OkHttpClient provideWebDAVOkHttpClient() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @TmdbClient()
    @org.jetbrains.annotations.NotNull()
    public final okhttp3.OkHttpClient provideTmdbOkHttpClient() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.tvplayer.webdav.data.webdav.SimpleWebDAVClient provideWebDAVClient(@WebDAVClient()
    @org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final retrofit2.Retrofit provideTmdbRetrofit(@TmdbClient()
    @org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient okHttpClient) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.tvplayer.webdav.data.tmdb.TmdbApiService provideTmdbApiService(@org.jetbrains.annotations.NotNull()
    retrofit2.Retrofit retrofit) {
        return null;
    }
}