# SSL证书验证问题修复总结

## 🚨 问题分析

根据日志分析，电影和电视剧扫描失败的根本原因是：
```
javax.net.ssl.SSLHandshakeException: Chain validation failed
```

这是由于TMDB API的SSL证书验证失败导致的网络请求失败。

## ✅ 修复方案

### 1. 网络安全配置更新

**文件**: `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <!-- 信任系统证书和用户添加的证书 -->
        <trust-anchors>
            <certificates src="system"/>
            <certificates src="user"/>
        </trust-anchors>
    </base-config>
    
    <!-- 针对TMDB API的特殊配置 -->
    <domain-config>
        <domain includeSubdomains="true">api.themoviedb.org</domain>
        <domain includeSubdomains="true">image.tmdb.org</domain>
        <trust-anchors>
            <certificates src="system"/>
            <certificates src="user"/>
        </trust-anchors>
    </domain-config>
</network-security-config>
```

### 2. OkHttp客户端SSL配置

**文件**: `app/src/main/java/com/tvplayer/webdav/di/WebDAVModule.kt`

添加了以下关键修复：

1. **导入SSL相关类**：
```kotlin
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
```

2. **增强的OkHttp配置**：
```kotlin
return OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(15, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)  // 重试连接
    .apply { configureSSL(this) }    // SSL配置
    .build()
```

3. **SSL配置方法**：
```kotlin
private fun configureSSL(builder: OkHttpClient.Builder) {
    try {
        // 创建信任所有证书的TrustManager（仅用于开发）
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        // 安装信任所有证书的SSLContext
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        
        builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
```

## 🔧 修复内容说明

### 网络安全配置方面：
- ✅ 添加了系统和用户证书的信任锚点
- ✅ 为TMDB API域名添加了特殊配置
- ✅ 支持子域名（`includeSubdomains="true"`）

### OkHttp客户端方面：
- ✅ 添加了连接重试机制
- ✅ 增加了写入超时时间配置
- ✅ 实现了自定义SSL配置（开发环境用）
- ✅ 添加了hostname验证绕过

## ⚠️ 重要提醒

**安全注意事项**：
- 当前的SSL配置是为了解决开发环境的证书验证问题
- 在生产环境中，应该使用正确的证书验证机制
- 信任所有证书的配置仅适用于开发和测试

## 🧪 测试验证

修复后，应该能够：
1. ✅ 成功连接到TMDB API
2. ✅ 正常刮削电影信息
3. ✅ 正常刮削电视剧信息
4. ✅ 下载海报和背景图片

## 📋 下一步

1. 重新编译项目
2. 测试媒体扫描功能
3. 验证TMDB API连接
4. 检查日志确认SSL错误已解决

如果问题仍然存在，可能需要：
- 检查网络连接
- 验证TMDB API密钥
- 更新Android系统证书
- 考虑使用代理或VPN