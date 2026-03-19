package com.kwyr.runnerplanner.ui.screens.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.*
import com.kwyr.runnerplanner.data.repository.TrainingPlanRepository
import com.kwyr.runnerplanner.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WeeklyPlanUiState(
    val trainingPlan: TrainingPlan? = null,
    val currentWeekStart: String = "",
    val weekPlan: WeekPlan = WeekPlan(weekStart = ""),
    val weekOffset: Int = 0,
    val completedRuns: Int = 0,
    val totalPlannedRuns: Int = 0,
    val totalPlannedDistance: Double = 0.0,
    val completedDistance: Double = 0.0,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TrainingPlanViewModel @Inject constructor(
    private val trainingPlanRepository: TrainingPlanRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _weekOffset = MutableStateFlow(0)
    
    private val _uiState = MutableStateFlow(WeeklyPlanUiState())
    val uiState: StateFlow<WeeklyPlanUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                trainingPlanRepository.trainingPlanFlow,
                trainingPlanRepository.weekPlansFlow,
                _weekOffset,
                userRepository.userProfileFlow
            ) { plan, weekPlans, offset, userProfile ->
                val weekStart = trainingPlanRepository.getWeekStartForOffset(offset)
                val weekPlan = weekPlans[weekStart] ?: WeekPlan(weekStart = weekStart)
                
                val completedRuns = weekPlan.days.count { it.completed && it.runType != RunType.REST && it.runType != RunType.WORKOUT }
                val totalPlannedRuns = weekPlan.days.count { it.runType != RunType.REST && it.runType != RunType.WORKOUT }
                val totalPlannedDistance = weekPlan.days.sumOf { it.targetDistance ?: 0.0 }
                val completedDistance = weekPlan.days.filter { it.completed }.sumOf { it.targetDistance ?: 0.0 }

                WeeklyPlanUiState(
                    trainingPlan = plan,
                    currentWeekStart = weekStart,
                    weekPlan = weekPlan,
                    weekOffset = offset,
                    completedRuns = completedRuns,
                    totalPlannedRuns = totalPlannedRuns,
                    totalPlannedDistance = totalPlannedDistance,
                    completedDistance = completedDistance,
                    userProfile = userProfile,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun navigateWeek(offset: Int) {
        _weekOffset.value = offset
    }

    fun updateDayPlan(dayOfWeek: Int, dayPlan: DayPlan) {
        viewModelScope.launch {
            val weekStart = _uiState.value.currentWeekStart
            trainingPlanRepository.updateDayPlan(weekStart, dayOfWeek, dayPlan)
        }
    }

    fun toggleDayCompletion(dayOfWeek: Int) {
        viewModelScope.launch {
            val weekStart = _uiState.value.currentWeekStart
            trainingPlanRepository.toggleDayCompletion(weekStart, dayOfWeek)
        }
    }

    fun saveGoal(goalName: String, goalType: GoalType, targetDate: String?) {
        viewModelScope.launch {
            val currentPlan = _uiState.value.trainingPlan ?: TrainingPlan()
            val updatedPlan = currentPlan.copy(
                goalName = goalName,
                goalType = goalType,
                targetDate = targetDate,
                currentWeekStart = trainingPlanRepository.getCurrentWeekStart(),
                updatedAt = System.currentTimeMillis().toString()
            )
            trainingPlanRepository.saveTrainingPlan(updatedPlan)
        }
    }

    fun generatePlanFromGoal(goalDistanceMeters: Int, goalTimeSeconds: Int) {
        viewModelScope.launch {
            val generatedWeeks = com.kwyr.runnerplanner.util.TrainingPlanGenerator.generatePlan(
                goalDistanceMeters,
                goalTimeSeconds
            )
            
            val goalName = when (goalDistanceMeters) {
                5000 -> "5K Training Plan"
                7000 -> "7K Training Plan"
                10000 -> "10K Training Plan"
                13000 -> "13K Training Plan"
                15000 -> "15K Training Plan"
                17000 -> "17K Training Plan"
                19000 -> "19K Training Plan"
                21097 -> "Half Marathon Plan"
                25000 -> "25K Training Plan"
                27000 -> "27K Training Plan"
                30000 -> "30K Training Plan"
                33000 -> "33K Training Plan"
                35000 -> "35K Training Plan"
                37000 -> "37K Training Plan"
                42195 -> "Marathon Plan"
                else -> "Training Plan"
            }
            
            val plan = TrainingPlan(
                goalName = goalName,
                goalType = when (goalDistanceMeters) {
                    5000 -> GoalType.RACE_5K
                    7000 -> GoalType.RACE_7K
                    10000 -> GoalType.RACE_10K
                    13000 -> GoalType.RACE_13K
                    15000 -> GoalType.RACE_15K
                    17000 -> GoalType.RACE_17K
                    19000 -> GoalType.RACE_19K
                    21097 -> GoalType.RACE_HALF
                    25000 -> GoalType.RACE_25K
                    27000 -> GoalType.RACE_27K
                    30000 -> GoalType.RACE_30K
                    33000 -> GoalType.RACE_33K
                    35000 -> GoalType.RACE_35K
                    37000 -> GoalType.RACE_37K
                    42195 -> GoalType.RACE_MARATHON
                    else -> GoalType.CUSTOM
                },
                goalDistanceMeters = goalDistanceMeters,
                goalTimeSeconds = goalTimeSeconds,
                currentWeekStart = trainingPlanRepository.getCurrentWeekStart(),
                updatedAt = System.currentTimeMillis().toString()
            )
            
            // Save the training plan first
            trainingPlanRepository.saveTrainingPlan(plan).getOrNull()
            
            // Clear all existing week plans before saving new one
            trainingPlanRepository.clearAllWeekPlans().getOrNull()
            
            // Save only the current week (it will be used for all weeks)
            val currentWeekStart = trainingPlanRepository.getCurrentWeekStart()
            val weekPlan = WeekPlan(
                weekStart = currentWeekStart,
                days = generatedWeeks.first().days
            )
            
            android.util.Log.d("TrainingPlan", "Generated week: ${generatedWeeks.first().days.count { it.runType != RunType.REST }} workouts, ${generatedWeeks.first().totalVolumeMeters/1000.0}km")
            generatedWeeks.first().days.forEach { day ->
                android.util.Log.d("TrainingPlan", "Day ${day.dayOfWeek}: ${day.runType.displayName} - ${day.targetDistance}km")
            }
            
            trainingPlanRepository.saveWeekPlan(weekPlan).getOrNull()
            
            // Reset to current week to show the generated plan
            _weekOffset.value = 0
        }
    }

    fun getDayName(dayOfWeek: Int): String {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY + dayOfWeek)
        return SimpleDateFormat("EEE", Locale.US).format(calendar.time).uppercase()
    }

    fun getWeekDateRange(): String {
        val weekStart = _uiState.value.currentWeekStart
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val displayFormat = SimpleDateFormat("MMM d", Locale.US)
            val startDate = dateFormat.parse(weekStart) ?: return ""
            
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            val startStr = displayFormat.format(startDate)
            
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val endStr = displayFormat.format(calendar.time)
            
            return "$startStr - $endStr"
        } catch (e: Exception) {
            return ""
        }
    }
}
