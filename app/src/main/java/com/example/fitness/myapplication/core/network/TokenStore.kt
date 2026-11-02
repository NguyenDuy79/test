package com.example.fitness.myapplication.core.network

import com.example.fitness.myapplication.core.local.AppPreferences
import com.example.fitness.myapplication.core.local.PrefsKey

/**
 * Quản lý Token và định danh phiên người dùng.
 */
interface TokenStore {
    var accessToken: String
    var tempAccessToken: String
    var accessUser: String
    var tempAccessUser: String
    var edupiaToken: String
    var activeProfileId: Int
    var activeAccountId: Int

    fun clearAuth()
}

class TokenStoreImpl(private val prefs: AppPreferences) : TokenStore {

    override var accessToken: String
        get() = prefs.getString(PrefsKey.ACCESS_TOKEN)
        set(value) = prefs.setString(PrefsKey.ACCESS_TOKEN, value)

    override var tempAccessToken: String
        get() = prefs.getString(PrefsKey.TEMP_ACCESS_TOKEN)
        set(value) = prefs.setString(PrefsKey.TEMP_ACCESS_TOKEN, value)

    override var accessUser: String
        get() = prefs.getString(PrefsKey.ACCESS_USER)
        set(value) = prefs.setString(PrefsKey.ACCESS_USER, value)

    override var tempAccessUser: String
        get() = prefs.getString(PrefsKey.TEMP_ACCESS_USER)
        set(value) = prefs.setString(PrefsKey.TEMP_ACCESS_USER, value)

    override var edupiaToken: String
        get() = prefs.getString(PrefsKey.EDUPIA_TOKEN)
        set(value) = prefs.setString(PrefsKey.EDUPIA_TOKEN, value)

    override var activeProfileId: Int
        get() = prefs.getInt(PrefsKey.ACTIVE_PROFILE_ID, defaultValue = 0)
        set(value) = prefs.setInt(PrefsKey.ACTIVE_PROFILE_ID, value)

    override var activeAccountId: Int
        get() = prefs.getInt(PrefsKey.ACTIVE_ACCOUNT_ID, defaultValue = 0)
        set(value) = prefs.setInt(PrefsKey.ACTIVE_ACCOUNT_ID, value)

    override fun clearAuth() {
        prefs.remove(PrefsKey.ACCESS_TOKEN)
        prefs.remove(PrefsKey.TEMP_ACCESS_TOKEN)
        prefs.remove(PrefsKey.ACCESS_USER)
        prefs.remove(PrefsKey.TEMP_ACCESS_USER)
        prefs.remove(PrefsKey.EDUPIA_TOKEN)
        prefs.remove(PrefsKey.ACTIVE_PROFILE_ID)
        prefs.remove(PrefsKey.ACTIVE_ACCOUNT_ID)
    }
}
