package com.tvplayer.webdav.ui.player.subtitle

import android.content.Context
import android.media.AudioManager
import android.net.TrafficStats
import android.net.Uri
import android.os.Message
import android.view.Surface
import androidx.annotation.Nullable
import androidx.media3.common.Player
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.video.PlaceholderSurface
import com.shuyu.gsyvideoplayer.cache.ICacheManager
import com.shuyu.gsyvideoplayer.model.VideoOptionModel
import com.shuyu.gsyvideoplayer.player.BasePlayerManager
import tv.danmaku.ijk.media.player.IMediaPlayer

/**
 * 字幕播放器管理器 - 基于官方实现
 */
class GSYExoSubTitlePlayerManager : BasePlayerManager() {

    private var context: Context? = null
    private var mediaPlayer: GSYExoSubTitlePlayer? = null
    private var surface: Surface? = null
    private var dummySurface: PlaceholderSurface? = null
    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0

    override fun getMediaPlayer(): IMediaPlayer? {
        return mediaPlayer
    }

    override fun initVideoPlayer(
        context: Context,
        msg: Message,
        optionModelList: List<VideoOptionModel>?,
        cacheManager: ICacheManager?
    ) {
        this.context = context.applicationContext
        mediaPlayer = GSYExoSubTitlePlayer(context)
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        
        if (dummySurface == null) {
            dummySurface = PlaceholderSurface.newInstanceV17(context, false)
        }

        // 使用自己的cache模式
        val gsyModel = msg.obj as GSYExoSubTitleModel
        try {
            mediaPlayer?.setLooping(gsyModel.isLooping)
            if (gsyModel.getSubTitle() != null) {
                mediaPlayer?.setSubTitile(gsyModel.getSubTitle()!!)
            }
            mediaPlayer?.setPreview(gsyModel.mapHeadData.isNotEmpty())
            
            if (gsyModel.isCache && cacheManager != null) {
                // 通过管理器处理
                cacheManager.doCacheLogic(
                    context, 
                    mediaPlayer, 
                    gsyModel.url, 
                    gsyModel.mapHeadData, 
                    gsyModel.cachePath
                )
            } else {
                // 通过自己的内部缓存机制
                mediaPlayer?.setCache(gsyModel.isCache)
                mediaPlayer?.setCacheDir(gsyModel.cachePath)
                mediaPlayer?.setOverrideExtension(gsyModel.overrideExtension)
                mediaPlayer?.setDataSource(context, Uri.parse(gsyModel.url), gsyModel.mapHeadData)
            }
            
            if (gsyModel.speed != 1f && gsyModel.speed > 0) {
                mediaPlayer?.setSpeed(gsyModel.speed, 1f)
            }
            
            mediaPlayer?.setTextOutput(gsyModel.getTextOutput())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initSuccess(gsyModel)
    }

    override fun showDisplay(msg: Message) {
        if (mediaPlayer == null) {
            return
        }
        if (msg.obj == null) {
            mediaPlayer?.setSurface(dummySurface)
        } else {
            val holder = msg.obj as Surface
            surface = holder
            mediaPlayer?.setSurface(holder)
        }
    }

    override fun setSpeed(speed: Float, soundTouch: Boolean) {
        mediaPlayer?.let {
            try {
                it.setSpeed(speed, 1f)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun setNeedMute(needMute: Boolean) {
        mediaPlayer?.let {
            if (needMute) {
                it.setVolume(0f, 0f)
            } else {
                it.setVolume(1f, 1f)
            }
        }
    }

    override fun setVolume(left: Float, right: Float) {
        mediaPlayer?.setVolume(left, right)
    }

    override fun releaseSurface() {
        surface = null
    }

    override fun release() {
        mediaPlayer?.let {
            it.setSurface(null)
            it.release()
        }
        mediaPlayer = null
        
        dummySurface?.let {
            it.release()
        }
        dummySurface = null
        lastTotalRxBytes = 0
        lastTimeStamp = 0
    }

    override fun getBufferedPercentage(): Int {
        return mediaPlayer?.getBufferedPercentage() ?: 0
    }

    override fun getNetSpeed(): Long {
        return if (mediaPlayer != null) {
            getNetSpeed(context)
        } else 0
    }

    override fun setSpeedPlaying(speed: Float, soundTouch: Boolean) {
        // 简化实现
    }

    override fun start() {
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun getVideoWidth(): Int {
        return mediaPlayer?.getVideoWidth() ?: 0
    }

    override fun getVideoHeight(): Int {
        return mediaPlayer?.getVideoHeight() ?: 0
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying() ?: false
    }

    override fun seekTo(time: Long) {
        mediaPlayer?.seekTo(time)
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer?.getCurrentPosition() ?: 0
    }

    override fun getDuration(): Long {
        return mediaPlayer?.getDuration() ?: 0
    }

    override fun getVideoSarNum(): Int {
        return mediaPlayer?.getVideoSarNum() ?: 1
    }

    override fun getVideoSarDen(): Int {
        return mediaPlayer?.getVideoSarDen() ?: 1
    }

    override fun isSurfaceSupportLockCanvas(): Boolean {
        return false
    }

    fun addTextOutputPlaying(textOutput: Player.Listener) {
        mediaPlayer?.addTextOutputPlaying(textOutput)
    }

    fun removeTextOutput(textOutput: Player.Listener) {
        mediaPlayer?.removeTextOutput(textOutput)
    }

    /**
     * 设置seek 的临近帧。
     */
    fun setSeekParameter(@Nullable seekParameters: SeekParameters?) {
        mediaPlayer?.setSeekParameter(seekParameters)
    }

    private fun getNetSpeed(context: Context?): Long {
        if (context == null) {
            return 0
        }
        val nowTotalRxBytes = if (TrafficStats.getUidRxBytes(context.applicationInfo.uid) == TrafficStats.UNSUPPORTED.toLong()) {
            0L
        } else {
            TrafficStats.getTotalRxBytes() / 1024 // 转为KB
        }
        val nowTimeStamp = System.currentTimeMillis()
        val calculationTime = nowTimeStamp - lastTimeStamp
        if (calculationTime == 0L) {
            return 0L
        }
        // 毫秒转换
        val speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / calculationTime
        lastTimeStamp = nowTimeStamp
        lastTotalRxBytes = nowTotalRxBytes
        return speed
    }
}
