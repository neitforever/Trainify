package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: Exercise)

    @Query("SELECT * FROM exercises WHERE bodyPart = :bodyPart ORDER BY isFavorite DESC, name ASC")
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>>

    @Query("SELECT DISTINCT bodyPart FROM exercises")
    fun getAllBodyParts(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int

    @Query("SELECT * FROM exercises")
    fun getAllExercisesWithNotes(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercisesOnce(): List<Exercise>

    @Query("UPDATE exercises SET note = :newNote WHERE id = :id")
    suspend fun updateExerciseNote(id: String, newNote: String)

    @Query("SELECT note FROM exercises WHERE id = :id")
    suspend fun getExerciseNote(id: String): String?

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    fun getExerciseById(id: String): Exercise?

    @Query("SELECT * FROM exercises WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteExercises(): Flow<List<Exercise>>

    @Query("UPDATE exercises SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY isFavorite DESC, name ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Query("UPDATE exercises SET name = :newName WHERE id = :id")
    suspend fun updateExerciseName(id: String, newName: String)


    @Query("SELECT DISTINCT equipment FROM exercises")
    fun getAllEquipment(): Flow<List<String>>

    @Query("UPDATE exercises SET equipment = :newEquipment WHERE id = :id")
    suspend fun updateExerciseEquipment(id: String, newEquipment: String)

    @Query("UPDATE exercises SET bodyPart = :newBodyPart WHERE id = :id")
    suspend fun updateExerciseBodyPart(id: String, newBodyPart: String)


    @Query("UPDATE exercises SET secondaryMuscles = :newSecondaryMuscles WHERE id = :id")
    suspend fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String)

    @Query("SELECT DISTINCT secondaryMuscles FROM exercises")
    fun getAllSecondaryMuscles(): Flow<List<String>>

    @Query("UPDATE exercises SET instructions = :newInstructions WHERE id = :id")
    suspend fun updateExerciseInstructions(id: String, newInstructions: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)
}

