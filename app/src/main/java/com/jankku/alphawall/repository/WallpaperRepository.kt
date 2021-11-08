package com.jankku.alphawall.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.jankku.alphawall.database.WallpaperDatabase
import com.jankku.alphawall.database.model.Category
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.network.AlphaCodersApiService
import com.jankku.alphawall.network.CategoryPagingSource
import com.jankku.alphawall.network.SearchPagingSource
import com.jankku.alphawall.network.WallpaperPagingSource
import com.jankku.alphawall.util.Constants.PAGE_SIZE
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(
    private val api: AlphaCodersApiService,
    private val database: WallpaperDatabase
) {

    fun fetchWallpapers(sortMethod: String): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { WallpaperPagingSource(api, sortMethod) },
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE + (PAGE_SIZE * 2)
            )
        ).flow
    }

    fun fetchWallpapersFromCategory(category: Category): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { CategoryPagingSource(api, category) },
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE + (PAGE_SIZE * 2)
            )
        ).flow
    }

    suspend fun saveCategoriesToDatabase() {
        try {
            val response = api.getCategoryList(method = "category_list")
            if (response.success) {
                database.categoryDao().deleteAll()
                database.categoryDao().insertAll(response.categories)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchCategories() = database.categoryDao().getAll()

    fun search(term: String): Flow<PagingData<Wallpaper>> {
        return Pager(
            pagingSourceFactory = { SearchPagingSource(api, term) },
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE * 2,
                initialLoadSize = PAGE_SIZE + (PAGE_SIZE * 2)
            )
        ).flow
    }
}
