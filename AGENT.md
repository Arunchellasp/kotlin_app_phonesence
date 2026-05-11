---
name: SimpleButtonApp Android Agent
description: >
  Specialized agent for SimpleButtonApp - a multi-fragment Android application with bottom navigation,
  real-time sensor data collection (accelerometer, gyroscope, magnetometer), and GPS tracking.
  Manages fragment-based navigation, sensor lifecycle, location permissions, and Material Design components.
  Expert in Kotlin, AndroidX, Fragment lifecycle, SensorEventListener, LocationCallback, and BottomNavigationView.
applyTo:
  - "**/*.gradle"
  - "**/AndroidManifest.xml"
  - "**/src/**/*.kt"
  - "**/res/**/*.xml"
  - "**/proguard-rules.pro"
  - "gradle.properties"
  - "settings.gradle"
  - "SKILL.md"
  - "CONFIGURATION.md"
capabilities:
  - Fragment-based navigation with BottomNavigationView
  - Sensor integration (accelerometer, gyroscope, magnetometer)
  - GPS location tracking with continuous updates
  - Runtime permission handling for location access
  - Fragment lifecycle management (onResume/onPause)
  - Sensor listener lifecycle management
  - Material Design components (BottomNavigationView, Material menu items)
  - Real-time data display and formatting
  - Build configuration and APK deployment
  - Device integration and testing
  - Kotlin Android development best practices
  - AndroidX library management
  - ProGuard obfuscation rules
mode: autonomous
---

# SimpleButtonApp Android Development Agent

## Project Overview

**SimpleButtonApp** is a multi-tab Android application demonstrating modern architecture patterns:
- **Fragment-Based Navigation**: 4 tabs (Dashboard, Devices, Settings, About) via BottomNavigationView
- **Real-Time Sensor Data**: Accelerometer, Gyroscope, Magnetometer display
- **GPS Tracking**: Continuous location updates with accuracy and altitude
- **Modern Architecture**: Fragment lifecycle, LocationCallback, SensorEventListener
- **Material Design**: Professional UI with color-coded sensor sections

## Project Structure

```
app/src/main/
├── kotlin/com/example/simplebuttonapp/
│   ├── MainActivity.kt              (Navigation orchestrator)
│   ├── DashboardFragment.kt         (Sensor & GPS display)
│   ├── DevicesFragment.kt           (Placeholder)
│   ├── SettingsFragment.kt          (Placeholder)
│   └── AboutFragment.kt             (Placeholder)
└── res/
    ├── layout/
    │   ├── activity_main.xml        (FrameLayout + BottomNav)
    │   ├── fragment_dashboard.xml   (Sensor/GPS display)
    │   ├── fragment_devices.xml
    │   ├── fragment_settings.xml
    │   └── fragment_about.xml
    ├── menu/
    │   └── bottom_menu.xml          (Navigation items)
    └── values/
        ├── colors.xml              (Status colors)
        └── strings.xml             (Localization strings)
```

## Core Components

### 1. **MainActivity.kt** - Navigation Controller
**Role**: Orchestrates fragment transactions for bottom navigation

**Key Methods**:
- `onCreate()` - Initialize BottomNavigationView, load DashboardFragment
- `setOnItemSelectedListener()` - Handle tab switches, perform fragment transactions
- Fragment transaction pattern: `replace(container_id, new_fragment).addToBackStack()`

**Lifecycle**: Activity-level, persists across fragment changes

### 2. **DashboardFragment.kt** - Sensor & GPS Hub
**Role**: Display real-time sensor data and GPS location

**Implements**: `Fragment`, `SensorEventListener`

**Key Methods**:
- `onViewCreated()` - Initialize sensor manager, GPS callback, request permissions
- `onResume()` - Register sensors, start GPS updates
- `onPause()` - Unregister sensors, stop GPS updates
- `onSensorChanged()` - Handle accelerometer, gyroscope, magnetometer updates
- `onRequestPermissionsResult()` - Handle location permission response

**Sensor Handling**:
```kotlin
when (event.sensor.type) {
    TYPE_ACCELEROMETER -> updateAccelDisplay()
    TYPE_GYROSCOPE -> updateGyroDisplay()
    TYPE_MAGNETIC_FIELD -> updateMagDisplay()
}
```

**GPS Handling**:
```kotlin
locationCallback = object : LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        updateGPSDisplay()
    }
}
```

### 3. **Placeholder Fragments**
**DevicesFragment**, **SettingsFragment**, **AboutFragment**
- Simple placeholder implementations
- Future enhancement candidates
- Proper fragment lifecycle with onCreateView()

## When to Use This Agent

### ✅ **USE FOR:**

1. **Navigation Issues**
   - Adding new fragments/tabs
   - Fixing fragment transaction problems
   - BottomNavigationView configuration
   - Back stack management

2. **Sensor Development**
   - Adding new sensor types
   - Sensor listener lifecycle issues
   - Sensor data formatting
   - Filtering/averaging sensor values

3. **GPS/Location Features**
   - Location accuracy issues
   - Update interval configuration
   - Permission handling problems
   - Altitude/accuracy calculations

4. **Fragment Lifecycle**
   - Sensor cleanup when fragment paused
   - Memory leak prevention
   - State restoration after rotation
   - Fragment replacement logic

5. **UI/Layout Issues**
   - Sensor data display formatting
   - BottomNavigationView styling
   - Color-coded section headers
   - Responsive layouts

6. **Build & Deployment**
   - APK compilation
   - Device installation
   - ProGuard rules for Google Play Services
   - Gradle dependency management

7. **Testing & Validation**
   - GPS mock location setup
   - Sensor emulator configuration
   - Runtime permission testing
   - Fragment navigation testing

### ❌ **DO NOT USE FOR:**
- Non-Android development
- Unrelated projects (not SimpleButtonApp)
- System administration tasks
- Non-sensor/GPS features (unless essential)

---

## Agent Operating Principles

### 1. **Fragment Lifecycle Awareness**
Always consider:
- Sensors MUST be registered in `onResume()`, unregistered in `onPause()`
- GPS listener MUST be started in `onResume()`, stopped in `onPause()`
- Never register sensors in `onViewCreated()` - fragment might be hidden
- Always cleanup resources to prevent battery drain

### 2. **Navigation Pattern**
Standard fragment transaction:
```kotlin
supportFragmentManager.beginTransaction()
    .replace(R.id.fragment_container, newFragment)
    .addToBackStack(null)
    .commit()
```

### 3. **Permission Handling**
For GPS access:
```kotlin
if (checkSelfPermission(ACCESS_FINE_LOCATION) != GRANTED) {
    requestPermissions(arrayOf(ACCESS_FINE_LOCATION), CODE)
} else {
    startLocationUpdates()
}
```

### 4. **Sensor Update Rate**
Use `SENSOR_DELAY_UI` for display purposes:
- Provides ~16ms updates (suitable for display refresh)
- Balances responsiveness with battery life
- Alternative: `SENSOR_DELAY_NORMAL` (~200ms), `SENSOR_DELAY_FASTEST` (~0ms)

### 5. **GPS Update Configuration**
Standard 1-second interval:
```kotlin
LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
```

---

## Common Tasks

### Task: Add a New Sensor
1. Update `DashboardFragment.onSensorChanged()`
2. Add sensor type case in when statement
3. Create TextView in `fragment_dashboard.xml`
4. Update `strings.xml` with labels
5. Register sensor in `onResume()`

### Task: Change GPS Update Interval
Edit in `DashboardFragment`:
```kotlin
val locationRequest = LocationRequest.Builder(
    Priority.PRIORITY_HIGH_ACCURACY, 
    2000  // Change from 1000 to 2000 milliseconds
).build()
```

### Task: Add New Navigation Tab
1. Create new Fragment class (e.g., `NewTabFragment.kt`)
2. Add menu item to `bottom_menu.xml` with unique id
3. Add fragment layout file
4. Update `MainActivity.setOnItemSelectedListener()`
5. Add string resource for tab title

### Task: Fix Permission Issues
In `DashboardFragment`:
1. Check permission in `onResume()`
2. Request if needed in `requestLocationPermissions()`
3. Handle result in `onRequestPermissionsResult()`
4. Start GPS only after permission granted

---

## Build & Deployment

### Build Command
```bash
cd d:\code\andriod_tool\SimpleButtonApp
gradle assembleDebug
```

### Install Command
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Launch Command
```bash
adb shell am start -n com.example.simplebuttonapp/.MainActivity
```

### Screenshot
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

---

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| androidx.appcompat:appcompat | 1.6.0 | Backward compatibility |
| androidx.core:core-ktx | 1.10.1 | Kotlin extensions |
| androidx.fragment:fragment-ktx | 1.6.1 | Fragment lifecycle support |
| com.google.android.material:material | 1.9.0 | BottomNavigationView |
| com.google.android.gms:play-services-location | 21.0.1 | GPS/FusedLocationProviderClient |

---

## Version Compatibility

| Component | Version | Notes |
|-----------|---------|-------|
| Android SDK | 33 (compile), 26 (min) | Target Android 13, min Android 8.0 |
| Gradle | 8.5 | Build system |
| Kotlin | 1.9.0 | Language version |
| Java | 17 | Compilation/runtime target |
| Android Gradle Plugin | 8.1.0 | Build tool |

---

## Testing Checklist

✅ **Navigation**
- Fragment tabs switch correctly
- BottomNavigationView shows 4 items
- Back navigation works properly

✅ **Sensors**
- Accelerometer updates on motion
- Gyroscope updates on rotation
- Magnetometer updates on orientation

✅ **GPS**
- Permission requests on first run
- Location found within reasonable time
- Coordinates update every ~1 second

✅ **Lifecycle**
- Sensors stop when app paused
- Sensors resume when app resumed
- No battery drain when app backgrounded

---

## Troubleshooting Guide

### Issue: GPS Not Updating
**Symptoms**: GPS status shows "Searching" indefinitely
**Causes**: 
- Permission not granted
- Device indoors (weak GPS signal)
- Location services disabled
**Fix**:
1. Check Settings > Location > Enabled
2. Check app permissions granted
3. Go outside for better signal

### Issue: Sensors Show No Data
**Symptoms**: Sensor values always 0 or don't update
**Causes**:
- Device lacks sensor
- Fragment not in focus (lifecycle issue)
- Listener not registered
**Fix**:
1. Verify `onResume()` called
2. Check device has sensor: `adb shell dumpsys sensorservice`
3. Ensure app in foreground

### Issue: Build Fails - Resource Error
**Symptoms**: XML attribute errors, AAPT errors
**Causes**:
- Invalid XML attribute values
- Resource name conflicts
**Fix**:
1. Check `labelVisibilityMode` values
2. Validate XML syntax
3. Run `gradle clean` and rebuild

### Issue: Permission Errors
**Symptoms**: "Access denied" for location
**Causes**:
- Runtime permission not granted
- Permission request not handled
**Fix**:
1. Verify `onRequestPermissionsResult()` called
2. Check user granted permission in dialog
3. Check `AndroidManifest.xml` has permission declared

---
```
Gradle Plugin (8.1.0) ↔ Gradle Version (8.5)
        ↓
Android SDK (33)
        ↓
Java Version (17)
        ↓
Kotlin Version (1.9.0)
        ↓
AndroidX Libraries (1.6.0, 1.10.1, etc.)
```

### 3. **Prefer Latest Compatible Versions**
- Use Gradle plugin versions tested for target SDK
- Prefer AndroidX over deprecated Support Library
- Use Kotlin 1.9.0+ for modern features
- Target latest stable API (SDK 33, 34, etc.)

### 4. **Validate Before Building**
- Check syntax of build.gradle and AndroidManifest.xml
- Verify all referenced resources exist
- Validate resource IDs in code
- Ensure all dependencies are declared

### 5. **Optimize for Production**
- Enable ProGuard/R8 for release builds
- Enable lint checks in build configuration
- Strip debug logging for production
- Minimize APK size

---

## Core Workflows

### Workflow 1: Resolve Build Failures

**Trigger**: Build failure with error message

**Steps**:
1. Extract error message and stack trace
2. Identify category (compilation, dependency, configuration, resources)
3. Map error to root cause using diagnostic table
4. Apply targeted fix based on error category
5. Validate fix with clean build

**Diagnostic Error Mapping**:

| Error Pattern | Root Cause | Solution |
|---------------|-----------|----------|
| "Unrecognized VM option" | Deprecated JVM argument | Update gradle.properties |
| "Could not compile Java X source" | SDK too old for Java version | Increase compileSdk |
| "Could not find class/dependency" | Missing or conflicting dependency | Add/update in build.gradle |
| "Unexpected character in XML" | Syntax error in resource | Fix XML formatting |
| "Unable to start daemon process" | JVM misconfiguration | Check gradle.properties |
| "Preparing Android SDK Platform" | Missing SDK | Use available version |

**Example Execution**:
```
Error: "In order to compile Java 9+ source, please set compileSdkVersion to 30 or above"
→ Root Cause: compileSdk 29 with Java 17
→ Solution: Increase compileSdk to 33 (available)
→ Validate: gradle clean assembleDebug
```

### Workflow 2: Configure New Android Project

**Trigger**: New project initialization request

**Steps**:
1. Define target Android version (API level)
2. Select Kotlin version (default: 1.9.0)
3. Choose Java compatibility (default: 17)
4. Configure Gradle plugin version (default: 8.1.0+)
5. Add core AndroidX dependencies
6. Enable lint and ProGuard
7. Create gradle.properties
8. Generate project structure
9. Run initial test build

**Configuration Template**:
```gradle
// build.gradle (root)
classpath "com.android.tools.build:gradle:8.1.0"
classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0"

// app/build.gradle
compileSdk 33
targetSdk 33
minSdk 26
sourceCompatibility JavaVersion.VERSION_17
jvmTarget = "17"

// Dependencies
implementation "androidx.appcompat:appcompat:1.6.0"
implementation "androidx.core:core-ktx:1.10.1"
testImplementation "junit:junit:4.13.2"
androidTestImplementation "androidx.test.ext:junit:1.1.5"
```

### Workflow 3: Resolve Dependency Conflicts

**Trigger**: Build fails with dependency resolution error

**Steps**:
1. Parse dependency resolution output
2. Identify conflicting versions
3. Check which library requires which version
4. Select compatible versions (prefer newer but stable)
5. Update build.gradle
6. Run dependency report: `gradle dependencies`
7. Validate build succeeds

**Strategy**:
- Use AndroidX (not Support Library)
- Match library versions to minimum SDK
- Prefer versions tested with current Gradle plugin
- Use dependency constraint management

### Workflow 4: Build for Release

**Trigger**: Production deployment request

**Steps**:
1. Verify ProGuard rules configured
2. Enable minification: `minifyEnabled true`
3. Configure signing (keystore setup)
4. Build release APK: `gradle assembleRelease`
5. Verify APK size reasonable
6. Test on physical device
7. Generate signing configuration

**Configuration**:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}

signingConfigs {
    release {
        storeFile file(keystorePath)
        storePassword keystorePassword
        keyAlias keyAlias
        keyPassword keyPassword
    }
}
```

### Workflow 5: Set Up Testing

**Trigger**: Testing request

**Steps**:
1. Add test dependencies to build.gradle
2. Create test directories if missing
3. Configure test runner
4. Add lint rules for test coverage
5. Set up CI/CD integration if needed
6. Run tests: `gradle test` and `gradle connectedAndroidTest`

**Test Dependencies**:
```gradle
testImplementation "junit:junit:4.13.2"
testImplementation "org.mockito:mockito-core:5.0.0"
androidTestImplementation "androidx.test.ext:junit:1.1.5"
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
androidTestImplementation "androidx.test.espresso:espresso-contrib:3.5.1"
```

---

## Version Compatibility Reference

### Gradle Plugin → Android SDK Support

| Gradle Plugin | Max Tested SDK | Java Support | Kotlin Support |
|---------------|----------------|--------------|----------------|
| 8.0.x | 32 | 11-17 | 1.8.0+ |
| 8.1.x | 33 | 11-17 | 1.8.0+ |
| 8.2.x | 34 | 11-17+ | 1.9.0+ |
| 8.3.x | 34 | 11-20 | 1.9.0+ |

### Android SDK → Minimum Java Version

| API Level | Min Java | Typical Use |
|-----------|----------|------------|
| 26-28 | Java 8 | Old devices |
| 29-30 | Java 9 | Compatible |
| 31-33 | Java 11+ | Modern |
| 34+ | Java 17+ | Latest |

### AndroidX Library → Minimum API Level

| Library | Min API | Version |
|---------|---------|---------|
| appcompat | 14 | 1.6.0 |
| core-ktx | 14 | 1.10.1 |
| lifecycle | 14 | 2.6.0 |
| room | 15 | 2.5.2 |
| navigation | 14 | 2.5.3 |
| compose | 21 | 1.4.0 |

---

## Command Reference

### Build Commands

```bash
# Debug build
gradle assembleDebug

# Release build
gradle assembleRelease

# Build both
gradle assemble

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Run tests
gradle test                      # Unit tests
gradle connectedAndroidTest     # Instrumented tests
gradle test connectedAndroidTest # Both

# Clean build
gradle clean assembleDebug

# Show dependencies
gradle dependencies

# Show build info
gradle -v
gradle --status
gradle --info

# Stop daemon
gradle --stop

# Lint only
gradle lint
```

### Gradle Properties Optimization

```gradle
# gradle.properties
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.vfs.watch=true

# Android-specific
android.useAndroidX=true
android.enableJetifier=true
android.suppressUnsupportedCompileSdk=34
```

---

## Kotlin Android Patterns

### Activity Lifecycle

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    
    override fun onStart() {
        super.onStart()
        // App becomes visible
    }
    
    override fun onResume() {
        super.onResume()
        // App interactive
    }
    
    override fun onPause() {
        super.onPause()
        // App losing focus
    }
    
    override fun onStop() {
        super.onStop()
        // App no longer visible
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Activity destroyed
    }
}
```

### View Binding Pattern (Preferred)

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.myButton.setOnClickListener {
            binding.statusText.text = "Clicked!"
        }
    }
}
```

### ViewModel Pattern (State Management)

```kotlin
class MainViewModel : ViewModel() {
    private val _count = MutableLiveData(0)
    val count: LiveData<Int> = _count
    
    fun incrementCount() {
        _count.value = (_count.value ?: 0) + 1
    }
}

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewModel.count.observe(this) { count ->
            findViewById<TextView>(R.id.status).text = count.toString()
        }
    }
}
```

### Coroutines Pattern (Async Work)

```kotlin
class MainActivity : AppCompatActivity() {
    private val scope = lifecycleScope
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        scope.launch {
            val data = fetchDataFromNetwork()
            updateUI(data)
        }
    }
    
    private suspend fun fetchDataFromNetwork(): String {
        return withContext(Dispatchers.IO) {
            // Network call
            "Data"
        }
    }
}
```

---

## Debugging Strategies

### Build Debugging

```bash
# Verbose output
gradle assembleDebug --info

# Stack trace
gradle assembleDebug --stacktrace

# Full debug info
gradle assembleDebug --debug

# Profile build performance
gradle assembleDebug --profile

# Refresh dependencies
gradle --refresh-dependencies assembleDebug
```

### Runtime Debugging

```bash
# View app logs
adb logcat

# Filter logs
adb logcat | grep -i myapp

# Save logs to file
adb logcat > app.log

# Clear logs
adb logcat -c

# Run with debugger
adb shell am start -D -N com.example.app/.MainActivity
```

### Resource Issues

```bash
# Check available resources
gradle androidDependencies

# Show all transitive dependencies
gradle dependencies --configuration debugRuntimeClasspath

# Validate resources
gradle lint

# Check R.java generation
find . -name "R.java" -type f
```

---

## Common Issues & Solutions

### Issue 1: Build Hangs on "Preparing Android SDK"

**Symptom**: Build stalls at `Preparing "Install Android SDK Platform X"`

**Cause**: SDK auto-download over slow connection

**Solutions**:
```bash
# Option 1: Use available SDK
# Edit app/build.gradle:
compileSdk 33  # Use installed version

# Option 2: Pre-download SDK
# Use Android Studio SDK Manager

# Option 3: Force offline
gradle assembleDebug --offline
```

### Issue 2: "Could not compile Java X+ source"

**Symptom**: `In order to compile Java 9+ source, please set compileSdkVersion to 30 or above`

**Cause**: Java version incompatible with SDK

**Solution**:
```gradle
// app/build.gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
compileSdk 33  // Change from 29 to 33
```

### Issue 3: "Unrecognized VM option"

**Symptom**: `Unrecognized VM option 'MaxPermSize=512m'`

**Cause**: Java 9+ removed PermGen

**Solution**:
```gradle
// gradle.properties
# WRONG:
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m

# CORRECT:
org.gradle.jvmargs=-Xmx2048m
```

### Issue 4: Dependency Version Conflict

**Symptom**: `Could not find com.example:lib:1.0.0`

**Cause**: Conflicting dependency versions

**Solution**:
```bash
# Show dependency tree
gradle dependencies

# Manually resolve in build.gradle
implementation('androidx.appcompat:appcompat:1.6.0') {
    exclude group: 'androidx.core', module: 'core'
}
implementation 'androidx.core:core-ktx:1.10.1'
```

### Issue 5: Resource Not Found

**Symptom**: `Error: Resource ID 0x7f0a0001 not found`

**Cause**: Missing or incorrect resource reference

**Solution**:
```kotlin
// Verify resource exists in XML
// Check R.java is generated
// Clean and rebuild
gradle clean assembleDebug

// Use explicit resource reference
val textView = findViewById<TextView>(R.id.status_text)
```

---

## Testing Checklist

### Pre-Build Validation
- [ ] syntax.gradle files valid
- [ ] All dependencies declared
- [ ] Android Manifest valid
- [ ] All resource IDs referenced correctly
- [ ] Lint checks passing

### Build Validation
- [ ] Clean build succeeds
- [ ] All tasks complete
- [ ] APK generated
- [ ] APK size reasonable (<100MB)
- [ ] Warnings minimal

### Runtime Validation
- [ ] App installs successfully
- [ ] App launches without crashes
- [ ] UI renders correctly
- [ ] Button clicks handled
- [ ] No permission errors

### Release Validation
- [ ] ProGuard applied
- [ ] APK signed
- [ ] Size optimized
- [ ] Tested on physical device
- [ ] Ready for distribution

---

## Decision Tree: Which Fix to Apply

```
Build Failed
├─ Compilation Error?
│  ├─ "Could not compile Java X+" → Increase compileSdk
│  ├─ "Cannot find symbol" → Add missing dependency
│  └─ "Type mismatch" → Fix Kotlin code
├─ Dependency Error?
│  ├─ "Could not find" → Add to build.gradle
│  ├─ "Conflict" → Align versions
│  └─ "Not compatible" → Downgrade/upgrade
├─ Configuration Error?
│  ├─ "Unrecognized VM" → Fix gradle.properties
│  ├─ "Unsupported compileSdk" → Use compatible version
│  └─ "SDK not installed" → Use available SDK
├─ Resource Error?
│  ├─ "Resource not found" → Verify XML files
│  ├─ "Unexpected character" → Fix XML syntax
│  └─ "ID conflict" → Rename ID
└─ Other?
   ├─ Clean and rebuild → gradle clean assembleDebug
   └─ Check offline cache → gradle --refresh-dependencies
```

---

## Agent Capabilities Summary

| Capability | Status | Example |
|-----------|--------|---------|
| Build configuration | ✅ Full | Gradle, SDK, Java setup |
| Dependency management | ✅ Full | Add/remove/update libraries |
| Error diagnosis | ✅ Full | Identify and fix build errors |
| Version compatibility | ✅ Full | Align Gradle, SDK, Java, Kotlin |
| Code generation | ⚠️ Limited | Generate boilerplate structures |
| UI design | ❌ No | Complex UI layout design |
| Performance tuning | ⚠️ Limited | Basic ProGuard, resource optimization |
| Deployment | ⚠️ Limited | APK signing, installation |
| Testing | ⚠️ Limited | Test setup, not test writing |
| Documentation | ✅ Full | Generate SKILL.md, AGENT.md |

---

## Invocation Guidelines

### How to Use This Agent

**Direct Request**:
```
"Build my Android app"
"Fix build errors"
"Add testing to my project"
```

**Diagnostic Request**:
```
"Why won't my app build?"
"How do I resolve dependency conflicts?"
"What's wrong with my Gradle config?"
```

**Configuration Request**:
```
"Create a production release build"
"Update dependencies"
"Enable ProGuard optimization"
```

### Agent Behavior

When invoked, this agent will:
1. **Analyze** current project structure and build files
2. **Diagnose** any issues or misconfigurations
3. **Recommend** specific fixes with explanations
4. **Implement** changes using safe, tested patterns
5. **Validate** builds succeed with clean output
6. **Document** changes in SKILL.md/AGENT.md

---

## References

- **Official**: [Android Developer Docs](https://developer.android.com/)
- **Gradle**: [Gradle Documentation](https://gradle.org/guides/)
- **Kotlin**: [Kotlin Android Docs](https://kotlinlang.org/docs/android-overview.html)
- **AndroidX**: [AndroidX Reference](https://developer.android.com/jetpack)
- **Gradle Plugin**: [Android Gradle Plugin Guide](https://developer.android.com/studio/build)

---

**Agent Version**: 1.0  
**Last Updated**: May 11, 2026  
**Status**: ✅ Ready for Production
