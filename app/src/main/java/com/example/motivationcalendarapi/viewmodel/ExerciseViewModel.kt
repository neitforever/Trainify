package com.example.motivationcalendarapi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.mapper.toEntity
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.network.ApiClient
import com.example.motivationcalendarapi.repositories.ExerciseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ExerciseViewModel(val exerciseRepository: ExerciseRepository ,   private val auth: FirebaseAuth, private val context: Context
): ViewModel()  {

    val allBodyParts: Flow<List<String>> = exerciseRepository.getAllBodyParts()

    init {
        viewModelScope.launch {
            exerciseRepository.syncExercisesWithFirestore()
        }
    }

    private val currentUser get() = auth.currentUser



    ///
//    private val translationRepo by lazy { ExerciseTranslationRepository(context) }
//
//    // Новая функция для загрузки и перевода упражнений
//    fun uploadExercisesToFirestore() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val exercises = exerciseRepository.getAllExercisesOnce()
//                Log.d("Translation", "Найдено упражнений для перевода: ${exercises.size}")
//                if (exercises.isNotEmpty()) {
//                    translationRepo.uploadAndTranslateExercises(exercises)
//                } else {
//                    Log.d("Translation", "Нет упражнений для перевода")
//                }
//            } catch (e: Exception) {
//                Log.e("Translation", "Ошибка при подготовке перевода", e)
//            }
//        }
//    }

//    fun testTranslation(
//        text: String,
//        onRussianTranslated: (String) -> Unit,
//        onBelarusianTranslated: (String) -> Unit
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Создаем переводчики
//                val russianTranslator = Translation.getClient(
//                    TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.ENGLISH)
//                        .setTargetLanguage(TranslateLanguage.RUSSIAN)
//                        .build()
//                )
//
//                val belarusianTranslator = Translation.getClient(
//                    TranslatorOptions.Builder()
//                        .setSourceLanguage(TranslateLanguage.ENGLISH)
//                        .setTargetLanguage(TranslateLanguage.BELARUSIAN)
//                        .build()
//                )
//
//
//                // Загружаем модели
//                russianTranslator.downloadModelIfNeeded().await()
//                belarusianTranslator.downloadModelIfNeeded().await()
//
//                // Выполняем перевод
//                val russian = russianTranslator.translate(text).await()
//                onRussianTranslated(russian)
//
//                val belarusian = belarusianTranslator.translate(text).await()
//                onBelarusianTranslated(belarusian)
//
//                // Закрываем переводчики
//                russianTranslator.close()
//                belarusianTranslator.close()
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                onBelarusianTranslated("Translation error: ${e.message}")
//            }
//        }
//    }
    ///
    suspend fun fetchAndSaveExercises() {
        try {
            if (currentUser != null) {
                exerciseRepository.syncExercisesWithFirestore()
            }

            val localCount = exerciseRepository.getExerciseCount()
            if (localCount == 0) {
                val response = ApiClient.apiService.getExercises()
                val exercises = response.map { it.toEntity() }
                exercises.forEach { ex ->
                    exerciseRepository.insertExercise(ex.copy(
                        favorite = false // Явно инициализируем isFavorite
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

//    fun getExerciseFromApi() {
//        viewModelScope.launch(Dispatchers.IO) {
//            exerciseRepository.getExerciseFromApi()
//        }
//    }

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





//    val allSecondaryMuscles: Flow<List<String>> = exerciseRepository.getAllSecondaryMuscles()

    fun updateExerciseInstructions(id: String, newInstructions: List<String>) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseInstructions(id, newInstructions)
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
            favorite = false,
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

//    fun updateExerciseSecondaryMuscles(id: String, newSecondaryMuscles: String) {
//        viewModelScope.launch {
//            exerciseRepository.updateExerciseSecondaryMuscles(id, newSecondaryMuscles)
//        }
//    }

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
