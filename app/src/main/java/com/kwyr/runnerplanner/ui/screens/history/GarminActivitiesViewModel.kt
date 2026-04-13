package com.kwyr.runnerplanner.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.ActivityMode
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

    val selectedMode: StateFlow<ActivityMode> = userRepository.selectedModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityMode.RUNNING
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

    // Available years filtered by type
    val availableYears: StateFlow<List<Int>> = selectedMode
        .flatMapLatest { mode ->
            activityRepository.getDistinctYearsByType(mode.type)
                .map { years ->
                    if (years.isEmpty()) listOf(currentYear - 2, currentYear - 1, currentYear)
                    else years
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(currentYear)
        )

    // Available months filtered by type and year
    val availableMonths: StateFlow<List<Int>> = combine(
        _selectedYear,
        selectedMode
    ) { year, mode -> Pair(year, mode) }
        .flatMapLatest { (year, mode) ->
            activityRepository.getDistinctMonthsForYearByType(year, mode.type)
                .map { months ->
                    if (months.isEmpty()) (1..12).toList() else months
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(currentMonth)
        )

    // Activities filtered by week and type
    val activities: StateFlow<List<Activity>> = combine(
        combine(_selectedYear, _selectedMonth, _selectedWeek) { y, m, w -> Triple(y, m, w) },
        selectedMode
    ) { ymw, mode -> Pair(ymw, mode) }
        .flatMapLatest { (ymw, mode) ->
            activityRepository.getActivitiesByWeekAndType(ymw.first, ymw.second, ymw.third, mode.type)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weekStats: StateFlow<WeekStats> = activities
        .map { activityList ->
            WeekStats(
                totalDistance = activityList.sumOf { it.totalDistance },
                totalDuration = activityList.sumOf { it.totalDuration }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeekStats(0.0, 0.0)
        )

    init {
        viewModelScope.launch {
            activityRepository.migrateFromDataStore()
        }
    }

    fun selectYear(year: Int) {
        _selectedYear.value = year
        viewModelScope.launch {
            val mode = selectedMode.value
            val months = activityRepository.getDistinctMonthsForYearByType(year, mode.type).first()
            if (months.isNotEmpty()) _selectedMonth.value = months.first()
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
