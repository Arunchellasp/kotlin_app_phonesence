# ProGuard rules for homosep

# Keep Android framework classes
-keep class android.** { *; }
-keep interface android.** { *; }

# Keep AndroidX libraries
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep Google Play Services (Location/GPS)
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep app package
-keep class com.example.homosep.** { *; }

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
