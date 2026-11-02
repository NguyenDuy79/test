package com.example.fitness.myapplication.core.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LoadDataStatus {
    INITIAL,
    LOADING,
    SUCCESS,
    FAILURE
}

@Serializable
data class Pagination(
    @SerialName("page") val page: Int = 1,
    @SerialName("pageSize") val pageSize: Int = 10,
    @SerialName("total") val total: Int = 0,
    @SerialName("totalPages") val totalPages: Int = 0,
) {
    val hasNextPage: Boolean get() = page < totalPages
}
