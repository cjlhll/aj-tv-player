package com.tvplayer.webdav.ui.scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvplayer.webdav.data.model.MediaItem
import com.tvplayer.webdav.data.model.MediaType
import com.tvplayer.webdav.data.scanner.MediaScanner
import com.tvplayer.webdav.data.model.WebDAVFile
import com.tvplayer.webdav.data.webdav.SimpleWebDAVClient
import com.tvplayer.webdav.data.storage.WebDAVServerStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * 扫描器ViewModel
 * 管理媒体扫描的状态和进度
 */
@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val mediaScanner: MediaScanner,
    private val webdavClient: SimpleWebDAVClient,
    private val serverStorage: WebDAVServerStorage,
    private val mediaCache: com.tvplayer.webdav.data.storage.MediaCache
) : ViewModel() {

    private val _currentPath = MutableLiveData<String>("/")
    val currentPath: LiveData<String> = _currentPath

    private val _directoryItems = MutableLiveData<List<WebDAVFile>>()
    val directoryItems: LiveData<List<WebDAVFile>> = _directoryItems

    private val _browseError = MutableLiveData<String?>()
    val browseError: LiveData<String?> = _browseError



    private val _isScanning = MutableLiveData<Boolean>()
    val isScanning: LiveData<Boolean> = _isScanning

    private val _scanProgress = MutableLiveData<String>()
    val scanProgress: LiveData<String> = _scanProgress

    private val _scanStatus = MutableLiveData<String>()
    val scanStatus: LiveData<String> = _scanStatus

    private val _scanResult = MutableLiveData<Result<List<MediaItem>>?>()
    val scanResult: LiveData<Result<List<MediaItem>>?> = _scanResult

    init {
        _isScanning.value = false
        _scanStatus.value = "准备扫描"
        _scanProgress.value = ""
    }


    fun canStartScan(): Boolean = !serverStorage.getMoviesDir().isNullOrEmpty() || !serverStorage.getTvDir().isNullOrEmpty()


    /**
     * 扫描电影目录
     */
    fun startScanMovies() {
        val moviesDir = serverStorage.getMoviesDir()
        if (moviesDir.isNullOrEmpty()) return

        if (_isScanning.value == true) return
        _isScanning.value = true
        _scanResult.value = null
        _scanStatus.value = "开始扫描电影目录..."
        _scanProgress.value = "准备中..."

        viewModelScope.launch {
            try {
                // 连接服务器
                val server = serverStorage.getServer()
                if (server == null) {
                    _scanStatus.value = "未找到已保存的WebDAV服务器"
                    _scanResult.value = Result.failure(IllegalStateException("No saved server"))
                    _isScanning.value = false
                    return@launch
                }
                val connect = webdavClient.connect(server)
                if (connect.isFailure) {
                    _scanStatus.value = "连接服务器失败"
                    _scanResult.value = Result.failure(connect.exceptionOrNull() ?: Exception("连接失败"))
                    _isScanning.value = false
                    return@launch
                }

                _scanStatus.postValue("扫描电影目录: ${decodeForDisplay(moviesDir)}")
                val items = mediaScanner.scanDirectory(moviesDir, recursive = true, modeHint = MediaScanner.ModeHint.MOVIE, callback = object : MediaScanner.ScanProgressCallback {
                    override fun onProgress(current: Int, total: Int, currentFile: String) {
                        _scanProgress.postValue("电影: $current/$total")
                    }
                    override fun onComplete(scannedItems: List<MediaItem>) {}
                    override fun onError(error: String) { _scanStatus.postValue("电影扫描错误: $error") }
                })

                // 获取现有缓存并添加新的电影项目
                val existingItems = mediaCache.getItems().toMutableList()
                // 移除现有的电影项目（避免重复）
                existingItems.removeAll { it.mediaType == MediaType.MOVIE }
                // 添加新扫描的电影项目
                existingItems.addAll(items)
                mediaCache.setItems(existingItems)

                _scanResult.postValue(Result.success(items))
                _scanStatus.postValue("电影扫描完成: 共 ${items.size} 个")
                _isScanning.postValue(false)

            } catch (e: Exception) {
                _scanProgress.postValue("扫描失败")
                _scanStatus.postValue("错误: ${e.message}")
                _scanResult.postValue(Result.failure(e))
                _isScanning.postValue(false)
            }
        }
    }

    /**
     * 扫描电视剧目录
     */
    fun startScanTv() {
        val tvDir = serverStorage.getTvDir()
        if (tvDir.isNullOrEmpty()) return

        if (_isScanning.value == true) return
        _isScanning.value = true
        _scanResult.value = null
        _scanStatus.value = "开始扫描电视剧目录..."
        _scanProgress.value = "准备中..."

        viewModelScope.launch {
            try {
                // 连接服务器
                val server = serverStorage.getServer()
                if (server == null) {
                    _scanStatus.value = "未找到已保存的WebDAV服务器"
                    _scanResult.value = Result.failure(IllegalStateException("No saved server"))
                    _isScanning.value = false
                    return@launch
                }
                val connect = webdavClient.connect(server)
                if (connect.isFailure) {
                    _scanStatus.value = "连接服务器失败"
                    _scanResult.value = Result.failure(connect.exceptionOrNull() ?: Exception("连接失败"))
                    _isScanning.value = false
                    return@launch
                }

                _scanStatus.postValue("扫描电视剧目录: ${decodeForDisplay(tvDir)}")
                val items = mediaScanner.scanDirectory(tvDir, recursive = true, modeHint = MediaScanner.ModeHint.TV, callback = object : MediaScanner.ScanProgressCallback {
                    override fun onProgress(current: Int, total: Int, currentFile: String) {
                        _scanProgress.postValue("电视剧: $current/$total")
                    }
                    override fun onComplete(scannedItems: List<MediaItem>) {}
                    override fun onError(error: String) { _scanStatus.postValue("电视剧扫描错误: $error") }
                })

                // 获取现有缓存并添加新的电视剧项目
                val existingItems = mediaCache.getItems().toMutableList()
                // 移除现有的电视剧项目（避免重复）
                existingItems.removeAll { it.mediaType == MediaType.TV_SERIES || it.mediaType == MediaType.TV_EPISODE }
                // 添加新扫描的电视剧项目
                existingItems.addAll(items)
                mediaCache.setItems(existingItems)

                _scanResult.postValue(Result.success(items))
                _scanStatus.postValue("电视剧扫描完成: 共 ${items.size} 个")
                _isScanning.postValue(false)

            } catch (e: Exception) {
                _scanProgress.postValue("扫描失败")
                _scanStatus.postValue("错误: ${e.message}")
                _scanResult.postValue(Result.failure(e))
                _isScanning.postValue(false)
            }
        }
    }

    /**
     * 扫描电影和电视剧目录
     */
    fun startScanMoviesAndTv(moviesDir: String?, tvDir: String?) {
        if (_isScanning.value == true) return
        _isScanning.value = true
        _scanResult.value = null
        _scanStatus.value = "开始扫描电影/电视剧目录..."
        _scanProgress.value = "准备中..."

        viewModelScope.launch {
            try {
                // 连接服务器
                val server = serverStorage.getServer()
                if (server == null) {
                    _scanStatus.value = "未找到已保存的WebDAV服务器"
                    _scanResult.value = Result.failure(IllegalStateException("No saved server"))
                    _isScanning.value = false
                    return@launch
                }
                val connect = webdavClient.connect(server)
                if (connect.isFailure) {
                    _scanStatus.value = "连接服务器失败"
                    _scanResult.value = Result.failure(connect.exceptionOrNull() ?: Exception("连接失败"))
                    _isScanning.value = false
                    return@launch
                }

                val allItems = mutableListOf<MediaItem>()

                if (!moviesDir.isNullOrEmpty()) {
                    _scanStatus.postValue("扫描电影目录: ${decodeForDisplay(moviesDir)}")
                    val items = mediaScanner.scanDirectory(moviesDir, recursive = true, modeHint = MediaScanner.ModeHint.MOVIE, callback = object : MediaScanner.ScanProgressCallback {
                        override fun onProgress(current: Int, total: Int, currentFile: String) {
                            _scanProgress.postValue("电影: $current/$total")
                        }
                        override fun onComplete(scannedItems: List<MediaItem>) {}
                        override fun onError(error: String) { _scanStatus.postValue("电影扫描错误: $error") }
                    })
                    allItems.addAll(items)
                }

                if (!tvDir.isNullOrEmpty()) {
                    _scanStatus.postValue("扫描电视剧目录: ${decodeForDisplay(tvDir)}")
                    val items = mediaScanner.scanDirectory(tvDir, recursive = true, modeHint = MediaScanner.ModeHint.TV, callback = object : MediaScanner.ScanProgressCallback {
                        override fun onProgress(current: Int, total: Int, currentFile: String) {
                            _scanProgress.postValue("电视剧: $current/$total")
                        }
                        override fun onComplete(scannedItems: List<MediaItem>) {}
                        override fun onError(error: String) { _scanStatus.postValue("电视剧扫描错误: $error") }
                    })
                    allItems.addAll(items)
                }

                // 更新缓存，供首页显示海报
                mediaCache.setItems(allItems)
                _scanResult.postValue(Result.success(allItems))
                _scanStatus.postValue("扫描完成: 共 ${allItems.size} 个")
                _isScanning.postValue(false)

            } catch (e: Exception) {
                _scanProgress.postValue("扫描失败")
                _scanStatus.postValue("错误: ${e.message}")
                _scanResult.postValue(Result.failure(e))
                _isScanning.postValue(false)
            }
        }
    }

    /**
     * 开始扫描
     */
    suspend fun startScan(path: String, recursive: Boolean = true) {
        if (_isScanning.value == true) return

        _isScanning.value = true
        _scanResult.value = null
        _scanStatus.value = "开始扫描..."
        _scanProgress.value = "准备中..."

        viewModelScope.launch {
            try {
                // 确保已连接
                val server = serverStorage.getServer()
                if (server == null) {
                    _scanStatus.value = "未找到已保存的WebDAV服务器"
                    _scanResult.value = Result.failure(IllegalStateException("No saved server"))
                    _isScanning.value = false
                    return@launch
                }
                val connect = webdavClient.connect(server)
                if (connect.isFailure) {
                    _scanStatus.value = "连接服务器失败"
                    _scanResult.value = Result.failure(connect.exceptionOrNull() ?: Exception("连接失败"))
                    _isScanning.value = false
                    return@launch
                }

                val callback = object : MediaScanner.ScanProgressCallback {
                    override fun onProgress(current: Int, total: Int, currentFile: String) {
                        _scanProgress.postValue("扫描进度: $current/$total")
                        _scanStatus.postValue("正在处理: ${decodeForDisplay(currentFile.substringAfterLast('/'))}")
                    }

                    override fun onComplete(scannedItems: List<MediaItem>) {
                        _scanProgress.postValue("扫描完成")
                        _scanStatus.postValue("成功扫描 ${scannedItems.size} 个媒体文件")
                        _scanResult.postValue(Result.success(scannedItems))
                        _isScanning.postValue(false)
                    }

                    override fun onError(error: String) {
                        _scanProgress.postValue("扫描失败")
                        _scanStatus.postValue("错误: $error")
                        _scanResult.postValue(Result.failure(Exception(error)))
                        _isScanning.postValue(false)
                    }
                }

                mediaScanner.scanDirectory(path, recursive, callback)

            } catch (e: Exception) {
                _scanProgress.value = "扫描失败"
                _scanStatus.value = "错误: ${e.message}"
                _scanResult.value = Result.failure(e)
                _isScanning.value = false
            }
        }
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        // TODO: 实现扫描取消逻辑
        _isScanning.value = false
        _scanStatus.value = "扫描已停止"
        _scanProgress.value = ""
    }

    /**
     * 清除扫描结果
     */
    fun clearScanResult() {
        _scanResult.value = null
    }

    /**
     * 连接并列出当前路径
     */
    fun loadCurrentDirectory() {
        viewModelScope.launch {
            try {
                val server = serverStorage.getServer()
                if (server == null) {
                    _browseError.value = "未找到已保存的WebDAV服务器"
                    return@launch
                }
                val connect = webdavClient.connect(server)
                if (connect.isFailure) {
                    _browseError.value = connect.exceptionOrNull()?.message ?: "连接失败"
                    return@launch
                }

                val p = _currentPath.value ?: "/"
                var res = webdavClient.listFiles(p)
                if (res.isFailure) {
                    val retryPath = if (p.endsWith("/")) p else "$p/"
                    if (retryPath != p) {
                        res = webdavClient.listFiles(retryPath)
                        if (res.isSuccess) {
                            _currentPath.value = retryPath // 同步当前路径
                        }
                    }
                }
                _directoryItems.value = res.getOrNull().orEmpty()
                _browseError.value = if (res.isSuccess) null else (res.exceptionOrNull()?.message ?: "列目录失败")
            } catch (e: Exception) {
                _browseError.value = e.message
            }
        }
    }

    /**
     * 进入子目录
     */
    fun enterDirectory(dir: WebDAVFile) {
        if (!dir.isDirectory) return
        _currentPath.value = dir.path
        loadCurrentDirectory()
    }

    // 分类目录存取
    fun setMoviesDir(path: String) = serverStorage.setMoviesDir(path)
    fun setTvDir(path: String) = serverStorage.setTvDir(path)
    fun getMoviesDir(): String? = serverStorage.getMoviesDir()
    fun getTvDir(): String? = serverStorage.getTvDir()

    /**
     * 返回上级目录
     */
    fun goUp() {
        val path = _currentPath.value ?: "/"
        val trimmed = path.trimEnd('/')
        val parent = if (trimmed.isEmpty() || trimmed == "/") "/" else trimmed.substringBeforeLast('/', missingDelimiterValue = "/") + "/"
        _currentPath.value = parent
        loadCurrentDirectory()
    }

    /**
     * 解码用于显示的路径/文件名，优先UTF-8，处理多次编码，必要时回退GBK/ISO-8859-1
     */
    private fun decodeForDisplay(input: String?): String {
        if (input.isNullOrEmpty()) return input ?: ""
        val s = input
        return try {
            // 快速路径：无%则直接返回
            if (!s.contains("%")) return s
            var decoded = URLDecoder.decode(s, StandardCharsets.UTF_8.toString())
            var prev = decoded
            // 处理双重/多重编码
            repeat(3) {
                if (!decoded.contains("%")) return@repeat
                val next = try { URLDecoder.decode(decoded, StandardCharsets.UTF_8.toString()) } catch (_: Exception) { decoded }
                if (next == prev) return@repeat
                prev = next
                decoded = next
            }
            decoded
        } catch (_: Exception) {
            try {
                URLDecoder.decode(s, "GBK")
            } catch (_: Exception) {
                try { URLDecoder.decode(s, "ISO-8859-1") } catch (_: Exception) { s }
            }
        }
    }

}
