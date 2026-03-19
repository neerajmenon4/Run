package com.kwyr.runnerplanner.ui.screens.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.UnitSystem
import com.kwyr.runnerplanner.data.parser.TcxParser
import com.kwyr.runnerplanner.data.parser.GpxParser
import com.kwyr.runnerplanner.data.repository.ActivityRepository
import com.kwyr.runnerplanner.data.repository.UserRepository
import com.kwyr.runnerplanner.ui.screens.import_gpx.ImportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SpeedDataPoint(
    val date: String,
    val speed: Double
)

enum class TimePeriod {
    ONE_WEEK,
    ONE_MONTH,
    THREE_MONTHS
}

data class HomeUiState(
    val userName: String = "Runner",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val activities: List<Activity> = emptyList(),
    val totalDistance: Double = 0.0,
    val avgSpeed: Double = 0.0,
    val timePeriod: TimePeriod = TimePeriod.ONE_MONTH,
    val speedData: List<SpeedDataPoint> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    init {
        loadData()
    }

    fun parseFile(uri: Uri, content: String, fileName: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading

            try {
                val activity = when {
                    fileName.endsWith(".tcx", ignoreCase = true) -> {
                        TcxParser.parseTcx(content)
                    }
                    fileName.endsWith(".gpx", ignoreCase = true) -> {
                        val gpxTrack = GpxParser.parseGpx(content)
                        null
                    }
                    else -> null
                }

                if (activity != null) {
                    activityRepository.saveActivity(activity)
                    _importState.value = ImportState.Success(activity)
                } else {
                    _importState.value = ImportState.Error("Failed to parse file")
                }
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetImportState() {
        _importState.value = ImportState.Idle
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                activityRepository.activitiesFlow,
                userRepository.userProfileFlow
            ) { activities, profile ->
                val totalDist = activities.sumOf { it.totalDistance }
                val totalSeconds = activities.sumOf { it.totalDuration }
                val totalKm = totalDist / 1000.0
                val totalHours = totalSeconds / 3600.0
                val avgSpd = if (totalHours > 0) totalKm / totalHours else 0.0

                _uiState.value.copy(
                    userName = profile.name,
                    unitSystem = profile.unitSystem,
                    activities = activities,
                    totalDistance = totalDist,
                    avgSpeed = avgSpd,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
                generateSpeedData(newState.activities, newState.timePeriod)
            }
        }
    }

    fun setTimePeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(timePeriod = period)
        generateSpeedData(_uiState.value.activities, period)
    }

    private fun generateSpeedData(activities: List<Activity>, period: TimePeriod) {
        val now = Date()
        val daysBack = when (period) {
            TimePeriod.ONE_WEEK -> 7
            TimePeriod.ONE_MONTH -> 30
            TimePeriod.THREE_MONTHS -> 90
        }

        val cutoffDate = Date(now.time - daysBack * 24 * 60 * 60 * 1000L)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val filteredActivities = activities.filter {
            try {
                val activityDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(it.startTime)
                activityDate != null && activityDate >= cutoffDate
            } catch (e: Exception) {
                false
            }
        }

        val dailyData = mutableMapOf<String, Triple<Double, Double, Int>>()

        filteredActivities.forEach { activity ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(activity.startTime)
                if (date != null) {
                    val dayKey = dateFormat.format(date)
                    val current = dailyData[dayKey] ?: Triple(0.0, 0.0, 0)
                    dailyData[dayKey] = Triple(
                        current.first + activity.totalDistance,
                        current.second + activity.totalDuration,
                        current.third + 1
                    )
                }
            } catch (e: Exception) {
            }
        }

        val data = dailyData.map { (date, stats) ->
            val speed = (stats.first / 1000.0) / (stats.second / 3600.0)
            SpeedDataPoint(date, speed)
        }.sortedBy { it.date }

        _uiState.value = _uiState.value.copy(speedData = data)
    }
}
