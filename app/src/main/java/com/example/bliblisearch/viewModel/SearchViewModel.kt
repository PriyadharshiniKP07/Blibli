package com.example.bliblisearch.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bliblisearch.model.Product
import com.example.bliblisearch.repository.ProductRepository
import com.example.bliblisearch.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: ProductRepository
) : ViewModel() {

    private var currentQuery = ""
    private var currentPage = 1
    private var currentStart = 0
    private var totalPages = 1

    private val results = mutableListOf<Product>()

    private val pageSize = Constants.PAGE_SIZE

    private val _pagedProducts = MutableLiveData<List<Product>>(emptyList())
    val pagedProducts: LiveData<List<Product>> = _pagedProducts

    private val _loadingPage = MutableLiveData(false)
    val loadingPage: LiveData<Boolean> = _loadingPage

    private val _empty = MutableLiveData(false)
    val empty: LiveData<Boolean> = _empty

    /** Start a fresh search. If query is empty, caller should handle showing home/clearing. */
    fun search(query: String) {
        val q = query.trim()
        if (q == currentQuery) return

        currentQuery = q
        currentPage = 1
        currentStart = 0
        totalPages = 1
        results.clear()

        if (q.isEmpty()) {
            // treat as cleared search
            clear()
            return
        }

        fetch(reset = true)
    }

    fun loadNextSearchPage() {
        if (_loadingPage.value == true) return
        if (currentPage >= totalPages) return

        currentPage++
        currentStart = (currentPage - 1) * pageSize
        fetch(reset = false)
    }

    /** Clear all search state and show empty list (caller can finish() to return to Home) */
    fun clear() {
        currentQuery = ""
        currentPage = 1
        currentStart = 0
        totalPages = 1
        results.clear()

        _pagedProducts.value = emptyList()
        _empty.value = true
        _loadingPage.value = false
    }

    private fun fetch(reset: Boolean) {
        viewModelScope.launch {
            try {
                _loadingPage.value = true

                // perform real API call via repository
                val response = repo.searchProducts(
                    search = currentQuery,
                    page = currentPage,
                    start = currentStart
                )

                val newProducts = response.data?.products ?: emptyList()
                val paging = response.data?.paging
                totalPages = paging?.total_page ?: 1

                if (reset) results.clear()
                results.addAll(newProducts)

                _pagedProducts.value = results.toList()
                _empty.value = results.isEmpty()

            } catch (t: Throwable) {
                if (reset) {
                    results.clear()
                    _pagedProducts.value = emptyList()
                    _empty.value = true
                }
            } finally {
                _loadingPage.value = false
            }
        }
    }

    fun clearSearchQuery() {
        currentQuery = ""
    }
  fun resetToHome() {
      // Clear stored search value if any
      currentQuery = ""

      viewModelScope.launch {
          // Emit home data again (mock list from repository)
          val homeItems = repo.fetchProducts()
          _pagedProducts.value = homeItems
      }
  }

}
