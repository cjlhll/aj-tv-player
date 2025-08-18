package com.tvplayer.webdav.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * WebDAV服务器配置数据模型
 */
@Parcelize
data class WebDAVServer(
    val id: Long = 0L,
    val name: String,
    val url: String,
    val username: String,
    val password: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
) : Parcelable {
    
    /**
     * 获取格式化的服务器URL
     */
    fun getFormattedUrl(): String {
        return if (url.endsWith("/")) url else "$url/"
    }
    
    /**
     * 验证服务器配置是否有效
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && 
               url.isNotBlank() && 
               username.isNotBlank() && 
               password.isNotBlank() &&
               (url.startsWith("http://") || url.startsWith("https://"))
    }
    
    /**
     * 获取显示名称（隐藏敏感信息）
     */
    fun getDisplayInfo(): String {
        val host = try {
            java.net.URL(url).host
        } catch (e: Exception) {
            url
        }
        return "$name ($host)"
    }
}
