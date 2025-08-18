package com.tvplayer.webdav.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tvplayer.webdav.R
import com.tvplayer.webdav.ui.webdav.WebDAVConnectionFragment
import com.tvplayer.webdav.ui.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main fragment for Android TV
 * Displays the main navigation and content
 */
@AndroidEntryPoint
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置按钮点击事件
        val btnEnterHome = view.findViewById<Button>(R.id.btn_enter_home)
        btnEnterHome.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val fragment = HomeFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToWebDAVSetup() {
        val fragment = WebDAVConnectionFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

}
