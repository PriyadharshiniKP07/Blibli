package com.example.bliblisearch.view

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bliblisearch.R
import com.example.bliblisearch.databinding.DialogProductInfoBinding
import com.example.bliblisearch.databinding.ItemLoaderBinding
import com.example.bliblisearch.databinding.ItemProductBinding
import com.example.bliblisearch.model.Product
import com.example.bliblisearch.util.Constants

class ProductAdapter(private val onAddToCart: (Product) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Product>()
    var isLoading = false

    override fun getItemViewType(position: Int): Int {
        return if (position == items.size && isLoading) Constants.LOADER_VIEW else Constants.ITEM_VIEW
    }

    override fun getItemCount(): Int = if (isLoading) items.size + 1 else items.size

    // RESET / INITIAL LOAD
    fun updateData(list: List<Product>) {
        isLoading = false
        items.clear()
        val filtered = list.filter { isValid(it) }
        items.addAll(filtered)
        notifyDataSetChanged()
    }


    // PAGINATION APPEND
    fun addMoreProducts(list: List<Product>) {
        val validList = list.filter { isValid(it) }
        val start = items.size
        items.addAll(validList)
        notifyItemRangeInserted(start, validList.size)
    }

    // LOADING FOOTER
    fun showLoading() {
        if (!isLoading) {
            isLoading = true
            notifyItemInserted(items.size)
        }
    }

    fun hideLoading() {
        if (isLoading) {
            isLoading = false
            notifyItemRemoved(items.size)
        }
    }

    inner class ProductVH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)
    inner class LoaderVH(val binding: ItemLoaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == Constants.ITEM_VIEW) {
            ProductVH(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            LoaderVH(ItemLoaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is LoaderVH) return

        val b = (holder as ProductVH).binding
        val item = items[position]

        with(b) {

            // ---------------- IMAGE ---------------------
            val rawImageUrl = item.images?.firstOrNull()
            val imageUrl = rawImageUrl?.takeIf { !it.isNullOrBlank() && it.lowercase() != "none" }

            Glide.with(imgProduct.context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .into(imgProduct)

            // ---------------- USP TAGS ---------------------
            layoutUspTags.removeAllViews()
            val uspTags = item.uspLabelsTags ?: emptyList()

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
                    val iv = ImageView(root.context).apply {
                        setImageResource(iconRes)
                        scaleType = ImageView.ScaleType.FIT_XY
                        layoutParams = LinearLayout.LayoutParams(
                            Constants.USP_TAG_WIDTH_DP.dp,
                            Constants.USP_TAG_HEIGHT_DP.dp
                        )
                    }
                    layoutUspTags.addView(iv)
                }
            }

            // ---------------- TITLE ---------------------
            txtTitle.text = item.name.orEmpty()

            // ---------------- PRICE ---------------------
            val priceObj = item.contextualPrice ?: item.price

            val priceDisplay = priceObj?.priceDisplay
            val strike = priceObj?.strikeThroughPriceDisplay
            val discount = priceObj?.discount ?: 0

            if (!priceDisplay.isNullOrBlank()) {

                layoutPrice.visibility = View.VISIBLE

                txtPrice.text = priceDisplay
                txtStrike.text = strike.orEmpty()
                txtDiscount.text = if (discount > 0) "$discount%" else ""

                txtStrike.visibility =
                    if (!strike.isNullOrBlank() && discount > 0) View.VISIBLE else View.GONE

                txtDiscount.visibility = if (discount > 0) View.VISIBLE else View.GONE

                txtStrike.paintFlags =
                    if (!strike.isNullOrBlank() && discount > 0)
                        txtStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    else
                        txtStrike.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                root.post { trimPriceRowProperly(b, priceDisplay, strike, discount) }

            } else {
                layoutPrice.visibility = View.GONE
            }

            // ---------------- FREE SHIPPING ---------------------
            txtFree.visibility =
                if (item.tags?.contains(Constants.TAG_FREE_SHIPPING) == true)
                    View.VISIBLE else View.GONE

            // ---------------- RATING ---------------------
            val review = item.review
            val absRating = review?.absoluteRating
            val sellerRating = review?.sellerRating

            when {
                absRating != null && absRating > 0.0 -> {
                    layoutRating.visibility = View.VISIBLE
                    imgRatingIcon.setImageResource(R.drawable.ic_star)
                    txtRatingValue.text = absRating.toString()
                }

                sellerRating != null && sellerRating > 0.0 -> {
                    layoutRating.visibility = View.VISIBLE
                    imgRatingIcon.setImageResource(R.drawable.ic_homestar)
                    txtRatingValue.text = "$sellerRating rating seller"
                }

                else -> layoutRating.visibility = View.GONE
            }

            // ---------------- BOTTOM ROW ---------------------
            val brand = item.brand?.lowercase()
            imgVerified.visibility =
                if (brand != null && brand != Constants.BRAND_NO_BRAND) View.VISIBLE else View.GONE

            val rawBadgeUrl = item.badge?.merchantBadgeUrl
            val merchantBadgeUrl = rawBadgeUrl?.takeIf { !it.isNullOrBlank() && it.lowercase() != "none" }
            val hasBadge = !merchantBadgeUrl.isNullOrBlank()

            imgMerchantBadge.visibility = if (hasBadge) View.VISIBLE else View.GONE
            if (hasBadge) {
                Glide.with(imgMerchantBadge.context)
                    .load(merchantBadgeUrl)
                    .placeholder(Constants.PLACEHOLDER_LOCATION)
                    .into(imgMerchantBadge)
            }

            val loc = item.location
            val hasLocation = !loc.isNullOrBlank()

            txtLocation.text = loc.orEmpty()
            txtLocation.visibility = if (hasLocation) View.VISIBLE else View.GONE
            imgLocationIcon.visibility = if (hasLocation && !hasBadge) View.VISIBLE else View.GONE

            root.post { trimLocationProperly(b, loc) }

            layoutBottom.visibility =
                if (imgVerified.visibility == View.VISIBLE ||
                    imgMerchantBadge.visibility == View.VISIBLE ||
                    txtLocation.visibility == View.VISIBLE
                ) View.VISIBLE else View.GONE

            // ---------------- INFO BUTTON ---------------------
            btnShowInfo.setOnClickListener {
                showInfoDialog(holder.itemView.context, item)
            }
        }
    }

    // ============================================================================================
    // DIALOG
    // ============================================================================================
    private fun showInfoDialog(context: Context, product: Product) {

        val binding = DialogProductInfoBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(0x00000000.toInt()))

        binding.btnAddToCart.setOnClickListener {
            onAddToCart(product)   // ViewModel will ensure ID & JSON
            dialog.dismiss()
        }


        dialog.show()

        // IMAGE
        val dialogRawImage = product.images?.firstOrNull()
        val dialogImage = dialogRawImage?.takeIf { !it.isNullOrBlank() && it.lowercase() != "none" }
        Glide.with(binding.dialogImgProduct.context)
            .load(dialogImage)
            .placeholder(R.drawable.bg_image_placeholder)
            .into(binding.dialogImgProduct)

        // TITLE
        binding.dialogTxtTitle.text = product.name.orEmpty()

        // PRICE (contextual or regular)
        val priceObj = product.contextualPrice ?: product.price
        val priceDisplay = priceObj?.priceDisplay
        val strike = priceObj?.strikeThroughPriceDisplay
        val discount = priceObj?.discount ?: 0

        if (!priceDisplay.isNullOrBlank()) {
            binding.dialogLayoutPrice.visibility = View.VISIBLE
            binding.dialogTxtPrice.text = priceDisplay

            if (!strike.isNullOrBlank() && discount > 0) {
                binding.dialogTxtStrike.visibility = View.VISIBLE
                binding.dialogTxtStrike.text = strike
                binding.dialogTxtStrike.paintFlags =
                    binding.dialogTxtStrike.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                binding.dialogTxtDiscount.visibility = View.VISIBLE
                binding.dialogTxtDiscount.text = "$discount%"
            } else {
                binding.dialogTxtStrike.visibility = View.GONE
                binding.dialogTxtDiscount.visibility = View.GONE
            }

        } else {
            binding.dialogLayoutPrice.visibility = View.GONE
        }

        // FREE SHIPPING
        val free = product.tags?.contains(Constants.TAG_FREE_SHIPPING) == true
        binding.dialogTxtFree.visibility = if (free) View.VISIBLE else View.GONE

        // RATING
        val review = product.review
        val absRating = review?.absoluteRating
        val sellerRating = review?.sellerRating

        when {
            absRating != null && absRating > 0 -> {
                binding.dialogLayoutRating.visibility = View.VISIBLE
                binding.dialogImgRatingIcon.setImageResource(R.drawable.ic_star)
                binding.dialogTxtRatingValue.text = absRating.toString()
            }
            sellerRating != null && sellerRating > 0 -> {
                binding.dialogLayoutRating.visibility = View.VISIBLE
                binding.dialogImgRatingIcon.setImageResource(R.drawable.ic_homestar)
                binding.dialogTxtRatingValue.text = "$sellerRating rating seller"
            }
            else -> binding.dialogLayoutRating.visibility = View.GONE
        }

        // VERIFIED BRAND
        val brand = product.brand?.lowercase()
        val showVerified = brand != null && brand != Constants.BRAND_NO_BRAND
        binding.dialogImgVerified.visibility = if (showVerified) View.VISIBLE else View.GONE

        // MERCHANT BADGE
        val dialogRawBadge = product.badge?.merchantBadgeUrl
        val dialogBadgeUrl = dialogRawBadge?.takeIf { !it.isNullOrBlank() && it.lowercase() != "none" }
        val hasBadge = !dialogBadgeUrl.isNullOrBlank()

        binding.dialogImgMerchantBadge.visibility = if (hasBadge) View.VISIBLE else View.GONE
        if (hasBadge) {
            Glide.with(binding.dialogImgMerchantBadge.context)
                .load(dialogBadgeUrl)
                .placeholder(R.drawable.bg_image_placeholder)
                .into(binding.dialogImgMerchantBadge)
        }

        // LOCATION
        val loc = product.location
        val hasLoc = !loc.isNullOrBlank()

        binding.dialogTxtLocation.text = loc.orEmpty()
        binding.dialogTxtLocation.visibility = if (hasLoc) View.VISIBLE else View.GONE
        binding.dialogImgLocationIcon.visibility = if (hasLoc && !hasBadge) View.VISIBLE else View.GONE

        // CLOSE BTN
        binding.btnCloseDesc.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ============================================================================================
    // HELPERS
    // ============================================================================================
    private val Int.dp get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun trimToFitSingleLine(tv: TextView, maxWidthPx: Int, original: String): String {
        tv.text = original
        tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        if (tv.measuredWidth <= maxWidthPx) return original

        var trimmed = original
        while (trimmed.length > 4) {
            trimmed = trimmed.dropLast(1)
            val temp = "$trimmed…"
            tv.text = temp
            tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
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

    private fun isValid(item: Product): Boolean {
        val titleValid = !item.name.isNullOrBlank()

        val priceObj = item.contextualPrice ?: item.price
        val priceValid = !priceObj?.priceDisplay.isNullOrBlank()

        return titleValid && priceValid
    }

    private fun ensureProductId(p: Product): String {
        val cleanId = p.id?.trim()?.lowercase()

        if (!cleanId.isNullOrBlank()) return cleanId

        // Fallback ID (prevents duplicates of visually identical items)
        return listOf(
            p.name?.trim()?.lowercase(),
            p.price?.priceDisplay?.trim()?.lowercase(),
            p.images?.firstOrNull()?.trim()?.lowercase()
        ).filterNotNull().joinToString("_")
    }



}
