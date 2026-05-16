package com.vextrainer.android.presentation

import androidx.lifecycle.ViewModel
import com.vextrainer.android.data.local.preferences.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    securePreferences: SecurePreferences
) : ViewModel() {
    val isLoggedIn: Boolean = securePreferences.isLoggedIn()
}
