package com.jankku.alphawall.ui.home

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.R
import com.jankku.alphawall.database.model.SortStatus
import com.jankku.alphawall.databinding.FragmentHomeBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.ui.common.FastGridLayoutManager
import com.jankku.alphawall.ui.common.WallpaperAdapter
import com.jankku.alphawall.ui.common.WallpaperLoadingStateAdapter
import com.jankku.alphawall.util.navigateSafe
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: WallpaperAdapter? = null
    private val adapter get() = _adapter!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as AlphaWallApplication).repository)
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
        _binding = FragmentHomeBinding.inflate(
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
        binding.rvWallpaper.adapter = null
        _adapter = null
        _binding = null
    }

    private fun setupAdapter() {
        _adapter = WallpaperAdapter { wallpaper ->
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToWallpaperDetailFragment(
                    wallpaper
                )
            )
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                lifecycleScope.launch {
                    binding.rvWallpaper.isVisible =
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
        binding.rvWallpaper.let {
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
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupScrollToTop() {
        binding.fabUp.hide()
        binding.rvWallpaper.addOnScrollListener(
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
            binding.rvWallpaper.smoothScrollToPosition(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                findNavController().navigateSafe(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
                true
            }
            R.id.action_sort -> {
                sortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortDialog() {
        val checkedItem = viewModel.sortStatus.value!!.ordinal
        val sortMethods = SortStatus.toArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialog_sort_title))
            .setSingleChoiceItems(sortMethods, checkedItem) { _, index ->
                val status = SortStatus.values()[index]
                viewModel.setSortMethod(status)
            }
            .setPositiveButton(resources.getString(R.string.dialog_sort_button)) { _, _ ->
                viewModel.fetchWallpapers()
            }.show()
    }
}
