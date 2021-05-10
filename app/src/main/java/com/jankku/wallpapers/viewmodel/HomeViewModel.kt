package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@ExperimentalPagingApi
class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val _wallpapers: Flow<PagingData<Wallpaper>> = fetchWallpapers()
    val wallpapers: Flow<PagingData<Wallpaper>>
        get() = _wallpapers

    private val _isEmpty = MutableLiveData(false)
    val isEmpty: LiveData<Boolean>
        get() = _isEmpty

    private var _networkError = MutableLiveData(false)
    val networkError: LiveData<Boolean>
        get() = _networkError

    private fun fetchWallpapers(): Flow<PagingData<Wallpaper>> {
        return repository
            .refreshWallpapers()
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
