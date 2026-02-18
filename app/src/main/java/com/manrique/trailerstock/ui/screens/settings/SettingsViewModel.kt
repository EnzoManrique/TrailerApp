package com.manrique.trailerstock.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.repository.UserPreferencesRepository
import com.manrique.trailerstock.data.repository.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences?> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun toggleVisibility(key: String, visible: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateVisibility(key, visible)
        }
    }

    fun updateStagnantThreshold(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateStagnantThreshold(days)
        }
    }
}
