package com.tvplayer.webdav.ui.home

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator

/**
 * 焦点高亮辅助类
 * 为TV界面添加焦点动画效果，优化遥控器选择体验
 */
object FocusHighlightHelper {

    private const val SCALE_FOCUSED = 1.04f  // 减少缩放比例，避免边框被裁剪
    private const val SCALE_NORMAL = 1.0f
    private const val ANIMATION_DURATION = 300L  // 更平滑的动画时长

    /**
     * 设置焦点变化监听器
     */
    fun setupFocusHighlight(view: View) {
        view.setOnFocusChangeListener { v, hasFocus ->
            animateFocusChange(v, hasFocus)
        }
    }

    /**
     * 焦点变化动画 - 优化的平滑动画效果
     */
    private fun animateFocusChange(view: View, hasFocus: Boolean) {
        // 取消之前的动画
        view.clearAnimation()

        val scale = if (hasFocus) SCALE_FOCUSED else SCALE_NORMAL

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, scale)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, scale)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = ANIMATION_DURATION

        // 获得焦点时使用弹性插值器，失去焦点时使用平滑插值器
        animatorSet.interpolator = if (hasFocus) {
            OvershootInterpolator(0.1f)
        } else {
            AccelerateDecelerateInterpolator()
        }

        animatorSet.start()
    }
    
    /**
     * 为RecyclerView的所有子项设置焦点高亮
     */
    fun setupRecyclerViewFocusHighlight(view: View) {
        setupFocusHighlight(view)
    }
}
