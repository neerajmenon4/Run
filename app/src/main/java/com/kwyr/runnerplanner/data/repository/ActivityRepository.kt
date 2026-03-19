package com.kwyr.runnerplanner.data.repository

import com.kwyr.runnerplanner.data.local.PreferencesDataStore
import com.kwyr.runnerplanner.data.local.dao.ActivityDao
import com.kwyr.runnerplanner.data.mapper.ActivityMapper
import com.kwyr.runnerplanner.data.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val dataStore: PreferencesDataStore
) {
    // Flow of all activities
    val activitiesFlow: Flow<List<Activity>> = activityDao.getAllActivities()
        .map { entities -> ActivityMapper.fromEntityList(entities) }

    // Get all activities
    suspend fun getAllActivities(): Result<List<Activity>> {
        return try {
            val activities = activitiesFlow.first()
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get activities by year
    fun getActivitiesByYear(year: Int): Flow<List<Activity>> {
        return activityDao.getActivitiesByYear(year)
            .map { entities -> ActivityMapper.fromEntityList(entities) }
    }

    // Get activities by month
    fun getActivitiesByMonth(year: Int, month: Int): Flow<List<Activity>> {
        return activityDao.getActivitiesByMonth(year, month)
            .map { entities -> ActivityMapper.fromEntityList(entities) }
    }

    // Get activities by week
    fun getActivitiesByWeek(year: Int, month: Int, week: Int): Flow<List<Activity>> {
        return activityDao.getActivitiesByWeek(year, month, week)
            .map { entities -> ActivityMapper.fromEntityList(entities) }
    }

    // Get distinct years for picker
    fun getDistinctYears(): Flow<List<Int>> {
        return activityDao.getDistinctYears()
    }

    // Get distinct months for a year
    fun getDistinctMonthsForYear(year: Int): Flow<List<Int>> {
        return activityDao.getDistinctMonthsForYear(year)
    }

    // Get week count for a month
    suspend fun getWeekCountForMonth(year: Int, month: Int): Int {
        return activityDao.getWeekCountForMonth(year, month) ?: 0
    }

    // Save activity
    suspend fun saveActivity(activity: Activity): Result<Unit> {
        return try {
            val entity = ActivityMapper.toEntity(activity)
            activityDao.insertActivity(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete activity
    suspend fun deleteActivity(activityId: String): Result<Unit> {
        return try {
            val entity = activityDao.getActivityById(activityId)
            if (entity != null) {
                activityDao.deleteActivity(entity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get activity by ID
    suspend fun getActivityById(activityId: String): Result<Activity?> {
        return try {
            val entity = activityDao.getActivityById(activityId)
            val activity = entity?.let { ActivityMapper.fromEntity(it) }
            Result.success(activity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get total distance for a week
    suspend fun getTotalDistanceForWeek(year: Int, month: Int, week: Int): Double {
        return activityDao.getTotalDistanceForWeek(year, month, week) ?: 0.0
    }

    // Get total duration for a week
    suspend fun getTotalDurationForWeek(year: Int, month: Int, week: Int): Double {
        return activityDao.getTotalDurationForWeek(year, month, week) ?: 0.0
    }

    // Migration: Move activities from DataStore to Room (one-time operation)
    suspend fun migrateFromDataStore(): Result<Unit> {
        return try {
            val oldActivities = dataStore.activitiesFlow.first()
            if (oldActivities.isNotEmpty()) {
                val entities = ActivityMapper.toEntityList(oldActivities)
                activityDao.insertActivities(entities)
                // Clear DataStore after successful migration
                dataStore.saveActivities(emptyList())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
