package com.example.data.model

data class CampusResource(
    val id: String,
    val name: String,
    val category: ResourceCategory,
    val block: String,
    val floor: String,
    val capacity: Int,
    val departmentOwner: String,
    val facilities: List<String>,
    val description: String,
    val isEquipment: Boolean = false,
    val serialNumber: String? = null
) {
    val fullLocation: String
        get() = if (isEquipment) "AV Central Hub, $block" else "$block, $floor"

    val capacityText: String
        get() = if (isEquipment) "Unit / 1 Set" else "$capacity Seats"
}

object CampusResourceCatalog {
    val resources = listOf(
        // SEMINAR HALLS
        CampusResource(
            id = "hall-visvesvaraya",
            name = "Sir M. Visvesvaraya Auditorium & Seminar Hall",
            category = ResourceCategory.SEMINAR_HALL,
            block = "Main Central Academic Block",
            floor = "2nd Floor",
            capacity = 350,
            departmentOwner = "Central Administration / Principal Office",
            facilities = listOf("Dual 4K Laser Projectors", "Dolby Surround Sound", "Central Air Conditioning", "Digital Smart Podium", "Live Webcast Setup"),
            description = "Flagship auditorium for national hackathons, graduation day, international conferences, and institution guest lectures."
        ),
        CampusResource(
            id = "hall-kalam",
            name = "Dr. A.P.J. Abdul Kalam Seminar Hall",
            category = ResourceCategory.SEMINAR_HALL,
            block = "Mechanical Engineering Block",
            floor = "1st Floor",
            capacity = 180,
            departmentOwner = "Mechanical & Allied Depts",
            facilities = listOf("Interactive Projection", "PA System with Wireless Mics", "Acoustic Wall Treatment", "High-speed Wi-Fi 6"),
            description = "Equipped for departmental technical seminars, faculty development programs, and inter-college symposiums."
        ),
        CampusResource(
            id = "hall-cse-seminar",
            name = "CS Department Seminar Hall",
            category = ResourceCategory.SEMINAR_HALL,
            block = "Computer Science Block",
            floor = "3rd Floor",
            capacity = 120,
            departmentOwner = "Dept. of Computer Science & Engineering",
            facilities = listOf("Touch Smart Board", "High-Gain Audio", "Dual Displays", "Dedicated Power Backup", "Gigabit LAN Ports"),
            description = "Modern technical seminar hall tailored for code sprints, project presentations, and developer club meetups."
        ),

        // LABORATORIES
        CampusResource(
            id = "lab-ai-datascience",
            name = "AI & Data Science Computing Lab",
            category = ResourceCategory.LABORATORY,
            block = "Computer Science Block",
            floor = "2nd Floor (Room 214)",
            capacity = 60,
            departmentOwner = "Dept. of AI & Machine Learning",
            facilities = listOf("60 High-End RTX GPU Workstations", "10 Gbps Fiber Backbone", "Interactive Presentation Display", "Dual UPS 20kVA"),
            description = "Specialized lab equipped for deep learning model training, data analytics workshops, and hackathon dev sprints."
        ),
        CampusResource(
            id = "lab-iot-embedded",
            name = "IoT & Embedded Systems Research Lab",
            category = ResourceCategory.LABORATORY,
            block = "Electronics & Comm. Block",
            floor = "1st Floor (Room 108)",
            capacity = 45,
            departmentOwner = "Dept. of Electronics & Communication",
            facilities = listOf("40 Embedded Dev Kits (ESP32/STM32/RPi)", "Digital Storage Oscilloscopes", "Sensor Arrays", "Soldering & Prototyping Benches"),
            description = "Cutting-edge hardware prototyping space for smart sensor development, robotics interfacing, and electronics research."
        ),
        CampusResource(
            id = "lab-advanced-computing",
            name = "Advanced Software Engineering Lab",
            category = ResourceCategory.LABORATORY,
            block = "Information Science Block",
            floor = "2nd Floor (Room 202)",
            capacity = 70,
            departmentOwner = "Dept. of Information Science & Engineering",
            facilities = listOf("70 Core i7 Workstations", "Linux & Windows Dual Boot", "Overhead Projector", "Centralized Server Deployment Sandbox"),
            description = "Full stack software development environment for competitive coding competitions and practical laboratory exams."
        ),

        // CLASSROOMS
        CampusResource(
            id = "class-room-101",
            name = "Room 101 – Smart Tiered Lecture Hall",
            category = ResourceCategory.CLASSROOM,
            block = "Main Academic Block",
            floor = "Ground Floor",
            capacity = 85,
            departmentOwner = "First Year Basic Science Dept.",
            facilities = listOf("Interactive Smart Screen", "Tiered Gallery Seating", "Ceiling Mic Array", "Motorized Blinds"),
            description = "Large gallery classroom perfect for common orientation lectures and combined batch engineering sections."
        ),
        CampusResource(
            id = "class-room-204",
            name = "Room 204 – Interactive Digital Classroom",
            category = ResourceCategory.CLASSROOM,
            block = "Main Academic Block",
            floor = "2nd Floor",
            capacity = 65,
            departmentOwner = "Dept. of Civil Engineering",
            facilities = listOf("Full HD Projector", "Magnetic Ceramic Whiteboard", "Wall Mount Audio Speakers", "Natural Ventilation"),
            description = "Spacious classroom ideal for technical problem-solving sessions, branch tutorials, and club meetings."
        ),
        CampusResource(
            id = "class-room-305",
            name = "Room 305 – Core Engineering Classroom",
            category = ResourceCategory.CLASSROOM,
            block = "New Academic Wing",
            floor = "3rd Floor",
            capacity = 75,
            departmentOwner = "Dept. of Electrical Engineering",
            facilities = listOf("Overhead Projector", "Dual Sliding Whiteboard", "Public Address System", "Individual Desk Charging"),
            description = "Designed for regular semester lectures, remedial classes, and internal assessment tests."
        ),

        // PROJECTORS
        CampusResource(
            id = "proj-epson-laser",
            name = "Epson EB-L200F 4500L Laser Projector (Kit 1)",
            category = ResourceCategory.PROJECTOR,
            block = "Central AV Store Room",
            floor = "Ground Floor (AV Counter)",
            capacity = 1,
            departmentOwner = "Central IT & Media Support",
            facilities = listOf("4,500 ANSI Lumens", "Wireless Screen Cast (Miracast/AirPlay)", "HDMI / USB-C Cables", "Tripod Stand & Remote"),
            description = "High-brightness portable projector kit ready for event deployment in any classroom or outdoors canopy.",
            isEquipment = true,
            serialNumber = "JIT-AV-PROJ-01"
        ),
        CampusResource(
            id = "proj-benq-4k",
            name = "BenQ 5000L Ultra-Bright 4K Projector (Kit 2)",
            category = ResourceCategory.PROJECTOR,
            block = "Central AV Store Room",
            floor = "Ground Floor (AV Counter)",
            capacity = 1,
            departmentOwner = "Central IT & Media Support",
            facilities = listOf("5,000 ANSI Lumens", "Native 4K HDR", "Long Distance HDMI Extender", "Heavy Duty Wheeled Case"),
            description = "Professional grade cinema projector for large hall screening, keynote presentations, and tech festivals.",
            isEquipment = true,
            serialNumber = "JIT-AV-PROJ-02"
        ),
        CampusResource(
            id = "proj-short-throw",
            name = "Sony Ultra Short Throw Interactive Projector (Kit 3)",
            category = ResourceCategory.PROJECTOR,
            block = "Central AV Store Room",
            floor = "Ground Floor (AV Counter)",
            capacity = 1,
            departmentOwner = "Central IT & Media Support",
            facilities = listOf("Ultra Short Throw Lens", "Interactive Digital Pen Support", "Wall-Mount Bracket", "Carry Pouch"),
            description = "Compact short-throw projector ideal for workshops in compact conference spaces with zero shadow cast.",
            isEquipment = true,
            serialNumber = "JIT-AV-PROJ-03"
        ),

        // CONFERENCE ROOMS
        CampusResource(
            id = "conf-boardroom",
            name = "JIT Governing Council Board Room",
            category = ResourceCategory.CONFERENCE_ROOM,
            block = "Administrative Block",
            floor = "1st Floor (Near Principal Office)",
            capacity = 32,
            departmentOwner = "Principal & Management Office",
            facilities = listOf("Polycom HD Video Conferencing", "360° Tabletop Microphone Grid", "75-inch 4K Display", "Executive Leather Seating", "Dedicated Beverage Station"),
            description = "High-level executive boardroom for board meetings, academic council sessions, and visiting accreditation panels (NAAC/NBA)."
        ),
        CampusResource(
            id = "conf-hod-room",
            name = "HOD & Academic Council Meeting Room",
            category = ResourceCategory.CONFERENCE_ROOM,
            block = "Main Central Academic Block",
            floor = "1st Floor",
            capacity = 22,
            departmentOwner = "Dean Academic Affairs",
            facilities = listOf("65-inch Smart TV", "Conference Audio Hub", "Whiteboard Wall", "Ergonomic Chairs"),
            description = "Collaborative meeting space for department heads, syllabus revision committees, and faculty coordinators."
        ),
        CampusResource(
            id = "conf-placement-discussion",
            name = "Training & Placement Corporate Discussion Room",
            category = ResourceCategory.CONFERENCE_ROOM,
            block = "Placement & Career Development Cell",
            floor = "Ground Floor",
            capacity = 16,
            departmentOwner = "Training & Placement Cell",
            facilities = listOf("Dual Webcams for Remote Interviewing", "High-Speed Dedicated Fiber Link", "Acoustic Soundproofing", "Document Presenter"),
            description = "Reserved for campus recruitment corporate HR panels, group discussions, and placement pre-talks."
        )
    )

    fun getResourceById(id: String): CampusResource? {
        return resources.firstOrNull { it.id == id }
    }
}
