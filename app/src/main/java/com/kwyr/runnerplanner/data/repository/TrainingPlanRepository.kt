package com.kwyr.runnerplanner.data.repository

import com.kwyr.runnerplanner.data.local.PreferencesDataStore
import com.kwyr.runnerplanner.data.model.DayPlan
import com.kwyr.runnerplanner.data.model.TrainingPlan
import com.kwyr.runnerplanner.data.model.WeekPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingPlanRepository @Inject constructor(
    private val dataStore: PreferencesDataStore
) {
    val trainingPlanFlow: Flow<TrainingPlan?> = dataStore.trainingPlanFlow
    val weekPlansFlow: Flow<Map<String, WeekPlan>> = dataStore.weekPlansFlow

    suspend fun getTrainingPlan(): Result<TrainingPlan?> {
        return try {
            val plan = dataStore.trainingPlanFlow.first()
            Result.success(plan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTrainingPlan(plan: TrainingPlan): Result<Unit> {
        return try {
            dataStore.saveTrainingPlan(plan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeekPlan(weekStart: String): Result<WeekPlan> {
        return try {
            val weekPlans = dataStore.weekPlansFlow.first()
            val weekPlan = weekPlans[weekStart] ?: WeekPlan(weekStart = weekStart)
            Result.success(weekPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveWeekPlan(weekPlan: WeekPlan): Result<Unit> {
        return try {
            val currentWeekPlans = dataStore.weekPlansFlow.first().toMutableMap()
            currentWeekPlans[weekPlan.weekStart] = weekPlan
            dataStore.saveWeekPlans(currentWeekPlans)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllWeekPlans(): Result<Unit> {
        return try {
            dataStore.saveWeekPlans(emptyMap())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDayPlan(weekStart: String, dayOfWeek: Int, dayPlan: DayPlan): Result<Unit> {
        return try {
            val currentWeekPlans = dataStore.weekPlansFlow.first().toMutableMap()
            val weekPlan = currentWeekPlans[weekStart] ?: WeekPlan(weekStart = weekStart)
            val updatedDays = weekPlan.days.toMutableList()
            updatedDays[dayOfWeek] = dayPlan
            currentWeekPlans[weekStart] = weekPlan.copy(days = updatedDays)
            dataStore.saveWeekPlans(currentWeekPlans)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleDayCompletion(weekStart: String, dayOfWeek: Int): Result<Unit> {
        return try {
            val currentWeekPlans = dataStore.weekPlansFlow.first().toMutableMap()
            val weekPlan = currentWeekPlans[weekStart] ?: WeekPlan(weekStart = weekStart)
            val updatedDays = weekPlan.days.toMutableList()
            val currentDay = updatedDays[dayOfWeek]
            updatedDays[dayOfWeek] = currentDay.copy(completed = !currentDay.completed)
            currentWeekPlans[weekStart] = weekPlan.copy(days = updatedDays)
            dataStore.saveWeekPlans(currentWeekPlans)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }

    fun getWeekStartForOffset(offset: Int): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }
}
