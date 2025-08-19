package com.tvplayer.webdav.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
    
    private const val SCALE_FOCUSED = 1.04f  // 减小缩放避免裁剪
    private const val SCALE_NORMAL = 1.0f
    private const val ANIMATION_DURATION = 400L
    private const val STAGGER_DELAY = 50L  // 错开动画延迟
    
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
        itemView.setOnFocusChangeListener { _, hasFocus ->
            animatePosterFocus(itemView, cardView, playButton, ratingBadge, hasFocus)
            onFocusChange?.invoke(hasFocus)
        }
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
     * 获得焦点动画
     */
    private fun animateFocusIn(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?
    ) {
        // 主容器缩放动画（移除阴影效果）
        val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", itemView.scaleX, SCALE_FOCUSED)
        val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", itemView.scaleY, SCALE_FOCUSED)

        // 边框透明度动画（增强焦点效果）
        val borderAlpha = ObjectAnimator.ofFloat(itemView, "alpha", itemView.alpha, 1.0f)
        
        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playTogether(scaleX, scaleY, borderAlpha)
        mainAnimatorSet.duration = ANIMATION_DURATION
        mainAnimatorSet.interpolator = OvershootInterpolator(0.15f)
        
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
     * 失去焦点动画
     */
    private fun animateFocusOut(
        itemView: View,
        cardView: CardView,
        playButton: ImageView?,
        ratingBadge: TextView?
    ) {
        // 主容器恢复动画（移除阴影效果）
        val scaleX = ObjectAnimator.ofFloat(itemView, "scaleX", itemView.scaleX, SCALE_NORMAL)
        val scaleY = ObjectAnimator.ofFloat(itemView, "scaleY", itemView.scaleY, SCALE_NORMAL)

        // 边框透明度恢复
        val borderAlpha = ObjectAnimator.ofFloat(itemView, "alpha", itemView.alpha, 1.0f)

        val mainAnimatorSet = AnimatorSet()
        mainAnimatorSet.playTogether(scaleX, scaleY, borderAlpha)
        mainAnimatorSet.duration = ANIMATION_DURATION
        mainAnimatorSet.interpolator = AccelerateDecelerateInterpolator()
        
        // 播放按钮动画 - 淡出
        playButton?.let { button ->
            if (button.visibility == View.VISIBLE) {
                val fadeOut = ObjectAnimator.ofFloat(button, "alpha", button.alpha, 0.9f)
                val buttonScale = ObjectAnimator.ofFloat(button, "scaleX", button.scaleX, 0.95f)
                val buttonScaleY = ObjectAnimator.ofFloat(button, "scaleY", button.scaleY, 0.95f)
                
                val buttonAnimatorSet = AnimatorSet()
                buttonAnimatorSet.playTogether(fadeOut, buttonScale, buttonScaleY)
                buttonAnimatorSet.duration = ANIMATION_DURATION / 2
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
                badgeAnimatorSet.duration = ANIMATION_DURATION / 2
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
}
