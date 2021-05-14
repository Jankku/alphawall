package com.jankku.wallpapers.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.wallpapers.WallpaperApplication
import com.jankku.wallpapers.databinding.FragmentCategoryDetailBinding
import com.jankku.wallpapers.viewmodel.CategoryItemViewModel
import com.jankku.wallpapers.viewmodel.CategoryItemViewModelFactory

@ExperimentalPagingApi
class CategoryDetailFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WallpaperAdapter
    private val args: CategoryDetailFragmentArgs by navArgs()

    private val viewModel: CategoryItemViewModel by viewModels {
        CategoryItemViewModelFactory(
            args.category,
            (application as WallpaperApplication).repository
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
        _binding = FragmentCategoryDetailBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner

        setupObservers()
        setupAdapter()
        setupRecyclerView()
        setupSwipeRefresh()
    }

    private fun setupAdapter() {
        adapter = WallpaperAdapter { wallpaper ->
            // This is executed when clicking wallpaper
            val action =
                CategoryDetailFragmentDirections.actionCategoryDetailFragmentToDetailFragment(
                    wallpaper = wallpaper
                )
            findNavController().navigate(action)
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                binding.rvCategoryDetail.isVisible = loadState.source.refresh !is LoadState.Error
                binding.spinner.isVisible = loadState.source.refresh is LoadState.Loading
                binding.btnRetry.isVisible = loadState.source.refresh is LoadState.Error
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

