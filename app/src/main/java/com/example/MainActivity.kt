package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.TrendRepository
import com.example.ui.screens.MainTrendScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TrendViewModel
import com.example.widget.RealtimeTrendWidgetProvider

class MainActivity : ComponentActivity() {

    private lateinit var repository: TrendRepository
    private lateinit var viewModel: TrendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = TrendRepository(applicationContext)
        viewModel = TrendViewModel(repository)

        // Handle intent from Widget click
        handleWidgetIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainTrendScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val keyword = intent?.getStringExtra(RealtimeTrendWidgetProvider.EXTRA_KEYWORD)
            ?: intent?.data?.lastPathSegment
        if (!keyword.isNullOrBlank()) {
            viewModel.selectKeywordForDetail(keyword)
        }
    }
}
