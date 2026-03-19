package com.kwyr.runnerplanner.data.model

enum class UnitSystem {
    METRIC,
    IMPERIAL
}

data class UserProfile(
    val name: String = "Runner",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val theme: String = "light"
)
