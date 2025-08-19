package com.tvplayer.webdav.ui.details

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.Actor
import com.tvplayer.webdav.data.model.MediaItem
import dagger.hilt.android.AndroidEntryPoint

/**
 * 视频详情页面Fragment
 * 根据设计图实现视频详情页面布局
 */
@AndroidEntryPoint
class VideoDetailsFragment : Fragment() {

    private val viewModel: VideoDetailsViewModel by viewModels()
    
    private lateinit var mediaItem: MediaItem
    
    // UI组件
    private lateinit var ivBackdrop: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var btnPlay: Button
    private lateinit var tvRating: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvFileSize: TextView
    private lateinit var tvOverview: TextView
    private lateinit var movieInfoContainer: LinearLayout

    // 演员表组件
    private var rvActors: RecyclerView? = null
    private var tvFilename: TextView? = null
    private var tvSourcePath: TextView? = null
    private var tvDurationSize: TextView? = null
    private var btnBackToTop: View? = null

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
            requireActivity().finish()
            return
        }
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
    }

    private fun initViews(view: View) {
        ivBackdrop = view.findViewById(R.id.iv_backdrop)
        tvTitle = view.findViewById(R.id.tv_title)
        btnPlay = view.findViewById(R.id.btn_play)
        tvRating = view.findViewById(R.id.tv_rating)
        tvYear = view.findViewById(R.id.tv_year)
        tvDuration = view.findViewById(R.id.tv_duration)
        tvGenre = view.findViewById(R.id.tv_genre)
        tvFileSize = view.findViewById(R.id.tv_file_size)
        tvOverview = view.findViewById(R.id.tv_overview)
        movieInfoContainer = view.findViewById(R.id.movie_info_container)

        // 初始化演员表相关组件（可选的，因为可能不存在）
        try {
            rvActors = view.findViewById(R.id.rv_actors)
            tvFilename = view.findViewById(R.id.tv_filename)
            tvSourcePath = view.findViewById(R.id.tv_source_path)
            tvDurationSize = view.findViewById(R.id.tv_duration_size)
            btnBackToTop = view.findViewById(R.id.btn_back_to_top)
        } catch (e: Exception) {
            // 如果找不到这些组件，说明布局没有包含演员表部分
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
        
        // 设置时长
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
                view.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
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



        // 默认焦点设置到播放按钮
        btnPlay.requestFocus()
    }

    private fun observeViewModel() {
        // TODO: 观察ViewModel的数据变化
    }

    private fun startPlayback() {
        // TODO: 启动视频播放器
        // 这里将来会启动PlayerActivity
        android.widget.Toast.makeText(context, "开始播放: ${mediaItem.getDisplayTitle()}", android.widget.Toast.LENGTH_SHORT).show()

        // 模拟播放器启动
        viewModel.startPlayback()
    }

    private fun setupActorsIfAvailable() {
        rvActors?.let { recyclerView ->
            // 创建示例演员数据（实际应用中应该从API或数据库获取）
            val actors = listOf(
                Actor("1", "易小星", "导演", null, true),
                Actor("2", "常远", "饰", null, false),
                Actor("3", "邓家佳", "饰", null, false),
                Actor("4", "王耀庆", "饰", null, false),
                Actor("5", "田雨", "饰", null, false),
                Actor("6", "于洋", "饰", null, false),
                Actor("7", "李宗恒", "饰", null, false)
            )
            
            // 设置RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val actorAdapter = ActorAdapter(actors) { actor ->
                // 处理演员点击事件
                android.widget.Toast.makeText(context, "点击了: ${actor.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = actorAdapter
        }
    }

    private fun setupVideoDetailsIfAvailable() {
        // 设置文件名和技术信息（从filePath中提取文件名）
        tvFilename?.let { textView ->
            val fileName = try {
                val path = mediaItem.filePath
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
        }
        
        // 设置来源路径
        tvSourcePath?.let { textView ->
            val sourcePath = try {
                val path = mediaItem.filePath
                if (path.isNotBlank()) {
                    val lastSlashIndex = path.lastIndexOf('/')
                    if (lastSlashIndex > 0) {
                        path.substring(0, lastSlashIndex + 1)
                    } else {
                        path
                    }
                } else {
                    "未知路径"
                }
            } catch (e: Exception) {
                "未知路径"
            }
            textView.text = sourcePath
        }
        
        // 设置时长和大小
        tvDurationSize?.let { textView ->
            val durationText = if (mediaItem.duration > 0) {
                val hours = mediaItem.duration / 3600
                val minutes = (mediaItem.duration % 3600) / 60
                if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
            } else {
                "未知时长"
            }
            
            val sizeText = if (mediaItem.fileSize > 0) {
                val sizeInGB = mediaItem.fileSize / (1024.0 * 1024.0 * 1024.0)
                String.format("%.2f GB", sizeInGB)
            } else {
                "未知大小"
            }
            
            textView.text = "$durationText 其他 $sizeText"
        }
    }

    private fun scrollToTop() {
        // 找到ScrollView并滚动到顶部
        val scrollView = view?.findViewById<ScrollView>(R.id.scroll_view)
        scrollView?.smoothScrollTo(0, 0)
    }

}
