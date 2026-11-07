# NAGARLENS — Android Application Release & Signing Guide

> **Target Version:** 1.0.0 (Version Code: 100)  
> **Application ID:** `com.nagarlens.app`  

---

## 1. Release Signing & Build Commands

Build Release APK:

```cmd
cd android
set ANDROID_PREFS_ROOT=
gradlew.bat assembleRelease
```

Build Google Play Android App Bundle (AAB):

```cmd
cd android
set ANDROID_PREFS_ROOT=
gradlew.bat bundleRelease
```

Output Artifact Locations:
- **Debug APK**: `android/app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `android/app/build/outputs/apk/release/app-release-unsigned.apk`
- **Release AAB**: `android/app/build/outputs/bundle/release/app-release.aab`
