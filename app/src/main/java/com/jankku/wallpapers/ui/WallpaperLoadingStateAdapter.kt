package com.jankku.wallpapers.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jankku.wallpapers.R
import com.jankku.wallpapers.databinding.LoadingStateBinding

class WallpaperLoadingStateAdapter(
    private val retryCallback: () -> Unit
) : LoadStateAdapter<WallpaperLoadingStateAdapter.NetworkStateViewHolder>() {

    override fun onBindViewHolder(
        holder: NetworkStateViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateViewHolder {
        val binding: LoadingStateBinding = LoadingStateBinding.bind(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.loading_state, parent, false)
        )
        return NetworkStateViewHolder(binding) { retryCallback() }
    }

    class NetworkStateViewHolder(
        private val binding: LoadingStateBinding,
        private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryButton.setOnClickListener { retryCallback() }
        }

        fun bind(loadState: LoadState) {
            with(binding) {
                progressBar.isVisible = loadState is LoadState.Loading
                retryButton.isVisible = loadState is LoadState.Error
                errorMsg.isVisible = !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
                errorMsg.text = (loadState as? LoadState.Error)?.error?.message
            }
        }
    }
}