package com.jankku.wallpapers.util

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.jankku.wallpapers.R
import com.jankku.wallpapers.viewmodel.DetailViewModel

/**
 * Binding adapter used to display images from URL using Glide
 */
@BindingAdapter("imageUrl")
fun loadImage(imageView: ImageView, url: String?) {
    Glide
        .with(imageView.context)
        .load(url)
        .centerCrop()
        //.transition(DrawableTransitionOptions.withCrossFade())
        .error(R.drawable.ic_error)
        .into(imageView)
}

/**
 * Binding adapter used to display images on detail page from URL using Glide
 */
@BindingAdapter("detailImgUrl", "viewModel")
fun detailImageLoad(imageView: ImageView, url: String, viewModel: DetailViewModel) {
    Glide
        .with(imageView.context)
        .load(url)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                viewModel.isLoading.value = false
                viewModel.networkError.value = true
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                viewModel.isLoading.value = false
                return false
            }
        })
        .into(imageView)
}

@BindingAdapter("detailCategory")
fun detailCategory(textView: TextView, category: String) {
    textView.text = textView.context.getString(R.string.wallpaper_category, category)
}

@BindingAdapter("detailSubmitter")
fun detailSubmitter(textView: TextView, submitter: String) {
    textView.text = textView.context.getString(R.string.wallpaper_submitter, submitter)
}

/**
 * Binding adapter used to hide view when loading finishes
 */
@BindingAdapter("isLoading")
fun hideIfNotLoading(view: View, isLoading: Boolean) {
    if (isLoading) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

/**
 * Gets the image size in bytes, converts it to megabytes and sets it to TextView
 */
@BindingAdapter("detailSize")
fun detailSize(textView: TextView, size: String) {
    val sizeInMB: Double = ((size.toDouble() / 1000) / 1000).roundTo(2)
    textView.text = textView.context.getString(R.string.wallpaper_size, sizeInMB.toString())
}

/**
 * Gets the image width and height and uses it to display the resolution
 */
@BindingAdapter("detailWidth", "detailHeight")
fun detailResolution(textView: TextView, width: String, height: String) {
    textView.text = textView.context.getString(R.string.wallpaper_resolution, width, height)
}