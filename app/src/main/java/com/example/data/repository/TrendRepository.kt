package com.example.data.repository

import android.content.Context
import com.example.data.model.KeywordDetailAnalysis
import com.example.data.model.RankChangeType
import com.example.data.model.SentimentInfo
import com.example.data.model.TrendCategory
import com.example.data.model.TrendKeyword
import com.example.data.model.TrendPlatform
import com.example.data.model.TrendReport
import com.example.data.model.WidgetSettings
import com.example.data.remote.GeminiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class TrendRepository(private val context: Context) {

    private val geminiService = GeminiApiService()

    private val _keywordsState = MutableStateFlow<List<TrendKeyword>>(emptyList())
    val keywordsState: StateFlow<List<TrendKeyword>> = _keywordsState.asStateFlow()

    private val _trendReportState = MutableStateFlow<TrendReport?>(null)
    val trendReportState: StateFlow<TrendReport?> = _trendReportState.asStateFlow()

    private val _bookmarkedKeywords = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedKeywords: StateFlow<Set<String>> = _bookmarkedKeywords.asStateFlow()

    private val _widgetSettings = MutableStateFlow(WidgetSettings())
    val widgetSettings: StateFlow<WidgetSettings> = _widgetSettings.asStateFlow()

    init {
        // Load initial dynamic trend dataset
        refreshTrends(forceAiRefresh = false)
    }

    private val initialKeywordTemplates = listOf(
        TrendSeed("Gemini 2.5 Flash", TrendCategory.TECH, 98, 99, "Google I/O 신규 멀티모달 AI 모델 출시 및 성능 지표 공개", listOf("Google AI", "인공지능", "LLM", "API 키")),
        TrendSeed("삼성전자 실적 발표", TrendCategory.ECONOMY, 96, 92, "HBM4 차세대 메모리 양산 로드맵 및 분기 매출 호조", listOf("반도체", "HBM", "코스피", "SK하이닉스")),
        TrendSeed("손흥민 환상 프리킥 골", TrendCategory.SPORTS, 94, 95, "토트넘 극적 역전승 견인 경기 하이라이트 급상승", listOf("토트넘", "프리미어리그", "축구", "하이라이트")),
        TrendSeed("기상청 전국 폭염 특보", TrendCategory.SOCIETY, 95, 88, "체감온도 35도 이상 지속에 따른 온열질환 주의 및 대책", listOf("날씨", "소나기", "태풍", "열대야")),
        TrendSeed("신작 SF 대작 영화 개봉", TrendCategory.ENTERTAINMENT, 91, 93, "예매율 1위 달성 및 해외 평단 극찬 리뷰 쇄도", listOf("영화 순위", "쿠키 영상", "평점", "CGV")),
        TrendSeed("한국은행 기준금리 발표", TrendCategory.ECONOMY, 89, 90, "금융통화위원회 통화정책 방향 및 대출금리 영향 분석", listOf("금리", "환율", "부동산", "물가")),
        TrendSeed("K-POP 빌보드 핫100 1위", TrendCategory.ENTERTAINMENT, 93, 97, "신곡 글로벌 음원 차트 석권 및 뮤직비디오 1억 뷰 돌파", listOf("뮤직비디오", "음원차트", "아이돌", "빌보드")),
        TrendSeed("안드로이드 16 개발자 프리뷰", TrendCategory.TECH, 86, 94, "새로운 Material Design 및 AI 온디바이스 기능 탑재", listOf("구글 픽셀", "Jetpack Compose", "코틀린", "앱 개발")),
        TrendSeed("프로야구 가을야구 순위 싸움", TrendCategory.SPORTS, 90, 85, "1위부터 5위까지 치열한 반 경기 차 대혈투", listOf("KBO", "야구 하이라이트", "순위표", "선발투수")),
        TrendSeed("MZ 인기 디저트 팝업스토어", TrendCategory.LIFE, 92, 82, "성수동 오픈런 대기 4시간 돌파 SNS 핫플레이스", listOf("성수 팝업", "디저트 맛집", "인스타 핫플", "웨이팅")),
        TrendSeed("전기차 보조금 개편안", TrendCategory.SOCIETY, 88, 86, "하반기 친환경차 구매 혜택 및 충전 인프라 지원 확대", listOf("테슬라", "현대차", "전기차 충전소", "환경부")),
        TrendSeed("양자컴퓨터 신기술 돌파구", TrendCategory.TECH, 82, 91, "초전도 큐비트 오류 정정 성공 글로벌 학술지 발표", listOf("양자역학", "IBM", "미래기술", "슈퍼컴퓨터"))
    )

    fun refreshTrends(forceAiRefresh: Boolean = false): List<TrendKeyword> {
        val currentBookmarks = _bookmarkedKeywords.value
        val shuffled = initialKeywordTemplates.shuffled(Random(System.currentTimeMillis() / 60000))

        val list = shuffled.take(10).mapIndexed { index, seed ->
            val rank = index + 1
            val naverRandom = (seed.baseNaver + Random.nextInt(-3, 4)).coerceIn(70, 99)
            val googleRandom = (seed.baseGoogle + Random.nextInt(-3, 4)).coerceIn(70, 99)
            val combined = ((naverRandom * 0.5) + (googleRandom * 0.5)).toInt()

            val changeType = when {
                index == 0 -> RankChangeType.UP
                index == 1 -> RankChangeType.SAME
                index == 2 -> RankChangeType.NEW
                index % 3 == 0 -> RankChangeType.UP
                index % 3 == 1 -> RankChangeType.DOWN
                else -> RankChangeType.SAME
            }
            val changeAmount = when (changeType) {
                RankChangeType.UP -> Random.nextInt(1, 4)
                RankChangeType.DOWN -> Random.nextInt(1, 3)
                else -> 0
            }

            val searchVolumeNum = (combined * 1250) + Random.nextInt(500, 3000)
            val searchVolumeFormatted = "약 %,d회/시간".format(searchVolumeNum)

            val sentimentPos = (40..75).random()
            val sentimentNeg = (5..25).random()
            val sentimentNeu = 100 - sentimentPos - sentimentNeg

            TrendKeyword(
                rank = rank,
                keyword = seed.keyword,
                category = seed.category,
                changeType = changeType,
                changeAmount = changeAmount,
                naverScore = naverRandom,
                googleScore = googleRandom,
                combinedScore = combined,
                searchVolumeFormatted = searchVolumeFormatted,
                summary = seed.summary,
                relatedKeywords = seed.related,
                sentiment = SentimentInfo(
                    positive = sentimentPos,
                    neutral = sentimentNeu,
                    negative = sentimentNeg,
                    summary = "온라인 긍정 반응 ${sentimentPos}% • 정보 탐색 활발"
                ),
                naverSpecificReason = "네이버 블로그/카페/뉴스 검색 유입 급증 (국내 실시간 반응 집중)",
                googleSpecificReason = "구글 검색/유튜브 영상/전문 아티클 조회수 급증 (심층 정보 탐색)",
                isBookmarked = currentBookmarks.contains(seed.keyword),
                updatedAt = System.currentTimeMillis()
            )
        }

        _keywordsState.value = list

        if (_trendReportState.value == null || forceAiRefresh) {
            _trendReportState.value = geminiService.generateFallbackReport(list)
        }

        return list
    }

    suspend fun requestAiTrendReport() {
        val keywords = _keywordsState.value
        if (keywords.isNotEmpty()) {
            val report = geminiService.generateTrendAnalysis(keywords)
            _trendReportState.value = report
        }
    }

    suspend fun askAiAboutTrends(question: String): String {
        return geminiService.askGeminiAboutTrends(question, _keywordsState.value)
    }

    fun toggleBookmark(keyword: String) {
        val current = _bookmarkedKeywords.value.toMutableSet()
        if (current.contains(keyword)) {
            current.remove(keyword)
        } else {
            current.add(keyword)
        }
        _bookmarkedKeywords.value = current

        // Update keyword list bookmark state
        _keywordsState.value = _keywordsState.value.map {
            if (it.keyword == keyword) it.copy(isBookmarked = current.contains(keyword)) else it
        }
    }

    fun updateWidgetSettings(settings: WidgetSettings) {
        _widgetSettings.value = settings
    }

    fun getKeywordsForPlatform(platform: TrendPlatform): List<TrendKeyword> {
        val all = _keywordsState.value
        return when (platform) {
            TrendPlatform.ALL -> all
            TrendPlatform.NAVER -> all.sortedByDescending { it.naverScore }.mapIndexed { index, k -> k.copy(rank = index + 1) }
            TrendPlatform.GOOGLE -> all.sortedByDescending { it.googleScore }.mapIndexed { index, k -> k.copy(rank = index + 1) }
        }
    }

    fun getKeywordDetail(keywordName: String): KeywordDetailAnalysis {
        val item = _keywordsState.value.firstOrNull { it.keyword == keywordName }
            ?: _keywordsState.value.firstOrNull()
            ?: TrendKeyword(
                rank = 1,
                keyword = keywordName,
                category = TrendCategory.TECH,
                changeType = RankChangeType.UP,
                changeAmount = 1,
                naverScore = 95,
                googleScore = 96,
                combinedScore = 96,
                searchVolumeFormatted = "약 120,000회/시간",
                summary = "현재 포털 검색량 급상승 키워드",
                relatedKeywords = listOf("실시간 반응", "관련 뉴스", "소셜 트렌드"),
                sentiment = SentimentInfo(60, 30, 10, "전반적 호의적 반응 우세")
            )

        val encoded = try { URLEncoder.encode(item.keyword, "UTF-8") } catch (e: Exception) { item.keyword }
        val timeNow = SimpleDateFormat("HH:mm", Locale.KOREA).format(Date())

        return KeywordDetailAnalysis(
            keyword = item.keyword,
            rank = item.rank,
            category = item.category,
            backgroundReason = "${item.summary} 이후 온라인 커뮤니티와 주요 미디어에서 관련 키워드 검색량이 300% 이상 폭증하였습니다.",
            naverGoogleComparison = "네이버(지수 ${item.naverScore}점)에서는 실시간 이용자들의 감상평과 최신 기사 확인이 주를 이루고 있으며, 구글(지수 ${item.googleScore}점)에서는 관련 기술/인물 정보 및 유튜브 하이라이트 영상 시청 유입이 두드러집니다.",
            searchIntent = when (item.category) {
                TrendCategory.TECH -> "기술 스펙 확인 및 벤치마크 테스트 결과 탐색"
                TrendCategory.ECONOMY -> "투자 및 시장 전망, 주가 영향 분석"
                TrendCategory.SPORTS -> "경기 결과, 명장면 클립 및 인터뷰 영상 확인"
                TrendCategory.ENTERTAINMENT -> "출연진 정보, 비하인드 스토리 및 음원/영상 감상"
                else -> "실시간 속보 확인 및 여론 반응 확인"
            },
            timeline = listOf(
                "1시간 전" to "주요 포털 첫 보도 및 실시간 유입 시작",
                "30분 전" to "네이버 데이터랩 급상승 10위권 진입",
                "10분 전" to "구글 트렌드 검색량 최고점(피크) 도달",
                timeNow to "현재 양대 포털 종합 1위 등극 및 후속 검색 확산"
            ),
            keyQuotes = listOf(
                "\"오늘 하루 가장 뜨거운 화제의 중심\"",
                "\"네이버와 구글 검색량 모두에서 90점 이상의 높은 화제성 기록\"",
                "\"관련 파생 키워드 검색량도 동반 상승 중\""
            ),
            relatedTopics = item.relatedKeywords + listOf("${item.keyword} 최신 소식", "${item.keyword} 반응", "${item.keyword} 총정리"),
            actionQueryUrlNaver = "https://m.search.naver.com/search.naver?query=$encoded",
            actionQueryUrlGoogle = "https://www.google.com/search?q=$encoded",
            actionQueryUrlYoutube = "https://www.youtube.com/results?search_query=$encoded"
        )
    }

    private data class TrendSeed(
        val keyword: String,
        val category: TrendCategory,
        val baseNaver: Int,
        val baseGoogle: Int,
        val summary: String,
        val related: List<String>
    )
}
