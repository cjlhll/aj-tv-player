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
import com.tvplayer.webdav.data.model.TVSeriesSummary
import com.tvplayer.webdav.ui.home.PosterFocusAnimator
import com.tvplayer.webdav.ui.home.FocusHighlightHelper

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
            tvSubtitle.text = series.getSubtitle()
            tvSubtitle.visibility = View.VISIBLE

            // 显示观看进度
            if (series.hasWatchedProgress()) {
                val progressPercentage = (series.getWatchedProgress() * 100).toInt()
                progressBar.progress = progressPercentage
                tvProgress.text = "${series.watchedEpisodes}/${series.totalEpisodes}集"
                layoutProgress.visibility = View.VISIBLE
            } else {
                layoutProgress.visibility = View.GONE
            }

            // 隐藏评分标签
            tvRating.visibility = View.GONE

            // 加载海报图片
            val posterUrl = series.posterPath
            if (!posterUrl.isNullOrEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(posterUrl)
                        .centerCrop()
                        .placeholder(R.drawable.ic_tv)
                        .into(ivPoster)
                } catch (_: Exception) {
                    ivPoster.setImageResource(R.drawable.ic_tv)
                }
            } else {
                ivPoster.setImageResource(R.drawable.ic_tv)
            }

            // 设置焦点效果（使用与电影海报相同的焦点动画）
            val cardView = itemView.findViewById<CardView>(R.id.card_view)
            if (cardView != null) {
                // 设置点击事件在CardView上
                cardView.setOnClickListener {
                    onSeriesClick(series)
                }

                // 使用PosterFocusAnimator设置焦点动画，与MediaPosterAdapter保持一致
                PosterFocusAnimator.setupPosterFocusAnimation(
                    cardView, cardView, null, tvRating
                ) { hasFocus ->
                    if (hasFocus) {
                        onItemFocused?.invoke(series)
                    }
                }
            } else {
                // 备用方案
                itemView.setOnClickListener {
                    onSeriesClick(series)
                }
                
                // 使用FocusHighlightHelper作为备用焦点效果
                FocusHighlightHelper.setupFocusHighlight(itemView)
                
                itemView.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        onItemFocused?.invoke(series)
                    }
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
