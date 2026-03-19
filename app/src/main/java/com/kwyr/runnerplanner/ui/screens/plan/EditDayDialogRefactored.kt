package com.kwyr.runnerplanner.ui.screens.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kwyr.runnerplanner.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDayDialogRefactored(
    dayName: String,
    currentDayPlan: DayPlan,
    onDismiss: () -> Unit,
    onSave: (DayPlan) -> Unit
) {
    var selectedRunType by remember { mutableStateOf(currentDayPlan.runType) }
    var expanded by remember { mutableStateOf(false) }
    
    // State for each run type configuration
    var easyConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? EasyRunConfig) ?: EasyRunConfig()
    ) }
    var tempoConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? TempoRunConfig) ?: TempoRunConfig()
    ) }
    var intervalsConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? IntervalsConfig) ?: IntervalsConfig()
    ) }
    var longConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? LongRunConfig) ?: LongRunConfig()
    ) }
    var workoutConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? WorkoutConfig) ?: WorkoutConfig()
    ) }
    var restConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? RestConfig) ?: RestConfig()
    ) }
    var raceConfig by remember { mutableStateOf(
        (currentDayPlan.configuration as? RaceConfig) ?: RaceConfig()
    ) }

    // Reset configuration when run type changes
    LaunchedEffect(selectedRunType) {
        if (selectedRunType != currentDayPlan.runType) {
            when (selectedRunType) {
                RunType.EASY -> easyConfig = EasyRunConfig()
                RunType.TEMPO -> tempoConfig = TempoRunConfig()
                RunType.SPEED -> intervalsConfig = IntervalsConfig()
                RunType.LONG -> longConfig = LongRunConfig()
                RunType.WORKOUT -> workoutConfig = WorkoutConfig()
                RunType.REST -> restConfig = RestConfig()
                RunType.RACE -> raceConfig = RaceConfig()
                RunType.RACE_PRACTICE -> tempoConfig = TempoRunConfig()
            }
        }
    }

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
                // Run Type Selector
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Dynamic fields based on run type
                when (selectedRunType) {
                    RunType.EASY -> EasyRunFields(
                        config = easyConfig,
                        onConfigChange = { easyConfig = it }
                    )
                    RunType.TEMPO, RunType.RACE_PRACTICE -> TempoRunFields(
                        config = tempoConfig,
                        onConfigChange = { tempoConfig = it }
                    )
                    RunType.SPEED -> IntervalsFields(
                        config = intervalsConfig,
                        onConfigChange = { intervalsConfig = it }
                    )
                    RunType.LONG -> LongRunFields(
                        config = longConfig,
                        onConfigChange = { longConfig = it }
                    )
                    RunType.WORKOUT -> WorkoutFields(
                        config = workoutConfig,
                        onConfigChange = { workoutConfig = it }
                    )
                    RunType.REST -> RestFields(
                        config = restConfig,
                        onConfigChange = { restConfig = it }
                    )
                    RunType.RACE -> RaceFields(
                        config = raceConfig,
                        onConfigChange = { raceConfig = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val configuration = when (selectedRunType) {
                        RunType.EASY -> easyConfig
                        RunType.TEMPO, RunType.RACE_PRACTICE -> tempoConfig
                        RunType.SPEED -> intervalsConfig
                        RunType.LONG -> longConfig
                        RunType.WORKOUT -> workoutConfig
                        RunType.REST -> restConfig
                        RunType.RACE -> raceConfig
                    }
                    
                    val updatedDayPlan = currentDayPlan.copy(
                        runType = selectedRunType,
                        configuration = configuration
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

@Composable
private fun EasyRunFields(
    config: EasyRunConfig,
    onConfigChange: (EasyRunConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Easy Run Configuration", style = MaterialTheme.typography.labelLarge)
        
        // Duration vs Distance toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.durationType == DurationType.DURATION,
                onClick = { onConfigChange(config.copy(durationType = DurationType.DURATION)) },
                label = { Text("Duration") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.durationType == DurationType.DISTANCE,
                onClick = { onConfigChange(config.copy(durationType = DurationType.DISTANCE)) },
                label = { Text("Distance") },
                modifier = Modifier.weight(1f)
            )
        }

        if (config.durationType == DurationType.DURATION) {
            OutlinedTextField(
                value = config.duration?.toString() ?: "",
                onValueChange = { onConfigChange(config.copy(duration = it.toIntOrNull())) },
                label = { Text("Duration (minutes)") },
                placeholder = { Text("30") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = config.distance?.toString() ?: "",
                onValueChange = { onConfigChange(config.copy(distance = it.toDoubleOrNull())) },
                label = { Text("Distance (km)") },
                placeholder = { Text("5.0") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = config.effort,
            onValueChange = { onConfigChange(config.copy(effort = it)) },
            label = { Text("Effort Level") },
            placeholder = { Text("Easy, Moderate, RPE 3") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Pace Range (Optional)", style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = config.paceRangeMin ?: "",
                onValueChange = { onConfigChange(config.copy(paceRangeMin = it.ifBlank { null })) },
                label = { Text("Min (5:30)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = config.paceRangeMax ?: "",
                onValueChange = { onConfigChange(config.copy(paceRangeMax = it.ifBlank { null })) },
                label = { Text("Max (6:00)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
private fun TempoRunFields(
    config: TempoRunConfig,
    onConfigChange: (TempoRunConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tempo Run Configuration", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = config.warmupDuration?.toString() ?: "",
            onValueChange = { onConfigChange(config.copy(warmupDuration = it.toIntOrNull())) },
            label = { Text("Warmup (minutes, optional)") },
            placeholder = { Text("10") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = config.tempoDuration.toString(),
            onValueChange = { onConfigChange(config.copy(tempoDuration = it.toIntOrNull() ?: 20)) },
            label = { Text("Tempo Duration (minutes) *") },
            placeholder = { Text("20") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.tempoType == TempoType.EFFORT,
                onClick = { onConfigChange(config.copy(tempoType = TempoType.EFFORT)) },
                label = { Text("By Effort") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.tempoType == TempoType.PACE,
                onClick = { onConfigChange(config.copy(tempoType = TempoType.PACE)) },
                label = { Text("By Pace") },
                modifier = Modifier.weight(1f)
            )
        }

        if (config.tempoType == TempoType.EFFORT) {
            OutlinedTextField(
                value = config.tempoEffort,
                onValueChange = { onConfigChange(config.copy(tempoEffort = it)) },
                label = { Text("Tempo Effort") },
                placeholder = { Text("Comfortably Hard, Threshold") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = config.tempoPace ?: "",
                onValueChange = { onConfigChange(config.copy(tempoPace = it.ifBlank { null })) },
                label = { Text("Tempo Pace (min/km)") },
                placeholder = { Text("4:30") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = config.cooldownDuration?.toString() ?: "",
            onValueChange = { onConfigChange(config.copy(cooldownDuration = it.toIntOrNull())) },
            label = { Text("Cooldown (minutes, optional)") },
            placeholder = { Text("10") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
private fun IntervalsFields(
    config: IntervalsConfig,
    onConfigChange: (IntervalsConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Intervals Configuration", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = config.warmupDuration?.toString() ?: "",
            onValueChange = { onConfigChange(config.copy(warmupDuration = it.toIntOrNull())) },
            label = { Text("Warmup (minutes, optional)") },
            placeholder = { Text("10") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = config.reps.toString(),
            onValueChange = { onConfigChange(config.copy(reps = it.toIntOrNull() ?: 6)) },
            label = { Text("Number of Repeats *") },
            placeholder = { Text("6") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Text("Work Interval", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.workType == IntervalType.DISTANCE,
                onClick = { onConfigChange(config.copy(workType = IntervalType.DISTANCE)) },
                label = { Text("Distance") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.workType == IntervalType.TIME,
                onClick = { onConfigChange(config.copy(workType = IntervalType.TIME)) },
                label = { Text("Time") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = config.workValue,
                onValueChange = { onConfigChange(config.copy(workValue = it)) },
                label = { Text(if (config.workType == IntervalType.DISTANCE) "Meters" else "Seconds") },
                placeholder = { Text(if (config.workType == IntervalType.DISTANCE) "400" else "90") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = config.workPace,
                onValueChange = { onConfigChange(config.copy(workPace = it)) },
                label = { Text("Pace") },
                placeholder = { Text("4:00/km") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Text("Rest Interval", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.restType == IntervalType.TIME,
                onClick = { onConfigChange(config.copy(restType = IntervalType.TIME)) },
                label = { Text("Time") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.restType == IntervalType.DISTANCE,
                onClick = { onConfigChange(config.copy(restType = IntervalType.DISTANCE)) },
                label = { Text("Distance") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = config.restValue,
            onValueChange = { onConfigChange(config.copy(restValue = it)) },
            label = { Text(if (config.restType == IntervalType.TIME) "Seconds" else "Meters") },
            placeholder = { Text(if (config.restType == IntervalType.TIME) "90" else "200") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = config.cooldownDuration?.toString() ?: "",
            onValueChange = { onConfigChange(config.copy(cooldownDuration = it.toIntOrNull())) },
            label = { Text("Cooldown (minutes, optional)") },
            placeholder = { Text("10") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongRunFields(
    config: LongRunConfig,
    onConfigChange: (LongRunConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Long Run Configuration", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.durationType == DurationType.DURATION,
                onClick = { onConfigChange(config.copy(durationType = DurationType.DURATION)) },
                label = { Text("Duration") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.durationType == DurationType.DISTANCE,
                onClick = { onConfigChange(config.copy(durationType = DurationType.DISTANCE)) },
                label = { Text("Distance") },
                modifier = Modifier.weight(1f)
            )
        }

        if (config.durationType == DurationType.DURATION) {
            OutlinedTextField(
                value = config.duration?.toString() ?: "",
                onValueChange = { onConfigChange(config.copy(duration = it.toIntOrNull())) },
                label = { Text("Duration (minutes)") },
                placeholder = { Text("90") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = config.distance?.toString() ?: "",
                onValueChange = { onConfigChange(config.copy(distance = it.toDoubleOrNull())) },
                label = { Text("Distance (km)") },
                placeholder = { Text("15.0") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.paceType == PaceType.EFFORT,
                onClick = { onConfigChange(config.copy(paceType = PaceType.EFFORT)) },
                label = { Text("By Effort") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.paceType == PaceType.PACE,
                onClick = { onConfigChange(config.copy(paceType = PaceType.PACE)) },
                label = { Text("By Pace") },
                modifier = Modifier.weight(1f)
            )
        }

        if (config.paceType == PaceType.EFFORT) {
            OutlinedTextField(
                value = config.effort,
                onValueChange = { onConfigChange(config.copy(effort = it)) },
                label = { Text("Effort Level") },
                placeholder = { Text("Easy, Conversational") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            OutlinedTextField(
                value = config.pace ?: "",
                onValueChange = { onConfigChange(config.copy(pace = it.ifBlank { null })) },
                label = { Text("Target Pace (min/km)") },
                placeholder = { Text("5:30") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        var progressionExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = progressionExpanded,
            onExpandedChange = { progressionExpanded = !progressionExpanded }
        ) {
            OutlinedTextField(
                value = when (config.progression) {
                    ProgressionType.STEADY -> "Steady Pace"
                    ProgressionType.NEGATIVE_SPLIT -> "Negative Split"
                    ProgressionType.PROGRESSIVE -> "Progressive"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Progression") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = progressionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = progressionExpanded,
                onDismissRequest = { progressionExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Steady Pace") },
                    onClick = {
                        onConfigChange(config.copy(progression = ProgressionType.STEADY))
                        progressionExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Negative Split") },
                    onClick = {
                        onConfigChange(config.copy(progression = ProgressionType.NEGATIVE_SPLIT))
                        progressionExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Progressive") },
                    onClick = {
                        onConfigChange(config.copy(progression = ProgressionType.PROGRESSIVE))
                        progressionExpanded = false
                    }
                )
            }
        }

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutFields(
    config: WorkoutConfig,
    onConfigChange: (WorkoutConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Workout Configuration", style = MaterialTheme.typography.labelLarge)

        var workoutTypeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = workoutTypeExpanded,
            onExpandedChange = { workoutTypeExpanded = !workoutTypeExpanded }
        ) {
            OutlinedTextField(
                value = when (config.workoutType) {
                    WorkoutType.STRENGTH -> "Strength Training"
                    WorkoutType.CYCLING -> "Cycling"
                    WorkoutType.MOBILITY -> "Mobility/Yoga"
                    WorkoutType.SWIMMING -> "Swimming"
                    WorkoutType.OTHER -> "Other"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Workout Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workoutTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = workoutTypeExpanded,
                onDismissRequest = { workoutTypeExpanded = false }
            ) {
                WorkoutType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(when (type) {
                            WorkoutType.STRENGTH -> "Strength Training"
                            WorkoutType.CYCLING -> "Cycling"
                            WorkoutType.MOBILITY -> "Mobility/Yoga"
                            WorkoutType.SWIMMING -> "Swimming"
                            WorkoutType.OTHER -> "Other"
                        }) },
                        onClick = {
                            onConfigChange(config.copy(workoutType = type))
                            workoutTypeExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = config.duration.toString(),
            onValueChange = { onConfigChange(config.copy(duration = it.toIntOrNull() ?: 30)) },
            label = { Text("Duration (minutes)") },
            placeholder = { Text("30") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Text("Intensity Level", style = MaterialTheme.typography.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.intensity == IntensityLevel.LOW,
                onClick = { onConfigChange(config.copy(intensity = IntensityLevel.LOW)) },
                label = { Text("Low") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.intensity == IntensityLevel.MEDIUM,
                onClick = { onConfigChange(config.copy(intensity = IntensityLevel.MEDIUM)) },
                label = { Text("Medium") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.intensity == IntensityLevel.HIGH,
                onClick = { onConfigChange(config.copy(intensity = IntensityLevel.HIGH)) },
                label = { Text("High") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@Composable
private fun RestFields(
    config: RestConfig,
    onConfigChange: (RestConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Rest Day Configuration", style = MaterialTheme.typography.labelLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = config.restType == RestType.FULL_REST,
                onClick = { onConfigChange(config.copy(restType = RestType.FULL_REST)) },
                label = { Text("Full Rest") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = config.restType == RestType.ACTIVE_RECOVERY,
                onClick = { onConfigChange(config.copy(restType = RestType.ACTIVE_RECOVERY)) },
                label = { Text("Active Recovery") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            placeholder = { Text("Light stretching, walking, etc.") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RaceFields(
    config: RaceConfig,
    onConfigChange: (RaceConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Race Day Configuration", style = MaterialTheme.typography.labelLarge)

        OutlinedTextField(
            value = config.raceName,
            onValueChange = { onConfigChange(config.copy(raceName = it)) },
            label = { Text("Race Name") },
            placeholder = { Text("City 5K Championship") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = config.distance.toString(),
            onValueChange = { onConfigChange(config.copy(distance = it.toDoubleOrNull() ?: 5.0)) },
            label = { Text("Distance (km)") },
            placeholder = { Text("5.0") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        var goalTypeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = goalTypeExpanded,
            onExpandedChange = { goalTypeExpanded = !goalTypeExpanded }
        ) {
            OutlinedTextField(
                value = when (config.goalType) {
                    RaceGoalType.FINISH -> "Just Finish"
                    RaceGoalType.TIME -> "Target Time"
                    RaceGoalType.PACE -> "Target Pace"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Goal Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = goalTypeExpanded,
                onDismissRequest = { goalTypeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Just Finish") },
                    onClick = {
                        onConfigChange(config.copy(goalType = RaceGoalType.FINISH, goalValue = null))
                        goalTypeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Target Time") },
                    onClick = {
                        onConfigChange(config.copy(goalType = RaceGoalType.TIME))
                        goalTypeExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Target Pace") },
                    onClick = {
                        onConfigChange(config.copy(goalType = RaceGoalType.PACE))
                        goalTypeExpanded = false
                    }
                )
            }
        }

        if (config.goalType != RaceGoalType.FINISH) {
            OutlinedTextField(
                value = config.goalValue ?: "",
                onValueChange = { onConfigChange(config.copy(goalValue = it.ifBlank { null })) },
                label = { Text(if (config.goalType == RaceGoalType.TIME) "Goal Time (20:00)" else "Goal Pace (4:00/km)") },
                placeholder = { Text(if (config.goalType == RaceGoalType.TIME) "20:00" else "4:00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = config.notes,
            onValueChange = { onConfigChange(config.copy(notes = it)) },
            label = { Text("Notes") },
            placeholder = { Text("Race strategy, nutrition plan, etc.") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
    }
}
