package com.buph.srms.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Wraps SharedPreferences for everything the app needs to remember between
 * launches: which server to talk to, who's logged in, and their token.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("srms_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role" // "student" or "admin"
        private const val KEY_NAME = "display_name"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"

        // Default points at XAMPP running on the host machine, reachable
        // from the Android emulator via the special alias 10.0.2.2.
        // For a real device on the same Wi-Fi, change this in
        // Server Settings to something like http://192.168.1.20/srms/api/
        const val DEFAULT_BASE_URL = "http://10.0.2.2/srms/api/"
    }

    var baseUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        set(value) = prefs.edit().putString(KEY_ROLE, value).apply()

    var displayName: String?
        get() = prefs.getString(KEY_NAME, null)
        set(value) = prefs.edit().putString(KEY_NAME, value).apply()

    var profileComplete: Boolean
        get() = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, value).apply()

    val isLoggedIn: Boolean get() = !token.isNullOrEmpty()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_ROLE)
            .remove(KEY_NAME)
            .remove(KEY_PROFILE_COMPLETE)
            .apply()
    }
}
