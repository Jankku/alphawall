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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.adapter.WallpaperAdapter
import com.jankku.alphawall.databinding.FragmentSearchBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.util.Keyboard.Companion.hideKeyboard
import com.jankku.alphawall.util.Keyboard.Companion.showKeyboard
import com.jankku.alphawall.viewmodel.SearchViewModel
import com.jankku.alphawall.viewmodel.SearchViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@ExperimentalPagingApi
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(layoutInflater)

        setupSearchGuide()
        setupSearch()
        setupObservers()
        setupAdapter()
        setupRecyclerView()
        setupScrollToTop()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

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
            // This is executed when clicking wallpaper
            val action =
                SearchFragmentDirections.actionSearchFragmentToWallpaperDetailFragment(wallpaper)
            findNavController().navigate(action)
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
            it.adapter = adapter
        }
    }

    private fun setupObservers() {
        viewModel.wallpapers.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }
    }

    private fun setupSearchGuide() {
        viewModel.searchGuideFlow.onEach { event ->
            when (event) {
                is SearchViewModel.Event.SearchGuide -> {
                    if (event.hideSearchGuide) {
                        binding.guideSearch.clSearchGuide.visibility = View.GONE
                    } else {
                        binding.guideSearch.clSearchGuide.visibility = View.VISIBLE
                    }
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun setupScrollToTop() {
        binding.fabUp.hide()
        binding.rvSearch.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 5)
                        binding.fabUp.show()
                    else if (dy < 0)
                        binding.fabUp.hide()
                }
            })
        binding.fabUp.setOnClickListener {
            binding.rvSearch.smoothScrollToPosition(0)
        }
    }
}
