package com.example.fitness.myapplication.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Lớp bao bọc (Envelope) chuẩn của Backend Edupia cho đối tượng đơn T.
 */
@Serializable
data class ApiEnvelope<T>(
    @SerialName("errorCode") val rawErrorCode: JsonElement? = null,
    @SerialName("code") val rawCode: JsonElement? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("message") val rawMessage: JsonElement? = null,
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("access_token") val accessTokenSnake: String? = null,
    @SerialName("edupiaToken") val edupiaToken: String? = null,
) {
    val errorCode: Int
        get() = rawErrorCode?.jsonPrimitive?.intOrNull
            ?: rawErrorCode?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: 0

    val statusCode: Int
        get() = rawCode?.jsonPrimitive?.intOrNull
            ?: rawCode?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: 200

    val message: String
        get() = rawMessage?.jsonPrimitive?.contentOrNull ?: ""

    val token: String?
        get() = accessToken ?: accessTokenSnake

    val isSuccessful: Boolean
        get() = (errorCode == 0 && (statusCode == 200 || statusCode == 0))
}

/**
 * Lớp bao bọc (Envelope) chuẩn của Backend Edupia cho danh sách List<T>.
 */
@Serializable
data class ApiListEnvelope<T>(
    @SerialName("errorCode") val rawErrorCode: JsonElement? = null,
    @SerialName("code") val rawCode: JsonElement? = null,
    @SerialName("data") val data: List<T>? = null,
    @SerialName("message") val rawMessage: JsonElement? = null,
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("pagination") val pagination: PaginationDto? = null,
) {
    val errorCode: Int
        get() = rawErrorCode?.jsonPrimitive?.intOrNull
            ?: rawErrorCode?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: 0

    val statusCode: Int
        get() = rawCode?.jsonPrimitive?.intOrNull
            ?: rawCode?.jsonPrimitive?.contentOrNull?.toIntOrNull()
            ?: 200

    val message: String
        get() = rawMessage?.jsonPrimitive?.contentOrNull ?: ""

    val isSuccessful: Boolean
        get() = (errorCode == 0 && (statusCode == 200 || statusCode == 0))
}

@Serializable
data class PaginationDto(
    @SerialName("page") val page: Int? = 1,
    @SerialName("pageSize") val pageSize: Int? = 10,
    @SerialName("total") val total: Int? = 0,
    @SerialName("totalPages") val totalPages: Int? = 0,
)
