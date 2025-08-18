package com.tvplayer.webdav.data.tmdb

import com.google.gson.annotations.SerializedName

/**
 * TMDB API数据模型
 */

/**
 * 电影搜索结果
 */
data class TmdbMovieSearchResponse(
    val page: Int,
    val results: List<TmdbMovie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

/**
 * 电影信息
 */
data class TmdbMovie(
    val id: Int,
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String?,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    val runtime: Int?,
    val genres: List<TmdbGenre>?,
    @SerializedName("genre_ids")
    val genreIds: List<Int>?
)

/**
 * 电视剧搜索结果
 */
data class TmdbTVSearchResponse(
    val page: Int,
    val results: List<TmdbTVShow>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

/**
 * 电视剧信息
 */
data class TmdbTVShow(
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String?,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int?,
    @SerializedName("number_of_episodes")
    val numberOfEpisodes: Int?,
    val genres: List<TmdbGenre>?,
    @SerializedName("genre_ids")
    val genreIds: List<Int>?,
    val seasons: List<TmdbSeason>?
)

/**
 * 电视剧季信息
 */
data class TmdbSeason(
    val id: Int,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("air_date")
    val airDate: String?,
    @SerializedName("episode_count")
    val episodeCount: Int
)

/**
 * 电视剧集信息
 */
data class TmdbEpisode(
    val id: Int,
    @SerializedName("episode_number")
    val episodeNumber: Int,
    val name: String,
    val overview: String?,
    @SerializedName("still_path")
    val stillPath: String?,
    @SerializedName("air_date")
    val airDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Float,
    val runtime: Int?
)

/**
 * 季详情（包含剧集列表）
 */
data class TmdbSeasonDetails(
    val id: Int,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("air_date")
    val airDate: String?,
    val episodes: List<TmdbEpisode>
)

/**
 * 类型信息
 */
data class TmdbGenre(
    val id: Int,
    val name: String
)

/**
 * 配置信息
 */
data class TmdbConfiguration(
    val images: TmdbImageConfiguration
)

/**
 * 图片配置
 */
data class TmdbImageConfiguration(
    @SerializedName("base_url")
    val baseUrl: String,
    @SerializedName("secure_base_url")
    val secureBaseUrl: String,
    @SerializedName("backdrop_sizes")
    val backdropSizes: List<String>,
    @SerializedName("poster_sizes")
    val posterSizes: List<String>
)

/**
 * 翻译响应
 */
 data class TmdbTranslationsResponse(
     val id: Int,
     val translations: List<TmdbTranslation>
 )

 data class TmdbTranslation(
     val iso_3166_1: String?,
     val iso_639_1: String?,
     val name: String?,
     val english_name: String?,
     val data: TmdbTranslationData?
 )

 data class TmdbTranslationData(
     val title: String?,
     val name: String?,
     val overview: String?
 )

/**
 * 错误响应
 */
data class TmdbErrorResponse(
    @SerializedName("status_code")
    val statusCode: Int,
    @SerializedName("status_message")
    val statusMessage: String
)
