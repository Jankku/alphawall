package com.jankku.alphawall.ui.category

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.jankku.alphawall.database.model.Category
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.repository.WallpaperRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val category: Category,
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _wallpapers: MutableLiveData<PagingData<Wallpaper>> = MutableLiveData()
    val wallpapers: LiveData<PagingData<Wallpaper>> get() = _wallpapers

    private val _retryBtnClick: MutableLiveData<Boolean> = MutableLiveData(false)
    val retryBtnClick: LiveData<Boolean> get() = _retryBtnClick

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

    fun setRetryBtnClick(value: Boolean) {
        _retryBtnClick.postValue(value)
    }
}

class CategoryViewModelFactory(
    private val category: Category,
    private val repository: WallpaperRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryViewModel(category, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
