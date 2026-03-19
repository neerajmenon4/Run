package com.kwyr.runnerplanner.data.parser

import com.kwyr.runnerplanner.data.model.Coordinate
import com.kwyr.runnerplanner.data.model.Route
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import kotlin.math.*

data class GpxCoordinate(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double? = null,
    val time: String? = null
)

data class GpxTrack(
    val name: String,
    val description: String? = null,
    val coordinates: List<GpxCoordinate>
)

object GpxParser {
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
            var inTrkpt = false

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
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if ((parser.name == "trkpt" || parser.name == "wpt") && inTrkpt) {
                            if (currentLat != null && currentLon != null) {
                                coordinates.add(
                                    GpxCoordinate(
                                        latitude = currentLat,
                                        longitude = currentLon,
                                        elevation = currentEle,
                                        time = currentTime
                                    )
                                )
                            }
                            inTrkpt = false
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

    fun calculateDistance(coordinates: List<GpxCoordinate>): Double {
        if (coordinates.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until coordinates.size - 1) {
            val lat1 = coordinates[i].latitude
            val lon1 = coordinates[i].longitude
            val lat2 = coordinates[i + 1].latitude
            val lon2 = coordinates[i + 1].longitude

            totalDistance += haversineDistance(lat1, lon1, lat2, lon2)
        }

        return totalDistance
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
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
