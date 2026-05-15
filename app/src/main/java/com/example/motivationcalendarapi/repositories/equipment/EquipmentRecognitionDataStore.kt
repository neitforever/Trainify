package com.example.motivationcalendarapi.repositories.equipment

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.equipmentRecognitionDataStore: DataStore<Preferences> by preferencesDataStore(name = "equipment_recognition_cache")

class EquipmentRecognitionDataStore(private val context: Context) {
    private object Keys {
        val LAST_STATE_JSON = stringPreferencesKey("last_state_json")
    }

    val lastStateJsonFlow: Flow<String?> = context.equipmentRecognitionDataStore.data.map { preferences ->
        preferences[Keys.LAST_STATE_JSON]
    }

    suspend fun saveLastState(json: String) {
        context.equipmentRecognitionDataStore.edit { preferences ->
            preferences[Keys.LAST_STATE_JSON] = json
        }
    }

    suspend fun clearLastState() {
        context.equipmentRecognitionDataStore.edit { preferences ->
            preferences.remove(Keys.LAST_STATE_JSON)
        }
    }
}
