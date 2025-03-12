package com.motivationcalendar.data

import androidx.room.*
import com.example.motivationcalendarapi.model.Workout

import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insert(workout: Workout)

    @Query("SELECT * FROM workout_table")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workout_table WHERE id = :id")
    fun getWorkoutById(id: Long): Workout

    @Query("DELETE FROM workout_table WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Update
    suspend fun updateWorkout(workout: Workout)

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
