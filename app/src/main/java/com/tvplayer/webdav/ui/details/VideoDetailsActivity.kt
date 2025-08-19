package com.tvplayer.webdav.ui.details

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaItem
import dagger.hilt.android.AndroidEntryPoint

/**
 * 视频详情页面Activity
 * 显示视频的详细信息，包括海报、简介、演员表等
 */
@AndroidEntryPoint
class VideoDetailsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_details)

        val mediaItem = intent.getParcelableExtra<MediaItem>("media_item")
        if (mediaItem == null) {
            finish()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.details_fragment_container, 
                    VideoDetailsFragment.newInstance(mediaItem))
                .commitNow()
        }
    }
}
