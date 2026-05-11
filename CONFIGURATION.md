# Configuration Documentation - SimpleButtonApp

**Project**: SimpleButtonApp  
**Date**: May 11, 2026  
**Status**: Production Ready  

---

## Table of Contents

1. [Project-Level Configuration](#project-level-configuration)
2. [App-Level Configuration](#app-level-configuration)
3. [Gradle Properties](#gradle-properties)
4. [Android Manifest](#android-manifest)
5. [Build Types](#build-types)
6. [Dependencies](#dependencies)
7. [Resource Configuration](#resource-configuration)
8. [ProGuard Configuration](#proguard-configuration)
9. [Version Reference](#version-reference)
10. [Configuration Checklist](#configuration-checklist)

---

## Project-Level Configuration

### File: `build.gradle` (Root)

Located at: `d:\code\andriod_tool\SimpleButtonApp\build.gradle`

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

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

#### Configuration Breakdown

| Setting | Value | Purpose |
|---------|-------|---------|
| `ext.kotlin_version` | 1.9.0 | Global Kotlin version variable |
| `classpath gradle` | 8.1.0 | Android Gradle Plugin version |
| `repositories` | google(), mavenCentral() | Maven repositories for dependencies |

#### Key Settings Explained

**Kotlin Version (1.9.0)**
- Latest stable Kotlin version as of this project
- Compatible with Java 17 target
- Supports modern Kotlin features (data classes, extension functions, coroutines)
- Used globally across all modules

**Android Gradle Plugin (8.1.0)**
- Version tested up to Android SDK 33
- Supports Java 11-17
- Compatible with Gradle 8.5
- Latest stable for this SDK level
- **Note**: SDK 34 requires plugin 8.2.0+

**Repositories Configuration**
- `google()` - Official Google repository for Android libraries
- `mavenCentral()` - Central Maven repository for third-party libraries
- Order matters: Google checked first for Android-specific packages

---

## App-Level Configuration

### File: `app/build.gradle`

Located at: `d:\code\andriod_tool\SimpleButtonApp\app\build.gradle`

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    namespace 'com.example.simplebuttonapp'

    compileSdk 33

    defaultConfig {
        applicationId "com.example.simplebuttonapp"
        minSdk 26
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkReleaseBuilds true
        warningsAsErrors true
    }
}

dependencies {
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"

    implementation "androidx.appcompat:appcompat:1.6.0"
    implementation "androidx.core:core-ktx:1.10.1"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}
```

### Plugin Configuration

```gradle
plugins {
    id 'com.android.application'  // Android app plugin
    id 'kotlin-android'            // Kotlin support
}
```

| Plugin | Purpose |
|--------|---------|
| `com.android.application` | Applies Android app build configuration |
| `kotlin-android` | Enables Kotlin language support |

### Android Block - Core Settings

#### Namespace
```gradle
namespace 'com.example.simplebuttonapp'
```
- Java package name for the application
- Must be unique across all Android apps
- Used to generate R.java resource file
- Cannot be changed after initial release on Play Store

#### SDK Versions
```gradle
compileSdk 33        // Build target SDK
minSdk 26            // Minimum supported Android version
targetSdk 33         // Optimized for this Android version
```

**SDK Version Details**:

| Setting | Value | Meaning |
|---------|-------|---------|
| `compileSdk` | 33 | Android 13 - Used for compilation |
| `targetSdk` | 33 | Android 13 - Optimizations applied |
| `minSdk` | 26 | Android 8.0 - Oldest supported version |

**Compatibility Impact**:
- **compileSdk 33**: Requires Gradle plugin 8.1.0+ (supports up to 33)
- **targetSdk 33**: App optimized for Android 13 features and behaviors
- **minSdk 26**: App runs on Android 8.0+ devices (~95% of Google Play)

#### Version Information
```gradle
applicationId "com.example.simplebuttonapp"  // Unique identifier
versionCode 1                                   // Integer version (incremented per release)
versionName "1.0"                               // User-visible version string
```

| Setting | Value | Purpose |
|---------|-------|---------|
| `applicationId` | com.example... | Unique package identifier (immutable) |
| `versionCode` | 1 | Internal version number for Play Store ordering |
| `versionName` | 1.0 | Display version for users |

**Versioning Strategy**:
- `versionCode`: Must increase with each release (1, 2, 3, ...)
- `versionName`: Can be any format (1.0, 1.1, 2.0.0, etc.)
- Play Store uses `versionCode` for update detection

### Compilation Options

#### Java Compatibility
```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
```

| Setting | Value | Impact |
|---------|-------|--------|
| `sourceCompatibility` | Java 17 | Code written in Java 17 syntax |
| `targetCompatibility` | Java 17 | Bytecode generated for Java 17 VM |

**Version Requirement**: Java 17 requires SDK 30 or higher (we use SDK 33)

#### Kotlin Configuration
```gradle
kotlinOptions {
    jvmTarget = "17"
}
```

- Matches Java target version
- Enables Java 17 features in Kotlin code
- Must align with `targetCompatibility`

### Lint Configuration

```gradle
lint {
    checkReleaseBuilds true    // Enable lint for release builds
    warningsAsErrors true      // Fail build on warnings
}
```

| Setting | Value | Effect |
|---------|-------|--------|
| `checkReleaseBuilds` | true | Lint checked before release |
| `warningsAsErrors` | true | Build fails on lint warnings |

**Benefits**:
- Catches potential issues early
- Enforces code quality standards
- Prevents bad code from reaching production

---

## Gradle Properties

### File: `gradle.properties`

Located at: `d:\code\andriod_tool\SimpleButtonApp\gradle.properties`

```gradle
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true

# Android-specific properties
android.useAndroidX=true
android.enableJetifier=true
android.suppressUnsupportedCompileSdk=34

# Kotlin
kotlin.code.style=official
```

### JVM Arguments

```gradle
org.gradle.jvmargs=-Xmx2048m
```

| Argument | Value | Purpose |
|----------|-------|---------|
| `-Xmx` | 2048m | Maximum heap size (2GB) |

**Explanation**:
- JVM starts with up to 2GB RAM
- Prevents out-of-memory errors during large builds
- Java 17 doesn't support `-XX:MaxPermSize` (was for Java 8)

**Troubleshooting**:
- Increase to 3048m on high-end machines with large projects
- Decrease if testing on limited RAM systems
- ❌ NEVER use: `-XX:MaxPermSize` (removed in Java 9+)

### Build Performance

```gradle
org.gradle.parallel=true       # Compile modules in parallel
org.gradle.caching=true        # Cache task outputs
```

| Setting | Value | Benefit |
|---------|-------|---------|
| `parallel` | true | Faster builds (multi-threaded) |
| `caching` | true | Reuse task outputs |

**Performance Impact**:
- Parallel: ~30-50% faster on multi-core machines
- Caching: ~40-60% faster on incremental builds
- Combined: ~70% faster overall compared to serial builds

### Android Settings

```gradle
android.useAndroidX=true          # Use AndroidX libraries (not deprecated Support Library)
android.enableJetifier=true       # Convert Support Library to AndroidX
android.suppressUnsupportedCompileSdk=34  # Suppress SDK 34 warnings (8.1.0 limitation)
```

| Setting | Value | Reason |
|---------|-------|--------|
| `useAndroidX` | true | Required for modern Android development |
| `enableJetifier` | true | Compatibility layer for legacy libraries |
| `suppressUnsupportedCompileSdk` | 34 | Work around Gradle plugin limitation |

### Kotlin Settings

```gradle
kotlin.code.style=official
```

- Enforces official Kotlin code style conventions
- Helps with code consistency across team

---

## Android Manifest

### File: `AndroidManifest.xml`

Located at: `d:\code\andriod_tool\SimpleButtonApp\app\src\main\AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>

<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:label="SimpleButtonApp"
        android:theme="@style/Theme.AppCompat.Light">

        <activity
            android:name=".MainActivity"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>

        </activity>

    </application>

</manifest>
```

### Manifest Structure

#### Root Element
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
```
- Namespace declaration for Android attributes

#### Application Tag

```xml
<application
    android:allowBackup="true"
    android:label="SimpleButtonApp"
    android:theme="@style/Theme.AppCompat.Light">
```

| Attribute | Value | Purpose |
|-----------|-------|---------|
| `allowBackup` | true | Allow backup/restore of app data |
| `label` | SimpleButtonApp | App name shown to users |
| `theme` | Theme.AppCompat.Light | Default UI theme |

**allowBackup Explanation**:
- `true`: Android can backup/restore app data
- `false`: Disables backup (security-sensitive apps might use this)
- Modern: Use BackupAgent for fine-grained control

#### Activity Configuration

```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
```

| Attribute | Value | Meaning |
|-----------|-------|---------|
| `name` | .MainActivity | Activity class name (relative to package) |
| `exported` | true | Other apps can launch this activity |

**Exported Activities**:
- `true`: Accessible from other apps/intents (needed for LAUNCHER)
- `false`: Only accessible from within app
- Required for main launcher activity

#### Intent Filter

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN"/>
    <category android:name="android.intent.category.LAUNCHER"/>
</intent-filter>
```

| Element | Meaning |
|---------|---------|
| `action MAIN` | This is the main entry point |
| `category LAUNCHER` | Show in app launcher |

**Intent Filter Purpose**:
- Declares how activity can be started
- MAIN + LAUNCHER = App appears in launcher and starts on tap
- Other combinations: Share, View, etc.

---

## Build Types

### Debug Build
```gradle
// Implicit - always available
buildTypes {
    debug {
        // Default settings:
        // - minifyEnabled false (not obfuscated)
        // - debuggable true (can attach debugger)
        // - signingConfig uses debug keystore
    }
}
```

**Debug Characteristics**:
- ✅ Fast builds
- ✅ Debuggable with IDE
- ✅ Larger APK size
- ✅ Not optimized
- ✅ Includes debugging info

### Release Build
```gradle
buildTypes {
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

**Release Characteristics**:
- ✅ Optimized code
- ✅ Obfuscated
- ✅ Smaller APK size
- ❌ Not debuggable (requires symbols)
- ✅ Production-ready

#### minifyEnabled
```gradle
minifyEnabled true
```

- Enables ProGuard/R8 code obfuscation
- Removes unused code
- Shrinks APK size by 30-50%
- **Debug builds**: Use `false` for faster compilation
- **Release builds**: Use `true` for optimized production APK

#### ProGuard Files
```gradle
proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
```

| File | Source | Purpose |
|------|--------|---------|
| proguard-android-optimize.txt | Android SDK | Default ProGuard rules |
| proguard-rules.pro | Project | Custom rules for app |

---

## Dependencies

### File: `app/build.gradle` - Dependencies Block

```gradle
dependencies {
    // Kotlin Standard Library
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"

    // AndroidX Core Libraries
    implementation "androidx.appcompat:appcompat:1.6.0"
    implementation "androidx.core:core-ktx:1.10.1"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
}
```

### Dependency Types

#### Implementation
```gradle
implementation "androidx.appcompat:appcompat:1.6.0"
```
- Included in APK
- Available to app at runtime
- Transitive: all its dependencies included
- Most common for features

#### Test Implementation
```gradle
testImplementation "junit:junit:4.13.2"
```
- Local unit tests (run on dev machine)
- Not included in APK
- Mocking and testing only

#### Android Test Implementation
```gradle
androidTestImplementation "androidx.test.ext:junit:1.1.5"
```
- Instrumented tests (run on device/emulator)
- Not included in APK
- UI testing with Espresso

### Core Dependencies

#### Kotlin Standard Library
```gradle
implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.0"
```

| Aspect | Details |
|--------|---------|
| Purpose | Kotlin runtime library |
| Requirement | Must match Kotlin compiler version |
| Size Impact | ~1.5 MB |
| Update | With Kotlin version upgrades |

#### AndroidX AppCompat
```gradle
implementation "androidx.appcompat:appcompat:1.6.0"
```

| Aspect | Details |
|--------|---------|
| Purpose | Backward compatibility layer |
| Provides | AppCompatActivity, themes, resources |
| Min API | 14 (Android 4.0) |
| Version | 1.6.0 (tested with SDK 33) |

#### AndroidX Core KTX
```gradle
implementation "androidx.core:core-ktx:1.10.1"
```

| Aspect | Details |
|--------|---------|
| Purpose | Kotlin extension functions |
| Provides | Convenience functions (findViewById→ binding) |
| Min API | 14 (Android 4.0) |
| Version | 1.10.1 (compatible with SDK 33) |

### Testing Dependencies

#### JUnit
```gradle
testImplementation "junit:junit:4.13.2"
```
- Unit testing framework
- Run locally on dev machine
- Mocking and assertions

#### AndroidX Test JUnit
```gradle
androidTestImplementation "androidx.test.ext:junit:1.1.5"
```
- Android test runner
- Instrumented tests on device
- JUnit 4 on Android

#### Espresso
```gradle
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
```
- UI testing framework
- Click, type, verify UI elements
- Functional/integration tests

### Version Pinning

```gradle
dependencies {
    // NOT RECOMMENDED - Flexible versions cause issues
    implementation "androidx.appcompat:appcompat:+" // Latest
    implementation "androidx.appcompat:appcompat:1.6.+" // Patch updates
    
    // RECOMMENDED - Fixed versions for reproducibility
    implementation "androidx.appcompat:appcompat:1.6.0" // Exact version
}
```

**Best Practice**: Always use exact versions for reproducible builds

---

## Resource Configuration

### File: `strings.xml`

Located at: `app/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">SimpleButtonApp</string>
    <string name="initial_status">Press the button</string>
    <string name="button_label">Tap Me</string>
    <string name="click_message">Button pressed %1$d time(s)!</string>
</resources>
```

#### String Resource Types

| Resource | Usage | Example |
|----------|-------|---------|
| Simple | App name | `<string name="app_name">SimpleButtonApp</string>` |
| Formatted | With parameters | `<string name="click_message">Button pressed %1$d time(s)!</string>` |

#### Localization Structure
```
res/
├── values/strings.xml          (English - default)
├── values-es/strings.xml       (Spanish)
├── values-fr/strings.xml       (French)
└── values-de/strings.xml       (German)
```

Android automatically selects based on device language.

### File: `colors.xml`

Located at: `app/src/main/res/values/colors.xml`

```xml
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="button_primary">#FF6200EE</color>
    <color name="button_text">#FFFFFFFF</color>
</resources>
```

#### Color Format
- Hex format: `#AARRGGBB`
- AA = Alpha (FF = opaque, 80 = 50% transparent)
- RR = Red
- GG = Green
- BB = Blue

#### Material Design Colors
- **Purple**: Primary brand color
- **Teal**: Secondary/accent color
- **Black/White**: Text and background

### File: `activity_main.xml`

Located at: `app/src/main/res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="32dp">

    <TextView
        android:id="@+id/statusText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/initial_status"
        android:textSize="22sp"
        android:layout_marginBottom="32dp"/>

    <Button
        android:id="@+id/myButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/button_label"
        android:textSize="18sp"/>

</LinearLayout>
```

#### Layout Attributes

| Attribute | Value | Purpose |
|-----------|-------|---------|
| `layout_width` | match_parent | Full width of container |
| `layout_height` | match_parent | Full height of container |
| `orientation` | vertical | Stack children vertically |
| `gravity` | center | Center children |
| `padding` | 32dp | 32 density-independent pixels inside |

#### View IDs
```xml
android:id="@+id/statusText"  // Unique identifier
android:id="@+id/myButton"    // Used to reference in code
```

Referenced in code as:
```kotlin
val statusText = findViewById<TextView>(R.id.statusText)
val button = findViewById<Button>(R.id.myButton)
```

---

## ProGuard Configuration

### File: `proguard-rules.pro`

Located at: `app/proguard-rules.pro`

```gradle
# Keep Android framework classes
-keep class android.** { *; }
-keep interface android.** { *; }

# Keep AndroidX libraries
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep app package
-keep class com.example.simplebuttonapp.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
```

### ProGuard Rules Explained

#### Keep Classes
```gradle
-keep class android.** { *; }
```
- `keep` = Don't remove or obfuscate
- `class android.**` = All Android classes
- `{ *; }` = Keep all members

#### Remove Logging
```gradle
-assumenosideeffects class android.util.Log {
    public static *** d(...);
}
```
- Removes all Log.d() calls from production
- Reduces APK size and improves performance
- Other log levels (e, w) removed similarly

#### Keep Enums
```gradle
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```
- Preserves enum functionality
- Allows enum.valueOf() to work in production

### ProGuard Processing Steps

1. **Shrinking**: Remove unused code
2. **Obfuscation**: Rename classes/methods to random names
3. **Optimization**: Inline methods, remove dead code
4. **Preverification**: Add type checking (optional)

**Impact**:
- APK size reduction: 30-50%
- Build time increase: 5-10 seconds
- Runtime behavior: Identical (when configured correctly)

---

## Version Reference

### Gradle and Build Tools

| Tool | Version | Compatibility |
|------|---------|---------------|
| Gradle | 8.5 | Latest stable |
| Gradle Plugin | 8.1.0 | Tested up to SDK 33 |
| Android Build Tools | 33.0.1 | Matches compileSdk |
| Kotlin | 1.9.0 | Latest stable |
| Java | 17 | Modern LTS |

### Android SDK Versions

| SDK | Name | API | Released | minSdk? | targetSdk? |
|-----|------|-----|----------|---------|------------|
| 26 | Android 8.0 (Oreo) | 26 | 2017 | ✅ | ❌ |
| 29 | Android 10 | 29 | 2019 | ✅ | ❌ |
| 33 | Android 13 | 33 | 2022 | ❌ | ✅ |

**Our Configuration**: minSdk 26, compileSdk 33, targetSdk 33

### AndroidX Library Versions

| Library | Version | Min SDK | Purpose |
|---------|---------|---------|---------|
| appcompat | 1.6.0 | 14 | Backward compatibility |
| core-ktx | 1.10.1 | 14 | Kotlin extensions |
| junit | 1.1.5 | N/A | Test runner |
| espresso | 3.5.1 | N/A | UI testing |

---

## Configuration Checklist

### Initial Setup
- [ ] `build.gradle` (root) has correct Gradle plugin version
- [ ] `app/build.gradle` specifies namespace
- [ ] `compileSdk`, `targetSdk`, `minSdk` set correctly
- [ ] `sourceCompatibility` and `targetCompatibility` match Java version
- [ ] `gradle.properties` configured with JVM arguments

### Dependencies
- [ ] All dependencies listed in `build.gradle`
- [ ] Version numbers are explicit (not using +)
- [ ] AndroidX used (not Support Library)
- [ ] Testing dependencies included
- [ ] No duplicate dependencies

### Manifest
- [ ] Package name set
- [ ] Activities declared with correct names
- [ ] Intent filters for MAIN/LAUNCHER activity
- [ ] `android:exported="true"` for launcher activity
- [ ] Permissions declared if needed
- [ ] Theme applied to application

### Resources
- [ ] `strings.xml` contains all text strings
- [ ] `colors.xml` has color definitions
- [ ] `activity_main.xml` references string resources
- [ ] No hard-coded strings in layouts
- [ ] No hard-coded strings in code

### Build Configuration
- [ ] ProGuard rules created (`proguard-rules.pro`)
- [ ] Lint checks enabled
- [ ] Build types configured (debug/release)
- [ ] Release build has minifyEnabled true
- [ ] Release build has ProGuard files specified

### Testing
- [ ] Unit test dependencies added
- [ ] Instrumented test dependencies added
- [ ] Test directories created if needed
- [ ] At least one test runs successfully

### Pre-Build Validation
- [ ] Gradle syntax valid (no typos)
- [ ] XML files well-formed
- [ ] All resource IDs referenced correctly
- [ ] No circular dependencies
- [ ] Build succeeds without errors

### Before Release
- [ ] `versionCode` incremented
- [ ] `versionName` updated
- [ ] Release build tested on device
- [ ] ProGuard applied and working
- [ ] APK size verified reasonable
- [ ] No warnings in build output

---

## Troubleshooting Configuration Issues

### Issue: "Could not determine the dependencies"

**Configuration Problem**: Conflicting Gradle versions

**Solution**:
```gradle
// Verify these match:
classpath "com.android.tools.build:gradle:8.1.0"  // in build.gradle
// gradle/wrapper/gradle-wrapper.properties should have:
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### Issue: "Unrecognized VM option 'MaxPermSize'"

**Configuration Problem**: Deprecated JVM argument in gradle.properties

**Solution**:
```gradle
# WRONG:
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m

# CORRECT:
org.gradle.jvmargs=-Xmx2048m
```

### Issue: "Could not compile Java 9+ source"

**Configuration Problem**: SDK too old for Java version

**Solution**:
```gradle
// Don't do this:
compileSdk 29
sourceCompatibility JavaVersion.VERSION_17

// Do this:
compileSdk 33  // Minimum for Java 17
sourceCompatibility JavaVersion.VERSION_17
```

### Issue: Build hangs on "Preparing Android SDK"

**Configuration Problem**: Missing SDK auto-download

**Solution**:
```gradle
// Use already-installed SDK:
compileSdk 33  // Instead of 34

// Or manually install:
// Android Studio > SDK Manager > Select API 34
```

### Issue: R.java not generated

**Configuration Problem**: Namespace or resource issue

**Solution**:
```gradle
// Verify namespace is set:
android {
    namespace 'com.example.simplebuttonapp'
}

// Check resource files are valid XML
// Verify all IDs are unique
// Clean and rebuild:
gradle clean assembleDebug
```

---

## Configuration Performance Tips

### Faster Builds

```gradle
// gradle.properties
org.gradle.parallel=true        # Parallel compilation
org.gradle.caching=true         # Task output caching
org.gradle.jvmargs=-Xmx3048m   # More JVM memory
org.gradle.workers.max=8        # Worker threads
```

**Expected Improvements**:
- Parallel: +30-50% faster
- Caching: +40-60% faster on incremental
- Larger heap: Better for large projects

### Smaller APKs

```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true        # Remove unused resources
        proguardFiles ...
    }
}
```

**Size Reduction**:
- ProGuard: 30-50%
- shrinkResources: Additional 5-10%
- Combined: Often 40-60% reduction

---

## Configuration Backup

### Files to Version Control

```
SimpleButtonApp/
├── build.gradle              ✅ Track
├── settings.gradle           ✅ Track
├── gradle.properties         ✅ Track
├── .gitignore               ✅ Track
├── app/
│   ├── build.gradle         ✅ Track
│   ├── proguard-rules.pro   ✅ Track
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml  ✅ Track
│           ├── kotlin/              ✅ Track
│           └── res/                 ✅ Track
└── .gradle/                 ❌ Don't track (auto-generated)
    build/                   ❌ Don't track (build outputs)
    local.properties         ❌ Don't track (local settings)
```

### Configuration Export

To share configuration with team:

1. **Export settings**:
```bash
gradle exportProperties
```

2. **Document versions**:
```markdown
- Gradle: 8.5
- Gradle Plugin: 8.1.0
- Kotlin: 1.9.0
- Java: 17
- Min SDK: 26
- Target SDK: 33
- Compile SDK: 33
```

3. **Share gradle.properties** (without local paths)

---

**Last Updated**: May 11, 2026  
**Configuration Version**: 1.0  
**Status**: ✅ Production Ready
