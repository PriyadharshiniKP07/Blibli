package com.example.bliblisearch.model

data class DataResponse(
    val searchTerm: String?,
    val products: List<Product>?,
    val paging: Paging?
)
