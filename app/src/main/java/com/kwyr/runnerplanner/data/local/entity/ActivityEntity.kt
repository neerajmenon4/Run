package com.kwyr.runnerplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kwyr.runnerplanner.data.local.converter.Converters
import com.kwyr.runnerplanner.data.model.Split
import com.kwyr.runnerplanner.data.model.Trackpoint

@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class ActivityEntity(
    @PrimaryKey
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
    val updatedAt: String,
    
    // Date organization fields
    val year: Int,
    val month: Int, // 1-12
    val weekOfMonth: Int, // 1-5
    val dayOfWeek: Int, // 1-7 (Monday = 1)
    val timestamp: Long // Unix timestamp for sorting
)
