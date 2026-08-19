package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.TrendCategory
import com.example.data.model.TrendReport
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendAnalysisSection(
    report: TrendReport?,
    isAiAnalyzing: Boolean,
    onRefreshAiReport: () -> Unit,
    chatMessages: List<ChatMessage>,
    isAskingAi: Boolean,
    onSendAiQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var questionInput by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "오늘 1위 검색어 급상승 이유는?",
        "네이버와 구글 검색 차이점 요약",
        "IT/기술 분야 핵심 이슈는?",
        "오늘 가장 화제인 연예/스포츠는?"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Geometric Balance Signature Callout Card (#EADDFF rounded-3xl)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoPrimaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = GeoOnPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TREND ANALYSIS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeoOnPrimaryContainer,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(
                            onClick = onRefreshAiReport,
                            enabled = !isAiAnalyzing,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GeoSurface.copy(alpha = 0.5f))
                                .testTag("refresh_ai_report_btn")
                        ) {
                            if (isAiAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = GeoOnPrimaryContainer,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "AI 분석 새로고침",
                                    tint = GeoOnPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Headline
                    Text(
                        text = report?.headline ?: "실시간 검색 트렌드 분석 중...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GeoOnPrimaryContainer,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = report?.executiveSummary ?: "현재 생성형 AI 기술 혁신과 실시간 주요 포털 트렌드가 화제를 주도하고 있습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GeoOnPrimaryContainer,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Featured Theme Pill
                    report?.let { rep ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GeoSurface.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🔥 ${rep.topThemeTitle}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeoOnPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = rep.topThemeDescription,
                                    fontSize = 12.sp,
                                    color = GeoTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Naver vs Google Data Comparison Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, GeoBorder), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = GeoPrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "네이버 vs 구글 검색 트렌드 비교",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Naver Insight
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NaverGreen.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, NaverGreen.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NaverGreen
                                ) {
                                    Text(
                                        text = "NAVER",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "국내 실시간 반응 & 라이프스타일",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaverGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report?.naverKeyInsight ?: "국내 실시간 시사 뉴스 및 생활 정보 검색이 집중됩니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google Insight
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoogleBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, GoogleBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = GoogleBlue
                                ) {
                                    Text(
                                        text = "GOOGLE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "글로벌 테크 & 심층 정보 탐색",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoogleBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report?.googleKeyInsight ?: "글로벌 IT/테크 및 심층 배경 지식 검색이 두드러집니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Combined Insight
                    Text(
                        text = "💡 종합 비교: ${report?.platformComparisonInsight ?: "네이버는 속보 중심, 구글은 심층 분석 중심의 검색 경향을 보입니다."}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeoTextPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Category Distribution
        item {
            report?.categoryRatios?.let { ratios ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, GeoBorder), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = GeoSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = GeoPrimaryViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "카테고리별 검색 화제성 분포",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GeoTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ratios.forEach { (cat, ratio) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${cat.iconEmoji} ${cat.displayName}",
                                        fontSize = 12.sp,
                                        color = GeoTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${(ratio * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeoPrimaryViolet
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = when (cat) {
                                        TrendCategory.TECH -> GeoPrimaryViolet
                                        TrendCategory.ECONOMY -> Color(0xFFF59E0B)
                                        TrendCategory.ENTERTAINMENT -> Color(0xFFEC4899)
                                        TrendCategory.SPORTS -> Color(0xFF10B981)
                                        TrendCategory.SOCIETY -> Color(0xFF6366F1)
                                        else -> Color(0xFF8B5CF6)
                                    },
                                    trackColor = GeoPillBg
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive AI Chat / Ask Trend Assistant
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, GeoBorder), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GeoSurface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = GeoPrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "트렌드 AI에게 직접 물어보기",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GeoTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Suggestion Chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQuestions.forEach { q ->
                            SuggestionChip(
                                onClick = {
                                    questionInput = q
                                    onSendAiQuestion(q)
                                },
                                label = { Text(q, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = GeoSurfaceVariant,
                                    labelColor = GeoTextPrimary
                                ),
                                border = BorderStroke(1.dp, GeoBorder)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat messages box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GeoSurfaceVariant)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatMessages.takeLast(4).forEach { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (msg.isUser) 12.dp else 2.dp,
                                        bottomEnd = if (msg.isUser) 2.dp else 12.dp
                                    ),
                                    color = if (msg.isUser) GeoPrimaryViolet else GeoPrimaryContainer,
                                    border = BorderStroke(1.dp, if (msg.isUser) GeoPrimaryViolet else GeoBorder)
                                ) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.isUser) Color.White else GeoOnPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        if (isAskingAi) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = GeoPrimaryViolet,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI가 실시간 데이터를 분석하여 답변 작성 중...", fontSize = 11.sp, color = GeoPrimaryViolet)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            placeholder = { Text("궁금한 트렌드 질문을 입력하세요", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_question_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GeoPrimaryViolet,
                                unfocusedBorderColor = GeoBorder,
                                focusedContainerColor = GeoSurface,
                                unfocusedContainerColor = GeoSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (questionInput.isNotBlank()) {
                                    val q = questionInput
                                    questionInput = ""
                                    onSendAiQuestion(q)
                                }
                            },
                            enabled = !isAskingAi && questionInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GeoPrimaryViolet),
                            modifier = Modifier.testTag("send_ai_question_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "전송", tint = Color.White)
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
