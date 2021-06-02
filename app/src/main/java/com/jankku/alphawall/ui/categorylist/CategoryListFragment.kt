package com.jankku.alphawall.ui.categorylist

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import com.jankku.alphawall.AlphaWallApplication
import com.jankku.alphawall.adapter.CategoryAdapter
import com.jankku.alphawall.databinding.FragmentCategoryListBinding
import com.jankku.alphawall.ui.BaseFragment
import com.jankku.alphawall.viewmodel.CategoryListViewModel
import com.jankku.alphawall.viewmodel.CategoryListViewModelFactory
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryListFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter


    private val viewModel: CategoryListViewModel by viewModels {
        CategoryListViewModelFactory((application as AlphaWallApplication).repository)
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
        _binding = FragmentCategoryListBinding.inflate(
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
    }

    private fun setupAdapter() {
        adapter = CategoryAdapter { category ->
            // This is executed when clicking wallpaper
            val action =
                CategoryListFragmentDirections.actionCategoryListFragmentToCategoryFragment(
                    category,
                    category.name
                )
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        binding.rvCategory.setHasFixedSize(true)
        binding.rvCategory.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.categories.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

