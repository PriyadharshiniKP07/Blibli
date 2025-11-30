package com.example.bliblisearch.network

import android.content.Context
import com.example.bliblisearch.util.Constants
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.InputStreamReader

class MockInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        // Only mock when explicitly requested
        val useMock = request.header("Use-Mock")?.toBoolean() == true

        if (useMock && request.url.encodedPath.contains(Constants.MOCK_PRODUCTS_PATH_KEY)){

            // Read JSON file from assets/productList.json
            val input = context.assets.open(Constants.MOCK_PRODUCT_LIST_FILE)
            val bodyStr = InputStreamReader(input).use { it.readText() }

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(Constants.HTTP_OK)
                .message(Constants.HTTP_OK_MSG)
                .body(bodyStr.toResponseBody(Constants.MIME_JSON.toMediaType()))
                .build()
        }

        // Pass through for any other requests
        return chain.proceed(request)
    }
}
