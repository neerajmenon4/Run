package com.kwyr.runnerplanner.ui.screens.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kwyr.runnerplanner.data.model.DayPlan
import com.kwyr.runnerplanner.data.model.RunType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayDialog(
    dayName: String,
    currentDayPlan: DayPlan,
    onDismiss: () -> Unit,
    onSave: (DayPlan) -> Unit
) {
    var selectedRunType by remember { mutableStateOf(currentDayPlan.runType) }
    var targetDistance by remember { mutableStateOf(currentDayPlan.targetDistance?.toString() ?: "") }
    var targetDuration by remember { mutableStateOf(
        currentDayPlan.targetDuration?.let { (it / 60).toInt().toString() } ?: ""
    ) }
    var targetPaceMin by remember { mutableStateOf(
        currentDayPlan.targetPace?.let { (it / 60).toInt().toString() } ?: ""
    ) }
    var targetPaceSec by remember { mutableStateOf(
        currentDayPlan.targetPace?.let { (it % 60).toInt().toString() } ?: ""
    ) }
    var notes by remember { mutableStateOf(currentDayPlan.notes) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Plan $dayName",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedRunType.emoji} ${selectedRunType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Run Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        RunType.values().forEach { runType ->
                            DropdownMenuItem(
                                text = { Text("${runType.emoji} ${runType.displayName}") },
                                onClick = {
                                    selectedRunType = runType
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedRunType != RunType.REST) {
                    OutlinedTextField(
                        value = targetDistance,
                        onValueChange = { targetDistance = it },
                        label = { Text("Distance (km)") },
                        placeholder = { Text("5.0") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = targetDuration,
                        onValueChange = { targetDuration = it },
                        label = { Text("Duration (minutes)") },
                        placeholder = { Text("30") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetPaceMin,
                            onValueChange = { targetPaceMin = it },
                            label = { Text("Pace (min)") },
                            placeholder = { Text("5") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = targetPaceSec,
                            onValueChange = { targetPaceSec = it },
                            label = { Text("Pace (sec)") },
                            placeholder = { Text("30") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("e.g., 3x1km intervals") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedDayPlan = currentDayPlan.copy(
                        runType = selectedRunType,
                        targetDistance = targetDistance.toDoubleOrNull(),
                        targetDuration = targetDuration.toIntOrNull()?.let { it * 60.0 },
                        targetPace = if (targetPaceMin.isNotBlank() && targetPaceSec.isNotBlank()) {
                            (targetPaceMin.toIntOrNull() ?: 0) * 60.0 + (targetPaceSec.toIntOrNull() ?: 0)
                        } else null,
                        notes = notes
                    )
                    onSave(updatedDayPlan)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
