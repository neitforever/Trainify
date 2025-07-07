package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motivationcalendarapi.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: Workout)

    @Query("SELECT * FROM workout_table")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workout_table WHERE id = :id")
    fun getWorkoutById(id: String): Workout

    @Query("DELETE FROM workout_table WHERE id = :id")
    suspend fun deleteWorkout(id: String)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Query("DELETE FROM workout_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM workout_table WHERE strftime('%Y-%m-%d', timestamp/1000, 'unixepoch', 'localtime') = strftime('%Y-%m-%d', 'now', 'localtime')")
    fun getWorkoutsToday(): Flow<List<Workout>>
}

//
//@Dao
//interface MuscleGroupDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup): Long
//
//    @Query("SELECT * FROM muscle_group_table WHERE id IN (:ids)")
//    suspend fun getMuscleGroupsByIds(ids: List<Long>): List<MuscleGroup>
//
//    @Query("SELECT * FROM muscle_group_table")
//    fun getAllMuscleGroups(): Flow<List<MuscleGroup>>
//}
//
