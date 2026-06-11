package com.deepseek.balance

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.deepseek.balance.api.BalanceRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class BalanceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // 首次添加小组件时启动定时更新
        scheduleWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        // 所有小组件被移除时取消定时更新
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_WORK_NAME)
    }

    companion object {
        private const val WIDGET_WORK_NAME = "widget_balance_update"

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferencesManager(context)
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // 显示缓存数据
            val balance = prefs.lastTotalBalance
            val granted = prefs.lastGrantedBalance
            val toppedUp = prefs.lastToppedUpBalance
            val available = prefs.lastAvailable
            val lastUpdate = prefs.lastUpdateTime

            views.setTextViewText(R.id.widget_total_balance, "¥$balance")
            views.setTextViewText(R.id.widget_granted_amount, granted)
            views.setTextViewText(R.id.widget_topped_up_amount, toppedUp)

            // 状态指示灯
            views.setImageViewResource(
                R.id.widget_status_icon,
                if (available) R.drawable.ic_dot_green
                else R.drawable.ic_dot_red
            )

            // 最后更新时间
            val timeStr = if (lastUpdate > 0) {
                val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                "更新: ${sdf.format(java.util.Date(lastUpdate))}"
            } else {
                "暂无数据"
            }
            views.setTextViewText(R.id.widget_last_update, timeStr)

            // 点击打开主应用
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun scheduleWidgetUpdate(context: Context) {
            val prefs = PreferencesManager(context)
            val intervalMinutes = prefs.widgetUpdateInterval.coerceAtLeast(15)

            val workRequest = PeriodicWorkRequestBuilder<BalanceUpdateWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WIDGET_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
    }
}

class BalanceUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val prefs = PreferencesManager(applicationContext)
        val apiKey = prefs.apiKey

        if (apiKey.isEmpty()) return Result.retry()

        return try {
            val result = runBlocking {
                BalanceRepository().getBalance(apiKey)
            }

            result.fold(
                onSuccess = { response ->
                    val info = response.balanceInfos.firstOrNull()
                    if (info != null) {
                        prefs.lastTotalBalance = info.totalBalance
                        prefs.lastGrantedBalance = info.grantedBalance
                        prefs.lastToppedUpBalance = info.toppedUpBalance
                        prefs.lastAvailable = response.isAvailable
                        prefs.lastUpdateTime = System.currentTimeMillis()
                    }

                    // 更新所有小组件
                    val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
                    val componentName = android.content.ComponentName(
                        applicationContext, BalanceWidget::class.java
                    )
                    val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                    for (id in appWidgetIds) {
                        BalanceWidget.updateAppWidget(applicationContext, appWidgetManager, id)
                    }
                    Result.success()
                },
                onFailure = { Result.retry() }
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
