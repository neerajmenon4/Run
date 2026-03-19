package com.kwyr.runnerplanner.data.model

import com.google.gson.annotations.SerializedName

sealed class RunConfiguration {
    abstract val notes: String
}

data class EasyRunConfig(
    val durationType: DurationType = DurationType.DURATION,
    val duration: Int? = null, // minutes
    val distance: Double? = null, // km
    val effort: String = "Easy", // Easy, Moderate, RPE 1-10
    val paceRangeMin: String? = null, // "5:30"
    val paceRangeMax: String? = null, // "6:00"
    override val notes: String = ""
) : RunConfiguration()

data class TempoRunConfig(
    val warmupDuration: Int? = null, // minutes
    val tempoDuration: Int = 20, // minutes - required
    val tempoType: TempoType = TempoType.EFFORT,
    val tempoEffort: String = "Hard", // Comfortably Hard, Threshold
    val tempoPace: String? = null, // "4:30/km"
    val cooldownDuration: Int? = null, // minutes
    override val notes: String = ""
) : RunConfiguration()

data class IntervalsConfig(
    val warmupDuration: Int? = null, // minutes
    val reps: Int = 6,
    val workType: IntervalType = IntervalType.DISTANCE,
    val workValue: String = "400", // "400" for meters or "90" for seconds
    val workPace: String = "4:00/km",
    val restType: IntervalType = IntervalType.TIME,
    val restValue: String = "90", // seconds or meters
    val cooldownDuration: Int? = null, // minutes
    override val notes: String = ""
) : RunConfiguration()

data class LongRunConfig(
    val durationType: DurationType = DurationType.DURATION,
    val duration: Int? = null, // minutes
    val distance: Double? = null, // km
    val paceType: PaceType = PaceType.EFFORT,
    val pace: String? = null, // "5:30/km"
    val effort: String = "Easy",
    val progression: ProgressionType = ProgressionType.STEADY,
    override val notes: String = ""
) : RunConfiguration()

data class WorkoutConfig(
    val workoutType: WorkoutType = WorkoutType.STRENGTH,
    val duration: Int = 30, // minutes
    val intensity: IntensityLevel = IntensityLevel.MEDIUM,
    override val notes: String = ""
) : RunConfiguration()

data class RestConfig(
    val restType: RestType = RestType.FULL_REST,
    override val notes: String = ""
) : RunConfiguration()

data class RaceConfig(
    val raceName: String = "",
    val distance: Double = 5.0, // km
    val goalType: RaceGoalType = RaceGoalType.FINISH,
    val goalValue: String? = null, // time "20:00" or pace "4:00/km"
    override val notes: String = ""
) : RunConfiguration()

// Enums for configuration options
enum class DurationType {
    @SerializedName("duration") DURATION,
    @SerializedName("distance") DISTANCE
}

enum class TempoType {
    @SerializedName("effort") EFFORT,
    @SerializedName("pace") PACE
}

enum class IntervalType {
    @SerializedName("time") TIME,
    @SerializedName("distance") DISTANCE
}

enum class PaceType {
    @SerializedName("effort") EFFORT,
    @SerializedName("pace") PACE
}

enum class ProgressionType {
    @SerializedName("steady") STEADY,
    @SerializedName("negative_split") NEGATIVE_SPLIT,
    @SerializedName("progressive") PROGRESSIVE
}

enum class WorkoutType {
    @SerializedName("strength") STRENGTH,
    @SerializedName("cycling") CYCLING,
    @SerializedName("mobility") MOBILITY,
    @SerializedName("swimming") SWIMMING,
    @SerializedName("other") OTHER
}

enum class IntensityLevel {
    @SerializedName("low") LOW,
    @SerializedName("medium") MEDIUM,
    @SerializedName("high") HIGH
}

enum class RestType {
    @SerializedName("full_rest") FULL_REST,
    @SerializedName("active_recovery") ACTIVE_RECOVERY
}

enum class RaceGoalType {
    @SerializedName("finish") FINISH,
    @SerializedName("time") TIME,
    @SerializedName("pace") PACE
}
