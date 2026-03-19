package com.kwyr.runnerplanner.data.model

data class TrainingPlan(
    val id: String = System.currentTimeMillis().toString(),
    val goalName: String = "",
    val goalType: GoalType = GoalType.CUSTOM,
    val targetDate: String? = null,
    val goalDistanceMeters: Int = 0,
    val goalTimeSeconds: Int = 0,
    val weeklyVolume: Double = 0.0,
    val currentWeekStart: String = "",
    val createdAt: String = System.currentTimeMillis().toString(),
    val updatedAt: String = System.currentTimeMillis().toString()
)

data class WeekPlan(
    val weekStart: String,
    val days: List<DayPlan> = List(7) { DayPlan(dayOfWeek = it) }
)

data class DayPlan(
    val dayOfWeek: Int,
    val runType: RunType = RunType.REST,
    val configuration: RunConfiguration? = null,
    val completed: Boolean = false,
    val linkedActivityId: String? = null,
    // Legacy fields for backward compatibility - will be removed
    @Deprecated("Use configuration instead")
    val targetDistance: Double? = null,
    @Deprecated("Use configuration instead")
    val targetDuration: Double? = null,
    @Deprecated("Use configuration instead")
    val targetPace: Double? = null,
    @Deprecated("Use configuration instead")
    val notes: String = ""
)

enum class RunType(val displayName: String, val emoji: String, val colorHex: String) {
    EASY("Easy Run", "🏃‍♂️", "#4CAF50"),
    TEMPO("Tempo Run", "⚡", "#FF9800"),
    SPEED("Speed/Intervals", "🚀", "#F44336"),
    LONG("Long Run", "🛣️", "#2196F3"),
    WORKOUT("Workout", "💪", "#9C27B0"),
    REST("Rest Day", "🧘", "#9E9E9E"),
    RACE("Race Day", "🏁", "#FF6B35"),
    RACE_PRACTICE("Race Practice", "🏁", "#FF6B35")
}

enum class GoalType(val displayName: String) {
    RACE_5K("5K Race"),
    RACE_7K("7K Race"),
    RACE_10K("10K Race"),
    RACE_13K("13K Race"),
    RACE_15K("15K Race"),
    RACE_17K("17K Race"),
    RACE_19K("19K Race"),
    RACE_HALF("Half Marathon"),
    RACE_25K("25K Race"),
    RACE_27K("27K Race"),
    RACE_30K("30K Race"),
    RACE_33K("33K Race"),
    RACE_35K("35K Race"),
    RACE_37K("37K Race"),
    RACE_MARATHON("Marathon"),
    DISTANCE_MILESTONE("Distance Goal"),
    CONSISTENCY("Consistency Goal"),
    SPEED_IMPROVEMENT("Speed Goal"),
    CUSTOM("Custom Goal")
}
