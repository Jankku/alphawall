package com.jankku.wallpapers.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jankku.wallpapers.BuildConfig
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.database.WallpaperDatabase
import com.jankku.wallpapers.network.AlphaCodersApiService
import com.jankku.wallpapers.network.CategoryDetailPagingSource
import com.jankku.wallpapers.network.WallpaperRemoteMediator
import com.jankku.wallpapers.util.Constants.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class WallpaperRepository(
    private val api: AlphaCodersApiService,
    private val database: WallpaperDatabase
) {

    fun fetchWallpapers(sortMethod: String): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { database.wallpaperDao().getAll() },
            remoteMediator = WallpaperRemoteMediator(api, database, sortMethod),
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE + (PAGE_SIZE * 2),
            )
        ).flow
    }

    fun fetchWallpapersFromCategory(category: Category): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { CategoryDetailPagingSource(api, category) },
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE + (PAGE_SIZE * 2),
            )
        ).flow
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
            if (response.success) {
                database.categoryDao().deleteAll()
                database.categoryDao().insertAll(response.categories)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: HttpException) {
            e.printStackTrace()
        }
    }
}