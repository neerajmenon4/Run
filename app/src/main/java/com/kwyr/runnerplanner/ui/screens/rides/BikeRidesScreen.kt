package com.kwyr.runnerplanner.ui.screens.rides

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.Trackpoint
import com.kwyr.runnerplanner.data.model.UnitSystem
import com.kwyr.runnerplanner.util.UnitConversion
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.min

private val ZONE_SLOW  = Color(0xFF4FC3F7)
private val ZONE_MID   = Color(0xFF66BB6A)
private val ZONE_TEMPO = Color(0xFFFFA726)
private val ZONE_FAST  = Color(0xFFEF5350)

private fun speedFractionToColor(fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return when {
        f < 0.33f -> lerp(ZONE_SLOW,  ZONE_MID,   f / 0.33f)
        f < 0.66f -> lerp(ZONE_MID,   ZONE_TEMPO, (f - 0.33f) / 0.33f)
        else      -> lerp(ZONE_TEMPO, ZONE_FAST,  (f - 0.66f) / 0.34f)
    }
}

@Composable
fun BikeRidesScreen(
    viewModel: BikeRidesViewModel = hiltViewModel()
) {
    val rides           by viewModel.rides.collectAsStateWithLifecycle()
    val unitSystem      by viewModel.unitSystem.collectAsStateWithLifecycle()
    val selectedYear    by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth   by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val selectedWeek    by viewModel.selectedWeek.collectAsStateWithLifecycle()
    val availableYears  by viewModel.availableYears.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()

    // Collapse state lives at screen level so LazyColumn recycling doesn't reset it
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar()

        WeekSelector(
            selectedYear    = selectedYear,
            selectedMonth   = selectedMonth,
            selectedWeek    = selectedWeek,
            availableYears  = availableYears,
            availableMonths = availableMonths,
            onYearSelected  = viewModel::selectYear,
            onMonthSelected = viewModel::selectMonth,
            onWeekSelected  = viewModel::selectWeek
        )

        if (rides.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rides, key = { it.id }) { ride ->
                    val isExpanded = expandedStates[ride.id] ?: true
                    RideCard(
                        ride       = ride,
                        unitSystem = unitSystem,
                        isExpanded = isExpanded,
                        onToggle   = { expandedStates[ride.id] = !isExpanded }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text  = "RIDES",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
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
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

    var yearExpanded  by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var weekExpanded  by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Period:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.tertiary)

        // Year
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { yearExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = selectedYear.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                availableYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = { onYearSelected(year); yearExpanded = false }
                    )
                }
            }
        }

        // Month
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { monthExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = monthNames.getOrElse(selectedMonth - 1) { selectedMonth.toString() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                availableMonths.forEach { month ->
                    DropdownMenuItem(
                        text = { Text(monthNames.getOrElse(month - 1) { month.toString() }) },
                        onClick = { onMonthSelected(month); monthExpanded = false }
                    )
                }
            }
        }

        // Week
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { weekExpanded = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = "Week $selectedWeek",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
            DropdownMenu(expanded = weekExpanded, onDismissRequest = { weekExpanded = false }) {
                (1..5).forEach { week ->
                    DropdownMenuItem(
                        text = { Text("Week $week") },
                        onClick = { onWeekSelected(week); weekExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No rides this week", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.tertiary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Import a GPX file from Garmin to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun RideCard(
    ride: Activity,
    unitSystem: UnitSystem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val gpsPoints = remember(ride.id) {
        ride.trackpoints.filter { it.latitude != null && it.longitude != null }
    }
    val hasRoute = gpsPoints.size >= 2

    // Entire card is clickable — no arrow indicator, same pattern as History
    Surface(
        shape   = RoundedCornerShape(16.dp),
        color   = MaterialTheme.colorScheme.surface,
        onClick = { if (hasRoute) onToggle() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: name + date
            Text(
                text     = ride.name,
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = formatDate(ride.startTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Collapsible route section
            AnimatedVisibility(
                visible = isExpanded && hasRoute,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            RoutePathCanvas(
                                trackpoints = gpsPoints,
                                modifier    = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(12.dp)
                            )
                            SpeedZoneLegend(
                                trackpoints = gpsPoints,
                                modifier    = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val distFormatted = UnitConversion.formatDistance(ride.totalDistance / 1000.0, unitSystem)
                StatChip("DIST",  distFormatted.value, distFormatted.unit, Modifier.weight(1f))
                StatChip("TIME",  formatDuration(ride.totalDuration), "", Modifier.weight(1f))
                val speedMs = if (ride.totalDuration > 0) ride.totalDistance / ride.totalDuration else 0.0
                val speedFormatted = UnitConversion.formatSpeed(speedMs, unitSystem)
                StatChip("SPEED", speedFormatted.value, speedFormatted.unit, Modifier.weight(1f))
                ride.averageHeartRate?.let { hr ->
                    StatChip("HR", hr.toString(), "bpm", Modifier.weight(1f))
                }
            }

            if ((ride.totalElevationGain ?: 0.0) > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = "↑ ${ride.totalElevationGain!!.toInt()}m gain",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun RoutePathCanvas(trackpoints: List<Trackpoint>, modifier: Modifier = Modifier) {
    val speedValues = remember(trackpoints) { trackpoints.mapNotNull { it.speed?.takeIf { s -> s > 0.1 } } }
    val minSpeed    = remember(speedValues) { speedValues.minOrNull() ?: 0.0 }
    val maxSpeed    = remember(speedValues) { speedValues.maxOrNull() ?: 1.0 }
    val speedRange  = (maxSpeed - minSpeed).coerceAtLeast(0.1)

    Canvas(modifier = modifier) {
        val lats = trackpoints.mapNotNull { it.latitude }
        val lons = trackpoints.mapNotNull { it.longitude }
        if (lats.size < 2) return@Canvas

        val minLat = lats.min(); val maxLat = lats.max()
        val minLon = lons.min(); val maxLon = lons.max()

        val avgLat        = (minLat + maxLat) / 2.0
        val mPerDegreeLat = 111_000.0
        val mPerDegreeLon = 111_000.0 * cos(Math.toRadians(avgLat))
        val latMeters     = (maxLat - minLat).coerceAtLeast(0.0001) * mPerDegreeLat
        val lonMeters     = (maxLon - minLon).coerceAtLeast(0.0001) * mPerDegreeLon
        val scale         = min(size.width / lonMeters, size.height / latMeters)
        val offsetX       = (size.width  - lonMeters * scale) / 2f
        val offsetY       = (size.height - latMeters * scale) / 2f

        fun toX(lon: Double) = ((lon - minLon) * mPerDegreeLon * scale + offsetX).toFloat()
        fun toY(lat: Double) = (size.height - ((lat - minLat) * mPerDegreeLat * scale + offsetY)).toFloat()

        val valid = trackpoints.filter { it.latitude != null && it.longitude != null }
        for (i in 1 until valid.size) {
            val prev     = valid[i - 1]
            val curr     = valid[i]
            val fraction = ((( curr.speed ?: 0.0) - minSpeed) / speedRange).toFloat().coerceIn(0f, 1f)
            drawLine(
                color       = speedFractionToColor(fraction),
                start       = Offset(toX(prev.longitude!!), toY(prev.latitude!!)),
                end         = Offset(toX(curr.longitude!!), toY(curr.latitude!!)),
                strokeWidth = 4f,
                cap         = StrokeCap.Round
            )
        }

        val first = valid.first(); val last = valid.last()
        drawCircle(Color(0xFF4CAF50), 7f, Offset(toX(first.longitude!!), toY(first.latitude!!)))
        drawCircle(Color(0xFFF44336), 7f, Offset(toX(last.longitude!!),  toY(last.latitude!!)))
    }
}

@Composable
private fun SpeedZoneLegend(trackpoints: List<Trackpoint>, modifier: Modifier = Modifier) {
    val speedValues = remember(trackpoints) { trackpoints.mapNotNull { it.speed?.takeIf { s -> s > 0.1 } } }
    if (speedValues.isEmpty()) return
    val minKmh = (speedValues.min() * 3.6).toInt()
    val maxKmh = (speedValues.max() * 3.6).toInt()

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
            val steps = 100; val segW = size.width / steps
            for (i in 0 until steps) {
                drawRect(
                    color   = speedFractionToColor(i.toFloat() / steps),
                    topLeft = Offset(i * segW, 0f),
                    size    = Size(segW + 1f, size.height)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${minKmh} km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            Text("SPEED",         style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            Text("${maxKmh} km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall,  color = MaterialTheme.colorScheme.tertiary)
        Text(value, style = MaterialTheme.typography.titleLarge,  color = MaterialTheme.colorScheme.onBackground)
        if (unit.isNotEmpty()) Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
    }
}

private fun formatDate(startTime: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(startTime) ?: return startTime
        SimpleDateFormat("EEE, MMM d · HH:mm", Locale.US).format(date)
    } catch (e: Exception) { startTime }
}

private fun formatDuration(seconds: Double): String {
    val t = seconds.toLong()
    val h = t / 3600; val m = (t % 3600) / 60; val s = t % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
