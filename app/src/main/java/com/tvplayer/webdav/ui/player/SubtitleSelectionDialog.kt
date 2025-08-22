package com.tvplayer.webdav.ui.player

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.Subtitle
import com.tvplayer.webdav.data.model.SubtitleConfig
import com.tvplayer.webdav.data.subtitle.SubtitleMatch
import kotlinx.coroutines.*

/**
 * 字幕选择对话框
 * 允许用户搜索、选择和管理字幕
 */
class SubtitleSelectionDialog(
    context: Context,
    private val onSubtitleSelected: (Subtitle) -> Unit,
    private val onSearchSubtitles: () -> Unit,
    private val onConfigSubtitles: () -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    
    companion object {
        private const val TAG = "SubtitleSelectionDialog"
    }
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var searchButton: Button
    private lateinit var configButton: Button
    private lateinit var closeButton: Button
    private lateinit var subtitleAdapter: SubtitleAdapter
    
    private var subtitles: List<Subtitle> = emptyList()
    private var subtitleMatches: List<SubtitleMatch> = emptyList()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDialog()
    }
    
    private fun setupDialog() {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_subtitle_selection, null)
        setContentView(contentView)
        
        initViews(contentView)
        setupRecyclerView()
        setupClickListeners()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_subtitles)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.text_empty)
        searchButton = view.findViewById(R.id.btn_search)
        configButton = view.findViewById(R.id.btn_config)
        closeButton = view.findViewById(R.id.btn_close)
    }
    
    private fun setupRecyclerView() {
        subtitleAdapter = SubtitleAdapter { subtitle ->
            onSubtitleSelected(subtitle)
            dismiss()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = subtitleAdapter
        }
    }
    
    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            onSearchSubtitles()
        }
        
        configButton.setOnClickListener {
            onConfigSubtitles()
        }
        
        closeButton.setOnClickListener {
            dismiss()
        }
    }
    
    /**
     * 更新字幕列表
     */
    fun updateSubtitles(subtitles: List<Subtitle>) {
        this.subtitles = subtitles

        // 按优先级排序字幕
        val sortedSubtitles = sortSubtitlesByPriority(subtitles)
        subtitleAdapter.updateSubtitles(sortedSubtitles)
        updateEmptyState()
    }

    /**
     * 按优先级排序字幕（中文优先）
     */
    private fun sortSubtitlesByPriority(subtitles: List<Subtitle>): List<Subtitle> {
        return subtitles.sortedWith { a, b ->
            // 优先级：简体中文 > 繁体中文 > 其他中文 > 英文 > 其他语言
            val aPriority = getLanguagePriority(a.language)
            val bPriority = getLanguagePriority(b.language)

            when {
                aPriority != bPriority -> aPriority.compareTo(bPriority)
                else -> {
                    // 相同语言按评分和下载量排序
                    val aScore = (a.metadata["rating"]?.toFloatOrNull() ?: 0f) +
                                (a.metadata["downloads"]?.toIntOrNull() ?: 0) * 0.001f
                    val bScore = (b.metadata["rating"]?.toFloatOrNull() ?: 0f) +
                                (b.metadata["downloads"]?.toIntOrNull() ?: 0) * 0.001f
                    bScore.compareTo(aScore) // 降序排列
                }
            }
        }
    }

    /**
     * 获取语言优先级（数字越小优先级越高）
     */
    private fun getLanguagePriority(language: String?): Int {
        return when (language?.lowercase()) {
            "zh-cn", "zh_cn", "chinese (simplified)", "简体中文" -> 1
            "zh-tw", "zh_tw", "chinese (traditional)", "繁体中文" -> 2
            "zh", "chinese", "中文" -> 3
            "en", "english", "英文" -> 4
            else -> 5
        }
    }
    
    /**
     * 更新字幕匹配结果
     */
    fun updateSubtitleMatches(matches: List<SubtitleMatch>) {
        this.subtitleMatches = matches
        subtitleAdapter.updateSubtitleMatches(matches)
        updateEmptyState()
    }
    
    /**
     * 显示加载状态
     */
    fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }
    
    /**
     * 隐藏加载状态
     */
    fun hideLoading() {
        progressBar.visibility = View.GONE
        updateEmptyState()
    }
    
    private fun updateEmptyState() {
        val hasData = subtitles.isNotEmpty() || subtitleMatches.isNotEmpty()
        
        if (hasData) {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyView.text = "暂无字幕\n点击搜索按钮查找字幕"
        }
    }
}

/**
 * 字幕列表适配器
 */
class SubtitleAdapter(
    private val onSubtitleClick: (Subtitle) -> Unit
) : RecyclerView.Adapter<SubtitleAdapter.SubtitleViewHolder>() {
    
    private var subtitles: List<Subtitle> = emptyList()
    private var subtitleMatches: List<SubtitleMatch> = emptyList()
    private var displayItems: List<DisplayItem> = emptyList()
    
    fun updateSubtitles(subtitles: List<Subtitle>) {
        this.subtitles = subtitles
        this.displayItems = subtitles.map { DisplayItem.SubtitleItem(it) }
        notifyDataSetChanged()
    }
    
    fun updateSubtitleMatches(matches: List<SubtitleMatch>) {
        this.subtitleMatches = matches
        this.displayItems = matches.map { DisplayItem.MatchItem(it) }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subtitle, parent, false)
        return SubtitleViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is DisplayItem.SubtitleItem -> holder.bind(item.subtitle)
            is DisplayItem.MatchItem -> holder.bind(item.match.subtitle, item.match)
        }
    }
    
    override fun getItemCount(): Int = displayItems.size
    
    inner class SubtitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.text_title)
        private val languageText: TextView = itemView.findViewById(R.id.text_language)
        private val infoText: TextView = itemView.findViewById(R.id.text_info)
        private val ratingText: TextView = itemView.findViewById(R.id.text_rating)
        private val statusText: TextView = itemView.findViewById(R.id.text_status)
        private val matchIndicator: View = itemView.findViewById(R.id.indicator_match)
        
        fun bind(subtitle: Subtitle, match: SubtitleMatch? = null) {
            titleText.text = subtitle.getDisplayTitle()
            languageText.text = subtitle.languageName
            
            // 信息文本
            val infoList = mutableListOf<String>()
            if (subtitle.source.displayName.isNotEmpty()) {
                infoList.add(subtitle.source.displayName)
            }
            if (subtitle.downloadCount > 0) {
                infoList.add("下载: ${subtitle.downloadCount}")
            }
            if (subtitle.fileSize > 0) {
                val sizeMB = subtitle.fileSize / (1024f * 1024f)
                infoList.add("大小: %.1fMB".format(sizeMB))
            }
            infoText.text = infoList.joinToString(" | ")
            
            // 评分
            if (subtitle.rating > 0) {
                ratingText.text = "%.1f".format(subtitle.rating)
                ratingText.visibility = View.VISIBLE
            } else {
                ratingText.visibility = View.GONE
            }
            
            // 状态
            if (subtitle.isDownloaded) {
                statusText.text = "已下载"
                statusText.setBackgroundResource(R.drawable.status_background)
                statusText.visibility = View.VISIBLE
            } else {
                statusText.visibility = View.GONE
            }
            
            // 匹配度指示器
            if (match != null) {
                matchIndicator.visibility = View.VISIBLE
                val color = when (match.qualityLevel) {
                    com.tvplayer.webdav.data.subtitle.MatchQuality.EXCELLENT -> 
                        android.graphics.Color.parseColor("#4CAF50")
                    com.tvplayer.webdav.data.subtitle.MatchQuality.GOOD -> 
                        android.graphics.Color.parseColor("#FF9800")
                    com.tvplayer.webdav.data.subtitle.MatchQuality.FAIR -> 
                        android.graphics.Color.parseColor("#FFC107")
                    else -> android.graphics.Color.parseColor("#9E9E9E")
                }
                matchIndicator.setBackgroundColor(color)
            } else {
                matchIndicator.visibility = View.GONE
            }
            
            // 点击事件
            itemView.setOnClickListener {
                onSubtitleClick(subtitle)
            }
            
            // 焦点效果（Android TV）
            itemView.isFocusable = true
            itemView.setOnFocusChangeListener { view, hasFocus ->
                view.isSelected = hasFocus
            }
        }
    }
    
    sealed class DisplayItem {
        data class SubtitleItem(val subtitle: Subtitle) : DisplayItem()
        data class MatchItem(val match: SubtitleMatch) : DisplayItem()
    }
}