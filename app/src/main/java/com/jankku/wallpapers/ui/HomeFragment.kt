package com.jankku.wallpapers.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jankku.wallpapers.R
import com.jankku.wallpapers.WallpaperApplication
import com.jankku.wallpapers.databinding.FragmentHomeBinding
import com.jankku.wallpapers.viewmodel.HomeViewModel
import com.jankku.wallpapers.viewmodel.HomeViewModelFactory

@ExperimentalPagingApi
class HomeFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WallpaperAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as WallpaperApplication).repository)
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
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setupAdapter()
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
    }

    private fun setupAdapter() {
        adapter = WallpaperAdapter { wallpaper ->
            // This is executed when clicking wallpaper
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(wallpaper)
            findNavController().navigate(action)
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                binding.rvWallpaper.isVisible = loadState.source.refresh !is LoadState.Error
                binding.spinner.isVisible = loadState.source.refresh is LoadState.Loading
                binding.btnRetry.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvWallpaper.let {
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
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            R.id.action_sort -> {
                //SortDialogFragment().show(parentFragmentManager, "TAG")
                sortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sortDialog() {
        val checkedItem = 0
        val singleItems = arrayOf(
            getString(R.string.dialog_sort_newest),
            getString(R.string.dialog_sort_rating),
            getString(R.string.dialog_sort_views),
            getString(R.string.dialog_sort_favorites),
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.dialog_sort_title))
            .setPositiveButton(resources.getString(R.string.dialog_sort_button)) { dialog, _ ->
                dialog.dismiss()
            }
            .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                    }
                    1 -> {
                    }
                    2 -> {
                    }
                    3 -> {
                    }
                    else -> {
                    }
                }
            }.show()
    }
}
