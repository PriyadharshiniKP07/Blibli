package com.example.bliblisearch.network

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val chainRequest = chain.request().newBuilder()
            .addHeader("Cache-Control", "no-cache")
            .addHeader("User-Agent", "AndroidApp/1.0")
            .addHeader("Accept", "*/*")
            .build()

        return chain.proceed(chainRequest)
    }
}
