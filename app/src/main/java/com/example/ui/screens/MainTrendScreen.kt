package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TrendCategory
import com.example.data.model.TrendPlatform
import com.example.ui.components.KeywordDetailSheet
import com.example.ui.components.TrendAnalysisSection
import com.example.ui.components.TrendCard
import com.example.ui.components.WidgetPreviewSection
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.GeoBorder
import com.example.ui.theme.GeoOnPrimaryContainer
import com.example.ui.theme.GeoPillBg
import com.example.ui.theme.GeoPrimaryContainer
import com.example.ui.theme.GeoPrimaryViolet
import com.example.ui.theme.GeoSurface
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextMuted
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.NaverGreen
import com.example.ui.theme.RankNewGreen
import com.example.ui.viewmodel.TrendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTrendScreen(
    viewModel: TrendViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedNavTab by remember { mutableIntStateOf(0) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val filteredKeywords by viewModel.filteredKeywords.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isAiAnalyzing by viewModel.isAiAnalyzing.collectAsState()
    val autoRefreshSeconds by viewModel.autoRefreshSeconds.collectAsState()
    val isAutoRefreshActive by viewModel.isAutoRefreshActive.collectAsState()
    val selectedDetail by viewModel.selectedKeywordDetail.collectAsState()
    val trendReport by viewModel.trendReport.collectAsState()
    val widgetSettings by viewModel.widgetSettings.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAskingAi by viewModel.isAskingAi.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    // Handle snackbar
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    // Modal sheet for keyword deep dive
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (selectedDetail != null) {
        KeywordDetailSheet(
            detail = selectedDetail!!,
            sheetState = sheetState,
            onDismiss = { viewModel.clearKeywordDetail() }
        )
    }

    // Pulsing live indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GeoBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.background(GeoBackground)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GeoPrimaryViolet,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "실시간 트렌드",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = (-0.5).sp,
                                    color = GeoTextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(RankNewGreen)
                                            .alpha(pulseAlpha)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE • ${if (isAutoRefreshActive) "${autoRefreshSeconds}초 후 갱신" else "일시정지"}",
                                        fontSize = 11.sp,
                                        color = if (isAutoRefreshActive) RankNewGreen else GeoTextSecondary
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Toggle search
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("toggle_search_btn")
                        ) {
                            Icon(
                                if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "검색",
                                tint = GeoTextSecondary
                            )
                        }

                        // Toggle auto refresh
                        IconButton(
                            onClick = { viewModel.toggleAutoRefresh() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("toggle_auto_refresh_btn")
                        ) {
                            Icon(
                                if (isAutoRefreshActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = "자동 갱신 토글",
                                tint = if (isAutoRefreshActive) GeoPrimaryViolet else GeoTextSecondary
                            )
                        }

                        // Manual Refresh Button (Pill styled in Geometric Balance)
                        IconButton(
                            onClick = { viewModel.refreshTrends(isSilent = false, context = context) },
                            enabled = !isRefreshing,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("manual_refresh_btn")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = GeoPrimaryViolet,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "새로고침",
                                    tint = GeoTextSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = GeoBackground
                    )
                )

                // Search Bar Expandable
                AnimatedVisibility(visible = isSearchExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("키워드 또는 요약 검색...", fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("keyword_search_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeoPrimaryViolet,
                                unfocusedBorderColor = GeoBorder,
                                focusedContainerColor = GeoSurface,
                                unfocusedContainerColor = GeoSurface
                            )
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = GeoBackground,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = { Icon(Icons.Default.Equalizer, contentDescription = "랭킹") },
                    label = { Text("랭킹", fontSize = 11.sp, fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeoPrimaryViolet,
                        selectedTextColor = GeoPrimaryViolet,
                        indicatorColor = GeoPrimaryContainer,
                        unselectedIconColor = GeoTextSecondary,
                        unselectedTextColor = GeoTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_ranking")
                )
                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "트렌드 분석") },
                    label = { Text("트렌드 분석", fontSize = 11.sp, fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeoPrimaryViolet,
                        selectedTextColor = GeoPrimaryViolet,
                        indicatorColor = GeoPrimaryContainer,
                        unselectedIconColor = GeoTextSecondary,
                        unselectedTextColor = GeoTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_analysis")
                )
                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = "홈 위젯") },
                    label = { Text("홈 위젯", fontSize = 11.sp, fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GeoPrimaryViolet,
                        selectedTextColor = GeoPrimaryViolet,
                        indicatorColor = GeoPrimaryContainer,
                        unselectedIconColor = GeoTextSecondary,
                        unselectedTextColor = GeoTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_widget")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedNavTab) {
                0 -> {
                    // Realtime Ranking List View
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Geometric Balance Platform Segment Pill (<div class='flex p-1 bg-[#E1E2EC] rounded-full'>)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(GeoPillBg)
                                .padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TrendPlatform.values().forEach { platform ->
                                    val isSelected = selectedPlatform == platform
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(if (isSelected) GeoSurface else Color.Transparent)
                                            .then(if (isSelected) Modifier.shadow(2.dp, RoundedCornerShape(50.dp)) else Modifier)
                                            .clickable { viewModel.setPlatform(platform) }
                                            .padding(vertical = 8.dp)
                                            .testTag("tab_platform_${platform.name}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (platform) {
                                                TrendPlatform.ALL -> "통합 전체"
                                                TrendPlatform.NAVER -> "Naver"
                                                TrendPlatform.GOOGLE -> "Google"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isSelected) GeoTextPrimary else GeoTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Filter Chips
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TrendCategory.values()) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { viewModel.setCategory(category) },
                                    label = {
                                        Text(
                                            text = "${category.iconEmoji} ${category.displayName}",
                                            fontSize = 12.sp,
                                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeoPrimaryContainer,
                                        selectedLabelColor = GeoOnPrimaryContainer,
                                        containerColor = GeoSurface,
                                        labelColor = GeoTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCategory == category,
                                        borderColor = GeoBorder,
                                        selectedBorderColor = GeoPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(50.dp),
                                    modifier = Modifier.testTag("chip_category_${category.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ranking Items List (Geometric Balance List Grid Style)
                        if (filteredKeywords.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = GeoTextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "해당 조건의 검색어가 없습니다.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GeoTextSecondary
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = filteredKeywords,
                                    key = { "${it.rank}_${it.keyword}" }
                                ) { keyword ->
                                    TrendCard(
                                        keyword = keyword,
                                        onClick = { viewModel.selectKeywordForDetail(keyword.keyword) },
                                        onBookmarkToggle = { viewModel.toggleBookmark(keyword.keyword) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // AI Trend Analysis View
                    TrendAnalysisSection(
                        report = trendReport,
                        isAiAnalyzing = isAiAnalyzing,
                        onRefreshAiReport = { viewModel.requestAiDeepReport() },
                        chatMessages = chatMessages,
                        isAskingAi = isAskingAi,
                        onSendAiQuestion = { viewModel.askAi(it) }
                    )
                }

                2 -> {
                    // Widget Center & Live Preview View
                    WidgetPreviewSection(
                        keywords = filteredKeywords,
                        settings = widgetSettings,
                        onSettingsChanged = { viewModel.updateWidgetSettings(it, context) },
                        onRequestPinWidget = { viewModel.requestPinWidget(it) },
                        onManualSync = { viewModel.refreshTrends(isSilent = false, context = context) }
                    )
                }
            }
        }
    }
}
