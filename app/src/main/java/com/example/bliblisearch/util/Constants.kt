package com.example.bliblisearch.util

import com.example.bliblisearch.R
import okhttp3.logging.HttpLoggingInterceptor

object Constants {
    // ------------------- NETWORK -------------------
    const val MOCK_BASE_URL = "https://mock.local/"
    const val TIMEOUT_SECONDS = 30L

    // Log level (interceptor uses enum, so cannot be const val)
    val LOG_LEVEL = HttpLoggingInterceptor.Level.BODY

    // ------------------- NETWORK MOCK -------------------
    const val MOCK_PRODUCT_LIST_FILE = "productList.json"
    const val MOCK_PRODUCTS_PATH_KEY = "products"

    const val HTTP_OK = 200
    const val HTTP_OK_MSG = "OK"

    const val MIME_JSON = "application/json"

    // --------- REPOSITORY ERRORS ---------
    const val ERROR_FETCH_PRODUCTS = "Failed to fetch products"

    // ---------------- VIEW TYPES ----------------
    const val ITEM_VIEW = 1
    const val LOADER_VIEW = 2

    // ---------------- USP TAGS ----------------
    const val TAG_FREE_SHIPPING = "FREE_SHIPPING"
    const val TAG_2HD_1 = "2HD"
    const val TAG_2HD_2 = "2_JAM"
    const val TAG_2HD_3 = "2HOUR"
    const val TAG_2HD_4 = "2_JAM_SAMPAI"

    // ---------------- BRAND CHECK ----------------
    const val BRAND_NO_BRAND = "no brand"

    // ---------------- DRAWABLE PLACEHOLDERS ----------------
    val PLACEHOLDER_LOCATION = R.drawable.ic_location

    // ---------------- DIMENSIONS (dp) ----------------
    const val USP_TAG_WIDTH_DP = 40
    const val USP_TAG_HEIGHT_DP = 16
    const val DEFAULT_IMAGE_WIDTH_DP = 135
    const val LOCATION_PADDING_DP = 60

    // ---------------- PRICE TRIM RATIOS ----------------
    const val PRICE_RATIO_STRIKE = 0.28f
    const val PRICE_RATIO_PRICE = 0.55f

    // ---------------- SEARCH RULES ----------------
    const val MIN_SEARCH_LENGTH = 3
    const val PAGINATION_THRESHOLD = 3

    // Pagination
    const val PAGE_SIZE = 10
    const val LOAD_DELAY_MS = 2000L

    // Search filters
    const val MIN_QUERY_LENGTH = 3

    // Keywords
    const val SEARCH_FIELD_NAME = "name"
    const val SEARCH_FIELD_BRAND = "brand"
    const val SEARCH_FIELD_LOCATION = "location"

    val PLACEHOLDER_IMAGE = R.drawable.bg_image_placeholder
    const val REAL_BASE_URL = "https://www.blibli.com/"
    const val CONTEXTUAL_PRICE_ENABLED = true




}