package com.tvplayer.webdav

import android.app.Application
import android.util.Log
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for TV Player
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class TVPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppGlobals.init(this)
        // Initialize any global configurations here

        // 全局切换到ExoPlayer内核以获得更好的WebDAV支持
        initializeVideoPlayer()

        // 禁用GSYVideoPlayer的全局缓存以支持WebDAV播放
        disableGSYVideoPlayerCache()
    }

    /**
     * 初始化视频播放器配置
     */
    private fun initializeVideoPlayer() {
        try {
            // 全局设置ExoPlayer作为默认播放内核
            PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
            Log.i("TVPlayerApplication", "ExoPlayer kernel initialized globally for better WebDAV support")
        } catch (e: Exception) {
            Log.e("TVPlayerApplication", "Failed to initialize ExoPlayer kernel: ${e.message}")
        }
    }

    /**
     * 禁用GSYVideoPlayer的缓存机制，解决WebDAV播放问题
     */
    private fun disableGSYVideoPlayerCache() {
        try {
            // 禁用ProxyCache服务器
            val proxyCacheManagerClass = Class.forName("com.danikula.videocache.ProxyCacheManager")
            val isInstanceMethod = proxyCacheManagerClass.getMethod("isInstance")
            val isInstance = isInstanceMethod.invoke(null) as Boolean

            if (isInstance) {
                val shutdownMethod = proxyCacheManagerClass.getMethod("shutdown")
                shutdownMethod.invoke(null)
                Log.i("TVPlayerApplication", "ProxyCache disabled for WebDAV support")
            }
        } catch (e: Exception) {
            Log.w("TVPlayerApplication", "Could not disable ProxyCache, using alternative method: ${e.message}")

            // 替代方法：设置空的缓存目录
            try {
                System.setProperty("http.proxyHost", "")
                System.setProperty("http.proxyPort", "")
                System.setProperty("https.proxyHost", "")
                System.setProperty("https.proxyPort", "")
                Log.i("TVPlayerApplication", "Proxy settings cleared for WebDAV support")
            } catch (ex: Exception) {
                Log.e("TVPlayerApplication", "Failed to configure proxy settings: ${ex.message}")
            }
        }
    }
}
