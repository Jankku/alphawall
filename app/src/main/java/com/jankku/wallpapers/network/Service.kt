package com.jankku.wallpapers.network

import android.util.Log
import com.jankku.wallpapers.util.Constants.BASE_URL
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

private val okHttpClient = OkHttpClient
    .Builder()
    .addNetworkInterceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        Log.d("LOG_RESPONSE", response.request().url().toString())

        return@addNetworkInterceptor response
    }
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
    @GET("get.php")
    suspend fun getWallpapers(
        @Query("auth") apiKey: String,
        @Query("method") method: String,
        @Query("page") page: Int,
        @Query("check_last") checkIfLastPage: Int // 1 = true, 0 = false
    ): NetworkResponse

    @Streaming
    @GET
    suspend fun getWallpaper(@Url url: String): Response<ResponseBody>
}

object AlphaCodersApi {
    val wallpaperService: AlphaCodersApiService by lazy {
        retrofit.create(AlphaCodersApiService::class.java)
    }
}
