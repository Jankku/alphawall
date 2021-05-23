package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.repository.WallpaperRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class CategoryItemViewModel(
    private val category: Category,
    private val repository: WallpaperRepository
) : ViewModel() {
    private val _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>>
        get() = _wallpapers

    init {
        fetchWallpapersFromCategory()
    }

    private fun fetchWallpapersFromCategory() {
        viewModelScope.launch {
            repository
                .fetchWallpapersFromCategory(category)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _wallpapers.value = pagingData
                }
        }
    }
}

@ExperimentalPagingApi
class CategoryItemViewModelFactory(
    private val category: Category,
    private val repository: WallpaperRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryItemViewModel(category, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
