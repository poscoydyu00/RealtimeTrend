package com.example.data.model

enum class RankChangeType {
    UP,
    DOWN,
    NEW,
    SAME
}

enum class TrendPlatform(val displayName: String, val brandColor: Long) {
    ALL("통합 트렌드", 0xFF6366F1),
    NAVER("네이버 데이터랩", 0xFF03C75A),
    GOOGLE("구글 트렌드", 0xFF4285F4)
}

enum class TrendCategory(val displayName: String, val iconEmoji: String) {
    ALL("전체", "🔥"),
    SOCIETY("사회/뉴스", "📰"),
    TECH("IT/과학", "💻"),
    ENTERTAINMENT("연예/문화", "🎬"),
    ECONOMY("경제/증시", "📈"),
    SPORTS("스포츠", "⚽"),
    LIFE("라이프/트렌드", "✨")
}

data class SentimentInfo(
    val positive: Int,
    val neutral: Int,
    val negative: Int,
    val summary: String
)

data class TrendKeyword(
    val rank: Int,
    val keyword: String,
    val category: TrendCategory,
    val changeType: RankChangeType,
    val changeAmount: Int = 0,
    val naverScore: Int, // 0..100
    val googleScore: Int, // 0..100
    val combinedScore: Int, // 0..100
    val searchVolumeFormatted: String,
    val summary: String,
    val relatedKeywords: List<String>,
    val sentiment: SentimentInfo,
    val peakTime: String = "최근 1시간 내 급상승",
    val naverSpecificReason: String = "",
    val googleSpecificReason: String = "",
    val isBookmarked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class TrendReport(
    val timestamp: Long = System.currentTimeMillis(),
    val headline: String,
    val executiveSummary: String,
    val topThemeTitle: String,
    val topThemeDescription: String,
    val naverKeyInsight: String,
    val googleKeyInsight: String,
    val platformComparisonInsight: String,
    val categoryRatios: Map<TrendCategory, Float>,
    val aiGeneratedAt: String
)

data class KeywordDetailAnalysis(
    val keyword: String,
    val rank: Int,
    val category: TrendCategory,
    val backgroundReason: String,
    val naverGoogleComparison: String,
    val searchIntent: String,
    val timeline: List<Pair<String, String>>, // time to event description
    val keyQuotes: List<String>,
    val relatedTopics: List<String>,
    val actionQueryUrlNaver: String,
    val actionQueryUrlGoogle: String,
    val actionQueryUrlYoutube: String
)

data class WidgetSettings(
    val themeMode: WidgetThemeMode = WidgetThemeMode.DARK_GLASS,
    val targetPlatform: TrendPlatform = TrendPlatform.ALL,
    val refreshIntervalMinutes: Int = 5,
    val showCategoryBadge: Boolean = true,
    val showHeatScore: Boolean = true
)

enum class WidgetThemeMode(val title: String) {
    DARK_GLASS("다크 글래스"),
    CLEAN_LIGHT("클린 화이트"),
    CYBER_NEON("사이버 네온"),
    NAVER_GREEN("네이버 그린"),
    GOOGLE_BLUE("구글 블루")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
