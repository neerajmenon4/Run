package com.kwyr.runnerplanner.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.kwyr.runnerplanner.data.local.AppDatabase
import com.kwyr.runnerplanner.data.local.dao.ActivityDao
import com.kwyr.runnerplanner.data.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

class RunConfigurationAdapter : TypeAdapter<RunConfiguration>() {
    override fun write(out: JsonWriter, value: RunConfiguration?) {
        if (value == null) {
            out.nullValue()
            return
        }
        
        out.beginObject()
        out.name("type")
        when (value) {
            is EasyRunConfig -> {
                out.value("easy")
                out.name("data")
                out.beginObject()
                out.name("durationType").value(value.durationType.name)
                out.name("duration").value(value.duration)
                out.name("distance").value(value.distance)
                out.name("effort").value(value.effort)
                out.name("paceRangeMin").value(value.paceRangeMin)
                out.name("paceRangeMax").value(value.paceRangeMax)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is TempoRunConfig -> {
                out.value("tempo")
                out.name("data")
                out.beginObject()
                out.name("warmupDuration").value(value.warmupDuration)
                out.name("tempoDuration").value(value.tempoDuration)
                out.name("tempoType").value(value.tempoType.name)
                out.name("tempoEffort").value(value.tempoEffort)
                out.name("tempoPace").value(value.tempoPace)
                out.name("cooldownDuration").value(value.cooldownDuration)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is IntervalsConfig -> {
                out.value("intervals")
                out.name("data")
                out.beginObject()
                out.name("warmupDuration").value(value.warmupDuration)
                out.name("reps").value(value.reps)
                out.name("workType").value(value.workType.name)
                out.name("workValue").value(value.workValue)
                out.name("workPace").value(value.workPace)
                out.name("restType").value(value.restType.name)
                out.name("restValue").value(value.restValue)
                out.name("cooldownDuration").value(value.cooldownDuration)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is LongRunConfig -> {
                out.value("long")
                out.name("data")
                out.beginObject()
                out.name("durationType").value(value.durationType.name)
                out.name("duration").value(value.duration)
                out.name("distance").value(value.distance)
                out.name("paceType").value(value.paceType.name)
                out.name("pace").value(value.pace)
                out.name("effort").value(value.effort)
                out.name("progression").value(value.progression.name)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is WorkoutConfig -> {
                out.value("workout")
                out.name("data")
                out.beginObject()
                out.name("workoutType").value(value.workoutType.name)
                out.name("duration").value(value.duration)
                out.name("intensity").value(value.intensity.name)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is RestConfig -> {
                out.value("rest")
                out.name("data")
                out.beginObject()
                out.name("restType").value(value.restType.name)
                out.name("notes").value(value.notes)
                out.endObject()
            }
            is RaceConfig -> {
                out.value("race")
                out.name("data")
                out.beginObject()
                out.name("raceName").value(value.raceName)
                out.name("distance").value(value.distance)
                out.name("goalType").value(value.goalType.name)
                out.name("goalValue").value(value.goalValue)
                out.name("notes").value(value.notes)
                out.endObject()
            }
        }
        out.endObject()
    }

    override fun read(reader: JsonReader): RunConfiguration? {
        var type: String? = null
        var config: RunConfiguration? = null
        
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> type = reader.nextString()
                "data" -> {
                    reader.beginObject()
                    when (type) {
                        "easy" -> {
                            var durationType = DurationType.DURATION
                            var duration: Int? = null
                            var distance: Double? = null
                            var effort = "Easy"
                            var paceRangeMin: String? = null
                            var paceRangeMax: String? = null
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "durationType" -> durationType = DurationType.valueOf(reader.nextString())
                                    "duration" -> duration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "distance" -> distance = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextDouble()
                                    "effort" -> effort = reader.nextString()
                                    "paceRangeMin" -> paceRangeMin = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextString()
                                    "paceRangeMax" -> paceRangeMax = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextString()
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = EasyRunConfig(durationType, duration, distance, effort, paceRangeMin, paceRangeMax, notes)
                        }
                        "tempo" -> {
                            var warmupDuration: Int? = null
                            var tempoDuration = 20
                            var tempoType = TempoType.EFFORT
                            var tempoEffort = "Hard"
                            var tempoPace: String? = null
                            var cooldownDuration: Int? = null
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "warmupDuration" -> warmupDuration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "tempoDuration" -> tempoDuration = reader.nextInt()
                                    "tempoType" -> tempoType = TempoType.valueOf(reader.nextString())
                                    "tempoEffort" -> tempoEffort = reader.nextString()
                                    "tempoPace" -> tempoPace = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextString()
                                    "cooldownDuration" -> cooldownDuration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = TempoRunConfig(warmupDuration, tempoDuration, tempoType, tempoEffort, tempoPace, cooldownDuration, notes)
                        }
                        "intervals" -> {
                            var warmupDuration: Int? = null
                            var reps = 6
                            var workType = IntervalType.DISTANCE
                            var workValue = "400"
                            var workPace = "4:00/km"
                            var restType = IntervalType.TIME
                            var restValue = "90"
                            var cooldownDuration: Int? = null
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "warmupDuration" -> warmupDuration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "reps" -> reps = reader.nextInt()
                                    "workType" -> workType = IntervalType.valueOf(reader.nextString())
                                    "workValue" -> workValue = reader.nextString()
                                    "workPace" -> workPace = reader.nextString()
                                    "restType" -> restType = IntervalType.valueOf(reader.nextString())
                                    "restValue" -> restValue = reader.nextString()
                                    "cooldownDuration" -> cooldownDuration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = IntervalsConfig(warmupDuration, reps, workType, workValue, workPace, restType, restValue, cooldownDuration, notes)
                        }
                        "long" -> {
                            var durationType = DurationType.DURATION
                            var duration: Int? = null
                            var distance: Double? = null
                            var paceType = PaceType.EFFORT
                            var pace: String? = null
                            var effort = "Easy"
                            var progression = ProgressionType.STEADY
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "durationType" -> durationType = DurationType.valueOf(reader.nextString())
                                    "duration" -> duration = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextInt()
                                    "distance" -> distance = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextDouble()
                                    "paceType" -> paceType = PaceType.valueOf(reader.nextString())
                                    "pace" -> pace = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextString()
                                    "effort" -> effort = reader.nextString()
                                    "progression" -> progression = ProgressionType.valueOf(reader.nextString())
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = LongRunConfig(durationType, duration, distance, paceType, pace, effort, progression, notes)
                        }
                        "workout" -> {
                            var workoutType = WorkoutType.STRENGTH
                            var duration = 30
                            var intensity = IntensityLevel.MEDIUM
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "workoutType" -> workoutType = WorkoutType.valueOf(reader.nextString())
                                    "duration" -> duration = reader.nextInt()
                                    "intensity" -> intensity = IntensityLevel.valueOf(reader.nextString())
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = WorkoutConfig(workoutType, duration, intensity, notes)
                        }
                        "rest" -> {
                            var restType = RestType.FULL_REST
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "restType" -> restType = RestType.valueOf(reader.nextString())
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = RestConfig(restType, notes)
                        }
                        "race" -> {
                            var raceName = ""
                            var distance = 5.0
                            var goalType = RaceGoalType.FINISH
                            var goalValue: String? = null
                            var notes = ""
                            
                            while (reader.hasNext()) {
                                when (reader.nextName()) {
                                    "raceName" -> raceName = reader.nextString()
                                    "distance" -> distance = reader.nextDouble()
                                    "goalType" -> goalType = RaceGoalType.valueOf(reader.nextString())
                                    "goalValue" -> goalValue = if (reader.peek() == com.google.gson.stream.JsonToken.NULL) { reader.nextNull(); null } else reader.nextString()
                                    "notes" -> notes = reader.nextString()
                                    else -> reader.skipValue()
                                }
                            }
                            config = RaceConfig(raceName, distance, goalType, goalValue, notes)
                        }
                    }
                    reader.endObject()
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        
        return config
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(RunConfiguration::class.java, RunConfigurationAdapter())
            .setPrettyPrinting()
            .create()
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "runner_planner.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: AppDatabase): ActivityDao {
        return database.activityDao()
    }
}
