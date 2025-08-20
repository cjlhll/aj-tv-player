package com.tvplayer.webdav.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.Episode

/**
 * 剧集列表适配器
 * 使用与主页海报相同的设计风格
 */
class EpisodeAdapter(
    private val onEpisodeClick: (Episode) -> Unit,
    private val onItemFocused: ((Episode) -> Unit)? = null
) : ListAdapter<Episode, EpisodeAdapter.EpisodeViewHolder>(EpisodeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_poster, parent, false)

        // Modify the CardView to use 16:9 aspect ratio for episodes
        val cardView = view.findViewById<androidx.cardview.widget.CardView>(R.id.card_view)
        val cardParams = cardView.layoutParams
        val episodeWidth = 280 // Wider for 16:9 aspect ratio
        val episodeHeight = (episodeWidth * 9 / 16) // Calculate 16:9 height = 157.5 ≈ 158
        cardParams.width = episodeWidth
        cardParams.height = episodeHeight
        cardView.layoutParams = cardParams

        return EpisodeViewHolder(view, onEpisodeClick, onItemFocused)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EpisodeViewHolder(
        itemView: View,
        private val onEpisodeClick: (Episode) -> Unit,
        private val onItemFocused: ((Episode) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.card_view)
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val layoutProgress: View = itemView.findViewById(R.id.layout_progress)

        fun bind(episode: Episode) {
            // 设置标题（集数）
            tvTitle.text = episode.getDisplayTitle()
            
            // 设置副标题（剧集名称）
            val subtitle = episode.getSubtitle()
            if (!subtitle.isNullOrBlank()) {
                tvSubtitle.text = subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            // 显示观看进度（使用关联的MediaItem数据）
            val mediaItem = episode.mediaItem
            if (mediaItem.watchedProgress > 0f) {
                val progressPercentage = (mediaItem.watchedProgress * 100).toInt()
                progressBar.progress = progressPercentage
                tvProgress.text = "${progressPercentage}%"
                layoutProgress.visibility = View.VISIBLE
            } else {
                layoutProgress.visibility = View.GONE
            }

            // 显示评分（如果有）
            if (episode.rating > 0) {
                tvRating.text = String.format("%.1f", episode.rating)
                tvRating.visibility = View.VISIBLE
            } else {
                tvRating.visibility = View.GONE
            }

            // 加载剧集静态图片
            if (!episode.stillPath.isNullOrEmpty()) {
                try {
                    Glide.with(itemView.context)
                        .load(episode.stillPath)
                        .centerCrop()
                        .placeholder(R.drawable.ic_video)
                        .error(R.drawable.ic_video)
                        .into(ivPoster)
                } catch (e: Exception) {
                    ivPoster.setImageResource(R.drawable.ic_video)
                }
            } else {
                ivPoster.setImageResource(R.drawable.ic_video)
            }

            // 设置点击事件
            cardView.setOnClickListener {
                onEpisodeClick(episode)
            }

            // 设置焦点事件
            cardView.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    onItemFocused?.invoke(episode)
                }
            }
        }
    }

    class EpisodeDiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem == newItem
        }
    }
}
