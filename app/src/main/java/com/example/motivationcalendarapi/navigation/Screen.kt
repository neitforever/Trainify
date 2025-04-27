import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object AddWorkout : Screen("add_workout", "Workout", Icons.Default.Add)
    object WorkoutHistory : Screen("workout_history", "History", Icons.Filled.Menu)
    object ExercisesView : Screen("exercises", "Exercises", Icons.AutoMirrored.Rounded.List)
    object TemplateDetailView : Screen("template_detail", "Template", Icons.AutoMirrored.Rounded.List)
    object EditTemplateName : Screen("edit_template/{templateId}", "Edit Template Name", Icons.AutoMirrored.Rounded.List)
    object WorkoutDetail : Screen("workout_history", "Workout", Icons.Rounded.Check)
    object ExerciseDetailView : Screen("exercise_detail", "Exercise", Icons.AutoMirrored.Filled.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Add)
    object ThemeSettings : Screen("themes_settings", "Theme", Icons.Default.Add)
    object WorkoutSettings : Screen("workout_settings", "Workout Settings", Icons.Default.Add)
    object SearchExercise : Screen("search_exercise", "Search", Icons.Default.Add)
    object EditExerciseName : Screen("edit_exercise_name", "Edit Name", Icons.Default.Edit)
    object EquipmentSelection : Screen("equipment_selection", "Select Equipment", Icons.Default.List)
    object BodyPartSelection : Screen("body_part_selection", "Select Body Part", Icons.Default.List)
    object SecondaryMusclesSelection : Screen("secondary_muscles_selection", "Secondary Muscles", Icons.Default.List)
    object EditExerciseInstructions : Screen("edit_instructions", "Edit Instructions", Icons.Default.Edit)
    object CreateExercise : Screen("create_exercise", "New Exercise", Icons.Default.Add)
    object Auth : Screen("auth", "Auth", Icons.Default.Add)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountBox)
    object BodyProgress : Screen("body_progress", "Weight progress", Icons.Default.AccountBox)
}