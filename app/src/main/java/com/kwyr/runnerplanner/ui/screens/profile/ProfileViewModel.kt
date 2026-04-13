package com.kwyr.runnerplanner.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.ActivityMode
import com.kwyr.runnerplanner.data.model.UnitSystem
import com.kwyr.runnerplanner.data.model.UserProfile
import com.kwyr.runnerplanner.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = userRepository.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val theme: StateFlow<String> = userRepository.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "light"
        )

    val selectedMode: StateFlow<ActivityMode> = userRepository.selectedModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityMode.RUNNING
        )

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val currentProfile = userProfile.value
            userRepository.updateUserProfile(
                currentProfile.copy(name = name)
            )
        }
    }

    fun updateUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            val currentProfile = userProfile.value
            userRepository.updateUserProfile(
                currentProfile.copy(unitSystem = unitSystem)
            )
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            userRepository.toggleTheme()
        }
    }
}
