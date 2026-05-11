# SimpleButtonApp Configuration

## Project Identity

| Setting | Value |
| --- | --- |
| Project name | SimpleButtonApp |
| Module | app |
| Package namespace | com.example.simplebuttonapp |
| Application ID | com.example.simplebuttonapp |
| App label | SimpleButtonApp |
| Version code | 1 |
| Version name | 1.0 |

## Root Gradle Configuration

File: `build.gradle`

The root build file defines:

- Kotlin version: `1.9.0`
- Android Gradle Plugin: `8.1.0`
- Repositories: `google()` and `mavenCentral()`

```gradle
buildscript {
    ext.kotlin_version = "1.9.0"

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:8.1.0"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
```

## Settings

File: `settings.gradle`

```gradle
rootProject.name = "SimpleButtonApp"
include ':app'
```

## Gradle Properties

File: `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true

android.useAndroidX=true
android.enableJetifier=true
android.suppressUnsupportedCompileSdk=34

kotlin.code.style=official
```

Notes:

- The project uses Java 17, so no deprecated Java 8-era JVM arguments such as `MaxPermSize` are present.
- AndroidX is enabled.
- Jetifier is enabled for compatibility with any legacy support-library transitive dependencies.
- `android.suppressUnsupportedCompileSdk=34` is present, though the actual app `compileSdk` is 33.

## App Gradle Configuration

File: `app/build.gradle`

| Setting | Value |
| --- | --- |
| Plugin | `com.android.application` |
| Plugin | `kotlin-android` |
| namespace | `com.example.simplebuttonapp` |
| compileSdk | 33 |
| minSdk | 26 |
| targetSdk | 33 |
| Java sourceCompatibility | 17 |
| Java targetCompatibility | 17 |
| Kotlin jvmTarget | 17 |
| Release minifyEnabled | true |
| Release ProGuard | `proguard-android-optimize.txt`, `proguard-rules.pro` |
| Lint release checks | enabled |
| Lint warnings as errors | enabled |

## Dependencies

Runtime dependencies:

| Dependency | Purpose |
| --- | --- |
| `org.jetbrains.kotlin:kotlin-stdlib:1.9.0` | Kotlin runtime |
| `androidx.appcompat:appcompat:1.6.0` | `AppCompatActivity` and AppCompat theme support |
| `androidx.core:core-ktx:1.10.1` | Kotlin extensions and permission helpers |
| `com.google.android.gms:play-services-location:21.0.1` | Fused location provider and location callbacks |
| `com.google.android.material:material:1.9.0` | `BottomNavigationView` |
| `androidx.fragment:fragment-ktx:1.6.1` | Fragment support |
| `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5` | MQTT broker connectivity |

Test dependencies:

| Dependency | Purpose |
| --- | --- |
| `junit:junit:4.13.2` | Local unit tests |
| `androidx.test.ext:junit:1.1.5` | AndroidX instrumented JUnit |
| `androidx.test.espresso:espresso-core:3.5.1` | UI testing |

## Android Manifest

File: `app/src/main/AndroidManifest.xml`

The manifest declares:

- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `MainActivity` as the exported launcher activity
- AppCompat light theme: `@style/Theme.AppCompat.Light`
- Backup enabled through `android:allowBackup="true"`

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

`INTERNET` is required for MQTT. Location permissions are required for GPS and network-assisted location updates.

## Resources

### Layouts

| File | Purpose |
| --- | --- |
| `activity_main.xml` | Vertical root layout with `FrameLayout` fragment host and `BottomNavigationView` |
| `fragment_dashboard.xml` | Scrollable sensor and GPS display |
| `fragment_devices.xml` | MQTT broker/device-name input and GPS publish controls |
| `fragment_settings.xml` | Full-screen WebView for `http://10.3.3.31:5000` |

There is no XML layout for About. That fragment currently creates a placeholder `TextView` directly in Kotlin.

### XML

| File | Purpose |
| --- | --- |
| `network_security_config.xml` | Allows cleartext HTTP traffic to `10.3.3.31` |

### Menu

File: `app/src/main/res/menu/bottom_menu.xml`

Defines four navigation items:

- `R.id.nav_dashboard`
- `R.id.nav_devices`
- `R.id.nav_settings`
- `R.id.nav_about`

### Strings

File: `app/src/main/res/values/strings.xml`

Contains Dashboard section titles, sensor axis labels, GPS labels, GPS status text, format strings, and navigation labels.

Known cleanup item:

- Several strings contain mojibake characters caused by encoding conversion. They should be corrected in a resource cleanup pass.

### Colors

File: `app/src/main/res/values/colors.xml`

Contains default Material-style colors plus:

- `status_blue`
- `status_green`
- `status_red`

These status colors are used by the Dashboard section headers and GPS status text.

## ProGuard

File: `app/proguard-rules.pro`

The release build keeps:

- Android framework classes
- AndroidX classes
- Google Play Services classes
- App package classes
- Kotlin metadata
- Enum members and native method names

The file also strips debug/info/verbose logging calls from release builds through `-assumenosideeffects`.

## Build Commands

```bash
gradle assembleDebug
gradle clean assembleDebug
gradle assembleRelease
gradle test
gradle connectedAndroidTest
```

Install and launch:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.simplebuttonapp/.MainActivity
```

## Configuration Risks and Follow-Ups

- Runtime permission handling should be modernized so GPS starts immediately after a permission grant.
- Placeholder fragment text should move into `strings.xml`.
- Mojibake in string resources should be corrected.
- The project has build outputs checked into the workspace; generated files under `app/build/` should normally be ignored by version control.
