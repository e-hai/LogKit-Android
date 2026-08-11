package com.kit.log.strategy

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.Arrays
import java.util.LinkedList

class DiskLogWriteReadStrategy private constructor(
    private val handler: Handler
) : LogStrategy {

    override fun log(priority: Int, tag: String?, message: String) {
        handler.sendMessage(handler.obtainMessage(WHAT_WRITE, message))
    }

    fun readLog(callback: LogCallback) {
        handler.sendMessage(handler.obtainMessage(WHAT_READ, callback))
    }

    fun interface LogCallback {
        fun onLogsRead(logs: List<String>?)
    }

    internal class WriteReadHandler(
        looper: Looper,
        private val folder: String,
        private val maxFileSize: Int
    ) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_WRITE -> {
                    val content = msg.obj as? String ?: return
                    try {
                        writeLog(folder, "logs", content)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                WHAT_READ -> {
                    val callback = msg.obj as? LogCallback ?: return
                    try {
                        readLog(folder, "logs", callback)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onLogsRead(null)
                    }
                }
            }
        }

        @Throws(IOException::class)
        private fun writeLog(folderName: String, fileName: String, content: String) {
            val logFile = getWriteLogFile(folderName, fileName)
            FileWriter(logFile, true).use { writer ->
                writer.append(content)
                writer.flush()
            }
        }

        private fun getWriteLogFile(folderName: String, fileName: String): File {
            val folder = File(folderName)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            var newFileCount = 0
            var existingFile: File? = null
            var newFile = File(folder, "${fileName}_${newFileCount}.csv")

            while (newFile.exists()) {
                existingFile = newFile
                newFileCount++
                newFile = File(folder, "${fileName}_${newFileCount}.csv")
            }

            if (existingFile != null) {
                if (existingFile.length() >= maxFileSize) {
                    return newFile
                }
                return existingFile
            }

            return newFile
        }

        private fun readLog(folderName: String, fileName: String, callback: LogCallback) {
            val folder = File(folderName)
            if (!folder.exists()) {
                callback.onLogsRead(null)
                return
            }

            val files = folder.listFiles { _, name ->
                name.startsWith(fileName) && name.endsWith(".csv")
            }
            if (files.isNullOrEmpty()) {
                callback.onLogsRead(null)
                return
            }

            Arrays.sort(files) { f1, f2 ->
                val num1 = f1.name.replace("${fileName}_", "").replace(".csv", "").toIntOrNull() ?: 0
                val num2 = f2.name.replace("${fileName}_", "").replace(".csv", "").toIntOrNull() ?: 0
                num2.compareTo(num1)
            }

            val recentLogs = LinkedList<String>()
            for (file in files) {
                val currentFileLines = LinkedList<String>()
                BufferedReader(FileReader(file)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let { currentFileLines.add(it) }
                    }
                }

                while (currentFileLines.isNotEmpty() && recentLogs.size < MAX_READ_LINES) {
                    recentLogs.add(currentFileLines.pollLast()!!)
                }

                if (recentLogs.size >= MAX_READ_LINES) {
                    break
                }
            }

            callback.onLogsRead(recentLogs)
        }
    }

    companion object {
        private const val MAX_READ_LINES = 3000
        private const val MAX_BYTES = 500 * 1024 // 500KB
        private const val WHAT_WRITE = 1
        private const val WHAT_READ = 2

        fun build(context: Context): DiskLogWriteReadStrategy {
            val diskPath = context.filesDir.absolutePath
            val folder = "$diskPath${File.separatorChar}logger"
            val ht = HandlerThread("LogKitFileLogger.$folder")
            ht.start()
            val handler = WriteReadHandler(ht.looper, folder, MAX_BYTES)
            return DiskLogWriteReadStrategy(handler)
        }
    }
}
