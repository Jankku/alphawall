package com.jankku.wallpapers.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jankku.wallpapers.BuildConfig
import com.jankku.wallpapers.database.Category
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.util.Constants.STARTING_PAGE_INDEX

class CategoryDetailPagingSource(
    private val api: AlphaCodersApiService,
    private val category: Category
) : PagingSource<Int, Wallpaper>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
        val nextPage = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = api.getCategory(
                apiKey = BuildConfig.apiKey,
                method = "category",
                id = category.id.toInt(),
                page = nextPage,
                checkIfLastPage = 1
            )

            LoadResult.Page(
                data = response.wallpapers,
                prevKey = if (nextPage == STARTING_PAGE_INDEX) null else nextPage - 1,
                nextKey = if (response.isLastPage) null else nextPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int = STARTING_PAGE_INDEX
}