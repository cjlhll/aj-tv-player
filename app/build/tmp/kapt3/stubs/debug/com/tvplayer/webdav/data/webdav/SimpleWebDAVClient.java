package com.tvplayer.webdav.data.webdav;

import com.tvplayer.webdav.data.model.WebDAVFile;
import com.tvplayer.webdav.data.model.WebDAVServer;
import kotlinx.coroutines.Dispatchers;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 简单的WebDAV客户端实现
 * 使用OkHttp实现基本的WebDAV操作
 */
@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J$\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\u0006H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\r\u0010\u000eJ\b\u0010\u000f\u001a\u00020\u0010H\u0002J\u0006\u0010\u0011\u001a\u00020\u0012J\u001a\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0014H\u0002J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0018\u001a\u00020\u0019J,\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u001b0\n2\b\b\u0002\u0010\u001c\u001a\u00020\u0014H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001d\u0010\u001eJ\u0018\u0010\u001f\u001a\u00020\u00142\u0006\u0010 \u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u0014H\u0002J\u0012\u0010\"\u001a\u0004\u0018\u00010#2\u0006\u0010$\u001a\u00020\u0014H\u0002J&\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00190\u001b2\u0006\u0010&\u001a\u00020\u00142\u0006\u0010\'\u001a\u00020\u00142\u0006\u0010!\u001a\u00020\u0014H\u0002J\u0016\u0010(\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006)"}, d2 = {"Lcom/tvplayer/webdav/data/webdav/SimpleWebDAVClient;", "", "baseHttpClient", "Lokhttp3/OkHttpClient;", "(Lokhttp3/OkHttpClient;)V", "currentServer", "Lcom/tvplayer/webdav/data/model/WebDAVServer;", "sardine", "Lcom/thegrizzlylabs/sardineandroid/Sardine;", "connect", "Lkotlin/Result;", "", "server", "connect-gIAlu-s", "(Lcom/tvplayer/webdav/data/model/WebDAVServer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createPropfindBody", "Lokhttp3/RequestBody;", "disconnect", "", "extractXmlValue", "", "xml", "tag", "getFileUrl", "file", "Lcom/tvplayer/webdav/data/model/WebDAVFile;", "listFiles", "", "path", "listFiles-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "normalizeHref", "rawHref", "baseUrl", "parseDate", "Ljava/util/Date;", "dateString", "parseWebDAVResponse", "responseBody", "currentPath", "testConnection", "app_debug"})
public final class SimpleWebDAVClient {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient baseHttpClient = null;
    @org.jetbrains.annotations.Nullable()
    private com.thegrizzlylabs.sardineandroid.Sardine sardine;
    @org.jetbrains.annotations.Nullable()
    private com.tvplayer.webdav.data.model.WebDAVServer currentServer;
    
    @javax.inject.Inject()
    public SimpleWebDAVClient(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient baseHttpClient) {
        super();
    }
    
    /**
     * 测试连接
     */
    private final java.lang.Object testConnection(com.tvplayer.webdav.data.model.WebDAVServer server, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * 规范化WebDAV href为以/开头的相对路径，并剥离baseUrl前缀
     */
    private final java.lang.String normalizeHref(java.lang.String rawHref, java.lang.String baseUrl) {
        return null;
    }
    
    /**
     * 获取文件下载URL
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFileUrl(@org.jetbrains.annotations.NotNull()
    com.tvplayer.webdav.data.model.WebDAVFile file) {
        return null;
    }
    
    /**
     * 创建PROPFIND请求体
     */
    private final okhttp3.RequestBody createPropfindBody() {
        return null;
    }
    
    /**
     * 解析WebDAV响应（宽松解析，忽略命名空间与大小写）
     */
    private final java.util.List<com.tvplayer.webdav.data.model.WebDAVFile> parseWebDAVResponse(java.lang.String responseBody, java.lang.String currentPath, java.lang.String baseUrl) {
        return null;
    }
    
    /**
     * 从XML中提取值
     */
    private final java.lang.String extractXmlValue(java.lang.String xml, java.lang.String tag) {
        return null;
    }
    
    /**
     * 解析日期
     */
    private final java.util.Date parseDate(java.lang.String dateString) {
        return null;
    }
    
    /**
     * 断开连接
     */
    public final void disconnect() {
    }
}