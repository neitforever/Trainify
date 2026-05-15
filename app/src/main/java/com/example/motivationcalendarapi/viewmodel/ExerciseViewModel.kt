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
import kotlinx.coroutines.flow.asStateFlow
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




    init {
        viewModelScope.launch {
            exerciseRepository.initializeExercises()
            exerciseRepository.syncMissingExercisesFromFirestore()
        }
    }

    fun getAllEquipmentLocalized(lang: String): Flow<List<String>> {
        return exerciseRepository.getAllEquipmentLocalized(lang)
    }

//    private val currentUser get() = auth.currentUser
//
//
//
//    fun getExercisesByBodyPart(bodyPart: String): Flow<List<Exercise>> {
//        return exerciseRepository.getExercisesByBodyPart(bodyPart)
//    }

    suspend fun getExerciseById(id: String): Exercise? {
        return exerciseRepository.getExerciseById(id)
    }




    fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getAllExercises()
    }

    fun getFavoriteExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getFavoriteExercises()
    }

    private val _isRefreshingExercisesFromFirestore = MutableStateFlow(false)
    val isRefreshingExercisesFromFirestore: StateFlow<Boolean> = _isRefreshingExercisesFromFirestore.asStateFlow()

    fun refreshMissingExercisesFromFirestore() {
        if (_isRefreshingExercisesFromFirestore.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshingExercisesFromFirestore.value = true
            try {
                exerciseRepository.syncMissingExercisesFromFirestore()
            } catch (e: Exception) {
                Log.e("ExerciseDebug", "refreshMissingExercisesFromFirestore ERROR", e)
            } finally {
                _isRefreshingExercisesFromFirestore.value = false
            }
        }
    }

    fun toggleFavorite(exercise: Exercise) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.updateFavoriteStatus(
                id = exercise.id,
                isFavorite = !exercise.favorite
            )
        }
    }


    fun searchExercises(query: String, lang: String): Flow<List<Exercise>> {
        return exerciseRepository.searchExercises(query, lang)
    }













//    val allSecondaryMuscles: Flow<List<String>> = exerciseRepository.getAllSecondaryMuscles()



    private val _tempExercise = MutableStateFlow<Exercise?>(null)
    val tempExercise: StateFlow<Exercise?> = _tempExercise


    fun initializeNewExercise(id: String) {
        _tempExercise.value = Exercise(
            id = id,
            nameLocalized = emptyMap(),
            bodyPartLocalized = emptyMap(),
            equipmentLocalized = emptyMap(),
            targetLocalized = emptyMap(),
            secondaryMusclesLocalized = emptyMap(),
            instructionsLocalized = emptyMap(),
            gifUrl = "",
            favorite = false,
            note = ""
        )
        Log.d("ExerciseDebug", "initializeNewExercise: ${_tempExercise.value}")
    }
//fun initializeNewExercise(id: String) {
//    _tempExercise.value = Exercise(
//        id = id,
//        nameLocalized = mapOf(
//            "en" to "",
//            "ru" to "",
//            "be" to ""
//        ),
//        bodyPartLocalized = mapOf(
//            "en" to "",
//            "ru" to "",
//            "be" to ""
//        ),
//        equipmentLocalized = mapOf(
//            "en" to "",
//            "ru" to "",
//            "be" to ""
//        ),
//        targetLocalized = mapOf(
//            "en" to "",
//            "ru" to "",
//            "be" to ""
//        ),
//        secondaryMusclesLocalized = mapOf(
//            "en" to emptyList(),
//            "ru" to emptyList(),
//            "be" to emptyList()
//        ),
//        instructionsLocalized = mapOf(
//            "en" to emptyList(),
//            "ru" to emptyList(),
//            "be" to emptyList()
//        ),
//        gifUrl = "",
//        favorite = false,
//        note = ""
//    )
//    Log.d("tempExerciseInit", "Initialized exercise: ${_tempExercise.value}")
//}

    fun finalizeNewExercise(
        lang: String,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val temp = _tempExercise.value
        if (temp == null) {
            Log.e("ExerciseDebug", "finalizeNewExercise: tempExercise is null")
            onError()
            return
        }

        val normalized = temp.normalizedForSave(lang)

        if (!normalized.isValidForCreate(lang)) {
            Log.e("ExerciseDebug", "finalizeNewExercise: invalid data -> $normalized")
            onError()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("ExerciseDebug", "finalizeNewExercise START: ${normalized.id}")
                exerciseRepository.insertExercise(normalized)
                _tempExercise.value = null
                Log.d("ExerciseDebug", "finalizeNewExercise SUCCESS: ${normalized.id}")
                launch(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("ExerciseDebug", "finalizeNewExercise ERROR", e)
                launch(Dispatchers.Main) {
                    onError()
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




//    fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
//        viewModelScope.launch {
//            exerciseRepository.updateExerciseSecondaryMuscles(id, newSecondaryMuscles)
//        }
//    }

    fun updateExerciseName(id: String, newName: Map<String, String>) {
        android.util.Log.d("ExerciseDebug", "VM updateExerciseName START id=$id newName=$newName tempId=${_tempExercise.value?.id}")

        if (id == _tempExercise.value?.id) {
            android.util.Log.d("ExerciseDebug", "VM updateExerciseName -> tempExercise branch")
            updateTempExercise { it.copy(nameLocalized = newName) }
        } else {
            viewModelScope.launch {
                try {
                    android.util.Log.d("ExerciseDebug", "VM updateExerciseName -> repository branch")
                    exerciseRepository.updateExerciseName(id, newName)
                    android.util.Log.d("ExerciseDebug", "VM updateExerciseName SUCCESS")
                } catch (e: Exception) {
                    android.util.Log.e("ExerciseDebug", "VM updateExerciseName ERROR", e)
                }
            }
        }
    }
    fun updateExerciseEquipment(id: String, newEquipment: Map<String, String>) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ExerciseDebug", "VM updateExerciseEquipment START id=$id newEquipment=$newEquipment")
                exerciseRepository.updateExerciseEquipment(id, newEquipment)
                android.util.Log.d("ExerciseDebug", "VM updateExerciseEquipment SUCCESS")
            } catch (e: Exception) {
                android.util.Log.e("ExerciseDebug", "VM updateExerciseEquipment ERROR", e)
            }
        }
    }

    fun updateExerciseBodyPart(id: String, newBodyPart: Map<String, String>) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ExerciseDebug", "VM updateExerciseBodyPart START id=$id newBodyPart=$newBodyPart")
                exerciseRepository.updateExerciseBodyPart(id, newBodyPart)
                android.util.Log.d("ExerciseDebug", "VM updateExerciseBodyPart SUCCESS")
            } catch (e: Exception) {
                android.util.Log.e("ExerciseDebug", "VM updateExerciseBodyPart ERROR", e)
            }
        }
    }

    fun updateExerciseInstructions(id: String, newInstructions: Map<String, List<String>>) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ExerciseDebug", "VM updateExerciseInstructions START id=$id newInstructions=$newInstructions")
                exerciseRepository.updateExerciseInstructions(id, newInstructions)
                android.util.Log.d("ExerciseDebug", "VM updateExerciseInstructions SUCCESS")
            } catch (e: Exception) {
                android.util.Log.e("ExerciseDebug", "VM updateExerciseInstructions ERROR", e)
            }
        }
    }

    fun updateTempExercise(update: (Exercise) -> Exercise) {
        _tempExercise.value?.let { current ->
            val updated = update(current)
            _tempExercise.value = updated
            Log.d("ExerciseDebug", "updateTempExercise: $updated")
        } ?: run {
            Log.e("ExerciseDebug", "updateTempExercise: tempExercise is null")
        }
    }


    fun deleteExercise(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            exerciseRepository.deleteExercise(id)
        }
    }



}
