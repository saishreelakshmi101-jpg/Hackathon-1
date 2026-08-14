package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookingCheckResult
import com.example.data.model.CampusResource
import com.example.data.model.ResourceCategory
import com.example.data.model.TimeSlot
import com.example.ui.BookingViewModel
import com.example.ui.components.ConflictWarningBanner
import com.example.ui.components.TimeSlotPicker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var resourceDropdownExpanded by remember { mutableStateOf(false) }
    var departmentDropdownExpanded by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val departments = listOf(
        "Dept. of Computer Science & Engineering",
        "Dept. of Information Science & Engineering",
        "Dept. of AI & Machine Learning",
        "Dept. of Electronics & Communication",
        "Dept. of Mechanical Engineering",
        "Dept. of Civil Engineering",
        "JIT Training & Placement Cell",
        "First Year Basic Science Dept.",
        "Student Activity Center & Clubs",
        "Principal & Administrative Office"
    )

    val roles = listOf(
        "Faculty Coordinator",
        "Professor & HOD",
        "Student Coordinator / CR",
        "Technical Staff / Lab Incharge",
        "Placement Officer"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("booking_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Select Resource
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_1_select_resource"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    StepHeader(stepNumber = "1", title = "Select Campus Resource")
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = resourceDropdownExpanded,
                        onExpandedChange = { resourceDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedResource?.let { "${it.name} (${it.category.displayName})" }
                                ?: "Choose a Hall, Lab, Classroom, or Projector...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resourceDropdownExpanded) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("resource_selector_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = resourceDropdownExpanded,
                            onDismissRequest = { resourceDropdownExpanded = false }
                        ) {
                            viewModel.allResources.forEach { resource ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = resource.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${resource.category.displayName} • ${resource.fullLocation} • ${resource.capacityText}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectResource(resource)
                                        resourceDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("dropdown_item_${resource.id}")
                                )
                            }
                        }
                    }

                    // Resource quick info summary if selected
                    uiState.selectedResource?.let { res ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = res.fullLocation,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = res.capacityText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Select Date
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_2_select_date"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    StepHeader(stepNumber = "2", title = "Select Booking Date")
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date quick presets row
                    val todayCal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val today = sdf.format(todayCal.time)
                    todayCal.add(Calendar.DAY_OF_YEAR, 1)
                    val tomorrow = sdf.format(todayCal.time)
                    todayCal.add(Calendar.DAY_OF_YEAR, 1)
                    val dayAfter = sdf.format(todayCal.time)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedDate == today,
                            onClick = { viewModel.selectDate(today) },
                            label = { Text("Today (${TimeSlot.formatDateForDisplay(today).substring(0, 7)})") },
                            modifier = Modifier.testTag("date_chip_today")
                        )
                        FilterChip(
                            selected = uiState.selectedDate == tomorrow,
                            onClick = { viewModel.selectDate(tomorrow) },
                            label = { Text("Tomorrow") },
                            modifier = Modifier.testTag("date_chip_tomorrow")
                        )
                        FilterChip(
                            selected = uiState.selectedDate == dayAfter,
                            onClick = { viewModel.selectDate(dayAfter) },
                            label = { Text("Day After") },
                            modifier = Modifier.testTag("date_chip_day_after")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Date Button picker
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            val parts = uiState.selectedDate.split("-")
                            val y = parts.getOrNull(0)?.toIntOrNull() ?: c.get(Calendar.YEAR)
                            val m = (parts.getOrNull(1)?.toIntOrNull() ?: (c.get(Calendar.MONTH) + 1)) - 1
                            val d = parts.getOrNull(2)?.toIntOrNull() ?: c.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                viewModel.selectDate(formattedDate)
                            }, y, m, d).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("date_picker_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Date: ${TimeSlot.formatDateForDisplay(uiState.selectedDate)} (Change...)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Step 3: Select Time
        item {
            TimeSlotPicker(
                selectedSlot = uiState.selectedTimeSlot,
                isCustomMode = uiState.isCustomTimeMode,
                customStartHour = uiState.customStartHour,
                customStartMinute = uiState.customStartMinute,
                customEndHour = uiState.customEndHour,
                customEndMinute = uiState.customEndMinute,
                onSelectPreset = { viewModel.selectPresetTimeSlot(it) },
                onToggleCustomMode = { viewModel.setCustomTimeMode(it) },
                onUpdateCustomStartTime = { h, m -> viewModel.updateCustomStartTime(h, m) },
                onUpdateCustomEndTime = { h, m -> viewModel.updateCustomEndTime(h, m) }
            )
        }

        // Step 4: Check Availability Button & Conflict Status
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_4_check_availability")
            ) {
                Button(
                    onClick = { viewModel.checkAvailability() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("check_availability_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = uiState.selectedResource != null && !uiState.isCheckingAvailability
                ) {
                    if (uiState.isCheckingAvailability) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Checking Real-time Conflicts...")
                    } else {
                        Icon(imageVector = Icons.Default.Spellcheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Availability in Real-Time",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Conflict or Availability Status
                ConflictWarningBanner(
                    result = uiState.checkResult,
                    onSelectAlternativeSlot = { slot ->
                        viewModel.selectPresetTimeSlot(slot)
                        viewModel.checkAvailability()
                    }
                )
            }
        }

        // Step 5: Confirm Booking Details Form (Active when resource selected and not conflicted)
        item {
            AnimatedVisibility(
                visible = uiState.selectedResource != null && uiState.checkResult !is BookingCheckResult.Conflict,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_5_confirm_booking_form"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        StepHeader(stepNumber = "5", title = "Event & Applicant Details")
                        Spacer(modifier = Modifier.height(14.dp))

                        // Event Title
                        OutlinedTextField(
                            value = uiState.eventName,
                            onValueChange = { viewModel.onEventNameChanged(it) },
                            label = { Text("Event / Purpose Title *") },
                            placeholder = { Text("e.g., Hackathon Final Round / Project Demo") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_event_name"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Department Dropdown
                        ExposedDropdownMenuBox(
                            expanded = departmentDropdownExpanded,
                            onExpandedChange = { departmentDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.department,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Department / Cell *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departmentDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("input_department"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = departmentDropdownExpanded,
                                onDismissRequest = { departmentDropdownExpanded = false }
                            ) {
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept) },
                                        onClick = {
                                            viewModel.onDepartmentChanged(dept)
                                            departmentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Applicant Name
                        OutlinedTextField(
                            value = uiState.bookedByName,
                            onValueChange = { viewModel.onBookedByNameChanged(it) },
                            label = { Text("Applicant / Organizer Name *") },
                            placeholder = { Text("e.g. Prof. Ramesh / Sai Lakshmi") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_booked_by_name"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Role & Expected Attendees Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Role
                            ExposedDropdownMenuBox(
                                expanded = roleDropdownExpanded,
                                onExpandedChange = { roleDropdownExpanded = it },
                                modifier = Modifier.weight(1.2f)
                            ) {
                                OutlinedTextField(
                                    value = uiState.bookedByRole,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Role *") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("input_role"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false }
                                ) {
                                    roles.forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role) },
                                            onClick = {
                                                viewModel.onBookedByRoleChanged(role)
                                                roleDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Expected Attendees
                            OutlinedTextField(
                                value = uiState.expectedAttendees,
                                onValueChange = { viewModel.onExpectedAttendeesChanged(it) },
                                label = { Text("Attendees") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("input_attendees"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Contact Email & Phone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.contactEmail,
                                onValueChange = { viewModel.onContactEmailChanged(it) },
                                label = { Text("Email") },
                                placeholder = { Text("name@jitd.in") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_contact_email"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = uiState.contactPhone,
                                onValueChange = { viewModel.onContactPhoneChanged(it) },
                                label = { Text("Phone") },
                                placeholder = { Text("+91 98450...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_contact_phone"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Special Requirements
                        OutlinedTextField(
                            value = uiState.specialRequirements,
                            onValueChange = { viewModel.onSpecialRequirementsChanged(it) },
                            label = { Text("Special Facilities / AV Requirements") },
                            placeholder = { Text("e.g. Need 2 wireless mics, HDMI cable, podium setup") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_special_requirements"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Confirm Booking Button
                        Button(
                            onClick = { viewModel.submitBooking() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_booking_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            ),
                            enabled = !uiState.isSubmittingBooking
                        ) {
                            if (uiState.isSubmittingBooking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Reserving Campus Resource...")
                            } else {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Confirm & Reserve Resource",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StepHeader(stepNumber: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
