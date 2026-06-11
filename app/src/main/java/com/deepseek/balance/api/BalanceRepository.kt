package com.deepseek.balance.api

import com.deepseek.balance.model.BalanceResponse

class BalanceRepository {

    suspend fun getBalance(apiKey: String): Result<BalanceResponse> {
        return try {
            val response = ApiClient.apiService.getBalance("Bearer $apiKey")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "API Key 无效，请检查后重试"
                    403 -> "没有权限访问"
                    429 -> "请求过于频繁，请稍后再试"
                    else -> "查询失败 (${response.code()}): ${response.errorBody()?.string() ?: "未知错误"}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络连接失败: ${e.localizedMessage}"))
        }
    }
}
