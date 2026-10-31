package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBengaliDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toBengaliDigits(number: Long): String {
        return toBengaliDigits(number.toString())
    }

    fun toBengaliDigits(number: Int): String {
        return toBengaliDigits(number.toString())
    }

    fun toBengaliDigits(number: Double): String {
        val formatted = if (number % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", number)
        } else {
            String.format(Locale.US, "%.1f", number)
        }
        return toBengaliDigits(formatted)
    }

    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val day = SimpleDateFormat("dd", Locale.US).format(date)
        val monthNum = SimpleDateFormat("MM", Locale.US).format(date)
        val year = SimpleDateFormat("yyyy", Locale.US).format(date)

        val monthName = getBengaliMonthName(monthNum.toIntOrNull() ?: 1)
        return "${toBengaliDigits(day)} $monthName, ${toBengaliDigits(year)}"
    }

    fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(date)
        return toBengaliDigits(timeStr)
    }

    fun formatDateTime(timestamp: Long): String {
        return "${formatDate(timestamp)} • ${formatTime(timestamp)}"
    }

    fun getMonthKey(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("yyyy-MM", Locale.US).format(date)
    }

    fun getMonthDisplayName(monthKey: String): String {
        // monthKey format: YYYY-MM
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: 1
            return "${getBengaliMonthName(month)} ${toBengaliDigits(year)}"
        }
        return monthKey
    }

    private fun getBengaliMonthName(month: Int): String {
        return when (month) {
            1 -> "জানুয়ারি"
            2 -> "ফেব্রুয়ারি"
            3 -> "মার্চ"
            4 -> "এপ্রিল"
            5 -> "মে"
            6 -> "জুন"
            7 -> "জুলাই"
            8 -> "আগস্ট"
            9 -> "সেপ্টেম্বর"
            10 -> "অক্টোবর"
            11 -> "নভেম্বর"
            12 -> "ডিসেম্বর"
            else -> "মাস"
        }
    }
}
