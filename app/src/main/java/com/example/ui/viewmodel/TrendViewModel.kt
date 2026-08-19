package com.example.ui.viewmodel

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.KeywordDetailAnalysis
import com.example.data.model.TrendCategory
import com.example.data.model.TrendKeyword
import com.example.data.model.TrendPlatform
import com.example.data.model.TrendReport
import com.example.data.model.WidgetSettings
import com.example.data.repository.TrendRepository
import com.example.widget.RealtimeTrendWidgetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrendViewModel(private val repository: TrendRepository) : ViewModel() {

    private val _selectedPlatform = MutableStateFlow(TrendPlatform.ALL)
    val selectedPlatform: StateFlow<TrendPlatform> = _selectedPlatform.asStateFlow()

    private val _selectedCategory = MutableStateFlow(TrendCategory.ALL)
    val selectedCategory: StateFlow<TrendCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    private val _autoRefreshSeconds = MutableStateFlow(30)
    val autoRefreshSeconds: StateFlow<Int> = _autoRefreshSeconds.asStateFlow()

    private val _isAutoRefreshActive = MutableStateFlow(true)
    val isAutoRefreshActive: StateFlow<Boolean> = _isAutoRefreshActive.asStateFlow()

    private val _selectedKeywordDetail = MutableStateFlow<KeywordDetailAnalysis?>(null)
    val selectedKeywordDetail: StateFlow<KeywordDetailAnalysis?> = _selectedKeywordDetail.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                isUser = false,
                text = "안녕하세요! 네이버 & 구글 실시간 검색 트렌드 AI 어시스턴트입니다. 특정 키워드가 왜 급상승했는지, 양대 포털의 반응 차이나 오늘 가장 화제인 분야를 물어보세요!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAskingAi = MutableStateFlow(false)
    val isAskingAi: StateFlow<Boolean> = _isAskingAi.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val trendReport: StateFlow<TrendReport?> = repository.trendReportState
    val widgetSettings: StateFlow<WidgetSettings> = repository.widgetSettings
    val bookmarkedKeywords: StateFlow<Set<String>> = repository.bookmarkedKeywords

    // Filtered list based on platform, category, and search query
    val filteredKeywords: StateFlow<List<TrendKeyword>> = combine(
        repository.keywordsState,
        _selectedPlatform,
        _selectedCategory,
        _searchQuery
    ) { allKeywords, platform, category, query ->
        val platformSorted = when (platform) {
            TrendPlatform.ALL -> allKeywords
            TrendPlatform.NAVER -> allKeywords.sortedByDescending { it.naverScore }.mapIndexed { idx, k -> k.copy(rank = idx + 1) }
            TrendPlatform.GOOGLE -> allKeywords.sortedByDescending { it.googleScore }.mapIndexed { idx, k -> k.copy(rank = idx + 1) }
        }

        platformSorted.filter { item ->
            val matchCategory = category == TrendCategory.ALL || item.category == category
            val matchQuery = query.isBlank() || item.keyword.contains(query, ignoreCase = true) || item.summary.contains(query, ignoreCase = true)
            matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var countdownJob: Job? = null

    init {
        startAutoRefreshTimer()
    }

    private fun startAutoRefreshTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                if (_isAutoRefreshActive.value) {
                    if (_autoRefreshSeconds.value <= 1) {
                        _autoRefreshSeconds.value = 30
                        refreshTrends(isSilent = true)
                    } else {
                        _autoRefreshSeconds.value -= 1
                    }
                }
                delay(1000)
            }
        }
    }

    fun toggleAutoRefresh() {
        _isAutoRefreshActive.value = !_isAutoRefreshActive.value
        _snackbarMessage.value = if (_isAutoRefreshActive.value) "실시간 자동 갱신이 활성화되었습니다." else "자동 갱신이 일시 정지되었습니다."
    }

    fun setPlatform(platform: TrendPlatform) {
        _selectedPlatform.value = platform
    }

    fun setCategory(category: TrendCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshTrends(isSilent: Boolean = false, context: Context? = null) {
        viewModelScope.launch {
            if (!isSilent) _isRefreshing.value = true
            _autoRefreshSeconds.value = 30
            val updated = repository.refreshTrends(forceAiRefresh = false)
            context?.let {
                RealtimeTrendWidgetProvider.updateAllWidgets(it, updated)
            }
            if (!isSilent) {
                delay(400)
                _isRefreshing.value = false
                _snackbarMessage.value = "최신 실시간 검색어와 트렌드 지수가 갱신되었습니다."
            }
        }
    }

    fun requestAiDeepReport() {
        viewModelScope.launch {
            _isAiAnalyzing.value = true
            try {
                repository.requestAiTrendReport()
                _snackbarMessage.value = "Gemini AI 실시간 트렌드 분석 리포트가 생성되었습니다."
            } catch (e: Exception) {
                _snackbarMessage.value = "AI 리포트 생성 중 오류가 발생했습니다."
            } finally {
                _isAiAnalyzing.value = false
            }
        }
    }

    fun selectKeywordForDetail(keywordName: String) {
        _selectedKeywordDetail.value = repository.getKeywordDetail(keywordName)
    }

    fun clearKeywordDetail() {
        _selectedKeywordDetail.value = null
    }

    fun toggleBookmark(keywordName: String) {
        repository.toggleBookmark(keywordName)
        val isNowBookmarked = repository.bookmarkedKeywords.value.contains(keywordName)
        _snackbarMessage.value = if (isNowBookmarked) "'$keywordName' 관심 키워드로 등록되었습니다." else "'$keywordName' 관심 키워드에서 해제되었습니다."
    }

    fun askAi(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = question)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAskingAi.value = true

        viewModelScope.launch {
            try {
                val answer = repository.askAiAboutTrends(question)
                val aiMsg = ChatMessage(isUser = false, text = answer)
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = "답변 생성 중 일시적인 문제가 발생했습니다. 다시 시도해주세요.")
            } finally {
                _isAskingAi.value = false
            }
        }
    }

    fun updateWidgetSettings(settings: WidgetSettings, context: Context) {
        repository.updateWidgetSettings(settings)
        RealtimeTrendWidgetProvider.updateAllWidgets(context, repository.keywordsState.value)
        _snackbarMessage.value = "위젯 설정이 저장되고 홈 화면 위젯에 적용되었습니다."
    }

    fun requestPinWidget(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, RealtimeTrendWidgetProvider::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val pinnedWidgetCallbackIntent = Intent(context, RealtimeTrendWidgetProvider::class.java)
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    999,
                    pinnedWidgetCallbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                _snackbarMessage.value = "홈 화면에 실시간 검색어 위젯 추가 요청을 전송했습니다."
            } else {
                _snackbarMessage.value = "홈 화면 런처에서 위젯 메뉴를 길게 눌러 직접 추가해주세요."
            }
        } else {
            _snackbarMessage.value = "홈 화면을 길게 눌러 위젯 메뉴에서 '실시간 검색어 순위'를 추가해주세요."
        }
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
