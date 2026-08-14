package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BookingEntity
import com.example.data.model.CampusResource
import com.example.data.model.ResourceCategory
import com.example.data.model.TimeSlot
import com.example.ui.BookingViewModel
import com.example.ui.theme.JitError
import com.example.ui.theme.JitErrorContainer
import com.example.ui.theme.JitSuccess
import com.example.ui.theme.JitSuccessContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineScheduleScreen(
    viewModel: BookingViewModel,
    onBookResource: (CampusResource) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedCategoryFilter by remember { mutableStateOf<ResourceCategory?>(null) }

    val todayCal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val today = sdf.format(todayCal.time)
    todayCal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrow = sdf.format(todayCal.time)
    todayCal.add(Calendar.DAY_OF_YEAR, 1)
    val dayAfter = sdf.format(todayCal.time)

    // Filter confirmed bookings for the active timeline date
    val dayBookings = allBookings.filter {
        it.isConfirmed && it.bookingDate == uiState.timelineDate
    }

    val resourcesToShow = viewModel.allResources.filter { res ->
        selectedCategoryFilter == null || res.category == selectedCategoryFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("timeline_schedule_screen")
    ) {
        // Date Selector Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CAMPUS SCHEDULE TIMELINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = TimeSlot.formatDateForDisplay(uiState.timelineDate),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Calendar Date Picker Button
                    OutlinedButton(
                        onClick = {
                            val c = Calendar.getInstance()
                            val parts = uiState.timelineDate.split("-")
                            val y = parts.getOrNull(0)?.toIntOrNull() ?: c.get(Calendar.YEAR)
                            val m = (parts.getOrNull(1)?.toIntOrNull() ?: (c.get(Calendar.MONTH) + 1)) - 1
                            val d = parts.getOrNull(2)?.toIntOrNull() ?: c.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                viewModel.setTimelineDate(formattedDate)
                            }, y, m, d).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("timeline_pick_date_button")
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Date", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date shortcut chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.timelineDate == today,
                        onClick = { viewModel.setTimelineDate(today) },
                        label = { Text("Today") },
                        modifier = Modifier.testTag("timeline_chip_today")
                    )
                    FilterChip(
                        selected = uiState.timelineDate == tomorrow,
                        onClick = { viewModel.setTimelineDate(tomorrow) },
                        label = { Text("Tomorrow") },
                        modifier = Modifier.testTag("timeline_chip_tomorrow")
                    )
                    FilterChip(
                        selected = uiState.timelineDate == dayAfter,
                        onClick = { viewModel.setTimelineDate(dayAfter) },
                        label = { Text("Day After") },
                        modifier = Modifier.testTag("timeline_chip_day_after")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("timeline_cat_all")
                    )
                    ResourceCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat.displayName, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("timeline_cat_${cat.id}")
                        )
                    }
                }
            }
        }

        // Timeline Resources List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(
                items = resourcesToShow,
                key = { it.id }
            ) { resource ->
                val resourceBookings = dayBookings.filter { it.resourceId == resource.id }
                TimelineResourceCard(
                    resource = resource,
                    bookings = resourceBookings,
                    onBookNow = { onBookResource(resource) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun TimelineResourceCard(
    resource: CampusResource,
    bookings: List<BookingEntity>,
    onBookNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_card_${resource.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${resource.category.displayName} • ${resource.fullLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Booking count indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (bookings.isEmpty()) JitSuccessContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (bookings.isEmpty()) "Completely Free" else "${bookings.size} Reserved",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bookings.isEmpty()) JitSuccess else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Hour Bar (09:00 to 18:00)
            CampusHourVisualBar(bookings = bookings)

            Spacer(modifier = Modifier.height(10.dp))

            // Bookings Details or Free Note
            if (bookings.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    bookings.forEach { b ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = b.eventName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${b.department} • ${b.bookedByName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = JitErrorContainer
                                ) {
                                    Text(
                                        text = b.displayTimeRange,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = JitError
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = JitSuccessContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = JitSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No conflicts scheduled. All session slots are available!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = JitSuccess,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Book Button
            OutlinedButton(
                onClick = onBookNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "+ Reserve Time on this Resource",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun CampusHourVisualBar(bookings: List<BookingEntity>) {
    // 9 AM to 6 PM (9 slots: 9-10, 10-11, 11-12, 12-1, 1-2, 2-3, 3-4, 4-5, 5-6)
    val hours = listOf(9, 10, 11, 12, 13, 14, 15, 16, 17)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(JitSuccess.copy(alpha = 0.2f))
        ) {
            hours.forEach { hour ->
                val slotStart = hour * 60
                val slotEnd = (hour + 1) * 60
                val isOccupied = bookings.any { b ->
                    TimeSlot.checkOverlap(b.startMinutes, b.endMinutes, slotStart, slotEnd)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(if (isOccupied) JitError else JitSuccess.copy(alpha = 0.35f))
                        .padding(horizontal = 0.5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "9 AM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "12 PM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "3 PM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "6 PM", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
