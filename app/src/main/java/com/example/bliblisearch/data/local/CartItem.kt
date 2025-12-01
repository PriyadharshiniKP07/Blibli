package com.example.bliblisearch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val productId: String,   // use API id for duplicate check
    val productJson: String  // store entire product JSON string
)
