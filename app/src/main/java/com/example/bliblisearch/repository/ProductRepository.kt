package com.example.bliblisearch.repository

import com.example.bliblisearch.model.Product
import com.example.bliblisearch.network.ApiService
import javax.inject.Inject
import javax.inject.Named

class ProductRepository @Inject constructor(
    @Named("MockApi") private val mockApi: ApiService,
    @Named("RealApi") private val realApi: ApiService
) {

    // ---------------- INITIAL MOCK LIST ----------------
    suspend fun fetchProducts(): List<Product> {
        val response = mockApi.getProductsMock()
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to load mock products")
        }
    }

    // ---------------- SEARCH USING REAL API ----------------
    suspend fun searchProducts(
        search: String,
        page: Int,
        start: Int
    ) = realApi.searchProducts(search, page, start)
}
