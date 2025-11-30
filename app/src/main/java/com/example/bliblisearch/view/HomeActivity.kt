package com.example.bliblisearch.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bliblisearch.databinding.ActivityHomeBinding
import com.example.bliblisearch.util.Constants
import com.example.bliblisearch.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupObservers()
        setupSearchBox()

        if (savedInstanceState == null) {
            viewModel.loadMockProducts()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etSearch.setText("")
        viewModel.resetToHome()
        binding.rvProducts.post {
            binding.rvProducts.scrollToPosition(0)
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

                val isNearBottom = first + visible >= total - Constants.PAGINATION_THRESHOLD

                if (isNearBottom && viewModel.loadingPage.value == false) {
                    adapter.showLoading()
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {

        viewModel.pagedProducts.observe(this) { newList ->
            val oldSize = adapter.itemCount - if (adapter.isLoading) 1 else 0

            if (newList.size > oldSize) {
                // APPEND
                val diff = newList.takeLast(newList.size - oldSize)
                adapter.addMoreProducts(diff)
            } else {
                // FIRST LOAD / RESET
                adapter.updateData(newList)
            }
        }

        viewModel.loadingPage.observe(this) { loading ->
            if (!loading) adapter.hideLoading()
        }

        viewModel.empty.observe(this) { isEmpty ->
            binding.emptyStateView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvProducts.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun setupSearchBox() {
        binding.etSearch.addTextChangedListener { txt ->
            val q = txt.toString().trim()

            binding.iconClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE

            if (q.isEmpty()) {
                viewModel.resetToHome()
                return@addTextChangedListener
            }

            if (q.length >= Constants.MIN_SEARCH_LENGTH) {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("query", q)
                startActivity(intent)
            }
        }

        binding.iconClear.setOnClickListener {
            binding.etSearch.setText("")
            viewModel.resetToHome()
        }
    }
}
