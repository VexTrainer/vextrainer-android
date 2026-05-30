package com.vextrainer.android.presentation.navigation

import androidx.compose.runtime.compositionLocalOf

/**
 * Navigation callbacks provided by NavGraph and consumed by VexTopAppBar.
 * Using CompositionLocal means no screen files need to pass callbacks through —
 * VexTopAppBar reads them directly from the composition.
 */
data class TopNavCallbacks(
    val isProvided: Boolean  = false,   // false = don't show nav icons (Login/Register)
    val onLessons:  () -> Unit = {},
    val onQuizzes:  () -> Unit = {},
    val onProfile:  () -> Unit = {}
)

val LocalTopNavCallbacks = compositionLocalOf { TopNavCallbacks() }
