package com.jankku.alphawall.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import com.jankku.alphawall.database.model.Category
import com.jankku.alphawall.repository.WallpaperRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryListViewModel(private val repository: WallpaperRepository) : ViewModel() {
    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>> get() = _categories

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private fun setLoadingStatus(value: Boolean) {
        _isLoading.postValue(value)
    }

    init {
        fetchCategories()
        saveCategoriesToDB()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            setLoadingStatus(true)
            repository
                .fetchCategories()
                .collect { categories ->
                    _categories.value = categories
                    delay(1_000)
                    setLoadingStatus(false)
                }
        }
    }

    private fun saveCategoriesToDB() {
        viewModelScope.launch {
            repository.saveCategoriesToDatabase()
        }
    }
}

@ExperimentalPagingApi
class CategoryListViewModelFactory(private val repository: WallpaperRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
