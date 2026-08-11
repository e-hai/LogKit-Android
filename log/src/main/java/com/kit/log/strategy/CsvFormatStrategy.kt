package com.kit.log.strategy

import com.kit.log.Utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvFormatStrategy private constructor(builder: Builder) : FormatStrategy {

    private val date: Date = builder.date
    private val dateFormat: SimpleDateFormat = builder.dateFormat
    private val logStrategy: LogStrategy = builder.logStrategy ?: LogStrategy.DEFAULT_LOGCAT
    private val tag: String? = builder.tag

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        val formatTag = formatTag(onceOnlyTag)
        date.time = System.currentTimeMillis()

        val builder = StringBuilder()
        // machine-readable date/time
        builder.append(date.time.toString())
        builder.append(NEW_LINE_REPLACEMENT)

        // human-readable date/time
        builder.append(dateFormat.format(date))
        builder.append(NEW_LINE_REPLACEMENT)

        // level
        builder.append(Utils.logLevelName(priority))
        builder.append(NEW_LINE_REPLACEMENT)

        // tag
        builder.append(formatTag)
        builder.append(NEW_LINE_REPLACEMENT)

        // message
        if (message.contains("\n")) {
            builder.append(message.replace("\n", " "))
        } else {
            builder.append(message)
        }

        // new line
        builder.append(NEW_LINE)

        logStrategy.log(priority, formatTag, builder.toString())
    }

    private fun formatTag(tag: String?): String? {
        if (!tag.isNullOrEmpty() && this.tag != tag) {
            return "${this.tag}-$tag"
        }
        return this.tag
    }

    class Builder {
        internal var date: Date = Date()
        internal var dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
        internal var logStrategy: LogStrategy? = null
        internal var tag: String? = DEFAULT_TAG

        fun date(valDate: Date) = apply { this.date = valDate }
        fun dateFormat(valFormat: SimpleDateFormat) = apply { this.dateFormat = valFormat }
        fun logStrategy(valStrategy: LogStrategy?) = apply {
            this.logStrategy = valStrategy
        }
        fun tag(valTag: String?) = apply { this.tag = valTag }

        fun build(): CsvFormatStrategy {
            return CsvFormatStrategy(this)
        }
    }

    companion object {
        private const val NEW_LINE = "\n"
        private const val NEW_LINE_REPLACEMENT = ","
        private const val DEFAULT_TAG = "LOG_KIT"

        fun newBuilder(): Builder = Builder()
    }
}
