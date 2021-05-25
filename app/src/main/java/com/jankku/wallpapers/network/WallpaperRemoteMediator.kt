package com.jankku.wallpapers.network

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.jankku.wallpapers.BuildConfig
import com.jankku.wallpapers.database.RemoteKey
import com.jankku.wallpapers.database.Wallpaper
import com.jankku.wallpapers.database.WallpaperDatabase
import com.jankku.wallpapers.util.Constants.STARTING_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException

@ExperimentalPagingApi
class WallpaperRemoteMediator(
    private val api: AlphaCodersApiService,
    private val database: WallpaperDatabase,
    private val sortMethod: String
) : RemoteMediator<Int, Wallpaper>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, Wallpaper>
    ): MediatorResult {

        val page = when (val pageKeyData = getKeyPageData(loadType, state)) {
            is MediatorResult.Success -> {
                return pageKeyData
            }
            else -> {
                pageKeyData as Int
            }
        }

        try {
            val response = api.getWallpapers(
                apiKey = BuildConfig.apiKey,
                method = "featured",
                sort = sortMethod,
                page = page,
                infoLevel = 2,
                checkIfLastPage = 1 // 1 = true, 0 = false

            )
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.wallpaperDao().deleteAll()
                    database.remoteKeyDao().deleteAll()
                }
                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (response.isLastPage) null else page + 1

                val keys = response.wallpapers.map {
                    RemoteKey(it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.wallpaperDao().insertAll(response.wallpapers)
                database.remoteKeyDao().insertAll(keys)
            }
            return MediatorResult.Success(endOfPaginationReached = response.isLastPage)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getKeyPageData(
        loadType: LoadType,
        state: PagingState<Int, Wallpaper>
    ): Any {
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
            }
            LoadType.APPEND -> {
                val remoteKeys = getLastRemoteKey(state)
                val nextKey = remoteKeys?.nextKey
                nextKey ?: MediatorResult.Success(endOfPaginationReached = false)
            }
            LoadType.PREPEND -> {
                MediatorResult.Success(endOfPaginationReached = false)
//                val remoteKeys = getFirstRemoteKey(state)
//                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = false)
//                prevKey
            }
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Wallpaper>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeyDao().remoteKeysId(id)
            }
        }
    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, Wallpaper>): RemoteKey? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { wallpaper -> database.remoteKeyDao().remoteKeysId(wallpaper.id) }
    }

    private suspend fun getFirstRemoteKey(state: PagingState<Int, Wallpaper>): RemoteKey? {
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { wallpaper -> database.remoteKeyDao().remoteKeysId(wallpaper.id) }
    }
}