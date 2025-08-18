package com.tvplayer.webdav.data.storage

import android.content.SharedPreferences
import com.tvplayer.webdav.data.model.WebDAVServer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用SharedPreferences保存/读取WebDAV服务器配置
 */
@Singleton
class WebDAVServerStorage @Inject constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_NAME = "webdav_name"
        private const val KEY_URL = "webdav_url"
        private const val KEY_USERNAME = "webdav_username"
        private const val KEY_PASSWORD = "webdav_password"
        private const val KEY_IS_DEFAULT = "webdav_is_default"
        private const val KEY_IS_ACTIVE = "webdav_is_active"
        private const val KEY_MOVIES_DIR = "webdav_movies_dir"
        private const val KEY_TV_DIR = "webdav_tv_dir"
    }

    fun saveServer(server: WebDAVServer) {
        prefs.edit()
            .putString(KEY_NAME, server.name)
            .putString(KEY_URL, server.url)
            .putString(KEY_USERNAME, server.username)
            .putString(KEY_PASSWORD, server.password)
            .putBoolean(KEY_IS_DEFAULT, server.isDefault)
            .putBoolean(KEY_IS_ACTIVE, server.isActive)
            .apply()
    }

    fun getServer(): WebDAVServer? {
        val url = prefs.getString(KEY_URL, null) ?: return null
        val name = prefs.getString(KEY_NAME, "默认服务器") ?: "默认服务器"
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        val isDefault = prefs.getBoolean(KEY_IS_DEFAULT, true)
        val isActive = prefs.getBoolean(KEY_IS_ACTIVE, true)
        return WebDAVServer(
            name = name,
            url = url,
            username = username,
            password = password,
            isDefault = isDefault,
            isActive = isActive
        )
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_NAME)
            .remove(KEY_URL)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_IS_DEFAULT)
            .remove(KEY_IS_ACTIVE)
            .remove(KEY_MOVIES_DIR)
            .remove(KEY_TV_DIR)
            .apply()
    }

    fun setMoviesDir(path: String?) {
        prefs.edit().putString(KEY_MOVIES_DIR, path ?: "").apply()
    }

    fun setTvDir(path: String?) {
        prefs.edit().putString(KEY_TV_DIR, path ?: "").apply()
    }

    fun getMoviesDir(): String? = prefs.getString(KEY_MOVIES_DIR, null)
    fun getTvDir(): String? = prefs.getString(KEY_TV_DIR, null)
}


