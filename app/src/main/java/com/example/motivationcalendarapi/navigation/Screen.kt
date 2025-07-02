import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.motivationcalendarapi.R

sealed class Screen(
    val route: String,
    @StringRes private val title: Int,
    val icon: ImageVector
) {
    fun getTitle(context: Context): String = context.getString(title)

    // Auth
    object Auth : Screen("auth", R.string.auth, Icons.Default.Add)

    // Profile
    object Profile : Screen("profile", R.string.profile, Icons.Default.AccountBox)

    // Weight Progress
    object BodyProgress : Screen("body_progress", R.string.weight_progress, Icons.Default.Add)

    // Workout
    object AddWorkout : Screen("workout", R.string.workout, Icons.Default.Add)

    // History
    object WorkoutHistory : Screen("workout_history", R.string.history, Icons.Filled.Menu)
    object WorkoutDetail : Screen("workout_detail", R.string.workout, Icons.Rounded.Check)

    // Exercises
    object ExercisesView : Screen("exercises", R.string.exercise, Icons.AutoMirrored.Rounded.List)
    object TemplateDetailView : Screen("template_detail", R.string.template, Icons.AutoMirrored.Rounded.List)
    object EditTemplateName : Screen("edit_template/{templateId}", R.string.edit_template_name, Icons.Default.Edit)
    object ExerciseDetailView : Screen("exercise_detail", R.string.exercise, Icons.Default.Edit)
    object SearchExercise : Screen("search_exercise", R.string.search, Icons.Default.Search)
    object CreateExercise : Screen("create_exercise", R.string.new_exercise, Icons.Default.Add)
    object EditExerciseName : Screen("edit_exercise_name", R.string.edit_name, Icons.Default.Edit)
    object EquipmentSelection : Screen("equipment_selection", R.string.select_equipment, Icons.Default.List)
    object BodyPartSelection : Screen("body_part_selection", R.string.select_body_part, Icons.Default.List)
    object EditExerciseInstructions : Screen("edit_instructions", R.string.edit_instructions, Icons.Default.Edit)

    // Settings
    object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)
    object ThemeSettings : Screen("settings_themes", R.string.theme_settings, Icons.Default.Add)
    object WorkoutSettings : Screen("settings_workout", R.string.workout_settings, Icons.Default.Add)
}