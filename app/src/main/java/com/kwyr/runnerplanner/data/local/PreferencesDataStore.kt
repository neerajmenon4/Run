package com.kwyr.runnerplanner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.Route
import com.kwyr.runnerplanner.data.model.TrainingPlan
import com.kwyr.runnerplanner.data.model.UserProfile
import com.kwyr.runnerplanner.data.model.WeekPlan
import com.kwyr.runnerplanner.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    private object Keys {
        val ROUTES = stringPreferencesKey(Constants.PreferenceKeys.ROUTES)
        val ACTIVITIES = stringPreferencesKey(Constants.PreferenceKeys.ACTIVITIES)
        val USER_PROFILE = stringPreferencesKey(Constants.PreferenceKeys.USER_PROFILE)
        val THEME = stringPreferencesKey(Constants.PreferenceKeys.THEME)
        val TRAINING_PLAN = stringPreferencesKey(Constants.PreferenceKeys.TRAINING_PLAN)
        val WEEK_PLANS = stringPreferencesKey(Constants.PreferenceKeys.WEEK_PLANS)
    }

    val routesFlow: Flow<List<Route>> = dataStore.data.map { preferences ->
        val json = preferences[Keys.ROUTES] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<Route>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveRoutes(routes: List<Route>) {
        dataStore.edit { preferences ->
            preferences[Keys.ROUTES] = gson.toJson(routes)
        }
    }

    val activitiesFlow: Flow<List<Activity>> = dataStore.data.map { preferences ->
        val json = preferences[Keys.ACTIVITIES] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<Activity>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveActivities(activities: List<Activity>) {
        dataStore.edit { preferences ->
            preferences[Keys.ACTIVITIES] = gson.toJson(activities)
        }
    }

    val userProfileFlow: Flow<UserProfile> = dataStore.data.map { preferences ->
        val json = preferences[Keys.USER_PROFILE]
        if (json != null) {
            try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                UserProfile()
            }
        } else {
            UserProfile()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[Keys.USER_PROFILE] = gson.toJson(profile)
        }
    }

    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[Keys.THEME] ?: "light"
    }

    suspend fun saveTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme
        }
    }

    val trainingPlanFlow: Flow<TrainingPlan?> = dataStore.data.map { preferences ->
        val json = preferences[Keys.TRAINING_PLAN]
        if (json != null) {
            try {
                gson.fromJson(json, TrainingPlan::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    suspend fun saveTrainingPlan(plan: TrainingPlan) {
        dataStore.edit { preferences ->
            preferences[Keys.TRAINING_PLAN] = gson.toJson(plan)
        }
    }

    val weekPlansFlow: Flow<Map<String, WeekPlan>> = dataStore.data.map { preferences ->
        val json = preferences[Keys.WEEK_PLANS] ?: return@map emptyMap()
        try {
            val type = object : TypeToken<Map<String, WeekPlan>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun saveWeekPlans(weekPlans: Map<String, WeekPlan>) {
        dataStore.edit { preferences ->
            preferences[Keys.WEEK_PLANS] = gson.toJson(weekPlans)
        }
    }
}
