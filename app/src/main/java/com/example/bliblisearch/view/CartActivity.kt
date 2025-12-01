package com.example.bliblisearch.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bliblisearch.R
import com.example.bliblisearch.databinding.ActivityCartBinding
import com.example.bliblisearch.viewModel.CartViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private val viewModel: CartViewModel by viewModels()

    private lateinit var adapter: CartAdapter
    private lateinit var bannerHandler: android.os.Handler
    private var bannerRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = SharedPreferenceManager.getLoggedInUser(this)
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bannerHandler = android.os.Handler(mainLooper)

        setupRecycler()
        setupObservers()
        setupClicks()

        viewModel.initUser(user)
        viewModel.loadBanners()
    }

    override fun onResume() {
        super.onResume()
        SharedPreferenceManager.getLoggedInUser(this)?.let { viewModel.initUser(it) }
    }


    // ---------------------------- Recycler ----------------------------

    private fun setupRecycler() {
        adapter = CartAdapter(viewModel)
        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartItems.adapter = adapter

        binding.rvCartItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {

                val lm = rv.layoutManager as LinearLayoutManager
                val lastVisible = lm.findLastVisibleItemPosition()

                // No more data → remove loader
                if (!viewModel.hasMore()) {
                    hideLoaderIfShown()
                    return
                }

                // Trigger pagination only at bottom + not already loading
                if (lastVisible == adapter.itemCount - 1 && !adapter.showLoader) {
                    adapter.showLoader = true
                    adapter.notifyItemInserted(adapter.itemCount)
                    viewModel.loadNextPage()
                }
            }
        })
    }


    // ---------------------------- Observers ----------------------------

    private fun setupObservers() {

        // Cart count update always correct
        viewModel.cartCount.observe(this) { count ->
            binding.txtCartCount.text = "My Cart ($count items)"
        }

        viewModel.cartItems.observe(this) { list ->

            val empty = list.isEmpty()
            binding.emptyCart.root.visibility = if (empty) View.VISIBLE else View.GONE
            binding.rvCartItems.visibility = if (!empty) View.VISIBLE else View.GONE

            adapter.setData(list, viewModel.hasMore())

            // Stop loader if no more items
            if (!viewModel.hasMore()) hideLoaderIfShown()
        }


        // Banner updates
        viewModel.banners.observe(this) { urls ->
            if (urls.isNullOrEmpty()) return@observe

            binding.viewPagerBanner.adapter = BannerAdapter(this, urls)

            TabLayoutMediator(binding.bannerIndicator, binding.viewPagerBanner) { tab, _ ->
                tab.setCustomView(R.layout.item_banner_dot)
            }.attach()

            binding.bannerIndicator.getTabAt(0)?.customView
                ?.findViewById<ImageView>(R.id.imageView)
                ?.setImageResource(R.drawable.banner_dot_selected)

            binding.bannerIndicator.addOnTabSelectedListener(object :
                com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    (tab?.customView as? ImageView)?.setImageResource(R.drawable.banner_dot_selected)
                }

                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    (tab?.customView as? ImageView)?.setImageResource(R.drawable.banner_dot)
                }

                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            })

            startAutoBannerScroll()
        }

        // Toast listener
        viewModel.toast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }


    // ---------------------------- Banner Scroll ----------------------------

    private fun startAutoBannerScroll() {
        val vp = binding.viewPagerBanner
        val adapter = vp.adapter ?: return

        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }

        bannerRunnable = object : Runnable {
            override fun run() {
                val next = (vp.currentItem + 1) % adapter.itemCount
                vp.setCurrentItem(next, true)
                bannerHandler.postDelayed(this, 3000)
            }
        }

        bannerHandler.postDelayed(bannerRunnable!!, 3000)

        vp.registerOnPageChangeCallback(object :
            androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bannerRunnable?.let {
                    bannerHandler.removeCallbacks(it)
                    bannerHandler.postDelayed(it, 3000)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }
    }


    // ---------------------------- Click Actions ----------------------------

    private fun setupClicks() {
        binding.clickSearch.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            SharedPreferenceManager.clearLoggedInUser(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    // ---------------------------- Loader Helpers ----------------------------

    private fun hideLoaderIfShown() {
        if (adapter.showLoader) {
            adapter.showLoader = false
            notifyLoaderRemoved()
        }
    }

    private fun notifyLoaderRemoved() {
        val position = adapter.itemCount
        if (position >= 0) adapter.notifyItemRemoved(position)
    }
}
