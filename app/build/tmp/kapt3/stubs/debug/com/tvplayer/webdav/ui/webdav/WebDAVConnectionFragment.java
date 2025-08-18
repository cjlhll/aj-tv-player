package com.tvplayer.webdav.ui.webdav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.tvplayer.webdav.R;
import com.tvplayer.webdav.data.model.WebDAVServer;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * WebDAV连接配置Fragment
 * 允许用户输入服务器信息并测试连接
 */
@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 (2\u00020\u0001:\u0001(B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\n\u0010\u0017\u001a\u0004\u0018\u00010\u0016H\u0002J\b\u0010\u0018\u001a\u00020\u0014H\u0002J\u0010\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u0014H\u0002J&\u0010\u001d\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\u001a\u0010$\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0016J\b\u0010%\u001a\u00020\u0014H\u0002J\b\u0010&\u001a\u00020\u0014H\u0002J\b\u0010\'\u001a\u00020\u0014H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\r\u001a\u00020\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\u0012\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006)"}, d2 = {"Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionFragment;", "Landroidx/fragment/app/Fragment;", "()V", "btnConnect", "Landroid/widget/Button;", "btnTest", "etPassword", "Landroid/widget/EditText;", "etServerName", "etServerUrl", "etUsername", "tvStatus", "Landroid/widget/TextView;", "viewModel", "Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionViewModel;", "getViewModel", "()Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "applyServerToForm", "", "server", "Lcom/tvplayer/webdav/data/model/WebDAVServer;", "createServerFromInput", "fillExampleData", "initViews", "view", "Landroid/view/View;", "observeViewModel", "onCreateView", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onViewCreated", "saveAndConnect", "setupListeners", "testConnection", "Companion", "app_debug"})
public final class WebDAVConnectionFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private android.widget.EditText etServerUrl;
    private android.widget.EditText etUsername;
    private android.widget.EditText etPassword;
    private android.widget.EditText etServerName;
    private android.widget.Button btnConnect;
    private android.widget.Button btnTest;
    private android.widget.TextView tvStatus;
    @org.jetbrains.annotations.NotNull()
    public static final com.tvplayer.webdav.ui.webdav.WebDAVConnectionFragment.Companion Companion = null;
    
    public WebDAVConnectionFragment() {
        super();
    }
    
    private final com.tvplayer.webdav.ui.webdav.WebDAVConnectionViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initViews(android.view.View view) {
    }
    
    private final void setupListeners() {
    }
    
    private final void observeViewModel() {
    }
    
    private final void testConnection() {
    }
    
    private final void saveAndConnect() {
    }
    
    private final com.tvplayer.webdav.data.model.WebDAVServer createServerFromInput() {
        return null;
    }
    
    private final void fillExampleData() {
    }
    
    private final void applyServerToForm(com.tvplayer.webdav.data.model.WebDAVServer server) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionFragment$Companion;", "", "()V", "newInstance", "Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionFragment;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.tvplayer.webdav.ui.webdav.WebDAVConnectionFragment newInstance() {
            return null;
        }
    }
}