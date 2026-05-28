package com.vextrainer.android.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.data.local.preferences.SecurePreferences
import com.vextrainer.android.domain.model.dashboard.Dashboard
import com.vextrainer.android.domain.usecase.dashboard.GetDashboardUseCase
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean     = false,  // true on first load — shows full LoadingOverlay
    val isRefreshing: Boolean  = false,  // true on pull-to-refresh — shows indicator over content
    val dashboard: Dashboard? = null,
    val userName: String = "",
    val error: UiText? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardUseCase: GetDashboardUseCase,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(userName = securePreferences.getUserName() ?: "")
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // init intentionally omitted — DashboardScreen triggers load via
    // repeatOnLifecycle(RESUMED) so the data refreshes every time the
    // screen becomes visible again (e.g. returning from a topic).

    /**
     * @param isRefresh true when triggered by pull-to-refresh; keeps existing
     *                  content visible and shows the swipe indicator instead of
     *                  the full-screen LoadingOverlay.
     */
    fun loadDashboard(isRefresh: Boolean = false) {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing) return
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true, error = null)
                else           it.copy(isLoading    = true, error = null)
            }
            getDashboardUseCase()
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            isRefreshing = false,
                            dashboard    = dashboard
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error     = e.toUiText(R.string.error_load_dashboard)
                        )
                    }
                }
        }
    }
}
