package com.jankku.alphawall.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.R
import com.jankku.alphawall.database.Category
import com.jankku.alphawall.databinding.ItemCategoryBinding

class CategoryAdapter(private val clickListener: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemCategoryBinding = DataBindingUtil.inflate(
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
        val categoryItem = getItem(position)
        if (categoryItem != null) {
            holder.binding.also {
                it.category = categoryItem
            }
        }
    }

    class ViewHolder(
        val binding: ItemCategoryBinding,
        clickAtPosition: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { clickAtPosition(absoluteAdapterPosition) }
        }

        companion object {
            @LayoutRes
            val LAYOUT = R.layout.item_category
        }
    }

    companion object {
        object DiffCallback : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: Category, newItem: Category) =
                oldItem.id == newItem.id
        }
    }
}