package com.jankku.wallpapers

import android.app.Application
import androidx.paging.ExperimentalPagingApi
import com.jankku.wallpapers.database.WallpaperDatabase
import com.jankku.wallpapers.network.AlphaCodersApi
import com.jankku.wallpapers.repository.WallpaperRepository

class WallpaperApplication : Application() {
    private val database by lazy { WallpaperDatabase.getDatabase(this) }
    @ExperimentalPagingApi
    val repository by lazy {
        WallpaperRepository(
            AlphaCodersApi.wallpaperService,
            database
        )
    }
}