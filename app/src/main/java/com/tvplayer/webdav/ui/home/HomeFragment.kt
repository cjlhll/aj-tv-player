package com.tvplayer.webdav.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import javax.inject.Inject

/**
 * 主界面Fragment - 类似Infuse的海报墙设计
 * 包含分类导航和媒体内容展示
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var focusStateManager: FocusStateManager

    private lateinit var rvPlaybackHistory: RecyclerView
    private lateinit var rvMovies: RecyclerView
    private lateinit var rvTVShows: RecyclerView
    private var ivBackdrop: ImageView? = null
    private lateinit var ivSettings: ImageView

    private lateinit var playbackHistoryAdapter: MediaPosterAdapter
    private lateinit var moviesAdapter: MediaPosterAdapter
    private lateinit var tvShowsAdapter: TVSeriesAdapter

    // 标记是否需要恢复焦点状态
    private var shouldRestoreFocus = false
    // 标记是否是首次启动（用于设置默认焦点）
    private var isFirstLaunch = true

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

    override fun onResume() {
        super.onResume()

        // 如果需要恢复焦点状态，延迟执行以确保数据已加载
        if (shouldRestoreFocus) {
            // 延迟恢复焦点，确保RecyclerView已经完成数据绑定
            view?.postDelayed({
                restoreFocusStates()
                shouldRestoreFocus = false
            }, 300) // 给足够时间让数据加载和视图更新
        } else if (isFirstLaunch) {
            // 首次启动时设置默认焦点
            view?.postDelayed({
                setDefaultFocus()
                isFirstLaunch = false
            }, 500) // 给更多时间确保数据加载完成
        }
    }

    override fun onPause() {
        super.onPause()

        // 保存当前的焦点状态
        saveFocusStates()

        // 标记下次恢复时需要恢复焦点
        shouldRestoreFocus = true
    }

    private fun initViews(view: View) {
        rvPlaybackHistory = view.findViewById<RecyclerView>(R.id.rv_playback_history)
        rvMovies = view.findViewById<RecyclerView>(R.id.rv_movies)
        rvTVShows = view.findViewById<RecyclerView>(R.id.rv_tv_shows)
        ivBackdrop = view.findViewById<ImageView>(R.id.iv_backdrop)
        ivSettings = view.findViewById<ImageView>(R.id.iv_settings)

        // 设置设置按钮的点击事件
        ivSettings.setOnClickListener {
            navigateToSettings()
        }
    }

    private fun setupAdapters() {
        // 播放历史适配器
        playbackHistoryAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvPlaybackHistory.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = playbackHistoryAdapter
            // 为边缘item的焦点效果添加更多padding，增加垂直padding防止裁剪
            setPadding(24, 12, 24, 12)
            clipToPadding = false
        }

        // 电影适配器
        moviesAdapter = MediaPosterAdapter(
            onMediaClick = { mediaItem -> onMediaItemClick(mediaItem) },
            onItemFocused = { mediaItem -> onPosterFocused(mediaItem) }
        )
        rvMovies.apply {
            layoutManager = GridLayoutManager(context, 6) // 改为6列
            adapter = moviesAdapter
            // 更新间距装饰器为6列
            addItemDecoration(GridSpacingItemDecoration(6, 6, true))
            setPadding(24, 12, 24, 12)
            clipToPadding = false
        }

        // 电视剧适配器
        tvShowsAdapter = TVSeriesAdapter(
            onSeriesClick = { series -> onTVSeriesClick(series) },
            onItemFocused = { series -> onTVSeriesFocused(series) }
        )
        rvTVShows.apply {
            layoutManager = GridLayoutManager(context, 6) // 改为6列
            adapter = tvShowsAdapter
            addItemDecoration(GridSpacingItemDecoration(6, 6, true))
            setPadding(24, 12, 24, 12)
            clipToPadding = false
        }
    }

    private fun observeViewModel() {
        viewModel.playbackHistory.observe(viewLifecycleOwner) { items ->
            playbackHistoryAdapter.submitList(items) {
                // 数据提交完成后，处理焦点设置
                handleFocusAfterDataLoad()
            }
        }

        viewModel.movies.observe(viewLifecycleOwner) { items ->
            moviesAdapter.submitList(items) {
                // 数据提交完成后，处理焦点设置
                handleFocusAfterDataLoad()
            }
        }

        viewModel.tvShows.observe(viewLifecycleOwner) { items ->
            tvShowsAdapter.submitList(items) {
                // 数据提交完成后，处理焦点设置
                handleFocusAfterDataLoad()
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: 显示/隐藏加载指示器
        }
    }

    private fun onMediaItemClick(mediaItem: com.tvplayer.webdav.data.model.MediaItem) {
        // 导航到视频详情页面
        val intent = android.content.Intent(requireContext(), com.tvplayer.webdav.ui.details.VideoDetailsActivity::class.java)
        intent.putExtra("media_item", mediaItem)
        startActivity(intent)
    }

    private fun onPosterFocused(mediaItem: com.tvplayer.webdav.data.model.MediaItem) {
        // 禁用遥控器选择时的背景图更新
        // val backdropUrl = mediaItem.backdropPath ?: mediaItem.posterPath
        // updateBackdropWithTransition(backdropUrl)
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
        // 禁用遥控器选择时的背景图更新
        // val backdropUrl = series.backdropPath ?: series.posterPath
        // updateBackdropWithTransition(backdropUrl)
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

    /**
     * 保存所有RecyclerView的焦点状态
     */
    private fun saveFocusStates() {
        try {
            focusStateManager.saveAllFocusStates(
                continueWatchingRv = null,
                playbackHistoryRv = if (::rvPlaybackHistory.isInitialized) rvPlaybackHistory else null,
                moviesRv = if (::rvMovies.isInitialized) rvMovies else null,
                tvShowsRv = if (::rvTVShows.isInitialized) rvTVShows else null
            )
            android.util.Log.d("HomeFragment", "Saved focus states")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error saving focus states", e)
        }
    }

    /**
     * 恢复所有RecyclerView的焦点状态
     */
    private fun restoreFocusStates() {
        try {
            // 检查适配器是否有数据
            val playbackHistoryHasData = ::playbackHistoryAdapter.isInitialized && playbackHistoryAdapter.itemCount > 0
            val moviesHasData = ::moviesAdapter.isInitialized && moviesAdapter.itemCount > 0
            val tvShowsHasData = ::tvShowsAdapter.isInitialized && tvShowsAdapter.itemCount > 0

            if (!playbackHistoryHasData && !moviesHasData && !tvShowsHasData) {
                android.util.Log.d("HomeFragment", "No data available for focus restoration, will retry later")
                // 如果没有数据，延迟重试
                view?.postDelayed({
                    restoreFocusStates()
                }, 500)
                return
            }

            // 获取最后聚焦的RecyclerView类型
            val lastFocusedType = focusStateManager.getLastFocusedRecyclerView()

            when (lastFocusedType) {
                FocusStateManager.RecyclerViewType.CONTINUE_WATCHING -> {
                    // 继续观看已移除，默认聚焦到电影区域
                    if (moviesHasData) {
                        rvMovies.post {
                            val firstViewHolder = rvMovies.findViewHolderForAdapterPosition(0)
                            firstViewHolder?.itemView?.requestFocus()
                        }
                    }
                }
                FocusStateManager.RecyclerViewType.PLAYBACK_HISTORY -> {
                    if (playbackHistoryHasData) {
                        focusStateManager.restoreFocusState(rvPlaybackHistory, FocusStateManager.RecyclerViewType.PLAYBACK_HISTORY)
                    }
                }
                FocusStateManager.RecyclerViewType.MOVIES -> {
                    if (moviesHasData) {
                        focusStateManager.restoreFocusState(rvMovies, FocusStateManager.RecyclerViewType.MOVIES)
                    }
                }
                FocusStateManager.RecyclerViewType.TV_SHOWS -> {
                    if (tvShowsHasData) {
                        focusStateManager.restoreFocusState(rvTVShows, FocusStateManager.RecyclerViewType.TV_SHOWS)
                    }
                }
                null -> {
                    // 首次进入应用时，按优先级设置默认焦点
                    setDefaultFocus()
                }
            }

            android.util.Log.d("HomeFragment", "Restored focus states, last focused: $lastFocusedType")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error restoring focus states", e)
        }
    }

    /**
     * 处理数据加载完成后的焦点设置
     */
    private fun handleFocusAfterDataLoad() {
        if (shouldRestoreFocus && isResumed) {
            view?.postDelayed({
                restoreFocusStates()
            }, 100)
        } else if (isFirstLaunch && isResumed) {
            view?.postDelayed({
                setDefaultFocus()
                isFirstLaunch = false
            }, 200)
        }
    }

    /**
     * 设置默认焦点，按优先级顺序：播放历史 → 电影 → 电视剧
     */
    private fun setDefaultFocus() {
        try {
            // 检查适配器是否有数据
            val playbackHistoryHasData = ::playbackHistoryAdapter.isInitialized && playbackHistoryAdapter.itemCount > 0
            val moviesHasData = ::moviesAdapter.isInitialized && moviesAdapter.itemCount > 0
            val tvShowsHasData = ::tvShowsAdapter.isInitialized && tvShowsAdapter.itemCount > 0

            android.util.Log.d("HomeFragment", "Setting default focus - History: $playbackHistoryHasData, Movies: $moviesHasData, TV: $tvShowsHasData")

            when {
                // 第一优先级：播放历史
                playbackHistoryHasData -> {
                    android.util.Log.d("HomeFragment", "Setting focus to playback history")
                    setFocusToRecyclerView(rvPlaybackHistory, "播放历史")
                }
                // 第二优先级：电影
                moviesHasData -> {
                    android.util.Log.d("HomeFragment", "Setting focus to movies")
                    setFocusToRecyclerView(rvMovies, "电影")
                }
                // 第三优先级：电视剧
                tvShowsHasData -> {
                    android.util.Log.d("HomeFragment", "Setting focus to TV shows")
                    setFocusToRecyclerView(rvTVShows, "电视剧")
                }
                else -> {
                    android.util.Log.w("HomeFragment", "No data available for default focus")
                    // 如果没有数据，延迟重试
                    view?.postDelayed({
                        setDefaultFocus()
                    }, 500)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting default focus", e)
        }
    }

    /**
     * 设置焦点到指定RecyclerView的第一个item
     */
    private fun setFocusToRecyclerView(recyclerView: RecyclerView, name: String) {
        recyclerView.post {
            try {
                // 确保RecyclerView已经布局完成
                if (recyclerView.adapter?.itemCount ?: 0 > 0) {
                    val firstViewHolder = recyclerView.findViewHolderForAdapterPosition(0)
                    if (firstViewHolder != null) {
                        val itemView = firstViewHolder.itemView

                        // 请求焦点
                        val focusResult = itemView.requestFocus()
                        android.util.Log.d("HomeFragment", "Focus request to $name result: $focusResult")

                        // 手动触发焦点动画和边框效果
                        itemView.postDelayed({
                            triggerFocusEffects(itemView)
                        }, 50)

                    } else {
                        android.util.Log.w("HomeFragment", "ViewHolder not found for $name, retrying...")
                        // ViewHolder还没创建，延迟重试
                        recyclerView.postDelayed({
                            setFocusToRecyclerView(recyclerView, name)
                        }, 100)
                    }
                } else {
                    android.util.Log.w("HomeFragment", "No items in $name adapter")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error setting focus to $name", e)
            }
        }
    }

    /**
     * 手动触发焦点效果（边框和缩放动画）
     */
    private fun triggerFocusEffects(itemView: View) {
        try {
            val cardView = itemView.findViewById<CardView>(R.id.card_view)
            val tvRating = itemView.findViewById<TextView>(R.id.tv_rating)

            // 使用PosterFocusAnimator手动触发焦点效果
            PosterFocusAnimator.restoreFocusWithAnimation(
                itemView, cardView, null, tvRating, true
            ) { hasFocus ->
                android.util.Log.d("HomeFragment", "Focus animation triggered, hasFocus: $hasFocus")
            }

            android.util.Log.d("HomeFragment", "Focus effects triggered for item")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error triggering focus effects", e)
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
