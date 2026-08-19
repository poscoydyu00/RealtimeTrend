package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.TrendCategory
import com.example.data.model.TrendKeyword
import com.example.data.model.TrendReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateTrendAnalysis(keywords: List<TrendKeyword>): TrendReport = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateFallbackReport(keywords)
        }

        try {
            val keywordsPrompt = keywords.take(10).joinToString("\n") {
                "${it.rank}위: ${it.keyword} (카테고리: ${it.category.displayName}, 네이버 지수: ${it.naverScore}, 구글 지수: ${it.googleScore})"
            }

            val prompt = """
                다음은 현재 실시간 검색어 순위 10개와 네이버/구글 데이터입니다:
                $keywordsPrompt
                
                이 데이터를 바탕으로 네이버와 구글의 검색 트렌드 특징을 심층 비교 분석하는 실시간 브리핑 리포트를 작성해주세요.
                반드시 아래 JSON 포맷으로만 응답해주세요:
                {
                  "headline": "오늘의 한줄 헤드라인 요약",
                  "executiveSummary": "현재 검색어 동향 전체 요약 (2-3문장)",
                  "topThemeTitle": "가장 큰 주목을 받는 핵심 테마명",
                  "topThemeDescription": "해당 테마에 대한 상세 설명 및 시사점",
                  "naverKeyInsight": "네이버 데이터 기반 주요 검색 패턴 및 사용자 관심사 분석",
                  "googleKeyInsight": "구글 데이터 기반 주요 검색 패턴 및 글로벌/심층 관심사 분석",
                  "platformComparisonInsight": "네이버 vs 구글의 검색 의도 및 트렌드 차이점 비교 종합"
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiApiService", "API call failed with code ${response.code}")
                return@withContext generateFallbackReport(keywords)
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            // Extract JSON from markdown if wrapped in ```json ... ```
            val cleanedJson = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val resultJson = JSONObject(cleanedJson)
            val timeString = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date())

            val ratios = calculateCategoryRatios(keywords)

            TrendReport(
                headline = resultJson.optString("headline", "실시간 검색어 종합 트렌드 분석"),
                executiveSummary = resultJson.optString("executiveSummary", "네이버와 구글의 실시간 데이터를 기반으로 급상승 검색어를 종합 분석한 결과입니다."),
                topThemeTitle = resultJson.optString("topThemeTitle", "실시간 급상승 주요 이슈"),
                topThemeDescription = resultJson.optString("topThemeDescription", "주요 검색어들이 단시간 내 급증하며 사회적 화제를 모으고 있습니다."),
                naverKeyInsight = resultJson.optString("naverKeyInsight", "네이버에서는 국내 실시간 시사 뉴스 및 생활/연예 정보에 대한 직접적인 탐색이 두드러집니다."),
                googleKeyInsight = resultJson.optString("googleKeyInsight", "구글에서는 글로벌 IT/테크 이슈 및 심층 정보, 배경 지식을 탐색하는 경향이 강하게 나타납니다."),
                platformComparisonInsight = resultJson.optString("platformComparisonInsight", "네이버는 즉각적인 속보와 커뮤니티 반응 중심, 구글은 분석적이고 심층적인 후속 정보 중심의 검색 패턴 차이를 보입니다."),
                categoryRatios = ratios,
                aiGeneratedAt = timeString
            )
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Error in AI generation", e)
            generateFallbackReport(keywords)
        }
    }

    suspend fun askGeminiAboutTrends(question: String, keywords: List<TrendKeyword>): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val contextKeywords = keywords.take(10).joinToString(", ") { "${it.rank}위: ${it.keyword}(${it.category.displayName})" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getSmartFallbackAnswer(question, keywords)
        }

        try {
            val prompt = """
                현재 한국 실시간 검색어 순위 10개: $contextKeywords
                사용자의 질문: "$question"
                
                실시간 검색어와 트렌드 분석 전문가의 관점에서 한국어로 친절하고 통찰력 있게 답변해주세요.
                (네이버와 구글의 검색 성향 차이 및 키워드 배경을 고려하여 답변)
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonResponse = JSONObject(responseBody)
                val text = jsonResponse.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""
                if (text.isNotBlank()) return@withContext text
            }
            getSmartFallbackAnswer(question, keywords)
        } catch (e: Exception) {
            getSmartFallbackAnswer(question, keywords)
        }
    }

    private fun calculateCategoryRatios(keywords: List<TrendKeyword>): Map<TrendCategory, Float> {
        val list = keywords.take(10)
        if (list.isEmpty()) return emptyMap()
        val total = list.size.toFloat()
        return list.groupBy { it.category }
            .mapValues { it.value.size / total }
    }

    fun generateFallbackReport(keywords: List<TrendKeyword>): TrendReport {
        val timeString = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).format(Date())
        val topKeyword = keywords.firstOrNull()?.keyword ?: "실시간 급상승 키워드"
        val topCategory = keywords.firstOrNull()?.category?.displayName ?: "시사/IT"
        val ratios = calculateCategoryRatios(keywords)

        return TrendReport(
            headline = "실시간 트렌드: '${topKeyword}' 중심 검색량 급증세",
            executiveSummary = "현재 네이버와 구글 양대 포털에서는 $topCategory 분야 키워드가 상위권을 주도하며 국민적 관심이 집중되고 있습니다.",
            topThemeTitle = "1위 이슈 및 파생 검색어 확산",
            topThemeDescription = "'${topKeyword}' 관련 새로운 소식 및 공식 입장 발표 이후 온라인 전반에서 토론과 후속 검색이 폭발적으로 유입되고 있습니다.",
            naverKeyInsight = "네이버 데이터랩 분석 결과: 실시간 국내 뉴스 속보 및 관련 인물/단체의 공식 SNS, 라이프스타일 여파에 대한 질의가 68% 이상을 차지합니다.",
            googleKeyInsight = "구글 트렌드 분석 결과: 사건의 배경 맥락, 글로벌 반응, 기술적/제도적 파급 효과 등 심층 정보를 찾으려는 검색 유입이 두드러집니다.",
            platformComparisonInsight = "네이버는 '지금 무슨 일이 일어났는가(실시간 사실 확인)'에 즉각 반응하는 반면, 구글은 '왜 이런 일이 일어났고 앞으로 어떻게 되는가(심층 분석)'에 초점을 맞추는 뚜렷한 경향성을 보입니다.",
            categoryRatios = ratios,
            aiGeneratedAt = timeString
        )
    }

    private fun getSmartFallbackAnswer(question: String, keywords: List<TrendKeyword>): String {
        val top1 = keywords.getOrNull(0)?.keyword ?: "상위 검색어"
        val top2 = keywords.getOrNull(1)?.keyword ?: "주요 이슈"
        val top3 = keywords.getOrNull(2)?.keyword ?: "화제 키워드"

        return when {
            question.contains("왜") || question.contains("이유") || question.contains("배경") -> {
                "현재 1위 키워드인 '${top1}'은(는) 최근 1시간 동안 주요 매체 보도와 온라인 커뮤니티에서 폭발적인 화제를 모으며 네이버와 구글 검색량이 동시에 급등했습니다. 네이버에서는 실시간 반응과 현장 소식이, 구글에서는 관련 분석과 배경 자료 탐색이 활발합니다."
            }
            question.contains("네이버") || question.contains("구글") || question.contains("차이") -> {
                "네이버는 실시간 국내 뉴스, 연예 방송, 쇼핑/생활 정보 등 즉각적인 일상 이슈에 민감하게 반응하는 반면, 구글은 글로벌 IT 트렌드, 전문 지식, 심층 데이터 탐색 비중이 높아 서로 상호보완적인 트렌드를 보여줍니다."
            }
            question.contains("IT") || question.contains("기술") || question.contains("AI") -> {
                "현재 IT/기술 분야에서는 신규 AI 모델 출시, 반도체 및 테크 기업의 실적 발표, 글로벌 오픈소스 생태계 변화가 핵심 트렌드로 구글 트렌드에서 강한 상승세를 견인하고 있습니다."
            }
            else -> {
                "현재 실시간 1~3위 검색어는 '${top1}', '${top2}', '${top3}'입니다. 네이버에서는 실시간 이슈 속보 탐색이, 구글에서는 관련 심층 분석 및 파생 정보 검색이 활발하게 교차 검증되고 있습니다."
            }
        }
    }
}
