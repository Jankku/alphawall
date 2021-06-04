package com.jankku.alphawall.viewmodel

import androidx.lifecycle.*
import com.jankku.alphawall.database.model.Wallpaper

class WallpaperDetailViewModel(wallpaper: Wallpaper) : ViewModel() {

    private val _wallpaper = liveData { emit(wallpaper) }
    val wallpaper: LiveData<Wallpaper>
        get() = _wallpaper

    val isLoading = MutableLiveData(true)
    val networkError = MutableLiveData(false)
    val setWallpaper = MutableLiveData(false)
    val downloadWallpaper = MutableLiveData(false)
    val openWallpaperPage = MutableLiveData(false)

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

class WallpaperDetailViewModelFactory(private val wallpaper: Wallpaper) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallpaperDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallpaperDetailViewModel(wallpaper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}