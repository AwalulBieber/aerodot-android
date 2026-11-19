# AeroDot

**AeroDot** is a native Android crosshair overlay and gaming utility app inspired by the feature set of JFF Crosshair Pro.

> Development status: early implementation. The APK has not yet passed a successful CI build or device test.

## Included in the current implementation
- Floating crosshair overlay using Android's Display over other apps permission
- 16 reticle shapes with 8 variants each (128 selectable presets)
- Size, thickness, gap, opacity, X/Y position and center-dot controls
- 16 color swatches and RGB sliders
- Optional outline / glow
- Show, Hide and Stop actions from the persistent notification
- Aim Trainer with Easy, Normal and Hard target sizes
- 5-round Reaction Test with best-time tracking
- 10-second Tap Speed / CPS test
- TCP latency checker to 1.1.1.1:443
- Local crosshair profiles

## Tech stack
- Kotlin
- Jetpack Compose
- Android WindowManager overlay API
- SharedPreferences for local configuration and profiles

## Requirements
- Android 8.0 (API 26) or newer
- Android Studio with JDK 17 and Android SDK Platform 35

## Build
Open the repository in Android Studio, allow Gradle sync, then run the `app` configuration.

A GitHub Actions workflow is included to build a debug APK and upload it as an artifact after a successful run.

## Reference and originality
JFF Crosshair Pro is used as a functional reference. This project is an independent implementation; its source code and visual assets are not copied. The referenced repository does not expose the Android app source in its visible root contents.
