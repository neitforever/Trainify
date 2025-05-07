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



    object Auth : Screen("auth", "Auth", Icons.Default.Add)
    //Profile
    object Profile : Screen("profile", "Profile", Icons.Default.AccountBox)
    //Weight progress
    object BodyProgress : Screen("body_progress", "Weight progress", Icons.Default.AccountBox)
    //Workout
    object AddWorkout : Screen("workout", "Workout", Icons.Default.Add)
    //History
    object WorkoutHistory : Screen("workout_history", "History", Icons.Filled.Menu)
    object WorkoutDetail : Screen("workout_history", "Workout", Icons.Rounded.Check)
    //Exercises
    object ExercisesView : Screen("exercises", "Exercises", Icons.AutoMirrored.Rounded.List)
    object TemplateDetailView : Screen("exercises/template_detail", "Template", Icons.AutoMirrored.Rounded.List)
    object EditTemplateName : Screen("exercises/edit_template/{templateId}", "Edit Template Name", Icons.AutoMirrored.Rounded.List)
    object ExerciseDetailView : Screen("exercises/exercise_detail", "Exercise", Icons.AutoMirrored.Filled.List)
    object SearchExercise : Screen("exercises/search_exercise", "Search", Icons.Default.Add)
    object CreateExercise : Screen("exercises/create_exercise", "New Exercise", Icons.Default.Add)

    object EditExerciseName : Screen("exercises/edit_exercise_name", "Edit Name", Icons.Default.Edit)
    object EquipmentSelection : Screen("exercises/equipment_selection", "Select Equipment", Icons.Default.List)
    object BodyPartSelection : Screen("exercises/body_part_selection", "Select Body Part", Icons.Default.List)
    object EditExerciseInstructions : Screen("exercises/edit_instructions", "Edit Instructions", Icons.Default.Edit)
    //Settings
    object Settings : Screen("settings", "Settings", Icons.Default.Add)
    object ThemeSettings : Screen("settings/themes", "Theme Settings", Icons.Default.Add)
    object WorkoutSettings : Screen("settings/workout", "Workout Settings", Icons.Default.Add)
}