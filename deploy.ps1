$ErrorActionPreference = "Stop"
$env:JAVA_HOME='D:\code\andriod_tool\jdk-17.0.10+7'
$env:ANDROID_HOME='D:\code\andriod_tool'
$env:ANDROID_SDK_ROOT='D:\code\andriod_tool'
$env:PATH='D:\code\andriod_tool\platform-tools;D:\code\andriod_tool\jdk-17.0.10+7\bin;D:\code\andriod_tool\gradle\bin;' + $env:PATH

cd D:\code\andriod_tool\homosep
Write-Host "Building debug APK..."
gradle clean assembleDebug

Write-Host "Installing APK on device..."
adb -s 001956547003155 install -r app\build\outputs\apk\debug\app-debug.apk

Write-Host "Launching App..."
adb -s 001956547003155 shell am start -n com.example.homosep/.MainActivity
Write-Host "Deployment complete."
