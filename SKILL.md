# homosep Skill

## Purpose

Use this skill when working on homosep, a Kotlin Android app that displays live device sensor data and GPS location through a fragment-based UI.

The current codebase is a BottomNavigationView app with Dashboard sensor/GPS data, Devices MQTT GPS publishing, Settings WebView content, and About USB serial communication.

## Current Application

homosep contains:

- One activity: `MainActivity`
- Four fragments:
  - `DashboardFragment`
  - `DevicesFragment`
  - `MqttGpsPublisherManager`
  - `SettingsFragment`
  - `AboutFragment`
- One activity layout: `activity_main.xml`
- One dashboard layout: `fragment_dashboard.xml`
- One devices layout: `fragment_devices.xml`
- One settings layout: `fragment_settings.xml`
- One about layout: `fragment_about.xml`
- One bottom navigation menu: `bottom_menu.xml`

## Architecture

```text
MainActivity
|-- FrameLayout: R.id.fragment_container
`-- BottomNavigationView: R.id.bottom_navigation
    |-- Dashboard -> DashboardFragment
    |-- Devices   -> DevicesFragment
    |-- Settings  -> SettingsFragment
    `-- About     -> AboutFragment
```

`MainActivity` owns navigation only. `DashboardFragment` owns sensor display behavior. `DevicesFragment` owns the MQTT UI. `MqttGpsPublisherManager` owns MQTT connection state, GPS updates, and continuous publishing so switching fragments does not interrupt active publishing. `SettingsFragment` owns the embedded WebView for `http://10.3.3.31:5000`. `AboutFragment` owns the USB serial console.

## Dashboard Behavior

`DashboardFragment` displays:

- Accelerometer values: X, Y, Z
- Gyroscope values: X, Y, Z
- Magnetometer values: X, Y, Z
- GPS values:
  - Status
  - Latitude
  - Longitude
  - Accuracy
  - Altitude

Sensor values update through `SensorEventListener.onSensorChanged(...)`.

GPS updates use:

- `FusedLocationProviderClient`
- `LocationCallback`
- `LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)`
- `Looper.getMainLooper()`

The GPS interval is currently 1000 ms.

## Lifecycle Rules

Keep this lifecycle pattern intact:

- Initialize views, sensors, and location callback in `onViewCreated(...)`.
- Register sensors in `onResume()`.
- Start location updates in `onResume()` only when fine location permission is granted.
- Unregister sensors in `onPause()`.
- Remove location updates in `onPause()`.

This prevents unnecessary battery use when the fragment is not active.

## Permissions

The manifest declares:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

`INTERNET` is required for MQTT broker connectivity. `DashboardFragment` and `DevicesFragment` use location permissions for GPS.

Current caveat:

- The code does not currently implement a permission result callback in `DashboardFragment`.
- If the permission flow is improved, prefer AndroidX Activity Result APIs or a fragment-scoped permission callback.

## Build Stack

| Component | Current Value |
| --- | --- |
| Kotlin | 1.9.0 |
| Android Gradle Plugin | 8.1.0 |
| compileSdk | 33 |
| targetSdk | 33 |
| minSdk | 26 |
| Java compatibility | 17 |
| Kotlin JVM target | 17 |

## Dependencies

```gradle
implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"
implementation "androidx.appcompat:appcompat:1.6.0"
implementation "androidx.core:core-ktx:1.10.1"
implementation "com.google.android.gms:play-services-location:21.0.1"
implementation "com.google.android.material:material:1.9.0"
implementation "androidx.fragment:fragment-ktx:1.6.1"
implementation "org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5"
implementation "com.github.mik3y:usb-serial-for-android:3.10.0"

testImplementation "junit:junit:4.13.2"
androidTestImplementation "androidx.test.ext:junit:1.1.5"
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
```

## Resource Notes

`fragment_dashboard.xml` uses a `ScrollView` with vertical sections for each sensor group and GPS.

`strings.xml` contains labels and format strings for Dashboard and navigation text. Some current string values contain encoding artifacts such as `m/sÃ‚Â²`, `ÃŽÂ¼T`, `Ã‚Â°`, `Ã‚Â±`, and `Ã¢Å“â€œ`. These appear to be mojibake versions of `m/sÂ²`, `ÂµT`, `Â°`, `Â±`, and a check mark. Fixing those is a source/resource cleanup task, not a documentation task.

`AboutFragment` inflates `fragment_about.xml` and provides USB serial communication controls.

## About USB Serial Console

`AboutFragment` supports external microcontrollers connected through OTG USB-to-TTL converters.

Controls:

- Scan USB Devices
- Device/port selector
- Baud rate selector
- Connect and Disconnect
- Message input
- Send Message
- Received Data viewer

The serial port is opened as 8 data bits, 1 stop bit, no parity. Receive data is read through `SerialInputOutputManager`.

## Settings WebView

`SettingsFragment` loads:

```text
http://10.3.3.31:5000
```

It uses a full-screen `WebView` with JavaScript and DOM storage enabled so the page is interactive. The app includes `network_security_config.xml` to allow cleartext HTTP traffic to `10.3.3.31`.

## Devices MQTT Publishing

`DevicesFragment` lets the user enter:

- Broker IP or link
- MQTT port
- Device name

The topic is generated as:

```text
devices/<device-name>/gps
```

For example, device name `xyz` becomes:

```text
devices/xyz/gps
```

The Start Publish GPS button begins continuous publishing. While active, every GPS update is sent as JSON until Stop Publish is tapped. This process is held by `MqttGpsPublisherManager`, so it keeps running if the user switches to Dashboard, Settings, or About:

```json
{"lat":19.076,"lng":72.8777,"speed":45,"accuracy":8,"heading":120,"altitude":14}
```

The app maps Android `Location.bearing` to `heading`.

## Common Changes

### Add a New Sensor

1. Add a sensor property to `DashboardFragment`.
2. Initialize it from `sensorManager.getDefaultSensor(...)`.
3. Register it in `onResume()`.
4. Handle it in `onSensorChanged(...)`.
5. Add TextViews to `fragment_dashboard.xml`.
6. Add labels and format strings to `strings.xml`.

### Change GPS Interval

Edit `DashboardFragment.startLocationUpdates()`:

```kotlin
LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
```

Change `1000` to the desired interval in milliseconds.

### Add Real Content to a Placeholder Tab

1. Create a layout XML file under `app/src/main/res/layout/`.
2. Move visible strings into `strings.xml`.
3. Inflate the layout from the fragment.
4. Keep fragment-specific behavior inside the fragment.

### Add a New Navigation Tab

1. Create a new fragment class.
2. Add a menu item to `bottom_menu.xml`.
3. Add a string resource for the tab title.
4. Add a branch in `MainActivity.setOnItemSelectedListener`.

## Commands

```bash
gradle assembleDebug
gradle clean assembleDebug
gradle test
gradle connectedAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.homosep/.MainActivity
```
