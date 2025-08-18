package com.tvplayer.webdav.ui.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tvplayer.webdav.R
import com.tvplayer.webdav.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Android TV
 * 直接加载主界面，无启动页
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            // 直接进入主界面，跳过启动页，添加淡入动画
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, 0)
                .replace(R.id.main_browse_fragment, HomeFragment.newInstance())
                .commitNow()
        }
    }
}
