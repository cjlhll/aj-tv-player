package com.tvplayer.webdav.ui.webdav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.data.model.WebDAVServer
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WebDAV连接ViewModel
 * 处理WebDAV服务器连接逻辑
 */
@HiltViewModel
class WebDAVConnectionViewModel @Inject constructor(
    private val webdavClient: SimpleWebDAVClient,
    private val serverStorage: WebDAVServerStorage
) : ViewModel() {

    private val _connectionStatus = MutableLiveData<String>()
    val connectionStatus: LiveData<String> = _connectionStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _connectionResult = MutableLiveData<Result<Boolean>?>()
    val connectionResult: LiveData<Result<Boolean>?> = _connectionResult

    private val _currentServer = MutableLiveData<WebDAVServer?>()
    val currentServer: LiveData<WebDAVServer?> = _currentServer

    init {
        _connectionStatus.value = "未连接"
        _isLoading.value = false
        // 尝试加载已保存的服务器
        serverStorage.getServer()?.let { saved ->
            _currentServer.value = saved
            _connectionStatus.value = "已加载已保存的配置"
        }
    }

    /**
     * 测试WebDAV服务器连接
     */
    suspend fun testConnection(server: WebDAVServer) {
        _isLoading.value = true
        _connectionStatus.value = "正在测试连接..."

        viewModelScope.launch {
            try {
                val result = webdavClient.connect(server)
                
                if (result.isSuccess) {
                    _connectionStatus.value = "连接测试成功"
                    _connectionResult.value = Result.success(true)
                } else {
                    val error = result.exceptionOrNull()
                    _connectionStatus.value = "连接测试失败: ${error?.message ?: "未知错误"}"
                    _connectionResult.value = Result.failure(error ?: Exception("连接失败"))
                }
            } catch (e: Exception) {
                _connectionStatus.value = "连接测试失败: ${e.message}"
                _connectionResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 保存服务器配置并连接
     */
    suspend fun saveAndConnect(server: WebDAVServer) {
        _isLoading.value = true
        _connectionStatus.value = "正在连接并保存配置..."

        viewModelScope.launch {
            try {
                val result = webdavClient.connect(server)
                
                if (result.isSuccess) {
                    _currentServer.value = server
                    _connectionStatus.value = "已连接到 ${server.name}"
                    _connectionResult.value = Result.success(true)
                    
                    // TODO: 保存服务器配置到数据库
                    saveServerConfig(server)
                } else {
                    val error = result.exceptionOrNull()
                    _connectionStatus.value = "连接失败: ${error?.message ?: "未知错误"}"
                    _connectionResult.value = Result.failure(error ?: Exception("连接失败"))
                }
            } catch (e: Exception) {
                _connectionStatus.value = "连接失败: ${e.message}"
                _connectionResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 断开当前连接
     */
    fun disconnect() {
        webdavClient.disconnect()
        _currentServer.value = null
        _connectionStatus.value = "已断开连接"
        _connectionResult.value = null
    }

    /**
     * 获取当前连接的服务器
     */
    fun getCurrentServer(): WebDAVServer? {
        return _currentServer.value
    }

    /**
     * 检查是否已连接
     */
    fun isConnected(): Boolean {
        return _currentServer.value != null
    }

    /**
     * 保存服务器配置到本地存储
     */
    private fun saveServerConfig(server: WebDAVServer) {
        serverStorage.saveServer(server)
    }

    /**
     * 清除连接结果状态
     */
    fun clearConnectionResult() {
        _connectionResult.value = null
    }
}
