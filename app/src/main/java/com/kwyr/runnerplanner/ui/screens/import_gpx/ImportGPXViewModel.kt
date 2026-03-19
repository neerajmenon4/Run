package com.kwyr.runnerplanner.ui.screens.import_gpx

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.parser.GpxParser
import com.kwyr.runnerplanner.data.parser.TcxParser
import com.kwyr.runnerplanner.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Success(val activity: Activity) : ImportState()
    data class Error(val message: String) : ImportState()
}

@HiltViewModel
class ImportGPXViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

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

    fun resetState() {
        _importState.value = ImportState.Idle
    }
}
