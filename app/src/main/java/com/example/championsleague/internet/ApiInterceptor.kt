package com.example.championsleague.internet

import okhttp3.Interceptor
import okhttp3.Response

class ApiInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authRequest = chain.request().newBuilder()
                .addHeader(Repository.AUTH_HEADER, Repository.AUTH_TOKEN).build()
        return chain.proceed(authRequest)
    }
}