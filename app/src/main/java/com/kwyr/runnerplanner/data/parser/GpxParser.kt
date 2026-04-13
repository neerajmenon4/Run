package com.kwyr.runnerplanner.data.parser

import com.kwyr.runnerplanner.data.model.Activity
import com.kwyr.runnerplanner.data.model.Coordinate
import com.kwyr.runnerplanner.data.model.Route
import com.kwyr.runnerplanner.data.model.Split
import com.kwyr.runnerplanner.data.model.Trackpoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

data class GpxCoordinate(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val time: String? = null,
    val heartRate: Int? = null
)

data class GpxTrack(
    val name: String,
    val description: String? = null,
    val coordinates: List<GpxCoordinate>
)

object GpxParser {

    private val gpxDateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).also { it.timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).also { it.timeZone = TimeZone.getTimeZone("UTC") }
    )
    private val outputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    fun parseGpx(gpxContent: String): GpxTrack? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(gpxContent))

            var name = "Imported Route"
            var description: String? = null
            val coordinates = mutableListOf<GpxCoordinate>()

            var eventType = parser.eventType
            var currentLat: Double? = null
            var currentLon: Double? = null
            var currentEle: Double? = null
            var currentTime: String? = null
            var currentHr: Int? = null
            var inTrkpt = false
            var inExtensions = false
            var inTpx = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "name" -> {
                                if (parser.next() == XmlPullParser.TEXT) {
                                    name = parser.text ?: "Imported Route"
                                }
                            }
                            "desc" -> {
                                if (parser.next() == XmlPullParser.TEXT) {
                                    description = parser.text
                                }
                            }
                            "trkpt", "wpt" -> {
                                inTrkpt = true
                                currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                currentLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                                currentEle = null
                                currentTime = null
                                currentHr = null
                            }
                            "ele" -> {
                                if (inTrkpt && parser.next() == XmlPullParser.TEXT) {
                                    currentEle = parser.text?.toDoubleOrNull()
                                }
                            }
                            "time" -> {
                                if (inTrkpt && parser.next() == XmlPullParser.TEXT) {
                                    currentTime = parser.text
                                }
                            }
                            "extensions" -> inExtensions = true
                            "ns3:TrackPointExtension" -> if (inExtensions && inTrkpt) inTpx = true
                            "ns3:hr" -> {
                                if (inTpx && parser.next() == XmlPullParser.TEXT) {
                                    currentHr = parser.text?.toIntOrNull()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name) {
                            "trkpt", "wpt" -> {
                                if (inTrkpt && currentLat != null && currentLon != null) {
                                    coordinates.add(
                                        GpxCoordinate(
                                            latitude = currentLat,
                                            longitude = currentLon,
                                            elevation = currentEle,
                                            time = currentTime,
                                            heartRate = currentHr
                                        )
                                    )
                                }
                                inTrkpt = false
                            }
                            "extensions" -> inExtensions = false
                            "ns3:TrackPointExtension" -> inTpx = false
                        }
                    }
                }
                eventType = parser.next()
            }

            if (coordinates.isEmpty()) null
            else GpxTrack(name, description, coordinates)
        } catch (e: Exception) {
            null
        }
    }

    fun convertToActivity(gpxContent: String): Activity? {
        val track = parseGpx(gpxContent) ?: return null
        val coords = track.coordinates
        if (coords.isEmpty()) return null

        val startDate = coords.firstOrNull()?.time?.let { parseGpxTime(it) } ?: return null
        val endDate = coords.lastOrNull()?.time?.let { parseGpxTime(it) } ?: startDate
        val totalDuration = ((endDate.time - startDate.time) / 1000.0).coerceAtLeast(0.0)

        // Build trackpoints with cumulative distance and speed
        var cumulativeDistance = 0.0
        val trackpoints = mutableListOf<Trackpoint>()

        coords.forEachIndexed { index, coord ->
            val distFromPrev = if (index > 0) {
                haversineDistance(
                    coords[index - 1].latitude, coords[index - 1].longitude,
                    coord.latitude, coord.longitude
                )
            } else 0.0
            cumulativeDistance += distFromPrev

            val speed: Double? = if (index > 0) {
                val prevTime = coords[index - 1].time?.let { parseGpxTime(it) }
                val currTime = coord.time?.let { parseGpxTime(it) }
                if (prevTime != null && currTime != null) {
                    val timeDiffSec = (currTime.time - prevTime.time) / 1000.0
                    if (timeDiffSec > 0) distFromPrev / timeDiffSec else 0.0
                } else null
            } else 0.0

            trackpoints.add(
                Trackpoint(
                    time = coord.time ?: "",
                    latitude = coord.latitude,
                    longitude = coord.longitude,
                    altitude = coord.elevation,
                    distance = cumulativeDistance,
                    heartRate = coord.heartRate,
                    speed = speed,
                    cadence = null
                )
            )
        }

        val totalDistance = cumulativeDistance

        // HR stats
        val hrValues = trackpoints.mapNotNull { it.heartRate }
        val avgHr = if (hrValues.isNotEmpty()) hrValues.average().toInt() else null
        val maxHr = hrValues.maxOrNull()
        val minHr = hrValues.minOrNull()

        // Elevation gain/loss
        var elevationGain = 0.0
        var elevationLoss = 0.0
        for (i in 1 until coords.size) {
            val diff = (coords[i].elevation ?: 0.0) - (coords[i - 1].elevation ?: 0.0)
            if (diff > 0) elevationGain += diff else elevationLoss += -diff
        }

        // Splits at every 1km
        val splits = computeSplits(trackpoints)

        val avgPace = if (totalDistance > 0) totalDuration / (totalDistance / 1000.0) else null
        val now = outputDateFormat.format(Date())

        return Activity(
            id = "bike_${startDate.time}",
            name = track.name,
            type = "biking",
            startTime = outputDateFormat.format(startDate),
            endTime = outputDateFormat.format(endDate),
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averageHeartRate = avgHr,
            maxHeartRate = maxHr,
            minHeartRate = minHr,
            totalElevationGain = if (elevationGain > 0) elevationGain else null,
            totalElevationLoss = if (elevationLoss > 0) elevationLoss else null,
            averagePace = avgPace,
            trackpoints = trackpoints,
            splits = splits,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun computeSplits(trackpoints: List<Trackpoint>): List<Split> {
        val splits = mutableListOf<Split>()
        var splitNumber = 1
        var splitStartIdx = 0
        var splitHrValues = mutableListOf<Int>()
        var splitElevGain = 0.0
        var splitElevLoss = 0.0

        for (i in 1 until trackpoints.size) {
            val tp = trackpoints[i]
            val prevTp = trackpoints[i - 1]

            tp.heartRate?.let { splitHrValues.add(it) }
            val elevDiff = (tp.altitude ?: 0.0) - (prevTp.altitude ?: 0.0)
            if (elevDiff > 0) splitElevGain += elevDiff else splitElevLoss += -elevDiff

            val kmMark = splitNumber * 1000.0
            if (tp.distance >= kmMark) {
                val splitStart = trackpoints[splitStartIdx]
                val splitDist = tp.distance - splitStart.distance
                val startMs = parseGpxTime(splitStart.time)?.time ?: 0L
                val endMs = parseGpxTime(tp.time)?.time ?: 0L
                val splitDuration = ((endMs - startMs) / 1000.0).coerceAtLeast(0.0)
                val splitSpeed = if (splitDuration > 0) splitDist / splitDuration else 0.0
                val splitPace = if (splitDist > 0) splitDuration / (splitDist / 1000.0) else 0.0

                splits.add(
                    Split(
                        splitNumber = splitNumber,
                        distance = splitDist,
                        duration = splitDuration,
                        pace = splitPace,
                        speed = splitSpeed,
                        averageHeartRate = if (splitHrValues.isNotEmpty()) splitHrValues.average().toInt() else null,
                        maxHeartRate = splitHrValues.maxOrNull(),
                        minHeartRate = splitHrValues.minOrNull(),
                        elevationGain = splitElevGain,
                        elevationLoss = splitElevLoss
                    )
                )

                splitNumber++
                splitStartIdx = i
                splitHrValues = mutableListOf()
                splitElevGain = 0.0
                splitElevLoss = 0.0
            }
        }

        return splits
    }

    private fun parseGpxTime(timeStr: String): Date? {
        for (fmt in gpxDateFormats) {
            try {
                return fmt.parse(timeStr)
            } catch (_: Exception) {}
        }
        return null
    }

    fun calculateDistance(coordinates: List<GpxCoordinate>): Double {
        if (coordinates.size < 2) return 0.0
        var totalDistance = 0.0
        for (i in 0 until coordinates.size - 1) {
            totalDistance += haversineDistance(
                coordinates[i].latitude, coordinates[i].longitude,
                coordinates[i + 1].latitude, coordinates[i + 1].longitude
            )
        }
        return totalDistance
    }

    fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = toRad(lat2 - lat1)
        val dLon = toRad(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRad(lat1)) * cos(toRad(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    private fun toRad(degrees: Double): Double = degrees * (PI / 180)

    fun convertToRoute(gpxTrack: GpxTrack): Route {
        val distanceMeters = calculateDistance(gpxTrack.coordinates)
        return Route(
            id = "gpx_${System.currentTimeMillis()}",
            name = gpxTrack.name,
            coordinates = gpxTrack.coordinates.map { coord ->
                Coordinate(coord.longitude, coord.latitude)
            },
            distanceMeters = distanceMeters,
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString(),
            targetTimeSec = null,
            targetPaceSecPerKm = null
        )
    }
}
