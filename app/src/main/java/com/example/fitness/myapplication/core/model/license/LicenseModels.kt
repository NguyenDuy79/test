package com.example.fitness.myapplication.core.model.license

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class License(
    @SerialName("id") val id: Int = 0,
    @SerialName("code") val code: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("expire_date") val expireDate: String? = null,
    @SerialName("status") val status: Int = 0,
    @SerialName("package_type") val packageType: String = "",
    @SerialName("is_trial") val isTrial: Boolean = false,
    @SerialName("product_id") val productId: Int? = null,
    @SerialName("grade_id") val gradeId: Int? = null,
) {
    val isActive: Boolean get() = status == 1
}

@Serializable
data class LicenseTutor(
    @SerialName("id") val id: Int = 0,
    @SerialName("vip_status") val vipStatus: Int = 0,
    @SerialName("expired_at") val expiredAt: String? = null,
    @SerialName("package_name") val packageName: String = "",
) {
    val isVip: Boolean get() = vipStatus == 1
}
