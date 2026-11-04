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

    private val allLogs = mutableListOf<LogInfo>()
    private val rawTags = linkedSetOf<String>()

    val tagMSF: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val logMSF: MutableStateFlow<List<LogInfo>> = MutableStateFlow(emptyList())

    // 选中的 Tags 集合（空集合或包含 TAG_SHOW_ALL_LOG 均表示不限 Tag）
    val selectedTagsMSF: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    init {
        LogKit.readLog { result ->
            allLogs.clear()
            rawTags.clear()
            rawTags.add(TAG_SHOW_ALL_LOG)
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
                    allLogs.add(oneLogInfo)
                    rawTags.add(tag)
                }
            }
            loadTag()
            applyFilter()
        }
    }

    /**
     * 切换某个 Tag 的选中状态（多选模式）
     */
    fun toggleTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = selectedTagsMSF.value.toMutableSet()
            if (tag == TAG_SHOW_ALL_LOG) {
                // 点击全选，清空具体过滤
                current.clear()
            } else {
                current.remove(TAG_SHOW_ALL_LOG)
                if (current.contains(tag)) {
                    current.remove(tag)
                } else {
                    current.add(tag)
                }
            }
            selectedTagsMSF.emit(current)
            applyFilter()
        }
    }

    /**
     * 移除单个指定的已选 Tag
     */
    fun removeTag(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = selectedTagsMSF.value.toMutableSet()
            current.remove(tag)
            selectedTagsMSF.emit(current)
            applyFilter()
        }
    }

    /**
     * 一键重置清空全部已选 Tag
     */
    fun clearAllTags() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedTagsMSF.emit(emptySet())
            applyFilter()
        }
    }

    private fun applyFilter() {
        val selected = selectedTagsMSF.value
        if (selected.isEmpty() || selected.contains(TAG_SHOW_ALL_LOG)) {
            logMSF.tryEmit(allLogs)
        } else {
            logMSF.tryEmit(allLogs.filter { selected.contains(it.tag) })
        }
    }

    fun loadTag() {
        viewModelScope.launch(Dispatchers.IO) {
            tagMSF.emit(rawTags.toList())
        }
    }

    companion object {
        const val TAG_SHOW_ALL_LOG = "ALL TAG"
    }
}