package com.tvplayer.webdav.ui.details

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.Actor
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.WebDAVFile
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * 视频详情页面Fragment
 * 根据设计图实现视频详情页面布局
 */
@AndroidEntryPoint
class VideoDetailsFragment : Fragment() {

    private val viewModel: VideoDetailsViewModel by viewModels()
    
    @Inject
    lateinit var webdavClient: SimpleWebDAVClient
    
    @Inject
    lateinit var serverStorage: WebDAVServerStorage
    
    private lateinit var mediaItem: MediaItem
    
    // UI组件
    private lateinit var ivBackdrop: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var btnPlay: Button
    private lateinit var progressPlayback: ProgressBar
    private lateinit var tvRating: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var tvOverview: TextView
    private lateinit var movieInfoContainer: LinearLayout
    private lateinit var scrollView: ScrollView

    // 滚动位置常量
    private val FIRST_SCREEN_HEIGHT = 600 // dp转换为px后使用
    private var firstScreenHeightPx = 0

    // 演员表组件
    private var rvActors: RecyclerView? = null
    private var tvFilename: TextView? = null
    private var tvSourcePath: TextView? = null
    private var tvDurationSize: TextView? = null
    private var btnBackToTop: View? = null

    // TV系列组件
    private var tvSeriesSection: LinearLayout? = null
    private var spinnerSeason: android.widget.Spinner? = null
    private var rvEpisodes: RecyclerView? = null
    private var episodeAdapter: EpisodeAdapter? = null

    companion object {
        private const val ARG_MEDIA_ITEM = "media_item"

        fun newInstance(mediaItem: MediaItem): VideoDetailsFragment {
            val fragment = VideoDetailsFragment()
            val args = Bundle()
            args.putParcelable(ARG_MEDIA_ITEM, mediaItem)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaItem = arguments?.getParcelable(ARG_MEDIA_ITEM) ?: run {
            android.util.Log.e("VideoDetailsFragment", "MediaItem is null in arguments")
            requireActivity().finish()
            return
        }
        android.util.Log.d("VideoDetailsFragment", "Fragment created with MediaItem: $mediaItem")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupData()
        setupListeners(view)
        observeViewModel()
        
        // 加载详情数据
        viewModel.loadVideoDetails(mediaItem)

        // 立即检查并设置TV系列UI（因为MediaItem已经可用）
        setupTVSeriesUI()
    }

    private fun initViews(view: View) {
        ivBackdrop = view.findViewById<ImageView>(R.id.iv_backdrop)
        tvTitle = view.findViewById<TextView>(R.id.tv_title)
        btnPlay = view.findViewById<Button>(R.id.btn_play)
        progressPlayback = view.findViewById<ProgressBar>(R.id.progress_playback)
        tvRating = view.findViewById<TextView>(R.id.tv_rating)
        tvYear = view.findViewById<TextView>(R.id.tv_year)
        tvDuration = view.findViewById<TextView>(R.id.tv_duration)
        tvGenre = view.findViewById<TextView>(R.id.tv_genre)
        tvFileSize = view.findViewById<TextView>(R.id.tv_file_size)
        tvOverview = view.findViewById<TextView>(R.id.tv_overview)
        movieInfoContainer = view.findViewById<LinearLayout>(R.id.movie_info_container)
        scrollView = view.findViewById<ScrollView>(R.id.scroll_view)

        // 计算第一屏高度的像素值
        firstScreenHeightPx = (FIRST_SCREEN_HEIGHT * resources.displayMetrics.density).toInt()

        // 初始化演员表相关组件（可选的，因为可能不存在）
        try {
            rvActors = view.findViewById<RecyclerView>(R.id.rv_actors)
            tvFilename = view.findViewById<TextView>(R.id.tv_filename)
            tvSourcePath = view.findViewById<TextView>(R.id.tv_source_path)
            tvDurationSize = view.findViewById<TextView>(R.id.tv_duration_size)
            btnBackToTop = view.findViewById<View>(R.id.btn_back_to_top)
        } catch (e: Exception) {
            // 如果找不到这些组件，说明布局没有包含演员表部分
        }

        // 初始化TV系列组件
        try {
            tvSeriesSection = view.findViewById<LinearLayout>(R.id.tv_series_section)
            spinnerSeason = view.findViewById<android.widget.Spinner>(R.id.spinner_season)
            rvEpisodes = view.findViewById<RecyclerView>(R.id.rv_episodes)
            android.util.Log.d("VideoDetailsFragment", "TV series components initialized: " +
                "tvSeriesSection=${tvSeriesSection != null}, " +
                "spinnerSeason=${spinnerSeason != null}, " +
                "rvEpisodes=${rvEpisodes != null}")
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Failed to initialize TV series components", e)
        }
    }

    private fun setupData() {
        // 设置基本信息
        tvTitle.text = mediaItem.getDisplayTitle()
        
        // 设置评分
        if (mediaItem.rating > 0) {
            tvRating.text = String.format("%.1f", mediaItem.rating)
            tvRating.visibility = View.VISIBLE
        } else {
            tvRating.visibility = View.GONE
        }
        
        // 设置年份（显示完整日期格式）
        mediaItem.releaseDate?.let { date ->
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            tvYear.text = formatter.format(date)
        } ?: run {
            tvYear.text = "未知日期"
        }
        
        // 设置时长（TV系列显示 总X集（库中有Y集），电影显示时长）
        if (viewModel.isTVSeries()) {
            val total = viewModel.tmdbTotalEpisodes.value ?: 0
            val local = viewModel.localAvailableEpisodes.value ?: 0
            tvDuration.text = "总${total}集（库中有${local}集）"
            tvDuration.visibility = View.VISIBLE
        } else {
            if (mediaItem.duration > 0) {
                val hours = mediaItem.duration / 3600
                val minutes = (mediaItem.duration % 3600) / 60
                tvDuration.text = if (hours > 0) {
                    "${hours}小时${minutes}分钟"
                } else {
                    "${minutes}分钟"
                }
            } else {
                tvDuration.visibility = View.GONE
            }
        }
        
        // 设置类型
        if (mediaItem.genre.isNotEmpty()) {
            tvGenre.text = mediaItem.genre.joinToString(" · ")
        } else {
            tvGenre.text = when (mediaItem.mediaType) {
                com.tvplayer.webdav.data.model.MediaType.MOVIE -> "电影"
                com.tvplayer.webdav.data.model.MediaType.TV_EPISODE -> "电视剧"
                com.tvplayer.webdav.data.model.MediaType.TV_SERIES -> "电视剧"
                else -> "视频"
            }
        }
        
        // 设置文件大小
        if (mediaItem.fileSize > 0) {
            val sizeInGB = mediaItem.fileSize / (1024.0 * 1024.0 * 1024.0)
            tvFileSize.text = String.format("%.1f GB", sizeInGB)
        } else {
            tvFileSize.visibility = View.GONE
        }
        
        // 设置简介
        if (!mediaItem.overview.isNullOrEmpty()) {
            tvOverview.text = mediaItem.overview
        } else {
            tvOverview.text = "暂无简介"
        }
        
        // 加载背景图片
        loadBackdropImage()
        
        // 设置演员表（如果存在）
        setupActorsIfAvailable()

        // 设置视频详情信息（如果存在）
        setupVideoDetailsIfAvailable()

        // 为演员表设置按键监听
        setupActorsKeyListener()

        // 观察播放状态变化，动态更新视频详情
        setupPlaybackStateObserver()
    }

    private fun loadBackdropImage() {
        val backdropUrl = mediaItem.backdropPath ?: mediaItem.posterPath
        if (!backdropUrl.isNullOrEmpty()) {
            try {
                com.bumptech.glide.Glide.with(this)
                    .load(backdropUrl)
                    .centerCrop()
                    .into(ivBackdrop)
            } catch (e: Exception) {
                // 加载失败时使用默认图片
                ivBackdrop.setImageResource(R.drawable.ic_video)
            }
        } else {
            ivBackdrop.setImageResource(R.drawable.ic_video)
        }
    }

    private fun setupListeners(view: View) {
        // 播放按钮点击
        btnPlay.setOnClickListener {
            startPlayback()
        }
        
        // 初始化播放按钮文字
        updatePlayButtonText()

        // 返回顶部按钮点击（如果存在）
        btnBackToTop?.setOnClickListener {
            scrollToTop()
        }

        // 设置播放按钮焦点效果
        btnPlay.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .start()
            } else {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }

        // 设置返回顶部按钮焦点效果（如果存在）
        btnBackToTop?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // 获取焦点时的动画：放大并增强阴影效果
                view.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .translationY(-2f)
                    .setDuration(250)
                    .setInterpolator(android.view.animation.OvershootInterpolator(0.8f))
                    .withStartAction {
                        // 添加轻微的透明度变化
                        view.alpha = 0.9f
                        view.animate().alpha(1.0f).setDuration(150).start()
                    }
                    .start()
            } else {
                // 失去焦点时的动画：平滑恢复原状
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .translationY(0f)
                    .setDuration(200)
                    .setInterpolator(android.view.animation.DecelerateInterpolator(1.2f))
                    .start()
            }
        }
        
        // 设置返回顶部按钮点击效果（如果存在）
        btnBackToTop?.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    // 按下时的反馈动画
                    view.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .start()
                    false
                }
                android.view.MotionEvent.ACTION_UP, 
                android.view.MotionEvent.ACTION_CANCEL -> {
                    // 释放时的恢复动画
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
                        .start()
                    false
                }
                else -> false
            }
        }
        
        // 设置返回顶部按钮点击事件（如果存在）
        btnBackToTop?.setOnClickListener {
            // 滚动到顶部的动画
            scrollView.smoothScrollTo(0, 0)
            
            // 添加视觉反馈
            it.animate()
                .rotation(360f)
                .setDuration(600)
                .withEndAction {
                    it.rotation = 0f
                }
                .start()
                
            // 显示提示信息
            android.widget.Toast.makeText(context, "已返回顶部", android.widget.Toast.LENGTH_SHORT).show()
        }

        // 设置按键监听器来处理遥控器滚动
        setupKeyListener(view)

        // 设置滚动监听器来处理遮罩层显示
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            handleScrollPosition(scrollY)
        }

        // 默认焦点设置到播放按钮
        btnPlay.requestFocus()
    }

    /**
     * 处理滚动位置变化，控制遮罩层显示
     */
    private fun handleScrollPosition(scrollY: Int) {
        // 当滚动到第二页位置时显示遮罩层
        if (scrollY >= firstScreenHeightPx) {
            // 添加半透明遮罩层
            addOverlayMask()
        } else {
            // 移除遮罩层逻辑
            removeOverlayMask()
        }
    }

    /**
     * 添加半透明遮罩层
     */
    private fun addOverlayMask() {
        val overlayMask = view?.findViewById<View>(R.id.overlay_mask)
        overlayMask?.let { mask ->
            if (mask.visibility != View.VISIBLE) {
                mask.alpha = 0f
                mask.visibility = View.VISIBLE
                mask.animate()
                    .alpha(1f)
                    .setDuration(100)
                    .start()
            }
        }
    }

    /**
     * 移除半透明遮罩层
     */
    private fun removeOverlayMask() {
        val overlayMask = view?.findViewById<View>(R.id.overlay_mask)
        overlayMask?.let { mask ->
            if (mask.visibility == View.VISIBLE) {
                mask.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .withEndAction {
                        mask.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    private fun observeViewModel() {
        // 观察演员数据变化
        viewModel.actors.observe(viewLifecycleOwner) { actors ->
            setupActors(actors)
        }

        // 观察媒体项目数据变化
        viewModel.mediaItem.observe(viewLifecycleOwner) { updatedMediaItem ->
            android.util.Log.d("VideoDetailsFragment", "MediaItem observer triggered: $updatedMediaItem")
            updatedMediaItem?.let { newMediaItem ->
                // 更新当前的mediaItem引用，以便setupVideoDetailsIfAvailable使用最新数据
                mediaItem = newMediaItem

                // 更新UI显示的媒体项目信息
                updateMediaItemDisplay(newMediaItem)
                // 设置TV系列相关UI（在MediaItem数据加载后）
                setupTVSeriesUI()
                // 重新设置视频详情信息（使用更新后的MediaItem）
                setupVideoDetailsIfAvailable()

                // 刷新基于集数的显示（例如 总X集（库中有Y集））
                if (viewModel.isTVSeries()) {
                    val total = viewModel.tmdbTotalEpisodes.value ?: 0
                    val local = viewModel.localAvailableEpisodes.value ?: 0
                    tvDuration.text = "总${total}集（库中有${local}集）"
                    // Note: tvDurationSize is now handled by setupVideoDetailsIfAvailable() to show episode duration/size
                }
            }
        }

        // 观察集数计数变化，实时刷新显示
        viewModel.tmdbTotalEpisodes.observe(viewLifecycleOwner) { total ->
            if (viewModel.isTVSeries()) {
                val local = viewModel.localAvailableEpisodes.value ?: 0
                tvDuration.text = "总${total}集（库中有${local}集）"
            }
        }
        viewModel.localAvailableEpisodes.observe(viewLifecycleOwner) { local ->
            if (viewModel.isTVSeries()) {
                val total = viewModel.tmdbTotalEpisodes.value ?: 0
                tvDuration.text = "总${total}集（库中有${local}集）"
            }
        }

        // 观察TV系列季数据变化
        viewModel.seasons.observe(viewLifecycleOwner) { seasons ->
            setupSeasonSpinner(seasons)
        }

        // 观察当前季变化
        viewModel.currentSeason.observe(viewLifecycleOwner) { seasonNumber ->
            updateSelectedSeason(seasonNumber)
        }

        // 观察剧集数据变化
        viewModel.episodes.observe(viewLifecycleOwner) { episodes ->
            setupEpisodes(episodes)
        }
        
        // 观察播放状态变化，实时更新播放按钮文字
        viewModel.getCurrentPlaybackState().observe(viewLifecycleOwner) { playbackState ->
            android.util.Log.d("VideoDetailsFragment", "Playback state changed, updating play button text")
            updatePlayButtonText()
        }
    }

    /**
     * 根据播放历史更新播放按钮文字和进度条
     */
    private fun updatePlayButtonText() {
        try {
            val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                            mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES
            
            if (isTVSeries) {
                // 电视剧逻辑
                val playbackState = getPlaybackStateForCurrentMedia()
                if (playbackState != null && playbackState.playbackProgress > 0) {
                    // 有播放历史，显示集数和进度
                    val episodeText = "第${playbackState.currentSeasonNumber}季第${playbackState.currentEpisodeNumber}集"
                    val progressText = playbackState.getFormattedProgress()
                    btnPlay.text = "播放 $episodeText $progressText"
                    
                    // 显示进度条
                    val progressPercentage = playbackState.getProgressPercentage()
                    progressPlayback.progress = (progressPercentage * 100).toInt()
                    progressPlayback.visibility = View.VISIBLE
                    
                    android.util.Log.d("VideoDetailsFragment", "Updated TV play button: 播放 $episodeText $progressText, progress: ${(progressPercentage * 100).toInt()}%")
                } else {
                    // 没有播放历史，播放第一集
                    btnPlay.text = "播放 第1季第1集"
                    progressPlayback.visibility = View.GONE
                    android.util.Log.d("VideoDetailsFragment", "Updated TV play button: 播放 第1季第1集")
                }
            } else {
                // 电影逻辑
                val playbackState = getPlaybackStateForCurrentMedia()
                if (playbackState != null && playbackState.playbackProgress > 0) {
                    // 有播放历史，显示播放进度
                    val progressText = playbackState.getFormattedProgress()
                    btnPlay.text = "播放 $progressText"
                    
                    // 显示进度条
                    val progressPercentage = playbackState.getProgressPercentage()
                    progressPlayback.progress = (progressPercentage * 100).toInt()
                    progressPlayback.visibility = View.VISIBLE
                    
                    android.util.Log.d("VideoDetailsFragment", "Updated movie play button: 播放 $progressText, progress: ${(progressPercentage * 100).toInt()}%")
                } else {
                    // 没有播放历史或者从头开始
                    btnPlay.text = "播放"
                    progressPlayback.visibility = View.GONE
                    android.util.Log.d("VideoDetailsFragment", "Updated movie play button: 播放")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error updating play button text and progress", e)
            btnPlay.text = "播放"
            progressPlayback.visibility = View.GONE
        }
    }
    
    /**
     * 获取当前媒体的播放状态
     */
    private fun getPlaybackStateForCurrentMedia(): com.tvplayer.webdav.data.model.PlaybackState? {
        return try {
            // 对于电影，使用文件路径作为seriesId
            val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                            mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES
                            
            if (isTVSeries) {
                // TV系列使用seriesId
                val seriesId = mediaItem.seriesId
                if (!seriesId.isNullOrBlank()) {
                    viewModel.getPlaybackState(seriesId)
                } else {
                    null
                }
            } else {
                // 电影使用文件路径作为唯一标识符
                val movieId = mediaItem.filePath
                if (movieId.isNotBlank()) {
                    viewModel.getPlaybackState(movieId)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error getting playback state", e)
            null
        }
    }

    private fun startPlayback() {
        val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                        mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES
        
        if (isTVSeries) {
            // 电视剧播放逻辑
            startTVSeriesPlayback()
        } else {
            // 电影播放逻辑
            startMoviePlayback()
        }
    }
    
    /**
     * 电视剧播放逻辑
     */
    private fun startTVSeriesPlayback() {
        try {
            val seriesId = mediaItem.seriesId
            if (seriesId.isNullOrBlank()) {
                android.widget.Toast.makeText(context, "无法播放：电视剧ID为空", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            
            // 获取当前系列的所有剧集
            val allItems = viewModel.getAllMediaItems()
            val seriesEpisodes = allItems.filter { item ->
                item.seriesId == seriesId &&
                item.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE
            }
            
            if (seriesEpisodes.isEmpty()) {
                android.widget.Toast.makeText(context, "无法播放：找不到剧集文件", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            
            // 按季数和集数排序
            val sortedEpisodes = seriesEpisodes.sortedWith(
                compareBy<com.tvplayer.webdav.data.model.MediaItem> { it.seasonNumber ?: 1 }
                    .thenBy { it.episodeNumber ?: 1 }
            )
            
            // 获取播放历史
            val playbackState = viewModel.getPlaybackState(seriesId)
            val targetEpisode = if (playbackState != null) {
                // 有播放历史，找到对应的剧集
                sortedEpisodes.find { episode ->
                    episode.seasonNumber == playbackState.currentSeasonNumber &&
                    episode.episodeNumber == playbackState.currentEpisodeNumber
                } ?: sortedEpisodes.first() // 如果找不到，就播放第一集
            } else {
                // 没有播放历史，播放第一集
                sortedEpisodes.first()
            }
            
            val startPosition = playbackState?.playbackProgress ?: 0L
            
            android.util.Log.d("VideoDetailsFragment", "Starting TV series playback:")
            android.util.Log.d("VideoDetailsFragment", "  Series: $seriesId")
            android.util.Log.d("VideoDetailsFragment", "  Episode: S${targetEpisode.seasonNumber}E${targetEpisode.episodeNumber}")
            android.util.Log.d("VideoDetailsFragment", "  Title: ${targetEpisode.title}")
            android.util.Log.d("VideoDetailsFragment", "  File: ${targetEpisode.filePath}")
            android.util.Log.d("VideoDetailsFragment", "  Start position: ${startPosition}s")
            
            // 启动具体剧集的播放
            startEpisodePlayback(targetEpisode, startPosition)
            
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error starting TV series playback", e)
            android.widget.Toast.makeText(context, "播放失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 启动具体剧集的播放
     */
    private fun startEpisodePlayback(episode: com.tvplayer.webdav.data.model.MediaItem, startPosition: Long) {
        // 使用WebDAV客户端生成正确的视频URL
        val rawPath = episode.filePath
        val path = try { decodeFilePath(rawPath) } catch (_: Exception) { rawPath }
        
        // 获取当前WebDAV服务器配置
        val server = serverStorage.getServer()
        if (server == null) {
            android.widget.Toast.makeText(context, "无法播放：未配置WebDAV服务器", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // 生成简化的WebDAV URL
        val webdavUrl = try {
            val baseUrl = server.url.removeSuffix("/")
            val normalizedPath = path.removePrefix("/")
            "$baseUrl/$normalizedPath"
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Failed to generate WebDAV URL", e)
            android.widget.Toast.makeText(context, "无法生成视频URL: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // 传递认证信息给PlayerActivity
        val uri = android.net.Uri.parse(webdavUrl)
        val intent = com.tvplayer.webdav.ui.player.PlayerActivity.intentFor(requireContext(), episode.getDisplayTitle(), uri)
        
        // 将认证信息作为额外参数传递
        intent.putExtra("webdav_username", server.username)
        intent.putExtra("webdav_password", server.password)
        
        // 传递播放位置信息
        intent.putExtra("start_position", startPosition)
        
        // 传递媒体标识符用于保存播放状态
        val seriesId = episode.seriesId ?: episode.filePath
        intent.putExtra("media_id", seriesId)
        intent.putExtra("media_title", episode.getDisplayTitle())
        
        startActivity(intent)
        
        // 开始播放时更新播放状态
        updateTVSeriesPlaybackStateOnStart(episode, startPosition)
    }
    
    /**
     * 电影播放逻辑
     */
    private fun startMoviePlayback() {
        // 使用WebDAV客户端生成正确的视频URL
        val rawPath = mediaItem.filePath
        val path = try { decodeFilePath(rawPath) } catch (_: Exception) { rawPath }
        
        // 获取当前WebDAV服务器配置
        val server = serverStorage.getServer()
        if (server == null) {
            android.widget.Toast.makeText(context, "无法播放：未配置WebDAV服务器", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取播放历史信息
        val playbackState = getPlaybackStateForCurrentMedia()
        val startPosition = playbackState?.playbackProgress ?: 0L // 秒为单位
        
        android.util.Log.d("VideoDetailsFragment", "Starting movie playback from position: ${startPosition}s")
        
        // 生成简化的WebDAV URL，防止编码问题导致循环
        val webdavUrl = try {
            val baseUrl = server.url.removeSuffix("/")
            val normalizedPath = path.removePrefix("/")
            "$baseUrl/$normalizedPath"
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Failed to generate WebDAV URL", e)
            android.widget.Toast.makeText(context, "无法生成视频URL: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        // 传递认证信息给PlayerActivity
        val uri = android.net.Uri.parse(webdavUrl)
        val intent = com.tvplayer.webdav.ui.player.PlayerActivity.intentFor(requireContext(), mediaItem.getDisplayTitle(), uri)
        
        // 将认证信息作为额外参数传递
        intent.putExtra("webdav_username", server.username)
        intent.putExtra("webdav_password", server.password)
        
        // 传递播放位置信息
        intent.putExtra("start_position", startPosition)
        
        // 传递媒体标识符用于保存播放状态
        val mediaId = mediaItem.filePath // 电影使用文件路径
        intent.putExtra("media_id", mediaId)
        intent.putExtra("media_title", mediaItem.getDisplayTitle())
        
        startActivity(intent)
        
        // 开始播放时更新播放状态
        updatePlaybackStateOnStart(mediaId, startPosition)
    }

    /**
     * 播放指定的剧集（从指定位置开始）
     */
    private fun startSpecificEpisodePlayback(episode: com.tvplayer.webdav.data.model.MediaItem, startPosition: Long) {
        try {
            android.util.Log.d("VideoDetailsFragment", "Starting specific episode playback:")
            android.util.Log.d("VideoDetailsFragment", "  Episode: S${episode.seasonNumber}E${episode.episodeNumber}")
            android.util.Log.d("VideoDetailsFragment", "  Title: ${episode.title}")
            android.util.Log.d("VideoDetailsFragment", "  File: ${episode.filePath}")
            android.util.Log.d("VideoDetailsFragment", "  Start position: ${startPosition}s")
            
            // 直接启动该剧集的播放
            startEpisodePlayback(episode, startPosition)
            
            // 更新播放状态为该剧集
            updateTVSeriesPlaybackStateOnStart(episode, startPosition)
            
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error starting specific episode playback", e)
            android.widget.Toast.makeText(context, "播放失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 电视剧开始播放时更新播放状态
     */
    private fun updateTVSeriesPlaybackStateOnStart(episode: com.tvplayer.webdav.data.model.MediaItem, startPosition: Long) {
        try {
            val seriesId = episode.seriesId
            if (seriesId.isNullOrBlank()) {
                android.util.Log.w("VideoDetailsFragment", "No seriesId for TV episode, cannot update playback state")
                return
            }
            
            val seasonNumber = episode.seasonNumber ?: 1
            val episodeNumber = episode.episodeNumber ?: 1
            
            val existingState = viewModel.getPlaybackState(seriesId)
            if (existingState != null) {
                // 更新现有状态
                val updatedState = existingState.copy(
                    currentSeasonNumber = seasonNumber,
                    currentEpisodeNumber = episodeNumber,
                    playbackProgress = startPosition,
                    totalDuration = episode.duration,
                    lastPlayedTimestamp = java.util.Date()
                )
                viewModel.savePlaybackState(updatedState)
            } else {
                // 创建新的播放状态
                val newState = com.tvplayer.webdav.data.model.PlaybackState(
                    seriesId = seriesId,
                    currentSeasonNumber = seasonNumber,
                    currentEpisodeNumber = episodeNumber,
                    playbackProgress = startPosition,
                    totalDuration = episode.duration
                )
                viewModel.savePlaybackState(newState)
            }
            
            android.util.Log.d("VideoDetailsFragment", "Updated TV series playback state: $seriesId S${seasonNumber}E${episodeNumber}, position: ${startPosition}s")
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error updating TV series playback state", e)
        }
    }

    /**
     * 开始播放时更新播放状态
     */
    private fun updatePlaybackStateOnStart(mediaId: String, startPosition: Long) {
        try {
            val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                            mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES
            
            if (isTVSeries) {
                // TV系列使用现有的PlaybackState管理
                val seriesId = mediaItem.seriesId
                if (!seriesId.isNullOrBlank()) {
                    val existingState = viewModel.getPlaybackState(seriesId)
                    if (existingState != null) {
                        // 更新现有状态的最后播放时间
                        val updatedState = existingState.copy(lastPlayedTimestamp = java.util.Date())
                        viewModel.savePlaybackState(updatedState)
                    } else {
                        // 创建新的播放状态
                        val newState = com.tvplayer.webdav.data.model.PlaybackState(
                            seriesId = seriesId,
                            currentSeasonNumber = mediaItem.seasonNumber ?: 1,
                            currentEpisodeNumber = mediaItem.episodeNumber ?: 1,
                            playbackProgress = startPosition,
                            totalDuration = mediaItem.duration
                        )
                        viewModel.savePlaybackState(newState)
                    }
                }
            } else {
                // 电影使用文件路径作为seriesId，创建一个简单的PlaybackState
                val existingState = viewModel.getPlaybackState(mediaId)
                if (existingState != null) {
                    // 更新现有状态
                    val updatedState = existingState.copy(
                        lastPlayedTimestamp = java.util.Date(),
                        playbackProgress = startPosition
                    )
                    viewModel.savePlaybackState(updatedState)
                } else {
                    // 创建新的电影播放状态
                    val newState = com.tvplayer.webdav.data.model.PlaybackState(
                        seriesId = mediaId, // 使用文件路径作为唯一标识符
                        currentSeasonNumber = 1, // 电影不需要季数
                        currentEpisodeNumber = 1, // 电影不需要集数
                        playbackProgress = startPosition,
                        totalDuration = mediaItem.duration
                    )
                    viewModel.savePlaybackState(newState)
                }
            }
            
            android.util.Log.d("VideoDetailsFragment", "Updated playback state for mediaId: $mediaId, position: ${startPosition}s")
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error updating playback state on start", e)
        }
    }

    private fun setupActorsIfAvailable() {
        // 这个方法现在只初始化RecyclerView，不设置数据
        // 数据将通过ViewModel的LiveData来更新
        rvActors?.let { recyclerView ->
            // 设置RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            // 初始设置为空列表
            val actorAdapter = ActorAdapter(emptyList()) { actor ->
                // 处理演员点击事件
                android.widget.Toast.makeText(context, "点击了: ${actor.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = actorAdapter
        }
    }
    
    private fun setupActors(actors: List<Actor>) {
        rvActors?.let { recyclerView ->
            val adapter = recyclerView.adapter as? ActorAdapter
            if (adapter != null) {
                // 更新现有适配器的数据
                adapter.updateActors(actors)
            } else {
                // 创建新的适配器
                recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val actorAdapter = ActorAdapter(actors) { actor ->
                    // 处理演员点击事件
                    android.widget.Toast.makeText(context, "点击了: ${actor.name}", android.widget.Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = actorAdapter
            }
        }
    }
    
    private fun updateMediaItemDisplay(updatedMediaItem: MediaItem) {
        // 更新标题
        tvTitle.text = updatedMediaItem.getDisplayTitle()
        
        // 更新评分
        if (updatedMediaItem.rating > 0) {
            tvRating.text = String.format("%.1f", updatedMediaItem.rating)
            tvRating.visibility = View.VISIBLE
        } else {
            tvRating.visibility = View.GONE
        }
        
        // 更新年份
        updatedMediaItem.releaseDate?.let { date ->
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            tvYear.text = formatter.format(date)
        } ?: run {
            tvYear.text = "未知日期"
        }
        
        // 更新时长（TV系列显示 总X集（库中有Y集），电影显示时长）
        if (viewModel.isTVSeries()) {
            val total = viewModel.tmdbTotalEpisodes.value ?: 0
            val local = viewModel.localAvailableEpisodes.value ?: 0
            tvDuration.text = "总${total}集（库中有${local}集）"
            tvDuration.visibility = View.VISIBLE
        } else {
            if (updatedMediaItem.duration > 0) {
                val hours = updatedMediaItem.duration / 3600
                val minutes = (updatedMediaItem.duration % 3600) / 60
                tvDuration.text = if (hours > 0) {
                    "${hours}小时${minutes}分钟"
                } else {
                    "${minutes}分钟"
                }
            } else {
                tvDuration.visibility = View.GONE
            }
        }
        
        // 更新类型
        if (updatedMediaItem.genre.isNotEmpty()) {
            tvGenre.text = updatedMediaItem.genre.joinToString(" · ")
        } else {
            tvGenre.text = when (updatedMediaItem.mediaType) {
                com.tvplayer.webdav.data.model.MediaType.MOVIE -> "电影"
                com.tvplayer.webdav.data.model.MediaType.TV_EPISODE -> "电视剧"
                com.tvplayer.webdav.data.model.MediaType.TV_SERIES -> "电视剧"
                else -> "视频"
            }
        }
        
        // 更新简介
        if (!updatedMediaItem.overview.isNullOrEmpty()) {
            tvOverview.text = updatedMediaItem.overview
        } else {
            tvOverview.text = "暂无简介"
        }
        
        // 更新背景图片
        val backdropUrl = updatedMediaItem.backdropPath ?: updatedMediaItem.posterPath
        if (!backdropUrl.isNullOrEmpty()) {
            try {
                com.bumptech.glide.Glide.with(this)
                    .load(backdropUrl)
                    .centerCrop()
                    .into(ivBackdrop)
            } catch (e: Exception) {
                // 加载失败时使用默认图片
                ivBackdrop.setImageResource(R.drawable.ic_video)
            }
        } else {
            ivBackdrop.setImageResource(R.drawable.ic_video)
        }
    }

    private fun setupVideoDetailsIfAvailable() {
        // 检查是否为TV系列
        val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                        mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES

        // 对于TV系列，获取当前播放剧集的文件信息来显示；对于电影，使用当前MediaItem
        val displayMediaItem = if (isTVSeries) {
            getCurrentlyPlayingEpisodeForDisplay() ?: mediaItem
        } else {
            mediaItem
        }

        android.util.Log.d("VideoDetailsFragment", "setupVideoDetailsIfAvailable: isTVSeries=$isTVSeries, using filePath: ${displayMediaItem.filePath}")

        // 设置文件名和技术信息（从filePath中提取文件名）
        tvFilename?.let { textView ->
            val fileName = try {
                val path = decodeFilePath(displayMediaItem.filePath)
                if (path.isNotBlank()) {
                    val fullFileName = path.substringAfterLast('/')
                    if (fullFileName.isNotBlank()) {
                        fullFileName
                    } else {
                        "未知文件"
                    }
                } else {
                    "未知文件"
                }
            } catch (e: Exception) {
                "未知文件"
            }
            textView.text = fileName
            textView.visibility = View.VISIBLE
        }

        // 设置来源路径
        tvSourcePath?.let { textView ->
            val sourcePath = try {
                val path = decodeFilePath(displayMediaItem.filePath)
                if (path.isNotBlank()) {
                    val lastSlashIndex = path.lastIndexOf('/')
                    if (lastSlashIndex > 0) {
                        // 格式化显示路径，添加前缀说明
                        val directory = path.substring(0, lastSlashIndex + 1)
                        "WebDAV: $directory"
                    } else {
                        "WebDAV: $path"
                    }
                } else {
                    "未知路径"
                }
            } catch (e: Exception) {
                "未知路径"
            }
            textView.text = sourcePath
            textView.visibility = View.VISIBLE
        }
        
        // 设置时长和大小信息（对于TV系列和电影都使用相同的格式）
        tvDurationSize?.let { textView ->
            android.util.Log.d("VideoDetailsFragment", "=== Setting Duration/Size Info ===")
            android.util.Log.d("VideoDetailsFragment", "displayMediaItem.duration: ${displayMediaItem.duration}")
            android.util.Log.d("VideoDetailsFragment", "displayMediaItem.fileSize: ${displayMediaItem.fileSize}")
            android.util.Log.d("VideoDetailsFragment", "displayMediaItem.title: ${displayMediaItem.title}")
            android.util.Log.d("VideoDetailsFragment", "displayMediaItem.filePath: ${displayMediaItem.filePath}")

            val durationText = if (displayMediaItem.duration > 0) {
                val hours = displayMediaItem.duration / 3600
                val minutes = (displayMediaItem.duration % 3600) / 60
                if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
            } else {
                android.util.Log.w("VideoDetailsFragment", "Duration is 0 or negative: ${displayMediaItem.duration}")
                "未知时长"
            }

            val sizeText = if (displayMediaItem.fileSize > 0) {
                val sizeInGB = displayMediaItem.fileSize / (1024.0 * 1024.0 * 1024.0)
                String.format("%.2f GB", sizeInGB)
            } else {
                android.util.Log.w("VideoDetailsFragment", "File size is 0 or negative: ${displayMediaItem.fileSize}")
                "未知大小"
            }

            // 检测视频分辨率
            val resolutionText = detectVideoResolution(displayMediaItem)
            android.util.Log.d("VideoDetailsFragment", "Detected resolution: $resolutionText")

            val finalText = "$durationText $resolutionText $sizeText"
            android.util.Log.d("VideoDetailsFragment", "Setting tvDurationSize text to: '$finalText'")
            textView.text = finalText
            textView.visibility = View.VISIBLE

            // Verify the text was actually set
            android.util.Log.d("VideoDetailsFragment", "tvDurationSize actual text after setting: '${textView.text}'")
        }
    }

    /**
     * 获取当前播放剧集的MediaItem用于显示文件信息
     * 优先返回当前播放的剧集，如果没有播放状态则返回第一集
     */
    private fun getCurrentlyPlayingEpisodeForDisplay(): MediaItem? {
        return try {
            // 从ViewModel获取当前系列的所有剧集
            val seriesId = mediaItem.seriesId
            if (seriesId.isNullOrBlank()) {
                android.util.Log.w("VideoDetailsFragment", "No seriesId found for TV series")
                return null
            }

            // 从MediaCache获取所有媒体项目
            val allItems = viewModel.getAllMediaItems()

            // 找到同一系列的所有剧集
            val seriesEpisodes = allItems.filter { item ->
                item.seriesId == seriesId &&
                item.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE
            }

            android.util.Log.d("VideoDetailsFragment", "Found ${seriesEpisodes.size} episodes for series: $seriesId")
            seriesEpisodes.forEach { episode ->
                android.util.Log.d("VideoDetailsFragment", "Episode: S${episode.seasonNumber}E${episode.episodeNumber}")
                android.util.Log.d("VideoDetailsFragment", "  - Title: ${episode.title}")
                android.util.Log.d("VideoDetailsFragment", "  - Duration: ${episode.duration}s")
                android.util.Log.d("VideoDetailsFragment", "  - File Size: ${episode.fileSize} bytes")
                android.util.Log.d("VideoDetailsFragment", "  - File Path: ${episode.filePath}")
            }

            if (seriesEpisodes.isEmpty()) {
                android.util.Log.w("VideoDetailsFragment", "No episodes found for series: $seriesId")
                return null
            }

            // 检查是否有播放状态
            val playbackState = viewModel.getPlaybackState(seriesId)
            val targetEpisode = if (playbackState != null) {
                // 找到当前播放的剧集
                seriesEpisodes.find { episode ->
                    episode.seasonNumber == playbackState.currentSeasonNumber &&
                    episode.episodeNumber == playbackState.currentEpisodeNumber
                }.also {
                    android.util.Log.d("VideoDetailsFragment", "Found currently playing episode: S${playbackState.currentSeasonNumber}E${playbackState.currentEpisodeNumber}, path: ${it?.filePath}")
                }
            } else {
                // 没有播放状态，返回第一集
                seriesEpisodes.sortedWith(
                    compareBy<MediaItem> { it.seasonNumber ?: 1 }
                        .thenBy { it.episodeNumber ?: 1 }
                ).firstOrNull().also {
                    android.util.Log.d("VideoDetailsFragment", "No playback state found, using first episode: S${it?.seasonNumber}E${it?.episodeNumber}, path: ${it?.filePath}")
                }
            }

            targetEpisode
        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error getting currently playing episode", e)
            null
        }
    }

    /**
     * 设置播放状态观察者，当播放状态变化时动态更新视频详情
     */
    private fun setupPlaybackStateObserver() {
        // 只对TV系列设置播放状态观察者
        val isTVSeries = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                        mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES

        if (isTVSeries) {
            viewModel.getCurrentPlaybackState().observe(viewLifecycleOwner) { playbackState ->
                android.util.Log.d("VideoDetailsFragment", "Playback state changed: ${playbackState?.getEpisodeIdentifier()}")

                // 当播放状态变化时，重新设置视频详情信息
                if (playbackState != null && playbackState.seriesId == mediaItem.seriesId) {
                    android.util.Log.d("VideoDetailsFragment", "Updating video details for playback state change")
                    setupVideoDetailsIfAvailable()
                }
            }
        }
    }

    /**
     * 开始播放指定剧集（用于测试和实际播放）
     */
    fun startPlaybackForEpisode(seasonNumber: Int, episodeNumber: Int, duration: Long = 0L) {
        val seriesId = mediaItem.seriesId
        if (!seriesId.isNullOrBlank()) {
            android.util.Log.d("VideoDetailsFragment", "Starting playback for S${seasonNumber}E${episodeNumber}")
            viewModel.startPlayback(seriesId, seasonNumber, episodeNumber, duration)
        }
    }

    /**
     * 更新播放进度（用于实际播放器集成）
     */
    fun updatePlaybackProgress(progress: Long, duration: Long = 0L) {
        val seriesId = mediaItem.seriesId
        if (!seriesId.isNullOrBlank()) {
            viewModel.updatePlaybackProgress(seriesId, progress, duration)
        }
    }

    /**
     * 切换到下一集
     */
    fun switchToNextEpisode(nextSeasonNumber: Int, nextEpisodeNumber: Int) {
        val seriesId = mediaItem.seriesId
        if (!seriesId.isNullOrBlank()) {
            android.util.Log.d("VideoDetailsFragment", "Switching to next episode: S${nextSeasonNumber}E${nextEpisodeNumber}")
            viewModel.switchToNextEpisode(seriesId, nextSeasonNumber, nextEpisodeNumber)
        }
    }

    /**
     * 测试播放状态功能（可以通过按键或其他方式触发）
     */
    private fun testPlaybackStateFeature() {
        val seriesId = mediaItem.seriesId
        if (!seriesId.isNullOrBlank()) {
            // 模拟播放第2季第3集
            android.util.Log.d("VideoDetailsFragment", "Testing playback state: switching to S02E03")
            startPlaybackForEpisode(2, 3, 3600L) // 1小时时长

            // 模拟播放进度到30分钟
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                updatePlaybackProgress(1800L, 3600L) // 30分钟进度
                android.util.Log.d("VideoDetailsFragment", "Updated playback progress to 30 minutes")
            }, 2000)
        }
    }

    /**
     * 从MediaItem中检测视频分辨率 - 优化版本
     * 基于行业标准和真实世界的视频文件命名约定
     */
    private fun detectVideoResolution(mediaItem: MediaItem): String {
        return try {
            val filePath = mediaItem.filePath.lowercase()
            val fileName = filePath.substringAfterLast('/').substringBeforeLast('.')
            val fullPath = filePath.lowercase()

            android.util.Log.d("VideoDetailsFragment", "Detecting resolution for: $fileName")
            android.util.Log.d("VideoDetailsFragment", "Full path: $fullPath")

            // 第一阶段：直接分辨率标识符检测（最高优先级）
            val directResolution = detectDirectResolutionMarkers(fileName, fullPath)
            if (directResolution != null) {
                android.util.Log.d("VideoDetailsFragment", "Direct resolution detected: $directResolution")
                return directResolution
            }

            // 第二阶段：质量标签推断
            val qualityResolution = detectResolutionFromQualityTags(fileName, fullPath)
            if (qualityResolution != null) {
                android.util.Log.d("VideoDetailsFragment", "Quality-based resolution detected: $qualityResolution")
                return qualityResolution
            }

            // 第三阶段：正则表达式模式匹配
            val regexResolution = detectResolutionFromRegexPatterns(fileName, fullPath)
            if (regexResolution != null) {
                android.util.Log.d("VideoDetailsFragment", "Regex-based resolution detected: $regexResolution")
                return regexResolution
            }

            // 第四阶段：文件大小和时长推断
            val inferredResolution = inferResolutionFromFileMetadata(mediaItem)
            if (inferredResolution != null) {
                android.util.Log.d("VideoDetailsFragment", "Inferred resolution from metadata: $inferredResolution")
                return inferredResolution
            }

            android.util.Log.d("VideoDetailsFragment", "No resolution pattern found, defaulting to 其他")
            "其他"

        } catch (e: Exception) {
            android.util.Log.e("VideoDetailsFragment", "Error detecting video resolution", e)
            "其他"
        }
    }

    /**
     * 第一阶段：检测直接分辨率标识符
     */
    private fun detectDirectResolutionMarkers(fileName: String, fullPath: String): String? {
        // 4K/UHD 检测 - 最全面的4K标识符
        val fourKPatterns = listOf(
            "4k", "2160p", "uhd", "ultra.hd", "ultrahd", "uhdtv", "4k.uhd",
            "3840x2160", "4096x2160", "2160", "uhd.4k", "4k.2160p"
        )

        // 2K/QHD 检测
        val twoKPatterns = listOf(
            "2k", "1440p", "qhd", "quad.hd", "quadhd", "wqhd",
            "2560x1440", "2048x1080", "1440", "2k.qhd"
        )

        // 1080P/FHD 检测 - 包含各种变体
        val fullHDPatterns = listOf(
            "1080p", "1080i", "fhd", "full.hd", "fullhd", "1920x1080",
            "1080", "fhd.1080p", "bluray.1080p", "web.1080p"
        )

        // 720P/HD 检测
        val hdPatterns = listOf(
            "720p", "720i", "hd", "1280x720", "720", "hd.720p",
            "web.720p", "hdtv.720p"
        )

        // 检查4K模式
        for (pattern in fourKPatterns) {
            if (fileName.contains(pattern) || fullPath.contains(pattern)) {
                return "4K"
            }
        }

        // 检查2K模式
        for (pattern in twoKPatterns) {
            if (fileName.contains(pattern) || fullPath.contains(pattern)) {
                return "2K"
            }
        }

        // 检查1080P模式
        for (pattern in fullHDPatterns) {
            if (fileName.contains(pattern) || fullPath.contains(pattern)) {
                return "1080P"
            }
        }

        // 检查720P模式
        for (pattern in hdPatterns) {
            if (fileName.contains(pattern) || fullPath.contains(pattern)) {
                return "720P"
            }
        }

        return null
    }

    /**
     * 第二阶段：从质量标签推断分辨率
     */
    private fun detectResolutionFromQualityTags(fileName: String, fullPath: String): String? {
        // 4K质量标签 - 通常与4K内容相关
        val fourKQualityTags = listOf(
            "remux", "bdremux", "uhd.remux", "4k.remux", "atmos", "dv", "dolby.vision",
            "hdr10", "hdr", "imax", "criterion", "masters"
        )

        // 1080P质量标签 - 高质量但通常是1080P
        val fullHDQualityTags = listOf(
            "bluray", "blu.ray", "bdrip", "brip", "brrip", "web.dl", "webdl", "webrip",
            "netflix", "amazon", "hulu", "disney", "hbo", "apple.tv", "paramount"
        )

        // 720P质量标签 - 中等质量
        val hdQualityTags = listOf(
            "hdtv", "hdcam", "hdts", "hdtc", "web.720p", "iptv"
        )

        // 低质量标签 - 通常是720P或更低
        val standardQualityTags = listOf(
            "dvdrip", "dvd.rip", "dvdscr", "cam", "ts", "tc", "workprint", "r5", "r6"
        )

        // 检查4K质量标签
        for (tag in fourKQualityTags) {
            if (fileName.contains(tag) || fullPath.contains(tag)) {
                // 如果有4K质量标签但没有明确的分辨率，推断为4K
                return "4K"
            }
        }

        // 检查1080P质量标签
        for (tag in fullHDQualityTags) {
            if (fileName.contains(tag) || fullPath.contains(tag)) {
                // 如果有高质量标签但没有明确分辨率，推断为1080P
                return "1080P"
            }
        }

        // 检查720P质量标签
        for (tag in hdQualityTags) {
            if (fileName.contains(tag) || fullPath.contains(tag)) {
                return "720P"
            }
        }

        // 检查标准质量标签
        for (tag in standardQualityTags) {
            if (fileName.contains(tag) || fullPath.contains(tag)) {
                return "720P" // 大多数DVD rips是720P或更低
            }
        }

        return null
    }

    /**
     * 第三阶段：正则表达式模式匹配
     */
    private fun detectResolutionFromRegexPatterns(fileName: String, fullPath: String): String? {
        // 增强的正则表达式模式
        val patterns = listOf(
            // 标准分辨率格式: 1920x1080, 3840x2160等
            Regex("(\\d{3,4})\\s*[x×]\\s*(\\d{3,4})"),
            // P格式: 1080p, 720p, 2160p等
            Regex("(\\d{3,4})p", RegexOption.IGNORE_CASE),
            // I格式: 1080i, 720i等
            Regex("(\\d{3,4})i", RegexOption.IGNORE_CASE),
            // 带点分隔: 1920.1080, 3840.2160
            Regex("(\\d{3,4})\\.\\s*(\\d{3,4})"),
            // 带下划线: 1920_1080
            Regex("(\\d{3,4})_\\s*(\\d{3,4})"),
            // 方括号格式: [1080p], [720p]
            Regex("\\[(\\d{3,4})[pi]\\]", RegexOption.IGNORE_CASE),
            // 圆括号格式: (1080p), (720p)
            Regex("\\((\\d{3,4})[pi]\\)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(fileName) ?: pattern.find(fullPath)
            if (match != null) {
                val width: Int
                val height: Int

                when {
                    // 宽x高格式
                    match.groupValues.size >= 3 && match.groupValues[1].isNotEmpty() && match.groupValues[2].isNotEmpty() -> {
                        width = match.groupValues[1].toIntOrNull() ?: 0
                        height = match.groupValues[2].toIntOrNull() ?: 0
                    }
                    // 单一数字格式 (p/i)
                    match.groupValues.size >= 2 && match.groupValues[1].isNotEmpty() -> {
                        height = match.groupValues[1].toIntOrNull() ?: 0
                        width = when (height) {
                            2160 -> 3840
                            1440 -> 2560
                            1080 -> 1920
                            720 -> 1280
                            576 -> 720  // PAL
                            480 -> 640  // NTSC
                            else -> 0
                        }
                    }
                    else -> {
                        width = 0
                        height = 0
                    }
                }

                android.util.Log.d("VideoDetailsFragment", "Regex extracted resolution: ${width}x${height}")

                // 分类分辨率
                return categorizeResolution(width, height)
            }
        }

        return null
    }

    /**
     * 第四阶段：从文件元数据推断分辨率
     */
    private fun inferResolutionFromFileMetadata(mediaItem: MediaItem): String? {
        val fileSize = mediaItem.fileSize
        val duration = mediaItem.duration

        if (fileSize <= 0 || duration <= 0) {
            return null
        }

        // 计算比特率 (bytes per second)
        val bitrate = fileSize.toDouble() / duration.toDouble()

        // 基于文件大小和时长的启发式推断
        // 这些值基于典型的视频编码比特率
        return when {
            // 4K内容通常有很高的比特率
            bitrate > 2_000_000 -> { // > 2MB/s
                android.util.Log.d("VideoDetailsFragment", "High bitrate detected (${String.format("%.2f", bitrate / 1_000_000)} MB/s), inferring 4K")
                "4K"
            }
            // 1080P内容的典型比特率
            bitrate > 800_000 -> { // > 800KB/s
                android.util.Log.d("VideoDetailsFragment", "Medium-high bitrate detected (${String.format("%.2f", bitrate / 1_000_000)} MB/s), inferring 1080P")
                "1080P"
            }
            // 720P内容的典型比特率
            bitrate > 300_000 -> { // > 300KB/s
                android.util.Log.d("VideoDetailsFragment", "Medium bitrate detected (${String.format("%.2f", bitrate / 1_000_000)} MB/s), inferring 720P")
                "720P"
            }
            else -> {
                android.util.Log.d("VideoDetailsFragment", "Low bitrate detected (${String.format("%.2f", bitrate / 1_000_000)} MB/s), cannot infer resolution")
                null
            }
        }
    }

    /**
     * 辅助方法：根据宽度和高度分类分辨率
     */
    private fun categorizeResolution(width: Int, height: Int): String? {
        return when {
            height >= 2160 || width >= 3840 -> "4K"
            height >= 1440 || width >= 2560 -> "2K"
            height >= 1080 || width >= 1920 -> "1080P"
            height >= 720 || width >= 1280 -> "720P"
            height >= 576 || width >= 720 -> "720P"  // PAL标准
            height >= 480 || width >= 640 -> "720P"  // NTSC标准，归类为720P
            else -> null
        }
    }

    /**
     * 测试视频详情显示功能
     */
    private fun testVideoDetailsDisplay() {
        val seriesId = mediaItem.seriesId
        if (!seriesId.isNullOrBlank()) {
            android.util.Log.d("VideoDetailsFragment", "=== Testing Video Details Display ===")
            android.util.Log.d("VideoDetailsFragment", "Series ID: $seriesId")
            android.util.Log.d("VideoDetailsFragment", "Original MediaItem: ${mediaItem.title}")
            android.util.Log.d("VideoDetailsFragment", "Original duration: ${mediaItem.duration}s, fileSize: ${mediaItem.fileSize} bytes")

            val currentEpisode = getCurrentlyPlayingEpisodeForDisplay()
            if (currentEpisode != null) {
                android.util.Log.d("VideoDetailsFragment", "Current Episode: S${currentEpisode.seasonNumber}E${currentEpisode.episodeNumber}")
                android.util.Log.d("VideoDetailsFragment", "Episode duration: ${currentEpisode.duration}s, fileSize: ${currentEpisode.fileSize} bytes")
                android.util.Log.d("VideoDetailsFragment", "Episode file path: ${currentEpisode.filePath}")

                // Test resolution detection
                val detectedResolution = detectVideoResolution(currentEpisode)
                android.util.Log.d("VideoDetailsFragment", "Detected resolution: $detectedResolution")
            } else {
                android.util.Log.w("VideoDetailsFragment", "No current episode found!")
            }

            // Check if tvDurationSize exists
            android.util.Log.d("VideoDetailsFragment", "tvDurationSize exists: ${tvDurationSize != null}")
            android.util.Log.d("VideoDetailsFragment", "Current tvDurationSize text: '${tvDurationSize?.text}'")

            // 刷新显示
            setupVideoDetailsIfAvailable()

            // Check again after refresh
            android.util.Log.d("VideoDetailsFragment", "After refresh tvDurationSize text: '${tvDurationSize?.text}'")
        }
    }

    /**
     * 测试分辨率检测功能 - 增强版本
     */
    private fun testResolutionDetection() {
        android.util.Log.d("VideoDetailsFragment", "=== Enhanced Resolution Detection Testing ===")

        // 测试各种真实世界的文件名格式
        val testCases = listOf(
            // 4K测试用例
            "Avengers.Endgame.2019.4K.UHD.2160p.BluRay.x265.HDR.mkv" to "4K",
            "The.Matrix.1999.UHD.BluRay.2160p.DTS-HD.MA.5.1.HEVC.REMUX.mkv" to "4K",
            "Dune.2021.3840x2160.HDR10.Dolby.Vision.mkv" to "4K",
            "Movie.4K.IMAX.Enhanced.mkv" to "4K",

            // 2K测试用例
            "Game.of.Thrones.S08E06.2K.QHD.1440p.WEB-DL.mkv" to "2K",
            "Film.2560x1440.WQHD.mp4" to "2K",

            // 1080P测试用例
            "Breaking.Bad.S01E01.1080p.BluRay.x264.mkv" to "1080P",
            "Movie.2023.FHD.1080p.WEB-DL.H264.mp4" to "1080P",
            "Series.1920x1080.Netflix.WEBRip.mkv" to "1080P",
            "Film.BRRip.1080p.x265.mp4" to "1080P",
            "Show.WEB.1080p.Amazon.Prime.mkv" to "1080P",
            "Content.BluRay.Remux.1080p.mkv" to "1080P",

            // 720P测试用例
            "Series.S01E01.720p.HDTV.x264.mkv" to "720P",
            "Movie.720p.WEB-DL.DD5.1.H264.mp4" to "720P",
            "Show.1280x720.HDTV.mkv" to "720P",
            "Film.DVDRip.720p.XviD.avi" to "720P",

            // 边缘情况
            "Movie.[1080p].BluRay.mkv" to "1080P",
            "Series.(720p).WEB-DL.mp4" to "720P",
            "Film_1920_1080_H264.mkv" to "1080P",
            "Show.1080i.HDTV.mkv" to "1080P",
            "Content.576p.DVDRip.avi" to "720P",

            // 质量标签推断测试
            "Movie.BluRay.Remux.No.Resolution.mkv" to "1080P",
            "Series.Netflix.WEBRip.Unknown.mkv" to "1080P",
            "Film.HDTV.Capture.mkv" to "720P",
            "Show.DVDRip.XviD.avi" to "720P",

            // 应该检测为"其他"的情况
            "Movie.CAM.LowQuality.avi" to "其他",
            "Series.Unknown.Format.mkv" to "其他"
        )

        var correctDetections = 0
        val totalTests = testCases.size

        testCases.forEach { (fileName, expectedResolution) ->
            // 创建测试用的MediaItem，包含不同的文件大小来测试推断逻辑
            val fileSize = when (expectedResolution) {
                "4K" -> 8_000_000_000L // 8GB
                "2K" -> 4_000_000_000L // 4GB
                "1080P" -> 2_000_000_000L // 2GB
                "720P" -> 1_000_000_000L // 1GB
                else -> 500_000_000L // 500MB
            }

            val testMediaItem = mediaItem.copy(
                filePath = "/test/path/$fileName",
                fileSize = fileSize,
                duration = 7200L // 2小时
            )

            val detectedResolution = detectVideoResolution(testMediaItem)
            val isCorrect = detectedResolution == expectedResolution

            if (isCorrect) correctDetections++

            val status = if (isCorrect) "✓" else "✗"
            android.util.Log.d("VideoDetailsFragment", "$status File: $fileName")
            android.util.Log.d("VideoDetailsFragment", "  Expected: $expectedResolution, Detected: $detectedResolution")
        }

        val accuracy = (correctDetections.toDouble() / totalTests.toDouble()) * 100
        android.util.Log.d("VideoDetailsFragment", "=== Test Results ===")
        android.util.Log.d("VideoDetailsFragment", "Correct: $correctDetections/$totalTests")
        android.util.Log.d("VideoDetailsFragment", "Accuracy: ${String.format("%.1f", accuracy)}%")

        // 测试当前剧集
        val currentEpisode = getCurrentlyPlayingEpisodeForDisplay()
        if (currentEpisode != null) {
            android.util.Log.d("VideoDetailsFragment", "=== Current Episode Test ===")
            val currentResolution = detectVideoResolution(currentEpisode)
            android.util.Log.d("VideoDetailsFragment", "Current episode resolution: $currentResolution")
            android.util.Log.d("VideoDetailsFragment", "Current episode file: ${currentEpisode.filePath}")
            android.util.Log.d("VideoDetailsFragment", "File size: ${currentEpisode.fileSize} bytes")
            android.util.Log.d("VideoDetailsFragment", "Duration: ${currentEpisode.duration} seconds")
        }
    }

    private fun scrollToTop() {
        scrollView.smoothScrollTo(0, 0)
    }

    private fun setupKeyListener(view: View) {
        // 为整个根视图设置按键监听
        view.isFocusableInTouchMode = true

        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        // 检查当前焦点是否在播放按钮上
                        if (btnPlay.hasFocus()) {
                            handleDownKey()
                            true
                        } else {
                            false // 让其他控件处理
                        }
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // 任何时候按上键都直接回到顶部
                        val currentScrollY = scrollView.scrollY
                        if (currentScrollY > 50) { // 如果不在顶部
                            scrollToTop()
                            btnPlay.requestFocus()
                            true
                        } else {
                            false
                        }
                    }
                    KeyEvent.KEYCODE_MENU -> {
                        // 按菜单键测试播放状态功能（仅用于测试）
                        testPlaybackStateFeature()
                        true
                    }
                    KeyEvent.KEYCODE_GUIDE -> {
                        // 按指南键测试分辨率检测功能（仅用于测试）
                        testResolutionDetection()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        // 也为播放按钮单独设置按键监听
        btnPlay.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                        handleDownKey()
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // 播放按钮按上键时，如果不在顶部就滚动到顶部
                        val currentScrollY = scrollView.scrollY
                        if (currentScrollY > 50) {
                            scrollToTop()
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }

    private fun handleDownKey() {
        val currentScrollY = scrollView.scrollY

        // 如果当前在第一屏（滚动位置小于第一屏高度的一半）
        if (currentScrollY < firstScreenHeightPx / 2) {
            // 直接滚动到第二屏（演员表区域）
            scrollToSecondScreen()
        } else {
            // 如果已经在第二屏，继续正常滚动
            scrollView.smoothScrollBy(0, 200)
        }
    }

    private fun handleUpKey() {
        val currentScrollY = scrollView.scrollY

        // 如果当前滚动位置大于100px（说明不在顶部），直接滚动到顶部
        if (currentScrollY > 100) {
            // 直接滚动回第一屏顶部
            scrollToTop()
        } else {
            // 如果已经在第一屏顶部，不做任何操作或者可以退出页面
            // 这里可以添加退出逻辑，比如返回上一页
        }
    }

    private fun scrollToSecondScreen() {
        // 滚动到第二屏的开始位置（演员表区域）
        scrollView.smoothScrollTo(0, firstScreenHeightPx)
    }

    private fun setupActorsKeyListener() {
        rvActors?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        // 直接回到第一屏顶部，不管当前在演员表的哪个位置
                        scrollToTop()
                        // 将焦点设置回播放按钮
                        btnPlay.requestFocus()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }

        // 为返回顶部按钮设置按键监听
        btnBackToTop?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> {
                        scrollToTop()
                        // 将焦点设置回播放按钮
                        btnPlay.requestFocus()
                        true
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }

    /**
     * 解码文件路径，处理URL编码的中文字符
     */
    private fun decodeFilePath(filePath: String): String {
        return try {
            // 检查是否包含URL编码字符
            if (!filePath.contains("%")) {
                return filePath
            }

            // 首先尝试UTF-8解码
            var decoded = URLDecoder.decode(filePath, StandardCharsets.UTF_8.toString())

            // 如果还有编码字符，再次尝试解码（处理双重编码的情况）
            var previousDecoded = decoded
            while (decoded.contains("%")) {
                try {
                    decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8.toString())
                    // 如果解码后没有变化，说明无法进一步解码，跳出循环
                    if (decoded == previousDecoded) {
                        break
                    }
                    previousDecoded = decoded
                } catch (e: Exception) {
                    break
                }
            }

            decoded
        } catch (e: Exception) {
            // 如果UTF-8解码失败，尝试其他编码方式
            try {
                URLDecoder.decode(filePath, "GBK")
            } catch (e2: Exception) {
                try {
                    // 最后尝试ISO-8859-1
                    URLDecoder.decode(filePath, "ISO-8859-1")
                } catch (e3: Exception) {
                    // 如果都失败了，返回原始路径
                    filePath
                }
            }
        }
    }

    /**
     * 设置TV系列相关UI
     */
    private fun setupTVSeriesUI() {
        val isTVSeries = viewModel.isTVSeries()
        val directCheck = mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_EPISODE ||
                         mediaItem.mediaType == com.tvplayer.webdav.data.model.MediaType.TV_SERIES
        android.util.Log.d("VideoDetailsFragment", "setupTVSeriesUI: isTVSeries = $isTVSeries, directCheck = $directCheck, mediaType = ${mediaItem.mediaType}")

        if (isTVSeries || directCheck) {
            android.util.Log.d("VideoDetailsFragment", "Setting TV series section visible")
            tvSeriesSection?.visibility = View.VISIBLE
            setupEpisodeList()
        } else {
            android.util.Log.d("VideoDetailsFragment", "Setting TV series section gone")
            tvSeriesSection?.visibility = View.GONE
        }
    }

    /**
     * 设置剧集列表
     */
    private fun setupEpisodeList() {
        android.util.Log.d("VideoDetailsFragment", "setupEpisodeList called, rvEpisodes = ${rvEpisodes != null}")
        rvEpisodes?.let { recyclerView ->
            android.util.Log.d("VideoDetailsFragment", "Setting up episode adapter and layout manager")
            episodeAdapter = EpisodeAdapter(
                onEpisodeClick = { episode ->
                    // 直接播放选中的剧集（从0开始）
                    val targetMediaItem = episode.mediaItem
                    android.util.Log.d("VideoDetailsFragment", "Episode clicked: S${targetMediaItem.seasonNumber}E${targetMediaItem.episodeNumber}")
                    android.util.Log.d("VideoDetailsFragment", "Episode file: ${targetMediaItem.filePath}")
                    
                    // 直接播放该剧集，从0开始
                    startSpecificEpisodePlayback(targetMediaItem, 0L)
                },
                onItemFocused = { episode ->
                    // 可以在这里处理焦点变化，比如更新详情信息
                    android.util.Log.d("VideoDetailsFragment", "Focused on episode ${episode.episodeNumber}: ${episode.name}")
                }
            )

            recyclerView.adapter = episodeAdapter
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    /**
     * 设置季选择下拉框
     */
    private fun setupSeasonSpinner(seasons: List<com.tvplayer.webdav.data.tmdb.TmdbSeason>) {
        spinnerSeason?.let { spinner ->
            val seasonNames = seasons.map { "第${it.seasonNumber}季" }
            val adapter = android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                seasonNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position < seasons.size) {
                        val selectedSeason = seasons[position]
                        viewModel.selectSeason(selectedSeason.seasonNumber)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }

    /**
     * 更新选中的季
     */
    private fun updateSelectedSeason(seasonNumber: Int) {
        // 这里可以更新UI显示当前选中的季
        // 由于spinner的选择会触发viewModel.selectSeason，这里主要用于初始化时设置
    }

    /**
     * 设置剧集列表数据
     */
    private fun setupEpisodes(episodes: List<com.tvplayer.webdav.data.model.Episode>) {
        episodeAdapter?.submitList(episodes)
    }

}
