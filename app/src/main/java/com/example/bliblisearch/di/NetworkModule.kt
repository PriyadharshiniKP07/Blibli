package com.example.bliblisearch.di

import android.content.Context
import com.example.bliblisearch.util.Constants
import com.example.bliblisearch.network.ApiService
import com.example.bliblisearch.network.HeaderInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.inject.Named

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {



    // ---------------- HEADER INTERCEPTOR ----------------
    @Provides
    @Singleton
    fun provideHeaderInterceptor(): HeaderInterceptor {
        return HeaderInterceptor()
    }



    // ---------------- REAL OKHTTP ----------------
    @Provides
    @Singleton
    @Named("RealOkHttp")
    fun provideRealOkHttp(
        headerInterceptor: HeaderInterceptor
    ): OkHttpClient {

        val logger = HttpLoggingInterceptor().apply {
            level = Constants.LOG_LEVEL
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(headerInterceptor)
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }


    // ---------------- REAL RETROFIT ----------------
    @Provides
    @Singleton
    @Named("RealRetrofit")
    fun provideRealRetrofit(
        @Named("RealOkHttp") client: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(Constants.REAL_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }


    @Provides
    @Singleton
    @Named("RealApi")
    fun provideRealApi(
        @Named("RealRetrofit") retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)
}
