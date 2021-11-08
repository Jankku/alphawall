package com.jankku.alphawall.network

import com.jankku.alphawall.BuildConfig
import com.jankku.alphawall.util.Constants.BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val okHttpClient = OkHttpClient
    .Builder()
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
    .build()

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface AlphaCodersApiService {
    @Suppress("LongParameterList")
    @GET("get.php")
    suspend fun getWallpapers(
        @Query("auth") apiKey: String = BuildConfig.apiKey,
        @Query("method") method: String,
        @Query("sort") sort: String,
        @Query("page") page: Int,
        @Query("info_level") infoLevel: Int,
        @Query("check_last") checkIfLastPage: Int // 1 = true, 0 = false
    ): NetworkWallpaperResponse

    @GET("get.php")
    suspend fun getCategoryList(
        @Query("auth") apiKey: String = BuildConfig.apiKey,
        @Query("method") method: String,
    ): NetworkCategoryResponse

    @Suppress("LongParameterList")
    @GET("get.php")
    suspend fun getCategory(
        @Query("auth") apiKey: String = BuildConfig.apiKey,
        @Query("method") method: String,
        @Query("id") id: Int,
        @Query("page") page: Int,
        @Query("info_level") infoLevel: Int,
        @Query("check_last") checkIfLastPage: Int // 1 = true, 0 = false
    ): NetworkWallpaperResponse

    @GET("get.php")
    suspend fun search(
        @Query("auth") apiKey: String = BuildConfig.apiKey,
        @Query("method") method: String,
        @Query("term") term: String,
        @Query("page") page: Int,
        @Query("info_level") infoLevel: Int,
    ): NetworkSearchResponse
}

object AlphaCodersApi {
    val wallpaperService: AlphaCodersApiService by lazy {
        retrofit.create(AlphaCodersApiService::class.java)
    }
}
