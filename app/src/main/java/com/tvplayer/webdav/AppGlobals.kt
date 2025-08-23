package com.tvplayer.webdav

import android.app.Application

/**
 * 全局应用上下文持有者，用于在无法直接获取Context的回调中使用
 */
object AppGlobals {
    lateinit var app: Application
        private set

    fun init(application: Application) {
        app = application
    }
}

