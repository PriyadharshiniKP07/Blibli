package com.example.bliblisearch.model

data class Product(
    val name: String?,            // title in JSON = "name"
    val brand: String?,
    val location: String?,

    val price: Price?,            // matches JSON.price object

    val review: Review?,          // rating object
    val tags: List<String>?,      // ["REGULAR","..."]
    val badge: Badge?,            // nested object
    val soldCountTotal: Int?,     // 70

    val uspLabelsTags: List<String>?, // extra tags
    val images: List<String>?,
    val contextualPrice: Price?
// images[0] is main product image
)
