package com.vextrainer.android.data.local.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "vextrainer_secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Token ─────────────────────────────────────────────────────────────────

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?      = prefs.getString(KEY_TOKEN, null)

    // ── Refresh token ─────────────────────────────────────────────────────────

    fun saveRefreshToken(token: String) = prefs.edit().putString(KEY_REFRESH, token).apply()
    fun getRefreshToken(): String?      = prefs.getString(KEY_REFRESH, null)

    // ── Expiry ────────────────────────────────────────────────────────────────

    /**
     * Stores the ISO-8601 expiry timestamp returned by the server on login/refresh.
     * e.g. "2026-05-16T02:54:18.6818625Z"
     */
    fun saveExpiryDate(expiry: String) = prefs.edit().putString(KEY_EXPIRY, expiry).apply()
    fun getExpiryDate(): String?       = prefs.getString(KEY_EXPIRY, null)

    // ── User info ─────────────────────────────────────────────────────────────

    fun saveUserId(id: Int)        = prefs.edit().putInt(KEY_USER_ID, id).apply()
    fun getUserId(): Int           = prefs.getInt(KEY_USER_ID, 0)

    fun saveUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()
    fun getUserName(): String?     = prefs.getString(KEY_USER_NAME, null)

    fun saveEmail(email: String)   = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getEmail(): String?        = prefs.getString(KEY_EMAIL, null)

    // ── Session state ─────────────────────────────────────────────────────────

    /**
     * Returns true only when:
     *  1. A token is stored, AND
     *  2. The stored expiry has not passed (with a 60-second safety buffer).
     *
     * If no expiry was ever stored (legacy install), we trust the token and let
     * the API return 401 if it is actually expired — the interceptor handles that.
     */
    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        val expiry = getExpiryDate() ?: return true   // no expiry on disk → assume valid
        return try {
            Instant.now().isBefore(Instant.parse(expiry).minusSeconds(60))
        } catch (_: Exception) {
            // Unparseable expiry string — don't lock the user out; let the API decide.
            true
        }
    }

    // ── Clear ─────────────────────────────────────────────────────────────────

    fun clearAll() = prefs.edit().clear().apply()

    // ── Keys ──────────────────────────────────────────────────────────────────

    companion object {
        private const val KEY_TOKEN     = "access_token"
        private const val KEY_REFRESH   = "refresh_token"
        private const val KEY_EXPIRY    = "expiry_date"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL     = "email"
    }
}
