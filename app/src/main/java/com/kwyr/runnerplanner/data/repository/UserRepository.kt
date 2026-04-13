package com.kwyr.runnerplanner.data.repository

import com.kwyr.runnerplanner.data.local.PreferencesDataStore
import com.kwyr.runnerplanner.data.model.ActivityMode
import com.kwyr.runnerplanner.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    val userProfileFlow: Flow<UserProfile> = dataStore.userProfileFlow
    val themeFlow: Flow<String> = dataStore.themeFlow
    val selectedModeFlow: Flow<ActivityMode> = dataStore.selectedModeFlow.map { modeStr ->
        if (modeStr == "biking") ActivityMode.BIKING else ActivityMode.RUNNING
    }

    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val profile = dataStore.userProfileFlow.first()
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            dataStore.saveUserProfile(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTheme(): Result<String> {
        return try {
            val theme = dataStore.themeFlow.first()
            Result.success(theme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTheme(theme: String): Result<Unit> {
        return try {
            dataStore.saveTheme(theme)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveActivityMode(mode: ActivityMode): Result<Unit> {
        return try {
            dataStore.saveSelectedMode(mode.type)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleTheme(): Result<String> {
        return try {
            val currentTheme = dataStore.themeFlow.first()
            val newTheme = when (currentTheme) {
                "light" -> "dark"
                "dark" -> "system"
                else -> "light"
            }
            dataStore.saveTheme(newTheme)
            Result.success(newTheme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
