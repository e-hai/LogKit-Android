package com.kit.log.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.kit.log.R

internal class LogActivity : ComponentActivity() {

    private val viewModel by viewModels<LogViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置边到边沉浸式 Decor 窗口
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LogScreen(
                viewModel = viewModel,
                onBackClick = { finish() }
            )
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(
            0,
            R.anim.slide_out_top_top_bottom
        )
    }

    companion object {
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, LogActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(
                R.anim.slide_in_bottom_to_top,
                0
            )
        }
    }
}
