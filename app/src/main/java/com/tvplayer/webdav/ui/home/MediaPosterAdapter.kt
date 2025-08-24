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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
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
            } else {
                layoutProgress.visibility = View.GONE
            }

            // 显示评分（如果有的话）
            // 这里可以根据实际需求显示评分，暂时隐藏
            tvRating.visibility = View.GONE

            // 加载海报图片（优先使用TMDB图片）并添加圆角效果
            val cornerRadius = (4f * itemView.resources.displayMetrics.density).toInt() // 4dp转换为像素
            
            val requestOptions = RequestOptions()
                .centerCrop() // 保持centerCrop但优化参数
                .dontAnimate() // 禁用动画提高性能
                .transform(RoundedCorners(cornerRadius))
                .placeholder(R.drawable.ic_video)
                .error(R.drawable.ic_video) // 添加错误占位符
            
            val posterUrl = mediaItem.posterPath
            if (!posterUrl.isNullOrEmpty()) {
                try {
                    Glide.with(itemView.context)
                        .load(posterUrl)
                        .apply(requestOptions)
                        .into(ivPoster)
                } catch (_: Exception) {
                    // 加载失败时也要应用圆角效果
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_video)
                        .apply(requestOptions)
                        .into(ivPoster)
                }
            } else {
                // 占位符也要应用圆角效果
                val placeholderDrawable = when (mediaItem.mediaType) {
                    com.tvplayer.webdav.data.model.MediaType.MOVIE -> R.drawable.ic_movie
                    com.tvplayer.webdav.data.model.MediaType.TV_EPISODE,
                    com.tvplayer.webdav.data.model.MediaType.TV_SERIES -> R.drawable.ic_tv
                    else -> R.drawable.ic_video
                }
                
                Glide.with(itemView.context)
                    .load(placeholderDrawable)
                    .apply(requestOptions)
                    .into(ivPoster)
            }

            // 设置焦点效果（将焦点设置在海报图片上）
            val cardView = itemView.findViewById<CardView>(R.id.card_view)

            // 将点击回调存储在tag中，供PosterFocusAnimator使用
            itemView.tag = { onMediaClick(mediaItem) }

            // 使用整个itemView作为焦点容器，这样放大动画能正确应用到整个item
            PosterFocusAnimator.setupPosterFocusAnimation(
                itemView, cardView, null, tvRating
            ) { hasFocus ->
                if (hasFocus) {
                    onItemFocused?.invoke(mediaItem)
                }
            }

            // 设置点击监听器，确保边框不丢失
            itemView.setOnClickListener { view ->
                // 立即确保焦点边框保持
                PosterFocusAnimator.ensureFocusStateAfterClick(view)

                // 确保有焦点
                if (!view.hasFocus()) {
                    view.requestFocus()
                }

                // 执行点击事件
                onMediaClick(mediaItem)

                // 再次确保边框保持（防止点击事件影响）
                view.post {
                    PosterFocusAnimator.ensureFocusStateAfterClick(view)
                }
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
