package com.kwyr.runnerplanner.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kwyr.runnerplanner.data.model.BackupData
import com.kwyr.runnerplanner.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val backupData: BackupData? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val backupResult = backupRepository.createBackup()
            if (backupResult.isSuccess) {
                val backup = backupResult.getOrNull()!!
                val exportResult = backupRepository.exportBackupToUri(uri, backup)
                
                if (exportResult.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = "Backup exported successfully"
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            message = "Failed to export backup: ${exportResult.exceptionOrNull()?.message}"
                        )
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = "Failed to create backup: ${backupResult.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = backupRepository.importBackupFromUri(uri)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        backupData = result.getOrNull()
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = "Failed to import backup: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun restoreBackup(mergeData: Boolean) {
        viewModelScope.launch {
            val backup = _uiState.value.backupData ?: return@launch
            
            _uiState.update { it.copy(isLoading = true, backupData = null) }
            
            val result = backupRepository.restoreBackup(backup, mergeData)
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = if (mergeData) "Data merged successfully" else "Data restored successfully"
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        message = "Failed to restore backup: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
