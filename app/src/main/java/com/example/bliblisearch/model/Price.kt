package com.example.bliblisearch.model

data class Price(
    val priceDisplay: String?,
    val strikeThroughPriceDisplay: String?,
    val discount: Int?,
    val discountPrice: Double?,
    val minPrice: Double?,
    val offerPriceDisplay: String?,
    val isPriceRange: Boolean?,
    val listPrice: Int?,
    val salePrice: Int?
)
