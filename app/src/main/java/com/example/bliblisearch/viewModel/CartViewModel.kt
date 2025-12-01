package com.example.bliblisearch.viewModel

import androidx.lifecycle.*
import com.example.bliblisearch.data.local.CartItem
import com.example.bliblisearch.model.Product
import com.example.bliblisearch.repository.CartRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repo: CartRepository
) : ViewModel() {

    private var user: String = ""

    private val PAGE_SIZE = 4
    private var offset = 0
    private var hasMoreItems = true
    private var isLoading = false

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _cartCount = MutableLiveData(0)
    val cartCount: LiveData<Int> = _cartCount

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    private val _banners = MutableLiveData<List<String>>()
    val banners: LiveData<List<String>> = _banners


    fun initUser(email: String) {
        user = email
        resetPagination()
        loadNextPage()
    }

    fun hasMore(): Boolean = hasMoreItems


    private fun resetPagination() {
        offset = 0
        hasMoreItems = true
        isLoading = false
        _cartItems.value = emptyList()
    }


    /** ---------------------- PAGINATION FIX ---------------------- */
    fun loadNextPage() {
        if (isLoading || !hasMoreItems) return

        isLoading = true

        viewModelScope.launch {

            val newItems = repo.getCart(user, PAGE_SIZE, offset)

            if (newItems.isEmpty()) {
                hasMoreItems = false
            } else {
                val updated = _cartItems.value.orEmpty().toMutableList()
                updated.addAll(newItems)
                _cartItems.value = updated

                offset += newItems.size
                hasMoreItems = newItems.size == PAGE_SIZE
            }

            _cartCount.value = repo.count(user)
            isLoading = false
        }
    }


    /** ---------------------- ADD TO CART FIX ---------------------- */
    fun addItemToCart(product: Product) {
        viewModelScope.launch {

            val productId = product.id ?: product.name.orEmpty()
            val json = Gson().toJson(product)

            // Prevent duplicates
            if (repo.exists(user, productId)) {
                _toast.value = "Product already exists"
                return@launch
            }

            val newItem = CartItem(
                userId = user,
                productId = productId,
                productJson = json
            )

            repo.insertItem(newItem)

            // Insert at top
            val updated = _cartItems.value.orEmpty().toMutableList()
            updated.add(0, newItem)
            _cartItems.value = updated

            _cartCount.value = repo.count(user)
            _toast.value = "Added to cart!"

            // Auto pagination load if needed
            if (updated.size < PAGE_SIZE && hasMoreItems) {
                loadNextPage()
            }
        }
    }



    /** ---------------------- DELETE ITEM ---------------------- */
    fun delete(item: CartItem) {
        viewModelScope.launch {
            repo.delete(item)

            val updated = _cartItems.value.orEmpty().toMutableList()
            updated.remove(item)
            _cartItems.value = updated

            _cartCount.value = repo.count(user)

            _toast.value = "Removed"

            // If list is short → fill with more items
            if (updated.size < PAGE_SIZE && hasMoreItems) {
                loadNextPage()
            }
        }
    }


    /** ---------------------- OPTIONAL: RELOAD ---------------------- */
    private fun refreshCart() {
        viewModelScope.launch {
            resetPagination()
            loadNextPage()
        }
    }


    /** ---------------------- BANNER LOADING ---------------------- */
    fun loadBanners() {
        viewModelScope.launch {
            try {
                val bannersFromApi = repo.getBanners()

                val urls = bannersFromApi.mapNotNull {
                    it.image.takeIf { img -> img.isNotBlank() }
                }

                _banners.postValue(urls)

            } catch (e: Exception) {
                _toast.postValue("Failed to load banners")
            }
        }
    }
}
