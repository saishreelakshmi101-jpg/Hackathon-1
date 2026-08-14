package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val bookingCode: String,
    val resourceId: String,
    val resourceName: String,
    val resourceCategory: String,
    val bookingDate: String, // Format: YYYY-MM-DD
    val startMinutes: Int,
    val endMinutes: Int,
    val startTimeDisplay: String,
    val endTimeDisplay: String,
    val department: String,
    val eventName: String,
    val bookedByName: String,
    val bookedByRole: String, // Faculty, Student Coordinator, HOD, Technical Staff
    val contactEmail: String,
    val contactPhone: String,
    val expectedAttendees: Int,
    val specialRequirements: String = "",
    val status: String = "CONFIRMED", // CONFIRMED, CANCELLED, REJECTED
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayTimeRange: String
        get() = "$startTimeDisplay to $endTimeDisplay"

    val isConfirmed: Boolean
        get() = status.equals("CONFIRMED", ignoreCase = true)

    val isCancelled: Boolean
        get() = status.equals("CANCELLED", ignoreCase = true)
}

sealed class BookingCheckResult {
    data class Available(
        val resource: CampusResource,
        val date: String,
        val slot: TimeSlot,
        val message: String
    ) : BookingCheckResult()

    data class Conflict(
        val resource: CampusResource,
        val date: String,
        val requestedSlot: TimeSlot,
        val conflictingBooking: BookingEntity,
        val suggestedAlternativeSlots: List<TimeSlot>,
        val message: String = "Booking rejected – resource unavailable for selected time."
    ) : BookingCheckResult()

    object Idle : BookingCheckResult()
}
