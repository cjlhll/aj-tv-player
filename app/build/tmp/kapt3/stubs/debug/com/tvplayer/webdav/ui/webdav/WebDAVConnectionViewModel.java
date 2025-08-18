package com.tvplayer.webdav.ui.webdav;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.tvplayer.webdav.data.model.WebDAVServer;
import com.tvplayer.webdav.data.storage.WebDAVServerStorage;
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

/**
 * WebDAV连接ViewModel
 * 处理WebDAV服务器连接逻辑
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u0019\u001a\u00020\u001aJ\u0006\u0010\u001b\u001a\u00020\u001aJ\b\u0010\u0017\u001a\u0004\u0018\u00010\u000eJ\u0006\u0010\u001c\u001a\u00020\nJ\u0016\u0010\u001d\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u001fJ\u0010\u0010 \u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u000eH\u0002J\u0016\u0010!\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u001fR\u001c\u0010\u0007\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001f\u0010\u0010\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\n\u0018\u00010\t0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u0019\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0013R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\n0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/tvplayer/webdav/ui/webdav/WebDAVConnectionViewModel;", "Landroidx/lifecycle/ViewModel;", "webdavClient", "Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "serverStorage", "Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;", "(Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;Lcom/tvplayer/webdav/data/storage/WebDAVServerStorage;)V", "_connectionResult", "Landroidx/lifecycle/MutableLiveData;", "Lkotlin/Result;", "", "_connectionStatus", "", "_currentServer", "Lcom/tvplayer/webdav/data/model/WebDAVServer;", "_isLoading", "connectionResult", "Landroidx/lifecycle/LiveData;", "getConnectionResult", "()Landroidx/lifecycle/LiveData;", "connectionStatus", "getConnectionStatus", "currentServer", "getCurrentServer", "isLoading", "clearConnectionResult", "", "disconnect", "isConnected", "saveAndConnect", "server", "(Lcom/tvplayer/webdav/data/model/WebDAVServer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveServerConfig", "testConnection", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class WebDAVConnectionViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _connectionStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> connectionStatus = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<kotlin.Result<java.lang.Boolean>> _connectionResult = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<kotlin.Result<java.lang.Boolean>> connectionResult = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<com.tvplayer.webdav.data.model.WebDAVServer> _currentServer = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<com.tvplayer.webdav.data.model.WebDAVServer> currentServer = null;
    
    @javax.inject.Inject()
    public WebDAVConnectionViewModel(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.webdav.SimpleWebDAVClient webdavClient, @org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.storage.WebDAVServerStorage serverStorage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getConnectionStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<kotlin.Result<java.lang.Boolean>> getConnectionResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<com.tvplayer.webdav.data.model.WebDAVServer> getCurrentServer() {
        return null;
    }
    
    /**
     * 测试WebDAV服务器连接
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object testConnection(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.WebDAVServer server, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 保存服务器配置并连接
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveAndConnect(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.WebDAVServer server, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * 断开当前连接
     */
    public final void disconnect() {
    }
    
    /**
     * 获取当前连接的服务器
     */
    @org.jetbrains.annotations.Nullable()
    public final com.tvplayer.webdav.data.model.WebDAVServer getCurrentServer() {
        return null;
    }
    
    /**
     * 检查是否已连接
     */
    public final boolean isConnected() {
        return false;
    }
    
    /**
     * 保存服务器配置到本地存储
     */
    private final void saveServerConfig(com.tvplayer.webdav.data.model.WebDAVServer server) {
    }
    
    /**
     * 清除连接结果状态
     */
    public final void clearConnectionResult() {
    }
}