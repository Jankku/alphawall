package com.jankku.alphawall.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "count") val count: String,
) : Parcelable
