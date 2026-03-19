# Runner Route Planner - Android

A native Android application for planning and tracking running routes, migrated from React Native to Kotlin with Jetpack Compose.

## Features

- **Home Dashboard**: View running statistics, speed improvement charts, and activity summaries
- **Route Management**: Save, view, and manage custom running routes
- **Activity History**: Import and track activities from Garmin (GPX/TCX files)
- **Profile Settings**: Customize unit system (metric/imperial) and theme (light/dark)
- **Modern UI**: Clean, minimalist design with Nothing-style custom fonts

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3
- **Architecture**: MVVM with StateFlow
- **Dependency Injection**: Hilt
- **Local Storage**: DataStore Preferences
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines & Flow

## Project Structure

```
app/src/main/java/com/kwyr/runnerplanner/
├── MainActivity.kt                 # Main entry point
├── MainViewModel.kt                # Theme management
├── AppNavGraph.kt                  # Navigation configuration
├── RunnerPlannerApplication.kt     # Hilt application class
├── ui/
│   ├── theme/                      # Theme system (colors, typography, shapes)
│   ├── components/                 # Reusable composables (icons, navigation)
│   └── screens/                    # Feature screens with ViewModels
│       ├── home/                   # Dashboard with stats & charts
│       ├── routes/                 # Saved routes list
│       ├── history/                # Activity history
│       ├── import_gpx/             # GPX/TCX file import
│       └── profile/                # User settings
├── data/
│   ├── model/                      # Data classes (Route, Activity, etc.)
│   ├── repository/                 # Repository layer with Result handling
│   ├── local/                      # DataStore implementation
│   ├── parser/                     # GPX/TCX XML parsers
│   └── remote/                     # Future: Garmin API integration
├── di/                             # Hilt dependency injection modules
└── util/                           # Utilities (unit conversion, constants)
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK API 26+ (minimum)
- Android SDK API 34 (target)

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

### Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

## Features in Detail

### Home Screen
- Personalized greeting with user name
- Current date display
- Total distance and average speed statistics
- Interactive speed improvement chart with time period filters (1W, 1M, 3M)
- Import activities from Garmin button

### Routes List Screen
- View all saved routes
- Display route name, distance, and creation date
- Swipe-to-delete functionality

### Activity History Screen
- List of imported activities from GPX/TCX files
- Activity details: name, date, distance, duration, heart rate
- Delete activities

### Import Screen
- File picker for GPX/TCX files
- Parse and import activity data
- Automatic navigation to history on success

### Profile Screen
- Toggle between metric and imperial units
- Switch between light and dark themes
- User profile management

## Data Models

### Route
- Coordinates (longitude, latitude pairs)
- Distance in meters
- Name and timestamps
- Optional target time and pace

### Activity
- Trackpoints with GPS, heart rate, cadence, speed
- Splits (1km segments)
- Comprehensive metrics (distance, duration, elevation, pace)
- Heart rate and cadence statistics

## Custom Fonts

The app uses Nothing-style custom fonts for a unique aesthetic:
- **Ndot57**: Primary font for most text
- **Ndot55**: Alternative display font
- **NType82**: Accent font
- **SpaceGrotesk**: Secondary font

## Theme System

### Light Theme
- Background: `#FFFFFF`
- Surface: `#F8F8F8`
- Primary: `#000000`
- Accent: `#FF6B35`

### Dark Theme
- Background: `#000000`
- Surface: `#1A1A1A`
- Primary: `#FFFFFF`
- Accent: `#FF6B35`

## Migration from React Native

This app was migrated from React Native following these principles:
- **One-to-one UI fidelity**: Exact replication of layout, spacing, and typography
- **Modern Android stack**: Pure Compose, no XML layouts
- **Native performance**: Leveraging Kotlin coroutines and Flow
- **Type safety**: Strong typing with Kotlin data classes

### Key Migrations
- `AsyncStorage` → DataStore Preferences
- `axios` → Retrofit (prepared for future API integration)
- `react-native-svg` → Compose Canvas
- `useState`/`useEffect` → StateFlow/LaunchedEffect
- `React Navigation` → Navigation Compose

## Future Enhancements

- [ ] Garmin OAuth integration for direct activity sync
- [ ] Map view for route visualization
- [ ] Route drawing functionality
- [ ] Activity analytics and insights
- [ ] Export routes to GPX
- [ ] Social features (share routes)
- [ ] Offline map support

## Dependencies

See `app/build.gradle.kts` for the complete list. Key dependencies:
- Compose BOM 2024.06.00
- Material3
- Navigation Compose
- Hilt for DI
- DataStore Preferences
- Retrofit (for future API calls)
- Coil (for image loading)

## License

Copyright © 2026 Kwyr

## Contributing

This is a migration project. For feature requests or bug reports, please open an issue.
