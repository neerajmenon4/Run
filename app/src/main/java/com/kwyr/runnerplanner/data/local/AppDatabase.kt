package com.kwyr.runnerplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kwyr.runnerplanner.data.local.converter.Converters
import com.kwyr.runnerplanner.data.local.dao.ActivityDao
import com.kwyr.runnerplanner.data.local.entity.ActivityEntity

@Database(
    entities = [ActivityEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}
