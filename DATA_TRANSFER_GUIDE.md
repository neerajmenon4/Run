# Data Persistence & Transfer Feature

## Overview

This feature allows users to transfer all their running data between devices **without any cloud services or account linking**. All data stays local and under the user's control.

## What Gets Backed Up

The backup file (`.kwyr` format) contains:
- **Routes**: All saved running routes with coordinates
- **Activities**: Imported Garmin activities (GPX/TCX data)
- **User Profile**: Name, unit preferences
- **Training Plans**: Active training plan and weekly schedules
- **App Settings**: Theme preference

## How It Works

### Export Process
1. User navigates to **Profile → Data Backup & Transfer**
2. Taps **Export Backup**
3. Chooses save location (Downloads, Documents, etc.)
4. A `.kwyr` file is created with timestamp (e.g., `kwyr_backup_1234567890.kwyr`)

### Transfer Methods
Users can transfer the `.kwyr` file via:
- **Bluetooth** (Android Nearby Share)
- **USB Cable** (file transfer to computer, then to new device)
- **Email/Messaging** (send file as attachment)
- **Cloud storage** (optional - user's choice, not required)
- **SD Card** (if device supports)

### Import Process
1. User receives `.kwyr` file on new device
2. Opens app → **Profile → Data Backup & Transfer**
3. Taps **Import Backup**
4. Selects the `.kwyr` file
5. Chooses restore option:
   - **Merge**: Combines backup data with existing data (no duplicates)
   - **Replace All**: Overwrites all existing data with backup

## Technical Implementation

### Architecture
```
BackupData (model)
    ↓
BackupRepository
    ├── createBackup() - Collects all data from DataStore & Room DB
    ├── exportBackupToUri() - Writes JSON to file
    ├── importBackupFromUri() - Reads JSON from file
    └── restoreBackup() - Writes data back to DataStore & Room DB
```

### File Format
- **Format**: JSON
- **Extension**: `.kwyr` (custom, but standard JSON)
- **Structure**:
```json
{
  "version": 1,
  "exportDate": "2026-03-22T17:06:00",
  "appVersion": "1.0",
  "routes": [...],
  "activities": [...],
  "userProfile": {...},
  "theme": "dark",
  "trainingPlan": {...},
  "weekPlans": {...}
}
```

### Data Sources
- **DataStore Preferences**: Routes, user profile, training plans, theme
- **Room Database**: Activities (imported from Garmin)

### Security & Privacy
- ✅ No cloud dependency
- ✅ No account required
- ✅ No data sent to servers
- ✅ User controls file location
- ✅ Standard Android file permissions (scoped storage on Android 13+)

## User Experience

### Settings Screen Location
**Profile Tab → Data Backup & Transfer** (bottom of profile screen)

### UI Features
- Clear export/import buttons with icons
- Loading indicators during operations
- Success/error toast messages
- Merge vs Replace dialog on import
- Instructions card explaining transfer methods

## Permissions

### Android Manifest
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

- **Android 13+**: Uses scoped storage (no permission needed)
- **Android 12 and below**: Requires WRITE_EXTERNAL_STORAGE

### File Access
Uses Android's Storage Access Framework (SAF):
- `ActivityResultContracts.CreateDocument` for export
- `ActivityResultContracts.OpenDocument` for import
- User explicitly chooses file location via system picker

## Future Enhancements

Potential improvements:
1. **Compression**: Gzip the JSON to reduce file size
2. **Encryption**: Optional password protection for backup files
3. **Selective Backup**: Let users choose what to backup (routes only, activities only, etc.)
4. **Automatic Backups**: Scheduled local backups
5. **Backup Verification**: Checksum validation
6. **Version Migration**: Handle backups from older app versions

## Testing Checklist

- [ ] Export backup with data
- [ ] Export backup with empty data
- [ ] Import backup (merge mode)
- [ ] Import backup (replace mode)
- [ ] Transfer file via Bluetooth
- [ ] Transfer file via USB
- [ ] Import corrupted file (error handling)
- [ ] Import backup from older version (future)

## Code Files

### New Files Created
- `data/model/BackupData.kt` - Backup data model
- `data/repository/BackupRepository.kt` - Backup logic
- `ui/screens/settings/SettingsScreen.kt` - Settings UI
- `ui/screens/settings/SettingsViewModel.kt` - Settings state management

### Modified Files
- `AppNavGraph.kt` - Added Settings route
- `ui/screens/profile/ProfileScreen.kt` - Added Settings navigation
- `util/Constants.kt` - Added SETTINGS constant
- `AndroidManifest.xml` - Added file permissions
