package com.kit.log

import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

object Utils {

    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6
    const val ASSERT = 7

    fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    fun logLevelName(logLevel: Int): String {
        return when (logLevel) {
            VERBOSE -> "VERBOSE"
            DEBUG -> "DEBUG"
            INFO -> "INFO"
            WARN -> "WARN"
            ERROR -> "ERROR"
            ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    fun toString(obj: Any?): String {
        if (obj == null) {
            return "null"
        }
        if (!obj.javaClass.isArray) {
            return obj.toString()
        }
        return when (obj) {
            is BooleanArray -> obj.contentToString()
            is ByteArray -> obj.contentToString()
            is CharArray -> obj.contentToString()
            is DoubleArray -> obj.contentToString()
            is FloatArray -> obj.contentToString()
            is IntArray -> obj.contentToString()
            is LongArray -> obj.contentToString()
            is ShortArray -> obj.contentToString()
            is Array<*> -> obj.contentDeepToString()
            else -> obj.toString()
        }
    }
}
