package com.jankku.wallpapers.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.database.WallpaperDatabase
import com.jankku.wallpapers.network.AlphaCodersApiService
import com.jankku.wallpapers.network.WallpaperRemoteMediator
import com.jankku.wallpapers.util.Constants.PAGE_SIZE
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(
    private val api: AlphaCodersApiService,
    private val database: WallpaperDatabase) {

    @ExperimentalPagingApi
    fun refreshWallpapers(): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { database.wallpaperDao().getAll() },
            remoteMediator = WallpaperRemoteMediator(api, database),
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE + (PAGE_SIZE * 2),
                enablePlaceholders = false
            )
        ).flow
    }
}