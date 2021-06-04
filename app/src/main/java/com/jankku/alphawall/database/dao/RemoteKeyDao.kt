package com.jankku.alphawall.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jankku.alphawall.database.model.RemoteKey

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE id = :id")
    suspend fun remoteKeysId(id: String): RemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKey>)

    @Query("DELETE FROM remote_keys")
    suspend fun deleteAll()
}