package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesOnce(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: String): Exercise?

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseByIdFlow(id: String): Flow<Exercise?>

    @Query("SELECT * FROM exercises WHERE favorite = 1 ORDER BY nameLocalized ASC")
    fun getFavoriteExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    fun getAllExercisesWithNotes(): Flow<List<Exercise>>

    @Query("SELECT note FROM exercises WHERE id = :id")
    suspend fun getExerciseNote(id: String): String?

    @Query("UPDATE exercises SET note = :newNote WHERE id = :id")
    suspend fun updateExerciseNote(id: String, newNote: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<Exercise>)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}