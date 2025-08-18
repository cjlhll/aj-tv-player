package com.tvplayer.webdav.data.tmdb

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB API服务接口
 */
interface TmdbApiService {

    /**
     * 搜索电影
     */
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "zh-CN",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("year") year: Int? = null
    ): Response<TmdbMovieSearchResponse>

    /**
     * 获取电影详情
     */
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN"
    ): Response<TmdbMovie>

    /**
     * 搜索电视剧
     */
    @GET("search/tv")
    suspend fun searchTVShows(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "zh-CN",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): Response<TmdbTVSearchResponse>

    /**
     * 获取电视剧详情
     */
    @GET("tv/{tv_id}")
    suspend fun getTVShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN"
    ): Response<TmdbTVShow>

    /**
     * 获取电视剧季详情
     */
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN"
    ): Response<TmdbSeasonDetails>

    /**
     * 获取配置信息
     */
    @GET("configuration")
    suspend fun getConfiguration(
        @Query("api_key") apiKey: String
    ): Response<TmdbConfiguration>
    /**
     * 电影翻译
     */
    @GET("movie/{movie_id}/translations")
    suspend fun getMovieTranslations(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<TmdbTranslationsResponse>

    /**
     * 电视剧翻译
     */
    @GET("tv/{tv_id}/translations")
    suspend fun getTVTranslations(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Response<TmdbTranslationsResponse>


    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

        // 常用的图片尺寸
        const val POSTER_SIZE_W342 = "w342"
        const val POSTER_SIZE_W500 = "w500"
        const val BACKDROP_SIZE_W780 = "w780"
        const val BACKDROP_SIZE_W1280 = "w1280"
    }
}
