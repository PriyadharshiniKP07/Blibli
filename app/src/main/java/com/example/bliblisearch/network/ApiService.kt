package com.example.bliblisearch.network

import com.example.bliblisearch.model.BaseResponse
import com.example.bliblisearch.model.Product
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("products")
    suspend fun getProductsMock(
        @Header("Use-Mock") useMock: String = "true"
    ): Response<List<Product>>

    @GET("backend/search/products")
    suspend fun searchProducts(
        @Query("searchTerm") search: String,
        @Query("page") page: Int,
        @Query("start") start: Int,
        @Query("merchantSearch") merchantSearch: Boolean = true,
        @Query("multiCategory") multiCategory: Boolean = true,
        @Query("intent") intent: Boolean = true,
        @Query("showFacet") showFacet: Boolean = false
    ): BaseResponse
}