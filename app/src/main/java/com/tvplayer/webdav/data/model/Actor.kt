package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 演员数据模型
 */
@Parcelize
data class Actor(
    val id: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val isDirector: Boolean = false
) : Parcelable