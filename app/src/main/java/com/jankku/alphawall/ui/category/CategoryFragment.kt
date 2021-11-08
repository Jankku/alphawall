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
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.R
import com.jankku.alphawall.databinding.FragmentCategoryBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.ui.common.FastGridLayoutManager
import com.jankku.alphawall.ui.common.WallpaperAdapter
import com.jankku.alphawall.ui.common.WallpaperLoadingStateAdapter
import kotlinx.coroutines.launch

class CategoryFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private var _adapter: WallpaperAdapter? = null
    private val adapter get() = _adapter!!
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(
            inflater,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setupObservers()
        setupAdapter()
        setupRecyclerView()
        setupSwipeRefresh()
        setupScrollToTop()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCategoryDetail.adapter = null
        _adapter = null
        _binding = null
    }

    private fun setupAdapter() {
        _adapter = WallpaperAdapter { wallpaper ->
            findNavController().navigate(
                CategoryFragmentDirections.actionCategoryFragmentToWallpaperDetailFragment(
                    wallpaper
                )
            )
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                viewLifecycleOwner.lifecycleScope.launch {
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
            it.layoutManager = FastGridLayoutManager(
                requireContext(),
                resources.getInteger(R.integer.wallpaper_grid_columns)
            )
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

    private fun setupScrollToTop() {
        binding.fabUp.hide()
        binding.rvCategoryDetail.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 5) {
                        binding.fabUp.show()
                    } else if (dy < 0) {
                        binding.fabUp.hide()
                    }
                }
            })
        binding.fabUp.setOnClickListener {
            binding.rvCategoryDetail.smoothScrollToPosition(0)
        }
    }
}
