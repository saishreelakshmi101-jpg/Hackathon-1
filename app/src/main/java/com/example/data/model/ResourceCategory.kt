package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

enum class ResourceCategory(
    val id: String,
    val displayName: String,
    val description: String,
    val iconName: String
) {
    SEMINAR_HALL(
        id = "seminar_hall",
        displayName = "Seminar Halls",
        description = "Large auditoriums & multimedia halls for guest lectures, symposiums & hackathons",
        iconName = "Apartment"
    ),
    LABORATORY(
        id = "laboratory",
        displayName = "Laboratories",
        description = "High-performance computing, AI, IoT, and engineering research labs",
        iconName = "Science"
    ),
    CLASSROOM(
        id = "classroom",
        displayName = "Classrooms",
        description = "Smart lecture rooms, tutorial halls, and academic discussion spaces",
        iconName = "MeetingRoom"
    ),
    PROJECTOR(
        id = "projector",
        displayName = "Projectors",
        description = "High-lumen portable & laser projection equipment for loan and setup",
        iconName = "Tv"
    ),
    CONFERENCE_ROOM(
        id = "conference_room",
        displayName = "Conference Rooms",
        description = "Executive boardrooms, HOD discussion halls, and placement interview rooms",
        iconName = "Cast"
    );

    companion object {
        fun fromId(id: String): ResourceCategory {
            return entries.firstOrNull { it.id == id } ?: SEMINAR_HALL
        }
    }
}
