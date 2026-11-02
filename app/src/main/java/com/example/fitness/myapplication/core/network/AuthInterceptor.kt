package com.example.fitness.myapplication.core.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor tự động bổ sung Access Token, Access User và Authorization
 * chính xác theo quy chuẩn của Edupia Backend (hỗ trợ cả Tutor & Edupia LMS).
 */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()

        val useTempAccess = originalRequest.header("use_temp_access")?.toBoolean() ?: false

        val token = if (useTempAccess) tokenStore.tempAccessToken else tokenStore.accessToken
        val user = if (useTempAccess) tokenStore.tempAccessUser else tokenStore.accessUser

        val isTutor = originalUrl.contains("tutor-api")

        val tokenHeaderKey = if (isTutor) "access-token" else "access_token"
        val userHeaderKey = if (isTutor) "access-user" else "access_user"

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (token.isNotEmpty()) {
            requestBuilder.header(tokenHeaderKey, token)
            // Cần thiết cho Edupia LMS
            requestBuilder.header("Authorization", token)
        }

        if (user.isNotEmpty()) {
            requestBuilder.header(userHeaderKey, user)
        }

        // Edupia Token phụ trợ nếu có
        if (tokenStore.edupiaToken.isNotEmpty()) {
            requestBuilder.header("edupia_token", tokenStore.edupiaToken)
        }

        return chain.proceed(requestBuilder.build())
    }
}
