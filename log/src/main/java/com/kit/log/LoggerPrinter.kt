package com.kit.log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import java.util.ArrayList
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class LoggerPrinter {

    private val localTag = ThreadLocal<String>()
    private val logAdapters: MutableList<LogAdapter> = ArrayList()

    fun t(tag: String?): LoggerPrinter {
        if (tag != null) {
            localTag.set(tag)
        }
        return this
    }

    fun d(message: String, vararg args: Any?) {
        log(Utils.DEBUG, null, message, *args)
    }

    fun d(obj: Any?) {
        log(Utils.DEBUG, null, Utils.toString(obj))
    }

    fun e(message: String, vararg args: Any?) {
        e(null, message, *args)
    }

    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        log(Utils.ERROR, throwable, message, *args)
    }

    fun w(message: String, vararg args: Any?) {
        log(Utils.WARN, null, message, *args)
    }

    fun i(message: String, vararg args: Any?) {
        log(Utils.INFO, null, message, *args)
    }

    fun v(message: String, vararg args: Any?) {
        log(Utils.VERBOSE, null, message, *args)
    }

    fun wtf(message: String, vararg args: Any?) {
        log(Utils.ASSERT, null, message, *args)
    }

    fun json(json: String?) {
        if (json.isNullOrEmpty()) {
            d("Empty/Null json content")
            return
        }
        try {
            val trimmed = json.trim()
            if (trimmed.startsWith("{")) {
                val jsonObject = JSONObject(trimmed)
                val message = jsonObject.toString(JSON_INDENT)
                d(message)
                return
            }
            if (trimmed.startsWith("[")) {
                val jsonArray = JSONArray(trimmed)
                val message = jsonArray.toString(JSON_INDENT)
                d(message)
                return
            }
            e("Invalid Json")
        } catch (e: JSONException) {
            e("Invalid Json")
        }
    }

    fun xml(xml: String?) {
        if (xml.isNullOrEmpty()) {
            d("Empty/Null xml content")
            return
        }
        try {
            val xmlInput: Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            d(xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n"))
        } catch (e: Exception) {
            e("Invalid xml")
        }
    }

    @Synchronized
    fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        var finalMessage = message
        if (throwable != null && finalMessage != null) {
            finalMessage += " : " + Utils.getStackTraceString(throwable)
        }
        if (throwable != null && finalMessage == null) {
            finalMessage = Utils.getStackTraceString(throwable)
        }
        if (finalMessage.isNullOrEmpty()) {
            finalMessage = "Empty/NULL log message"
        }

        for (adapter in logAdapters) {
            if (adapter.isLoggable(priority, tag)) {
                adapter.log(priority, tag, finalMessage)
            }
        }
    }

    fun clearLogAdapters() {
        logAdapters.clear()
    }

    fun addAdapter(adapter: LogAdapter) {
        logAdapters.add(adapter)
    }

    @Synchronized
    private fun log(
        priority: Int,
        throwable: Throwable?,
        msg: String,
        vararg args: Any?
    ) {
        val tag = getTag()
        val message = createMessage(msg, *args)
        log(priority, tag, message, throwable)
    }

    private fun getTag(): String? {
        val tag = localTag.get()
        if (tag != null) {
            localTag.remove()
            return tag
        }
        return null
    }

    private fun createMessage(message: String, vararg args: Any?): String {
        return if (args.isEmpty()) message else String.format(message, *args)
    }

    companion object {
        private const val JSON_INDENT = 2
    }
}
