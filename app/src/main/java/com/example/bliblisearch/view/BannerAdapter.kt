package com.example.bliblisearch.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bliblisearch.R
import com.example.bliblisearch.databinding.ItemBannerBinding

class BannerAdapter(
    private val context: Context,
    private val items: List<String>
) : RecyclerView.Adapter<BannerAdapter.VH>() {

    inner class VH(val binding: ItemBannerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemBannerBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.binding.imgBanner.context)
            .load(items[position])
            .placeholder(R.drawable.bg_image_placeholder)
            .into(holder.binding.imgBanner)

    }


    override fun getItemCount() = items.size
}
