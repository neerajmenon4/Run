package com.kwyr.runnerplanner.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.UserProfile
import com.kwyr.runnerplanner.data.repository.ActivityRepository
import com.kwyr.runnerplanner.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GarminActivitiesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    val userProfile: StateFlow<UserProfile> = userRepository.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    // Current date for default selection
    private val currentCalendar = Calendar.getInstance()
    private val currentYear = currentCalendar.get(Calendar.YEAR)
    private val currentMonth = currentCalendar.get(Calendar.MONTH) + 1
    
    // Selected filters
    private val _selectedYear = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()
    
    private val _selectedWeek = MutableStateFlow(1)
    val selectedWeek: StateFlow<Int> = _selectedWeek.asStateFlow()
    
    // Available years - show last 3 years and current year if no data exists
    val availableYears: StateFlow<List<Int>> = activityRepository.getDistinctYears()
        .map { years ->
            if (years.isEmpty()) {
                // Default to current year and previous 2 years
                listOf(currentYear - 2, currentYear - 1, currentYear)
            } else {
                years
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(currentYear)
        )
    
    // Available months - show all 12 months if no data for selected year
    val availableMonths: StateFlow<List<Int>> = _selectedYear
        .flatMapLatest { year ->
            activityRepository.getDistinctMonthsForYear(year)
                .map { months ->
                    if (months.isEmpty()) {
                        // Show all months
                        (1..12).toList()
                    } else {
                        months
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(currentMonth)
        )
    
    // Filtered activities based on selection
    val activities: StateFlow<List<Activity>> = combine(
        _selectedYear,
        _selectedMonth,
        _selectedWeek
    ) { year, month, week ->
        Triple(year, month, week)
    }.flatMapLatest { (year, month, week) ->
        activityRepository.getActivitiesByWeek(year, month, week)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Week statistics - automatically updates when activities change
    val weekStats: StateFlow<WeekStats> = combine(
        _selectedYear,
        _selectedMonth,
        _selectedWeek,
        activities
    ) { year, month, week, activityList ->
        // Calculate stats from the actual activity list
        val totalDistance = activityList.sumOf { it.totalDistance }
        val totalDuration = activityList.sumOf { it.totalDuration }
        WeekStats(totalDistance, totalDuration)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeekStats(0.0, 0.0)
    )
    
    init {
        // Trigger migration from DataStore to Room on first load
        viewModelScope.launch {
            activityRepository.migrateFromDataStore()
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
        // Reset to first available month when year changes
        viewModelScope.launch {
            val months = activityRepository.getDistinctMonthsForYear(year).first()
            if (months.isNotEmpty()) {
                _selectedMonth.value = months.first()
            }
            _selectedWeek.value = 1
        }
    }

    fun selectMonth(month: Int) {
        _selectedMonth.value = month
        _selectedWeek.value = 1
    }

    fun selectWeek(week: Int) {
        _selectedWeek.value = week
    }

    fun deleteActivity(activityId: String) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
        }
    }
}

data class WeekStats(
    val totalDistance: Double,
    val totalDuration: Double
)
