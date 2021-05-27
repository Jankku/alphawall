package com.jankku.alphawall.ui

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
import com.jankku.alphawall.databinding.FragmentCategoryBinding
import com.jankku.alphawall.viewmodel.CategoryViewModel
import com.jankku.alphawall.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryFragment : BaseFragment() {

    private lateinit var application: Application
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryAdapter


    private val viewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory((application as AlphaWallApplication).repository)
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
    }

    private fun setupAdapter() {
        adapter = CategoryAdapter { category ->
            // This is executed when clicking wallpaper
            val action = CategoryFragmentDirections.actionCategoryFragmentToCategoryDetailFragment(
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

