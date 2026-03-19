package com.kwyr.runnerplanner.data.local.dao

import androidx.room.*
import com.kwyr.runnerplanner.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    
    // Get all activities
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    // Get activities by year
    @Query("SELECT * FROM activities WHERE year = :year ORDER BY timestamp DESC")
    fun getActivitiesByYear(year: Int): Flow<List<ActivityEntity>>
    
    // Get activities by year and month
    @Query("SELECT * FROM activities WHERE year = :year AND month = :month ORDER BY timestamp DESC")
    fun getActivitiesByMonth(year: Int, month: Int): Flow<List<ActivityEntity>>
    
    // Get activities by year, month, and week
    @Query("SELECT * FROM activities WHERE year = :year AND month = :month AND weekOfMonth = :week ORDER BY timestamp DESC")
    fun getActivitiesByWeek(year: Int, month: Int, week: Int): Flow<List<ActivityEntity>>
    
    // Get activity by ID
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: String): ActivityEntity?
    
    // Get distinct years (for year picker)
    @Query("SELECT DISTINCT year FROM activities ORDER BY year DESC")
    fun getDistinctYears(): Flow<List<Int>>
    
    // Get distinct months for a year (for month picker)
    @Query("SELECT DISTINCT month FROM activities WHERE year = :year ORDER BY month DESC")
    fun getDistinctMonthsForYear(year: Int): Flow<List<Int>>
    
    // Get week count for a specific month (for week picker)
    @Query("SELECT MAX(weekOfMonth) FROM activities WHERE year = :year AND month = :month")
    suspend fun getWeekCountForMonth(year: Int, month: Int): Int?
    
    // Insert activity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)
    
    // Insert multiple activities
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)
    
    // Update activity
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    // Delete activity
    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)
    
    // Delete all activities
    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
    
    // Get total count
    @Query("SELECT COUNT(*) FROM activities")
    fun getActivityCount(): Flow<Int>
    
    // Get total distance for a week
    @Query("SELECT SUM(totalDistance) FROM activities WHERE year = :year AND month = :month AND weekOfMonth = :week")
    suspend fun getTotalDistanceForWeek(year: Int, month: Int, week: Int): Double?
    
    // Get total duration for a week
    @Query("SELECT SUM(totalDuration) FROM activities WHERE year = :year AND month = :month AND weekOfMonth = :week")
    suspend fun getTotalDurationForWeek(year: Int, month: Int, week: Int): Double?
}
