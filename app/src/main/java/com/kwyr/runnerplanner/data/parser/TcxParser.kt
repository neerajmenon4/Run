package com.kwyr.runnerplanner.data.parser

import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.Split
import com.kwyr.runnerplanner.data.model.Trackpoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*

object TcxParser {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parseTcx(tcxContent: String): Activity? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(tcxContent))

            var activityType = "running"
            var name = "Activity"
            val trackpoints = mutableListOf<Trackpoint>()

            var eventType = parser.eventType
            var inTrackpoint = false
            var currentTime = ""
            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentAlt: Double? = null
            var currentDist = 0.0
            var currentHr: Int? = null
            var currentSpeed: Double? = null
            var currentCadence: Int? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Activity" -> {
                                activityType = parser.getAttributeValue(null, "Sport")?.lowercase() ?: "running"
                            }
                            "Notes" -> {
                                if (parser.next() == XmlPullParser.TEXT) {
                                    name = parser.text ?: "Activity"
                                }
                            }
                            "Trackpoint" -> {
                                inTrackpoint = true
                                currentTime = ""
                                currentLat = null
                                currentLon = null
                                currentAlt = null
                                currentDist = 0.0
                                currentHr = null
                                currentSpeed = null
                                currentCadence = null
                            }
                            "Time" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentTime = parser.text ?: ""
                                }
                            }
                            "LatitudeDegrees" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentLat = parser.text?.toDoubleOrNull()
                                }
                            }
                            "LongitudeDegrees" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentLon = parser.text?.toDoubleOrNull()
                                }
                            }
                            "AltitudeMeters" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentAlt = parser.text?.toDoubleOrNull()
                                }
                            }
                            "DistanceMeters" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentDist = parser.text?.toDoubleOrNull() ?: 0.0
                                }
                            }
                            "Value" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentHr = parser.text?.toIntOrNull()
                                }
                            }
                            "Speed", "ns3:Speed" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentSpeed = parser.text?.toDoubleOrNull()
                                }
                            }
                            "RunCadence", "ns3:RunCadence" -> {
                                if (inTrackpoint && parser.next() == XmlPullParser.TEXT) {
                                    currentCadence = parser.text?.toIntOrNull()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Trackpoint" && inTrackpoint) {
                            trackpoints.add(
                                Trackpoint(
                                    time = currentTime,
                                    latitude = currentLat,
                                    longitude = currentLon,
                                    altitude = currentAlt,
                                    distance = currentDist,
                                    heartRate = currentHr,
                                    speed = currentSpeed,
                                    cadence = currentCadence
                                )
                            )
                            inTrackpoint = false
                        }
                    }
                }
                eventType = parser.next()
            }

            if (trackpoints.isEmpty()) return null

            val startTime = trackpoints.first().time
            val endTime = trackpoints.last().time
            val totalDistance = trackpoints.last().distance
            val totalDuration = calculateDuration(startTime, endTime)

            val heartRates = trackpoints.mapNotNull { it.heartRate }
            val averageHeartRate = if (heartRates.isNotEmpty()) heartRates.average().toInt() else null
            val maxHeartRate = heartRates.maxOrNull()
            val minHeartRate = heartRates.minOrNull()

            val cadences = trackpoints.mapNotNull { it.cadence }
            val averageCadence = if (cadences.isNotEmpty()) cadences.average().toInt() else null
            val maxCadence = cadences.maxOrNull()

            val elevationGain = calculateElevationGain(trackpoints)
            val elevationLoss = calculateElevationLoss(trackpoints)

            // Calculate average pace from total distance and duration (seconds per km)
            val averagePace = if (totalDistance > 0 && totalDuration > 0) {
                (totalDuration / totalDistance) * 1000.0
            } else null
            
            val paces = calculatePaces(trackpoints)
            val maxPace = paces.maxOrNull()
            val minPace = paces.minOrNull()

            val splits = generateSplits(trackpoints)

            Activity(
                id = "activity_${System.currentTimeMillis()}",
                name = name,
                type = activityType,
                startTime = startTime,
                endTime = endTime,
                totalDistance = totalDistance,
                totalDuration = totalDuration,
                averageHeartRate = averageHeartRate,
                maxHeartRate = maxHeartRate,
                minHeartRate = minHeartRate,
                averageCadence = averageCadence,
                maxCadence = maxCadence,
                totalElevationGain = elevationGain,
                totalElevationLoss = elevationLoss,
                averagePace = averagePace,
                maxPace = maxPace,
                minPace = minPace,
                trackpoints = trackpoints,
                splits = splits,
                createdAt = System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString()
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateDuration(startTime: String, endTime: String): Double {
        return try {
            val start = dateFormat.parse(startTime)?.time ?: 0L
            val end = dateFormat.parse(endTime)?.time ?: 0L
            ((end - start) / 1000.0)
        } catch (e: Exception) {
            0.0
        }
    }

    private fun calculateElevationGain(trackpoints: List<Trackpoint>): Double {
        var gain = 0.0
        for (i in 1 until trackpoints.size) {
            val prev = trackpoints[i - 1].altitude ?: 0.0
            val curr = trackpoints[i].altitude ?: 0.0
            if (curr > prev) {
                gain += curr - prev
            }
        }
        return gain
    }

    private fun calculateElevationLoss(trackpoints: List<Trackpoint>): Double {
        var loss = 0.0
        for (i in 1 until trackpoints.size) {
            val prev = trackpoints[i - 1].altitude ?: 0.0
            val curr = trackpoints[i].altitude ?: 0.0
            if (curr < prev) {
                loss += prev - curr
            }
        }
        return loss
    }

    private fun calculatePaces(trackpoints: List<Trackpoint>): List<Double> {
        val paces = mutableListOf<Double>()
        for (i in 1 until trackpoints.size) {
            val prev = trackpoints[i - 1]
            val curr = trackpoints[i]
            val distanceDiff = curr.distance - prev.distance
            val timeDiff = calculateDuration(prev.time, curr.time)

            if (distanceDiff > 0 && timeDiff > 0) {
                val pace = (timeDiff / distanceDiff) * 1000
                paces.add(pace)
            }
        }
        return paces
    }

    private fun generateSplits(trackpoints: List<Trackpoint>): List<Split> {
        val splits = mutableListOf<Split>()
        val splitDistance = 1000.0
        var splitNumber = 1
        var lastSplitIndex = 0

        for (i in 1 until trackpoints.size) {
            val currentDistance = trackpoints[i].distance
            val targetDistance = splitNumber * splitDistance

            if (currentDistance >= targetDistance) {
                val splitTrackpoints = trackpoints.subList(lastSplitIndex, i + 1)
                val previousDistance = if (lastSplitIndex > 0) trackpoints[lastSplitIndex].distance else 0.0
                val split = createSplit(splitNumber, splitTrackpoints, previousDistance)
                splits.add(split)

                lastSplitIndex = i
                splitNumber++
            }
        }

        if (lastSplitIndex < trackpoints.size - 1) {
            val finalTrackpoints = trackpoints.subList(lastSplitIndex, trackpoints.size)
            val split = createSplit(splitNumber, finalTrackpoints, trackpoints[lastSplitIndex].distance)
            splits.add(split)
        }

        return splits
    }

    private fun createSplit(splitNumber: Int, trackpoints: List<Trackpoint>, previousDistance: Double): Split {
        val startTp = trackpoints.first()
        val endTp = trackpoints.last()

        val distance = endTp.distance - previousDistance
        val duration = calculateDuration(startTp.time, endTp.time)
        val pace = if (duration > 0) (duration / distance) * 1000 else 0.0
        val speed = if (duration > 0) distance / duration else 0.0

        val heartRates = trackpoints.mapNotNull { it.heartRate }
        val averageHeartRate = if (heartRates.isNotEmpty()) heartRates.average().toInt() else null
        val maxHeartRate = heartRates.maxOrNull()
        val minHeartRate = heartRates.minOrNull()

        val cadences = trackpoints.mapNotNull { it.cadence }
        val averageCadence = if (cadences.isNotEmpty()) cadences.average().toInt() else null

        val elevationGain = calculateElevationGain(trackpoints)
        val elevationLoss = calculateElevationLoss(trackpoints)

        return Split(
            splitNumber = splitNumber,
            distance = distance,
            duration = duration,
            pace = pace,
            speed = speed,
            averageHeartRate = averageHeartRate,
            maxHeartRate = maxHeartRate,
            minHeartRate = minHeartRate,
            averageCadence = averageCadence,
            elevationGain = elevationGain,
            elevationLoss = elevationLoss
        )
    }
}
