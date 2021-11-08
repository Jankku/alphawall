package com.jankku.alphawall.ui.home

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.alphawall.database.model.SortStatus
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.repository.WallpaperRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val _sortStatus: MutableLiveData<SortStatus> = MutableLiveData(SortStatus.NEWEST)
    val sortStatus: LiveData<SortStatus> get() = _sortStatus

    private val _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>> get() = _wallpapers

    private val _retryBtnClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val retryBtnClick: LiveData<Boolean> get() = _retryBtnClick

    init {
        fetchWallpapers()
    }

    fun fetchWallpapers() {
        viewModelScope.launch {
            repository
                .fetchWallpapers(sortStatus.value!!.value)
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _wallpapers.value = pagingData
                }
        }
    }

    fun setSortMethod(status: SortStatus) {
        _sortStatus.postValue(status)
    }

    fun setRetryBtnClick(value: Boolean) {
        _retryBtnClick.postValue(value)
    }
}

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
