import android.content.Context
import androidx.annotation.StringRes
import com.example.motivationcalendarapi.R

sealed class Screen(
    val route: String,
    @StringRes private val title: Int
) {
    fun getTitle(context: Context): String = context.getString(title)

    // Auth
    object Auth : Screen("auth", R.string.auth)

    // Profile
    object Profile : Screen("profile", R.string.profile)

    // Weight Progress
    object BodyProgress : Screen("body_progress", R.string.weight_progress)

    // Workout
    object AddWorkout : Screen("workout", R.string.workout)

    // History
    object WorkoutHistory : Screen("workout_history", R.string.history)
    object WorkoutDetail : Screen("workout_detail", R.string.workout)

    // Exercises
    object ExercisesView : Screen("exercises", R.string.exercise)
    object TemplateDetailView : Screen("template_detail", R.string.template)
    object EditTemplateName : Screen("edit_template/{templateId}", R.string.edit_template_name)
    object ExerciseDetailView : Screen("exercise_detail", R.string.exercise)
    object SearchExercise : Screen("search_exercise", R.string.search)
    object CreateExercise : Screen("create_exercise", R.string.new_exercise)
    object EditExerciseName : Screen("edit_exercise_name", R.string.edit_name)
    object EquipmentSelection : Screen("equipment_selection", R.string.select_equipment)
    object BodyPartSelection : Screen("body_part_selection", R.string.select_body_part)
    object EditExerciseInstructions : Screen("edit_instructions", R.string.edit_instructions)

    // Settings
    object Settings : Screen("settings", R.string.settings)
    object ThemeSettings : Screen("settings_themes", R.string.theme_settings)
    object WorkoutSettings : Screen("settings_workout", R.string.workout_settings)
    object LanguageSettings : Screen("settings_language", R.string.language_settings)
}