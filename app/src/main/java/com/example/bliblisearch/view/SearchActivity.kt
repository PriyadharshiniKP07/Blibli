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
            suppressTextWatcher = true
            binding.etSearch.setText("")
            suppressTextWatcher = false
            updateClearIcon()
            viewModel.resetToDefaultSearch()}

            setupRecycler()
        setupObservers()
        setupSearchBox()

//        // If query was sent from HomeActivity, populate and search immediately
//        intent.getStringExtra("query")?.let { q ->
//
//            suppressTextWatcher = true          // prevent text listener from triggering
//            binding.etSearch.setText(q)
//            binding.etSearch.setSelection(binding.etSearch.text.length)
//            suppressTextWatcher = false         // re-enable listener
//
//            viewModel.search(q)
//        }

        if (savedInstanceState == null) {
            viewModel.resetToDefaultSearch()
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

        viewModel.pagedProducts.observe(this) { newList ->

            val wasEmpty = adapter.itemCount == 0
            val isResetSearch = viewModel.isNewSearchTriggered // <-- we’ll set this flag below

            adapter.updateData(newList ?: emptyList())

            // 👇 Scroll to top only if new search or reset, not pagination
            if (isResetSearch || wasEmpty) {
                binding.rvProducts.post {
                    binding.rvProducts.scrollToPosition(0)
                }
                viewModel.isNewSearchTriggered = false
            }
        }

        viewModel.loadingPage.observe(this) { loading ->
            if (!loading) adapter.hideLoading()
        }

        viewModel.empty.observe(this) { empty ->
            binding.emptyStateView.visibility = if (empty) View.VISIBLE else View.GONE
            binding.rvProducts.visibility = if (empty) View.GONE else View.VISIBLE
        }
    }



    private fun setupSearchBox() {

        binding.etSearch.addTextChangedListener { txt ->

            if (suppressTextWatcher) return@addTextChangedListener

            val query = txt?.toString()?.trim().orEmpty()

            updateClearIcon()

            // Cancel previous scheduled search
            searchJob?.cancel()

            // Debounce new search
            searchJob = lifecycleScope.launch {
                delay(1000)
                if (query.isEmpty()) {
                    viewModel.resetToDefaultSearch()
                }

                if (query.length >= Constants.MIN_SEARCH_LENGTH) {
                    viewModel.search(query)
                }
            }
        }

        binding.iconClear.setOnClickListener {
            suppressTextWatcher = true
            binding.etSearch.setText("")
            suppressTextWatcher = false
            searchJob?.cancel()
            updateClearIcon()
            viewModel.resetToDefaultSearch()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text?.toString()?.trim().orEmpty()
                if (query.length >= Constants.MIN_SEARCH_LENGTH) {
                    viewModel.search(query)
                }
                if(query.isEmpty()) {
                    viewModel.resetToDefaultSearch()
                }
                true
            } else false
        }
    }
    private fun updateClearIcon() {
        binding.iconClear.visibility =
            if (binding.etSearch.text?.isNotEmpty() == true) View.VISIBLE else View.GONE
    }


}
