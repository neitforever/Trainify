package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    //Надо
    @Query("SELECT * FROM exercises")
    fun getAllExercisesFlow(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: Exercise)

    @Query("SELECT * FROM exercises WHERE bodyPartLocalized = :bodyPart ORDER BY favorite DESC, nameLocalized ASC")
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>>

    @Query("SELECT DISTINCT bodyPartLocalized FROM exercises")
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

    @Query("SELECT * FROM exercises WHERE favorite = 1 ORDER BY nameLocalized ASC")
    fun getFavoriteExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercises(exercises: List<Exercise>)

    @Query("UPDATE exercises SET favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("SELECT * FROM exercises WHERE nameLocalized LIKE '%' || :query || '%' ORDER BY favorite DESC, nameLocalized ASC")
    fun searchExercises(query: String): Flow<List<Exercise>>

    @Query("UPDATE exercises SET nameLocalized = :newName WHERE id = :id")
    suspend fun updateExerciseName(id: String, newName: String)


    @Query("SELECT DISTINCT equipmentLocalized FROM exercises")
    fun getAllEquipment(): Flow<List<String>>

    @Query("UPDATE exercises SET equipmentLocalized = :newEquipment WHERE id = :id")
    suspend fun updateExerciseEquipment(id: String, newEquipment: String)

    @Query("UPDATE exercises SET bodyPartLocalized = :newBodyPart WHERE id = :id")
    suspend fun updateExerciseBodyPart(id: String, newBodyPart: String)


    @Query("UPDATE exercises SET secondaryMusclesLocalized = :newSecondaryMuscles WHERE id = :id")
    suspend fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String)

    @Query("SELECT DISTINCT secondaryMusclesLocalized FROM exercises")
    fun getAllSecondaryMuscles(): Flow<List<String>>

    @Query("UPDATE exercises SET instructionsLocalized = :newInstructions WHERE id = :id")
    suspend fun updateExerciseInstructions(id: String, newInstructions: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExercise(id: String)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}

