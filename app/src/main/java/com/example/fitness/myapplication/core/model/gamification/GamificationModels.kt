package com.example.fitness.myapplication.core.model.gamification

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Banner(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("action_type") val actionType: String = "",
    @SerialName("link") val link: String = "",
    @SerialName("position") val position: Int = 0,
)

@Serializable
data class Gamification(
    @SerialName("star") val star: Int = 0,
    @SerialName("diamond") val diamond: Int = 0,
    @SerialName("cup") val cup: Int = 0,
    @SerialName("streak") val streak: Int = 0,
    @SerialName("rank") val rank: Int = 0,
)

@Serializable
data class ContestMission(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("reward_star") val rewardStar: Int = 0,
    @SerialName("status") val status: Int = 0,
)
