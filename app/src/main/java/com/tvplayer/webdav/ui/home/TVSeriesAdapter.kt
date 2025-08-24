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
import com.tvplayer.webdav.data.model.TVSeriesSummary

/**
 * 电视剧系列适配器
 * 显示电视剧系列的海报墙，而不是单个剧集
 */
class TVSeriesAdapter(
    private val onSeriesClick: (TVSeriesSummary) -> Unit,
    private val onItemFocused: ((TVSeriesSummary) -> Unit)? = null
) : ListAdapter<TVSeriesSummary, TVSeriesAdapter.TVSeriesViewHolder>(TVSeriesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TVSeriesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_poster, parent, false)
        return TVSeriesViewHolder(view, onSeriesClick, onItemFocused)
    }

    override fun onBindViewHolder(holder: TVSeriesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TVSeriesViewHolder(
        itemView: View,
        private val onSeriesClick: (TVSeriesSummary) -> Unit,
        private val onItemFocused: ((TVSeriesSummary) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_poster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val layoutProgress: View = itemView.findViewById(R.id.layout_progress)


        fun bind(series: TVSeriesSummary) {
            tvTitle.text = series.seriesTitle
            
            // 显示季数和集数信息
            val subtitle = series.getSubtitle()
            if (subtitle != null) {
                tvSubtitle.text = subtitle
                tvSubtitle.visibility = View.VISIBLE
            } else {
                tvSubtitle.visibility = View.GONE
            }

            // 显示观看进度
            if (series.hasWatchedProgress()) {
                val progressPercentage = (series.getWatchedProgress() * 100).toInt()
                progressBar.progress = progressPercentage
                tvProgress.text = "${series.watchedEpisodes}/${series.totalEpisodes}集"
                layoutProgress.visibility = View.VISIBLE
            } else {
                layoutProgress.visibility = View.GONE
            }

            // 显示评分（如果有的话）
            // 这里可以根据实际需求显示评分，暂时隐藏
            tvRating.visibility = View.GONE

            // 加载海报图片（与MediaPosterAdapter保持完全一致的处理方式）
            val cornerRadius = (4f * itemView.resources.displayMetrics.density).toInt() // 4dp转换为像素
            
            val requestOptions = com.bumptech.glide.request.RequestOptions()
                .centerCrop() // 保持centerCrop但优化参数
                .dontAnimate() // 禁用动画提高性能
                .transform(com.bumptech.glide.load.resource.bitmap.RoundedCorners(cornerRadius))
                .placeholder(R.drawable.ic_tv)
                .error(R.drawable.ic_tv) // 添加错误占位符
            
            val posterUrl = series.posterPath
            if (!posterUrl.isNullOrEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(posterUrl)
                        .apply(requestOptions)
                        .into(ivPoster)
                } catch (_: Exception) {
                    // 加载失败时也要应用圆角效果
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(R.drawable.ic_tv)
                        .apply(requestOptions)
                        .into(ivPoster)
                }
            } else {
                // 占位符也要应用圆角效果
                com.bumptech.glide.Glide.with(itemView.context)
                    .load(R.drawable.ic_tv)
                    .apply(requestOptions)
                    .into(ivPoster)
            }

            // 设置焦点效果（与MediaPosterAdapter完全一致）
            val cardView = itemView.findViewById<CardView>(R.id.card_view)
            
            // 设置点击事件在整个itemView上
            itemView.setOnClickListener {
                onSeriesClick(series)
            }
            
            // 注释图片点击事件，统一使用itemView的点击事件
            // ivPoster.setOnClickListener {
            //     onSeriesClick(series)
            // }

            // 使用整个itemView作为焦点容器，这样放大动画能正确应用到整个item
            PosterFocusAnimator.setupPosterFocusAnimation(
                itemView, cardView, null, tvRating
            ) { hasFocus ->
                if (hasFocus) {
                    onItemFocused?.invoke(series)
                }
            }
        }
    }

    private class TVSeriesDiffCallback : DiffUtil.ItemCallback<TVSeriesSummary>() {
        override fun areItemsTheSame(oldItem: TVSeriesSummary, newItem: TVSeriesSummary): Boolean {
            return oldItem.seriesId == newItem.seriesId
        }

        override fun areContentsTheSame(oldItem: TVSeriesSummary, newItem: TVSeriesSummary): Boolean {
            return oldItem == newItem
        }
    }
}
