package com.example.bliblisearch.repository

import com.example.bliblisearch.data.local.CartDao
import com.example.bliblisearch.data.local.CartItem
import com.example.bliblisearch.model.Banner
import com.example.bliblisearch.network.ApiService
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val dao: CartDao,
    private val api: ApiService
) {

    /**
     * Load banners from real API safely
     */
    suspend fun getBanners(): List<Banner> {
        return try {
            val response = api.getHomeBanners()

            response.data
                ?.flatMap { it.blocks ?: emptyList() }
                ?.filter { it.id == "MAIN_CAROUSEL" }
                ?.flatMap { it.components ?: emptyList() }
                ?.flatMap { it.parameters ?: emptyList() }
                ?.filter { !it.image.isNullOrBlank() }
                ?.map { param ->
                    Banner(
                        image = param.image.orEmpty(),
                        url = param.url.orEmpty()
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun addToCart(user: String, productId: String, json: String): Boolean {
        val exists = dao.exists(user, productId)
        if (exists > 0) return false

        dao.insert(
            CartItem(
                userId = user,
                productId = productId,
                productJson = json
            )
        )
        return true
    }

    /**
     * Check if product already exists
     */
    suspend fun exists(user: String, productId: String): Boolean {
        return dao.exists(user, productId) > 0
    }

    /**
     * For direct item insert (used internally)
     */
    suspend fun insertItem(item: CartItem) {
        dao.insert(item)
    }

    /**
     * Paginated DB fetch
     */
    suspend fun getCart(user: String, limit: Int, offset: Int): List<CartItem> {
        return dao.getCartPaginated(user, limit, offset)
    }

    /**
     * Delete cart item
     */
    suspend fun delete(item: CartItem) {
        dao.delete(item)
    }

    /**
     * Get total count for UI badge
     */
    suspend fun count(user: String): Int {
        return dao.getCartCount(user)
    }
}
