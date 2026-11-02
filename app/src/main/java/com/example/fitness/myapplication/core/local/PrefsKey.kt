package com.example.fitness.myapplication.core.local

/**
 * Danh sách key cấu hình & lưu trữ cục bộ, ánh xạ từ Flutter PrefsKey trong app_core.
 */
enum class PrefsKey(val key: String) {
    ACCESS_TOKEN("access_token"),
    TEMP_ACCESS_TOKEN("temp_access_token"),
    ACCESS_USER("access_user"),
    TEMP_ACCESS_USER("temp_access_user"),
    EDUPIA_TOKEN("edupia_token"),
    ENABLE_ONBOARDING("first_open_app"),
    ACTIVE_PROFILE_ID("active_profile_id"),
    ACTIVE_ACCOUNT_ID("active_account_id"),
    LAST_IN_APP_NOTIFY_TIME("last_in_app_notify_time_key"),
    TOTAL_ISPEAK_SCORE("total_ispeak_score"),
    CONSENT_STATUS("consent_status"),
    FIRST_LOGIN("first_login"),
    LOGIN_TODAY("login_today"),
    END_LESSON("end_lesson"),
    FREE_TRIAL_PHONE("free_trial_phone"),
    FREE_TRIAL_ACCOUNT_ID("free_trial_account_id"),
    FREE_TRIAL_HOME_PROFILE_ID("free_trial_home_profile_id"),
    API_INSPECTOR_UNLOCKED("api_inspector_unlocked"),
    STT_ENGINE_OVERRIDE("stt_engine_override"),
    STT_VAD_MODE("stt_vad_mode"),
    APP_ENV("app_environment")
}
