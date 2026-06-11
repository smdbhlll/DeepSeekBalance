package com.deepseek.balance

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class BalanceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 自定义 WorkManager 配置
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(this, config)
    }
}
