package com.example.motivationcalendarapi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExerciseViewModel(
    val exerciseRepository: ExerciseRepository
): ViewModel()  {

    //Надо
    fun getBodyPartsLocalized(lang: String): Flow<List<String>> {
        return exerciseRepository.getBodyPartsLocalized(lang)
    }
    //Надо
    fun getExercisesLocalizedByBodyPart(bodyPart: String, lang: String): Flow<List<Exercise>> {
        return exerciseRepository.getExercisesLocalizedByBodyPart(bodyPart, lang)
    }



    val allBodyParts: Flow<List<String>> = exerciseRepository.getAllBodyParts()

    init {
        viewModelScope.launch {
            exerciseRepository.initializeExercises()
            exerciseRepository.syncExercisesWithFirestore()
        }
    }


//    private val currentUser get() = auth.currentUser
//
//
//
//    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
//        return exerciseRepository.getExercisesByBodyPart(bodyPart)
//    }

    fun getExerciseById(id: String): Exercise? {
        return exerciseRepository.getExerciseById(id)
    }



    fun getFavoriteExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getFavoriteExercises()
    }
    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.updateFavoriteStatus(
                id = exercise.id,
                isFavorite = !exercise.favorite
            )
        }
    }


    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseRepository.searchExercises(query)
    }





    val allEquipment: Flow<List<String>> = exerciseRepository.getAllEquipment()

    fun updateExerciseEquipment(id: String, newEquipment: Map<String, String>) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseEquipment(id, newEquipment)
        }
    }



    fun updateExerciseBodyPart(id: String, newBodyPart:  Map<String, String>) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseBodyPart(id, newBodyPart)
        }
    }





//    val allSecondaryMuscles: Flow<List<String>> = exerciseRepository.getAllSecondaryMuscles()

    fun updateExerciseInstructions(id: String, newInstructions:  Map<String, List<String>>) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseInstructions(id, newInstructions)
        }
    }




    private val _tempExercise = MutableStateFlow<Exercise?>(null)
    val tempExercise: StateFlow<Exercise?> = _tempExercise

//    fun initializeNewExercise(id: String) {
//        _tempExercise.value = Exercise(
//            id = id,
//            nameLocalized = emptyMap(),
//            bodyPartLocalized = emptyMap(),
//            equipmentLocalized = emptyMap(),
//            targetLocalized = emptyMap(),
//            secondaryMusclesLocalized = emptyMap(),
//            instructionsLocalized = emptyMap(),
//            gifUrl = "",
//            favorite = false,
//            note = ""
//        )
//    }
fun initializeNewExercise(id: String) {
    _tempExercise.value = Exercise(
        id = id,
        nameLocalized = mapOf(
            "en" to "chest",
            "ru" to "груди1",
            "be" to "грудзі"
        ),
        bodyPartLocalized = mapOf(
            "en" to "chest",
            "ru" to "груди1",
            "be" to "грудзі"
        ),
        equipmentLocalized = mapOf(
            "en" to "chest",
            "ru" to "груди1",
            "be" to "грудзі"
        ),
        targetLocalized = mapOf(
            "en" to "chest",
            "ru" to "груди1",
            "be" to "грудзі"
        ),
        secondaryMusclesLocalized = mapOf(
            "en" to listOf("chest"),
            "ru" to listOf("груди1"),
            "be" to listOf("грудзі")
        ),
        instructionsLocalized = mapOf(
            "en" to listOf("chest"),
            "ru" to listOf("груди1"),
            "be" to listOf("грудзі")
        ),
        gifUrl = "geg",
        favorite = false,
        note = "fsdfasdf"
    )
    Log.d("tempExerciseInit", "Initialized exercise: ${_tempExercise.value}")
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
        nameLocalized.isNotEmpty() &&
                equipmentLocalized.isNotEmpty() &&
                bodyPartLocalized.isNotEmpty() &&
                instructionsLocalized.isNotEmpty()


    fun updateTempExercise(update: (Exercise) -> Exercise) {
        _tempExercise.value?.let { current ->
            val updated = update(current)
            _tempExercise.value = updated
//            Log.d("ExerciseViewModel", "Updated tempExercise: ${updated.getName(lang)}")
        } ?: run {
            Log.e("ExerciseViewModel", "Attempted to update null tempExercise")
        }
    }

//    fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
//        viewModelScope.launch {
//            exerciseRepository.updateExerciseSecondaryMuscles(id, newSecondaryMuscles)
//        }
//    }

    fun updateExerciseName(id: String, newName: Map<String, String>) {
        if (id == _tempExercise.value?.id) {
            updateTempExercise { it.copy(nameLocalized = newName) }
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
