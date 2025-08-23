package com.tvplayer.webdav.ui.player.subtitle

import androidx.media3.common.Player
import com.shuyu.gsyvideoplayer.model.GSYModel
import java.io.File

/**
 * 字幕数据模型 - 基于官方实现
 */
class GSYExoSubTitleModel(
    url: String,
    private val subTitle: String?,
    private val textOutput: Player.Listener,
    mapHeadData: MutableMap<String, String>,
    loop: Boolean,
    speed: Float,
    cache: Boolean,
    cachePath: File?,
    overrideExtension: String?
) : GSYModel(url, mapHeadData, loop, speed, cache, cachePath, overrideExtension) {

    fun getSubTitle(): String? = subTitle
    
    fun getTextOutput(): Player.Listener = textOutput
}
