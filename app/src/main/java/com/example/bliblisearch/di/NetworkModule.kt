package com.example.bliblisearch.di

import android.content.Context
import com.example.bliblisearch.util.Constants
import com.example.bliblisearch.network.ApiService
import com.example.bliblisearch.network.HeaderInterceptor
import com.example.bliblisearch.network.MockInterceptor
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

    // ---------------- MOCK INTERCEPTOR ----------------
    @Provides
    fun provideMockInterceptor(@ApplicationContext context: Context): MockInterceptor {
        return MockInterceptor(context)
    }

    // ---------------- HEADER INTERCEPTOR ----------------
    @Provides
    @Singleton
    fun provideHeaderInterceptor(): HeaderInterceptor {
        return HeaderInterceptor()
    }

    // ---------------- MOCK OKHTTP ----------------
    @Provides
    @Singleton
    @Named("MockOkHttp")
    fun provideMockOkHttp(
        headerInterceptor: HeaderInterceptor,
        mockInterceptor: MockInterceptor
    ): OkHttpClient {

        val logger = HttpLoggingInterceptor().apply {
            level = Constants.LOG_LEVEL
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .addInterceptor(headerInterceptor)
            .addInterceptor(mockInterceptor)
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
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

    // ---------------- MOCK RETROFIT ----------------
    @Provides
    @Singleton
    @Named("MockRetrofit")
    fun provideMockRetrofit(
        @Named("MockOkHttp") client: OkHttpClient
    ): Retrofit {

        return Retrofit.Builder()
            .baseUrl(Constants.MOCK_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
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

    // ---------------- API SERVICES ----------------

    @Provides
    @Singleton
    @Named("MockApi")
    fun provideMockApi(
        @Named("MockRetrofit") retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    @Named("RealApi")
    fun provideRealApi(
        @Named("RealRetrofit") retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)
}
