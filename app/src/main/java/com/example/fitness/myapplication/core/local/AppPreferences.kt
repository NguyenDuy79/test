package com.example.fitness.myapplication.core.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý SharedPreferences kiểu an toàn cho toàn bộ ứng dụng Edupia AI.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "edupia_ai_prefs"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getString(key: PrefsKey, defaultValue: String = ""): String {
        return prefs.getString(key.key, defaultValue) ?: defaultValue
    }

    fun setString(key: PrefsKey, value: String) {
        prefs.edit().putString(key.key, value).apply()
    }

    fun getInt(key: PrefsKey, defaultValue: Int = 0): Int {
        return prefs.getInt(key.key, defaultValue)
    }

    fun setInt(key: PrefsKey, value: Int) {
        prefs.edit().putInt(key.key, value).apply()
    }

    fun getLong(key: PrefsKey, defaultValue: Long = 0L): Long {
        return prefs.getLong(key.key, defaultValue)
    }

    fun setLong(key: PrefsKey, value: Long) {
        prefs.edit().putLong(key.key, value).apply()
    }

    fun getBoolean(key: PrefsKey, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key.key, defaultValue)
    }

    fun setBoolean(key: PrefsKey, value: Boolean) {
        prefs.edit().putBoolean(key.key, value).apply()
    }

    fun remove(key: PrefsKey) {
        prefs.edit().remove(key.key).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
