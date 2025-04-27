package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.Template
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: Template)

    @Query("SELECT * FROM templates")
    fun getAllTemplates(): Flow<List<Template>>

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    fun getTemplateById(id: String): Flow<Template?>

    @Query("UPDATE templates SET name = :newName WHERE id = :id")
    suspend fun updateTemplateName(id: String, newName: String)
}