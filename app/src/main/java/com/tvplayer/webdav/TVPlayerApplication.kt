package com.tvplayer.webdav

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for TV Player
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class TVPlayerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any global configurations here
    }
}
