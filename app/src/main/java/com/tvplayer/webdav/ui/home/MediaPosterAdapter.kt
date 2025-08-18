package com.tvplayer.webdav.ui.home

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
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaItem

/**
 * 媒体海报适配器
 * 显示电影和电视剧的海报墙
 */
class MediaPosterAdapter(
    private val onMediaClick: (MediaItem) -> Unit,
    private val onItemFocused: ((MediaItem) -> Unit)? = null
) : ListAdapter<MediaItem, MediaPosterAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_poster, parent, false)
        return MediaViewHolder(view, onMediaClick, onItemFocused)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaViewHolder(
        itemView: View,
        private val onMediaClick: (MediaItem) -> Unit,
        private val onItemFocused: ((MediaItem) -> Unit)? = null
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val ivPlayButton: ImageView = itemView.findViewById(R.id.iv_play_button)
        private val layoutProgress: View = itemView.findViewById(R.id.layout_progress)
        private val bottomInfo: View = itemView.findViewById(R.id.layout_bottom_info)

        init {
            // 焦点动画在 bind() 中配置，以便拿到当前绑定的 mediaItem
        }

        fun bind(mediaItem: MediaItem) {
            tvTitle.text = mediaItem.getDisplayTitle()

            val subtitle = mediaItem.getSubtitle()
            if (subtitle != null) {
                tvSubtitle.text = subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            // 显示观看进度
            if (mediaItem.watchedProgress > 0) {
                progressBar.progress = mediaItem.getWatchedPercentage()
                tvProgress.text = "${mediaItem.getWatchedPercentage()}%"
                layoutProgress.visibility = View.VISIBLE
                ivPlayButton.visibility = View.GONE
            } else {
                layoutProgress.visibility = View.GONE
                ivPlayButton.visibility = View.VISIBLE
                // 为播放按钮添加悬浮动画
                PosterFocusAnimator.startPlayButtonFloatingAnimation(ivPlayButton)
            }

            // 显示评分（如果有的话）
            // 这里可以根据实际需求显示评分，暂时隐藏
            tvRating.visibility = View.GONE

            // 加载海报图片（优先使用TMDB图片）
            val posterUrl = mediaItem.posterPath
            if (!posterUrl.isNullOrEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(posterUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_video)
                        .into(ivPoster)
                } catch (_: Exception) {
                    ivPoster.setImageResource(R.drawable.ic_video)
                }
            } else {
                // 占位符
                ivPoster.setImageResource(when (mediaItem.mediaType) {
                    com.tvplayer.webdav.data.model.MediaType.MOVIE -> R.drawable.ic_movie
                    com.tvplayer.webdav.data.model.MediaType.TV_EPISODE,
                    com.tvplayer.webdav.data.model.MediaType.TV_SERIES -> R.drawable.ic_tv
                    else -> R.drawable.ic_video
                })
            }

            itemView.setOnClickListener {
                onMediaClick(mediaItem)
            }

            // 设置焦点效果（使用卡片焦点动画）
            val cardView = itemView.findViewById<CardView>(R.id.card_view)
            if (cardView != null) {
                PosterFocusAnimator.setupPosterFocusAnimation(
                    itemView, cardView, ivPlayButton, tvRating
                ) { hasFocus ->
                    bottomInfo.animate().alpha(if (hasFocus) 1f else 0f).setDuration(200).start()
                    if (hasFocus) {
                        onItemFocused?.invoke(mediaItem)
                    }
                }
            } else {
                FocusHighlightHelper.setupFocusHighlight(itemView)
            }
        }
    }

    private class MediaDiffCallback : DiffUtil.ItemCallback<MediaItem>() {

        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}
