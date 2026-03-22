package com.kwyr.runnerplanner.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kwyr.runnerplanner.R
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.UnitSystem
import com.kwyr.runnerplanner.ui.components.ChevronLeftIcon
import com.kwyr.runnerplanner.ui.components.TrashIcon
import com.kwyr.runnerplanner.util.UnitConversion
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GarminActivitiesScreen(
    viewModel: GarminActivitiesViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedWeek by viewModel.selectedWeek.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val weekStats by viewModel.weekStats.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    var expandedActivityId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar()
        
        // Week selector
        WeekSelector(
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            selectedWeek = selectedWeek,
            availableYears = availableYears,
            availableMonths = availableMonths,
            onYearSelected = viewModel::selectYear,
            onMonthSelected = viewModel::selectMonth,
            onWeekSelected = viewModel::selectWeek
        )
        
        // Week statistics
        WeekStatsCard(
            weekStats = weekStats,
            unitSystem = userProfile.unitSystem
        )

        if (activities.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    ActivityCard(
                        activity = activity,
                        unitSystem = userProfile.unitSystem,
                        onDelete = { viewModel.deleteActivity(activity.id) },
                        isExpanded = expandedActivityId == activity.id,
                        onToggleExpand = {
                            expandedActivityId = if (expandedActivityId == activity.id) null else activity.id
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.activities_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    unitSystem: UnitSystem,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Activity?") },
            text = { 
                Text("Are you sure you want to delete \"${activity.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val formattedDate = remember(activity.startTime) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(activity.startTime)
            if (date != null) dateFormat.format(date) else activity.startTime
        } catch (e: Exception) {
            activity.startTime
        }
    }

    val durationFormatted = remember(activity.totalDuration) {
        val hours = (activity.totalDuration / 3600).toInt()
        val minutes = ((activity.totalDuration % 3600) / 60).toInt()
        val seconds = (activity.totalDuration % 60).toInt()
        if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricItem(
                            label = "Distance",
                            value = {
                                val formatted = UnitConversion.formatDistance(activity.totalDistance / 1000.0, unitSystem)
                                "${formatted.value} ${formatted.unit}"
                            }(),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        MetricItem(
                            label = "Duration",
                            value = durationFormatted,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        activity.averageHeartRate?.let { hr ->
                            MetricItem(
                                label = "Avg HR",
                                value = "$hr bpm",
                                valueColor = androidx.compose.ui.graphics.Color(0xFFFF6B35)
                            )
                        }
                        activity.averagePace?.let { pace ->
                            val minutes = (pace / 60).toInt()
                            val seconds = (pace % 60).toInt()
                            MetricItem(
                                label = "Avg Pace",
                                value = "$minutes:${String.format("%02d", seconds)}/km",
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TrashIcon(color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        text = "Split Times",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (activity.splits.isEmpty()) {
                        Text(
                            text = "No split data available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        activity.splits.forEach { split ->
                            SplitItem(
                                splitNumber = split.splitNumber,
                                distance = split.distance,
                                duration = split.duration,
                                pace = split.pace,
                                heartRate = split.averageHeartRate,
                                unitSystem = unitSystem
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String, 
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

@Composable
private fun SplitItem(
    splitNumber: Int,
    distance: Double,
    duration: Double,
    pace: Double,
    heartRate: Int?,
    unitSystem: UnitSystem
) {
    val durationFormatted = remember(duration) {
        val minutes = (duration / 60).toInt()
        val seconds = (duration % 60).toInt()
        String.format("%d:%02d", minutes, seconds)
    }
    
    val distanceFormatted = remember(distance, unitSystem) {
        val formatted = UnitConversion.formatDistance(distance / 1000.0, unitSystem)
        "${formatted.value} ${formatted.unit}"
    }
    
    val paceFormatted = remember(pace, unitSystem) {
        val formatted = UnitConversion.formatPace(pace, unitSystem)
        "${formatted.value} /${formatted.unit.lowercase().replace("min/", "")}"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Split $splitNumber",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = durationFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pace",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = paceFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            heartRate?.let { hr ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "HR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "$hr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color(0xFFFF6B35)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekSelector(
    selectedYear: Int,
    selectedMonth: Int,
    selectedWeek: Int,
    availableYears: List<Int>,
    availableMonths: List<Int>,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onWeekSelected: (Int) -> Unit
) {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    
    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var weekExpanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Period:",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
        
        // Year dropdown
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { yearExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedYear.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false }
            ) {
                availableYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearSelected(year)
                            yearExpanded = false
                        }
                    )
                }
            }
        }
        
        // Month dropdown
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { monthExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = monthNames.getOrNull(selectedMonth - 1) ?: selectedMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false }
            ) {
                availableMonths.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(monthNames.getOrNull(month - 1) ?: month.toString()) },
                        onClick = {
                            onMonthSelected(month)
                            monthExpanded = false
                        }
                    )
                }
            }
        }
        
        // Week dropdown
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { weekExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Week $selectedWeek",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(
                expanded = weekExpanded,
                onDismissRequest = { weekExpanded = false }
            ) {
                (1..5).forEach { week ->
                    DropdownMenuItem(
                        text = { Text("Week $week") },
                        onClick = {
                            onWeekSelected(week)
                            weekExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekStatsCard(weekStats: WeekStats, unitSystem: UnitSystem) {
    if (weekStats.totalDistance > 0 || weekStats.totalDuration > 0) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Distance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = {
                            val formatted = UnitConversion.formatDistance(weekStats.totalDistance / 1000.0, unitSystem)
                            "${formatted.value} ${formatted.unit}"
                        }(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    val hours = (weekStats.totalDuration / 3600).toInt()
                    val minutes = ((weekStats.totalDuration % 3600) / 60).toInt()
                    Text(
                        text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_activities),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
