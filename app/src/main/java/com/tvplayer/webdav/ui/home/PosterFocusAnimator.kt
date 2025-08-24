package com.tvplayer.webdav.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

/**
 * 海报焦点动画器
 * 为海报卡片提供丰富的焦点切换动画效果
 */
object PosterFocusAnimator {
    
    private const val SCALE_FOCUSED = 1.04f  // 获得焦点时放大8%
    private const val SCALE_NORMAL = 1.0f   // 正常大小
    private const val ANIMATION_DURATION = 30L  // 放大动画稍微慢一点，更平滑
    private const val ANIMATION_DURATION_OUT = 30L  // 缩小动画快一点
    private const val STAGGER_DELAY = 0L  // 减少延迟
    
    /**
     * 设置海报焦点动画
     */
    fun setupPosterFocusAnimation(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?,
        onFocusChange: ((Boolean) -> Unit)? = null
    ) {
        val imageView = itemView.findViewById<ImageView>(com.tvplayer.webdav.R.id.iv_poster)

        itemView.setOnFocusChangeListener { _, hasFocus ->
            // 使用程序化边框控制，不依赖drawable状态
            setFocusBorder(imageView, hasFocus)
            animatePosterFocus(itemView, cardView, playButton, ratingBadge, hasFocus)
            onFocusChange?.invoke(hasFocus)
        }
    }

    /**
     * 程序化设置焦点边框，完全控制显示状态
     */
    private fun setFocusBorder(imageView: ImageView?, hasFocus: Boolean) {
        imageView?.let { iv ->
            if (hasFocus) {
                // 直接设置焦点边框drawable
                iv.foreground = iv.context.getDrawable(com.tvplayer.webdav.R.drawable.poster_focus_border)
            } else {
                // 移除边框
                iv.foreground = null
            }

            // 强制刷新
            iv.invalidate()
        }
    }

    /**
     * 更新ImageView的焦点状态 - 使用程序化边框控制
     */
    private fun updateImageViewFocusState(imageView: ImageView?, hasFocus: Boolean) {
        setFocusBorder(imageView, hasFocus)
    }
    
    /**
     * 海报焦点动画 - 多层次动画效果
     */
    private fun animatePosterFocus(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?,
        hasFocus: Boolean
    ) {
        // 取消之前的动画
        itemView.clearAnimation()
        playButton?.clearAnimation()
        ratingBadge?.clearAnimation()
        
        if (hasFocus) {
            animateFocusIn(itemView, cardView, playButton, ratingBadge)
        } else {
            animateFocusOut(itemView, cardView, playButton, ratingBadge)
        }
    }
    
    /**
     * 获得焦点动画 - 添加放大效果
     */
    private fun animateFocusIn(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?
    ) {
        // 放大动画 - 从当前大小到目标大小
        val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", itemView.scaleX, SCALE_FOCUSED)
        val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", itemView.scaleY, SCALE_FOCUSED)

        // 边框透明度动画（增强焦点效果）
        val borderAlpha = ObjectAnimator.ofFloat(itemView, "alpha", itemView.alpha, 1.0f)
        
        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playTogether(scaleX, scaleY, borderAlpha)
        mainAnimatorSet.duration = ANIMATION_DURATION
        mainAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        // 播放按钮动画 - 淡入 + 缩放
        playButton?.let { button ->
            if (button.visibility == View.VISIBLE) {
                val fadeIn = ObjectAnimator.ofFloat(button, "alpha", button.alpha, 1.0f)
                val buttonScale = ObjectAnimator.ofFloat(button, "scaleX", 0.8f, 1.0f)
                val buttonScaleY = ObjectAnimator.ofFloat(button, "scaleY", 0.8f, 1.0f)
                
                val buttonAnimatorSet = AnimatorSet()
                buttonAnimatorSet.playTogether(fadeIn, buttonScale, buttonScaleY)
                buttonAnimatorSet.duration = ANIMATION_DURATION - STAGGER_DELAY
                buttonAnimatorSet.startDelay = STAGGER_DELAY
                buttonAnimatorSet.interpolator = OvershootInterpolator(0.2f)
                buttonAnimatorSet.start()
            }
        }
        
        // 评分标签动画 - 滑入效果
        ratingBadge?.let { badge ->
            if (badge.visibility == View.VISIBLE) {
                val slideIn = ObjectAnimator.ofFloat(badge, "translationX", 20f, 0f)
                val fadeIn = ObjectAnimator.ofFloat(badge, "alpha", 0.7f, 1.0f)
                val scaleIn = ObjectAnimator.ofFloat(badge, "scaleX", 0.9f, 1.0f)
                
                val badgeAnimatorSet = AnimatorSet()
                badgeAnimatorSet.playTogether(slideIn, fadeIn, scaleIn)
                badgeAnimatorSet.duration = ANIMATION_DURATION - STAGGER_DELAY * 2
                badgeAnimatorSet.startDelay = STAGGER_DELAY * 2
                badgeAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
                badgeAnimatorSet.start()
            }
        }
        
        mainAnimatorSet.start()
    }
    
    /**
     * 失去焦点动画 - 添加缩小效果
     */
    private fun animateFocusOut(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?
    ) {
        // 缩小动画 - 从当前大小到正常大小
        val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", itemView.scaleX, SCALE_NORMAL)
        val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", itemView.scaleY, SCALE_NORMAL)

        // 边框透明度恢复
        val borderAlpha = ObjectAnimator.ofFloat(itemView, "alpha", itemView.alpha, 1.0f)

        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playTogether(scaleX, scaleY, borderAlpha)
        mainAnimatorSet.duration = ANIMATION_DURATION_OUT  // 失去焦点动画更快
        mainAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        // 播放按钮动画 - 淡出
        playButton?.let { button ->
            if (button.visibility == View.VISIBLE) {
                val fadeOut = ObjectAnimator.ofFloat(button, "alpha", button.alpha, 0.9f)
                val buttonScale = ObjectAnimator.ofFloat(button, "scaleX", button.scaleX, 0.95f)
                val buttonScaleY = ObjectAnimator.ofFloat(button, "scaleY", button.scaleY, 0.95f)
                
                val buttonAnimatorSet = AnimatorSet()
                buttonAnimatorSet.playTogether(fadeOut, buttonScale, buttonScaleY)
                buttonAnimatorSet.duration = ANIMATION_DURATION_OUT
                buttonAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
                buttonAnimatorSet.start()
            }
        }
        
        // 评分标签动画 - 轻微淡出
        ratingBadge?.let { badge ->
            if (badge.visibility == View.VISIBLE) {
                val fadeOut = ObjectAnimator.ofFloat(badge, "alpha", badge.alpha, 0.8f)
                val scaleOut = ObjectAnimator.ofFloat(badge, "scaleX", badge.scaleX, 0.95f)
                
                val badgeAnimatorSet = AnimatorSet()
                badgeAnimatorSet.playTogether(fadeOut, scaleOut)
                badgeAnimatorSet.duration = ANIMATION_DURATION_OUT
                badgeAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
                badgeAnimatorSet.start()
            }
        }
        
        mainAnimatorSet.start()
    }
    
    /**
     * 为播放按钮添加悬浮动画
     */
    fun startPlayButtonFloatingAnimation(playButton: ImageView) {
        val scaleUp = ObjectAnimator.ofFloat(playButton, "scaleX", 1.0f, 1.1f)
        val scaleDown = ObjectAnimator.ofFloat(playButton, "scaleX", 1.1f, 1.0f)
        val scaleUpY = ObjectAnimator.ofFloat(playButton, "scaleY", 1.0f, 1.1f)
        val scaleDownY = ObjectAnimator.ofFloat(playButton, "scaleY", 1.1f, 1.0f)
        
        val animatorSet = AnimatorSet()
        animatorSet.play(scaleUp).with(scaleUpY)
        animatorSet.play(scaleDown).with(scaleDownY).after(scaleUp)
        animatorSet.duration = 1000L
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        // 设置循环
        animatorSet.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (playButton.visibility == View.VISIBLE) {
                    animation.start() // 重新开始动画
                }
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
        
        animatorSet.start()
    }
    
    /**
     * 手动触发焦点状态同步（用于焦点恢复时）
     */
    fun triggerFocusStateSync(itemView: View, hasFocus: Boolean) {
        val imageView = itemView.findViewById<ImageView>(com.tvplayer.webdav.R.id.iv_poster)

        // 直接设置边框，不依赖系统状态
        setFocusBorder(imageView, hasFocus)

        // 同时设置View自身的焦点状态
        if (hasFocus) {
            itemView.isSelected = true
            imageView?.isSelected = true
        } else {
            itemView.isSelected = false
            imageView?.isSelected = false
        }

        // 触发父容器刷新
        itemView.invalidate()
        itemView.requestLayout()
    }

    /**
     * 程序化触发完整的焦点恢复（包含动画效果）
     * 用于从其他页面返回时恢复焦点状态
     */
    fun restoreFocusWithAnimation(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?,
        hasFocus: Boolean,
        onFocusChange: ((Boolean) -> Unit)? = null
    ) {
        try {
            // 首先同步焦点状态
            triggerFocusStateSync(itemView, hasFocus)

            // 如果需要显示焦点，执行动画
            if (hasFocus) {
                // 执行焦点动画
                animatePosterFocus(itemView, cardView, playButton, ratingBadge, true)

                // 触发焦点回调
                onFocusChange?.invoke(true)

                android.util.Log.d("PosterFocusAnimator", "Restored focus with animation for item")
            }

        } catch (e: Exception) {
            android.util.Log.e("PosterFocusAnimator", "Error restoring focus with animation", e)
        }
    }

    /**
     * 为RecyclerView的特定位置恢复焦点
     * 这是一个便捷方法，用于焦点状态管理器调用
     */
    fun restoreFocusAtPosition(
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        position: Int,
        onFocusChange: ((Boolean) -> Unit)? = null
    ) {
        try {
            recyclerView.post {
                val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                if (viewHolder != null) {
                    val itemView = viewHolder.itemView
                    val cardView = itemView.findViewById<CardView>(com.tvplayer.webdav.R.id.card_view)
                    val ratingBadge = itemView.findViewById<TextView>(com.tvplayer.webdav.R.id.tv_rating)

                    if (cardView != null) {
                        restoreFocusWithAnimation(itemView, cardView, null, ratingBadge, true, onFocusChange)
                    } else {
                        // 备用方案：只同步状态
                        triggerFocusStateSync(itemView, true)
                        onFocusChange?.invoke(true)
                    }

                    android.util.Log.d("PosterFocusAnimator", "Restored focus at position $position")
                } else {
                    android.util.Log.w("PosterFocusAnimator", "ViewHolder not found for position $position")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PosterFocusAnimator", "Error restoring focus at position $position", e)
        }
    }

    /**
     * 确保点击后焦点状态保持
     * 在点击事件后调用，确保白色边框不会丢失
     */
    fun ensureFocusStateAfterClick(itemView: View) {
        val imageView = itemView.findViewById<ImageView>(com.tvplayer.webdav.R.id.iv_poster)

        // 立即强制设置边框，不等待延迟
        setFocusBorder(imageView, true)

        // 确保View状态正确
        itemView.isSelected = true
        imageView?.isSelected = true

        // 再次延迟确认，防止系统覆盖
        itemView.postDelayed({
            setFocusBorder(imageView, true)
            android.util.Log.d("PosterFocusAnimator", "Force ensured focus border after click")
        }, 50)
    }
}
