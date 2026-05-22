package com.example.motivationcalendarapi.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import com.example.motivationcalendarapi.repositories.ai.AiGenerationHighDemandException
import com.example.motivationcalendarapi.repositories.ai.AiGenerationNetworkException
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiGenerationForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private val api = GeminiAiGenerationApi()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationHelper.createNotificationChannels(this)
        when (intent?.action) {
            ACTION_EXERCISE -> runExerciseGeneration(intent)
            ACTION_TEMPLATE -> runTemplateGeneration(intent)
        }
        return START_NOT_STICKY
    }

    private fun runExerciseGeneration(intent: Intent) {
        val prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        val bodyPart = intent.getStringExtra(EXTRA_BODY_PART).orEmpty()
        val equipment = intent.getStringExtra(EXTRA_EQUIPMENT).orEmpty()
        val difficulty = intent.getStringExtra(EXTRA_DIFFICULTY).orEmpty()
        val lang = intent.getStringExtra(EXTRA_LANG).orEmpty().ifBlank { "en" }
        val exercises = parseExercises(intent.getStringExtra(EXTRA_LOCAL_EXERCISES).orEmpty())

        startForeground(
            NotificationType.AI_EXERCISE_CREATED.id,
            buildProgressNotification(getString(R.string.notification_ai_exercise_generating_title), getString(R.string.notification_ai_exercise_generating_text))
        )
        AiGenerationBackgroundState.events.tryEmit(AiGenerationBackgroundEvent.ExerciseStarted)

        scope.launch {
            runCatching {
                api.generateExercise(prompt, bodyPart, equipment, difficulty, lang, exercises)
            }.onSuccess { exercise ->
                AiGenerationBackgroundState.events.emit(AiGenerationBackgroundEvent.ExerciseSuccess(exercise))
                if (NotificationSettingsDataStore(applicationContext).settingsFlow.first().aiExerciseCreatedEnabled) {
                    NotificationHelper.showAiExerciseCreated(applicationContext, exercise.getName(lang))
                }
            }.onFailure { error ->
                val message = generationMessage(error)
                AiGenerationBackgroundState.events.emit(
                    AiGenerationBackgroundEvent.ExerciseFailure(
                        message = message,
                        isNetworkError = error is AiGenerationNetworkException,
                        isHighDemandError = error is AiGenerationHighDemandException
                    )
                )
                if (NotificationSettingsDataStore(applicationContext).settingsFlow.first().aiExerciseCreatedEnabled) {
                    NotificationHelper.showAiExerciseGenerationFailed(applicationContext, message)
                }
            }
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun runTemplateGeneration(intent: Intent) {
        val prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        val bodyParts = intent.getStringArrayListExtra(EXTRA_BODY_PARTS).orEmpty()
        val equipment = intent.getStringArrayListExtra(EXTRA_EQUIPMENT_LIST).orEmpty()
        val difficulty = intent.getStringExtra(EXTRA_DIFFICULTY).orEmpty()
        val minExercises = intent.getIntExtra(EXTRA_MIN_EXERCISES, 4)
        val maxExercises = intent.getIntExtra(EXTRA_MAX_EXERCISES, 6)
        val lang = intent.getStringExtra(EXTRA_LANG).orEmpty().ifBlank { "en" }
        val exercises = parseExercises(intent.getStringExtra(EXTRA_LOCAL_EXERCISES).orEmpty())

        startForeground(
            NotificationType.AI_TEMPLATE_CREATED.id,
            buildProgressNotification(getString(R.string.notification_ai_template_generating_title), getString(R.string.notification_ai_template_generating_text))
        )
        AiGenerationBackgroundState.events.tryEmit(AiGenerationBackgroundEvent.TemplateStarted)

        scope.launch {
            runCatching {
                api.generateTemplate(prompt, bodyParts, equipment, difficulty, minExercises, maxExercises, lang, exercises)
            }.onSuccess { draft ->
                AiGenerationBackgroundState.events.emit(AiGenerationBackgroundEvent.TemplateSuccess(draft))
                if (NotificationSettingsDataStore(applicationContext).settingsFlow.first().aiTemplateCreatedEnabled) {
                    val name = draft.nameLocalized[lang] ?: draft.nameLocalized["en"] ?: draft.nameLocalized.values.firstOrNull().orEmpty()
                    NotificationHelper.showAiTemplateCreated(applicationContext, name)
                }
            }.onFailure { error ->
                val message = generationMessage(error)
                AiGenerationBackgroundState.events.emit(
                    AiGenerationBackgroundEvent.TemplateFailure(
                        message = message,
                        isNetworkError = error is AiGenerationNetworkException,
                        isHighDemandError = error is AiGenerationHighDemandException
                    )
                )
                if (NotificationSettingsDataStore(applicationContext).settingsFlow.first().aiTemplateCreatedEnabled) {
                    NotificationHelper.showAiTemplateGenerationFailed(applicationContext, message)
                }
            }
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun buildProgressNotification(title: String, text: String) = NotificationCompat.Builder(this, NotificationConstants.CHANNEL_AI_GENERATION)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setOngoing(true)
        .setAutoCancel(false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun generationMessage(error: Throwable): String {
        return when (error) {
            is AiGenerationHighDemandException -> getString(R.string.gemini_high_demand_message)
            else -> error.message ?: getString(R.string.ai_generation_error)
        }
    }

    private fun parseExercises(json: String): List<Exercise> {
        return runCatching {
            val type = object : TypeToken<List<Exercise>>() {}.type
            gson.fromJson<List<Exercise>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val ACTION_EXERCISE = "com.example.motivationcalendarapi.notifications.AI_EXERCISE"
        private const val ACTION_TEMPLATE = "com.example.motivationcalendarapi.notifications.AI_TEMPLATE"
        private const val EXTRA_PROMPT = "extra_prompt"
        private const val EXTRA_BODY_PART = "extra_body_part"
        private const val EXTRA_BODY_PARTS = "extra_body_parts"
        private const val EXTRA_EQUIPMENT = "extra_equipment"
        private const val EXTRA_EQUIPMENT_LIST = "extra_equipment_list"
        private const val EXTRA_DIFFICULTY = "extra_difficulty"
        private const val EXTRA_MIN_EXERCISES = "extra_min_exercises"
        private const val EXTRA_MAX_EXERCISES = "extra_max_exercises"
        private const val EXTRA_LANG = "extra_lang"
        private const val EXTRA_LOCAL_EXERCISES = "extra_local_exercises"

        fun startExercise(context: Context, prompt: String, bodyPart: String, equipment: String, difficulty: String, lang: String, localExercises: List<Exercise>) {
            val intent = Intent(context.applicationContext, AiGenerationForegroundService::class.java)
                .setAction(ACTION_EXERCISE)
                .putExtra(EXTRA_PROMPT, prompt)
                .putExtra(EXTRA_BODY_PART, bodyPart)
                .putExtra(EXTRA_EQUIPMENT, equipment)
                .putExtra(EXTRA_DIFFICULTY, difficulty)
                .putExtra(EXTRA_LANG, lang)
                .putExtra(EXTRA_LOCAL_EXERCISES, Gson().toJson(localExercises))
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun startTemplate(context: Context, prompt: String, bodyParts: List<String>, equipment: List<String>, difficulty: String, minExercises: Int, maxExercises: Int, lang: String, localExercises: List<Exercise>) {
            val intent = Intent(context.applicationContext, AiGenerationForegroundService::class.java)
                .setAction(ACTION_TEMPLATE)
                .putExtra(EXTRA_PROMPT, prompt)
                .putStringArrayListExtra(EXTRA_BODY_PARTS, ArrayList(bodyParts))
                .putStringArrayListExtra(EXTRA_EQUIPMENT_LIST, ArrayList(equipment))
                .putExtra(EXTRA_DIFFICULTY, difficulty)
                .putExtra(EXTRA_MIN_EXERCISES, minExercises)
                .putExtra(EXTRA_MAX_EXERCISES, maxExercises)
                .putExtra(EXTRA_LANG, lang)
                .putExtra(EXTRA_LOCAL_EXERCISES, Gson().toJson(localExercises))
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }
    }
}
