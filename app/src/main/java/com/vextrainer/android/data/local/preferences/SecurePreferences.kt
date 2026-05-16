package com.vextrainer.android.data.local.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun saveToken(token: String)        = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?             = prefs.getString(KEY_TOKEN, null)

    fun saveRefreshToken(token: String) = prefs.edit().putString(KEY_REFRESH, token).apply()
    fun getRefreshToken(): String?      = prefs.getString(KEY_REFRESH, null)

    fun saveUserId(id: Int)             = prefs.edit().putInt(KEY_USER_ID, id).apply()
    fun getUserId(): Int                = prefs.getInt(KEY_USER_ID, 0)

    fun saveUserName(name: String)      = prefs.edit().putString(KEY_USER_NAME, name).apply()
    fun getUserName(): String?          = prefs.getString(KEY_USER_NAME, null)

    fun saveEmail(email: String)        = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getEmail(): String?             = prefs.getString(KEY_EMAIL, null)

    fun isLoggedIn(): Boolean           = getToken() != null

    fun clearAll()                      = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN     = "access_token"
        private const val KEY_REFRESH   = "refresh_token"
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_EMAIL     = "email"
    }
}
