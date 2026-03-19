package com.kwyr.runnerplanner.ui.screens.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kwyr.runnerplanner.data.model.GoalType
import com.kwyr.runnerplanner.data.model.TrainingPlan

data class GoalInput(
    val goalDistanceMeters: Int,
    val goalTimeSeconds: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    currentGoal: TrainingPlan?,
    onDismiss: () -> Unit,
    onSave: (GoalInput) -> Unit
) {
    var selectedDistance by remember { mutableStateOf(0) }
    var hours by remember { mutableStateOf("0") }
    var minutes by remember { mutableStateOf("30") }
    var seconds by remember { mutableStateOf("0") }
    var distanceExpanded by remember { mutableStateOf(false) }

    val distances = listOf(
        "5K" to 5000,
        "7K" to 7000,
        "10K" to 10000,
        "13K" to 13000,
        "15K" to 15000,
        "17K" to 17000,
        "19K" to 19000,
        "Half Marathon" to 21097,
        "25K" to 25000,
        "27K" to 27000,
        "30K" to 30000,
        "33K" to 33000,
        "35K" to 35000,
        "37K" to 37000,
        "Marathon" to 42195
    )

    val totalSeconds = (hours.toIntOrNull() ?: 0) * 3600 + 
                      (minutes.toIntOrNull() ?: 0) * 60 + 
                      (seconds.toIntOrNull() ?: 0)
    
    val racePace = if (totalSeconds > 0 && distances[selectedDistance].second > 0) {
        val km = distances[selectedDistance].second / 1000.0
        val paceSeconds = totalSeconds / km
        val paceMin = (paceSeconds / 60).toInt()
        val paceSec = (paceSeconds % 60).toInt()
        String.format("%d:%02d /km", paceMin, paceSec)
    } else ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Training Plan",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "GOAL DISTANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                
                ExposedDropdownMenuBox(
                    expanded = distanceExpanded,
                    onExpandedChange = { distanceExpanded = !distanceExpanded }
                ) {
                    OutlinedTextField(
                        value = distances[selectedDistance].first,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distanceExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = distanceExpanded,
                        onDismissRequest = { distanceExpanded = false }
                    ) {
                        distances.forEachIndexed { index, (label, _) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedDistance = index
                                    distanceExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "GOAL TIME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) hours = it },
                        label = { Text("Hours") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) minutes = it },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) seconds = it },
                        label = { Text("Sec") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (racePace.isNotEmpty()) {
                    Text(
                        text = "Pace: $racePace",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (totalSeconds > 0) {
                        onSave(
                            GoalInput(
                                goalDistanceMeters = distances[selectedDistance].second,
                                goalTimeSeconds = totalSeconds
                            )
                        )
                    }
                },
                enabled = totalSeconds > 0
            ) {
                Text("Generate Plan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
