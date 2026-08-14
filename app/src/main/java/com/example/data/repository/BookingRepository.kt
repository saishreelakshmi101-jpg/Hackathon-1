package com.example.data.repository

import com.example.data.local.BookingDao
import com.example.data.model.BookingCheckResult
import com.example.data.model.BookingEntity
import com.example.data.model.CampusResource
import com.example.data.model.CampusResourceCatalog
import com.example.data.model.TimeSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class BookingRepository(private val bookingDao: BookingDao) {

    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookingsFlow()

    fun getConfirmedBookingsForDate(date: String): Flow<List<BookingEntity>> {
        return bookingDao.getConfirmedBookingsForDateFlow(date)
    }

    fun getAllResources(): List<CampusResource> {
        return CampusResourceCatalog.resources
    }

    fun getResourceById(id: String): CampusResource? {
        return CampusResourceCatalog.getResourceById(id)
    }

    suspend fun checkAvailability(
        resourceId: String,
        date: String,
        slot: TimeSlot
    ): BookingCheckResult = withContext(Dispatchers.IO) {
        val resource = getResourceById(resourceId) ?: return@withContext BookingCheckResult.Idle

        // Check if there is an overlapping confirmed booking
        val conflict = bookingDao.findConflict(
            resourceId = resourceId,
            date = date,
            reqStart = slot.startMinutes,
            reqEnd = slot.endMinutes
        )

        if (conflict != null) {
            val suggestedAlternatives = findSuggestedAlternativeSlots(resourceId, date, slot)
            BookingCheckResult.Conflict(
                resource = resource,
                date = date,
                requestedSlot = slot,
                conflictingBooking = conflict,
                suggestedAlternativeSlots = suggestedAlternatives,
                message = "Booking rejected – resource unavailable for selected time."
            )
        } else {
            val resourceShortName = resource.name.split("–", "-").firstOrNull()?.trim() ?: resource.name
            BookingCheckResult.Available(
                resource = resource,
                date = date,
                slot = slot,
                message = "Available\n$resourceShortName – ${slot.displayRange}\nBooking Status: READY TO CONFIRM"
            )
        }
    }

    suspend fun findSuggestedAlternativeSlots(
        resourceId: String,
        date: String,
        requestedSlot: TimeSlot? = null
    ): List<TimeSlot> = withContext(Dispatchers.IO) {
        val existingBookings = bookingDao.getConfirmedBookingsForResourceAndDate(resourceId, date)
        val availablePresets = mutableListOf<TimeSlot>()

        for (preset in TimeSlot.PRESET_SLOTS) {
            if (requestedSlot != null && preset.startMinutes == requestedSlot.startMinutes && preset.endMinutes == requestedSlot.endMinutes) {
                continue // skip the one user just requested
            }
            val hasOverlap = existingBookings.any { b ->
                TimeSlot.checkOverlap(b.startMinutes, b.endMinutes, preset.startMinutes, preset.endMinutes)
            }
            if (!hasOverlap) {
                availablePresets.add(preset)
            }
        }
        availablePresets.take(3)
    }

    suspend fun confirmBooking(
        resource: CampusResource,
        date: String,
        slot: TimeSlot,
        department: String,
        eventName: String,
        bookedByName: String,
        bookedByRole: String,
        contactEmail: String,
        contactPhone: String,
        expectedAttendees: Int,
        specialRequirements: String
    ): Result<BookingEntity> = withContext(Dispatchers.IO) {
        // Real-time conflict re-check before write to prevent race conditions
        val conflict = bookingDao.findConflict(
            resourceId = resource.id,
            date = date,
            reqStart = slot.startMinutes,
            reqEnd = slot.endMinutes
        )

        if (conflict != null) {
            return@withContext Result.failure(
                IllegalStateException("Booking rejected – resource unavailable for selected time.")
            )
        }

        val randomCode = String.format(Locale.US, "JIT-%04d-%03d", Calendar.getInstance().get(Calendar.YEAR), Random.nextInt(1000, 9999))
        val newBooking = BookingEntity(
            bookingCode = randomCode,
            resourceId = resource.id,
            resourceName = resource.name,
            resourceCategory = resource.category.displayName,
            bookingDate = date,
            startMinutes = slot.startMinutes,
            endMinutes = slot.endMinutes,
            startTimeDisplay = slot.startFormatted,
            endTimeDisplay = slot.endFormatted,
            department = department.trim(),
            eventName = eventName.trim(),
            bookedByName = bookedByName.trim(),
            bookedByRole = bookedByRole.trim(),
            contactEmail = contactEmail.trim(),
            contactPhone = contactPhone.trim(),
            expectedAttendees = expectedAttendees,
            specialRequirements = specialRequirements.trim(),
            status = "CONFIRMED",
            createdAt = System.currentTimeMillis()
        )

        val id = bookingDao.insertBooking(newBooking)
        val savedBooking = newBooking.copy(id = id)
        Result.success(savedBooking)
    }

    suspend fun cancelBooking(bookingId: Long) = withContext(Dispatchers.IO) {
        bookingDao.cancelBooking(bookingId)
    }

    suspend fun deleteBooking(bookingId: Long) = withContext(Dispatchers.IO) {
        bookingDao.deleteBooking(bookingId)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (bookingDao.getCount() > 0) return@withContext

        val today = TimeSlot.getTodayDateString()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dayAfter = sdf.format(cal.time)

        val seedList = listOf(
            // Today's demo bookings to show live conflict checking
            BookingEntity(
                bookingCode = "JIT-2026-1042",
                resourceId = "hall-visvesvaraya",
                resourceName = "Sir M. Visvesvaraya Auditorium & Seminar Hall",
                resourceCategory = "Seminar Halls",
                bookingDate = today,
                startMinutes = 14 * 60, // 02:00 PM
                endMinutes = 16 * 60,   // 04:00 PM
                startTimeDisplay = "02:00 PM",
                endTimeDisplay = "04:00 PM",
                department = "Dept. of Computer Science & Engineering",
                eventName = "National Hackathon 2026 – Inaugural Keynote & Team Briefing",
                bookedByName = "Dr. Shailesh Rao",
                bookedByRole = "Faculty Coordinator",
                contactEmail = "shailesh.cse@jitd.in",
                contactPhone = "+91 98450 12345",
                expectedAttendees = 280,
                specialRequirements = "Dual 4K projection, podium mic and 4 cordless mics required",
                status = "CONFIRMED"
            ),
            BookingEntity(
                bookingCode = "JIT-2026-2189",
                resourceId = "lab-ai-datascience",
                resourceName = "AI & Data Science Computing Lab",
                resourceCategory = "Laboratories",
                bookingDate = today,
                startMinutes = 9 * 60, // 09:00 AM
                endMinutes = 11 * 60,  // 11:00 AM
                startTimeDisplay = "09:00 AM",
                endTimeDisplay = "11:00 AM",
                department = "Dept. of AI & Machine Learning",
                eventName = "Deep Learning & PyTorch Hands-on Lab Session",
                bookedByName = "Prof. Ananya Patil",
                bookedByRole = "Assistant Professor",
                contactEmail = "ananya.aiml@jitd.in",
                contactPhone = "+91 94812 67890",
                expectedAttendees = 55,
                specialRequirements = "Enable GPU cluster accounts for 6th sem students",
                status = "CONFIRMED"
            ),
            BookingEntity(
                bookingCode = "JIT-2026-3391",
                resourceId = "conf-boardroom",
                resourceName = "JIT Governing Council Board Room",
                resourceCategory = "Conference Rooms",
                bookingDate = today,
                startMinutes = 11 * 60 + 15, // 11:15 AM
                endMinutes = 13 * 60 + 15,   // 01:15 PM
                startTimeDisplay = "11:15 AM",
                endTimeDisplay = "01:15 PM",
                department = "Principal & Management Office",
                eventName = "Quarterly Academic Review & VTU Affiliation Committee Meeting",
                bookedByName = "Dr. Ganesh B. (Dean)",
                bookedByRole = "Dean Academic",
                contactEmail = "dean.academics@jitd.in",
                contactPhone = "+91 94480 54321",
                expectedAttendees = 20,
                specialRequirements = "Polycom VC link with Governing Council Members",
                status = "CONFIRMED"
            ),
            BookingEntity(
                bookingCode = "JIT-2026-4402",
                resourceId = "proj-epson-laser",
                resourceName = "Epson EB-L200F 4500L Laser Projector (Kit 1)",
                resourceCategory = "Projectors",
                bookingDate = tomorrow,
                startMinutes = 10 * 60, // 10:00 AM
                endMinutes = 13 * 60,   // 01:00 PM
                startTimeDisplay = "10:00 AM",
                endTimeDisplay = "01:00 PM",
                department = "Dept. of Electronics & Communication",
                eventName = "VLSI Chip Design Guest Lecture Series",
                bookedByName = "Prof. Murugesh N.",
                bookedByRole = "Faculty Coordinator",
                contactEmail = "murugesh.ece@jitd.in",
                contactPhone = "+91 97401 88990",
                expectedAttendees = 70,
                specialRequirements = "Include 15m HDMI cable and remote control",
                status = "CONFIRMED"
            ),
            BookingEntity(
                bookingCode = "JIT-2026-5519",
                resourceId = "class-room-101",
                resourceName = "Room 101 – Smart Tiered Lecture Hall",
                resourceCategory = "Classrooms",
                bookingDate = tomorrow,
                startMinutes = 14 * 60, // 02:00 PM
                endMinutes = 16 * 60,   // 04:00 PM
                startTimeDisplay = "02:00 PM",
                endTimeDisplay = "04:00 PM",
                department = "JIT Placement & Training Cell",
                eventName = "Infosys & TCS Campus Recruitment Aptitude Training",
                bookedByName = "Vinay Kumar (Training Officer)",
                bookedByRole = "Staff",
                contactEmail = "placement@jitd.in",
                contactPhone = "+91 98800 11223",
                expectedAttendees = 80,
                specialRequirements = "Sound system & smart projector setup",
                status = "CONFIRMED"
            )
        )

        bookingDao.insertAll(seedList)
    }
}
