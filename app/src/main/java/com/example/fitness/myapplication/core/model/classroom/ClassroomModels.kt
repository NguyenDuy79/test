package com.example.fitness.myapplication.core.model.classroom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
enum class SessionStatus {
    @SerialName("OPEN")
    OPEN,

    @SerialName("IN_PROGRESS")
    IN_PROGRESS,

    @SerialName("COMPLETED")
    COMPLETED,

    @SerialName("CANCELLED")
    CANCELLED,

    @SerialName("UNKNOWN")
    UNKNOWN
}

@Serializable
enum class SessionType {
    @SerialName("LIVE")
    LIVE,

    @SerialName("AI")
    AI,

    @SerialName("TUTOR")
    TUTOR,

    @SerialName("SELF_LEARNING")
    SELF_LEARNING,

    @SerialName("UNKNOWN")
    UNKNOWN
}

@Serializable
data class Classroom(
    @SerialName("id") val id: Int = -1,
    @SerialName("name") val name: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("schedule_id") val scheduleId: Int = 0,
    @SerialName("grade") val grade: Int = 0,
    @SerialName("status") val status: Int = 0,
    @SerialName("quota_student") val quotaStudent: Int = 0,
)

@Serializable
data class Teacher(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("avatar") val avatar: String = "",
    @SerialName("phone") val phone: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("role") val role: String = "",
    @SerialName("short_name") val shortName: String = "",
    @SerialName("grade_teaching") val gradeTeaching: String = "",
    @SerialName("subject_teaching") val subjectTeaching: String = "",
    @SerialName("is_active") val isActive: Int = 0,
) {
    val isMainTeacher: Boolean get() = type == "MAIN_TEACHER"
    val isSubTeacher: Boolean get() = type == "SUB_TEACHER"
}

@Serializable
data class Course(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("grade_id") val gradeId: Int = 0,
    @SerialName("description") val description: String = "",
    @SerialName("thumbnail") val thumbnail: String = "",
)

@Serializable
data class Part(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("index") val index: Int = 0,
    @SerialName("total_questions") val totalQuestions: Int = 0,
)

@Serializable
data class Lesson(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("thumbnail") val thumbnail: String = "",
    @SerialName("parts") val parts: List<Part> = emptyList(),
)

@Serializable
data class Session(
    @SerialName("id") val id: Int = 0,
    @SerialName("stage_id") val stageId: Int = 0,
    @SerialName("stage_title") val stageTitle: String = "",
    @SerialName("status") val status: SessionStatus = SessionStatus.UNKNOWN,
    @SerialName("type") val type: SessionType = SessionType.UNKNOWN,
    @SerialName("k12_schedule_id") val k12ScheduleId: Int = 0,
    @SerialName("session_parent_id") val sessionParentId: Int = 0,
    @SerialName("class_room_id") val classRoomId: Int = 0,
    @SerialName("session_no") val rawSessionNo: JsonElement? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("title") val title: String = "",
    @SerialName("time_format") val timeFormat: String = "",
    @SerialName("embed_link") val embedLink: String = "",
    @SerialName("classin_url") val classinUrl: String = "",
    @SerialName("thumbnail") val thumbnail: String = "",
    @SerialName("teachers") val teachers: List<Teacher> = emptyList(),
    @SerialName("lesson") val lesson: Lesson? = null,
    @SerialName("is_self_learning") val isSelfLearning: Boolean = false,
) {
    val sessionNo: Int
        get() = rawSessionNo?.jsonPrimitive?.intOrNull
            ?: rawSessionNo?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: 0

    val primaryTeacher: Teacher?
        get() = teachers.firstOrNull { it.isMainTeacher } ?: teachers.firstOrNull()
}
