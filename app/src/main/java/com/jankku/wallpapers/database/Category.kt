package com.jankku.wallpapers.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "count") val count: String,
)