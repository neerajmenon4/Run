package com.kwyr.runnerplanner.util

import com.kwyr.runnerplanner.data.model.*
import kotlin.math.round

data class PaceRange(
    val minSecPerKm: Int,
    val maxSecPerKm: Int
)

data class TrainingPaces(
    val easy: PaceRange,
    val long: PaceRange,
    val tempo: PaceRange,
    val intervals: PaceRange,
    val racePace: Int
)

data class GeneratedWeekPlan(
    val weekNumber: Int,
    val phase: String,
    val totalVolumeMeters: Int,
    val days: List<DayPlan>,
    val isCutbackWeek: Boolean
)

object TrainingPlanGenerator {
    
    fun generatePlan(goalDistanceMeters: Int, goalTimeSeconds: Int): List<GeneratedWeekPlan> {
        val racePaceSecPerKm = (goalTimeSeconds / (goalDistanceMeters / 1000.0)).toInt()
        val paces = calculatePaces(racePaceSecPerKm)
        
        // Calculate appropriate weekly volume based on race distance
        val weeklyVolumeMeters = when {
            goalDistanceMeters <= 5000 -> 20000  // 20km/week for 5K
            goalDistanceMeters <= 10000 -> 35000 // 35km/week for 10K
            goalDistanceMeters <= 21097 -> 50000 // 50km/week for half
            else -> 70000 // 70km/week for marathon
        }
        
        // Generate a single balanced week with all workout types
        val workouts = generateBalancedWeek(
            weeklyVolumeMeters,
            goalDistanceMeters,
            paces
        )
        
        // Return single week that will be used repeatedly
        return listOf(
            GeneratedWeekPlan(
                weekNumber = 1,
                phase = "training",
                totalVolumeMeters = weeklyVolumeMeters,
                days = workouts,
                isCutbackWeek = false
            )
        )
    }
    
    private fun calculatePaces(racePaceSecPerKm: Int): TrainingPaces {
        return TrainingPaces(
            easy = PaceRange(
                minSecPerKm = round(racePaceSecPerKm * 1.25).toInt(),  // Pg × 1.25
                maxSecPerKm = round(racePaceSecPerKm * 1.30).toInt()
            ),
            long = PaceRange(
                minSecPerKm = round(racePaceSecPerKm * 1.15).toInt(),  // Pg × 1.15
                maxSecPerKm = round(racePaceSecPerKm * 1.20).toInt()
            ),
            tempo = PaceRange(
                minSecPerKm = round(racePaceSecPerKm * 1.05).toInt(),  // Pg × 1.05
                maxSecPerKm = round(racePaceSecPerKm * 1.08).toInt()
            ),
            intervals = PaceRange(
                minSecPerKm = round(racePaceSecPerKm * 0.95).toInt(),  // Pg × 0.95
                maxSecPerKm = round(racePaceSecPerKm * 0.98).toInt()
            ),
            racePace = racePaceSecPerKm
        )
    }
    
    private fun generateBalancedWeek(
        weeklyVolumeMeters: Int,
        goalDistanceMeters: Int,
        paces: TrainingPaces
    ): List<DayPlan> {
        // 7-Day Adaptive Framework using Volume-Based Scaling
        // Monday: RACE PRACTICE
        // Tuesday: Intervals (Vi = 1.0 × Dg)
        // Wednesday: Rest
        // Thursday: Threshold Tempo (Tt = (Dg × 4) + 5 minutes)
        // Friday: Easy Recovery (0.5 × Dg)
        // Saturday: Long Run (1.3 × Dg)
        // Sunday: Rest
        
        val goalDistanceKm = goalDistanceMeters / 1000.0
        
        // Volume-based calculations
        val intervalTotalMeters = goalDistanceMeters  // Vi = 1.0 × Dg
        val tempoDurationMinutes = ((goalDistanceKm * 4) + 5).toInt()  // Tt = (Dg × 4) + 5
        val easyRunMeters = (goalDistanceMeters * 0.5).toInt()  // 0.5 × Dg
        val longRunMeters = (goalDistanceMeters * 1.3).toInt()  // 1.3 × Dg
        
        return listOf(
            // Monday: RACE PRACTICE
            createRacePractice(0, goalDistanceMeters, paces.racePace),
            
            // Tuesday: Intervals (Vi = 1.0 × Dg at Pg × 0.95)
            createAdaptiveIntervals(1, goalDistanceMeters, intervalTotalMeters, paces),
            
            // Wednesday: Rest
            DayPlan(dayOfWeek = 2, runType = RunType.REST),
            
            // Thursday: Threshold Tempo (Tt = (Dg × 4) + 5 minutes at Pg × 1.05)
            createTempoRunByDuration(3, tempoDurationMinutes, paces.tempo),
            
            // Friday: Easy Recovery (0.5 × Dg at Pg × 1.25)
            createEasyRun(4, easyRunMeters, paces.easy),
            
            // Saturday: Long Run (1.3 × Dg at Pg × 1.15)
            createLongRun(5, longRunMeters, paces.long),
            
            // Sunday: Rest
            DayPlan(dayOfWeek = 6, runType = RunType.REST)
        )
    }
    
    private fun createEasyRun(dayOfWeek: Int, distanceMeters: Int, paceRange: PaceRange): DayPlan {
        val distanceKm = distanceMeters / 1000.0
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.EASY,
            targetDistance = distanceKm,
            configuration = EasyRunConfig(
                distance = distanceKm,
                durationType = DurationType.DISTANCE,
                effort = "Easy",
                paceRangeMin = formatPace(paceRange.minSecPerKm),
                paceRangeMax = formatPace(paceRange.maxSecPerKm),
                notes = ""
            )
        )
    }
    
    private fun createLongRun(dayOfWeek: Int, distanceMeters: Int, paceRange: PaceRange): DayPlan {
        val distanceKm = distanceMeters / 1000.0
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.LONG,
            targetDistance = distanceKm,
            configuration = LongRunConfig(
                distance = distanceKm,
                durationType = DurationType.DISTANCE,
                paceType = PaceType.EFFORT,
                effort = "Easy-Moderate",
                progression = ProgressionType.STEADY,
                notes = "Long run at conversational pace"
            )
        )
    }
    
    private fun createTempoRunByDuration(dayOfWeek: Int, durationMinutes: Int, paceRange: PaceRange): DayPlan {
        // Calculate distance based on tempo pace and duration
        val avgPaceSecPerKm = (paceRange.minSecPerKm + paceRange.maxSecPerKm) / 2
        val distanceKm = (durationMinutes * 60.0) / avgPaceSecPerKm
        
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.TEMPO,
            targetDistance = distanceKm,
            configuration = TempoRunConfig(
                warmupDuration = 10,
                tempoDuration = durationMinutes,
                tempoType = TempoType.PACE,
                tempoPace = formatPace(avgPaceSecPerKm),
                cooldownDuration = 10,
                notes = "Comfortably hard effort"
            )
        )
    }
    
    private fun createIntervalRun(
        dayOfWeek: Int,
        goalDistanceMeters: Int,
        totalIntervalDistanceMeters: Int,
        paceRange: PaceRange
    ): DayPlan {
        // Always 8x400m with 90s rest
        val repDistance = 400
        val reps = 8
        
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.SPEED,
            targetDistance = (repDistance * reps) / 1000.0,
            configuration = IntervalsConfig(
                warmupDuration = 15,
                reps = reps,
                workType = IntervalType.DISTANCE,
                workValue = repDistance.toString(),
                workPace = formatPace(paceRange.maxSecPerKm),
                restType = IntervalType.TIME,
                restValue = "90",
                cooldownDuration = 10,
                notes = "Fast intervals with recovery"
            )
        )
    }
    
    private fun createWorkout(dayOfWeek: Int): DayPlan {
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.WORKOUT,
            targetDistance = null,
            configuration = WorkoutConfig(
                workoutType = WorkoutType.STRENGTH,
                duration = 30,
                intensity = IntensityLevel.MEDIUM,
                notes = "Cross-training or strength work"
            )
        )
    }
    
    private fun createRacePractice(dayOfWeek: Int, distanceMeters: Int, racePace: Int): DayPlan {
        val distanceKm = distanceMeters / 1000.0
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.RACE_PRACTICE,
            targetDistance = distanceKm,
            configuration = TempoRunConfig(
                warmupDuration = 10,
                tempoDuration = round((distanceMeters / 1000.0) * (racePace / 60.0)).toInt(),
                tempoType = TempoType.PACE,
                tempoPace = formatPace(racePace),
                cooldownDuration = 10,
                notes = "Race pace practice at goal pace"
            )
        )
    }
    
    private fun createAdaptiveIntervals(
        dayOfWeek: Int,
        goalDistanceMeters: Int,
        totalIntervalDistanceMeters: Int,
        paces: TrainingPaces
    ): DayPlan {
        // Adaptive interval rep selection based on goal distance
        // If Dg < 5km: use 400m reps
        // If 5km ≤ Dg ≤ 10km: use 800m reps
        // If Dg > 10km: use 1600m reps
        
        val (repDistance, reps) = when {
            goalDistanceMeters < 5000 -> {
                // 400m reps for 5K and under
                val reps = (totalIntervalDistanceMeters / 400.0).toInt().coerceAtLeast(8)
                Pair(400, reps)
            }
            goalDistanceMeters <= 10000 -> {
                // 800m reps for 5K-10K
                val reps = (totalIntervalDistanceMeters / 800.0).toInt().coerceAtLeast(6)
                Pair(800, reps)
            }
            else -> {
                // 1600m reps for half marathon and beyond
                val reps = (totalIntervalDistanceMeters / 1600.0).toInt().coerceAtLeast(4)
                Pair(1600, reps)
            }
        }
        
        // Pace at Pg × 0.95 (5% faster than race pace)
        val intervalPace = round(paces.racePace * 0.95).toInt()
        
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.SPEED,
            targetDistance = (repDistance * reps) / 1000.0,
            configuration = IntervalsConfig(
                warmupDuration = 15,
                reps = reps,
                workType = IntervalType.DISTANCE,
                workValue = repDistance.toString(),
                workPace = formatPace(intervalPace),
                restType = IntervalType.TIME,
                restValue = "90",
                cooldownDuration = 10,
                notes = "Fast intervals at 95% of race pace"
            )
        )
    }
    
    private fun createRacePaceRun(dayOfWeek: Int, distanceMeters: Int, racePace: Int): DayPlan {
        val distanceKm = distanceMeters / 1000.0
        return DayPlan(
            dayOfWeek = dayOfWeek,
            runType = RunType.TEMPO,
            targetDistance = distanceKm,
            configuration = TempoRunConfig(
                warmupDuration = 10,
                tempoDuration = round((distanceMeters / 1000.0) * (racePace / 60.0)).toInt(),
                tempoType = TempoType.PACE,
                tempoPace = formatPace(racePace),
                cooldownDuration = 10,
                notes = "Race pace practice"
            )
        )
    }
    
    private fun formatPace(secPerKm: Int): String {
        val minutes = secPerKm / 60
        val seconds = secPerKm % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
