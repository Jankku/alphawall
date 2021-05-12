package com.jankku.wallpapers.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jankku.wallpapers.R
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.databinding.WallpaperItemBinding

class WallpaperAdapter(private val clickListener: (Wallpaper) -> Unit) :
    PagingDataAdapter<Wallpaper, WallpaperAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: WallpaperItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ViewHolder.LAYOUT,
            parent,
            false
        )
        return ViewHolder(binding) { position ->
            getItem(position)?.let { clickListener(it) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallpaper = getItem(position)
        if (wallpaper != null) {
            holder.binding.also {
                it.wallpaper = wallpaper
            }
        }
    }

    class ViewHolder(
        val binding: WallpaperItemBinding,
        clickAtPosition: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { clickAtPosition(absoluteAdapterPosition) }
        }

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.wallpaper_item
        }
    }

    companion object {
        object DiffCallback : DiffUtil.ItemCallback<Wallpaper>() {
            override fun areItemsTheSame(oldItem: Wallpaper, newItem: Wallpaper) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Wallpaper, newItem: Wallpaper) =
                oldItem.id == newItem.id
        }
    }
}