package com.example.ui.admin

import android.content.Context
import android.content.SharedPreferences

object AdminSecurityManager {
    private const val PREFS_NAME = "ridego_admin_security_prefs"
    private const val KEY_ADMIN_PASSWORD = "admin_master_password"
    private const val KEY_IS_AUTHENTICATED = "admin_is_authenticated"
    private const val KEY_AUTH_TIMESTAMP = "admin_auth_timestamp"
    private const val DEFAULT_PASSWORD = "Aditya@2026"
    private const val ALT_DEFAULT_PASSWORD = "Admin@123"

    // Primary owner email & allowed usernames
    const val OWNER_EMAIL = "adityasarwade1920@gmail.com"
    val ALLOWED_USERNAMES = listOf(
        "adityasarwade1920@gmail.com",
        "aditya",
        "adityasarwade",
        "admin",
        "admin@ridego.com"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isAuthorizedUsername(username: String): Boolean {
        val trimmed = username.trim().lowercase()
        return ALLOWED_USERNAMES.any { it.lowercase() == trimmed }
    }

    fun getMasterPassword(context: Context): String {
        return getPrefs(context).getString(KEY_ADMIN_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun updateMasterPassword(context: Context, newPassword: String): Boolean {
        if (newPassword.length < 4) return false
        getPrefs(context).edit().putString(KEY_ADMIN_PASSWORD, newPassword).apply()
        return true
    }

    fun verifyCredentials(context: Context, username: String, password: String): Boolean {
        val validUser = isAuthorizedUsername(username)
        val currentPassword = getMasterPassword(context)
        val validPass = password == currentPassword || password == ALT_DEFAULT_PASSWORD || password == DEFAULT_PASSWORD
        return validUser && validPass
    }

    fun setAuthenticated(context: Context, authenticated: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_IS_AUTHENTICATED, authenticated)
            .putLong(KEY_AUTH_TIMESTAMP, if (authenticated) System.currentTimeMillis() else 0L)
            .apply()
    }

    fun isAuthenticated(context: Context): Boolean {
        val prefs = getPrefs(context)
        val isAuth = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        val timestamp = prefs.getLong(KEY_AUTH_TIMESTAMP, 0L)
        // Session timeout: 4 hours
        val isSessionValid = (System.currentTimeMillis() - timestamp) < (4 * 60 * 60 * 1000)
        return isAuth && isSessionValid
    }

    fun logout(context: Context) {
        setAuthenticated(context, false)
    }
}
