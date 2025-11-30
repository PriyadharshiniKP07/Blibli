package com.example.bliblisearch.view

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bliblisearch.R
import com.example.bliblisearch.databinding.ActivitySearchBinding
import com.example.bliblisearch.databinding.LayoutEmptyStateBinding
import com.example.bliblisearch.util.Constants
import com.example.bliblisearch.viewModel.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels()
    private val adapter = ProductAdapter()
    private var suppressTextWatcher = false

    private var searchJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        layoutInflater.inflate(R.layout.layout_empty_state, binding.emptyStateView, true)

        val emptyBinding = LayoutEmptyStateBinding.bind(binding.emptyStateView.getChildAt(0))

        emptyBinding.btnTryAgain.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.resetToHome()
            viewModel.clearSearchQuery()
            finish()}

            setupRecycler()
        setupObservers()
        setupSearchBox()

        // If query was sent from HomeActivity, populate and search immediately
        intent.getStringExtra("query")?.let { q ->

            suppressTextWatcher = true          // prevent text listener from triggering
            binding.etSearch.setText(q)
            binding.etSearch.setSelection(binding.etSearch.text.length)
            suppressTextWatcher = false         // re-enable listener

            viewModel.search(q)
        }
    }

    private fun setupRecycler() {
        val lm = LinearLayoutManager(this)
        binding.rvProducts.layoutManager = lm
        binding.rvProducts.adapter = adapter

        binding.rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return

                val visible = lm.childCount
                val total = lm.itemCount
                val first = lm.findFirstVisibleItemPosition()
                val nearBottom = first + visible >= total - Constants.PAGINATION_THRESHOLD

                if (nearBottom && viewModel.loadingPage.value == false) {
                    adapter.showLoading()
                    viewModel.loadNextSearchPage()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.pagedProducts.observe(this) { list ->
            adapter.updateData(list ?: emptyList())
        }

        viewModel.loadingPage.observe(this) { loading ->
            if (!loading) adapter.hideLoading()
        }

        viewModel.empty.observe(this) { empty ->
            binding.emptyStateView.visibility = if (empty) View.VISIBLE else View.GONE
            binding.rvProducts.visibility = if (empty) View.GONE else View.VISIBLE
        }
    }

//    private fun setupSearchBox() {
//        binding.etSearch.addTextChangedListener { txt ->
//            val q = txt?.toString()?.trim().orEmpty()
//
//            binding.iconClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
//
//            // If empty -> treat as cleared search and close (return to Home)
//            if (q.isEmpty()) {
//                // finish so Home onResume can reset to mock list
//                finish()
//                return@addTextChangedListener
//            }
//
//            if (q.length >= Constants.MIN_SEARCH_LENGTH) {
//                viewModel.search(q)
//            }
//        }
//
//        // Support IME 'Search' action explicitly
//        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                val q = binding.etSearch.text?.toString()?.trim().orEmpty()
//                if (q.length >= Constants.MIN_SEARCH_LENGTH) {
//                    viewModel.search(q)
//                }
//                true
//            } else {
//                false
//            }
//        }
//
//        binding.iconClear.setOnClickListener {
//            binding.etSearch.setText("")
//            // finish to go back to Home (Home onResume will reset list)
//            finish()
//        }
//    }

    private fun setupSearchBox() {

        binding.etSearch.addTextChangedListener { txt ->

            if (suppressTextWatcher) return@addTextChangedListener

            val query = txt?.toString()?.trim().orEmpty()

            binding.iconClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE

            // Cancel previous scheduled search
            searchJob?.cancel()

            // Debounce new search
            searchJob = lifecycleScope.launch {
                delay(1000)
                if (query.isEmpty()) {
                    finish()
                    return@launch
                }

                if (query.length >= Constants.MIN_SEARCH_LENGTH) {
                    viewModel.search(query)
                }
            }
        }

        binding.iconClear.setOnClickListener {
            suppressTextWatcher = true
            binding.etSearch.setText("")
            viewModel.clearSearchQuery()
            suppressTextWatcher = false
            finish()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text?.toString()?.trim().orEmpty()
                if (query.length >= Constants.MIN_SEARCH_LENGTH) {
                    viewModel.search(query)
                }
                true
            } else false
        }
    }

}
