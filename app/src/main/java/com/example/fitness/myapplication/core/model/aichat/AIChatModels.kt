package com.example.fitness.myapplication.core.model.aichat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AIScript(
    @SerialName("id") val id: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("vocabulary") val vocabulary: String = "",
    @SerialName("sentence") val sentence: String = "",
    @SerialName("grade") val grade: Int = -1,
    @SerialName("level") val level: String = "",
    @SerialName("numberOfQuestion") val numberOfQuestion: Int = 0,
    @SerialName("status") val status: String = "",
    @SerialName("assistanceAvatarUrl") val assistanceAvatarUrl: String = "",
    @SerialName("userAvatarUrl") val userAvatarUrl: String = "",
    @SerialName("assistanceVoidId") val assistanceVoidId: String = "",
    @SerialName("assistanceDescription") val assistanceDescription: String = "",
    @SerialName("userDescription") val userDescription: String = "",
    @SerialName("useAiAvatar") val useAiAvatar: Int = 0,
    @SerialName("staticVideoUrl") val staticVideoUrl: String = "",
)

@Serializable
data class RecognizedWord(
    @SerialName("word") val word: String? = null,
    @SerialName("endTime") val endTime: Double? = null,
)

@Serializable
data class AIChatModel(
    @SerialName("questionId") val questionId: Int = -1,
    @SerialName("message") val message: String = "",
    @SerialName("hint") val hint: String = "",
    @SerialName("threadId") val threadId: String = "",
    @SerialName("audioData") val audioData: String = "",
    @SerialName("videoUrl") val videoUrl: String = "",
    @SerialName("isMine") val isMine: Boolean = false,
    @SerialName("messageVi") val messageVi: String = "",
    @SerialName("hintVi") val hintVi: String = "",
    @SerialName("isFailureMessage") val isFailureMessage: Boolean = false,
    @SerialName("isFinal") val isFinal: Boolean = false,
    @SerialName("assistanceAvatarUrl") val assistanceAvatarUrl: String = "",
) {
    val isEmpty: Boolean get() = message.isEmpty()
}

@Serializable
data class ReviewAnswer(
    @SerialName("totalScore") val totalScore: Int? = 0,
    @SerialName("rangeScore") val rangeScore: Int? = 0,
    @SerialName("accuracyScore") val accuracyScore: Int? = 0,
    @SerialName("fluencyScore") val fluencyScore: Int? = 0,
    @SerialName("overallFeedback") val overallFeedback: String? = null,
    @SerialName("rangeFeedback") val rangeFeedback: String? = null,
    @SerialName("accuracyFeedback") val accuracyFeedback: String? = null,
    @SerialName("fluencyFeedback") val fluencyFeedback: String? = null,
)

@Serializable
data class EnableAssemblyAi(
    @SerialName("assemblyAiEnabled") val assemblyAiEnabled: Boolean? = false,
    @SerialName("durationCount") val durationCount: Int? = 0,
)
