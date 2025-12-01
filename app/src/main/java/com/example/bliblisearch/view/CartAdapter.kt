package com.example.bliblisearch.view

import android.content.res.Resources
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bliblisearch.R
import com.example.bliblisearch.data.local.CartItem
import com.example.bliblisearch.databinding.ItemLoaderBinding
import com.example.bliblisearch.databinding.ItemProductBinding
import com.example.bliblisearch.model.Product
import com.example.bliblisearch.util.Constants
import com.example.bliblisearch.viewModel.CartViewModel
import com.google.gson.Gson

class CartAdapter(
    private val viewModel: CartViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<CartItem>()
    var showLoader = false

    private val TYPE_ITEM = 1
    private val TYPE_LOADER = 2

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------
    fun setData(list: List<CartItem>, hasMore: Boolean) {
        items.clear()
        items.addAll(list)
        showLoader = hasMore
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int =
        items.size + if (showLoader) 1 else 0

    override fun getItemViewType(position: Int): Int =
        if (position == items.size && showLoader) TYPE_LOADER else TYPE_ITEM

    inner class ItemVH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)
    inner class LoaderVH(val b: ItemLoaderBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == TYPE_ITEM) {
            ItemVH(
                ItemProductBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            LoaderVH(
                ItemLoaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoaderVH) return

        val cartItem = items[position]
        val product = Gson().fromJson(cartItem.productJson, Product::class.java)
        val b = (holder as ItemVH).b

        // =====================================================================================
        // IMAGE
        // =====================================================================================
        val rawImageUrl = product.images?.firstOrNull()
        val imageUrl = rawImageUrl?.takeIf {
            it.isNotBlank() && it.lowercase() != "none"
        }

        Glide.with(b.imgProduct.context)
            .load(imageUrl)
            .placeholder(R.drawable.bg_image_placeholder)
            .into(b.imgProduct)

        // =====================================================================================
        // USP TAGS
        // =====================================================================================
        b.layoutUspTags.removeAllViews()
        val uspTags = product.uspLabelsTags ?: emptyList()

        for (tag in uspTags) {
            val iconRes = when (tag.uppercase()) {
                Constants.TAG_FREE_SHIPPING -> R.drawable.ic_tag_free_shipping
                Constants.TAG_2HD_1,
                Constants.TAG_2HD_2,
                Constants.TAG_2HD_3,
                Constants.TAG_2HD_4 -> R.drawable.ic_tag_2hd
                else -> null
            }

            if (iconRes != null) {
                val iv = ImageView(b.root.context).apply {
                    setImageResource(iconRes)
                    scaleType = ImageView.ScaleType.FIT_XY
                    layoutParams = LinearLayout.LayoutParams(
                        Constants.USP_TAG_WIDTH_DP.dp,
                        Constants.USP_TAG_HEIGHT_DP.dp
                    )
                }
                b.layoutUspTags.addView(iv)
            }
        }

        // =====================================================================================
        // TITLE
        // =====================================================================================
        b.txtTitle.text = product.name.orEmpty()

        // =====================================================================================
        // PRICE (same as ProductAdapter)
        // =====================================================================================
        val priceObj = product.contextualPrice ?: product.price
        val priceDisplay = priceObj?.priceDisplay
        val strike = priceObj?.strikeThroughPriceDisplay
        val discount = priceObj?.discount ?: 0

        if (!priceDisplay.isNullOrBlank()) {
            b.layoutPrice.visibility = View.VISIBLE

            b.txtPrice.text = priceDisplay
            b.txtStrike.text = strike.orEmpty()
            b.txtDiscount.text = if (discount > 0) "$discount%" else ""

            b.txtStrike.visibility =
                if (!strike.isNullOrBlank() && discount > 0) View.VISIBLE else View.GONE
            b.txtDiscount.visibility =
                if (discount > 0) View.VISIBLE else View.GONE

            b.txtStrike.paintFlags =
                if (!strike.isNullOrBlank() && discount > 0)
                    b.txtStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else
                    b.txtStrike.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // Trim like in ProductAdapter
            b.root.post {
                trimPriceRowProperly(b, priceDisplay, strike, discount)
            }
        } else {
            b.layoutPrice.visibility = View.GONE
        }

        // =====================================================================================
        // FREE SHIPPING
        // =====================================================================================
        b.txtFree.visibility =
            if (product.tags?.contains(Constants.TAG_FREE_SHIPPING) == true)
                View.VISIBLE else View.GONE

        // =====================================================================================
        // RATING
        // =====================================================================================
        val review = product.review
        val absRating = review?.absoluteRating
        val sellerRating = review?.sellerRating

        when {
            absRating != null && absRating > 0.0 -> {
                b.layoutRating.visibility = View.VISIBLE
                b.imgRatingIcon.setImageResource(R.drawable.ic_star)
                b.txtRatingValue.text = absRating.toString()
            }

            sellerRating != null && sellerRating > 0.0 -> {
                b.layoutRating.visibility = View.VISIBLE
                b.imgRatingIcon.setImageResource(R.drawable.ic_homestar)
                b.txtRatingValue.text = "$sellerRating rating seller"
            }

            else -> b.layoutRating.visibility = View.GONE
        }

        // =====================================================================================
        // BOTTOM ROW: VERIFIED / BADGE / LOCATION
        // =====================================================================================
        val brand = product.brand?.lowercase()
        b.imgVerified.visibility =
            if (brand != null && brand != Constants.BRAND_NO_BRAND) View.VISIBLE else View.GONE

        val rawBadgeUrl = product.badge?.merchantBadgeUrl
        val badgeUrl = rawBadgeUrl?.takeIf {
            it.isNotBlank() && it.lowercase() != "none"
        }

        val hasBadge = !badgeUrl.isNullOrBlank()
        b.imgMerchantBadge.visibility = if (hasBadge) View.VISIBLE else View.GONE

        if (hasBadge) {
            Glide.with(b.imgMerchantBadge.context)
                .load(badgeUrl)
                .placeholder(Constants.PLACEHOLDER_LOCATION)
                .into(b.imgMerchantBadge)
        }

        val loc = product.location
        val hasLocation = !loc.isNullOrBlank()

        b.txtLocation.text = loc.orEmpty()
        b.txtLocation.visibility = if (hasLocation) View.VISIBLE else View.GONE
        b.imgLocationIcon.visibility = if (hasLocation && !hasBadge) View.VISIBLE else View.GONE

        b.root.post {
            trimLocationProperly(b, loc)
        }

        b.layoutBottom.visibility =
            if (b.imgVerified.visibility == View.VISIBLE ||
                b.imgMerchantBadge.visibility == View.VISIBLE ||
                b.txtLocation.visibility == View.VISIBLE
            ) View.VISIBLE else View.GONE

        // =====================================================================================
        // BUTTONS: HIDE INFO, SHOW DELETE
        // =====================================================================================
        b.btnShowInfo.visibility = View.GONE
        b.btnDelete.visibility = View.VISIBLE

        b.btnDelete.setOnClickListener {
            viewModel.delete(cartItem)
            Toast.makeText(b.root.context, "Removed", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================================================
    // Helpers copied from ProductAdapter so trimming & layout behave the same
    // =========================================================================================
    private val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun trimToFitSingleLine(tv: TextView, maxWidthPx: Int, original: String): String {
        tv.text = original
        tv.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        if (tv.measuredWidth <= maxWidthPx) return original

        var trimmed = original
        while (trimmed.length > 4) {
            trimmed = trimmed.dropLast(1)
            val temp = "$trimmed…"
            tv.text = temp
            tv.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            if (tv.measuredWidth <= maxWidthPx) return temp
        }
        return original
    }

    private fun trimPriceRowProperly(
        b: ItemProductBinding,
        price: String?,
        strike: String?,
        discount: Int
    ) {
        if (price.isNullOrBlank()) return

        val parentWidth = b.root.width.takeIf { it > 0 } ?: return
        val imgW = b.imgProduct.width.takeIf { it > 0 } ?: Constants.DEFAULT_IMAGE_WIDTH_DP.dp

        val available = parentWidth - imgW - Constants.LOCATION_PADDING_DP.dp
        if (available <= 0) return

        if (!strike.isNullOrBlank() && discount > 0 && b.txtStrike.visibility == View.VISIBLE) {
            val strikeMax = (available * Constants.PRICE_RATIO_STRIKE).toInt()
            b.txtStrike.text = trimToFitSingleLine(b.txtStrike, strikeMax, strike)
        }

        val priceMax = (available * Constants.PRICE_RATIO_PRICE).toInt()
        b.txtPrice.text = trimToFitSingleLine(b.txtPrice, priceMax, price)
    }

    private fun trimLocationProperly(b: ItemProductBinding, location: String?) {
        if (location.isNullOrBlank()) return

        val parentWidth = b.root.width.takeIf { it > 0 } ?: return
        val imgW = b.imgProduct.width.takeIf { it > 0 } ?: Constants.DEFAULT_IMAGE_WIDTH_DP.dp

        val available = parentWidth - imgW - Constants.LOCATION_PADDING_DP.dp
        if (available <= 0) return

        b.txtLocation.text = trimToFitSingleLine(b.txtLocation, available, location)
    }
}
