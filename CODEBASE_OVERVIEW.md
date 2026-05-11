# SimpleButtonApp Codebase Overview

## What This App Is

SimpleButtonApp is a Kotlin Android app for displaying real-time device sensor readings and GPS location data. It uses a single activity with bottom navigation and multiple fragments.

This overview reflects the current source code in the repository.

## High-Level Structure

```text
SimpleButtonApp
|-- MainActivity
|   `-- Hosts bottom navigation and swaps fragments
|
|-- DashboardFragment
|   |-- Displays accelerometer values
|   |-- Displays gyroscope values
|   |-- Displays magnetometer values
|   `-- Displays GPS status and location values
|
|-- DevicesFragment
|   `-- MQTT GPS publisher UI
|
|-- MqttGpsPublisherManager
|   `-- Long-running MQTT connection, GPS updates, and publishing state
|
|-- SettingsFragment
|   `-- Interactive WebView for http://10.3.3.31:5000
|
`-- AboutFragment
    `-- Placeholder TextView
```

## Authored Files

```text
build.gradle
settings.gradle
gradle.properties
AGENT.md
SKILL.md
CONFIGURATION.md
CODEBASE_OVERVIEW.md
app/build.gradle
app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/kotlin/com/example/simplebuttonapp/MainActivity.kt
app/src/main/kotlin/com/example/simplebuttonapp/DashboardFragment.kt
app/src/main/kotlin/com/example/simplebuttonapp/DevicesFragment.kt
app/src/main/kotlin/com/example/simplebuttonapp/MqttGpsPublisherManager.kt
app/src/main/kotlin/com/example/simplebuttonapp/SettingsFragment.kt
app/src/main/kotlin/com/example/simplebuttonapp/AboutFragment.kt
app/src/main/res/layout/activity_main.xml
app/src/main/res/layout/fragment_dashboard.xml
app/src/main/res/layout/fragment_devices.xml
app/src/main/res/layout/fragment_settings.xml
app/src/main/res/xml/network_security_config.xml
app/src/main/res/menu/bottom_menu.xml
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
```

Generated files live under `app/build/` and should not be used as source of truth.

## MainActivity

File: `app/src/main/kotlin/com/example/simplebuttonapp/MainActivity.kt`

`MainActivity` is the navigation host.

It does the following:

- Calls `setContentView(R.layout.activity_main)`.
- Finds `BottomNavigationView` by `R.id.bottom_navigation`.
- Loads `DashboardFragment` when `savedInstanceState == null`.
- Handles bottom navigation item selections.
- Replaces the fragment container with:
  - `DashboardFragment`
  - `DevicesFragment`
  - `SettingsFragment`
  - `AboutFragment`
- Adds each selected fragment transaction to the back stack.

## DashboardFragment

File: `app/src/main/kotlin/com/example/simplebuttonapp/DashboardFragment.kt`

`DashboardFragment` is the main functional fragment.

It implements:

```kotlin
Fragment(), SensorEventListener
```

### Sensors

The fragment initializes:

- `SensorManager`
- default accelerometer
- default gyroscope
- default magnetometer

It registers all available sensors in `onResume()` with:

```kotlin
SensorManager.SENSOR_DELAY_UI
```

It unregisters the listener in `onPause()`.

Sensor events update these TextViews:

- `accelXText`, `accelYText`, `accelZText`
- `gyroXText`, `gyroYText`, `gyroZText`
- `magXText`, `magYText`, `magZText`

Values are formatted with:

```kotlin
R.string.sensor_value_format
```

### GPS

The fragment initializes:

- `FusedLocationProviderClient`
- `LocationCallback`

It requests high-accuracy updates with:

```kotlin
LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
```

Location updates populate:

- `latitudeText`
- `longitudeText`
- `accuracyText`
- `altitudeText`
- `gpsStatusText`

Updates are removed in `onPause()`.

### Permission Handling

The fragment checks `ACCESS_FINE_LOCATION`. If missing, it requests both:

- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

Current limitation:

- There is no permission result callback in the current fragment code. A user granting permission may need a resume cycle before GPS starts.

## DevicesFragment

File: `app/src/main/kotlin/com/example/simplebuttonapp/DevicesFragment.kt`

`DevicesFragment` provides the MQTT GPS publishing UI.

It lets the user enter:

- Broker IP or link
- Port
- Device name

The fragment generates the MQTT topic as:

```text
devices/<device-name>/gps
```

For device name `xyz`, the topic becomes:

```text
devices/xyz/gps
```

The fragment delegates long-running work to `MqttGpsPublisherManager`.

## MqttGpsPublisherManager

File: `app/src/main/kotlin/com/example/simplebuttonapp/MqttGpsPublisherManager.kt`

`MqttGpsPublisherManager` is a singleton that keeps MQTT, GPS, and publishing state outside fragment lifecycle. This prevents active publishing from stopping when the user switches tabs.

It collects GPS through `FusedLocationProviderClient`, connects to the broker with Eclipse Paho, and continuously publishes GPS readings after Start Publish GPS is tapped. Publishing continues until Stop Publish or Disconnect is tapped. Each message is JSON:

```json
{"lat":19.076,"lng":72.8777,"speed":45,"accuracy":8,"heading":120,"altitude":14}
```

`heading` is populated from Android `Location.bearing`.

## SettingsFragment

File: `app/src/main/kotlin/com/example/simplebuttonapp/SettingsFragment.kt`

`SettingsFragment` loads this local web app in a full-screen WebView:

```text
http://10.3.3.31:5000
```

The WebView enables JavaScript, DOM storage, wide viewport layout, and in-WebView navigation. The app includes `network_security_config.xml` so Android allows cleartext HTTP traffic to `10.3.3.31`.

## Placeholder Fragments

Files:

- `AboutFragment.kt`

Each fragment currently returns a `TextView` from `onCreateView(...)` with hard-coded placeholder text and padding.

These are intentionally minimal and are candidates for future XML layouts and resource-backed strings.

## Layouts

### activity_main.xml

Root layout:

- Vertical `LinearLayout`
- `FrameLayout` with ID `fragment_container`
- `BottomNavigationView` with ID `bottom_navigation`

The `FrameLayout` uses `layout_weight="1"` so it takes all space above the bottom nav.

### fragment_dashboard.xml

Scrollable vertical layout with sections:

- Accelerometer
- Gyroscope
- Magnetometer
- GPS Data

The sensor sections use simple horizontal rows with labels on the left and values aligned to the right.

### fragment_devices.xml

Scrollable MQTT publisher layout with:

- Broker input
- Port input
- Device-name input
- Topic preview
- Connect and Disconnect buttons
- Start Publish GPS and Stop Publish buttons
- Status, latest location, and payload text

### fragment_settings.xml

Full-screen WebView used by `SettingsFragment`.

## Navigation Menu

File: `app/src/main/res/menu/bottom_menu.xml`

Navigation IDs:

- `nav_dashboard`
- `nav_devices`
- `nav_settings`
- `nav_about`

The menu uses Android built-in drawable icons.

## Resources

### strings.xml

Contains:

- App name
- Sensor section titles
- Sensor axis labels
- GPS status labels
- GPS format strings
- Navigation labels

Known issue:

- Some unit symbols are encoded incorrectly in the current XML. For example, `m/sÂ²` should likely be `m/s²`, and `Î¼T` should likely be `µT`.

### colors.xml

Contains Material-style colors plus status colors:

- Blue for GPS and gyroscope
- Green for accelerometer
- Red for magnetometer

## Build and Runtime Configuration

| Item | Value |
| --- | --- |
| Kotlin | 1.9.0 |
| Android Gradle Plugin | 8.1.0 |
| compileSdk | 33 |
| targetSdk | 33 |
| minSdk | 26 |
| Java target | 17 |
| Kotlin JVM target | 17 |
| Location library | Google Play Services Location 21.0.1 |
| UI library | Material Components 1.9.0 |
| Fragment library | AndroidX Fragment KTX 1.6.1 |
| MQTT library | Eclipse Paho MQTT Client 1.2.5 |
| Settings page | http://10.3.3.31:5000 |

## Build Commands

```bash
gradle assembleDebug
gradle clean assembleDebug
gradle assembleRelease
gradle test
gradle connectedAndroidTest
```

## Recommended Next Improvements

- Fix permission result handling in `DashboardFragment`.
- Move About placeholder fragment text into `strings.xml`.
- Add XML layouts for Devices, Settings, and About when those tabs gain real UI.
- Correct encoding artifacts in `strings.xml`.
- Consider avoiding `addToBackStack(null)` for bottom navigation tab switches if standard tab behavior is desired.
- Add tests for navigation and permission behavior.
