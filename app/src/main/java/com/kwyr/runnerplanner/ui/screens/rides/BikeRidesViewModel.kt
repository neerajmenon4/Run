package com.kwyr.runnerplanner.ui.screens.rides

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.UnitSystem
import com.kwyr.runnerplanner.data.repository.ActivityRepository
import com.kwyr.runnerplanner.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BikeRidesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val currentCalendar = Calendar.getInstance()
    private val currentYear  = currentCalendar.get(Calendar.YEAR)
    private val currentMonth = currentCalendar.get(Calendar.MONTH) + 1

    private val _selectedYear  = MutableStateFlow(currentYear)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(currentMonth)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedWeek  = MutableStateFlow(1)
    val selectedWeek: StateFlow<Int> = _selectedWeek.asStateFlow()

    val availableYears: StateFlow<List<Int>> = activityRepository.getDistinctYearsByType("biking")
        .map { years -> if (years.isEmpty()) listOf(currentYear - 1, currentYear) else years }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(currentYear))

    val availableMonths: StateFlow<List<Int>> = _selectedYear
        .flatMapLatest { year ->
            activityRepository.getDistinctMonthsForYearByType(year, "biking")
                .map { months -> if (months.isEmpty()) (1..12).toList() else months }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(currentMonth))

    val rides: StateFlow<List<Activity>> = combine(
        combine(_selectedYear, _selectedMonth, _selectedWeek) { y, m, w -> Triple(y, m, w) }
    ) { it[0] }
        .flatMapLatest { (year, month, week) ->
            activityRepository.getActivitiesByWeekAndType(year, month, week, "biking")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unitSystem: StateFlow<UnitSystem> = userRepository.userProfileFlow
        .map { it.unitSystem }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UnitSystem.METRIC)

    fun selectYear(year: Int) {
        _selectedYear.value = year
        viewModelScope.launch {
            val months = activityRepository.getDistinctMonthsForYearByType(year, "biking").first()
            _selectedMonth.value = if (months.isNotEmpty()) months.first() else currentMonth
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
}
