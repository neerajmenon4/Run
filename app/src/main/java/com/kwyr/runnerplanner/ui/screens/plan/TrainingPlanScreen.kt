package com.kwyr.runnerplanner.ui.screens.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kwyr.runnerplanner.R
import com.kwyr.runnerplanner.data.model.*
import com.kwyr.runnerplanner.ui.components.ChevronLeftIcon
import com.kwyr.runnerplanner.ui.components.ChevronRightIcon
import com.kwyr.runnerplanner.ui.components.PlusIcon
import com.kwyr.runnerplanner.util.UnitConversion

@Composable
fun TrainingPlanScreen(
    viewModel: TrainingPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGoalDialog by remember { mutableStateOf(false) }
    var showEditDayDialog by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(
            onSetGoal = { showGoalDialog = true }
        )

        if (uiState.trainingPlan == null) {
            EmptyGoalState(onSetGoal = { showGoalDialog = true })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GoalCard(
                        goalName = uiState.trainingPlan?.goalName ?: "",
                        goalType = uiState.trainingPlan?.goalType ?: GoalType.CUSTOM,
                        targetDate = uiState.trainingPlan?.targetDate,
                        goalDistanceMeters = uiState.trainingPlan?.goalDistanceMeters ?: 0,
                        goalTimeSeconds = uiState.trainingPlan?.goalTimeSeconds ?: 0,
                        unitSystem = uiState.userProfile?.unitSystem ?: UnitSystem.METRIC,
                        onEdit = { showGoalDialog = true }
                    )
                }

                item {
                    WeekNavigator(
                        weekDateRange = viewModel.getWeekDateRange(),
                        weekOffset = uiState.weekOffset,
                        onPreviousWeek = { viewModel.navigateWeek(uiState.weekOffset - 1) },
                        onNextWeek = { viewModel.navigateWeek(uiState.weekOffset + 1) },
                        onCurrentWeek = { viewModel.navigateWeek(0) }
                    )
                }

                item {
                    WeeklySummary(
                        completedRuns = uiState.completedRuns,
                        totalPlannedRuns = uiState.totalPlannedRuns,
                        completedDistance = uiState.completedDistance,
                        totalPlannedDistance = uiState.totalPlannedDistance,
                        unitSystem = uiState.userProfile?.unitSystem ?: UnitSystem.METRIC
                    )
                }

                itemsIndexed(uiState.weekPlan.days) { index, dayPlan ->
                    DayCard(
                        dayName = viewModel.getDayName(index),
                        dayPlan = dayPlan,
                        onToggleComplete = { viewModel.toggleDayCompletion(index) },
                        onEdit = { showEditDayDialog = index }
                    )
                }
            }
        }
    }

    if (showGoalDialog) {
        GoalDialog(
            currentGoal = uiState.trainingPlan,
            onDismiss = { showGoalDialog = false },
            onSave = { goalInput ->
                viewModel.generatePlanFromGoal(
                    goalInput.goalDistanceMeters,
                    goalInput.goalTimeSeconds
                )
                showGoalDialog = false
            }
        )
    }

    showEditDayDialog?.let { dayIndex ->
        EditDayDialogRefactored(
            dayName = viewModel.getDayName(dayIndex),
            currentDayPlan = uiState.weekPlan.days[dayIndex],
            onDismiss = { showEditDayDialog = null },
            onSave = { dayPlan ->
                viewModel.updateDayPlan(dayIndex, dayPlan)
                showEditDayDialog = null
            }
        )
    }
}

@Composable
private fun TopBar(
    onSetGoal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Training Plan",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        TextButton(onClick = onSetGoal) {
            Text(
                "Set Goal",
                color = Color(0xFFFF6B35)
            )
        }
    }
}

@Composable
private fun EmptyGoalState(onSetGoal: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No Goal Set",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set a training goal to start planning your weekly runs",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSetGoal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Set Your Goal")
            }
        }
    }
}

@Composable
private fun GoalCard(
    goalName: String,
    goalType: GoalType,
    targetDate: String?,
    goalDistanceMeters: Int,
    goalTimeSeconds: Int,
    unitSystem: UnitSystem,
    onEdit: () -> Unit
) {
    val distanceKm = goalDistanceMeters / 1000.0
    val hours = goalTimeSeconds / 3600
    val minutes = (goalTimeSeconds % 3600) / 60
    val seconds = goalTimeSeconds % 60
    
    val timeStr = when {
        hours > 0 -> String.format("%dh %02dm %02ds", hours, minutes, seconds)
        minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
        else -> String.format("%ds", seconds)
    }
    
    val paceSecPerKm = if (distanceKm > 0) goalTimeSeconds / distanceKm else 0.0
    val paceFormatted = UnitConversion.formatPace(paceSecPerKm, unitSystem)
    val paceStr = "${paceFormatted.value} /${paceFormatted.unit.lowercase().replace("min/", "")}"
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFF6B35)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goalType.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goalName,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
                
                if (goalDistanceMeters > 0 && goalTimeSeconds > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Goal Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Target Pace",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = paceStr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
                
                targetDate?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Target: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            TextButton(onClick = onEdit) {
                Text("Edit", color = Color.White)
            }
        }
    }
}

@Composable
private fun WeekNavigator(
    weekDateRange: String,
    weekOffset: Int,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) {
            ChevronLeftIcon(color = MaterialTheme.colorScheme.onBackground)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = weekDateRange,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (weekOffset != 0) {
                TextButton(onClick = onCurrentWeek) {
                    Text(
                        "Back to Current Week",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF6B35)
                    )
                }
            }
        }
        
        IconButton(onClick = onNextWeek) {
            ChevronRightIcon(color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun WeeklySummary(
    completedRuns: Int,
    totalPlannedRuns: Int,
    completedDistance: Double,
    totalPlannedDistance: Double,
    unitSystem: UnitSystem
) {
    val completionPercentage = if (totalPlannedRuns > 0) {
        (completedRuns.toFloat() / totalPlannedRuns.toFloat() * 100).toInt()
    } else 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Week Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$completionPercentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF6B35)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFF6B35),
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Runs",
                    value = "$completedRuns / $totalPlannedRuns"
                )
                SummaryItem(
                    label = "Distance",
                    value = {
                        val completedFormatted = UnitConversion.formatDistance(completedDistance, unitSystem)
                        val totalFormatted = UnitConversion.formatDistance(totalPlannedDistance, unitSystem)
                        "${completedFormatted.value} / ${totalFormatted.value} ${completedFormatted.unit}"
                    }()
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DayCard(
    dayName: String,
    dayPlan: DayPlan,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val runTypeColor = Color(android.graphics.Color.parseColor(dayPlan.runType.colorHex))
    val summaryText = getConfigSummary(dayPlan)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = { if (dayPlan.runType != RunType.REST) isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(runTypeColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayPlan.runType.emoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = dayPlan.runType.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (summaryText.isNotEmpty()) {
                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = runTypeColor
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dayPlan.runType != RunType.REST) {
                        Checkbox(
                            checked = dayPlan.completed,
                            onCheckedChange = { onToggleComplete() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF6B35)
                            )
                        )
                    }
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            PlusIcon(color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded && dayPlan.runType != RunType.REST,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    )
                    
                    ConfigurationDetails(dayPlan.configuration)
                }
            }
        }
    }
}

@Composable
private fun ConfigurationDetails(config: RunConfiguration?) {
    when (config) {
        is EasyRunConfig -> {
            if (config.durationType == DurationType.DURATION && config.duration != null) {
                DetailRow("Duration", "${config.duration} min")
            } else if (config.distance != null) {
                DetailRow("Distance", "${config.distance} km")
            }
            DetailRow("Effort", config.effort)
            if (config.paceRangeMin != null && config.paceRangeMax != null) {
                DetailRow("Pace Range", "${config.paceRangeMin} - ${config.paceRangeMax}")
            }
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is TempoRunConfig -> {
            if (config.warmupDuration != null) DetailRow("Warmup", "${config.warmupDuration} min")
            DetailRow("Tempo", "${config.tempoDuration} min")
            if (config.tempoType == TempoType.EFFORT) {
                DetailRow("Effort", config.tempoEffort)
            } else if (config.tempoPace != null) {
                DetailRow("Pace", config.tempoPace)
            }
            if (config.cooldownDuration != null) DetailRow("Cooldown", "${config.cooldownDuration} min")
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is IntervalsConfig -> {
            if (config.warmupDuration != null) DetailRow("Warmup", "${config.warmupDuration} min")
            DetailRow("Intervals", "${config.reps}x ${config.workValue}${if (config.workType == IntervalType.DISTANCE) "m" else "s"} @ ${config.workPace}")
            DetailRow("Rest", "${config.restValue}${if (config.restType == IntervalType.TIME) "s" else "m"}")
            if (config.cooldownDuration != null) DetailRow("Cooldown", "${config.cooldownDuration} min")
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is LongRunConfig -> {
            if (config.durationType == DurationType.DURATION && config.duration != null) {
                DetailRow("Duration", "${config.duration} min")
            } else if (config.distance != null) {
                DetailRow("Distance", "${config.distance} km")
            }
            if (config.paceType == PaceType.EFFORT) {
                DetailRow("Effort", config.effort)
            } else if (config.pace != null) {
                DetailRow("Pace", config.pace)
            }
            DetailRow("Progression", when (config.progression) {
                ProgressionType.STEADY -> "Steady"
                ProgressionType.NEGATIVE_SPLIT -> "Negative Split"
                ProgressionType.PROGRESSIVE -> "Progressive"
            })
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is WorkoutConfig -> {
            DetailRow("Type", when (config.workoutType) {
                WorkoutType.STRENGTH -> "Strength"
                WorkoutType.CYCLING -> "Cycling"
                WorkoutType.MOBILITY -> "Mobility"
                WorkoutType.SWIMMING -> "Swimming"
                WorkoutType.OTHER -> "Other"
            })
            DetailRow("Duration", "${config.duration} min")
            DetailRow("Intensity", when (config.intensity) {
                IntensityLevel.LOW -> "Low"
                IntensityLevel.MEDIUM -> "Medium"
                IntensityLevel.HIGH -> "High"
            })
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is RestConfig -> {
            DetailRow("Type", if (config.restType == RestType.FULL_REST) "Full Rest" else "Active Recovery")
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        is RaceConfig -> {
            if (config.raceName.isNotEmpty()) DetailRow("Race", config.raceName)
            DetailRow("Distance", "${config.distance} km")
            if (config.goalType != RaceGoalType.FINISH && config.goalValue != null) {
                DetailRow(
                    if (config.goalType == RaceGoalType.TIME) "Goal Time" else "Goal Pace",
                    config.goalValue
                )
            }
            if (config.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        null -> {
            Text("Tap + to configure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

private fun getConfigSummary(dayPlan: DayPlan): String {
    return when (val config = dayPlan.configuration) {
        is EasyRunConfig -> {
            when {
                config.duration != null -> "${config.duration} min"
                config.distance != null -> "${config.distance} km"
                else -> ""
            }
        }
        is TempoRunConfig -> "${config.tempoDuration} min tempo"
        is IntervalsConfig -> "${config.reps}x ${config.workValue}${if (config.workType == IntervalType.DISTANCE) "m" else "s"}"
        is LongRunConfig -> {
            when {
                config.duration != null -> "${config.duration} min"
                config.distance != null -> "${config.distance} km"
                else -> ""
            }
        }
        is WorkoutConfig -> "${config.duration} min"
        is RestConfig -> if (config.restType == RestType.FULL_REST) "Full rest" else "Active recovery"
        is RaceConfig -> "${config.distance} km"
        null -> ""
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatDuration(seconds: Double): String {
    val minutes = (seconds / 60).toInt()
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        String.format("%d:%02d hrs", hours, mins)
    } else {
        String.format("%d min", mins)
    }
}

private fun formatPace(secondsPerKm: Double): String {
    val minutes = (secondsPerKm / 60).toInt()
    val seconds = (secondsPerKm % 60).toInt()
    return String.format("%d:%02d /km", minutes, seconds)
}
