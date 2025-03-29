import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatTime(seconds: Int?): String {
    return String.format("%02d:%02d", seconds!! / 60, seconds!! % 60)
}


fun formatDate(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

fun getWeekOfMonth(timestamp: Long?): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp!!

    calendar.firstDayOfWeek = Calendar.MONDAY
    val weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)

    return "$weekOfMonth"
}
