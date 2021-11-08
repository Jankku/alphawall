package com.jankku.alphawall.ui.search

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.R
import com.jankku.alphawall.databinding.FragmentSearchBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.ui.common.FastGridLayoutManager
import com.jankku.alphawall.ui.common.WallpaperAdapter
import com.jankku.alphawall.util.Event
import com.jankku.alphawall.util.hideKeyboard
import com.jankku.alphawall.util.navigateSafe
import com.jankku.alphawall.util.showKeyboard
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment() {

    override var bottomNavigationVisibility = View.GONE

    private lateinit var application: Application
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var _adapter: WallpaperAdapter? = null
    private val adapter get() = _adapter!!
    private var keyboardShownOnce = false

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory((application as AlphaWallApplication).repository)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().application
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater)

        setupSearchGuide()
        setupObservers()
        setupAdapter()
        setupRecyclerView()
        setupScrollToTop()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setupSearch()

        // Show search guide only on initial load
        if (viewModel.searchDone.value == true) {
            viewModel.hideSearchGuide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSearch.adapter = null
        _adapter = null
        _binding = null
    }

    private fun setupSearch() {
        var searchHandled = false

        // Show keyboard only on initial load
        if (!keyboardShownOnce) {
            binding.tietSearch.showKeyboard()
            keyboardShownOnce = true
        }

        val searchTerm = binding.tietSearch.text

        binding.tietSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchTerm?.isNotBlank() == true) {
                    viewModel.hideSearchGuide()
                    binding.tietSearch.clearFocus()
                    binding.tietSearch.hideKeyboard()
                    viewModel.search(searchTerm.toString())
                    viewModel.setSearchDoneValue(true)
                    searchHandled = true
                }
            }
            searchHandled
        }
    }

    private fun setupAdapter() {
        _adapter = WallpaperAdapter { wallpaper ->
            findNavController().navigateSafe(
                SearchFragmentDirections.actionSearchFragmentToWallpaperDetailFragment(
                    wallpaper
                )
            )
        }

        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        adapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                lifecycleScope.launch {
                    binding.rvSearch.isVisible = loadState.source.refresh is LoadState.NotLoading
                    binding.progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvSearch.let {
            it.setHasFixedSize(true)
            it.layoutManager = FastGridLayoutManager(
                requireContext(),
                resources.getInteger(R.integer.wallpaper_grid_columns)
            )
            it.adapter = adapter
        }
    }

    private fun setupObservers() {
        viewModel.wallpapers.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }
    }

    private fun setupSearchGuide() {
        lifecycleScope.launch {
            viewModel.searchGuideFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is Event.SearchGuide -> {
                            if (event.hideSearchGuide) {
                                binding.guideSearch.clSearchGuide.visibility = View.GONE
                            } else {
                                binding.guideSearch.clSearchGuide.visibility = View.VISIBLE
                            }
                        }
                    }
                }
        }
    }

    private fun setupScrollToTop() {
        binding.rvSearch.addOnScrollListener(
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
            binding.rvSearch.smoothScrollToPosition(0)
        }
    }
}
