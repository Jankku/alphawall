package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.*
import com.jankku.wallpapers.database.Wallpaper

class DetailViewModel(wallpaper: Wallpaper) : ViewModel() {

    private val _wallpaper = liveData { emit(wallpaper) }
    val wallpaper: LiveData<Wallpaper>
        get() = _wallpaper

    val isLoading = MutableLiveData(true)
    val setWallpaper = MutableLiveData(false)
    val downloadWallpaper = MutableLiveData(false)
    val openWallpaperPage = MutableLiveData(false)
    val networkError = MutableLiveData(false)

    fun setWallpaper(value: Boolean) {
        setWallpaper.value = value
    }

    fun downloadWallpaper(value: Boolean) {
        downloadWallpaper.value = value
    }

    fun openWallpaperPage(value: Boolean) {
        openWallpaperPage.value = value
    }
}

class DetailViewModelFactory(private val wallpaper: Wallpaper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(wallpaper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}