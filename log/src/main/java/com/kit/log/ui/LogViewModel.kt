package com.kit.log.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kit.log.LogKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class LogInfo(
    val date: Long,         // 时间戳
    val dateString: String, // 时间字符串
    val priority: String,   // 日志级别
    val tag: String,        // 标签
    val message: String     // 内容
)

class LogViewModel : ViewModel() {

    private val logs = mutableListOf<LogInfo>()
    private val tags = linkedSetOf<String>()

    val tagMSF: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val logMSF: MutableStateFlow<List<LogInfo>> = MutableStateFlow(emptyList())

    init {
        LogKit.readLog { result ->
            logs.clear()
            tags.clear()
            tags.add(TAG_SHOW_ALL_LOG)
            result.forEach { line ->
                val oneLogLine = line.split(",", limit = 5)
                if (oneLogLine.size >= 4) {
                    val timeStamp = oneLogLine[0].toLongOrNull() ?: System.currentTimeMillis()
                    val dateString = oneLogLine[1]
                    val priority = oneLogLine[2]
                    val tag = oneLogLine[3]
                    val message = oneLogLine.getOrNull(4) ?: ""

                    val oneLogInfo = LogInfo(
                        timeStamp,
                        dateString,
                        priority,
                        tag,
                        message
                    )
                    logs.add(oneLogInfo)
                    tags.add(tag)
                }
            }
            loadTag()
            loadLog()
        }
    }

    fun loadLog(filterTag: String = TAG_SHOW_ALL_LOG) {
        viewModelScope.launch(Dispatchers.IO) {
            if (filterTag == TAG_SHOW_ALL_LOG) {
                logMSF.emit(logs)
            } else {
                logMSF.emit(logs.filter { it.tag == filterTag })
            }
        }
    }

    fun loadTag() {
        viewModelScope.launch(Dispatchers.IO) {
            tagMSF.emit(tags.toList())
        }
    }

    companion object {
        const val TAG_SHOW_ALL_LOG = "ALL TAG"
    }
}