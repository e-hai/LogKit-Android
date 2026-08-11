package com.kit.log.sample

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.kit.log.LogKit

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        LogKit.initAllLog(this)
        for (i in 0..1) {
            LogKit.v("A", "hello %s")
            LogKit.v("hello")
            LogKit.d("b", "输出调试信息 debug my log message,mamamamamama,bbbababba,ospspsp")
            LogKit.d("输出调试信息 debug my log message")
            LogKit.i("C", "输出必要信息 info my log message")
            LogKit.i("输出必要信息 info my log message")
            LogKit.w("d", "输出警告信息 warn my log message")
            LogKit.w("输出警告信息 warn my log message")
            LogKit.e("E", "输出错误信息 error my log message")
            LogKit.e("输出错误信息 error my log message")
        }


        findViewById<View>(R.id.tv_view).post({
            LogKit.showLogUi(this)
            finish()
        })

    }
}