package com.example.simplemusicapp.service

import com.example.simplemusicapp.data.entities.Music
import com.example.simplemusicapp.data.entities.Song
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://storage.googleapis.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface SongsApiService {
    @GET("uamp/catalog.json")
    suspend fun getSongs(): Music
}

object SongsApi {
    val retrofitService: SongsApiService by lazy { retrofit.create(SongsApiService::class.java) }
}