package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.repository.WallpaperRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@ExperimentalPagingApi
class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val _sortMethodId: MutableLiveData<Int> = MutableLiveData()
    val sortMethodId: LiveData<Int> get() = _sortMethodId

    private val _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>> get() = _wallpapers

    private val _retryBtnClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val retryBtnClick: LiveData<Boolean> get() = _retryBtnClick

    init {
        fetchWallpapers("newest") // Default sort method
    }

    fun fetchWallpapers(sortMethod: String) {
        viewModelScope.launch {
            repository
                .fetchWallpapers(sortMethod)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _wallpapers.value = pagingData
                }
        }
    }

    fun setSortMethodId(id: Int) {
        _sortMethodId.postValue(id)
    }

    fun setRetryBtnClick(value: Boolean) {
        _retryBtnClick.postValue(value)
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
