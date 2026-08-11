package com.kit.log.strategy

import android.util.Log

fun interface LogStrategy {
    fun log(priority: Int, tag: String?, message: String)

    companion object {
        val DEFAULT_LOGCAT = LogStrategy { priority, tag, message ->
            Log.println(priority, tag ?: "LOG_KIT", message)
        }
    }
}
