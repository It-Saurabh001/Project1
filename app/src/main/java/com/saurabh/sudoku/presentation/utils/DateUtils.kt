package com.saurabh.sudoku.presentation.utils

import java.text.SimpleDateFormat
import java.util.*
object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getCurrentTimestamp(): Long = System.currentTimeMillis()

    fun Long.formatTime(): String {
        if (this <= 0) return "--:--"

        val seconds = this / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            else -> String.format("%02d:%02d", minutes, seconds % 60)
        }
    }

    fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getDaysSince(timestamp: Long): Int {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = timestamp

        val daysBetween = (now.timeInMillis - then.timeInMillis) / (1000 * 60 * 60 * 24)
        return daysBetween.toInt()
    }
}