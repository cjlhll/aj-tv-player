package com.tvplayer.webdav.ui.main;

import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import com.tvplayer.webdav.R;
import kotlin.properties.Delegates;

/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u0000 !2\u00020\u0001:\u0001!B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\u0016J\u0010\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\u0010\u0010\u001b\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015H\u0016J\u0018\u0010\u001c\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002R+\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\n\u0010\u000b\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R+\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u00048B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0011\u0010\u000b\u001a\u0004\b\u000f\u0010\u0007\"\u0004\b\u0010\u0010\t\u00a8\u0006\""}, d2 = {"Lcom/tvplayer/webdav/ui/main/CardPresenter;", "Landroidx/leanback/widget/Presenter;", "()V", "<set-?>", "", "mDefaultBackgroundColor", "getMDefaultBackgroundColor", "()I", "setMDefaultBackgroundColor", "(I)V", "mDefaultBackgroundColor$delegate", "Lkotlin/properties/ReadWriteProperty;", "mDefaultCardImage", "Landroid/graphics/drawable/Drawable;", "mSelectedBackgroundColor", "getMSelectedBackgroundColor", "setMSelectedBackgroundColor", "mSelectedBackgroundColor$delegate", "onBindViewHolder", "", "viewHolder", "Landroidx/leanback/widget/Presenter$ViewHolder;", "item", "", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "onUnbindViewHolder", "updateCardBackgroundColor", "view", "Landroidx/leanback/widget/ImageCardView;", "selected", "", "Companion", "app_debug"})
public final class CardPresenter extends androidx.leanback.widget.Presenter {
    @org.jetbrains.annotations.Nullable()
    private android.graphics.drawable.Drawable mDefaultCardImage;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.properties.ReadWriteProperty mSelectedBackgroundColor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.properties.ReadWriteProperty mDefaultBackgroundColor$delegate = null;
    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.main.CardPresenter.Companion Companion = null;
    
    public CardPresenter() {
        super();
    }
    
    private final int getMSelectedBackgroundColor() {
        return 0;
    }
    
    private final void setMSelectedBackgroundColor(int p0) {
    }
    
    private final int getMDefaultBackgroundColor() {
        return 0;
    }
    
    private final void setMDefaultBackgroundColor(int p0) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.leanback.widget.Presenter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    androidx.leanback.widget.Presenter.ViewHolder viewHolder, @org.jetbrains.annotations.NotNull()
    java.lang.Object item) {
    }
    
    @java.lang.Override()
    public void onUnbindViewHolder(@org.jetbrains.annotations.NotNull()
    androidx.leanback.widget.Presenter.ViewHolder viewHolder) {
    }
    
    private final void updateCardBackgroundColor(androidx.leanback.widget.ImageCardView view, boolean selected) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/tvplayer/webdav/ui/main/CardPresenter$Companion;", "", "()V", "CARD_HEIGHT", "", "CARD_WIDTH", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}