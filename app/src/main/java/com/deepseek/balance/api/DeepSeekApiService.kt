package com.deepseek.balance.api

import com.deepseek.balance.model.BalanceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface DeepSeekApiService {

    @GET("user/balance")
    suspend fun getBalance(
        @Header("Authorization") authorization: String
    ): Response<BalanceResponse>
}
