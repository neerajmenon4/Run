package com.kwyr.runnerplanner.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kwyr.runnerplanner.data.model.Split
import com.kwyr.runnerplanner.data.model.Trackpoint

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTrackpointList(value: List<Trackpoint>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTrackpointList(value: String): List<Trackpoint> {
        val type = object : TypeToken<List<Trackpoint>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromSplitList(value: List<Split>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSplitList(value: String): List<Split> {
        val type = object : TypeToken<List<Split>>() {}.type
        return gson.fromJson(value, type)
    }
}
