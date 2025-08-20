# TV Series Year Extraction Implementation

## Overview
This implementation adds year extraction logic for TV series metadata scraping to improve TMDB API search accuracy. The system can detect and extract year information from TV series titles in various formats and use it to enhance TMDB search queries.

## Features Implemented

### 1. Year Pattern Detection
The system detects year patterns in TV series titles including:
- **Direct following**: `神话2025` (title directly followed by year)
- **Dot separator**: `神话.2025` (title with dot separator and year)
- **English parentheses**: `神话(2025)` (title with year in English parentheses)
- **Chinese parentheses**: `神话（2025）` (title with year in Chinese parentheses)
- **Spaced parentheses**: `神话 (2025)` (title with spaced parentheses)
- **Square brackets**: `神话[2025]` (title with year in square brackets)

### 2. Component Extraction
- **Clean title extraction**: Removes year information and separators to get clean title (e.g., "神话")
- **Year validation**: Extracts 4-digit years and validates they are within reasonable range (1900-2030)
- **Fallback handling**: Returns original title if no valid year is detected

### 3. TMDB API Integration
- **Enhanced search parameters**: Added `first_air_date_year` parameter to TMDB TV search API
- **Improved accuracy**: Uses extracted year to filter search results by release year
- **Dual language support**: Applies year filtering to both Chinese (zh-CN) and English (en-US) searches

## Code Changes

### 1. TmdbApiService.kt
```kotlin
@GET("search/tv")
suspend fun searchTVShows(
    @Query("api_key") apiKey: String,
    @Query("query") query: String,
    @Query("language") language: String = "zh-CN",
    @Query("page") page: Int = 1,
    @Query("include_adult") includeAdult: Boolean = false,
    @Query("first_air_date_year") firstAirDateYear: Int? = null  // ← Added
): Response<TmdbTVSearchResponse>
```

### 2. TmdbClient.kt - New Method
```kotlin
private fun extractTVYearAndTitle(seriesName: String): Pair<String, Int?> {
    // Generic regex patterns for various year formats
    val yearPatterns = listOf(
        Regex("^(.+?)(19|20)\\d{2}$"),           // Direct: 神话2025
        Regex("^(.+?)\\.(19|20)\\d{2}$"),        // Dot: 神话.2025
        Regex("^(.+?)\\((19|20)\\d{2}\\)$"),     // English parentheses
        Regex("^(.+?)（(19|20)\\d{2}）$"),        // Chinese parentheses
        Regex("^(.+?)\\s*[\\(\\[（]\\s*(19|20)\\d{2}\\s*[\\)\\]）]$") // Various brackets
    )
    
    // Pattern matching and validation logic...
}
```

### 3. TmdbClient.kt - Updated scrapeTVShow Method
```kotlin
suspend fun scrapeTVShow(...): MediaItem? {
    // Extract year and clean title
    val yearExtractionResult = extractTVYearAndTitle(seriesName)
    val cleanTitle = yearExtractionResult.first
    val year = yearExtractionResult.second
    
    // Use clean title for candidate generation
    val candidates = generateTVCandidates(cleanTitle)
    
    // Apply year filter to TMDB searches
    val searchZh = apiService.searchTVShows(
        apiKey = API_KEY,
        query = candidate,
        firstAirDateYear = year  // ← Year filtering
    )
    // ...
}
```

## Implementation Highlights

### Generic Pattern Matching
- Uses dynamic regex patterns instead of hardcoded examples
- Supports multiple year formats and separators
- Validates extracted years are within reasonable range (1900-2030)

### Robust Error Handling
- Falls back gracefully if no year is detected
- Preserves original title when year extraction fails
- Continues normal search flow without year filtering

### Logging and Debugging
- Added detailed logging for year extraction results
- Shows extracted year in candidate generation logs
- Helps with debugging and monitoring search accuracy

## Testing
A comprehensive test suite (`TmdbClientTest.kt`) was created to verify:
- All supported year formats are correctly detected
- Edge cases (boundary years, invalid years, empty strings)
- Complex titles with multiple potential year matches
- Fallback behavior when no year is present

## Benefits
1. **Improved Search Accuracy**: Year filtering reduces false positives in TMDB search results
2. **Better Chinese Content Support**: Particularly effective for Chinese TV series with year information
3. **Flexible Format Support**: Handles various naming conventions used in media libraries
4. **Backward Compatibility**: Maintains existing functionality when no year is detected
5. **Performance**: Reduces API calls by getting more accurate results on first attempt

## Usage Examples
```
Input: "神话2025" → Clean Title: "神话", Year: 2025
Input: "神话.2025" → Clean Title: "神话", Year: 2025  
Input: "神话(2025)" → Clean Title: "神话", Year: 2025
Input: "神话（2025）" → Clean Title: "神话", Year: 2025
Input: "神话" → Clean Title: "神话", Year: null
```

The implementation is now ready for use and should significantly improve the accuracy of TV series metadata scraping, especially for content with year information in the title.
