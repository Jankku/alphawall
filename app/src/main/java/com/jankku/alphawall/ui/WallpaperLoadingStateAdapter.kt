package com.jankku.alphawall.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.R
import com.jankku.alphawall.databinding.LoadingStateBinding

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
            binding.btnItemRetry.setOnClickListener { retryCallback() }
        }

        fun bind(loadState: LoadState) {
            with(binding) {
                pbItemLoader.isVisible = loadState is LoadState.Loading
                btnItemRetry.isVisible = loadState is LoadState.Error
                tvItemErrorMessage.isVisible =
                    !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
                tvItemErrorMessage.text = (loadState as? LoadState.Error)?.error?.message
            }
        }
    }
}