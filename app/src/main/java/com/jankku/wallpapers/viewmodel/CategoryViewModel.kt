package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.repository.WallpaperRepository
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryViewModel(private val repository: WallpaperRepository) : ViewModel() {
    private val _categories: LiveData<List<Category>> = repository.fetchCategories().asLiveData()
    val categories: LiveData<List<Category>>
        get() = _categories

    init {
        viewModelScope.launch {
            repository.saveCategoriesToDatabase()
        }
    }
}

@ExperimentalPagingApi
class CategoryViewModelFactory(private val repository: WallpaperRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
