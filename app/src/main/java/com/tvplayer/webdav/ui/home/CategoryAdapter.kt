package com.tvplayer.webdav.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.MediaCategory

/**
 * 分类适配器
 * 显示主界面的分类导航
 */
class CategoryAdapter(
    private val onCategoryClick: (MediaCategory) -> Unit
) : ListAdapter<MediaCategory, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        itemView: View,
        private val onCategoryClick: (MediaCategory) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvCategoryCount: TextView = itemView.findViewById(R.id.tv_category_count)

        fun bind(category: MediaCategory) {
            tvCategoryName.text = category.name

            // 设置图标
            val iconRes = when (category.id) {
                "movies" -> R.drawable.ic_movie
                "tv_shows" -> R.drawable.ic_tv
                "recently_added" -> R.drawable.ic_recent
                "continue_watching" -> R.drawable.ic_recent
                "favorites" -> R.drawable.ic_favorite
                "settings" -> R.drawable.ic_settings
                else -> R.drawable.ic_video
            }
            ivCategoryIcon.setImageResource(iconRes)

            if (category.itemCount > 0) {
                tvCategoryCount.text = category.itemCount.toString()
                tvCategoryCount.visibility = View.VISIBLE
            } else {
                tvCategoryCount.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onCategoryClick(category)
            }

            // 设置焦点效果
            itemView.setOnFocusChangeListener { _, hasFocus ->
                // 使用更小的缩放比例，避免上下边框被裁剪
                val scale = if (hasFocus) 1.04f else 1.0f
                val elevation = if (hasFocus) 6f else 2f

                itemView.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationZ(elevation)
                    .setDuration(250L)
                    .start()
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<MediaCategory>() {
        override fun areItemsTheSame(oldItem: MediaCategory, newItem: MediaCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaCategory, newItem: MediaCategory): Boolean {
            return oldItem == newItem
        }
    }
}
