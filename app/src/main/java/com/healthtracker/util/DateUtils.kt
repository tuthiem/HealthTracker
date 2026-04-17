package com.healthtracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val shortDateFormat = SimpleDateFormat("MM/dd", Locale.US)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatShortDate(timestamp: Long): String = shortDateFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis
        return start to end
    }

    fun daysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.timeInMillis
    }
}
