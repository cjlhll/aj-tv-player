package com.tvplayer.webdav.ui.player.subtitle

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.media3.common.Player
import androidx.media3.common.text.CueGroup
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.tvplayer.webdav.R
import java.io.File
import java.util.HashMap

/**
 * 官方字幕播放器 - 基于 GSYVideoPlayer 官方示例
 * 完全按照官方 GSYExoSubTitleVideoView 实现
 */
class SimpleSubtitleVideoPlayer : NormalGSYVideoPlayer, Player.Listener {

    private var mSubtitleView: SubtitleView? = null
    private var mSubTitle: String? = null

    constructor(context: Context, fullFlag: Boolean) : super(context, fullFlag)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun init(context: Context) {
        super.init(context)

        // 按照官方方式初始化字幕视图
        mSubtitleView = findViewById(R.id.sub_title_view)

        // 设置字幕样式 - 使用大字体，确保清晰可见
        mSubtitleView?.setStyle(
            CaptionStyleCompat(
                Color.WHITE, // 白色字体
                Color.TRANSPARENT, // 透明背景
                Color.TRANSPARENT, // 透明窗口
                CaptionStyleCompat.EDGE_TYPE_OUTLINE, // 轮廓边缘
                Color.BLACK, // 黑色边缘
                null
            )
        )
        // 设置更大的字体大小，确保在各种屏幕上都清晰可见
        val textSize = 28f // 增大到28dp
        mSubtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)

        // 设置字幕视图的其他属性
        mSubtitleView?.setApplyEmbeddedStyles(false) // 禁用嵌入样式，使用我们的设置
        mSubtitleView?.setApplyEmbeddedFontSizes(false) // 禁用嵌入字体大小，使用我们的设置

        Log.i("SimpleSubtitleVideoPlayer", "字幕样式设置完成 - 字体大小: ${textSize}dp")

        Log.i("SimpleSubtitleVideoPlayer", "官方字幕视图初始化完成")
    }

    override fun getLayoutId(): Int {
        return R.layout.video_layout_subtitle
    }

    override fun startPrepare() {
        if (gsyVideoManager.listener() != null) {
            gsyVideoManager.listener().onCompletion()
        }
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onStartPrepared")
            mVideoAllCallBack.onStartPrepared(mOriginUrl, mTitle, this)
        }
        gsyVideoManager.setListener(this)
        gsyVideoManager.setPlayTag(mPlayTag)
        gsyVideoManager.setPlayPosition(mPlayPosition)

        // Audio focus is now handled by the base class GSYAudioFocusManager
        try {
            if (mContext is Activity) {
                (mContext as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mBackUpPlayingBufferState = -1

        // 使用带字幕的 prepare 方法 - 这是关键！
        (gsyVideoManager as GSYExoSubTitleVideoManager).prepare(
            mUrl,
            mSubTitle,
            this,
            if (mMapHeadData == null) HashMap<String, String>() else HashMap(mMapHeadData),
            mLooping,
            mSpeed,
            mCache,
            mCachePath,
            mOverrideExtension
        )
        setStateAndUi(CURRENT_STATE_PREPAREING)
    }

    override fun onCues(cueGroup: CueGroup) {
        if (mSubtitleView != null) {
            mSubtitleView!!.setCues(cueGroup.cues)
            Log.i("SimpleSubtitleVideoPlayer", "字幕回调触发: ${cueGroup.cues.size} 条字幕")

            // 详细记录每条字幕内容
            cueGroup.cues.forEachIndexed { index, cue ->
                Log.i("SimpleSubtitleVideoPlayer", "字幕[$index]: ${cue.text}")
            }
        } else {
            Log.e("SimpleSubtitleVideoPlayer", "字幕视图为空，无法显示字幕")
        }
    }

    fun getSubTitle(): String? {
        return mSubTitle
    }

    fun setSubTitle(subTitle: String?) {
        this.mSubTitle = subTitle
        Log.i("SimpleSubtitleVideoPlayer", "设置字幕文件: $subTitle")

        // 验证字幕文件
        if (!subTitle.isNullOrEmpty()) {
            val file = File(subTitle)
            if (file.exists()) {
                Log.i("SimpleSubtitleVideoPlayer", "字幕文件存在，大小: ${file.length()} bytes")

                // 读取文件前几行内容用于调试
                try {
                    val content = file.readText(Charsets.UTF_8)
                    val lines = content.split("\n").take(5)
                    Log.i("SimpleSubtitleVideoPlayer", "字幕文件前5行:")
                    lines.forEachIndexed { index: Int, line: String ->
                        Log.i("SimpleSubtitleVideoPlayer", "  [$index]: $line")
                    }
                } catch (e: Exception) {
                    Log.e("SimpleSubtitleVideoPlayer", "读取字幕文件失败", e)
                }
            } else {
                Log.e("SimpleSubtitleVideoPlayer", "字幕文件不存在: $subTitle")
            }
        }
    }

    /**
     * 检查是否有字幕
     */
    fun hasSubtitle(): Boolean {
        return !mSubTitle.isNullOrEmpty()
    }

    /**
     * 清除字幕
     */
    fun clearSubtitle() {
        mSubTitle = null
        Log.i("SimpleSubtitleVideoPlayer", "清除字幕")
    }

    /**
     * 设置字幕字体大小
     */
    fun setSubtitleTextSize(textSizeDp: Float) {
        mSubtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDp)
        Log.i("SimpleSubtitleVideoPlayer", "字幕字体大小设置为: ${textSizeDp}dp")
    }

    /**
     * 设置字幕样式
     */
    fun setSubtitleStyle(
        textColor: Int = Color.WHITE,
        backgroundColor: Int = Color.TRANSPARENT,
        windowColor: Int = Color.TRANSPARENT,
        edgeType: Int = CaptionStyleCompat.EDGE_TYPE_OUTLINE,
        edgeColor: Int = Color.BLACK,
        textSize: Float = 28f
    ) {
        mSubtitleView?.let { subtitleView ->
            subtitleView.setStyle(
                CaptionStyleCompat(
                    textColor,
                    backgroundColor,
                    windowColor,
                    edgeType,
                    edgeColor,
                    null
                )
            )
            subtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
            subtitleView.setApplyEmbeddedStyles(false)
            subtitleView.setApplyEmbeddedFontSizes(false)
        }
        Log.i("SimpleSubtitleVideoPlayer", "字幕样式更新完成 - 字体大小: ${textSize}dp")
    }

    /**********以下重载 GSYVideoPlayer 的管理器相关实现***********/

    override fun getGSYVideoManager(): GSYExoSubTitleVideoManager {
        GSYExoSubTitleVideoManager.instance().initContext(context.applicationContext)
        return GSYExoSubTitleVideoManager.instance()
    }

    override fun backFromFull(context: Context): Boolean {
        return GSYExoSubTitleVideoManager.backFromWindowFull(context)
    }

    override fun releaseVideos() {
        GSYExoSubTitleVideoManager.releaseAllVideos()
    }

    override fun getFullId(): Int {
        return GSYExoSubTitleVideoManager.FULLSCREEN_ID
    }

    override fun getSmallId(): Int {
        return GSYExoSubTitleVideoManager.SMALL_ID
    }
}
