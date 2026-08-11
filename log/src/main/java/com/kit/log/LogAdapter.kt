package com.kit.log

import com.kit.log.strategy.CsvFormatStrategy
import com.kit.log.strategy.FormatStrategy
import com.kit.log.strategy.PrettyFormatStrategy

fun interface LogAdapter {

    fun log(priority: Int, tag: String?, message: String)

    fun isLoggable(priority: Int, tag: String?): Boolean = true

    companion object {
        fun createAndroidAdapter(
            formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder().build()
        ): LogAdapter = LogAdapter { priority, tag, message ->
            formatStrategy.log(priority, tag, message)
        }

        fun createDiskAdapter(
            formatStrategy: FormatStrategy = CsvFormatStrategy.newBuilder().build()
        ): LogAdapter = LogAdapter { priority, tag, message ->
            formatStrategy.log(priority, tag, message)
        }
    }
}
