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
import com.tvplayer.webdav.data.model.TVSeriesSummary
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
    private lateinit var tvShowsAdapter: TVSeriesAdapter

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
            // 为边缘item的焦点效果添加更多padding，增加垂直padding防止裁剪
            setPadding(24, 16, 24, 16)
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
            // 为边缘item的焦点效果添加更多padding，增加垂直padding防止裁剪
            setPadding(24, 16, 24, 16)
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
        tvShowsAdapter = TVSeriesAdapter(
            onSeriesClick = { series -> onTVSeriesClick(series) },
            onItemFocused = { series -> onTVSeriesFocused(series) }
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
        // 导航到视频详情页面
        val intent = android.content.Intent(requireContext(), com.tvplayer.webdav.ui.details.VideoDetailsActivity::class.java)
        intent.putExtra("media_item", mediaItem)
        startActivity(intent)
    }

    private fun onPosterFocused(mediaItem: com.tvplayer.webdav.data.model.MediaItem) {
        val backdropUrl = mediaItem.backdropPath ?: mediaItem.posterPath
        updateBackdropWithTransition(backdropUrl)
    }

    private fun onTVSeriesClick(series: TVSeriesSummary) {
        // 将TVSeriesSummary转换为MediaItem以便导航到详情页面
        val mediaItem = com.tvplayer.webdav.data.model.MediaItem(
            id = series.seriesId,
            title = series.seriesTitle,
            originalTitle = null,
            overview = series.overview,
            posterPath = series.posterPath,
            backdropPath = series.backdropPath,
            releaseDate = series.releaseDate,
            rating = series.rating,
            duration = 0L, // TV系列没有固定时长
            mediaType = com.tvplayer.webdav.data.model.MediaType.TV_EPISODE, // 使用TV_EPISODE类型触发TV系列UI
            filePath = "", // TV系列没有单一文件路径
            fileSize = 0L,
            lastModified = series.lastWatchedTime,
            seasonNumber = if (series.totalSeasons > 0) 1 else null, // 默认选择第1季
            episodeNumber = null,
            seriesId = series.seriesId,
            seriesTitle = series.seriesTitle,
            watchedProgress = 0f,
            isWatched = false,
            lastWatchedTime = series.lastWatchedTime,
            isFavorite = false,
            tags = emptyList(),
            genre = series.genre
        )

        android.util.Log.d("HomeFragment", "Created MediaItem for TV series: " +
            "id=${mediaItem.id}, title=${mediaItem.title}, mediaType=${mediaItem.mediaType}")

        // 导航到视频详情页面
        val intent = android.content.Intent(requireContext(), com.tvplayer.webdav.ui.details.VideoDetailsActivity::class.java)
        intent.putExtra("media_item", mediaItem)
        startActivity(intent)
    }

    private fun onTVSeriesFocused(series: TVSeriesSummary) {
        val backdropUrl = series.backdropPath ?: series.posterPath
        updateBackdropWithTransition(backdropUrl)
    }

    /**
     * 更新背景图片并添加平滑过渡效果
     */
    private fun updateBackdropWithTransition(backdropUrl: String?) {
        val imageView = ivBackdrop ?: return

        if (backdropUrl.isNullOrEmpty()) {
            // 淡出到透明
            imageView.animate()
                .alpha(0f)
                .setDuration(300)
                .start()
            return
        }

        try {
            // 创建一个临时的ImageView用于预加载新图片
            val tempImageView = android.widget.ImageView(requireContext())
            tempImageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

            // 使用Glide预加载新图片
            com.bumptech.glide.Glide.with(requireContext())
                .load(backdropUrl)
                .centerCrop()
                .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        // 新图片加载完成，开始交叉淡入淡出动画
                        performCrossfadeTransition(imageView, resource)
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // 加载被清除时的处理
                    }
                })
        } catch (e: Exception) {
            // 加载失败时保持当前状态
        }
    }

    /**
     * 执行交叉淡入淡出过渡动画
     */
    private fun performCrossfadeTransition(imageView: android.widget.ImageView, newDrawable: android.graphics.drawable.Drawable) {
        val currentDrawable = imageView.drawable

        if (currentDrawable == null) {
            // 如果当前没有图片，直接淡入新图片
            imageView.setImageDrawable(newDrawable)
            imageView.alpha = 0f
            imageView.animate()
                .alpha(1f)
                .setDuration(400)
                .start()
        } else {
            // 创建交叉淡入淡出效果
            val crossfadeDrawable = android.graphics.drawable.TransitionDrawable(
                arrayOf(currentDrawable, newDrawable)
            )

            imageView.setImageDrawable(crossfadeDrawable)
            crossfadeDrawable.startTransition(500) // 500ms的交叉淡入淡出
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
