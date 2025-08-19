package com.tvplayer.webdav.ui.home;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

/**
 * 海报焦点动画器
 * 为海报卡片提供丰富的焦点切换动画效果
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J,\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0002J,\u0010\u0013\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0002J4\u0010\u0014\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0015\u001a\u00020\u0016H\u0002JB\u0010\u0017\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0016\b\u0002\u0010\u0018\u001a\u0010\u0012\u0004\u0012\u00020\u0016\u0012\u0004\u0012\u00020\n\u0018\u00010\u0019J\u000e\u0010\u001a\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/tvplayer/webdav/ui/home/PosterFocusAnimator;", "", "()V", "ANIMATION_DURATION", "", "SCALE_FOCUSED", "", "SCALE_NORMAL", "STAGGER_DELAY", "animateFocusIn", "", "itemView", "Landroid/view/View;", "cardView", "Landroidx/cardview/widget/CardView;", "playButton", "Landroid/widget/ImageView;", "ratingBadge", "Landroid/widget/TextView;", "animateFocusOut", "animatePosterFocus", "hasFocus", "", "setupPosterFocusAnimation", "onFocusChange", "Lkotlin/Function1;", "startPlayButtonFloatingAnimation", "app_debug"})
public final class PosterFocusAnimator {
    private static final float SCALE_FOCUSED = 1.04F;
    private static final float SCALE_NORMAL = 1.0F;
    private static final long ANIMATION_DURATION = 400L;
    private static final long STAGGER_DELAY = 50L;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.home.PosterFocusAnimator INSTANCE = null;
    
    private PosterFocusAnimator() {
        super();
    }
    
    /**
     * 设置海报焦点动画
     */
    public final void setupPosterFocusAnimation(@org.jetbrains.annotations.NotNull()
    android.view.View itemView, @org.jetbrains.annotations.NotNull()
    androidx.cardview.widget.CardView cardView, @org.jetbrains.annotations.Nullable()
    android.widget.ImageView playButton, @org.jetbrains.annotations.Nullable()
    android.widget.TextView ratingBadge, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onFocusChange) {
    }
    
    /**
     * 海报焦点动画 - 多层次动画效果
     */
    private final void animatePosterFocus(android.view.View itemView, androidx.cardview.widget.CardView cardView, android.widget.ImageView playButton, android.widget.TextView ratingBadge, boolean hasFocus) {
    }
    
    /**
     * 获得焦点动画
     */
    private final void animateFocusIn(android.view.View itemView, androidx.cardview.widget.CardView cardView, android.widget.ImageView playButton, android.widget.TextView ratingBadge) {
    }
    
    /**
     * 失去焦点动画
     */
    private final void animateFocusOut(android.view.View itemView, androidx.cardview.widget.CardView cardView, android.widget.ImageView playButton, android.widget.TextView ratingBadge) {
    }
    
    /**
     * 为播放按钮添加悬浮动画
     */
    public final void startPlayButtonFloatingAnimation(@org.jetbrains.annotations.NotNull()
    android.widget.ImageView playButton) {
    }
}