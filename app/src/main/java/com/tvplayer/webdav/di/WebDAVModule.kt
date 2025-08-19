package com.tvplayer.webdav.di

import com.tvplayer.webdav.data.tmdb.TmdbApiService
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebDAVClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbClient

/**
 * 网络和API相关的依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object WebDAVModule {

    @Provides
    @Singleton
    @WebDAVClient
    fun provideWebDAVOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @TmdbClient
    fun provideTmdbOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            // SSL配置：重试连接
            .retryOnConnectionFailure(true)
            // 添加SSL配置以解决证书验证问题
            .apply { configureSSL(this) }
            .build()
    }

    /**
     * 配置SSL以解决证书验证问题
     * ⚠️ 注意：此配置仅用于开发环境，生产环境应使用正确的证书验证
     */
    private fun configureSSL(builder: OkHttpClient.Builder) {
        try {
            // 创建信任所有证书的TrustManager（仅用于开发）
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // 安装信任所有证书的SSLContext
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            
            builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            // 如果SSL配置失败，记录错误但不影响应用启动
            e.printStackTrace()
        }
    }

    @Provides
    @Singleton
    fun provideWebDAVClient(@WebDAVClient okHttpClient: OkHttpClient): SimpleWebDAVClient {
        return SimpleWebDAVClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideTmdbRetrofit(@TmdbClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(TmdbApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbApiService(retrofit: Retrofit): TmdbApiService {
        return retrofit.create(TmdbApiService::class.java)
    }
}
