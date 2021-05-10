package com.jankku.wallpapers.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers")
    fun getAll(): PagingSource<Int, Wallpaper>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallpapers: List<Wallpaper>)

    @Query("DELETE FROM wallpapers")
    suspend fun deleteAll()
}