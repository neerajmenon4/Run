package com.kwyr.runnerplanner.ui.screens.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kwyr.runnerplanner.R
import com.kwyr.runnerplanner.ui.components.SettingsIcon
import com.kwyr.runnerplanner.ui.components.UploadIcon
import com.kwyr.runnerplanner.util.UnitConversion
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onNavigateToImport: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessNotification by remember { mutableStateOf(false) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            try {
                val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                } ?: uri.lastPathSegment ?: "file.tcx"
                
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    java.io.BufferedReader(java.io.InputStreamReader(inputStream)).use { it.readText() }
                } ?: ""
                
                viewModel.parseFile(uri, content, fileName)
            } catch (e: Exception) {
                viewModel.parseFile(uri, "", "error.txt")
            }
        }
    }
    
    LaunchedEffect(importState) {
        when (importState) {
            is com.kwyr.runnerplanner.ui.screens.import_gpx.ImportState.Success -> {
                showSuccessNotification = true
                viewModel.resetImportState()
            }
            is com.kwyr.runnerplanner.ui.screens.import_gpx.ImportState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (importState as com.kwyr.runnerplanner.ui.screens.import_gpx.ImportState.Error).message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetImportState()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 100.dp)
        ) {
        Header(userName = uiState.userName)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        TimeSection()
        
        Spacer(modifier = Modifier.height(40.dp))
        
        StatsRow(
            totalDistance = uiState.totalDistance,
            avgSpeed = uiState.avgSpeed,
            unitSystem = uiState.unitSystem
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        SpeedChart(
            speedData = uiState.speedData,
            timePeriod = uiState.timePeriod,
            unitSystem = uiState.unitSystem,
            onTimePeriodChange = { viewModel.setTimePeriod(it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ImportButton(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(android.content.Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                filePickerLauncher.launch(intent)
            },
            isLoading = importState is com.kwyr.runnerplanner.ui.screens.import_gpx.ImportState.Loading
        )
        }
        
        // Success notification
        androidx.compose.animation.AnimatedVisibility(
            visible = showSuccessNotification,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            SuccessNotification(
                onDismiss = { showSuccessNotification = false },
                onGoToActivities = {
                    showSuccessNotification = false
                    onNavigateToHistory()
                }
            )
        }
    }
}

@Composable
private fun Header(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.greeting_hi),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                color = androidx.compose.ui.graphics.Color(0xFFFF6B35)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.greeting_run),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun TimeSection() {
    val currentDate = remember {
        SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())
    }
    
    Column {
        Text(
            text = currentDate,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ready_to_push),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StatsRow(
    totalDistance: Double,
    avgSpeed: Double,
    unitSystem: com.kwyr.runnerplanner.data.model.UnitSystem
) {
    val distanceFormatted = UnitConversion.formatDistance(totalDistance / 1000.0, unitSystem)
    val speedFormatted = UnitConversion.formatSpeed(avgSpeed, unitSystem)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = stringResource(R.string.total_distance),
            value = distanceFormatted.value,
            unit = distanceFormatted.unit,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.avg_speed),
            value = speedFormatted.value,
            unit = speedFormatted.unit,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun SpeedChart(
    speedData: List<SpeedDataPoint>,
    timePeriod: TimePeriod,
    unitSystem: com.kwyr.runnerplanner.data.model.UnitSystem,
    onTimePeriodChange: (TimePeriod) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.speed_improvement),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimePeriod.values().forEach { period ->
                    PeriodButton(
                        label = when (period) {
                            TimePeriod.ONE_WEEK -> stringResource(R.string.period_1week)
                            TimePeriod.ONE_MONTH -> stringResource(R.string.period_1month)
                            TimePeriod.THREE_MONTHS -> stringResource(R.string.period_3months)
                        },
                        selected = timePeriod == period,
                        onClick = { onTimePeriodChange(period) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
            ) {
                if (speedData.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_data_period),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    SpeedLineChart(speedData = speedData, unitSystem = unitSystem)
                }
            }
        }
    }
}

@Composable
private fun SpeedLineChart(
    speedData: List<SpeedDataPoint>,
    unitSystem: com.kwyr.runnerplanner.data.model.UnitSystem
) {
    var chartWidth by remember { mutableStateOf(0f) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                chartWidth = coordinates.size.width.toFloat()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        if (speedData.isNotEmpty() && chartWidth > 0) {
                            val percentage = (offset.x / chartWidth).coerceIn(0f, 1f)
                            val index = (percentage * (speedData.size - 1)).toInt()
                            hoveredIndex = index
                            tryAwaitRelease()
                            hoveredIndex = null
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (speedData.isEmpty()) return@Canvas
            
            val maxSpeed = speedData.maxOf { it.speed }
            val width = size.width
            val height = size.height
            
            val path = Path().apply {
                speedData.forEachIndexed { index, point ->
                    val x = (index.toFloat() / (speedData.size - 1).coerceAtLeast(1)) * width
                    val y = height - (point.speed / maxSpeed * height).toFloat()
                    
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
            
            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color(0xFFFF8C42),
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            
            speedData.forEachIndexed { index, point ->
                val x = (index.toFloat() / (speedData.size - 1).coerceAtLeast(1)) * width
                val y = height - (point.speed / maxSpeed * height).toFloat()
                
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFFFF8C42),
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun PeriodButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SuccessNotification(
    onDismiss: () -> Unit,
    onGoToActivities: () -> Unit
) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity uploaded",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun ImportButton(onClick: () -> Unit, isLoading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UploadIcon(color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    text = stringResource(R.string.import_from_garmin),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
