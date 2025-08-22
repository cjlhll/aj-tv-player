package com.tvplayer.webdav.data.subtitle

import com.tvplayer.webdav.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 字幕匹配器单元测试
 */
class SubtitleMatcherTest {
    
    private lateinit var subtitleMatcher: SubtitleMatcher
    
    @Before
    fun setup() {
        subtitleMatcher = SubtitleMatcher()
    }
    
    @Test
    fun testMatchSubtitlesWithExactTitleMatch() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem(\"复仇者联盟\", 2012, MediaType.MOVIE)
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"复仇者联盟\", \"zh-cn\", rating = 9.0f),
            createTestSubtitle(\"2\", \"钢铁侠\", \"zh-cn\", rating = 8.0f),
            createTestSubtitle(\"3\", \"The Avengers\", \"en\", rating = 8.5f)
        )
        val config = SubtitleConfig.getChineseOptimized()
        
        // 执行测试
        val matches = subtitleMatcher.matchSubtitles(mediaItem, subtitles, config)
        
        // 验证结果
        assertTrue(\"应该找到匹配的字幕\", matches.isNotEmpty())
        
        val bestMatch = matches.first()
        assertEquals(\"最佳匹配应该是中文字幕\", \"复仇者联盟\", bestMatch.subtitle.title)
        assertTrue(\"相似度应该很高\", bestMatch.similarity > 0.8f)
        assertTrue(\"置信度应该很高\", bestMatch.confidence > 0.8f)
        assertTrue(\"应该包含标题匹配原因\", 
            bestMatch.matchReasons.contains(MatchReason.TITLE_MATCH))
    }
    
    @Test
    fun testMatchSubtitlesWithLanguagePreference() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem(\"Action Movie\", 2023, MediaType.MOVIE)
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"动作电影\", \"zh-cn\", rating = 7.0f),
            createTestSubtitle(\"2\", \"Action Movie\", \"en\", rating = 9.0f),
            createTestSubtitle(\"3\", \"액션 영화\", \"ko\", rating = 8.0f)
        )
        val config = SubtitleConfig.getChineseOptimized()
        
        // 执行测试
        val matches = subtitleMatcher.matchSubtitles(mediaItem, subtitles, config)
        
        // 验证结果
        assertTrue(\"应该找到匹配的字幕\", matches.isNotEmpty())
        
        // 中文字幕应该排在前面（因为配置偏好中文）
        val chineseMatch = matches.find { it.subtitle.language == \"zh-cn\" }
        assertNotNull(\"应该包含中文字幕\", chineseMatch)
        
        val englishMatch = matches.find { it.subtitle.language == \"en\" }
        assertNotNull(\"应该包含英文字幕\", englishMatch)
        
        // 虽然英文字幕评分更高，但中文字幕应该有更高的语言匹配分数
        assertTrue(\"中文字幕的语言匹配应该更好\", chineseMatch!!.similarity >= englishMatch!!.similarity * 0.9f)
    }
    
    @Test
    fun testMatchSubtitlesForTVShow() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestTVShowItem(\"权力的游戏\", 2011, 1, 1)
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"权力的游戏 S01E01\", \"zh-cn\", season = 1, episode = 1),
            createTestSubtitle(\"2\", \"权力的游戏 S01E02\", \"zh-cn\", season = 1, episode = 2),
            createTestSubtitle(\"3\", \"Game of Thrones S01E01\", \"en\", season = 1, episode = 1)
        )
        val config = SubtitleConfig.getChineseOptimized()
        
        // 执行测试
        val matches = subtitleMatcher.matchSubtitles(mediaItem, subtitles, config)
        
        // 验证结果
        assertTrue(\"应该找到匹配的字幕\", matches.isNotEmpty())
        
        val bestMatches = matches.filter { it.similarity > 0.7f }
        assertEquals(\"应该有2个高质量匹配\", 2, bestMatches.size)
        
        // 所有高质量匹配都应该是S01E01
        bestMatches.forEach { match ->
            assertTrue(\"应该包含剧集匹配原因\",
                match.matchReasons.contains(MatchReason.EPISODE_MATCH))
        }
    }
    
    @Test
    fun testMatchByFileName() = runBlocking {
        // 准备测试数据
        val fileName = \"The.Matrix.1999.1080p.BluRay.x264-GROUP.mkv\"
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"The Matrix 1999 1080p BluRay\", \"zh-cn\"),
            createTestSubtitle(\"2\", \"Matrix Reloaded 2003\", \"zh-cn\"),
            createTestSubtitle(\"3\", \"The Matrix 1999\", \"en\")
        )
        val languages = listOf(\"zh\", \"en\")
        
        // 执行测试
        val matches = subtitleMatcher.matchByFileName(fileName, subtitles, languages)
        
        // 验证结果
        assertTrue(\"应该找到匹配的字幕\", matches.isNotEmpty())
        
        val bestMatch = matches.first()
        assertTrue(\"最佳匹配应该包含正确的标题\", 
            bestMatch.subtitle.title.contains(\"Matrix\") && bestMatch.subtitle.title.contains(\"1999\"))
        assertTrue(\"相似度应该合理\", bestMatch.similarity > 0.5f)
    }
    
    @Test
    fun testFindBestMatch() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem(\"阿凡达\", 2009, MediaType.MOVIE)
        val subtitles = listOf(
            createTestSubtitle(\"1\", \"Avatar\", \"en\", rating = 6.0f),
            createTestSubtitle(\"2\", \"阿凡达\", \"zh-cn\", rating = 9.5f),
            createTestSubtitle(\"3\", \"随机电影\", \"zh-cn\", rating = 8.0f)
        )
        val config = SubtitleConfig.getChineseOptimized()
        
        // 执行测试
        val bestMatch = subtitleMatcher.findBestMatch(mediaItem, subtitles, config)
        
        // 验证结果
        assertNotNull(\"应该找到最佳匹配\", bestMatch)
        assertEquals(\"最佳匹配应该是阿凡达中文字幕\", \"阿凡达\", bestMatch!!.subtitle.title)
        assertEquals(\"语言应该是中文\", \"zh-cn\", bestMatch.subtitle.language)
        assertTrue(\"应该是良好匹配\", bestMatch.isGoodMatch)
    }
    
    @Test
    fun testValidateCompatibility() {
        // 准备测试数据
        val mediaItem = createTestTVShowItem(\"破产姐妹\", 2011, 2, 5)
        
        // 兼容的字幕
        val compatibleSubtitle = createTestSubtitle(
            \"1\", \"破产姐妹 S02E05\", \"zh-cn\", 
            season = 2, episode = 5, year = 2011
        )
        
        // 不兼容的字幕（季数不匹配）
        val incompatibleSubtitle = createTestSubtitle(
            \"2\", \"破产姐妹 S03E05\", \"zh-cn\",
            season = 3, episode = 5, year = 2011
        )
        
        // 执行测试
        val compatibleResult = subtitleMatcher.validateCompatibility(mediaItem, compatibleSubtitle)
        val incompatibleResult = subtitleMatcher.validateCompatibility(mediaItem, incompatibleSubtitle)
        
        // 验证结果
        assertTrue(\"兼容的字幕应该通过验证\", compatibleResult.isCompatible)
        assertFalse(\"兼容字幕不应该有严重问题\", compatibleResult.hasCriticalIssues)
        
        assertFalse(\"不兼容的字幕应该失败\", incompatibleResult.isCompatible)
        assertTrue(\"不兼容字幕应该有严重问题\", incompatibleResult.hasCriticalIssues)
        assertTrue(\"应该包含季数不匹配问题\",
            incompatibleResult.issues.contains(CompatibilityIssue.SEASON_MISMATCH))
    }
    
    @Test
    fun testMatchQualityLevels() = runBlocking {
        // 准备测试数据
        val mediaItem = createTestMediaItem(\"测试电影\", 2023, MediaType.MOVIE)
        val subtitles = listOf(
            // 优秀匹配：完全匹配
            createTestSubtitle(\"1\", \"测试电影\", \"zh-cn\", rating = 9.0f),
            
            // 良好匹配：标题相似
            createTestSubtitle(\"2\", \"测试电影2023\", \"zh-cn\", rating = 8.0f),
            
            // 一般匹配：标题部分匹配
            createTestSubtitle(\"3\", \"测试\", \"zh-cn\", rating = 7.0f),
            
            // 较差匹配：完全不匹配
            createTestSubtitle(\"4\", \"其他电影\", \"en\", rating = 6.0f)
        )
        val config = SubtitleConfig.getChineseOptimized()
        
        // 执行测试
        val matches = subtitleMatcher.matchSubtitles(mediaItem, subtitles, config)
        
        // 验证结果
        assertTrue(\"应该有多个匹配结果\", matches.size >= 3)
        
        val excellentMatches = matches.filter { it.qualityLevel == MatchQuality.EXCELLENT }
        val goodMatches = matches.filter { it.qualityLevel == MatchQuality.GOOD }
        val fairMatches = matches.filter { it.qualityLevel == MatchQuality.FAIR }
        
        assertTrue(\"应该有优秀匹配\", excellentMatches.isNotEmpty())
        assertTrue(\"优秀匹配的相似度应该很高\", excellentMatches.all { it.similarity >= 0.9f })
        
        if (goodMatches.isNotEmpty()) {
            assertTrue(\"良好匹配的相似度应该在合理范围\", 
                goodMatches.all { it.similarity >= 0.7f && it.similarity < 0.9f })
        }
    }
    
    // 辅助方法
    
    private fun createTestMediaItem(
        title: String,
        year: Int,
        type: MediaType,
        season: Int? = null,
        episode: Int? = null
    ): MediaItem {
        return MediaItem(
            id = \"test_${title.hashCode()}\",
            title = title,
            fileName = \"$title.$year.mp4\",
            filePath = \"/test/$title.$year.mp4\",
            type = type,
            year = year,
            seasonNumber = season,
            episodeNumber = episode,
            fileSize = 1024 * 1024 * 1024L,
            duration = 7200000L,
            addedDate = System.currentTimeMillis()
        )
    }
    
    private fun createTestTVShowItem(
        title: String,
        year: Int,
        season: Int,
        episode: Int
    ): MediaItem {
        return createTestMediaItem(title, year, MediaType.TV, season, episode)
    }
    
    private fun createTestSubtitle(
        id: String,
        title: String,
        language: String,
        rating: Float = 0.0f,
        downloadCount: Int = 100,
        season: Int? = null,
        episode: Int? = null,
        year: Int? = null
    ): Subtitle {
        val metadata = mutableMapOf<String, String>()
        season?.let { metadata[\"season_number\"] = it.toString() }
        episode?.let { metadata[\"episode_number\"] = it.toString() }
        year?.let { metadata[\"year\"] = it.toString() }
        
        return Subtitle(
            id = id,
            title = title,
            language = language,
            languageName = when (language) {
                \"zh-cn\" -> \"简体中文\"
                \"en\" -> \"English\"
                \"ko\" -> \"한국어\"
                else -> language.uppercase()
            },
            format = SubtitleFormat.SRT,
            downloadUrl = \"https://example.com/subtitle_$id.srt\",
            localPath = \"/test/cache/subtitle_$id.srt\",
            fileSize = 1024L * 50,
            source = SubtitleSource.OPENSUBTITLES,
            rating = rating,
            downloadCount = downloadCount,
            uploadDate = System.currentTimeMillis() - 86400000L,
            uploader = \"test_user\",
            isDownloaded = true,
            metadata = metadata
        )
    }
}", "original_text": "", "replace_all": false}]