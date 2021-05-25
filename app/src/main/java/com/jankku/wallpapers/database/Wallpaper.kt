package com.jankku.wallpapers.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "wallpapers")
data class Wallpaper(
    @PrimaryKey
    @Json(name = "id") val id: String,
    @Json(name = "url_image") val imageUrl: String,
    @Json(name = "url_page") val pageUrl: String,
    @Json(name = "url_thumb") val thumbUrl: String,
    @Json(name = "width") val width: String,
    @Json(name = "height") val height: String,
    @Json(name = "file_size") val fileSize: String,
    @Json(name = "category") val category: String,
    @Json(name = "sub_category") val subCategory: String,
    @Json(name = "user_name") val userName: String,
) : Parcelable
