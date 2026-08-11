package com.kit.log.strategy

import com.kit.log.Utils

class PrettyFormatStrategy private constructor(builder: Builder) : FormatStrategy {

    private val methodCount: Int = builder.methodCount
    private val methodOffset: Int = builder.methodOffset
    private val showThreadInfo: Boolean = builder.showThreadInfo
    private val logStrategy: LogStrategy = builder.logStrategy
    private val tag: String? = builder.tag

    override fun log(priority: Int, onceOnlyTag: String?, message: String) {
        val formatTag = formatTag(onceOnlyTag)

        logTopBorder(priority, formatTag)
        logHeaderContent(priority, formatTag, methodCount)

        // get bytes of message with UTF-8 to divide into log chunks
        val bytes = message.toByteArray()
        val length = bytes.size
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(priority, formatTag)
            }
            logContent(priority, formatTag, message)
            logBottomBorder(priority, formatTag)
            return
        }

        if (methodCount > 0) {
            logDivider(priority, formatTag)
        }
        var i = 0
        while (i < length) {
            val count = Math.min(length - i, CHUNK_SIZE)
            logContent(priority, formatTag, String(bytes, i, count))
            i += CHUNK_SIZE
        }
        logBottomBorder(priority, formatTag)
    }

    private fun logTopBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, TOP_BORDER)
    }

    private fun logHeaderContent(logType: Int, tag: String?, methodCount: Int) {
        val trace = Thread.currentThread().stackTrace
        if (showThreadInfo) {
            logChunk(logType, tag, HORIZONTAL_LINE.toString() + " Thread: " + Thread.currentThread().name)
            logDivider(logType, tag)
        }
        var level = ""
        val stackOffset = getStackOffset(trace) + methodOffset

        var count = methodCount
        if (methodCount + stackOffset > trace.size) {
            count = trace.size - stackOffset - 1
        }

        for (i in count downTo 1) {
            val stackIndex = i + stackOffset
            if (stackIndex >= trace.size) {
                continue
            }
            val builder = StringBuilder()
            builder.append(HORIZONTAL_LINE)
                .append(" ")
                .append(level)
                .append(getSimpleClassName(trace[stackIndex].className))
                .append(".")
                .append(trace[stackIndex].methodName)
                .append(" ")
                .append(" (")
                .append(trace[stackIndex].fileName)
                .append(":")
                .append(trace[stackIndex].lineNumber)
                .append(")")
            level += " "
            logChunk(logType, tag, builder.toString())
        }
    }

    private fun logBottomBorder(logType: Int, tag: String?) {
        logChunk(logType, tag, BOTTOM_BORDER)
    }

    private fun logDivider(logType: Int, tag: String?) {
        logChunk(logType, tag, MIDDLE_BORDER)
    }

    private fun logContent(logType: Int, tag: String?, chunk: String) {
        val lineSeparator = System.getProperty("line.separator") ?: "\n"
        val lines = chunk.split(lineSeparator)
        for (line in lines) {
            logChunk(logType, tag, "$HORIZONTAL_LINE $line")
        }
    }

    private fun logChunk(priority: Int, tag: String?, chunk: String) {
        logStrategy.log(priority, tag, chunk)
    }

    private fun getSimpleClassName(name: String): String {
        val lastIndex = name.lastIndexOf('.')
        return if (lastIndex == -1) name else name.substring(lastIndex + 1)
    }

    private fun formatTag(tag: String?): String? {
        if (!tag.isNullOrEmpty() && this.tag != tag) {
            return "${this.tag}-$tag"
        }
        return this.tag
    }

    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        var i = MIN_STACK_OFFSET
        while (i < trace.size) {
            val e = trace[i]
            val name = e.className
            if (name != PrettyFormatStrategy::class.java.name &&
                name != "com.kit.log.LoggerPrinter" &&
                name != "com.kit.log.LogKit"
            ) {
                return i - 1
            }
            i++
        }
        return -1
    }

    class Builder {
        internal var methodCount = 2
        internal var methodOffset = 0
        internal var showThreadInfo = true
        internal var logStrategy: LogStrategy = LogStrategy.DEFAULT_LOGCAT
        internal var tag: String? = DEFAULT_TAG

        fun methodCount(valCount: Int) = apply { this.methodCount = valCount }
        fun methodOffset(valOffset: Int) = apply { this.methodOffset = valOffset }
        fun showThreadInfo(valShow: Boolean) = apply { this.showThreadInfo = valShow }
        fun logStrategy(valStrategy: LogStrategy?) = apply {
            if (valStrategy != null) this.logStrategy = valStrategy
        }

        fun tag(valTag: String?) = apply { this.tag = valTag }

        fun build(): PrettyFormatStrategy {
            return PrettyFormatStrategy(this)
        }
    }

    companion object {
        private const val CHUNK_SIZE = 4000
        private const val MIN_STACK_OFFSET = 5
        private const val DEFAULT_TAG = "LOG_KIT"

        private const val TOP_LEFT_CORNER = '┌'
        private const val BOTTOM_LEFT_CORNER = '└'
        private const val MIDDLE_CORNER = '├'
        private const val HORIZONTAL_LINE = '│'
        private const val DOUBLE_DIVIDER = "────────────────────────────────────────────────────────"
        private const val SINGLE_DIVIDER = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
        private const val TOP_BORDER = "$TOP_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER"
        private const val BOTTOM_BORDER = "$BOTTOM_LEFT_CORNER$DOUBLE_DIVIDER$DOUBLE_DIVIDER"
        private const val MIDDLE_BORDER = "$MIDDLE_CORNER$SINGLE_DIVIDER$SINGLE_DIVIDER"

        fun newBuilder(): Builder = Builder()
    }
}
