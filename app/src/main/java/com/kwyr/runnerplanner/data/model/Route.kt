package com.kwyr.runnerplanner.data.model

typealias Coordinate = Pair<Double, Double>

data class SavedRouteMeta(
    val id: String,
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val targetTimeSec: Int? = null,
    val targetPaceSecPerKm: Int? = null
)

data class Route(
    val id: String,
    val name: String,
    val coordinates: List<Coordinate>,
    val distanceMeters: Double,
    val createdAt: String,
    val updatedAt: String,
    val targetTimeSec: Int? = null,
    val targetPaceSecPerKm: Int? = null
)
