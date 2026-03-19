# Build and Test on Android Phone

## Prerequisites

1. **Android Studio** installed (latest version recommended)
2. **Physical Android phone** with USB debugging enabled
3. **USB cable** to connect phone to computer
4. **Java 17+** (usually comes with Android Studio)

## Step 1: Enable USB Debugging on Your Phone

### For Android 10+:
1. Go to **Settings** → **About phone**
2. Tap **Build number** 7 times to enable Developer Options
3. Go back to **Settings** → **System** → **Developer options**
4. Enable **USB debugging**
5. Enable **USB debugging (security settings)** (if available)

### For Android 11+:
1. Go to **Settings** → **About phone**
2. Tap **Build number** 7 times
3. Go to **Settings** → **Developer options**
4. Enable **USB debugging**
5. Enable **Wireless debugging** (optional but recommended)

## Step 2: Connect Your Phone

1. Connect your phone to computer via USB cable
2. When prompted, allow USB debugging on your phone
3. Make sure your computer recognizes the device

## Step 3: Open Project in Android Studio

1. Launch Android Studio
2. Click **Open** or **File** → **Open**
3. Navigate to `/Users/neerajmenon/Documents/kwyr/runner-route-planner-android`
4. Select the folder and click **Open**
5. Wait for Gradle sync to complete (may take a few minutes)

## Step 4: Build and Install

### Method 1: Using Android Studio (Recommended)
1. Make sure your device is selected in the device dropdown (top toolbar)
2. Click the **Run** button (green play icon) or press **Shift + F10**
3. Android Studio will build and install the app automatically

### Method 2: Using Command Line
1. Open Terminal
2. Navigate to project directory:
   ```bash
   cd /Users/neerajmenon/Documents/kwyr/runner-route-planner-android
   ```
3. Check if device is connected:
   ```bash
   ./gradlew devices
   ```
4. Build and install:
   ```bash
   ./gradlew installDebug
   ```

## Step 5: Launch the App

After installation, the app should appear on your phone's home screen or app drawer. Look for **"Runner Route Planner"**.

## Troubleshooting

### Device Not Detected
```bash
# Check connected devices
adb devices

# If no devices listed, try:
adb kill-server
adb start-server
adb devices
```

### Build Errors
1. **Gradle sync issues**: File → Invalidate Caches → Invalidate and Restart
2. **SDK issues**: Open SDK Manager and install missing components
3. **Permission issues**: Make sure you've allowed USB debugging

### Installation Issues
1. **App not installed**: Try uninstalling any existing version first
2. **Permission denied**: Check if "Install unknown apps" is enabled
3. **Storage space**: Ensure enough space on your phone

## Alternative: Build APK for Manual Installation

If you prefer to build an APK file and install it manually:

```bash
# Build debug APK
./gradlew assembleDebug

# Find the APK in:
# app/build/outputs/apk/debug/app-debug.apk
```

Then transfer the APK to your phone and install it using a file manager.

## Testing the App

Once installed, test these features:

1. **Home Screen**
   - Check that stats display correctly
   - Test the speed chart with different time periods
   - Tap the Import button

2. **Navigation**
   - Switch between tabs (Home, Routes, History, Profile)
   - Test back navigation

3. **Routes Screen**
   - Should show "No saved routes yet" initially
   - Test the UI layout

4. **History Screen**
   - Should show "No activities yet" initially
   - Test the UI layout

5. **Import Screen**
   - Test file picker (requires GPX/TCX files)
   - Check error handling

6. **Profile Screen**
   - Toggle between metric/imperial units
   - Toggle between light/dark themes

## Performance Tips

- Close other apps while testing
- Ensure phone has sufficient battery
- Test on both Wi-Fi and mobile data
- Check app behavior with phone rotation

## Logcat for Debugging

If you encounter issues, view logs:
```bash
# Filter logs for your app
adb logcat -s "RunnerRoutePlanner"

# Or use Android Studio's Logcat tab
```

Happy testing! 🚀
