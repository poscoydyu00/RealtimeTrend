package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RankChangeType
import com.example.data.model.TrendKeyword
import com.example.data.model.TrendPlatform
import com.example.data.model.WidgetSettings
import com.example.data.model.WidgetThemeMode
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
import com.example.ui.theme.RankDownBlue
import com.example.ui.theme.RankNewGreen
import com.example.ui.theme.RankUpRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WidgetPreviewSection(
    keywords: List<TrendKeyword>,
    settings: WidgetSettings,
    onSettingsChanged: (WidgetSettings) -> Unit,
    onRequestPinWidget: (Context) -> Unit,
    onManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var previewPage by remember { mutableIntStateOf(0) } // 0: 1~5, 1: 6~10
    val pageKeywords = if (previewPage == 0) keywords.take(5) else keywords.drop(5).take(5)

    val widgetBgBrush = when (settings.themeMode) {
        WidgetThemeMode.DARK_GLASS -> Brush.linearGradient(listOf(Color(0xFF2A2831), Color(0xFF1B1B1F)))
        WidgetThemeMode.CLEAN_LIGHT -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFDFBFF)))
        WidgetThemeMode.CYBER_NEON -> Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFF21005D)))
        WidgetThemeMode.NAVER_GREEN -> Brush.linearGradient(listOf(Color(0xFF034A26), Color(0xFF062314)))
        WidgetThemeMode.GOOGLE_BLUE -> Brush.linearGradient(listOf(Color(0xFF173566), Color(0xFF0B1933)))
    }

    val isLightMode = settings.themeMode == WidgetThemeMode.CLEAN_LIGHT
    val widgetTextColor = if (isLightMode) GeoTextPrimary else Color(0xFFF8FAFC)
    val widgetSubTextColor = if (isLightMode) GeoTextSecondary else Color(0xFFC7C5D0)

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Intro Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GeoPrimaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GeoSurface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = GeoPrimaryViolet, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "홈 화면 실시간 검색어 위젯",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnPrimaryContainer
                        )
                        Text(
                            text = "홈 화면에서 1위~10위 실시간 검색어와 AI 요약을 바로 확인하세요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeoOnPrimaryContainer
                        )
                    }
                }
            }
        }

        // Live Interactive Widget Preview Mock
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📱 실시간 위젯 라이브 프리뷰 (4x3 / 4x2)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GeoPrimaryViolet
                    )
                    Text(
                        text = "프리뷰 터치 가능",
                        fontSize = 11.sp,
                        color = GeoTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // The Widget Mock Card (mirrors RemoteViews widget)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(widgetBgBrush)
                        .border(
                            BorderStroke(
                                1.dp,
                                when (settings.themeMode) {
                                    WidgetThemeMode.CYBER_NEON -> GeoPrimaryViolet
                                    WidgetThemeMode.NAVER_GREEN -> NaverGreen
                                    WidgetThemeMode.GOOGLE_BLUE -> GoogleBlue
                                    WidgetThemeMode.CLEAN_LIGHT -> GeoBorder
                                    else -> GeoBorder
                                }
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                        .testTag("widget_mock_preview")
                ) {
                    Column {
                        // Widget Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "실시간 급상승 트렌드",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (settings.themeMode) {
                                        WidgetThemeMode.CLEAN_LIGHT -> GeoPrimaryViolet
                                        WidgetThemeMode.NAVER_GREEN -> Color(0xFF4ADE80)
                                        WidgetThemeMode.GOOGLE_BLUE -> Color(0xFF60A5FA)
                                        else -> Color(0xFFEADDFF)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isLightMode) GeoPillBg else Color(0xFF33303D)
                                ) {
                                    Text(
                                        text = settings.targetPlatform.displayName,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = widgetSubTextColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Page Toggle Pill
                                Surface(
                                    onClick = { previewPage = if (previewPage == 0) 1 else 0 },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isLightMode) GeoPillBg else Color(0xFF33303D),
                                    border = BorderStroke(1.dp, if (isLightMode) GeoBorder else Color(0xFF474554))
                                ) {
                                    Text(
                                        text = if (previewPage == 0) "1-5위" else "6-10위",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = widgetTextColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                // Sync Button
                                IconButton(
                                    onClick = onManualSync,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isLightMode) GeoPillBg else Color(0xFF33303D))
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "새로고침",
                                        tint = widgetTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Items List (5 items)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            pageKeywords.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Geometric Rank Box
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (item.rank) {
                                                    1 -> GeoPrimaryViolet
                                                    2 -> GeoPrimaryViolet.copy(alpha = 0.85f)
                                                    3 -> GeoPrimaryViolet.copy(alpha = 0.7f)
                                                    else -> if (isLightMode) GeoPillBg else Color(0xFF33303D)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.rank}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.rank <= 3) Color.White else widgetTextColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = item.keyword,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = widgetTextColor,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (settings.showCategoryBadge) {
                                        Text(
                                            text = item.category.displayName.substringBefore("/"),
                                            fontSize = 10.sp,
                                            color = widgetSubTextColor,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }

                                    // Rank Change
                                    val (changeText, changeColor) = when (item.changeType) {
                                        RankChangeType.UP -> "▲ ${if (item.changeAmount > 0) item.changeAmount else 1}" to RankUpRed
                                        RankChangeType.DOWN -> "▼ ${if (item.changeAmount > 0) item.changeAmount else 1}" to RankDownBlue
                                        RankChangeType.NEW -> "NEW" to RankNewGreen
                                        RankChangeType.SAME -> "-" to widgetSubTextColor
                                    }
                                    Text(
                                        text = changeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = changeColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Widget Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "업데이트: ${SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())}",
                                fontSize = 10.sp,
                                color = widgetSubTextColor
                            )
                            Text(
                                text = "터치하여 AI 트렌드 분석 열기 ↗",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLightMode) GeoPrimaryViolet else Color(0xFFD0BCFF)
                            )
                        }
                    }
                }
            }
        }

        // Add to Home Screen Button
        item {
            Button(
                onClick = { onRequestPinWidget(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("pin_widget_to_home_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryViolet)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "홈 화면에 위젯 바로 추가하기",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Widget Customization Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, GeoBorder), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = GeoPrimaryViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "위젯 세부 스타일 & 데이터 설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Theme selector
                    Text(
                        text = "위젯 테마 스타일",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WidgetThemeMode.values().forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { onSettingsChanged(settings.copy(themeMode = mode)) },
                                label = { Text(mode.title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeoPrimaryContainer,
                                    selectedLabelColor = GeoOnPrimaryContainer
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target platform selector
                    Text(
                        text = "위젯에 표시할 데이터 소스",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrendPlatform.values().forEach { plat ->
                            FilterChip(
                                selected = settings.targetPlatform == plat,
                                onClick = { onSettingsChanged(settings.copy(targetPlatform = plat)) },
                                label = { Text(plat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (plat) {
                                        TrendPlatform.NAVER -> NaverGreen
                                        TrendPlatform.GOOGLE -> GoogleBlue
                                        else -> GeoPrimaryContainer
                                    },
                                    selectedLabelColor = if (plat == TrendPlatform.ALL) GeoOnPrimaryContainer else Color.White
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto refresh interval selector
                    Text(
                        text = "위젯 백그라운드 갱신 주기",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 5, 15, 30).forEach { mins ->
                            FilterChip(
                                selected = settings.refreshIntervalMinutes == mins,
                                onClick = { onSettingsChanged(settings.copy(refreshIntervalMinutes = mins)) },
                                label = { Text("${mins}분마다", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GeoPrimaryContainer,
                                    selectedLabelColor = GeoOnPrimaryContainer
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
