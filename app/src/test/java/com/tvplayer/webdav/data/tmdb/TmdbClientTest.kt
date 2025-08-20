package com.tvplayer.webdav.data.tmdb

import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mockito.*
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Method

/**
 * 测试 TmdbClient 的年份提取逻辑
 */
class TmdbClientTest {

    @Test
    fun testExtractTVYearAndTitle() {
        // 创建 TmdbClient 实例用于测试
        val mockApiService = mock(TmdbApiService::class.java)
        val tmdbClient = TmdbClient(mockApiService)
        
        // 使用反射访问私有方法
        val method: Method = TmdbClient::class.java.getDeclaredMethod("extractTVYearAndTitle", String::class.java)
        method.isAccessible = true

        // 测试用例：直接跟随年份
        var result = method.invoke(tmdbClient, "神话2025") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：点分隔年份
        result = method.invoke(tmdbClient, "神话.2025") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：英文括号年份
        result = method.invoke(tmdbClient, "神话(2025)") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：中文括号年份
        result = method.invoke(tmdbClient, "神话（2025）") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：带空格的括号年份
        result = method.invoke(tmdbClient, "神话 (2025)") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：方括号年份
        result = method.invoke(tmdbClient, "神话[2025]") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)

        // 测试用例：没有年份
        result = method.invoke(tmdbClient, "神话") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertNull(result.second)

        // 测试用例：无效年份（超出范围）
        result = method.invoke(tmdbClient, "神话1800") as Pair<String, Int?>
        assertEquals("神话1800", result.first)
        assertNull(result.second)

        // 测试用例：无效年份（超出范围）
        result = method.invoke(tmdbClient, "神话2050") as Pair<String, Int?>
        assertEquals("神话2050", result.first)
        assertNull(result.second)

        // 测试用例：复杂标题
        result = method.invoke(tmdbClient, "权力的游戏.Game.of.Thrones.2011") as Pair<String, Int?>
        assertEquals("权力的游戏.Game.of.Thrones", result.first)
        assertEquals(2011, result.second)

        // 测试用例：英文标题
        result = method.invoke(tmdbClient, "Breaking Bad (2008)") as Pair<String, Int?>
        assertEquals("Breaking Bad", result.first)
        assertEquals(2008, result.second)
    }

    @Test
    fun testYearExtractionEdgeCases() {
        val mockApiService = mock(TmdbApiService::class.java)
        val tmdbClient = TmdbClient(mockApiService)
        
        val method: Method = TmdbClient::class.java.getDeclaredMethod("extractTVYearAndTitle", String::class.java)
        method.isAccessible = true

        // 测试边界年份
        var result = method.invoke(tmdbClient, "老剧1900") as Pair<String, Int?>
        assertEquals("老剧", result.first)
        assertEquals(1900, result.second)

        result = method.invoke(tmdbClient, "未来剧2030") as Pair<String, Int?>
        assertEquals("未来剧", result.first)
        assertEquals(2030, result.second)

        // 测试空字符串
        result = method.invoke(tmdbClient, "") as Pair<String, Int?>
        assertEquals("", result.first)
        assertNull(result.second)

        // 测试只有年份
        result = method.invoke(tmdbClient, "2025") as Pair<String, Int?>
        assertEquals("2025", result.first)
        assertNull(result.second)

        // 测试多个年份（应该匹配第一个模式）
        result = method.invoke(tmdbClient, "神话2025(2024)") as Pair<String, Int?>
        assertEquals("神话", result.first)
        assertEquals(2025, result.second)
    }
}
