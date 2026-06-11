package com.deepseek.balance

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.deepseek.balance.api.BalanceRepository
import com.deepseek.balance.databinding.ActivityMainBinding
import com.deepseek.balance.model.BalanceResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager
    private val repository = BalanceRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)

        setupViews()
        checkApiKey()
    }

    private fun setupViews() {
        // 下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            refreshBalance()
        }

        // 点击设置 API Key
        binding.cardSettings.setOnClickListener {
            showApiKeyDialog()
        }

        // 点击刷新
        binding.btnRefresh.setOnClickListener {
            refreshBalance()
        }

        // 小组件更新时间设置
        binding.cardWidgetSettings.setOnClickListener {
            showWidgetIntervalDialog()
        }
    }

    private fun checkApiKey() {
        if (prefs.apiKey.isEmpty()) {
            showApiKeyDialog()
        } else {
            refreshBalance()
        }
    }

    private fun showApiKeyDialog() {
        val editText = TextInputEditText(this).apply {
            setText(prefs.apiKey)
            hint = "sk-..."
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("设置 API Key")
            .setMessage("请输入你的 DeepSeek API Key")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val key = editText.text.toString().trim()
                if (key.isNotEmpty()) {
                    prefs.apiKey = key
                    refreshBalance()
                } else {
                    Toast.makeText(this, "API Key 不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showWidgetIntervalDialog() {
        val options = arrayOf("15分钟", "30分钟", "1小时", "2小时", "6小时")
        val values = arrayOf(15, 30, 60, 120, 360)
        val currentIndex = values.indexOf(prefs.widgetUpdateInterval).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle("小组件更新间隔")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                prefs.widgetUpdateInterval = values[which]
                BalanceWidget.scheduleWidgetUpdate(this)
                dialog.dismiss()
                Toast.makeText(this, "已设置为每 ${options[which]} 更新", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun refreshBalance() {
        val apiKey = prefs.apiKey
        if (apiKey.isEmpty()) {
            showApiKeyDialog()
            return
        }

        binding.swipeRefresh.isRefreshing = true
        binding.tvError.visibility = android.view.View.GONE
        binding.contentGroup.visibility = android.view.View.GONE

        lifecycleScope.launch {
            val result = repository.getBalance(apiKey)
            binding.swipeRefresh.isRefreshing = false

            result.fold(
                onSuccess = { response ->
                    updateUI(response)
                },
                onFailure = { error ->
                    showError(error.message ?: "查询失败")
                }
            )
        }
    }

    private fun updateUI(response: BalanceResponse) {
        binding.contentGroup.visibility = android.view.View.VISIBLE
        binding.tvError.visibility = android.view.View.GONE

        val info = response.balanceInfos.firstOrNull() ?: return

        // 更新主余额
        binding.tvTotalBalance.text = "¥${info.totalBalance}"
        binding.tvGrantedBalance.text = info.grantedBalance
        binding.tvToppedUpBalance.text = info.toppedUpBalance

        // 账户状态
        binding.tvStatus.text = if (response.isAvailable) "正常" else "受限"
        binding.tvStatus.setTextColor(
            if (response.isAvailable) getColor(R.color.status_available)
            else getColor(R.color.status_unavailable)
        )

        // 更新图标状态
        binding.ivStatusIcon.setImageResource(
            if (response.isAvailable) R.drawable.ic_check_circle
            else R.drawable.ic_warning
        )

        // 更新时间
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        binding.tvLastUpdate.text = "最后更新: $now"

        // 保存到本地
        prefs.lastTotalBalance = info.totalBalance
        prefs.lastGrantedBalance = info.grantedBalance
        prefs.lastToppedUpBalance = info.toppedUpBalance
        prefs.lastAvailable = response.isAvailable
        prefs.lastUpdateTime = System.currentTimeMillis()
    }

    private fun showError(message: String) {
        binding.contentGroup.visibility = android.view.View.GONE
        binding.tvError.visibility = android.view.View.VISIBLE
        binding.tvError.text = message
    }
}
