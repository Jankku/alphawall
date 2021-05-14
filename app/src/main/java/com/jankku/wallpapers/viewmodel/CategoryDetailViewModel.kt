package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.repository.WallpaperRepository

@ExperimentalPagingApi
class CategoryItemViewModel(
    private val category: Category,
    private val repository: WallpaperRepository
) : ViewModel() {
    private val _wallpapers: LiveData<PagingData<Wallpaper>> = fetchWallpapers()
    val wallpapers: LiveData<PagingData<Wallpaper>>
        get() = _wallpapers

    private fun fetchWallpapers(): LiveData<PagingData<Wallpaper>> {
        return repository
            .fetchCategory(category)
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
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
