---
name: SimpleButtonApp Android Agent
description: >
  Project-specific guidance for SimpleButtonApp, a Kotlin Android application
  with BottomNavigationView, fragment-based navigation, real-time sensor
  readings, and fused GPS location updates.
applyTo:
  - "**/*.gradle"
  - "**/AndroidManifest.xml"
  - "**/src/**/*.kt"
  - "**/res/**/*.xml"
  - "**/proguard-rules.pro"
  - "gradle.properties"
  - "settings.gradle"
  - "*.md"
capabilities:
  - Android Kotlin development
  - Fragment lifecycle management
  - BottomNavigationView navigation
  - SensorManager and SensorEventListener integration
  - FusedLocationProviderClient location updates
  - Runtime location permission handling
  - Android resource and Gradle configuration maintenance
mode: autonomous
---

# SimpleButtonApp Android Agent

## Project Summary

SimpleButtonApp is a Kotlin Android app that uses a single `MainActivity` as a navigation host. The activity displays four bottom navigation tabs:

- Dashboard
- Devices
- Settings
- About

The Dashboard tab displays live accelerometer, gyroscope, magnetometer, and GPS data. The Devices tab provides MQTT connectivity for publishing GPS data to a device-specific topic. The Settings tab embeds an interactive WebView for `http://10.3.3.31:5000`. About currently renders simple placeholder `TextView` content.

## Current Source Map

```text
SimpleButtonApp/
|-- build.gradle
|-- settings.gradle
|-- gradle.properties
|-- app/
    |-- build.gradle
    |-- proguard-rules.pro
    |-- src/main/
        |-- AndroidManifest.xml
        |-- kotlin/com/example/simplebuttonapp/
        |   |-- MainActivity.kt
        |   |-- DashboardFragment.kt
        |   |-- DevicesFragment.kt
        |   |-- MqttGpsPublisherManager.kt
        |   |-- SettingsFragment.kt
        |   `-- AboutFragment.kt
        `-- res/
            |-- layout/
            |   |-- activity_main.xml
            |   |-- fragment_dashboard.xml
            |   |-- fragment_devices.xml
            |   `-- fragment_settings.xml
            |-- xml/
            |   `-- network_security_config.xml
            |-- menu/
            |   `-- bottom_menu.xml
            `-- values/
                |-- colors.xml
                `-- strings.xml
```

## Core Behavior

### MainActivity.kt

`MainActivity` extends `AppCompatActivity`.

Responsibilities:

- Sets `activity_main.xml` as the content view.
- Finds `BottomNavigationView` by `R.id.bottom_navigation`.
- Loads `DashboardFragment` on first launch.
- Replaces `R.id.fragment_container` when a bottom navigation item is selected.
- Adds tab switches to the fragment back stack.

### DashboardFragment.kt

`DashboardFragment` extends `Fragment` and implements `SensorEventListener`.

Responsibilities:

- Inflates `fragment_dashboard.xml`.
- Initializes `FusedLocationProviderClient`.
- Initializes `SensorManager`.
- Reads default sensors:
  - `Sensor.TYPE_ACCELEROMETER`
  - `Sensor.TYPE_GYROSCOPE`
  - `Sensor.TYPE_MAGNETIC_FIELD`
- Registers sensor listeners in `onResume()`.
- Unregisters sensor listeners in `onPause()`.
- Requests fine and coarse location permissions if needed.
- Starts high-accuracy GPS updates every 1000 ms.
- Stops GPS updates in `onPause()`.
- Formats sensor values through `R.string.sensor_value_format`.
- Formats GPS values through string resources.

Important current limitation:

- The fragment requests permissions with `ActivityCompat.requestPermissions(...)` from inside a fragment and does not currently override a permission result callback. If the user grants permission from the prompt, GPS updates may not begin until a later resume path. Prefer fragment permission APIs or `requestPermissions(...)` with an explicit callback if this behavior is changed.

### SettingsFragment.kt

`SettingsFragment` embeds an Android `WebView` and loads `http://10.3.3.31:5000`.

WebView behavior:

- JavaScript enabled.
- DOM storage enabled.
- Navigation stays inside the WebView.
- Zoom controls are available without visible zoom buttons.
- State is saved and restored across fragment recreation.

The app uses `network_security_config.xml` to permit cleartext HTTP traffic to `10.3.3.31`.

### Placeholder Fragments

`AboutFragment` creates a `TextView` directly in Kotlin. It does not currently have an XML layout file.

### DevicesFragment.kt

`DevicesFragment` is the UI for the MQTT GPS publisher.

Responsibilities:

- Inflates `fragment_devices.xml`.
- Accepts broker IP/link, port, and device name.
- Builds the topic as `devices/<device-name>/gps`.
- Delegates MQTT, GPS, and continuous publishing state to `MqttGpsPublisherManager`.

### MqttGpsPublisherManager.kt

`MqttGpsPublisherManager` is a process-level singleton that keeps long-running MQTT/GPS work alive while the user switches fragments.

Responsibilities:

- Connects to an MQTT broker with Eclipse Paho.
- Reads GPS updates through `FusedLocationProviderClient`.
- Starts continuous GPS publishing when the user taps Start Publish GPS.
- Stops continuous publishing when the user taps Stop Publish.
- Publishes each GPS update as JSON while publishing is active:

```json
{"lat":19.076,"lng":72.8777,"speed":45,"accuracy":8,"heading":120,"altitude":14}
```

The actual values are read from Android `Location`: latitude, longitude, speed, accuracy, bearing, and altitude.

Fragment switches must not disconnect MQTT or stop publishing. Only explicit Stop Publish or Disconnect actions should stop the active process.

## Build Configuration

The app is configured with:

| Item | Value |
| --- | --- |
| Kotlin | 1.9.0 |
| Android Gradle Plugin | 8.1.0 |
| Gradle project name | SimpleButtonApp |
| Namespace | com.example.simplebuttonapp |
| Application ID | com.example.simplebuttonapp |
| compileSdk | 33 |
| targetSdk | 33 |
| minSdk | 26 |
| Java source/target | 17 |
| Kotlin JVM target | 17 |

Main dependencies:

- `androidx.appcompat:appcompat:1.6.0`
- `androidx.core:core-ktx:1.10.1`
- `androidx.fragment:fragment-ktx:1.6.1`
- `com.google.android.material:material:1.9.0`
- `com.google.android.gms:play-services-location:21.0.1`
- `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`
- JUnit and Espresso test dependencies

## Development Guidelines

- Keep sensor listener registration in `onResume()` and cleanup in `onPause()`.
- Keep GPS updates tied to visible lifecycle state to avoid battery drain.
- Prefer string resources for user-visible text. The placeholder fragments currently hard-code text and are good candidates for cleanup.
- If adding real Settings or About screens, create layout XML files and wire strings through `strings.xml`.
- Use exact dependency versions to keep builds reproducible.
- Validate changes with `gradle assembleDebug` when possible.

## Common Commands

```bash
gradle assembleDebug
gradle clean assembleDebug
gradle test
gradle connectedAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.simplebuttonapp/.MainActivity
```

## Known Documentation Notes

This file reflects the actual code currently present in the repository.
