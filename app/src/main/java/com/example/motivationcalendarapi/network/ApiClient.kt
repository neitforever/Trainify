package com.example.motivationcalendarapi.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://exercisedb.p.rapidapi.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-rapidapi-key", "565670aa5dmsh779fe7329189e32p135852jsn6f94a02c14ed")
                .addHeader("x-rapidapi-host", "exercisedb.p.rapidapi.com")
                .build()
            chain.proceed(request)
        }
        .build()

    val apiService: ExerciseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExerciseApiService::class.java)
    }
}
