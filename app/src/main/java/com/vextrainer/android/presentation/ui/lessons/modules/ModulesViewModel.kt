package com.vextrainer.android.presentation.ui.lessons.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vextrainer.android.R
import com.vextrainer.android.domain.model.lesson.Module
import com.vextrainer.android.domain.usecase.lesson.GetModulesUseCase
import com.vextrainer.android.presentation.util.UiText
import com.vextrainer.android.presentation.util.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModulesUiState(
    val isLoading: Boolean = true,
    val modules: List<Module> = emptyList(),
    val error: UiText? = null
)

@HiltViewModel
class ModulesViewModel @Inject constructor(
    private val getModulesUseCase: GetModulesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModulesUiState())
    val uiState: StateFlow<ModulesUiState> = _uiState.asStateFlow()

    init { loadModules() }

    fun loadModules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getModulesUseCase()
                .onSuccess { modules ->
                    _uiState.update { it.copy(isLoading = false, modules = modules) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.toUiText(R.string.error_load_modules))
                    }
                }
        }
    }
}
