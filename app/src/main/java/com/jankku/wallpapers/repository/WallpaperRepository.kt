package com.jankku.wallpapers.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.jankku.wallpapers.BuildConfig
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.database.WallpaperDatabase
import com.jankku.wallpapers.network.AlphaCodersApiService
import com.jankku.wallpapers.network.WallpaperRemoteMediator
import com.jankku.wallpapers.util.Constants.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

class WallpaperRepository(
    private val api: AlphaCodersApiService,
    private val database: WallpaperDatabase
) {

    @ExperimentalPagingApi
    fun fetchWallpapers(): LiveData<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { database.wallpaperDao().getAll() },
            remoteMediator = WallpaperRemoteMediator(api, database),
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE + (PAGE_SIZE * 2),
                enablePlaceholders = false
            )
        ).liveData
    }

    fun fetchCategories(): Flow<List<Category>> {
        return database.categoryDao().getAll()
    }

    suspend fun saveCategoriesToDatabase() {
        try {
            val response = api.getCategoryList(
                apiKey = BuildConfig.apiKey,
                method = "category_list"
            )
            database.categoryDao().insertAll(response.categories)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: HttpException) {
            e.printStackTrace()
        }
    }
}