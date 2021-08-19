package com.jankku.alphawall

import android.app.Application
import com.jankku.alphawall.database.WallpaperDatabase
import com.jankku.alphawall.network.AlphaCodersApi
import com.jankku.alphawall.repository.WallpaperRepository

class AlphaWallApplication : Application() {
    private val database by lazy { WallpaperDatabase.getDatabase(this) }

    val repository by lazy {
        WallpaperRepository(
            AlphaCodersApi.wallpaperService,
            database
        )
    }
}
