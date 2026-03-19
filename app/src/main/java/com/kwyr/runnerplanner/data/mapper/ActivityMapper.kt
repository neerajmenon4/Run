package com.kwyr.runnerplanner.data.mapper

import com.kwyr.runnerplanner.data.local.entity.ActivityEntity
import com.kwyr.runnerplanner.data.model.Activity
import java.text.SimpleDateFormat
import java.util.*

object ActivityMapper {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    
    fun toEntity(activity: Activity): ActivityEntity {
        val startDate = dateFormat.parse(activity.startTime) ?: Date()
        val calendar = Calendar.getInstance().apply {
            time = startDate
            firstDayOfWeek = Calendar.MONDAY
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-indexed
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Calculate week of month (1-5)
        val weekOfMonth = ((dayOfMonth - 1) / 7) + 1
        
        // Convert Sunday=1 to Monday=1 format
        val adjustedDayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1
        
        return ActivityEntity(
            id = activity.id,
            name = activity.name,
            type = activity.type,
            startTime = activity.startTime,
            endTime = activity.endTime,
            totalDistance = activity.totalDistance,
            totalDuration = activity.totalDuration,
            totalCalories = activity.totalCalories,
            averageHeartRate = activity.averageHeartRate,
            maxHeartRate = activity.maxHeartRate,
            minHeartRate = activity.minHeartRate,
            averageCadence = activity.averageCadence,
            maxCadence = activity.maxCadence,
            totalElevationGain = activity.totalElevationGain,
            totalElevationLoss = activity.totalElevationLoss,
            averagePace = activity.averagePace,
            maxPace = activity.maxPace,
            minPace = activity.minPace,
            trackpoints = activity.trackpoints,
            splits = activity.splits,
            createdAt = activity.createdAt,
            updatedAt = activity.updatedAt,
            year = year,
            month = month,
            weekOfMonth = weekOfMonth,
            dayOfWeek = adjustedDayOfWeek,
            timestamp = startDate.time
        )
    }
    
    fun fromEntity(entity: ActivityEntity): Activity {
        return Activity(
            id = entity.id,
            name = entity.name,
            type = entity.type,
            startTime = entity.startTime,
            endTime = entity.endTime,
            totalDistance = entity.totalDistance,
            totalDuration = entity.totalDuration,
            totalCalories = entity.totalCalories,
            averageHeartRate = entity.averageHeartRate,
            maxHeartRate = entity.maxHeartRate,
            minHeartRate = entity.minHeartRate,
            averageCadence = entity.averageCadence,
            maxCadence = entity.maxCadence,
            totalElevationGain = entity.totalElevationGain,
            totalElevationLoss = entity.totalElevationLoss,
            averagePace = entity.averagePace,
            maxPace = entity.maxPace,
            minPace = entity.minPace,
            trackpoints = entity.trackpoints,
            splits = entity.splits,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    fun fromEntityList(entities: List<ActivityEntity>): List<Activity> {
        return entities.map { fromEntity(it) }
    }
    
    fun toEntityList(activities: List<Activity>): List<ActivityEntity> {
        return activities.map { toEntity(it) }
    }
}
