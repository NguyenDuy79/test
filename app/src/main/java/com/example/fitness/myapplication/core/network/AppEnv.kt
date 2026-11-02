package com.example.fitness.myapplication.core.network

/**
 * Định nghĩa môi trường mạng (Dev, Prod, Staging) đồng bộ với AppEnv của app_core.
 */
enum class AppEnvType {
    DEV,
    PROD,
    STAGING
}

class AppEnv private constructor() {

    var currentEnv: AppEnvType = AppEnvType.DEV
        private set

    fun setEnvironment(env: AppEnvType) {
        currentEnv = env
    }

    companion object {
        val instance: AppEnv by lazy { AppEnv() }
    }
}
