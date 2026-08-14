package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TimeSlot(
    val title: String,
    val startMinutes: Int, // Minutes from 00:00 (e.g. 14:00 = 840)
    val endMinutes: Int,   // Minutes from 00:00 (e.g. 16:00 = 960)
    val startFormatted: String, // "02:00 PM"
    val endFormatted: String,   // "04:00 PM"
    val isCustom: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalMinutes = endMinutes - startMinutes
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}h ${mins}m"
                hours > 0 -> "${hours} hours"
                else -> "${mins} mins"
            }
        }

    val displayRange: String
        get() = "$startFormatted to $endFormatted"

    val time24Start: String
        get() = minutesTo24h(startMinutes)

    val time24End: String
        get() = minutesTo24h(endMinutes)

    companion object {
        fun minutesTo24h(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }

        fun minutesTo12h(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            val ampm = if (h >= 12) "PM" else "AM"
            val displayHour = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            return String.format(Locale.US, "%02d:%02d %s", displayHour, m, ampm)
        }

        fun parseTimeToMinutes(timeStr: String): Int {
            // Handles "14:00" or "02:00 PM"
            val trimmed = timeStr.trim()
            if (trimmed.contains("AM", ignoreCase = true) || trimmed.contains("PM", ignoreCase = true)) {
                return try {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                    val date = sdf.parse(trimmed) ?: return 0
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                } catch (e: Exception) {
                    0
                }
            } else if (trimmed.contains(":")) {
                val parts = trimmed.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.substring(0, 2)?.toIntOrNull() ?: 0
                return h * 60 + m
            }
            return 0
        }

        fun createFromMinutes(startMinutes: Int, endMinutes: Int, title: String = "Custom Slot"): TimeSlot {
            return TimeSlot(
                title = title,
                startMinutes = startMinutes,
                endMinutes = endMinutes,
                startFormatted = minutesTo12h(startMinutes),
                endFormatted = minutesTo12h(endMinutes),
                isCustom = true
            )
        }

        val PRESET_SLOTS = listOf(
            TimeSlot("Morning Session 1", 9 * 60, 11 * 60, "09:00 AM", "11:00 AM"),
            TimeSlot("Morning Session 2", 11 * 60 + 15, 13 * 60 + 15, "11:15 AM", "01:15 PM"),
            TimeSlot("Afternoon Session", 14 * 60, 16 * 60, "02:00 PM", "04:00 PM"),
            TimeSlot("Evening Session", 16 * 60 + 15, 17 * 60 + 45, "04:15 PM", "05:45 PM"),
            TimeSlot("Half Day (Morning)", 9 * 60, 13 * 60, "09:00 AM", "01:00 PM"),
            TimeSlot("Half Day (Afternoon)", 14 * 60, 18 * 60, "02:00 PM", "06:00 PM"),
            TimeSlot("Full Day Campus Event", 9 * 60, 17 * 60 + 30, "09:00 AM", "05:30 PM")
        )

        fun checkOverlap(
            startA: Int,
            endA: Int,
            startB: Int,
            endB: Int
        ): Boolean {
            // Overlaps if max(startA, startB) < min(endA, endB)
            return kotlin.math.max(startA, startB) < kotlin.math.min(endA, endB)
        }

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun formatDateForDisplay(dateStr: String): String {
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val formatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
                val date = parser.parse(dateStr)
                if (date != null) formatter.format(date) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }
    }
}
