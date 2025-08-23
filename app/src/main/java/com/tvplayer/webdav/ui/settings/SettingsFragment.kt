package com.tvplayer.webdav.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tvplayer.webdav.R
import com.tvplayer.webdav.ui.webdav.WebDAVConnectionFragment
import com.tvplayer.webdav.ui.scanner.ScannerFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * 设置Fragment
 * 包含应用的各种设置选项
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupFocusAnimations(view)
    }

    private fun setupViews(view: View) {
        // WebDAV设置
        val btnWebDAVSettings = view.findViewById<Button>(R.id.btn_webdav_settings)
        btnWebDAVSettings.setOnClickListener {
            navigateToWebDAVSettings()
        }

        // 媒体扫描
        val btnMediaScan = view.findViewById<Button>(R.id.btn_media_scan)
        btnMediaScan.setOnClickListener {
            navigateToMediaScan()
        }

        // 其他设置按钮可以在这里添加
        val btnAbout = view.findViewById<Button>(R.id.btn_about)
        btnAbout.setOnClickListener {
            // TODO: 显示关于信息
        }

        val btnClearCache = view.findViewById<Button>(R.id.btn_clear_cache)
        btnClearCache.setOnClickListener {
            // TODO: 清除缓存
        }
    }

    private fun setupFocusAnimations(view: View) {
        // 为所有设置按钮设置焦点动画
        val btnWebDAVSettings = view.findViewById<Button>(R.id.btn_webdav_settings)
        val btnMediaScan = view.findViewById<Button>(R.id.btn_media_scan)
        val btnClearCache = view.findViewById<Button>(R.id.btn_clear_cache)
        val btnPlaybackSettings = view.findViewById<Button>(R.id.btn_playback_settings)
        val btnAbout = view.findViewById<Button>(R.id.btn_about)

        SettingsFocusAnimator.setupAllSettingsButtons(
            btnWebDAVSettings,
            btnMediaScan,
            btnClearCache,
            btnPlaybackSettings,
            btnAbout
        )
    }

    private fun navigateToWebDAVSettings() {
        val fragment = WebDAVConnectionFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToMediaScan() {
        val fragment = ScannerFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
