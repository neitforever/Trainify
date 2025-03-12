package com.example.motivationcalendarapi.network

import com.example.motivationcalendarapi.model.Exercise
import retrofit2.http.GET
import retrofit2.http.Query



interface ExerciseApiService {
    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0
    ): List<Exercise>


}
