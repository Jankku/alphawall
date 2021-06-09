package com.jankku.alphawall.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.jankku.alphawall.BuildConfig
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.util.Constants.STARTING_PAGE_INDEX

class SearchPagingSource(
    private val api: AlphaCodersApiService,
    private val term: String
) : PagingSource<Int, Wallpaper>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Wallpaper> {
        val page = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = api.search(
                apiKey = BuildConfig.apiKey,
                method = "search",
                term = term,
                page = page,
                infoLevel = 2,
            )
            LoadResult.Page(
                data = response.wallpapers,
                prevKey = null,
                nextKey = if (response.wallpapers.isNullOrEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Wallpaper>): Int = STARTING_PAGE_INDEX

    fun responseHasWallpapersArray(response: NetworkSearchResponse): Boolean {
        return response.javaClass.kotlin.members.any { it.name == "wallpapers" }
    }
}