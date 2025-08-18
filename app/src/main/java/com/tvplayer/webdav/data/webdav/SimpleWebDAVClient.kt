package com.tvplayer.webdav.data.webdav

import com.tvplayer.webdav.data.model.WebDAVFile
import com.tvplayer.webdav.data.model.WebDAVServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 简单的WebDAV客户端实现
 * 使用OkHttp实现基本的WebDAV操作
 */
@Singleton
class SimpleWebDAVClient @Inject constructor(
    private val baseHttpClient: OkHttpClient
) {

    private var client: OkHttpClient? = null
    private var currentServer: WebDAVServer? = null
    
    /**
     * 连接到WebDAV服务器
     */
    suspend fun connect(server: WebDAVServer): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val authenticator = object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    val credential = Credentials.basic(server.username, server.password)
                    return response.request.newBuilder()
                        .header("Authorization", credential)
                        .build()
                }
            }
            
            client = baseHttpClient.newBuilder()
                .authenticator(authenticator)
                .build()
            
            currentServer = server
            
            // 测试连接
            val testResult = testConnection(server)
            Result.success(testResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 测试连接
     */
    private suspend fun testConnection(server: WebDAVServer): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(server.getFormattedUrl())
                .method("PROPFIND", createPropfindBody())
                .header("Depth", "0")
                .header("Content-Type", "application/xml")
                .build()
            
            client?.newCall(request)?.execute()?.use { response ->
                response.isSuccessful
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 列出目录内容
     */
    suspend fun listFiles(path: String = "/"): Result<List<WebDAVFile>> = withContext(Dispatchers.IO) {
        try {
            val server = currentServer ?: return@withContext Result.failure(
                IllegalStateException("Not connected to any server")
            )

            val normalizedPath = when {
                path.isBlank() || path == "/" -> ""
                else -> path.removePrefix("/")
            }
            // 构建两个候选URL：优先带尾斜杠，其次去掉尾斜杠（兼容不同服务器要求）
            val base = server.getFormattedUrl()
            val withSlash = (base + normalizedPath).let { if (it.endsWith("/")) it else "$it/" }
            val withoutSlash = withSlash.removeSuffix("/")

            fun doPropfind(u: String): Result<List<WebDAVFile>> {
                val req = Request.Builder()
                    .url(u)
                    .method("PROPFIND", createPropfindBody())
                    .header("Depth", "1")
                    .header("Content-Type", "application/xml")
                    .build()
                return client?.newCall(req)?.execute()?.use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string() ?: ""
                        val files = parseWebDAVResponse(bodyStr, path, base)
                        Result.success(files)
                    } else {
                        Result.failure(IOException("HTTP ${resp.code}: ${resp.message} @ $u"))
                    }
                } ?: Result.failure(IOException("Client not initialized"))
            }

            // 尝试带斜杠
            val first = doPropfind(withSlash)
            if (first.isSuccess) return@withContext first
            // 若404或失败，再试不带斜杠
            val second = doPropfind(withoutSlash)
            if (second.isSuccess) return@withContext second
            second
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取文件下载URL
     */
    fun getFileUrl(file: WebDAVFile): String? {
        val server = currentServer ?: return null
        return "${server.getFormattedUrl()}${file.path.removePrefix("/")}"
    }
    
    /**
     * 创建PROPFIND请求体
     */
    private fun createPropfindBody(): RequestBody {
        val xml = """<?xml version="1.0" encoding="utf-8" ?>
            <D:propfind xmlns:D="DAV:">
                <D:prop>
                    <D:displayname/>
                    <D:getcontentlength/>
                    <D:getcontenttype/>
                    <D:getlastmodified/>
                    <D:resourcetype/>
                </D:prop>
            </D:propfind>""".trimIndent()
        
        return xml.toRequestBody("application/xml".toMediaType())
    }
    
    /**
     * 解析WebDAV响应（宽松解析，忽略命名空间与大小写）
     */
    private fun parseWebDAVResponse(responseBody: String, currentPath: String, baseUrl: String): List<WebDAVFile> {
        fun normalizeHref(h: String): String {
            try {
                val s = h.trim()
                val uri = java.net.URI(s)
                val rawPath = if (uri.isAbsolute) uri.rawPath ?: uri.path else null
                val candidate = rawPath ?: s
                var href = candidate

                // 从 baseUrl 中提取 path 前缀（例如 /remote.php/dav/files/username/）
                val baseUri = java.net.URI(baseUrl)
                val basePath = baseUri.rawPath ?: baseUri.path ?: ""

                // 去掉完整 baseUrl 前缀或仅 path 前缀
                if (href.startsWith(baseUrl)) href = href.removePrefix(baseUrl)
                if (href.startsWith(basePath)) href = href.removePrefix(basePath)

                if (!href.startsWith("/")) href = "/" + href
                return href
            } catch (e: Exception) {
                var href = h
                val baseUri = try { java.net.URI(baseUrl) } catch (_: Exception) { null }
                val basePath = baseUri?.rawPath ?: baseUri?.path ?: ""
                if (href.startsWith(baseUrl)) href = href.removePrefix(baseUrl)
                if (basePath.isNotEmpty() && href.startsWith(basePath)) href = href.removePrefix(basePath)
                if (!href.startsWith("/")) href = "/" + href
                return href
            }
        }

        fun getTagValue(xml: String, localName: String): String? {
            val regex = Regex("<[^>]*:?" + localName + "\\b[^>]*>(.*?)</[^>]*:?" + localName + ">", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val match = regex.find(xml) ?: return null
            return match.groupValues[1].trim()
        }

        fun containsCollection(xml: String): Boolean {
            // 更稳健：在 <resourcetype> 块内查找 collection（支持自闭合或成对标签，忽略命名空间与大小写）
            val resourceRegex = Regex("<[^>]*:?resourcetype[^>]*>(.*?)</[^>]*:?resourcetype>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val block = resourceRegex.find(xml)?.groupValues?.get(1) ?: xml
            val selfClosing = Regex("<[^>]*:?collection\\s*/>", RegexOption.IGNORE_CASE).containsMatchIn(block)
            val paired = Regex("<[^>]*:?collection[^>]*/?>", RegexOption.IGNORE_CASE).containsMatchIn(block)
            return selfClosing || paired
        }

        val files = mutableListOf<WebDAVFile>()
        try {
            // 匹配每个 <response> ... </response>
            val respRegex = Regex("<[^>]*:?response[^>]*>(.*?)</[^>]*:?response>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            val expectedHref = normalizeHref(baseUrl + currentPath.removePrefix("/"))
            val expectedNorm = if (expectedHref.endsWith("/")) expectedHref else "$expectedHref/"

            respRegex.findAll(responseBody).forEach { m ->
                val response = m.value
                val rawHref = getTagValue(response, "href") ?: return@forEach
                var href = normalizeHref(rawHref)

                // 跳过当前目录
                val hrefNorm = if (href.endsWith("/")) href else "$href/"
                if (hrefNorm == expectedNorm) return@forEach

                val displayName = getTagValue(response, "displayname")
                    ?: href.split("/").lastOrNull { it.isNotEmpty() } ?: return@forEach

                val contentLength = getTagValue(response, "getcontentlength")?.toLongOrNull() ?: 0L
                val contentType = getTagValue(response, "getcontenttype")
                val lastModified = getTagValue(response, "getlastmodified")?.let { parseDate(it) }
                val isDirectory = containsCollection(response)

                files.add(
                    WebDAVFile(
                        name = displayName,
                        path = href,
                        isDirectory = isDirectory,
                        size = contentLength,
                        lastModified = lastModified,
                        mimeType = contentType,
                        displayName = displayName
                    )
                )
            }
        } catch (e: Exception) {
            // 忽略解析异常，返回已收集到的内容
        }

        return files.sortedWith(compareBy<WebDAVFile> { !it.isDirectory }.thenBy { it.name })
    }
    
    /**
     * 从XML中提取值
     */
    private fun extractXmlValue(xml: String, tag: String): String? {
        val startTag = "<$tag>"
        val endTag = "</$tag>"
        val startIndex = xml.indexOf(startTag)
        if (startIndex == -1) return null
        
        val valueStart = startIndex + startTag.length
        val endIndex = xml.indexOf(endTag, valueStart)
        if (endIndex == -1) return null
        
        return xml.substring(valueStart, endIndex).trim()
    }
    
    /**
     * 解析日期
     */
    private fun parseDate(dateString: String): Date? {
        return try {
            val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 断开连接
     */
    fun disconnect() {
        client = null
        currentServer = null
    }
}
