package com.jankku.alphawall.network

import com.jankku.alphawall.database.Category
import com.jankku.alphawall.database.Wallpaper
import com.squareup.moshi.Json

/**
 * Response from [AlphaCodersApiService]
 */
data class NetworkWallpaperResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "wallpapers") val wallpapers: List<Wallpaper>,
    @Json(name = "is_last") val isLastPage: Boolean
)

/**
 * Response from [AlphaCodersApiService]
 */
data class NetworkCategoryResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "categories") val categories: List<Category>,
)