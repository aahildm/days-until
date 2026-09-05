# Days Until

A simple Android home screen widget that counts down to a date you set.

## Features
- Set any date and event name
- Home screen widget shows remaining days
- Updates automatically every 24 hours
- Tap widget to open app and change date

## Build
GitHub Actions automatically builds APK on every push to `main`/`master`.

Download APK from **Actions** tab → latest workflow run → Artifacts.

## Manual Build
```bash
./gradlew assembleDebug
```

## Install
1. Download `days-until-debug-apk` from GitHub Actions
2. Install on Android device
3. Long-press home screen → Widgets → Days Until
4. Set your date and enjoy!

## Tech
- Java, Android SDK 34
- Min SDK 24 (Android 7.0+)
- AppWidgetProvider for home screen widget
