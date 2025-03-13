package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_settings")

class TimerDataStore(private val context: Context) {

    companion object {
        val WARMUP_TIME = intPreferencesKey("warmup_time")
        const val DEFAULT_WARMUP_TIME = 60
    }

    val warmupTimeFlow: Flow<Int> = context.timerDataStore.data
        .map { preferences ->
            preferences[WARMUP_TIME] ?: DEFAULT_WARMUP_TIME
        }

    suspend fun saveWarmupTime(time: Int) {
        context.timerDataStore.edit { preferences ->
            preferences[WARMUP_TIME] = time.coerceIn(0, 600)
        }
    }
}