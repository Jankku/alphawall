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

    private val _sortMethod: MutableLiveData<String> = MutableLiveData("newest")
    val sortMethod: LiveData<String> get() = _sortMethod

    private val _sortMethodId: MutableLiveData<Int> = MutableLiveData()
    val sortMethodId: LiveData<Int> get() = _sortMethodId

    private var _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>> get() = _wallpapers

    init {
        fetchWallpapers(_sortMethod.value!!)
    }

    private fun fetchWallpapers(sortMethod: String) {
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

    fun setSortMethod(method: String) {
        _sortMethod.postValue(method)
    }

    fun setSortMethodId(id: Int) {
        _sortMethodId.postValue(id)
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
