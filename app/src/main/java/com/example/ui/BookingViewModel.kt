package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BookingCheckResult
import com.example.data.model.BookingEntity
import com.example.data.model.CampusResource
import com.example.data.model.ResourceCategory
import com.example.data.model.TimeSlot
import com.example.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class BookingUiState(
    val selectedCategory: ResourceCategory? = null,
    val searchQuery: String = "",
    val filterOnlyAvailableNow: Boolean = false,
    
    // Booking Form State
    val selectedResource: CampusResource? = null,
    val selectedDate: String = TimeSlot.getTodayDateString(),
    val selectedTimeSlot: TimeSlot = TimeSlot.PRESET_SLOTS[2], // 02:00 PM - 04:00 PM
    val isCustomTimeMode: Boolean = false,
    val customStartHour: Int = 14,
    val customStartMinute: Int = 0,
    val customEndHour: Int = 16,
    val customEndMinute: Int = 0,
    
    // Availability Engine
    val checkResult: BookingCheckResult = BookingCheckResult.Idle,
    val isCheckingAvailability: Boolean = false,
    
    // Form Inputs
    val department: String = "Dept. of Computer Science & Engineering",
    val eventName: String = "",
    val bookedByName: String = "",
    val bookedByRole: String = "Faculty",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val expectedAttendees: String = "40",
    val specialRequirements: String = "",
    
    // Feedback & Receipt
    val isSubmittingBooking: Boolean = false,
    val recentlyConfirmedBooking: BookingEntity? = null,
    val showBookingSlipDialog: Boolean = false,
    val snackbarMessage: String? = null,
    
    // Timeline View
    val timelineDate: String = TimeSlot.getTodayDateString()
)

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BookingRepository(db.bookingDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    val allResources: List<CampusResource> = repository.getAllResources()

    val allBookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredResources: StateFlow<List<CampusResource>> = combine(
        _uiState,
        allBookings
    ) { state, bookings ->
        val nowCal = Calendar.getInstance()
        val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val todayStr = TimeSlot.getTodayDateString()

        allResources.filter { res ->
            // Category filter
            val matchesCategory = state.selectedCategory == null || res.category == state.selectedCategory
            
            // Search query filter
            val matchesSearch = state.searchQuery.isBlank() ||
                    res.name.contains(state.searchQuery, ignoreCase = true) ||
                    res.block.contains(state.searchQuery, ignoreCase = true) ||
                    res.facilities.any { it.contains(state.searchQuery, ignoreCase = true) } ||
                    res.departmentOwner.contains(state.searchQuery, ignoreCase = true)

            // Available right now filter
            val matchesAvailableNow = if (state.filterOnlyAvailableNow) {
                val isOccupiedNow = bookings.any { b ->
                    b.isConfirmed &&
                    b.resourceId == res.id &&
                    b.bookingDate == todayStr &&
                    currentMinutes >= b.startMinutes &&
                    currentMinutes < b.endMinutes
                }
                !isOccupiedNow
            } else {
                true
            }

            matchesCategory && matchesSearch && matchesAvailableNow
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allResources)

    fun onCategorySelected(category: ResourceCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFilterAvailableNow() {
        _uiState.update { it.copy(filterOnlyAvailableNow = !it.filterOnlyAvailableNow) }
    }

    fun selectResource(resource: CampusResource) {
        _uiState.update { 
            it.copy(
                selectedResource = resource,
                checkResult = BookingCheckResult.Idle
            ) 
        }
    }

    fun selectDate(dateString: String) {
        _uiState.update { 
            it.copy(
                selectedDate = dateString,
                checkResult = BookingCheckResult.Idle
            ) 
        }
    }

    fun setTimelineDate(dateString: String) {
        _uiState.update { it.copy(timelineDate = dateString) }
    }

    fun selectPresetTimeSlot(slot: TimeSlot) {
        _uiState.update { 
            it.copy(
                selectedTimeSlot = slot,
                isCustomTimeMode = false,
                checkResult = BookingCheckResult.Idle
            ) 
        }
    }

    fun setCustomTimeMode(enabled: Boolean) {
        _uiState.update { state ->
            if (enabled) {
                val startM = state.customStartHour * 60 + state.customStartMinute
                val endM = state.customEndHour * 60 + state.customEndMinute
                val customSlot = TimeSlot.createFromMinutes(startM, endM)
                state.copy(
                    isCustomTimeMode = true,
                    selectedTimeSlot = customSlot,
                    checkResult = BookingCheckResult.Idle
                )
            } else {
                state.copy(
                    isCustomTimeMode = false,
                    checkResult = BookingCheckResult.Idle
                )
            }
        }
    }

    fun updateCustomStartTime(hour: Int, minute: Int) {
        _uiState.update { state ->
            val startM = hour * 60 + minute
            var endM = state.customEndHour * 60 + state.customEndMinute
            if (endM <= startM) {
                endM = (startM + 60).coerceAtMost(23 * 60 + 59)
            }
            val endH = endM / 60
            val endMin = endM % 60
            val newSlot = TimeSlot.createFromMinutes(startM, endM)
            state.copy(
                customStartHour = hour,
                customStartMinute = minute,
                customEndHour = endH,
                customEndMinute = endMin,
                selectedTimeSlot = newSlot,
                checkResult = BookingCheckResult.Idle
            )
        }
    }

    fun updateCustomEndTime(hour: Int, minute: Int) {
        _uiState.update { state ->
            val startM = state.customStartHour * 60 + state.customStartMinute
            val endM = hour * 60 + minute
            val validEndM = if (endM <= startM) (startM + 30).coerceAtMost(23 * 60 + 59) else endM
            val newSlot = TimeSlot.createFromMinutes(startM, validEndM)
            state.copy(
                customEndHour = validEndM / 60,
                customEndMinute = validEndM % 60,
                selectedTimeSlot = newSlot,
                checkResult = BookingCheckResult.Idle
            )
        }
    }

    fun checkAvailability() {
        val state = _uiState.value
        val resource = state.selectedResource ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAvailability = true) }
            val result = repository.checkAvailability(
                resourceId = resource.id,
                date = state.selectedDate,
                slot = state.selectedTimeSlot
            )
            _uiState.update { 
                it.copy(
                    checkResult = result,
                    isCheckingAvailability = false
                ) 
            }
        }
    }

    fun onDepartmentChanged(dept: String) = _uiState.update { it.copy(department = dept) }
    fun onEventNameChanged(name: String) = _uiState.update { it.copy(eventName = name) }
    fun onBookedByNameChanged(name: String) = _uiState.update { it.copy(bookedByName = name) }
    fun onBookedByRoleChanged(role: String) = _uiState.update { it.copy(bookedByRole = role) }
    fun onContactEmailChanged(email: String) = _uiState.update { it.copy(contactEmail = email) }
    fun onContactPhoneChanged(phone: String) = _uiState.update { it.copy(contactPhone = phone) }
    fun onExpectedAttendeesChanged(count: String) = _uiState.update { it.copy(expectedAttendees = count) }
    fun onSpecialRequirementsChanged(req: String) = _uiState.update { it.copy(specialRequirements = req) }

    fun submitBooking() {
        val state = _uiState.value
        val resource = state.selectedResource
        if (resource == null) {
            _uiState.update { it.copy(snackbarMessage = "Please select a campus resource first.") }
            return
        }

        if (state.eventName.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter an event or purpose title.") }
            return
        }

        if (state.bookedByName.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Please enter the organizer / applicant name.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingBooking = true) }

            val attendeesInt = state.expectedAttendees.toIntOrNull() ?: 30
            val result = repository.confirmBooking(
                resource = resource,
                date = state.selectedDate,
                slot = state.selectedTimeSlot,
                department = state.department,
                eventName = state.eventName,
                bookedByName = state.bookedByName,
                bookedByRole = state.bookedByRole,
                contactEmail = state.contactEmail.ifBlank { "organizer@jitd.in" },
                contactPhone = state.contactPhone.ifBlank { "+91 98000 00000" },
                expectedAttendees = attendeesInt,
                specialRequirements = state.specialRequirements
            )

            result.fold(
                onSuccess = { savedBooking ->
                    _uiState.update {
                        it.copy(
                            isSubmittingBooking = false,
                            recentlyConfirmedBooking = savedBooking,
                            showBookingSlipDialog = true,
                            checkResult = BookingCheckResult.Available(
                                resource = resource,
                                date = state.selectedDate,
                                slot = state.selectedTimeSlot,
                                message = "Available\n${resource.name} – ${state.selectedTimeSlot.displayRange}\nBooking Status: CONFIRMED"
                            ),
                            snackbarMessage = "Booking confirmed! Code: ${savedBooking.bookingCode}"
                        )
                    }
                },
                onFailure = { error ->
                    val conflictCheck = repository.checkAvailability(resource.id, state.selectedDate, state.selectedTimeSlot)
                    _uiState.update {
                        it.copy(
                            isSubmittingBooking = false,
                            checkResult = conflictCheck,
                            snackbarMessage = error.message ?: "Booking rejected – resource unavailable for selected time."
                        )
                    }
                }
            )
        }
    }

    fun dismissBookingSlipDialog() {
        _uiState.update { it.copy(showBookingSlipDialog = false) }
    }

    fun showBookingSlip(booking: BookingEntity) {
        _uiState.update { 
            it.copy(
                recentlyConfirmedBooking = booking,
                showBookingSlipDialog = true
            ) 
        }
    }

    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            repository.cancelBooking(bookingId)
            _uiState.update { it.copy(snackbarMessage = "Booking cancelled successfully.") }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun getResourceStatus(resourceId: String): Pair<Boolean, String> {
        val nowCal = Calendar.getInstance()
        val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val todayStr = TimeSlot.getTodayDateString()
        
        val activeBookings = allBookings.value.filter { 
            it.isConfirmed && it.resourceId == resourceId && it.bookingDate == todayStr 
        }
        
        val currentBooking = activeBookings.firstOrNull { 
            currentMinutes >= it.startMinutes && currentMinutes < it.endMinutes 
        }

        return if (currentBooking != null) {
            Pair(false, "Occupied until ${currentBooking.endTimeDisplay}")
        } else {
            val nextBooking = activeBookings.filter { it.startMinutes > currentMinutes }
                .minByOrNull { it.startMinutes }
            if (nextBooking != null) {
                val minsUntil = nextBooking.startMinutes - currentMinutes
                Pair(true, "Free for next ${minsUntil / 60}h ${minsUntil % 60}m")
            } else {
                Pair(true, "Available all day")
            }
        }
    }
}
