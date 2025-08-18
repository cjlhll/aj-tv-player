package com.tvplayer.webdav.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaCategory
import com.tvplayer.webdav.data.model.MediaType
import com.tvplayer.webdav.ui.settings.SettingsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主界面Fragment - 类似Infuse的海报墙设计
 * 包含分类导航和媒体内容展示
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvRecentlyAdded: RecyclerView
    private lateinit var rvContinueWatching: RecyclerView
    private lateinit var rvMovies: RecyclerView
    private lateinit var rvTVShows: RecyclerView
    private var ivBackdrop: android.widget.ImageView? = null

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recentlyAddedAdapter: MediaPosterAdapter
    private lateinit var continueWatchingAdapter: MediaPosterAdapter
    private lateinit var moviesAdapter: MediaPosterAdapter
    private lateinit var tvShowsAdapter: MediaPosterAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAdapters()
        observeViewModel()

        // 加载数据
        viewModel.loadHomeData()
    }

    private fun initViews(view: View) {
        rvCategories = view.findViewById(R.id.rv_categories)
        rvRecentlyAdded = view.findViewById(R.id.rv_recently_added)
        rvContinueWatching = view.findViewById(R.id.rv_continue_watching)
        rvMovies = view.findViewById(R.id.rv_movies)
        rvTVShows = view.findViewById(R.id.rv_tv_shows)
        ivBackdrop = view.findViewById(R.id.iv_backdrop)
        // 刷新按钮
        view.findViewById<android.widget.ImageButton>(R.id.btn_refresh)?.setOnClickListener {
            viewModel.rescanAndScrape()
        }

    }

    private fun setupAdapters() {
        // 分类适配器
        categoryAdapter = CategoryAdapter { category ->
            onCategoryClick(category)
        }
        rvCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            // 为边缘item的焦点效果添加更多padding，特别是上下padding
            setPadding(24, 16, 24, 16)
            clipToPadding = false
        }

        // 最近添加适配器
        recentlyAddedAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvRecentlyAdded.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentlyAddedAdapter
            // 为边缘item的焦点效果添加更多padding
            setPadding(24, 8, 24, 8)
            clipToPadding = false
        }

        // 继续观看适配器
        continueWatchingAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvContinueWatching.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = continueWatchingAdapter
            // 为边缘item的焦点效果添加更多padding
            setPadding(24, 8, 24, 8)
            clipToPadding = false
        }

        // 电影适配器
        moviesAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvMovies.apply {
            layoutManager = GridLayoutManager(context, 5) // 改为5列
            adapter = moviesAdapter
            // 更新间距装饰器为5列
            addItemDecoration(GridSpacingItemDecoration(5, 8, true))
            setPadding(24, 16, 24, 16)
            clipToPadding = false
        }

        // 电视剧适配器
        tvShowsAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvTVShows.apply {
            layoutManager = GridLayoutManager(context, 5) // 改为5列
            adapter = tvShowsAdapter
            addItemDecoration(GridSpacingItemDecoration(5, 8, true))
            setPadding(24, 16, 24, 16)
            clipToPadding = false
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.recentlyAdded.observe(viewLifecycleOwner) { items ->
            recentlyAddedAdapter.submitList(items)
        }

        viewModel.continueWatching.observe(viewLifecycleOwner) { items ->
            continueWatchingAdapter.submitList(items)
        }

        viewModel.movies.observe(viewLifecycleOwner) { items ->
            moviesAdapter.submitList(items)
        }

        viewModel.tvShows.observe(viewLifecycleOwner) { items ->
            tvShowsAdapter.submitList(items)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 显示/隐藏加载指示器
        }
    }

    private fun onCategoryClick(category: MediaCategory) {
        when (category.id) {
            "settings" -> {
                navigateToSettings()
            }
            "movies" -> {
                // TODO: 导航到电影列表
            }
            "tv_shows" -> {
                // TODO: 导航到电视剧列表
            }
            "recently_added" -> {
                // TODO: 导航到最近添加列表
            }
            "favorites" -> {
                // TODO: 导航到收藏列表
            }
        }
    }

    private fun onMediaItemClick(mediaItem: com.tvplayer.webdav.data.model.MediaItem) {
        // TODO: 导航到播放器或详情页面
    }

    private fun onPosterFocused(mediaItem: com.tvplayer.webdav.data.model.MediaItem) {
        val backdropUrl = mediaItem.backdropPath ?: mediaItem.posterPath
        val imageView = ivBackdrop ?: return
        if (backdropUrl.isNullOrEmpty()) {
            imageView.animate().alpha(0f).setDuration(200).start()
            return
        }
        // 使用Glide加载背景图并淡入
        try {
            com.bumptech.glide.Glide.with(requireContext())
                .load(backdropUrl)
                .centerCrop()
                .into(imageView)
            imageView.animate().alpha(1f).setDuration(250).start()
        } catch (e: Exception) {
            // 忽略加载错误，保持现状
        }
    }

    private fun navigateToSettings() {
        val fragment = SettingsFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
