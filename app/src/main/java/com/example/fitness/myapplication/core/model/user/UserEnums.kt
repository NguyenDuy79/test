package com.example.fitness.myapplication.core.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Gender {
    @SerialName("0")
    MALE,

    @SerialName("1")
    FEMALE,

    @SerialName("2")
    OTHER
}

@Serializable
data class Province(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("code") val code: String = "",
)
