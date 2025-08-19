package com.tvplayer.webdav.ui.details;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.Actor;

/**
 * 演员列表适配器
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0013B\'\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\b\u0010\n\u001a\u00020\u000bH\u0016J\u001c\u0010\f\u001a\u00020\b2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\u000bH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/tvplayer/webdav/ui/details/ActorAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/tvplayer/webdav/ui/details/ActorAdapter$ActorViewHolder;", "actors", "", "Lcom/tvplayer/webdav/data/model/Actor;", "onActorClick", "Lkotlin/Function1;", "", "(Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ActorViewHolder", "app_debug"})
public final class ActorAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.tvplayer.webdav.ui.details.ActorAdapter.ActorViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tvplayer.webdav.data.model.Actor> actors = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.tvplayer.webdav.data.model.Actor, kotlin.Unit> onActorClick = null;
    
    public ActorAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tvplayer.webdav.data.model.Actor> actors, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tvplayer.webdav.data.model.Actor, kotlin.Unit> onActorClick) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tvplayer.webdav.ui.details.ActorAdapter.ActorViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.ui.details.ActorAdapter.ActorViewHolder holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/tvplayer/webdav/ui/details/ActorAdapter$ActorViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "itemView", "Landroid/view/View;", "(Lcom/tvplayer/webdav/ui/details/ActorAdapter;Landroid/view/View;)V", "ivActorAvatar", "Landroid/widget/ImageView;", "tvActorName", "Landroid/widget/TextView;", "tvActorRole", "bind", "", "actor", "Lcom/tvplayer/webdav/data/model/Actor;", "app_debug"})
    public final class ActorViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivActorAvatar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvActorName = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvActorRole = null;
        
        public ActorViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View itemView) {
            super(null);
        }
        
        public final void bind(@org.jetbrains.annotations.NotNull()
        com.tvplayer.webdav.data.model.Actor actor) {
        }
    }
}