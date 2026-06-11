package com.deepseek.balance

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class BalanceApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // 手动初始化 WorkManager
        WorkManager.initialize(this, workManagerConfiguration)
    }
}
