package com.jankku.alphawall.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.R
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.databinding.ItemWallpaperBinding

class WallpaperAdapter(private val clickListener: (Wallpaper) -> Unit) :
    PagingDataAdapter<Wallpaper, WallpaperAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemWallpaperBinding = DataBindingUtil.inflate(
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
        val binding: ItemWallpaperBinding,
        clickAtPosition: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { clickAtPosition(absoluteAdapterPosition) }
        }

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.item_wallpaper
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