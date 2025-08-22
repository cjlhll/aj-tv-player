package com.tvplayer.webdav.data.subtitle

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tvplayer.webdav.data.model.*
import com.tvplayer.webdav.data.storage.SubtitleCache
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.io.File

/**
 * 字幕管理器单元测试
 */
@RunWith(AndroidJUnit4::class)
class SubtitleManagerTest {
    
    @Mock
    private lateinit var mockOpenSubtitlesService: OpenSubtitlesService
    
    @Mock
    private lateinit var mockSubtitleCache: SubtitleCache
    
    private lateinit var context: Context
    private lateinit var subtitleManager: SubtitleManager
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        subtitleManager = SubtitleManager(context, mockOpenSubtitlesService, mockSubtitleCache)
    }
    
    @Test
    fun testSearchSubtitlesWithCache() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem()
        val config = SubtitleConfig.getDefault()
        val searchRequest = SubtitleSearchRequest.fromMediaItem(mediaItem, config)
        
        val cachedSubtitles = listOf(
            createTestSubtitle(\"1\", \"测试字幕1\", \"zh-cn\", isDownloaded = true),
            createTestSubtitle(\"2\", \"测试字幕2\", \"en\", isDownloaded = false)
        )
        
        // 模拟缓存返回结果
        `when`(mockSubtitleCache.getSubtitles(anyString())).thenReturn(cachedSubtitles)
        
        // 执行测试
        val result = subtitleManager.searchSubtitles(searchRequest, config)
        
        // 验证结果
        assertTrue(\"应该找到缓存的字幕\", result.hasResults)
        assertEquals(\"应该返回2个字幕\", 2, result.subtitles.size)
        assertEquals(\"来源应该是本地\", SubtitleSource.LOCAL, result.source)
        
        // 验证没有调用在线搜索
        verify(mockOpenSubtitlesService, never()).searchSubtitles(any())
    }
    
    @Test
    fun testSearchSubtitlesOnline() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem()
        val config = SubtitleConfig.getDefault()
        val searchRequest = SubtitleSearchRequest.fromMediaItem(mediaItem, config)
        
        val onlineSubtitles = listOf(
            createTestSubtitle(\"3\", \"在线字幕1\", \"zh-cn\"),
            createTestSubtitle(\"4\", \"在线字幕2\", \"en\")
        )
        
        // 模拟缓存为空
        `when`(mockSubtitleCache.getSubtitles(anyString())).thenReturn(emptyList())
        
        // 模拟在线服务可用并返回结果
        `when`(mockOpenSubtitlesService.isAvailable()).thenReturn(true)
        `when`(mockOpenSubtitlesService.searchSubtitles(any()))
            .thenReturn(Result.success(onlineSubtitles))
        
        // 执行测试
        val result = subtitleManager.searchSubtitles(searchRequest, config)
        
        // 验证结果
        assertTrue(\"应该找到在线字幕\", result.hasResults)
        assertEquals(\"应该返回2个字幕\", 2, result.subtitles.size)
        assertTrue(\"搜索时间应该大于0\", result.searchTime > 0)
        
        // 验证调用了在线搜索
        verify(mockOpenSubtitlesService).searchSubtitles(searchRequest)
        verify(mockSubtitleCache).saveSubtitle(any(), anyString())
    }
    
    @Test
    fun testDownloadSubtitle() = runBlocking {
        // 准备测试数据
        val subtitle = createTestSubtitle(\"5\", \"待下载字幕\", \"zh-cn\", isDownloaded = false)
        val mediaId = \"test_media_123\"
        
        val downloadedSubtitle = subtitle.copy(
            localPath = \"/test/path/subtitle.srt\",
            isDownloaded = true
        )
        
        // 模拟下载成功
        `when`(mockOpenSubtitlesService.downloadSubtitle(any(), anyString()))
            .thenReturn(Result.success(\"/test/path/subtitle.srt\"))
        
        // 执行测试
        val result = subtitleManager.downloadSubtitle(subtitle, mediaId)
        
        // 验证结果
        assertTrue(\"下载应该成功\", result.isSuccess)
        val downloadedResult = result.getOrNull()
        assertNotNull(\"应该返回下载的字幕\", downloadedResult)
        assertTrue(\"字幕应该标记为已下载\", downloadedResult!!.isDownloaded)
        assertFalse(\"本地路径不应该为空\", downloadedResult.localPath.isEmpty())
        
        // 验证调用了保存缓存
        verify(mockSubtitleCache).saveSubtitle(any())
    }
    
    @Test
    fun testSelectBestSubtitle() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem()
        val config = SubtitleConfig.getChineseOptimized()
        
        val availableSubtitles = listOf(
            createTestSubtitle(\"6\", \"英文字幕\", \"en\", rating = 8.0f, isDownloaded = true),
            createTestSubtitle(\"7\", \"中文字幕\", \"zh-cn\", rating = 9.0f, isDownloaded = true),
            createTestSubtitle(\"8\", \"日文字幕\", \"ja\", rating = 7.5f, isDownloaded = true)
        )
        
        // 模拟缓存返回可用字幕
        `when`(mockSubtitleCache.getSubtitles(anyString())).thenReturn(availableSubtitles)
        
        // 执行测试
        val result = subtitleManager.selectBestSubtitle(mediaItem, config)
        
        // 验证结果
        assertNotNull(\"应该选择到最佳字幕\", result)
        assertEquals(\"应该选择中文字幕\", \"zh-cn\", result!!.language)
        assertEquals(\"应该选择评分最高的中文字幕\", \"中文字幕\", result.title)
    }
    
    @Test
    fun testGetAvailableSubtitles() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem()
        val config = SubtitleConfig.getDefault()
        
        val cachedSubtitles = listOf(
            createTestSubtitle(\"9\", \"缓存字幕1\", \"zh-cn\", isDownloaded = true),
            createTestSubtitle(\"10\", \"缓存字幕2\", \"en\", isDownloaded = false) // 未下载，不应该包含
        )
        
        // 模拟缓存返回字幕
        `when`(mockSubtitleCache.getSubtitles(anyString())).thenReturn(cachedSubtitles)
        
        // 执行测试
        val result = subtitleManager.getAvailableSubtitles(mediaItem, config)
        
        // 验证结果
        assertEquals(\"应该只返回已下载的字幕\", 1, result.size)
        assertTrue(\"返回的字幕应该是可用的\", result.first().isAvailable())
        assertEquals(\"应该是中文字幕\", \"zh-cn\", result.first().language)
    }
    
    @Test
    fun testCleanExpiredCache() = runBlocking {
        // 执行测试
        subtitleManager.cleanExpiredCache()
        
        // 验证调用了缓存清理
        verify(mockSubtitleCache).cleanExpiredSubtitles(30)
    }
    
    // 辅助方法
    
    private fun createTestMediaItem(): MediaItem {
        return MediaItem(
            id = \"test_movie_123\",
            title = \"测试电影\",
            fileName = \"test_movie_2023.mp4\",
            filePath = \"/test/path/test_movie_2023.mp4\",
            type = MediaType.MOVIE,
            year = 2023,
            fileSize = 1024 * 1024 * 1024L, // 1GB
            duration = 7200000L, // 2小时
            addedDate = System.currentTimeMillis()
        )
    }
    
    private fun createTestSubtitle(
        id: String,
        title: String,
        language: String,
        rating: Float = 0.0f,
        downloadCount: Int = 100,
        isDownloaded: Boolean = false
    ): Subtitle {
        return Subtitle(
            id = id,
            title = title,
            language = language,
            languageName = when (language) {
                \"zh-cn\" -> \"简体中文\"
                \"en\" -> \"English\"
                \"ja\" -> \"日本語\"
                else -> language.uppercase()
            },
            format = SubtitleFormat.SRT,
            downloadUrl = \"https://example.com/subtitle_$id.srt\",
            localPath = if (isDownloaded) \"/test/cache/subtitle_$id.srt\" else \"\",
            fileSize = 1024L * 50, // 50KB
            source = SubtitleSource.OPENSUBTITLES,
            rating = rating,
            downloadCount = downloadCount,
            uploadDate = System.currentTimeMillis() - 86400000L, // 1天前
            uploader = \"test_user\",
            isDownloaded = isDownloaded
        )
    }
}", "original_text": "", "replace_all": false}]