package com.tvplayer.webdav.ui.player.subtitle

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.media3.common.Player
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager
import com.shuyu.gsyvideoplayer.player.IPlayerManager
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import java.io.File
import java.util.Map

/**
 * 字幕视频管理器 - 基于官方实现
 */
class GSYExoSubTitleVideoManager : GSYVideoBaseManager {

    companion object {
        val SMALL_ID = com.shuyu.gsyvideoplayer.R.id.small_id
        val FULLSCREEN_ID = com.shuyu.gsyvideoplayer.R.id.full_id
        const val TAG = "GSYExoSubTitleVideoManager"

        @SuppressLint("StaticFieldLeak")
        private var videoManager: GSYExoSubTitleVideoManager? = null

        /**
         * 单例管理器
         */
        @JvmStatic
        @Synchronized
        fun instance(): GSYExoSubTitleVideoManager {
            if (videoManager == null) {
                videoManager = GSYExoSubTitleVideoManager()
            }
            return videoManager!!
        }

        /**
         * 退出全屏，主要用于返回键
         */
        @JvmStatic
        @Suppress("ResourceType")
        fun backFromWindowFull(context: Context): Boolean {
            var backFrom = false
            val vp = CommonUtil.scanForActivity(context).findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT)
            val oldF = vp.findViewById<View>(FULLSCREEN_ID)
            if (oldF != null) {
                backFrom = true
                CommonUtil.hideNavKey(context)
                if (instance().lastListener() != null) {
                    instance().lastListener().onBackFullscreen()
                }
            }
            return backFrom
        }

        /**
         * 页面销毁了记得调用是否所有的video
         */
        @JvmStatic
        fun releaseAllVideos() {
            if (instance().listener() != null) {
                instance().listener().onCompletion()
            }
            instance().releaseMediaPlayer()
        }

        /**
         * 暂停播放
         */
        @JvmStatic
        fun onPause() {
            if (instance().listener() != null) {
                instance().listener().onVideoPause()
            }
        }

        /**
         * 恢复播放
         */
        @JvmStatic
        fun onResume() {
            if (instance().listener() != null) {
                instance().listener().onVideoResume()
            }
        }

        /**
         * 恢复暂停状态
         */
        @JvmStatic
        fun onResume(seek: Boolean) {
            if (instance().listener() != null) {
                instance().listener().onVideoResume(seek)
            }
        }
    }

    private constructor() : super() {
        init()
    }

    override fun getPlayManager(): IPlayerManager {
        playerManager = GSYExoSubTitlePlayerManager()
        return playerManager
    }

    /**
     * 带字幕的 prepare 方法
     */
    fun prepare(
        url: String,
        subTitle: String?,
        textOutput: Player.Listener,
        mapHeadData: MutableMap<String, String>,
        loop: Boolean,
        speed: Float,
        cache: Boolean,
        cachePath: File?,
        overrideExtension: String?
    ) {
        val msg = Message()
        msg.what = HANDLER_PREPARE
        msg.obj = GSYExoSubTitleModel(
            url, 
            subTitle, 
            textOutput, 
            mapHeadData, 
            loop, 
            speed, 
            cache, 
            cachePath, 
            overrideExtension
        )
        sendMessage(msg)
    }
}
