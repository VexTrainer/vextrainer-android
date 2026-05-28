package com.vextrainer.android.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = createPrefs()

    private fun createPrefs(): SharedPreferences {
        val fileName = "vextrainer_secure_prefs"
        return try {
            buildEncryptedPrefs(fileName)
        } catch (e: Exception) {
            val isCryptoFailure = e is javax.crypto.AEADBadTagException
                || e.cause is javax.crypto.AEADBadTagException
                || e is java.security.KeyStoreException
                || e.cause is java.security.KeyStoreException
            if (isCryptoFailure) {
                Log.w("SecurePreferences", "Keystore key invalidated, wiping prefs", e)
                context.deleteSharedPreferences(fileName)
                try {
                    buildEncryptedPrefs(fileName)
                } catch (e2: Exception) {
                    Log.e("SecurePreferences", "Second failure, using plain prefs", e2)
                    context.getSharedPreferences("${fileName}_fallback", Context.MODE_PRIVATE)
                }
            } else {
                Log.e("SecurePreferences", "Non-crypto failure, keeping prefs file", e)
                context.getSharedPreferences("${fileName}_fallback", Context.MODE_PRIVATE)
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun buildEncryptedPrefs(fileName: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String)   = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?        = prefs.getString(KEY_TOKEN, null)

    fun saveRefreshToken(token: String) = prefs.edit().putString(KEY_REFRESH, token).apply()
    fun getRefreshToken(): String?      = prefs.getString(KEY_REFRESH, null)

    fun saveExpiryDate(expiry: String) = prefs.edit().putString(KEY_EXPIRY, expiry).apply()
    fun getExpiryDate(): String?       = prefs.getString(KEY_EXPIRY, null)

    fun saveUserId(id: Int)        = prefs.edit().putInt(KEY_USER_ID, id).apply()
    fun getUserId(): Int           = prefs.getInt(KEY_USER_ID, 0)

    fun saveUserName(name: String) = prefs.edit().putString(KEY_USER_NAME, name).apply()
    fun getUserName(): String?     = prefs.getString(KEY_USER_NAME, null)

    fun saveEmail(email: String)   = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getEmail(): String?        = prefs.getString(KEY_EMAIL, null)

    /**
     * Returns true when a token is stored on disk, regardless of expiry.
     * Expiry is NOT checked here — the AuthInterceptor handles 401 refresh silently.
     * Checking expiry at launch caused the Login screen to appear on every cold start
     * after 30 minutes even though the session was still restorable.
     */
    fun isLoggedIn(): Boolean = getToken() != null

    /**
     * Decodes the role claim from the stored JWT without network.
     * Returns "Student", "User", "Admin" etc. or null if no token.
     * role_id 2 = "Student" per the API contract.
     */
    fun getUserRole(): String? {
        val token = getToken() ?: return null
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val padded  = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = String(android.util.Base64.decode(padded, android.util.Base64.URL_SAFE))
            org.json.JSONObject(decoded).optString("role", null)
        } catch (e: Exception) { null }
    }

    fun isStudent(): Boolean =
        getUserRole()?.equals("Student", ignoreCase = true) == true

    fun clearAll() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN     = "access_token"
        private const val KEY_REFRESH   = "refresh_token"
        private const val KEY_EXPIRY    = "expiry_date"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL     = "email"
    }
}
