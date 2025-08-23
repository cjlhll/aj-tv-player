package com.tvplayer.webdav.ui.settings

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button

/**
 * 设置页面焦点动画器
 * 为设置页面的按钮提供明显的焦点选择效果
 */
object SettingsFocusAnimator {

    private const val SCALE_FOCUSED = 1.05f
    private const val SCALE_NORMAL = 1.0f
    private const val ANIMATION_DURATION = 250L
    private const val ELEVATION_FOCUSED = 16f
    private const val ELEVATION_NORMAL = 4f

    /**
     * 设置按钮焦点动画
     */
    fun setupButtonFocusAnimation(button: Button) {
        button.setOnFocusChangeListener { _, hasFocus ->
            animateButtonFocus(button, hasFocus)
        }
    }

    /**
     * 按钮焦点动画
     */
    private fun animateButtonFocus(button: Button, hasFocus: Boolean) {
        // 取消之前的动画
        button.clearAnimation()

        if (hasFocus) {
            animateFocusIn(button)
        } else {
            animateFocusOut(button)
        }
    }

    /**
     * 获得焦点动画
     */
    private fun animateFocusIn(button: Button) {
        // 缩放动画
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", button.scaleX, SCALE_FOCUSED)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", button.scaleY, SCALE_FOCUSED)
        
        // 阴影动画
        val elevation = ObjectAnimator.ofFloat(button, "elevation", button.elevation, ELEVATION_FOCUSED)
        
        // 透明度动画（增强焦点效果）
        val alpha = ObjectAnimator.ofFloat(button, "alpha", button.alpha, 1.0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, elevation, alpha)
        animatorSet.duration = ANIMATION_DURATION
        animatorSet.interpolator = OvershootInterpolator(0.2f)
        animatorSet.start()
    }

    /**
     * 失去焦点动画
     */
    private fun animateFocusOut(button: Button) {
        // 缩放动画
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", button.scaleX, SCALE_NORMAL)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", button.scaleY, SCALE_NORMAL)
        
        // 阴影动画
        val elevation = ObjectAnimator.ofFloat(button, "elevation", button.elevation, ELEVATION_NORMAL)
        
        // 透明度动画
        val alpha = ObjectAnimator.ofFloat(button, "alpha", button.alpha, 0.9f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, elevation, alpha)
        animatorSet.duration = ANIMATION_DURATION
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }

    /**
     * 为所有设置按钮设置焦点动画
     */
    fun setupAllSettingsButtons(vararg buttons: Button) {
        buttons.forEach { button ->
            setupButtonFocusAnimation(button)
        }
    }
} 