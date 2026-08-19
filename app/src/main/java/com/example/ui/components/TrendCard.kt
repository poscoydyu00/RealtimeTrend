package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RankChangeType
import com.example.data.model.TrendKeyword
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
import com.example.ui.theme.RankSameGray
import com.example.ui.theme.RankUpRed
import java.net.URLEncoder

@Composable
fun TrendCard(
    keyword: TrendKeyword,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val rankTextColor = if (keyword.rank <= 3) GeoPrimaryViolet else GeoTextMuted
    val isTopCard = keyword.rank <= 3

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (keyword.rank == 1) GeoPrimaryContainer else GeoBorder
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .testTag("trend_card_${keyword.rank}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isTopCard) GeoSurfaceVariant else GeoSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Row: Geometric Rank, Keyword & Details, Change Indicator, Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geometric Rank Number
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isTopCard) GeoPrimaryContainer.copy(alpha = 0.6f) else GeoPillBg.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${keyword.rank}",
                        color = rankTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Keyword Title & Meta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = keyword.keyword,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${keyword.category.iconEmoji} ${keyword.category.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoBorder
                        )
                        Text(
                            text = keyword.searchVolumeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoPrimaryViolet,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Rank Change Indicator (Geometric Balance Style)
                RankChangeBadge(changeType = keyword.changeType, changeAmount = keyword.changeAmount)

                Spacer(modifier = Modifier.width(2.dp))

                // Bookmark Toggle
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_btn_${keyword.rank}")
                ) {
                    Icon(
                        imageVector = if (keyword.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "관심 키워드 등록",
                        tint = if (keyword.isBookmarked) GeoPrimaryViolet else GeoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Text
            Text(
                text = keyword.summary,
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Platform Comparison Score Bars (Naver vs Google)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GeoPillBg.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Naver Score
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Naver",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaverGreen
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { keyword.naverScore / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = NaverGreen,
                        trackColor = GeoSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${keyword.naverScore}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary
                    )
                }

                // Google Score
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Google",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoogleBlue
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(
                        progress = { keyword.googleScore / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(CircleShape),
                        color = GoogleBlue,
                        trackColor = GeoSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${keyword.googleScore}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeoTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: AI 분석, Naver, Google
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("AI 분석", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GeoOnPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = GeoPrimaryContainer,
                        labelColor = GeoOnPrimaryContainer
                    ),
                    border = BorderStroke(1.dp, GeoPrimaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Quick Naver Search
                Surface(
                    onClick = {
                        val encoded = try { URLEncoder.encode(keyword.keyword, "UTF-8") } catch (e: Exception) { keyword.keyword }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.search.naver.com/search.naver?query=$encoded"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = NaverGreen.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, NaverGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Naver", fontSize = 11.sp, color = NaverGreen, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = NaverGreen, modifier = Modifier.size(11.dp))
                    }
                }

                // Quick Google Search
                Surface(
                    onClick = {
                        val encoded = try { URLEncoder.encode(keyword.keyword, "UTF-8") } catch (e: Exception) { keyword.keyword }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = GoogleBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, GoogleBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Google", fontSize = 11.sp, color = GoogleBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = GoogleBlue, modifier = Modifier.size(11.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RankChangeBadge(
    changeType: RankChangeType,
    changeAmount: Int,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (changeType) {
        RankChangeType.UP -> "▲ ${if (changeAmount > 0) changeAmount else 1}" to RankUpRed
        RankChangeType.DOWN -> "▼ ${if (changeAmount > 0) changeAmount else 1}" to RankDownBlue
        RankChangeType.NEW -> "NEW" to RankUpRed
        RankChangeType.SAME -> "-" to RankSameGray
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
