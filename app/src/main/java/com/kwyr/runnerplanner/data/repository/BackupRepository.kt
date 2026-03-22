package com.kwyr.runnerplanner.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.kwyr.runnerplanner.data.local.PreferencesDataStore
import com.kwyr.runnerplanner.data.model.BackupData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: PreferencesDataStore,
    private val activityRepository: ActivityRepository,
    private val gson: Gson
) {
    
    suspend fun createBackup(): Result<BackupData> {
        return try {
            val routes = dataStore.routesFlow.first()
            val activities = activityRepository.getAllActivities().getOrNull() ?: emptyList()
            val userProfile = dataStore.userProfileFlow.first()
            val theme = dataStore.themeFlow.first()
            val trainingPlan = dataStore.trainingPlanFlow.first()
            val weekPlans = dataStore.weekPlansFlow.first()
            
            val backup = BackupData(
                version = 1,
                exportDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()),
                appVersion = "1.0",
                routes = routes,
                activities = activities,
                userProfile = userProfile,
                theme = theme,
                trainingPlan = trainingPlan,
                weekPlans = weekPlans
            )
            
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportBackupToUri(uri: Uri, backupData: BackupData): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = gson.toJson(backupData)
                outputStream.write(json.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importBackupFromUri(uri: Uri): Result<BackupData> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return Result.failure(Exception("Failed to read backup file"))
            
            val backup = gson.fromJson(json, BackupData::class.java)
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(backupData: BackupData, mergeData: Boolean = false): Result<Unit> {
        return try {
            if (mergeData) {
                val existingRoutes = dataStore.routesFlow.first()
                val existingActivities = activityRepository.getAllActivities().getOrNull() ?: emptyList()
                
                val mergedRoutes = (existingRoutes + backupData.routes)
                    .distinctBy { it.id }
                
                val mergedActivities = (existingActivities + backupData.activities)
                    .distinctBy { it.id }
                
                dataStore.saveRoutes(mergedRoutes)
                mergedActivities.forEach { activity ->
                    activityRepository.saveActivity(activity)
                }
            } else {
                dataStore.saveRoutes(backupData.routes)
                backupData.activities.forEach { activity ->
                    activityRepository.saveActivity(activity)
                }
            }
            
            dataStore.saveUserProfile(backupData.userProfile)
            dataStore.saveTheme(backupData.theme)
            backupData.trainingPlan?.let { dataStore.saveTrainingPlan(it) }
            dataStore.saveWeekPlans(backupData.weekPlans)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
