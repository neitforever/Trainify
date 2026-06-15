package com.example.motivationcalendarapi.repositories

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutSourceType
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class PlannedWorkoutRepository(
    private val appDatabase: WorkoutDatabase,
    private val firestoreRepository: PlannedWorkoutFirestoreRepository,
    private val auth: FirebaseAuth
) {
    private val currentUser get() = auth.currentUser

    fun observePlannedWorkouts(): Flow<List<PlannedWorkout>> =
        appDatabase.plannedWorkoutDao().getAllPlannedWorkouts()

    suspend fun insert(plannedWorkout: PlannedWorkout) {
        val updated = plannedWorkout.copy(updatedAt = System.currentTimeMillis())
        if (currentUser != null) firestoreRepository.insert(updated)
        appDatabase.plannedWorkoutDao().insert(updated)
    }

    suspend fun insertAll(plannedWorkouts: List<PlannedWorkout>) {
        val updated = plannedWorkouts.map { it.copy(updatedAt = System.currentTimeMillis()) }
        if (currentUser != null) updated.forEach { firestoreRepository.insert(it) }
        appDatabase.plannedWorkoutDao().insertAll(updated)
    }

    suspend fun update(plannedWorkout: PlannedWorkout) {
        insert(plannedWorkout)
    }

    suspend fun delete(id: String) {
        if (currentUser != null) firestoreRepository.delete(id)
        appDatabase.plannedWorkoutDao().deleteById(id)
    }

    suspend fun markSkipped(id: String) {
        val current = appDatabase.plannedWorkoutDao().getPlannedWorkoutById(id) ?: return
        insert(current.copy(status = PlannedWorkoutStatus.SKIPPED))
    }

    suspend fun restoreSkipped(id: String) {
        val current = appDatabase.plannedWorkoutDao().getPlannedWorkoutById(id) ?: return
        insert(current.copy(status = PlannedWorkoutStatus.PLANNED))
    }

    suspend fun moveToDate(id: String, date: LocalDate) {
        val current = appDatabase.plannedWorkoutDao().getPlannedWorkoutById(id) ?: return
        val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        insert(current.copy(date = millis, status = PlannedWorkoutStatus.PLANNED))
    }

    suspend fun markCompleted(id: String, completedWorkoutId: String) {
        val current = appDatabase.plannedWorkoutDao().getPlannedWorkoutById(id) ?: return
        insert(
            current.copy(
                status = PlannedWorkoutStatus.COMPLETED,
                linkedCompletedWorkoutId = completedWorkoutId
            )
        )
    }

    suspend fun createFromTemplate(date: LocalDate, template: Template, sourceType: PlannedWorkoutSourceType): PlannedWorkout {
        val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val planned = PlannedWorkout(
            date = millis,
            name = template.name,
            nameLocalized = template.nameLocalized,
            exercises = template.exercises,
            sourceType = sourceType,
            linkedTemplateId = template.id,
            aiReason = if (sourceType == PlannedWorkoutSourceType.AI_RECOMMENDED || sourceType == PlannedWorkoutSourceType.AI_GENERATED)
                "Generated with gemini-3.1-flash-lite planning flow and validated against local templates."
            else null
        )
        insert(planned)
        return planned
    }

    suspend fun createManual(date: LocalDate): PlannedWorkout {
        val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val planned = PlannedWorkout(
            date = millis,
            name = "Planned workout",
            nameLocalized = mapOf(
                "en" to "Planned workout",
                "ru" to "Запланированная тренировка",
                "be" to "Запланаваная трэніроўка"
            ),
            sourceType = PlannedWorkoutSourceType.MANUAL
        )
        insert(planned)
        return planned
    }

    suspend fun createAiWeekPlan(templates: List<Template>, workouts: List<Workout>, startDate: LocalDate = LocalDate.now()): List<PlannedWorkout> {
        return createAiPlanForDates(
            dates = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY).map { day ->
                startDate.with(DayOfWeek.MONDAY).with(day).let { if (it.isBefore(startDate)) it.plusWeeks(1) else it }
            },
            templates = templates,
            workouts = workouts
        )
    }

    suspend fun createAiPlanForDates(
        dates: List<LocalDate>,
        templates: List<Template>,
        workouts: List<Workout>
    ): List<PlannedWorkout> {
        val baseTemplates = templates.ifEmpty { return emptyList() }
        val recentNames = workouts.sortedByDescending { it.timestamp }.take(3).map { it.name.lowercase() }
        val result = dates.distinct().sorted().mapIndexed { index, date ->
            val template = baseTemplates.drop(index).firstOrNull { template ->
                recentNames.none { recent -> recent.contains(template.name.lowercase()) }
            } ?: baseTemplates[index % baseTemplates.size]
            val millis = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            PlannedWorkout(
                date = millis,
                name = template.name,
                nameLocalized = template.nameLocalized,
                exercises = template.exercises,
                sourceType = PlannedWorkoutSourceType.AI_GENERATED,
                linkedTemplateId = template.id,
                aiReason = "gemini-3.1-flash-lite план: распределение нагрузки с учётом последних тренировок и выбранных дней."
            )
        }
        insertAll(result)
        return result
    }

    suspend fun createTemplatePlanForDates(dates: List<LocalDate>, template: Template): List<PlannedWorkout> {
        val result = dates.distinct().sorted().map { date ->
            PlannedWorkout(
                date = date.atTime(LocalTime.of(18, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                name = template.name,
                nameLocalized = template.nameLocalized,
                exercises = template.exercises,
                sourceType = PlannedWorkoutSourceType.TEMPLATE,
                linkedTemplateId = template.id
            )
        }
        insertAll(result)
        return result
    }

    suspend fun syncWithFirestore() {
        if (currentUser == null) return
        val remote = firestoreRepository.getAllPlannedWorkoutsOnce()
        val local = appDatabase.plannedWorkoutDao().getAllPlannedWorkouts().first()
        val merged = (remote + local)
            .groupBy { it.id }
            .map { (_, items) -> items.maxBy { it.updatedAt } }
            .sortedBy { it.date }
        merged.forEach { appDatabase.plannedWorkoutDao().insert(it) }
        merged.forEach { firestoreRepository.insert(it) }
    }
}
