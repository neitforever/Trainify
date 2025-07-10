//package com.example.motivationcalendarapi
//
//import android.content.Context
//import android.util.Log
//import com.example.motivationcalendarapi.model.Exercise
//import com.google.mlkit.nl.translate.TranslateLanguage
//import com.google.mlkit.nl.translate.Translation
//import com.google.mlkit.nl.translate.TranslatorOptions
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.withContext
//
//class ExerciseTranslationRepository(context: Context) {
//    private val firestore = FirebaseFirestore.getInstance()
//    private val russianTranslator = Translation.getClient(
//        TranslatorOptions.Builder()
//            .setSourceLanguage(TranslateLanguage.ENGLISH)
//            .setTargetLanguage(TranslateLanguage.RUSSIAN)
//            .build()
//    )
//
//    private val belarusianTranslator = Translation.getClient(
//        TranslatorOptions.Builder()
//            .setSourceLanguage(TranslateLanguage.ENGLISH)
//            .setTargetLanguage(TranslateLanguage.BELARUSIAN)
//            .build()
//    )
//
//    init {
//        // Загрузка моделей при инициализации
//        russianTranslator.downloadModelIfNeeded()
//        belarusianTranslator.downloadModelIfNeeded()
//    }
//
//    suspend fun uploadAndTranslateExercises(exercises: List<Exercise>) {
//        withContext(Dispatchers.IO) {
//            try {
//                Log.d("Translation", "Начало обработки ${exercises.size} упражнений")
//
//                // Английские упражнения
//                Log.d("Translation", "Загрузка английских версий")
//                uploadExercises("english_exercises", exercises)
//                Log.d("Translation", "Английские версии загружены")
//
//                // Русские упражнения
//                Log.d("Translation", "Начало перевода на русский")
//                val russianExercises = exercises.mapIndexed { index, exercise ->
//                    Log.d("Translation", "Перевод упражнения ${index + 1}/${exercises.size}: ${exercise.id}")
//                    translateExercise(exercise, russianTranslator).apply {
//                        Log.d("Translation", "Упражнение ${exercise.id} переведено на русский")
//                    }
//                }
//                Log.d("Translation", "Все упражнения переведены на русский")
//                uploadExercises("russian_exercises", russianExercises)
//                Log.d("Translation", "Русские версии загружены")
//
//                // Белорусские упражнения
//                Log.d("Translation", "Начало перевода на белорусский")
//                val belarusianExercises = exercises.mapIndexed { index, exercise ->
//                    Log.d("Translation", "Перевод упражнения ${index + 1}/${exercises.size}: ${exercise.id}")
//                    translateExercise(exercise, belarusianTranslator).apply {
//                        Log.d("Translation", "Упражнение ${exercise.id} переведено на белорусский")
//                    }
//                }
//                Log.d("Translation", "Все упражнения переведены на белорусский")
//                uploadExercises("belarusian_exercises", belarusianExercises)
//                Log.d("Translation", "Белорусские версии загружены")
//
//                Log.d("Translation", "Все переводы и загрузки завершены успешно. Обработано упражнений: ${exercises.size}")
//
//            } catch (e: Exception) {
//                Log.e("Translation", "Ошибка в процессе перевода/загрузки", e)
//                throw e
//            }
//        }
//    }
//
//    private suspend fun uploadExercises(collectionName: String, exercises: List<Exercise>) {
//        try {
//            Log.d("Translation", "Начало загрузки $collectionName (${exercises.size} упражнений)")
//            val batch = firestore.batch()
//            exercises.forEach { exercise ->
//                val docRef = firestore.collection(collectionName).document(exercise.id)
//                batch.set(docRef, exercise)
//            }
//            batch.commit().await()
//            Log.d("Translation", "Успешная загрузка $collectionName")
//        } catch (e: Exception) {
//            Log.e("Translation", "Ошибка загрузки $collectionName: ${e.message}", e)
//            throw e
//        }
//    }
//
//    private suspend fun translateExercise(
//        exercise: Exercise,
//        translator: com.google.mlkit.nl.translate.Translator
//    ): Exercise {
//        return try {
//            Log.d("Translation", "Перевод упражнения ${exercise.id}: ${exercise.name}")
//
//            // Логируем количество переводимых элементов
//            Log.d("Translation", "Элементы для перевода: " +
//                    "1 название, " +
//                    "1 часть тела, " +
//                    "1 оборудование, " +
//                    "1 цель, " +
//                    "${exercise.secondaryMuscles.size} второстепенных мышц, " +
//                    "${exercise.instructions.size} инструкций")
//
//            val translatedName = translator.translate(exercise.name).await()
//            val translatedBodyPart = translator.translate(exercise.bodyPart).await()
//            val translatedEquipment = translator.translate(exercise.equipment).await()
//            val translatedTarget = translator.translate(exercise.target).await()
//
//            val translatedSecondaryMuscles = exercise.secondaryMuscles.map {
//                translator.translate(it).await()
//            }
//
//            val translatedInstructions = exercise.instructions.map {
//                translator.translate(it).await()
//            }
//
//            Log.d("Translation", "Перевод завершен для ${exercise.id}")
//
//            exercise.copy(
//                name = translatedName,
//                bodyPart = translatedBodyPart,
//                equipment = translatedEquipment,
//                target = translatedTarget,
//                secondaryMuscles = translatedSecondaryMuscles,
//                instructions = translatedInstructions
//            )
//        } catch (e: Exception) {
//            Log.e("Translation", "Ошибка перевода упражнения ${exercise.id}: ${e.message}", e)
//            throw e
//        }
//    }
//}