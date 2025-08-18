package com.tvplayer.webdav.data.tmdb;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * TMDB API服务接口
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u0000 $2\u00020\u0001:\u0001$J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J2\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\f\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\rJ(\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0010J<\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u00032\b\b\u0001\u0010\u0013\u001a\u00020\u000b2\b\b\u0001\u0010\u0014\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\f\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0015J2\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u0013\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0003\u0010\f\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\rJ(\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00032\b\b\u0001\u0010\u0013\u001a\u00020\u000b2\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0010JR\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001b\u001a\u00020\u00062\b\b\u0003\u0010\f\u001a\u00020\u00062\b\b\u0003\u0010\u001c\u001a\u00020\u000b2\b\b\u0003\u0010\u001d\u001a\u00020\u001e2\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u000bH\u00a7@\u00a2\u0006\u0002\u0010 JF\u0010!\u001a\b\u0012\u0004\u0012\u00020\"0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u001b\u001a\u00020\u00062\b\b\u0003\u0010\f\u001a\u00020\u00062\b\b\u0003\u0010\u001c\u001a\u00020\u000b2\b\b\u0003\u0010\u001d\u001a\u00020\u001eH\u00a7@\u00a2\u0006\u0002\u0010#\u00a8\u0006%"}, d2 = {"Lcom/tvplayer/webdav/data/tmdb/TmdbApiService;", "", "getConfiguration", "Lretrofit2/Response;", "Lcom/tvplayer/webdav/data/tmdb/TmdbConfiguration;", "apiKey", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMovieDetails", "Lcom/tvplayer/webdav/data/tmdb/TmdbMovie;", "movieId", "", "language", "(ILjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMovieTranslations", "Lcom/tvplayer/webdav/data/tmdb/TmdbTranslationsResponse;", "(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSeasonDetails", "Lcom/tvplayer/webdav/data/tmdb/TmdbSeasonDetails;", "tvId", "seasonNumber", "(IILjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTVShowDetails", "Lcom/tvplayer/webdav/data/tmdb/TmdbTVShow;", "getTVTranslations", "searchMovies", "Lcom/tvplayer/webdav/data/tmdb/TmdbMovieSearchResponse;", "query", "page", "includeAdult", "", "year", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZLjava/lang/Integer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "searchTVShows", "Lcom/tvplayer/webdav/data/tmdb/TmdbTVSearchResponse;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public abstract interface TmdbApiService {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BASE_URL = "https://api.themoviedb.org/3/";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String POSTER_SIZE_W342 = "w342";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String POSTER_SIZE_W500 = "w500";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BACKDROP_SIZE_W780 = "w780";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BACKDROP_SIZE_W1280 = "w1280";
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.data.tmdb.TmdbApiService.Companion Companion = null;
    
    /**
     * 搜索电影
     */
    @retrofit2.http.GET(value = "search/movie")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchMovies(@retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Query(value = "query")
    @org.jetbrains.annotations.NotNull()
    java.lang.String query, @retrofit2.http.Query(value = "language")
    @org.jetbrains.annotations.NotNull()
    java.lang.String language, @retrofit2.http.Query(value = "page")
    int page, @retrofit2.http.Query(value = "include_adult")
    boolean includeAdult, @retrofit2.http.Query(value = "year")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer year, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbMovieSearchResponse>> $completion);
    
    /**
     * 获取电影详情
     */
    @retrofit2.http.GET(value = "movie/{movie_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMovieDetails(@retrofit2.http.Path(value = "movie_id")
    int movieId, @retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Query(value = "language")
    @org.jetbrains.annotations.NotNull()
    java.lang.String language, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbMovie>> $completion);
    
    /**
     * 搜索电视剧
     */
    @retrofit2.http.GET(value = "search/tv")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchTVShows(@retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Query(value = "query")
    @org.jetbrains.annotations.NotNull()
    java.lang.String query, @retrofit2.http.Query(value = "language")
    @org.jetbrains.annotations.NotNull()
    java.lang.String language, @retrofit2.http.Query(value = "page")
    int page, @retrofit2.http.Query(value = "include_adult")
    boolean includeAdult, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbTVSearchResponse>> $completion);
    
    /**
     * 获取电视剧详情
     */
    @retrofit2.http.GET(value = "tv/{tv_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTVShowDetails(@retrofit2.http.Path(value = "tv_id")
    int tvId, @retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Query(value = "language")
    @org.jetbrains.annotations.NotNull()
    java.lang.String language, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbTVShow>> $completion);
    
    /**
     * 获取电视剧季详情
     */
    @retrofit2.http.GET(value = "tv/{tv_id}/season/{season_number}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSeasonDetails(@retrofit2.http.Path(value = "tv_id")
    int tvId, @retrofit2.http.Path(value = "season_number")
    int seasonNumber, @retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @retrofit2.http.Query(value = "language")
    @org.jetbrains.annotations.NotNull()
    java.lang.String language, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbSeasonDetails>> $completion);
    
    /**
     * 获取配置信息
     */
    @retrofit2.http.GET(value = "configuration")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getConfiguration(@retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbConfiguration>> $completion);
    
    /**
     * 电影翻译
     */
    @retrofit2.http.GET(value = "movie/{movie_id}/translations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMovieTranslations(@retrofit2.http.Path(value = "movie_id")
    int movieId, @retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbTranslationsResponse>> $completion);
    
    /**
     * 电视剧翻译
     */
    @retrofit2.http.GET(value = "tv/{tv_id}/translations")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTVTranslations(@retrofit2.http.Path(value = "tv_id")
    int tvId, @retrofit2.http.Query(value = "api_key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String apiKey, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tvplayer.webdav.data.tmdb.TmdbTranslationsResponse>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/tvplayer/webdav/data/tmdb/TmdbApiService$Companion;", "", "()V", "BACKDROP_SIZE_W1280", "", "BACKDROP_SIZE_W780", "BASE_URL", "IMAGE_BASE_URL", "POSTER_SIZE_W342", "POSTER_SIZE_W500", "app_debug"})
    public static final class Companion {
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BASE_URL = "https://api.themoviedb.org/3/";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String POSTER_SIZE_W342 = "w342";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String POSTER_SIZE_W500 = "w500";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BACKDROP_SIZE_W780 = "w780";
        @org.jetbrains.annotations.NotNull()
        public static final java.lang.String BACKDROP_SIZE_W1280 = "w1280";
        
        private Companion() {
            super();
        }
    }
    
    /**
     * TMDB API服务接口
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}