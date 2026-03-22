package com.kwyr.runnerplanner.util

object Constants {
    const val DATASTORE_NAME = "runner_planner_preferences"
    
    object PreferenceKeys {
        const val ROUTES = "routes"
        const val ACTIVITIES = "activities"
        const val USER_PROFILE = "user_profile"
        const val THEME = "theme"
        const val TRAINING_PLAN = "training_plan"
        const val WEEK_PLANS = "week_plans"
    }
    
    object Navigation {
        const val HOME = "home"
        const val PLAN = "plan"
        const val HISTORY = "history"
        const val PROFILE = "profile"
        const val IMPORT_GPX = "import_gpx"
        const val DRAW_ROUTE = "draw_route"
        const val SETTINGS = "settings"
    }
}
