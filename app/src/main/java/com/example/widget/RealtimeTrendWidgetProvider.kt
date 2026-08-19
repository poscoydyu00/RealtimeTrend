package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.RankChangeType
import com.example.data.model.TrendKeyword
import com.example.data.repository.TrendRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RealtimeTrendWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val repo = TrendRepository(context)
        val keywords = repo.refreshTrends(forceAiRefresh = false)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, keywords, isSecondPage = false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, RealtimeTrendWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        when (intent.action) {
            ACTION_REFRESH_WIDGET -> {
                val repo = TrendRepository(context)
                val keywords = repo.refreshTrends(forceAiRefresh = false)
                currentIsSecondPage = false
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, keywords, isSecondPage = false)
                }
            }
            ACTION_TOGGLE_WIDGET_PAGE -> {
                currentIsSecondPage = !currentIsSecondPage
                val repo = TrendRepository(context)
                val keywords = repo.keywordsState.value.ifEmpty { repo.refreshTrends(forceAiRefresh = false) }
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, keywords, isSecondPage = currentIsSecondPage)
                }
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"
        const val ACTION_TOGGLE_WIDGET_PAGE = "com.example.ACTION_TOGGLE_WIDGET_PAGE"
        const val EXTRA_KEYWORD = "extra_keyword"

        private var currentIsSecondPage = false

        fun updateAllWidgets(context: Context, keywords: List<TrendKeyword>? = null) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, RealtimeTrendWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            val data = keywords ?: TrendRepository(context).refreshTrends(false)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, data, currentIsSecondPage)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            keywords: List<TrendKeyword>,
            isSecondPage: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_realtime_trend)
            val timeString = SimpleDateFormat("HH:mm:ss", Locale.KOREA).format(Date())

            views.setTextViewText(R.id.widget_updated_time, "업데이트: $timeString")
            views.setTextViewText(R.id.widget_page_toggle, if (isSecondPage) "6-10위" else "1-5위")

            // Page toggle intent
            val toggleIntent = Intent(context, RealtimeTrendWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_WIDGET_PAGE
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_page_toggle, togglePendingIntent)

            // Refresh intent
            val refreshIntent = Intent(context, RealtimeTrendWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            // Title / root click opens app
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                context,
                102,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_tap_hint, appPendingIntent)

            // Populate 5 items based on page
            val pageItems = if (isSecondPage) {
                keywords.drop(5).take(5)
            } else {
                keywords.take(5)
            }

            val itemLayoutIds = listOf(
                Triple(R.id.widget_item_1, R.id.widget_item_rank_1, R.id.widget_item_title_1),
                Triple(R.id.widget_item_2, R.id.widget_item_rank_2, R.id.widget_item_title_2),
                Triple(R.id.widget_item_3, R.id.widget_item_rank_3, R.id.widget_item_title_3),
                Triple(R.id.widget_item_4, R.id.widget_item_rank_4, R.id.widget_item_title_4),
                Triple(R.id.widget_item_5, R.id.widget_item_rank_5, R.id.widget_item_title_5),
            )
            val tagIds = listOf(
                R.id.widget_item_tag_1,
                R.id.widget_item_tag_2,
                R.id.widget_item_tag_3,
                R.id.widget_item_tag_4,
                R.id.widget_item_tag_5
            )
            val changeIds = listOf(
                R.id.widget_item_change_1,
                R.id.widget_item_change_2,
                R.id.widget_item_change_3,
                R.id.widget_item_change_4,
                R.id.widget_item_change_5
            )

            for (i in 0 until 5) {
                val item = pageItems.getOrNull(i)
                val (rowId, rankId, titleId) = itemLayoutIds[i]
                val tagId = tagIds[i]
                val changeId = changeIds[i]

                if (item != null) {
                    val displayRank = item.rank
                    views.setTextViewText(rankId, "$displayRank")
                    views.setTextViewText(titleId, item.keyword)
                    views.setTextViewText(tagId, item.category.displayName.substringBefore("/"))

                    val changeText = when (item.changeType) {
                        RankChangeType.UP -> "▲ ${if (item.changeAmount > 0) item.changeAmount else 1}"
                        RankChangeType.DOWN -> "▼ ${if (item.changeAmount > 0) item.changeAmount else 1}"
                        RankChangeType.NEW -> "NEW"
                        RankChangeType.SAME -> "-"
                    }
                    val changeColor = when (item.changeType) {
                        RankChangeType.UP -> 0xFFEF4444.toInt()
                        RankChangeType.DOWN -> 0xFF3B82F6.toInt()
                        RankChangeType.NEW -> 0xFF10B981.toInt()
                        RankChangeType.SAME -> 0xFF94A3B8.toInt()
                    }
                    views.setTextViewText(changeId, changeText)
                    views.setTextColor(changeId, changeColor)

                    // Click item opens app focused on keyword
                    val itemIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_KEYWORD, item.keyword)
                        data = Uri.parse("realtimetrend://keyword/${item.keyword}")
                    }
                    val itemPendingIntent = PendingIntent.getActivity(
                        context,
                        200 + i + (if (isSecondPage) 10 else 0),
                        itemIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(rowId, itemPendingIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
