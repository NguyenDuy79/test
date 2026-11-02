package com.example.fitness.myapplication.core.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("avatar") val avatar: String = "",
    @SerialName("gender") val gender: Gender = Gender.MALE,
    @SerialName("grade_id") val gradeId: Int? = null,
    @SerialName("grade_level") val gradeLevel: Int = -1,
    @SerialName("status") val status: Int = 0,
    @SerialName("account_id") val accountId: Int = 0,
    @SerialName("school_id") val schoolId: Int = 0,
    @SerialName("is_profile_default") val isProfileDefault: Int = 0,
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("is_paid") val isPaid: Int = 0,
    @SerialName("avatar_border") val avatarBorder: String = "",
    @SerialName("type_regular") val typeRegular: String = "",
    @SerialName("grade_name") val gradeName: String = "",
) {
    val isPrimary: Boolean get() = isProfileDefault == 1
    val isUserPaid: Boolean get() = isPaid == 1
}

@Serializable
data class User(
    @SerialName("id") val id: Int = 0,
    @SerialName("phone") val phone: String = "",
    @SerialName("full_name") val fullName: String = "",
    @SerialName("gender") val gender: Gender = Gender.MALE,
    @SerialName("email") val email: String = "",
    @SerialName("address") val address: String = "",
    @SerialName("birthday") val birthday: String? = null,
    @SerialName("province_code") val provinceCode: String = "",
    @SerialName("profiles") val profiles: List<Profile> = emptyList(),
    @SerialName("is_referral") val isReferral: Int = 0,
    @SerialName("source") val source: Int = 0,
)
