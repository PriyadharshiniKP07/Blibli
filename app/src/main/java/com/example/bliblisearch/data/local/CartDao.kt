package com.example.bliblisearch.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE userId = :user LIMIT :limit OFFSET :offset")
    suspend fun getCartPaginated(user: String, limit: Int, offset: Int): List<CartItem>

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :user")
    suspend fun getCartCount(user: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItem)

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :user AND productId = :product")
    suspend fun exists(user: String, product: String): Int

    @Query("SELECT * FROM cart_items WHERE userId = :user AND productId = :product")
    suspend fun findProduct(user: String, product: String): CartItem?

    @Delete
    suspend fun delete(item: CartItem)
}
