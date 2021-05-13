package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.repository.WallpaperRepository

@ExperimentalPagingApi
class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val _wallpapers: LiveData<PagingData<Wallpaper>> = fetchWallpapers()
    val wallpapers: LiveData<PagingData<Wallpaper>>
        get() = _wallpapers

    private var _networkError = MutableLiveData(false)
    val networkError: LiveData<Boolean>
        get() = _networkError

    private fun fetchWallpapers(): LiveData<PagingData<Wallpaper>> {
        return repository
            .fetchWallpapers()
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
    }
}

@ExperimentalPagingApi
class HomeViewModelFactory(private val repository: WallpaperRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
