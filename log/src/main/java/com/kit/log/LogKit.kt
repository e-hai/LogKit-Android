package com.kit.log

import android.app.Activity
import android.content.Context
import com.kit.log.strategy.CsvFormatStrategy
import com.kit.log.strategy.DiskLogWriteReadStrategy
import com.kit.log.ui.LogActivity.Companion.startActivity

object LogKit {

    private const val DEFAULT_TAG = "LogKit"
    private var printer = LoggerPrinter()
    private var diskLogWriteReadStrategy: DiskLogWriteReadStrategy? = null

    // --- 核心配置与 Adapter 管理 ---

    @JvmStatic
    fun printer(printer: LoggerPrinter) {
        this.printer = printer
    }

    @JvmStatic
    fun addLogAdapter(adapter: LogAdapter) {
        printer.addAdapter(adapter)
    }

    @JvmStatic
    fun clearLogAdapters() {
        printer.clearLogAdapters()
    }

    /**
     * 临时设置日志标签（链式调用），只在当前打印操作中生效
     */
    @JvmStatic
    fun t(tag: String?): LoggerPrinter {
        return printer.t(tag)
    }

    // --- 初始化与管理入口 ---

    fun initOnlyAndroidLog() {
        clearLogAdapters()
        addLogAdapter(LogAdapter.createAndroidAdapter())
    }

    fun initOnlyDiskLog() {
        clearLogAdapters()
        addLogAdapter(LogAdapter.createDiskAdapter())
    }

    fun initAllLog(context: Context) {
        clearLogAdapters()
        addLogAdapter(LogAdapter.createAndroidAdapter())
        val strategy = DiskLogWriteReadStrategy.build(context)
        diskLogWriteReadStrategy = strategy
        val csvFormatStrategy = CsvFormatStrategy.newBuilder()
            .logStrategy(strategy)
            .build()
        addLogAdapter(LogAdapter.createDiskAdapter(csvFormatStrategy))
    }

    fun showLogUi(activity: Activity) {
        startActivity(activity)
    }

    fun readLog(callback: (List<String>) -> Unit) {
        diskLogWriteReadStrategy?.readLog { logs ->
            callback.invoke(logs ?: emptyList())
        } ?: callback.invoke(emptyList())
    }

    // --- 日志打印 API ---

    @JvmStatic
    fun log(priority: Int, tag: String?, message: String?, throwable: Throwable?) {
        printer.log(priority, tag, message, throwable)
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        printer.t(tag).d(message)
    }

    @JvmStatic
    fun d(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).d(message, *args)
    }

    @JvmStatic
    fun d(obj: Any?) {
        printer.t(DEFAULT_TAG).d(obj)
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        printer.t(tag).e(null, message)
    }

    @JvmStatic
    fun e(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).e(null, message, *args)
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable?, message: String) {
        printer.t(tag).e(throwable, message)
    }

    @JvmStatic
    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).e(throwable, message, *args)
    }

    @JvmStatic
    fun i(tag: String, message: String) {
        printer.t(tag).i(message)
    }

    @JvmStatic
    fun i(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).i(message, *args)
    }

    @JvmStatic
    fun v(tag: String, message: String) {
        printer.t(tag).v(message)
    }

    @JvmStatic
    fun v(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).v(message, *args)
    }

    @JvmStatic
    fun w(tag: String, message: String) {
        printer.t(tag).w(message)
    }

    @JvmStatic
    fun w(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).w(message, *args)
    }

    @JvmStatic
    fun wtf(tag: String, message: String) {
        printer.t(tag).wtf(message)
    }

    @JvmStatic
    fun wtf(message: String, vararg args: Any?) {
        printer.t(DEFAULT_TAG).wtf(message, *args)
    }

    @JvmStatic
    fun json(tag: String, json: String?) {
        printer.t(tag).json(json)
    }

    @JvmStatic
    fun json(json: String?) {
        printer.t(DEFAULT_TAG).json(json)
    }

    @JvmStatic
    fun xml(tag: String, xml: String?) {
        printer.t(tag).xml(xml)
    }

    @JvmStatic
    fun xml(xml: String?) {
        printer.t(DEFAULT_TAG).xml(xml)
    }
}
