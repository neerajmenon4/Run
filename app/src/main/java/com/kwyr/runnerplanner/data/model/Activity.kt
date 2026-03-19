package com.kwyr.runnerplanner.data.model

data class Trackpoint(
    val time: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val distance: Double,
    val heartRate: Int? = null,
    val speed: Double? = null,
    val cadence: Int? = null
)

data class Split(
    val splitNumber: Int,
    val distance: Double,
    val duration: Double,
    val pace: Double,
    val speed: Double,
    val averageHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val minHeartRate: Int? = null,
    val averageCadence: Int? = null,
    val elevationGain: Double? = null,
    val elevationLoss: Double? = null
)

data class Activity(
    val id: String,
    val name: String,
    val type: String,
    val startTime: String,
    val endTime: String,
    val totalDistance: Double,
    val totalDuration: Double,
    val totalCalories: Int? = null,
    val averageHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val minHeartRate: Int? = null,
    val averageCadence: Int? = null,
    val maxCadence: Int? = null,
    val totalElevationGain: Double? = null,
    val totalElevationLoss: Double? = null,
    val averagePace: Double? = null,
    val maxPace: Double? = null,
    val minPace: Double? = null,
    val trackpoints: List<Trackpoint>,
    val splits: List<Split>,
    val createdAt: String,
    val updatedAt: String
)
