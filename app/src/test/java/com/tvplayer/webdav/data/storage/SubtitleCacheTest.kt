package com.tvplayer.webdav.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.tvplayer.webdav.data.model.*
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
 * 字幕缓存单元测试
 */
@RunWith(AndroidJUnit4::class)
class SubtitleCacheTest {
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    private lateinit var context: Context
    private lateinit var gson: Gson
    private lateinit var subtitleCache: SubtitleCache
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        gson = Gson()
        
        // 模拟SharedPreferences行为
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        
        subtitleCache = SubtitleCache(context, gson)
    }
    
    @Test
    fun testSaveAndGetSubtitle() = runBlocking {
        // 准备测试数据
        val subtitle = createTestSubtitle(\"test_1\", \"测试字幕\", \"zh-cn\")
        val mediaId = \"test_media_123\"
        
        // 保存字幕
        subtitleCache.saveSubtitle(subtitle, mediaId)
        
        // 获取字幕
        val retrievedSubtitles = subtitleCache.getSubtitles(mediaId)
        
        // 验证结果
        assertEquals(\"应该返回1个字幕\", 1, retrievedSubtitles.size)
        val retrievedSubtitle = retrievedSubtitles.first()
        assertEquals(\"字幕ID应该匹配\", subtitle.id, retrievedSubtitle.id)
        assertEquals(\"字幕标题应该匹配\", subtitle.title, retrievedSubtitle.title)
        assertEquals(\"字幕语言应该匹配\", subtitle.language, retrievedSubtitle.language)
    }
    
    @Test
    fun testGetSubtitleById() = runBlocking {
        // 准备测试数据
        val subtitle = createTestSubtitle(\"test_2\", \"另一个测试字幕\", \"en\")
        
        // 保存字幕
        subtitleCache.saveSubtitle(subtitle)
        
        // 根据ID获取字幕
        val retrievedSubtitle = subtitleCache.getSubtitle(subtitle.id)
        
        // 验证结果
        assertNotNull(\"应该找到字幕\", retrievedSubtitle)
        assertEquals(\"字幕ID应该匹配\", subtitle.id, retrievedSubtitle!!.id)
        assertEquals(\"字幕标题应该匹配\", subtitle.title, retrievedSubtitle.title)
    }
    
    @Test
    fun testSearchSubtitles() = runBlocking {
        // 准备测试数据
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"复仇者联盟\", \"zh-cn\", isDownloaded = true),
            createTestSubtitle(\"2\", \"钢铁侠\", \"zh-cn\", isDownloaded = true),
            createTestSubtitle(\"3\", \"The Avengers\", \"en\", isDownloaded = true),
            createTestSubtitle(\"4\", \"未下载字幕\", \"zh-cn\", isDownloaded = false)
        )
        
        // 保存所有字幕
        subtitles.forEach { subtitle ->
            subtitleCache.saveSubtitle(subtitle)
        }
        
        // 搜索中文字幕
        val chineseResults = subtitleCache.searchSubtitles(\"复仇者\", \"zh\")
        
        // 验证结果
        assertEquals(\"应该找到1个中文字幕\", 1, chineseResults.size)
        assertEquals(\"应该是复仇者联盟\", \"复仇者联盟\", chineseResults.first().title)
        
        // 搜索所有语言
        val allResults = subtitleCache.searchSubtitles(\"联盟\")
        
        // 验证结果（只返回可用的字幕）
        assertEquals(\"应该找到1个字幕\", 1, allResults.size)
        assertTrue(\"返回的字幕应该是可用的\", allResults.first().isAvailable())
    }
    
    @Test
    fun testRemoveSubtitle() = runBlocking {
        // 准备测试数据
        val subtitle = createTestSubtitle(\"test_remove\", \"待删除字幕\", \"zh-cn\")
        val mediaId = \"test_media_remove\"
        
        // 保存字幕
        subtitleCache.saveSubtitle(subtitle, mediaId)
        
        // 验证字幕存在
        assertNotNull(\"字幕应该存在\", subtitleCache.getSubtitle(subtitle.id))
        assertEquals(\"媒体应该有关联字幕\", 1, subtitleCache.getSubtitles(mediaId).size)
        
        // 删除字幕
        subtitleCache.removeSubtitle(subtitle.id)
        
        // 验证字幕已删除
        assertNull(\"字幕应该被删除\", subtitleCache.getSubtitle(subtitle.id))
        assertEquals(\"媒体不应该有关联字幕\", 0, subtitleCache.getSubtitles(mediaId).size)
    }
    
    @Test
    fun testCleanExpiredSubtitles() = runBlocking {
        // 准备测试数据（过期字幕）
        val expiredTime = System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000) // 35天前
        val expiredSubtitle = createTestSubtitle(
            \"expired\", \"过期字幕\", \"zh-cn\",
            uploadDate = expiredTime
        )
        
        val recentSubtitle = createTestSubtitle(
            \"recent\", \"最近字幕\", \"zh-cn\",
            uploadDate = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) // 7天前
        )
        
        // 保存字幕
        subtitleCache.saveSubtitle(expiredSubtitle)
        subtitleCache.saveSubtitle(recentSubtitle)
        
        // 验证字幕存在
        assertNotNull(\"过期字幕应该存在\", subtitleCache.getSubtitle(expiredSubtitle.id))
        assertNotNull(\"最近字幕应该存在\", subtitleCache.getSubtitle(recentSubtitle.id))
        
        // 清理过期字幕
        subtitleCache.cleanExpiredSubtitles(30)
        
        // 验证结果
        assertNull(\"过期字幕应该被删除\", subtitleCache.getSubtitle(expiredSubtitle.id))
        assertNotNull(\"最近字幕应该保留\", subtitleCache.getSubtitle(recentSubtitle.id))
    }
    
    @Test
    fun testGetCacheStats() = runBlocking {
        // 准备测试数据
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"字幕1\", \"zh-cn\", isDownloaded = true),
            createTestSubtitle(\"2\", \"字幕2\", \"en\", isDownloaded = false),
            createTestSubtitle(\"3\", \"字幕3\", \"ja\", isDownloaded = true)
        )
        
        // 保存字幕
        subtitles.forEach { subtitle ->
            subtitleCache.saveSubtitle(subtitle)
        }
        
        // 获取缓存统计
        val stats = subtitleCache.getCacheStats()
        
        // 验证结果
        assertEquals(\"总字幕数应该是3\", 3, stats.totalSubtitles)
        assertEquals(\"已下载字幕数应该是2\", 2, stats.downloadedSubtitles)
        assertTrue(\"缓存大小应该大于等于0\", stats.cacheSize >= 0)
    }
    
    @Test
    fun testClearAllCache() = runBlocking {
        // 准备测试数据
        val subtitle = createTestSubtitle(\"clear_test\", \"清理测试字幕\", \"zh-cn\")
        val mediaId = \"clear_test_media\"
        
        // 保存字幕
        subtitleCache.saveSubtitle(subtitle, mediaId)
        
        // 验证字幕存在
        assertNotNull(\"字幕应该存在\", subtitleCache.getSubtitle(subtitle.id))
        assertEquals(\"媒体应该有关联字幕\", 1, subtitleCache.getSubtitles(mediaId).size)
        
        // 清空缓存
        subtitleCache.clearAllCache()
        
        // 验证缓存已清空
        assertNull(\"字幕应该被清除\", subtitleCache.getSubtitle(subtitle.id))
        assertEquals(\"媒体不应该有关联字幕\", 0, subtitleCache.getSubtitles(mediaId).size)
        
        val stats = subtitleCache.getCacheStats()
        assertEquals(\"总字幕数应该是0\", 0, stats.totalSubtitles)
    }
    
    @Test
    fun testMultipleMediaSubtitles() = runBlocking {
        // 准备测试数据
        val media1Subtitles = listOf(
            createTestSubtitle(\"m1s1\", \"电影1字幕1\", \"zh-cn\"),
            createTestSubtitle(\"m1s2\", \"电影1字幕2\", \"en\")
        )
        
        val media2Subtitles = listOf(
            createTestSubtitle(\"m2s1\", \"电影2字幕1\", \"zh-cn\"),
            createTestSubtitle(\"m2s2\", \"电影2字幕2\", \"ja\")
        )
        
        val media1Id = \"movie_1\"
        val media2Id = \"movie_2\"
        
        // 保存字幕
        media1Subtitles.forEach { subtitle ->
            subtitleCache.saveSubtitle(subtitle, media1Id)
        }
        
        media2Subtitles.forEach { subtitle ->
            subtitleCache.saveSubtitle(subtitle, media2Id)
        }
        
        // 验证每个媒体的字幕
        val movie1Subtitles = subtitleCache.getSubtitles(media1Id)
        val movie2Subtitles = subtitleCache.getSubtitles(media2Id)
        
        assertEquals(\"电影1应该有2个字幕\", 2, movie1Subtitles.size)
        assertEquals(\"电影2应该有2个字幕\", 2, movie2Subtitles.size)
        
        // 验证字幕内容
        val movie1ChineseSubtitle = movie1Subtitles.find { it.language == \"zh-cn\" }
        assertNotNull(\"电影1应该有中文字幕\", movie1ChineseSubtitle)
        assertEquals(\"中文字幕标题应该正确\", \"电影1字幕1\", movie1ChineseSubtitle!!.title)
        
        val movie2JapaneseSubtitle = movie2Subtitles.find { it.language == \"ja\" }
        assertNotNull(\"电影2应该有日文字幕\", movie2JapaneseSubtitle)
        assertEquals(\"日文字幕标题应该正确\", \"电影2字幕2\", movie2JapaneseSubtitle!!.title)
    }
    
    // 辅助方法
    
    private fun createTestSubtitle(
        id: String,
        title: String,
        language: String,
        rating: Float = 0.0f,
        downloadCount: Int = 100,
        isDownloaded: Boolean = true,
        uploadDate: Long = System.currentTimeMillis()
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
            fileSize = 1024L * 50,
            source = SubtitleSource.OPENSUBTITLES,
            rating = rating,
            downloadCount = downloadCount,
            uploadDate = uploadDate,
            uploader = \"test_user\",
            isDownloaded = isDownloaded
        )
    }
}"