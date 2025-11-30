package com.example.bliblisearch.model

data class BaseResponse(
    val code: Int,
    val status: String,
    val data: DataResponse?
)
