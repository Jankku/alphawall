package com.jankku.wallpapers.ui

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.wallpapers.R
import com.jankku.wallpapers.WallpaperApplication
import com.jankku.wallpapers.databinding.FragmentHomeBinding
import com.jankku.wallpapers.viewmodel.HomeViewModel
import com.jankku.wallpapers.viewmodel.HomeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var application: Application
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WallpaperAdapter

    @ExperimentalPagingApi
    private val homeViewModel: HomeViewModel by viewModels {
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

    @ExperimentalPagingApi
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

        adapter = WallpaperAdapter { wallpaper ->
            // This is executed when clicking wallpaper
            val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                wallpaper = wallpaper
            )
            findNavController().navigate(action)
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = homeViewModel

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefresh.isRefreshing = false
        }

        lifecycleScope.launch(Dispatchers.IO) {
            homeViewModel.wallpapers.collectLatest { pagingData ->
                adapter.submitData(lifecycle, pagingData)
            }
        }

        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter = adapter.withLoadStateHeaderAndFooter(
            header = WallpaperLoadingStateAdapter { adapter.retry() },
            footer = WallpaperLoadingStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->
            binding.recyclerview.isVisible = loadState.source.refresh !is LoadState.Error
            binding.spinner.isVisible = loadState.source.refresh is LoadState.Loading
            binding.btnRetry.isVisible = loadState.source.refresh is LoadState.Error
        }

        homeViewModel.networkError.observe(viewLifecycleOwner) { networkError ->
            if (networkError) {
                Toast.makeText(application, R.string.error_network, Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

