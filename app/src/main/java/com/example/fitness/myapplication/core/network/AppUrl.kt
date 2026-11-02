package com.example.fitness.myapplication.core.network

/**
 * Quản lý URL các dịch vụ Backend của Edupia AI.
 */
object AppUrl {

    // Mass Core API
    private const val DEV_BASE_URL = "https://mass-api.edupia.com.vn/"
    private const val PROD_BASE_URL = "https://mass-api.edupia.vn/"

    // Mass LMS API
    private const val DEV_LMS_URL = "https://mass-lms-api.edupia.com.vn/api/"
    private const val PROD_LMS_URL = "https://mass-lms-api.edupia.vn/api/"

    // Free Trial V2 API
    private const val DEV_FREETRIAL_V2_URL = "https://freetrial-v2-api.edupia.com.vn/api/"
    private const val PROD_FREETRIAL_V2_URL = "https://freetrial-v2-api.edupia.vn/api/"

    // Tutor API
    private const val DEV_TUTOR_URL = "https://tutor-api.edupia.com.vn/"
    private const val PROD_TUTOR_URL = "https://tutor-api.edupia.vn/"

    // Contest API
    private const val DEV_CONTEST_URL = "https://mass-contest-api.edupia.com.vn/"
    private const val PROD_CONTEST_URL = "https://mass-contest-api.edupia.vn/"

    // Service 3 API
    private const val DEV_SERVICE3_URL = "https://service3-dev.edupia.com.vn/"
    private const val PROD_SERVICE3_URL = "https://service3.edupia.vn/"

    // WebSocket / Socket.IO
    private const val DEV_WS_URL = "wss://mass-ws-io.edupia.com.vn/edupiaclass"
    private const val PROD_WS_URL = "wss://mass-ws-io.edupia.vn/edupiaclass"

    fun baseUrl(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_BASE_URL
        AppEnvType.PROD -> PROD_BASE_URL
        AppEnvType.STAGING -> DEV_BASE_URL
    }

    fun lmsUrl(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_LMS_URL
        AppEnvType.PROD -> PROD_LMS_URL
        AppEnvType.STAGING -> DEV_LMS_URL
    }

    fun freeTrialV2Url(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_FREETRIAL_V2_URL
        AppEnvType.PROD -> PROD_FREETRIAL_V2_URL
        AppEnvType.STAGING -> DEV_FREETRIAL_V2_URL
    }

    fun tutorUrl(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_TUTOR_URL
        AppEnvType.PROD -> PROD_TUTOR_URL
        AppEnvType.STAGING -> DEV_TUTOR_URL
    }

    fun contestUrl(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_CONTEST_URL
        AppEnvType.PROD -> PROD_CONTEST_URL
        AppEnvType.STAGING -> DEV_CONTEST_URL
    }

    fun service3Url(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_SERVICE3_URL
        AppEnvType.PROD -> PROD_SERVICE3_URL
        AppEnvType.STAGING -> DEV_SERVICE3_URL
    }

    fun socketIoUrl(env: AppEnvType = AppEnv.instance.currentEnv): String = when (env) {
        AppEnvType.DEV -> DEV_WS_URL
        AppEnvType.PROD -> PROD_WS_URL
        AppEnvType.STAGING -> DEV_WS_URL
    }
}
