package com.saurabh.sudoku.utils

import java.text.SimpleDateFormat
import java.util.*
object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getCurrentTimestamp(): Long = System.currentTimeMillis()

    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun getDaysSince(timestamp: Long): Int {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = timestamp

        val daysBetween = (now.timeInMillis - then.timeInMillis) / (1000 * 60 * 60 * 24)
        return daysBetween.toInt()
    }
}