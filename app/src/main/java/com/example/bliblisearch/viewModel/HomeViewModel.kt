package com.example.bliblisearch.viewModel

import androidx.lifecycle.*
import com.example.bliblisearch.model.Product
import com.example.bliblisearch.repository.ProductRepository
import com.example.bliblisearch.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ProductRepository
) : ViewModel() {

    private var fullList: List<Product> = emptyList()
    private var currentIndex = 0

    private val pageSize = Constants.PAGE_SIZE

    private val _pagedProducts = MutableLiveData<List<Product>>(emptyList())
    val pagedProducts: LiveData<List<Product>> = _pagedProducts

    private val _loadingPage = MutableLiveData(false)
    val loadingPage: LiveData<Boolean> = _loadingPage

    private val _empty = MutableLiveData(false)
    val empty: LiveData<Boolean> = _empty


    /** Load ONLY first page */
    fun loadMockProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            _loadingPage.postValue(true)
            try {
                fullList = repo.fetchProducts()

                // Start with ONLY first 10
                currentIndex = pageSize.coerceAtMost(fullList.size)

                _pagedProducts.postValue(fullList.take(currentIndex))
                _empty.postValue(fullList.isEmpty())

            } catch (e: Exception) {
                _pagedProducts.postValue(emptyList())
                _empty.postValue(true)
            } finally {
                _loadingPage.postValue(false)
            }
        }
    }

    /** Load NEXT page */
    fun loadNextPage() {
        if (_loadingPage.value == true) return
        if (currentIndex >= fullList.size) {
            _loadingPage.postValue(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _loadingPage.postValue(true)
            delay(1500)
            try {
                val nextIndex = (currentIndex + pageSize).coerceAtMost(fullList.size)

                val newChunk = fullList.subList(currentIndex, nextIndex)

                currentIndex = nextIndex

                // Append to previous list
                val updatedList = (_pagedProducts.value ?: emptyList()) + newChunk
                _pagedProducts.postValue(updatedList)

            } finally {
                _loadingPage.postValue(false)
            }
        }
    }

    fun resetToHome() {
        loadMockProducts()
    }
}
