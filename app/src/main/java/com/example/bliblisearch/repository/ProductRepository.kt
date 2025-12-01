package com.example.bliblisearch.repository

import com.example.bliblisearch.model.Product
import com.example.bliblisearch.network.ApiService
import javax.inject.Inject
import javax.inject.Named

class ProductRepository @Inject constructor(
private val realApi: ApiService
) {


    // ---------------- SEARCH USING REAL API ----------------
    suspend fun searchProducts(
        search: String,
        page: Int,
        start: Int
    ) = realApi.searchProducts(search, page, start)
}
