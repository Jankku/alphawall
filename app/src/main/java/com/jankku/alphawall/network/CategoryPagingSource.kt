package com.jankku.alphawall.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jankku.alphawall.database.model.Category
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.util.Constants.STARTING_PAGE_INDEX

class CategoryPagingSource(
    private val api: AlphaCodersApiService,
    private val category: Category
) : PagingSource<Int, Wallpaper>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
        val page = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = api.getCategory(
                method = "category",
                id = category.id.toInt(),
                page = page,
                infoLevel = 2,
                checkIfLastPage = 1
            )

            LoadResult.Page(
                data = response.wallpapers,
                prevKey = null,
                nextKey = if (response.isLastPage) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int = STARTING_PAGE_INDEX
}