package com.kit.log.strategy

interface FormatStrategy {
    fun log(priority: Int, onceOnlyTag: String?, message: String)
}
