package com.jankku.wallpapers.network

import com.jankku.wallpapers.database.Wallpaper
import com.squareup.moshi.Json

/**
 * Response from [AlphaCodersApiService]
 */
data class NetworkResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "wallpapers") val wallpapers: List<Wallpaper>,
    @Json(name = "is_last") val isLastPage: Boolean
)

/**
 * Convert Network results to database objects [Wallpaper]
 */
/*
fun NetworkResponse.asDatabaseModel(): List<Wallpaper> {
    return wallpapers.map {
        Wallpaper(
            id = it.id,
            imageUrl = it.imageUrl,
            pageUrl = it.pageUrl,
            thumbUrl = it.thumbUrl,
            width = it.width,
            height = it.height,
            fileSize = it.fileSize,
            fileType = it.fileType
        )
    }
}*/
