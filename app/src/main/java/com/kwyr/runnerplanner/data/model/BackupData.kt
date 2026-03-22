package com.kwyr.runnerplanner.data.model

data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val appVersion: String,
    val routes: List<Route>,
    val activities: List<Activity>,
    val userProfile: UserProfile,
    val theme: String,
    val trainingPlan: TrainingPlan?,
    val weekPlans: Map<String, WeekPlan>
)
