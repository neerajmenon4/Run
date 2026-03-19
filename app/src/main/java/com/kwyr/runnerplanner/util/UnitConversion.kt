package com.kwyr.runnerplanner.util

import com.kwyr.runnerplanner.data.model.UnitSystem

object UnitConversion {
    data class FormattedValue(val value: String, val unit: String)

    fun formatDistance(distanceKm: Double, unitSystem: UnitSystem): FormattedValue {
        return when (unitSystem) {
            UnitSystem.METRIC -> FormattedValue(
                value = String.format("%.2f", distanceKm),
                unit = "KM"
            )
            UnitSystem.IMPERIAL -> {
                val miles = distanceKm * 0.621371
                FormattedValue(
                    value = String.format("%.2f", miles),
                    unit = "MI"
                )
            }
        }
    }

    fun formatSpeed(speedKmh: Double, unitSystem: UnitSystem): FormattedValue {
        return when (unitSystem) {
            UnitSystem.METRIC -> FormattedValue(
                value = String.format("%.1f", speedKmh),
                unit = "KM/H"
            )
            UnitSystem.IMPERIAL -> {
                val mph = speedKmh * 0.621371
                FormattedValue(
                    value = String.format("%.1f", mph),
                    unit = "MPH"
                )
            }
        }
    }

    fun formatPace(paceSecPerKm: Double, unitSystem: UnitSystem): FormattedValue {
        return when (unitSystem) {
            UnitSystem.METRIC -> {
                val minutes = (paceSecPerKm / 60).toInt()
                val seconds = (paceSecPerKm % 60).toInt()
                FormattedValue(
                    value = String.format("%d:%02d", minutes, seconds),
                    unit = "MIN/KM"
                )
            }
            UnitSystem.IMPERIAL -> {
                val paceSecPerMile = paceSecPerKm * 1.60934
                val minutes = (paceSecPerMile / 60).toInt()
                val seconds = (paceSecPerMile % 60).toInt()
                FormattedValue(
                    value = String.format("%d:%02d", minutes, seconds),
                    unit = "MIN/MI"
                )
            }
        }
    }

    fun metersToKm(meters: Double): Double = meters / 1000.0
    
    fun metersToMiles(meters: Double): Double = meters * 0.000621371
    
    fun kmToMeters(km: Double): Double = km * 1000.0
}
