package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannedWorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plannedWorkout: PlannedWorkout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plannedWorkouts: List<PlannedWorkout>)

    @Update
    suspend fun update(plannedWorkout: PlannedWorkout)

    @Query("DELETE FROM planned_workouts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM planned_workouts ORDER BY date ASC, createdAt ASC, id ASC")
    fun getAllPlannedWorkouts(): Flow<List<PlannedWorkout>>

    @Query("SELECT * FROM planned_workouts WHERE id = :id LIMIT 1")
    suspend fun getPlannedWorkoutById(id: String): PlannedWorkout?

    @Query("SELECT * FROM planned_workouts WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, createdAt ASC, id ASC")
    fun getPlannedWorkoutsBetween(startDate: Long, endDate: Long): Flow<List<PlannedWorkout>>
}
