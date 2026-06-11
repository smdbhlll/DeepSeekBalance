package com.deepseek.balance

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var lastTotalBalance: String
        get() = prefs.getString(KEY_LAST_BALANCE, "0.00") ?: "0.00"
        set(value) = prefs.edit().putString(KEY_LAST_BALANCE, value).apply()

    var lastGrantedBalance: String
        get() = prefs.getString(KEY_LAST_GRANTED, "0.00") ?: "0.00"
        set(value) = prefs.edit().putString(KEY_LAST_GRANTED, value).apply()

    var lastToppedUpBalance: String
        get() = prefs.getString(KEY_LAST_TOPPED, "0.00") ?: "0.00"
        set(value) = prefs.edit().putString(KEY_LAST_TOPPED, value).apply()

    var lastAvailable: Boolean
        get() = prefs.getBoolean(KEY_LAST_AVAILABLE, true)
        set(value) = prefs.edit().putBoolean(KEY_LAST_AVAILABLE, value).apply()

    var lastUpdateTime: Long
        get() = prefs.getLong(KEY_LAST_UPDATE, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UPDATE, value).apply()

    var widgetUpdateInterval: Int
        get() = prefs.getInt(KEY_WIDGET_INTERVAL, 30)
        set(value) = prefs.edit().putInt(KEY_WIDGET_INTERVAL, value).apply()

    companion object {
        private const val PREFS_NAME = "deepseek_balance_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_LAST_BALANCE = "last_total_balance"
        private const val KEY_LAST_GRANTED = "last_granted_balance"
        private const val KEY_LAST_TOPPED = "last_topped_up_balance"
        private const val KEY_LAST_AVAILABLE = "last_available"
        private const val KEY_LAST_UPDATE = "last_update_time"
        private const val KEY_WIDGET_INTERVAL = "widget_update_interval"
    }
}
