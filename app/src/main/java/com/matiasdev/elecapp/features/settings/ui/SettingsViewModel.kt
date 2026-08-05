package com.matiasdev.elecapp.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matiasdev.elecapp.features.settings.data.ReminderSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: ReminderSettingsRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            repository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        firstReminderMinutes = settings.defaultFirstReminderMinutes?.toString().orEmpty(),
                        secondReminderEnabled = settings.defaultSecondReminderMinutes != null,
                        secondReminderMinutes = settings.defaultSecondReminderMinutes?.toString().orEmpty(),
                    )
                }
            }
        }
    }

    fun onFirstReminderChange(value: String) {
        _uiState.update { it.copy(firstReminderMinutes = value.filter(Char::isDigit), savedMessage = null) }
    }

    fun onSecondReminderEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(secondReminderEnabled = enabled, savedMessage = null) }
    }

    fun onSecondReminderChange(value: String) {
        _uiState.update { it.copy(secondReminderMinutes = value.filter(Char::isDigit), savedMessage = null) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch(ioDispatcher) {
            repository.save(
                firstMinutes = state.firstReminderMinutes.toIntOrNull(),
                secondMinutes = state.secondReminderMinutes.toIntOrNull().takeIf { state.secondReminderEnabled },
            )
            _uiState.update { it.copy(savedMessage = "Configuración guardada") }
        }
    }
}

class SettingsViewModelFactory(
    private val repository: ReminderSettingsRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}
