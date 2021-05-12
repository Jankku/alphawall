package com.jankku.wallpapers.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jankku.wallpapers.database.Wallpaper

class DetailViewModel : ViewModel() {

    val _wallpaper = MutableLiveData<Wallpaper>()
    val wallpaper: LiveData<Wallpaper>
        get() = _wallpaper

    val _isLoadingPicture = MutableLiveData(true)
    val isLoadingPicture: LiveData<Boolean>
        get() = _isLoadingPicture

    val _networkError = MutableLiveData(false)
    val networkError: LiveData<Boolean>
        get() = _networkError

    private val _setWallpaper = MutableLiveData(false)
    val setWallpaper: LiveData<Boolean>
        get() = _setWallpaper

    private val _downloadWallpaper = MutableLiveData(false)
    val downloadWallpaper: LiveData<Boolean>
        get() = _downloadWallpaper

    val _isDownloadingWallpaper = MutableLiveData(false)
    val isDownloadingWallpaper: LiveData<Boolean>
        get() = _isDownloadingWallpaper

    fun setWallpaper(value: Boolean) {
        _setWallpaper.value = value
    }

    fun downloadWallpaper(value: Boolean) {
        _downloadWallpaper.value = value
    }
}

class DetailViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}