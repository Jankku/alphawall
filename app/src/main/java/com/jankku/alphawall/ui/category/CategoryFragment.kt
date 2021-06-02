package com.jankku.alphawall.ui.category

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.adapter.WallpaperAdapter
import com.jankku.alphawall.adapter.WallpaperLoadingStateAdapter
import com.jankku.alphawall.databinding.FragmentCategoryBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.viewmodel.CategoryViewModel
import com.jankku.alphawall.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WallpaperAdapter
    private val args: CategoryFragmentArgs by navArgs()

    private val viewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(
            args.category,
            (application as AlphaWallApplication).repository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().application
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setupObservers()
        setupAdapter()
        setupRecyclerView()
        setupSwipeRefresh()
    }

    private fun setupAdapter() {
        adapter = WallpaperAdapter { wallpaper ->
            // This is executed when clicking wallpaper
            val action = CategoryFragmentDirections.actionCategoryFragmentToWallpaperDetailFragment(
                wallpaper
            )
            findNavController().navigate(action)
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                lifecycleScope.launch {
                    binding.rvCategoryDetail.isVisible =
                        loadState.source.refresh is LoadState.NotLoading
                    binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    binding.btnLoadRetry.isVisible = loadState.source.refresh is LoadState.Error
                    binding.tvLoadErrorMessage.isVisible =
                        loadState.source.refresh is LoadState.Error
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategoryDetail.let {
            it.setHasFixedSize(true)
            it.adapter = adapter.withLoadStateHeaderAndFooter(
                header = WallpaperLoadingStateAdapter { adapter.retry() },
                footer = WallpaperLoadingStateAdapter { adapter.retry() }
            )
        }
    }

    private fun setupObservers() {
        viewModel.wallpapers.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }

        viewModel.retryBtnClick.observe(viewLifecycleOwner) { click ->
            if (click) adapter.retry()
        }
    }

    private fun setupSwipeRefresh() {
        binding.srCategoryDetail.setOnRefreshListener {
            adapter.refresh()
            binding.srCategoryDetail.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

