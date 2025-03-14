package com.example.motivationcalendarapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.tools.screenshot.isValid
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.database.toDatabaseModels
import com.example.motivationcalendarapi.mapper.toEntity
import com.example.motivationcalendarapi.network.ApiClient
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(val exerciseRepository: ExerciseRepository): ViewModel()  {

    val allBodyParts: Flow<List<String>> = exerciseRepository.getAllBodyParts()



    fun getExerciseFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.getExerciseFromApi()
        }
    }

    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
        return exerciseRepository.getExercisesByBodyPart(bodyPart)
    }

    fun getExerciseById(id: String): Exercise? {
        return exerciseRepository.getExerciseById(id)
    }



    fun getFavoriteExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getFavoriteExercises()
    }

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.updateFavoriteStatus(exercise.id, !exercise.isFavorite)
        }
    }

    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseRepository.searchExercises(query)
    }





    val allEquipment: Flow<List<String>> = exerciseRepository.getAllEquipment()

    fun updateExerciseEquipment(id: String, newEquipment: String) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseEquipment(id, newEquipment)
        }
    }



    fun updateExerciseBodyPart(id: String, newBodyPart: String) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseBodyPart(id, newBodyPart)
        }
    }





    val allSecondaryMuscles: Flow<List<String>> = exerciseRepository.getAllSecondaryMuscles()

    fun updateExerciseInstructions(id: String, newInstructions: List<String>) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseInstructions(id, newInstructions)
        }
    }


    fun fetchAndSaveExercises() {
        viewModelScope.launch (Dispatchers.IO){
            try {
                val currentCount = exerciseRepository.getExerciseCount()

                val response = ApiClient.apiService.getExercises()

                val responseEntity= response.map { it.toEntity() }
                if (response.size != currentCount) {
                    if (response.isNotEmpty()) {

                        val exercises = responseEntity.toDatabaseModels()
                        exercises.forEach(){
                            ex->
                            exerciseRepository.insertExercise(ex)

                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _tempExercise = MutableStateFlow<Exercise?>(null)
    val tempExercise: StateFlow<Exercise?> = _tempExercise

    fun initializeNewExercise(id: String) {
        _tempExercise.value = Exercise(
            id = id,
            name = "",
            bodyPart = "",
            equipment = "",
            target = "",
            secondaryMuscles = listOf(),
            instructions = listOf(),
            gifUrl = "",
            isFavorite = false,
            note = ""
        )
    }

    fun finalizeNewExercise() {
        _tempExercise.value?.let { exercise ->
            viewModelScope.launch {
                if (exercise.isValid()) {
                    exerciseRepository.insertExercise(exercise)
                    _tempExercise.value = null
                }
            }
        }
    }
    fun clearTempExercise() {
        _tempExercise.value = null
    }

    fun Exercise.isValid() =
        name.isNotBlank() &&
                equipment.isNotBlank() &&
                bodyPart.isNotBlank() &&
                instructions.isNotEmpty()


    fun updateTempExercise(update: (Exercise) -> Exercise) {
        _tempExercise.value?.let { current ->
            val updated = update(current)
            _tempExercise.value = updated
            Log.d("ExerciseViewModel", "Updated tempExercise: ${updated.name}")
        } ?: run {
            Log.e("ExerciseViewModel", "Attempted to update null tempExercise")
        }
    }

    fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseSecondaryMuscles(id, newSecondaryMuscles)
        }
    }

    fun updateExerciseName(id: String, newName: String) {
        if (id == _tempExercise.value?.id) {
            updateTempExercise { it.copy(name = newName) }
        } else {
            viewModelScope.launch {
                exerciseRepository.updateExerciseName(id, newName)
            }
        }
    }

    fun deleteExercise(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.deleteExercise(id)
        }
    }


}
